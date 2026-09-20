# coding:utf-8
'''
@Time    : 2025/12/1 19:26
@Author  : dingkun-swjtu
@FileName: FFC_20251201.py
@Desc    : 功能描述
'''
import os

import torch
import torch.nn as nn
import torch.nn.functional as F
import numpy as np
import matplotlib.pyplot as plt
import time
import warnings
from datetime import datetime
import logging
from agrs_settings import *
import math
import gc
from data_setting import DATA_SETTING, batch_generator, balanced_batch_generator


class SupConLoss(nn.Module):
    def __init__(self, temperature=0.3):
        super().__init__()
        self.temperature = temperature

    def forward(self, projections, labels):
        device = projections.device
        mask = torch.eq(labels.unsqueeze(1), labels.unsqueeze(0)).float().to(device)
        norm_proj = F.normalize(projections, dim=1)
        sim_matrix = torch.matmul(norm_proj, norm_proj.T) / self.temperature

        # 排除对角线自身比较
        logits_mask = torch.ones_like(mask).fill_diagonal_(0)
        pos_mask = mask * logits_mask
        neg_mask = 1 - mask

        # 计算对比损失
        exp_logits = torch.exp(sim_matrix) * logits_mask
        log_prob = sim_matrix - torch.log(exp_logits.sum(dim=1, keepdim=True))
        loss = - (pos_mask * log_prob).sum(1) / pos_mask.sum(1)
        return loss.mean()


def min_max_normalize(tensor, feature_range=(-1, 1)):
    """
    最小-最大标准化，将数据缩放到指定范围[-1, 1]

    参数:
        tensor: 输入张量，形状为[1000, 1, 6000]
        feature_range: 缩放的目标范围，默认为(-1, 1)

    返回:
        标准化后的张量
    """
    # 计算每个样本的最小值和最大值（沿最后一个维度dim=2）
    x_min = tensor.min(dim=2, keepdim=True).values  # 形状变为[1000, 1, 1]
    x_max = tensor.max(dim=2, keepdim=True).values  # 形状变为[1000, 1, 1]

    # 防止除零错误
    range_diff = x_max - x_min
    range_diff[range_diff == 0] = 1.0  # 如果最大最小值相等，设为1避免除零

    # 最小-最大标准化公式
    normalized = (tensor - x_min) / range_diff

    # 缩放到目标范围
    a, b = feature_range
    scaled_tensor = normalized * (b - a) + a

    return scaled_tensor

def zero_normalize(tensor, feature_range=(-1, 1)):
    """
    去除零漂

    参数:
        tensor: 输入张量，形状为[1000, 1, 6000]
        feature_range: 缩放的目标范围，默认为(-1, 1)

    返回:
        标准化后的张量
    """
    # 计算每个样本的均值（沿最后一个维度dim=2）
    mean = tensor.mean(dim=2, keepdim=True)  # 形状变为[1000, 1, 1]

    # Z-score标准化公式
    standardized_tensor = (tensor - mean)

    return standardized_tensor


def z_score_normalize(tensor, eps=1e-8):
    """
    Z-score标准化（标准差标准化），使数据均值为0，标准差为1

    参数:
        tensor: 输入张量，形状为[1000, 1, 6000]
        eps: 防止除零错误的小常数

    返回:
        标准化后的张量
    """
    # 计算每个样本的均值和标准差（沿最后一个维度dim=2）
    mean = tensor.mean(dim=2, keepdim=True)  # 形状变为[1000, 1, 1]
    std = tensor.std(dim=2, keepdim=True)  # 形状变为[1000, 1, 1]

    # Z-score标准化公式
    standardized_tensor = (tensor - mean) / (std + eps)

    return standardized_tensor


def normalize_tensor(tensor, method='zscore', **kwargs):
    """
    统一的标准化函数，可选择不同的标准化方法

    参数:
        tensor: 输入张量，形状为[1000, 1, 6000]
        method: 标准化方法，可选 'zscore' 或 'minmax'
        **kwargs: 传递给具体标准化函数的参数

    返回:
        标准化后的张量
    """
    if method == 'minmax':
        return min_max_normalize(tensor, **kwargs)
    elif method == 'zscore':
        return z_score_normalize(tensor, **kwargs)
    elif method == 'zero_shift':
        return zero_normalize(tensor, **kwargs)
    else:
        raise ValueError("method参数必须是 'zscore' 或 'minmax' 或 'zero_shift'")

class FourierUnit1D(nn.Module):
    """一维傅里叶单元 - FFC的核心组件"""

    def __init__(self, in_channels, out_channels, ratio=0.5):
        super(FourierUnit1D, self).__init__()
        self.ratio = ratio  # 局部路径所占通道比例，默认0.5表示空间域和频域各占一半通道

        # 计算各路径的通道数
        self.l_channels = int(in_channels * ratio)  # 局部路径（空间域）
        # 局部路径(空间域)分配的通道数
        # 例如 in_channels=32, ratio=0.5 → l_channels=16
        self.g_channels = in_channels - self.l_channels  # 全局路径（频域）
        # 全局路径(频域)分配的通道数 = 总通道数 - 局部通道数
        # 保证两条路径通道数之和恰好等于输入通道数

        # ========== 局部路径：常规空间卷积 ==========
        self.local_conv = nn.Conv1d(self.l_channels, self.l_channels,
                                    kernel_size=3, stride=1, padding=1)

        # ========== 全局路径：频域卷积 ==========
        self.fourier_conv = nn.Conv1d(self.g_channels * 2, self.g_channels * 2,
                                      kernel_size=1, stride=1, padding=0)

        # 关键设计：通道数乘以2
        # 原因：FFT结果是复数，拆分为实部+虚部后在通道维度拼接
        # 所以频域特征的通道数 = g_channels(实部) + g_channels(虚部) = g_channels*2
        # kernel_size=1：在频域做逐点卷积，等价于对每个频率分量做线性变换
        # 这相当于学习一个可训练的频域滤波器

        # 输出卷积，融合两条路径的特征
        self.output_conv = nn.Conv1d(in_channels, out_channels,
                                     kernel_size=1, stride=1, padding=0)
        # 将两条路径拼接后的特征(in_channels)映射到目标通道数(out_channels)
        # 1×1卷积起到跨通道信息融合和维度变换的作用

        self.bn_local = nn.BatchNorm1d(self.l_channels)
        # 局部路径的批归一化
        self.bn_global = nn.BatchNorm1d(self.g_channels)
        # 全局路径的批归一化（注意：是对g_channels而非g_channels*2做BN）
        # BN在逆FFT之后、ReLU之前应用，作用于空间域信号
        self.bn_output = nn.BatchNorm1d(out_channels)
        # 输出融合后的批归一化
        self.relu = nn.ReLU(inplace=True)

    def forward(self, x):
        batch_size, channels, seq_len = x.shape

        # 分割输入到局部和全局路径
        x_local = x[:, :self.l_channels, :]  # 形状: [B, l_channels, L]
        x_global = x[:, self.l_channels:, :]  # 形状: [B, g_channels, L]

        # 局部路径：常规空间卷积
        local_out = self.local_conv(x_local)  # 空间卷积提取局部时序特征
        local_out = self.bn_local(local_out)
        local_out = self.relu(local_out)  # 输出形状: [B, l_channels, L]

        # 全局路径：傅里叶域处理
        if self.g_channels > 0:
            # --- Step 1: 正傅里叶变换 ---
            x_global_fft = torch.fft.rfft(x_global, dim=2, norm='ortho')
            # rfft: 对实数信号做FFT，只返回正频率部分（共L//2+1个频率点）
            # dim=2: 沿序列长度维度做变换
            # norm='ortho': 正交归一化，使正逆变换能量守恒，训练更稳定
            # 输出形状: [B, g_channels, L//2+1]，dtype=torch.complex64

            # --- Step 2: 复数拆分为实部+虚部 ---
            real_part = x_global_fft.real
            imag_part = x_global_fft.imag
            # 分别提取复数张量的实部和虚部
            # 各自形状: [B, g_channels, L//2+1]

            freq_features = torch.cat([real_part, imag_part], dim=1)
            # 在通道维度拼接实部和虚部
            # 形状: [B, g_channels*2, L//2+1]
            # 这样就将复数运算转化为标准实数卷积可处理的形式

            # --- Step 3: 频域卷积（可学习滤波器） ---
            freq_processed = self.fourier_conv(freq_features)
            # 1×1卷积对每个频率点的[实部;虚部]向量做线性变换
            # 等价于学习一个2×2的复数乘法矩阵作用于每个频率分量
            # 形状: [B, g_channels*2, L//2+1]

            # --- Step 4: 重新组合为复数 ---
            real_processed, imag_processed = torch.chunk(freq_processed, 2, dim=1)
            # 沿通道维度均分为两半，前半为处理后的实部，后半为虚部
            # 各自形状: [B, g_channels, L//2+1]

            global_out_fft = torch.complex(real_processed, imag_processed)
            # 将实部和虚部重新组合为复数张量
            # 形状: [B, g_channels, L//2+1], dtype=torch.complex64

            # --- Step 5: 逆傅里叶变换回空间域 ---
            global_out = torch.fft.irfft(global_out_fft, n=seq_len, dim=2, norm='ortho')
            # irfft: 逆FFT，从频域恢复到时域
            # n=seq_len: 指定输出长度，确保与原始序列长度一致
            # norm='ortho': 与正变换配对，保证完美重构
            # 输出形状: [B, g_channels, L]，dtype=torch.float32

            # --- Step 6: 归一化与激活 ---
            global_out = self.bn_global(global_out)
            global_out = self.relu(global_out)
            # 在空间域对恢复的信号做BN和ReLU
        else:
            global_out = torch.zeros(batch_size, 0, seq_len, device=x.device)
            # 如果g_channels=0（全部通道给了局部路径），创建空张量占位
            # 保证后续cat操作不会报错

        # ========== 双路径特征融合 ==========
        x_combined = torch.cat([local_out, global_out], dim=1)
        # 在通道维度拼接局部和全局特征
        # 形状: [B, l_channels+g_channels, L] = [B, in_channels, L]

        # ========== 输出变换 ==========
        output = self.output_conv(x_combined)
        # 1×1卷积融合双路径信息并调整通道数
        output = self.bn_output(output)
        output = self.relu(output)  # 输出形状: [B, out_channels, L]

        return output


class FFCBlock(nn.Module):
    """完整的FFC块，包含残差连接"""

    def __init__(self, channels, ratio=0.5):
        super(FFCBlock, self).__init__()
        self.ffc1 = FourierUnit1D(channels, channels, ratio)
        self.ffc2 = FourierUnit1D(channels, channels, ratio)

        # 残差连接
        self.shortcut = nn.Identity()

    def forward(self, x):
        residual = x
        x = self.ffc1(x)
        x = self.ffc2(x)
        x = x + self.shortcut(residual)  # 数学表达: y = F(x) + x
        return F.relu(x)


class FFCNet(nn.Module):
    """基于FFC的时序数据分类网络"""

    def __init__(self, input_channels=1, num_classes=1, seq_length=1024,
                 base_channels=32, num_blocks=4, ffc_ratio=0.5):
        # 初始化函数（构造函数）
        # input_channels: 输入信号的通道数，默认为1（单变量时序信号）
        # num_classes: 分类类别数，默认为1（注意：在main函数中实际被覆盖为8）
        # seq_length: 输入序列的长度，默认1024
        # base_channels: 基础卷积层的输出通道数，默认32
        # num_blocks: FFC残差块的数量，默认4个
        # ffc_ratio: FFC单元中局部路径(空间域)所占的通道比例，默认0.5表示一半通道走CNN，一半走FFT

        super(FFCNet, self).__init__()

        # 初始卷积层
        self.initial_conv = nn.Sequential(
            nn.Conv1d(input_channels, base_channels, kernel_size=7, stride=2, padding=3),
            nn.BatchNorm1d(base_channels),
            nn.ReLU(inplace=True),
            nn.MaxPool1d(kernel_size=2, stride=2)
        )

        # 计算经过初始卷积后的序列长度
        initial_seq_len = seq_length // 4 # 记录经过初始卷积+池化后的序列长度，用于后续跟踪尺寸变化

        # FFC块序列
        self.ffc_blocks = nn.ModuleList()
        current_channels = base_channels

        for i in range(num_blocks):
            self.ffc_blocks.append(FFCBlock(current_channels, ffc_ratio))

            # 在特定层后下采样
            if i in [1, 3]:
                self.ffc_blocks.append(nn.MaxPool1d(kernel_size=2, stride=2))
                initial_seq_len = initial_seq_len // 2

        # 自适应池化到固定大小
        self.adaptive_pool = nn.AdaptiveAvgPool1d(16) # 自适应平均池化：无论输入序列多长，都强制输出固定长度16  这消除了对固定输入长度的依赖，增强了模型的泛化能力

        # 计算全连接层输入尺寸
        self.fc_input_size = current_channels * 16  # 展平后的特征维度 = 通道数 × 池化后的固定长度

        # 分类器
        self.classifier = nn.Sequential(
            nn.Linear(self.fc_input_size, 256),
            nn.BatchNorm1d(256),
            nn.ReLU(inplace=True),
            nn.Dropout(0.3),

            nn.Linear(256, 128),
            nn.BatchNorm1d(128),
            nn.ReLU(inplace=True),
            nn.Dropout(0.2),

            nn.Linear(128, 64),
            nn.BatchNorm1d(64),
            nn.ReLU(inplace=True),
            nn.Dropout(0.1),

            nn.Linear(64, num_classes)
        )


    def forward(self, x):
        # 初始特征提取
        x = self.initial_conv(x)

        # 通过FFC块
        for block in self.ffc_blocks:
            x = block(x)

        # 全局平均池化
        x = self.adaptive_pool(x)

        # 展平
        x = x.view(x.size(0), -1)

        # 分类
        x = self.classifier(x)

        return x


def plot_training_curves(trainer):
    """绘制训练曲线"""
    plt.figure(figsize=(15, 5))

    plt.subplot(1, 2, 1)
    plt.plot(trainer.train_losses, label='Training Loss', linewidth=2)
    plt.plot(trainer.val_losses, label='Validation Loss', linewidth=2)
    plt.xlabel('Epoch')
    plt.ylabel('Loss')
    plt.legend()
    plt.title('Training and Validation Loss')
    plt.grid(True, alpha=0.3)
    plt.yscale('log')

    plt.subplot(1, 2, 2)
    # 绘制最后50个epoch的损失
    if len(trainer.train_losses) > 50:
        plt.plot(range(len(trainer.train_losses) - 50, len(trainer.train_losses)),
                 trainer.train_losses[-50:], label='Training Loss (last 50)')
        plt.plot(range(len(trainer.val_losses) - 50, len(trainer.val_losses)),
                 trainer.val_losses[-50:], label='Validation Loss (last 50)')
        plt.xlabel('Epoch')
        plt.ylabel('Loss')
        plt.legend()
        plt.title('Last 50 Epochs')
        plt.grid(True, alpha=0.3)

    plt.tight_layout()
    plt.show()


def plot_predictions_vs_targets(results, num_samples=20):
    """绘制预测值与真实值的对比"""
    predictions = results['predictions'].numpy().flatten()
    targets = results['targets'].numpy().flatten()

    plt.figure(figsize=(15, 5))

    plt.subplot(1, 2, 1)
    # 散点图
    plt.scatter(targets, predictions, alpha=0.6, s=50)
    plt.plot([targets.min(), targets.max()], [targets.min(), targets.max()], 'r--', linewidth=2)
    plt.xlabel('True Values')
    plt.ylabel('Predictions')
    plt.title(f'Predictions vs True Values\nR² = {results["r2"]:.4f}')
    plt.grid(True, alpha=0.3)

    plt.subplot(1, 2, 2)
    # 误差分布
    errors = predictions - targets
    plt.hist(errors, bins=30, alpha=0.7, edgecolor='black')
    plt.xlabel('Prediction Error')
    plt.ylabel('Frequency')
    plt.title(f'Error Distribution\nMAE = {results["mae"]:.4f}')
    plt.grid(True, alpha=0.3)

    plt.tight_layout()
    plt.show()

    print(f"R² Score: {results['r2']:.4f}")
    print(f"MAE: {results['mae']:.4f}")
    print(f"MSE: {results['mse']:.4f}")
    print(f"Monotonicity Ratio: {results['monotonic_ratio']:.4f}")


def analyze_frequency_components(model, sample_data, device):
    """分析模型学到的频率成分"""
    model.eval()
    with torch.no_grad():
        sample_data = sample_data.to(device)

        # 获取中间特征
        activations = {}

        def get_activation(name):
            def hook(model, input, output):
                activations[name] = output.detach()

            return hook

        # 注册钩子来获取中间层输出
        hooks = []
        for name, layer in model.named_modules():
            if isinstance(layer, FourierUnit1D):
                hooks.append(layer.register_forward_hook(get_activation(name)))

        # 前向传播
        output = model(sample_data.unsqueeze(0))

        # 移除钩子
        for hook in hooks:
            hook.remove()

        # 分析频率成分
        plt.figure(figsize=(15, 10))
        for i, (name, activation) in enumerate(activations.items()):
            if i >= 6:  # 只显示前6层
                break

            plt.subplot(2, 3, i + 1)
            # 计算频谱
            freq_activation = torch.fft.rfft(activation.squeeze(), dim=1)
            freq_magnitude = torch.abs(freq_activation).mean(0).cpu().numpy()

            plt.plot(freq_magnitude[:])  # 只显示前100个频率分量
            plt.title(f'{name} Frequency Response')
            plt.xlabel('Frequency')
            plt.ylabel('Magnitude')
            plt.grid(True, alpha=0.3)

        plt.tight_layout()
        plt.show()


def main(args, run_id, task_info, task_id, save_logs, plot_figs):
    S1_data, S1_label, S2_data, S2_label, T_data, T_label, transfer_task = task_info
    print('S1_train_size:{}  S2_train_size:{}   test_size:{}'.format(S1_data.shape[0], S2_data.shape[0], T_data.shape[0]))
    logger.info('S1_train_size:{}  S2_train_size:{}  test_size:{}'.format(S1_data.shape[0], S2_data.shape[0], T_data.shape[0]))


    sample_len = S1_data.shape[1]
    print("初始化FFC模型...")
    model = FFCNet(
        input_channels=1,
        num_classes=8,
        seq_length=sample_len,
        base_channels=32,
        num_blocks=4,
        ffc_ratio=0.5
    )

    print(f"模型参数量: {sum(p.numel() for p in model.parameters()):,}")

    print("开始训练...")

    device = 'cuda' if torch.cuda.is_available() else 'cpu'
    model = model.to(device)

    CE_loss = nn.CrossEntropyLoss().to(device)

    optimizer = torch.optim.Adam(model.parameters(), lr=args.lr, weight_decay=1e-4)
    scheduler = torch.optim.lr_scheduler.ReduceLROnPlateau(
        optimizer, mode='min', factor=0.5, patience=10, verbose=True
    )

    # 计算最大迭代次数（基于最大样本数的源域）
    max_samples = max(S1_data.shape[0], S2_data.shape[0])
    iters = math.ceil(max_samples / args.batch_size)

    criterion_con = SupConLoss()

    best_acc = 0

    for epoch in range(args.epochs):
        print("----------第{}轮训练开始----------".format(epoch + 1))
        # 训练阶段
        model.train()
        train_loss = 0
        train_acc = 0
        train_total = 0
        total_train_step = 0
        valid_acc, valid_total, valid_loss = 0, 0, 0

        S1_batches = balanced_batch_generator([S1_data, S1_label], args.batch_size, shuffle=True)
        S2_batches = balanced_batch_generator([S2_data, S2_label], args.batch_size, shuffle=True)

        for batch_idx in range(0, iters):
            xs1_batch, ys1_batch = next(S1_batches)
            xs2_batch, ys2_batch = next(S2_batches)

            xs_batch = torch.from_numpy(np.vstack([xs1_batch, xs2_batch])).to(device).float()
            ys_batch = torch.from_numpy(np.hstack([np.argmax(ys1_batch, axis=-1), np.argmax(ys2_batch, axis=-1)])).to(device).long()

            optimizer.zero_grad()
            y_pred = model(xs_batch)


            # 分类损失
            cls_loss = CE_loss(y_pred, ys_batch)

            loss_align = criterion_con(y_pred, ys_batch)

            # 总损失
            total_loss = cls_loss + 0.01 * loss_align

            total_loss.backward()
            torch.nn.utils.clip_grad_norm_(model.parameters(), max_norm=1.0)
            optimizer.step()

            predicted = torch.argmax(y_pred, 1)
            train_acc = train_acc + (predicted == ys_batch).sum().item()
            train_total = train_total + ys_batch.size(0)
            train_loss = train_loss + total_loss.item()
            total_train_step = total_train_step + 1


            if batch_idx % 10 == 0 or batch_idx == (iters - 1):
                print('Task_id: {}\tTask_info: {}\tRun_iter: {}\tEpoch_iter: {}\tBatch_iter: {} [({:.0f}%)]\tce_all_loss: {:.6f}\talign_loss: {:.6f}\ttotall_loss: {:.6f}'.format(
                            task_id, transfer_task, run_id + 1, epoch, batch_idx, 100. * batch_idx / (iters - 1), cls_loss, loss_align, total_loss))
                logger.info('Task_id: {}\tTask_info: {}\tRun_iter: {}\tEpoch_iter: {}\tBatch_iter: {} [({:.0f}%)]\tce_all_loss: {:.6f}\ttotall_loss: {:.6f}'.format(
                            task_id, transfer_task, run_id + 1, epoch, batch_idx, 100. * batch_idx / (iters - 1), cls_loss, total_loss))


        train_loss = train_loss / total_train_step
        train_accuracy = train_acc / train_total
        print("train_Loss:{:.6f}".format(train_loss))
        print("train_acc：{:.5f}".format(train_accuracy))
        logger.info("train_Loss:{:.6f}".format(train_loss))
        logger.info("train_acc：{:.5f}".format(train_accuracy))

        scheduler.step(train_loss)

        T_batches = batch_generator([T_data, T_label], T_data.shape[0], shuffle=False)
        xt_batch, yt_batch = next(T_batches)


        xt_batch = xt_batch.to(device).float()
        yt_batch_argmax = torch.argmax(yt_batch, dim=-1)
        yt_batch_argmax = yt_batch_argmax.to(device).long()

        print("评估模型性能...")
        model.eval()
        with torch.no_grad():
            y_pred_test = model(xt_batch)
        test_loss = CE_loss(y_pred_test, yt_batch_argmax)
        y_pred_test_argmax = torch.argmax(y_pred_test, 1)  # .data
        valid_acc = valid_acc + (y_pred_test_argmax == yt_batch_argmax).sum().item()
        valid_total = valid_total + yt_batch.shape[0]
        valid_loss = valid_loss + test_loss.item()
        valid_accuracy = valid_acc / valid_total
        valid_loss = valid_loss / len(xt_batch)


        print("test_Loss:{:.5f}".format(valid_loss))
        print("test_acc：{:.5f}".format(valid_accuracy))
        print("best_test_acc：{:.5f}".format(best_acc))
        logger.info("test_Loss:{:.5f}".format(valid_loss))
        logger.info("test_acc：{:.5f}".format(valid_accuracy))
        logger.info("best_test_acc：{:.5f}".format(best_acc))
        if valid_accuracy > best_acc:
            best_acc = valid_accuracy

            # === 新增：保存模型 ===
            save_path = f"model_saved.pth"
            torch.save({
                'model_state_dict': model.state_dict(),
                'optimizer_state_dict': optimizer.state_dict()
            }, save_path)
            print(f"✅ 模型已保存至: {save_path}")

    print('best_acc: %.9f' % (best_acc * 100))
    logger.info('best_acc: %.9f' % (best_acc * 100))

    del model
    del optimizer
    del S1_batches
    del S2_batches
    del xt_batch
    del yt_batch
    # 清空 CUDA 缓存
    if torch.cuda.is_available():
        torch.cuda.empty_cache()
    # 强制垃圾回收
    gc.collect()


    return best_acc * 100

if __name__ == "__main__":
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    # 记录开始时间
    time_strat = time.localtime()
    time_strat_str = time.strftime("%Y-%m-%d %H:%M:%S", time_strat)
    warnings.filterwarnings('ignore')  # 忽略 warnning
    get_args()
    args = parser.parse_args()

    # 日志记录
    date_now = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
    logger = logging.getLogger("main")
    logger.setLevel(logging.DEBUG)
    if os.path.exists(args.Model_log_path) == False:
        os.makedirs(args.Model_log_path)
    file_handler = logging.FileHandler(f"{args.Model_log_path}/main_{date_now}.log")
    formatter = logging.Formatter("%(asctime)s - %(levelname)s - %(message)s")
    file_handler.setFormatter(formatter)
    logger.addHandler(file_handler)

    # 设置随机种子
    torch.manual_seed(42)
    np.random.seed(42)

    N_train_repeat = 1  # 训练次数

    task_index = 1
    task_info = DATA_SETTING(task_index)

    acc_record = []
    logger.info(r'----------------------------------------------------------------------------------')

    for i in range(N_train_repeat):
        logger.info(f'task_id: {task_index + 1}, task_info: {task_info[-1]}, run_id: {i + 1}  开始')
        best_acc = main(args=args, task_info=task_info, task_id=(task_index + 1), run_id=i, save_logs=True, plot_figs=False)
        acc_record.append(best_acc)

        logger.info(f'task_id: {task_index + 1}, task_info: {task_info[-1]}, run_id: {i + 1}  结束')

    print(f'task_id: {task_index + 1}, task_info: {task_info[-1]}, acc: {np.array(acc_record)}, acc_mean: {np.mean(np.array(acc_record))}, acc_std: {np.std(np.array(acc_record))}')
    logger.info(f'task_info: {task_info[-1]}, acc: {np.array(acc_record)}, acc_mean: {np.mean(np.array(acc_record))}, acc_std: {np.std(np.array(acc_record))}')