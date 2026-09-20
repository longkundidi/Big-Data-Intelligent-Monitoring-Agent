# coding:utf-8
'''
@Time    : 2025/11/26 17:15
@Author  : dingkun-swjtu
@FileName: data_setting.py
@Desc    : 磨损值预测
'''
import scipy.io as sio
import numpy as np
import torch
import torch.fft as fft





# 对data随机排序
def shuffle_aligned_list(data):
    num = data[0].shape[0]
    shuffle_index = np.random.permutation(num)  # np.random.permutation 对一个list进行随机排序,行数索引随机排序
    return [d[shuffle_index] for d in data]

# batch生成器,对data在一个batchsize下进行随机排序
def batch_generator(data, batch_size, shuffle=True):
    if shuffle:
        data = shuffle_aligned_list(data)  # 对data随机排序
    batch_count = 0  # 计数器置0
    while True:
        if batch_count * batch_size + batch_size >= data[0].shape[0]:
            batch_count = 0
            if shuffle:
                data = shuffle_aligned_list(data)
        start = batch_count * batch_size
        end = start + batch_size
        batch_count += 1
        yield [d[start:end] for d in data]


def balanced_batch_generator(data, batch_size, shuffle=True):
    """
    平衡的batch生成器，确保每个batch都有完整的batch_size个样本
    通过重采样补足不足的样本

    Args:
        data: 包含多个数组的列表 [features, labels, ...]
        batch_size: 每个batch的大小
        shuffle: 是否打乱顺序

    Yields:
        每个batch的数据（始终包含batch_size个样本）
    """
    n_samples = data[0].shape[0]
    batch_count = 0

    # 初始洗牌
    if shuffle:
        data = shuffle_aligned_list(data)

    while True:
        start = batch_count * batch_size

        # 如果起始位置超过样本总数，重置
        if start >= n_samples:
            batch_count = 0
            if shuffle:
                data = shuffle_aligned_list(data)
            start = 0

        end = min(start + batch_size, n_samples)
        batch_count += 1

        # 生成当前batch（可能包含少于batch_size的样本）
        batch_data = [d[start:end] for d in data]

        # 如果当前batch样本数不足，进行重采样补足
        current_batch_size = batch_data[0].shape[0]
        if current_batch_size < batch_size:
            needed_samples = batch_size - current_batch_size

            # 从整个数据集中随机选择额外的样本
            additional_indices = np.random.choice(n_samples, needed_samples, replace=True)
            additional_data = [d[additional_indices] for d in data]

            # 合并原始batch和补充的样本
            batch_data = [
                np.concatenate([batch_d, additional_d], axis=0)
                for batch_d, additional_d in zip(batch_data, additional_data)
            ]

        yield batch_data

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

def normalize_min_max(data):
    """
    对形状为 [N, 1, D] 的张量进行 Min-Max 归一化到 [0, 1]。
    """
    min_val = data.min(dim=0, keepdim=True).values
    max_val = data.max(dim=0, keepdim=True).values

    range_val = max_val - min_val
    range_val = torch.where(range_val == 0, torch.tensor(1.0), range_val)

    data_norm = (data - min_val) / range_val

    return data_norm, (min_val, range_val)

class SignalFeaturesExtractor:
    """
    信号特征提取器 - 提取24个时域和频域特征
    输入形状: [batch_size, 1, seq_len]
    输出形状: [batch_size, 1, 24]
    """
    def __init__(self, sampling_rate=10000, eps=1e-8):
        """
        初始化特征提取器

        参数:
            sampling_rate: 采样频率 (Hz)
            eps: 防止除零的小常数
        """
        self.sampling_rate = sampling_rate
        self.eps = eps

    def extract_features(self, x):
        """
        提取24个时域和频域特征

        参数:
            x: 输入张量, 形状为 [batch_size, 1, seq_len]

        返回:
            features: 特征张量, 形状为 [batch_size, 1, 24]
        """
        batch_size, _, seq_len = x.shape

        # 确保输入是浮点类型
        x = x.float()

        # 计算时域特征
        time_features = self._extract_time_domain_features(x, seq_len)

        # 计算频域特征
        freq_features = self._extract_frequency_domain_features(x, seq_len)

        # 合并特征
        features = torch.cat([time_features, freq_features], dim=-1)

        return features.unsqueeze(1)  # 形状: [batch_size, 1, 24]

    def _extract_time_domain_features(self, x, N):
        """提取11个时域特征 (p1-p11)"""
        batch_size = x.shape[0]

        # 初始化特征张量
        features = torch.zeros(batch_size, 11, device=x.device)

        # p1: 均值
        p1 = torch.mean(x, dim=-1).squeeze()
        features[:, 0] = p1

        # p2: 标准差 (无偏估计)
        p2 = torch.std(x, dim=-1, unbiased=True).squeeze()
        features[:, 1] = p2

        # p3: 方根幅值
        p3 = (torch.mean(torch.sqrt(torch.abs(x)), dim=-1).squeeze()) ** 2
        features[:, 2] = p3

        # p4: 均方根 (RMS)
        p4 = torch.sqrt(torch.mean(x ** 2, dim=-1)).squeeze()
        features[:, 3] = p4

        # p5: 峰值 (绝对值的最大值)
        p5 = torch.max(torch.abs(x), dim=-1)[0].squeeze()
        features[:, 4] = p5

        # p6: 偏度
        p6 = torch.mean(((x - p1.unsqueeze(-1).unsqueeze(-1)) / (p2.unsqueeze(-1).unsqueeze(-1) + self.eps)) ** 3, dim=-1).squeeze()
        features[:, 5] = p6

        # p7: 峰度
        p7 = torch.mean(((x - p1.unsqueeze(-1).unsqueeze(-1)) / (p2.unsqueeze(-1).unsqueeze(-1) + self.eps)) ** 4, dim=-1).squeeze()
        features[:, 6] = p7

        # p8: 峰值因子 = p5 / p4
        p8 = p5 / (p4 + self.eps)
        features[:, 7] = p8

        # p9: 脉冲因子 = p5 / p3
        p9 = p5 / (p3 + self.eps)
        features[:, 8] = p9

        # p10: 波形因子 = p4 / 绝对值的均值
        p10 = p4 / (torch.mean(torch.abs(x), dim=-1).squeeze() + self.eps)
        features[:, 9] = p10

        # p11: 裕度因子 = p5 / 绝对值的均值
        p11 = p5 / (torch.mean(torch.abs(x), dim=-1).squeeze() + self.eps)
        features[:, 10] = p11

        return features

    def _extract_frequency_domain_features(self, x, N):
        """提取13个频域特征 (p12-p24)"""
        batch_size = x.shape[0]

        # 计算FFT和频谱
        x_fft = fft.rfft(x, dim=-1, norm='ortho')
        s_k = torch.abs(x_fft)  # 幅度谱
        K = s_k.shape[-1]  # 频谱线数量

        # 创建频率轴
        f_k = torch.fft.rfftfreq(N, 1.0 / self.sampling_rate).to(x.device)
        f_k = f_k.unsqueeze(0).unsqueeze(0)  # 扩展维度以匹配batch

        # 初始化特征张量
        features = torch.zeros(batch_size, 13, device=x.device)

        # p12: 频谱均值
        p12 = torch.mean(s_k, dim=-1).squeeze()
        features[:, 0] = p12

        # p13: 频谱方差
        p13 = torch.var(s_k, dim=-1, unbiased=True).squeeze()
        features[:, 1] = p13

        # p14: 频谱偏度
        p14 = torch.mean(((s_k - p12.unsqueeze(-1).unsqueeze(-1)) /
                          (torch.sqrt(p13.unsqueeze(-1).unsqueeze(-1)) + self.eps)) ** 3, dim=-1).squeeze()
        features[:, 2] = p14

        # p15: 频谱峰度
        p15 = torch.mean(((s_k - p12.unsqueeze(-1).unsqueeze(-1)) /
                          (p13.unsqueeze(-1).unsqueeze(-1) + self.eps)) ** 4, dim=-1).squeeze()
        features[:, 3] = p15

        # p16: 频率重心
        p16_numerator = torch.sum(f_k * s_k, dim=-1).squeeze()
        p16_denominator = torch.sum(s_k, dim=-1).squeeze()
        p16 = p16_numerator / (p16_denominator + self.eps)
        features[:, 4] = p16

        # p17: 频率标准差
        p17_numerator = torch.sum((f_k - p16.unsqueeze(-1).unsqueeze(-1)) ** 2 * s_k, dim=-1).squeeze()
        p17 = torch.sqrt(p17_numerator / K)
        features[:, 5] = p17

        # p18: 均方根频率
        p18_numerator = torch.sum(f_k ** 2 * s_k, dim=-1).squeeze()
        p18_denominator = torch.sum(s_k, dim=-1).squeeze()
        p18 = torch.sqrt(p18_numerator / (p18_denominator + self.eps))
        features[:, 6] = p18

        # p19: 频率均方根
        p19_numerator = torch.sum(f_k ** 4 * s_k, dim=-1).squeeze()
        p19_denominator = torch.sum(f_k ** 2 * s_k, dim=-1).squeeze()
        p19 = torch.sqrt(p19_numerator / (p19_denominator + self.eps))
        features[:, 7] = p19

        # p20: 频率方差因子
        p20_numerator = torch.sum(f_k ** 2 * s_k, dim=-1).squeeze()
        p20_denominator = torch.sqrt(torch.sum(s_k, dim=-1).squeeze() *
                                     torch.sum(f_k ** 4 * s_k, dim=-1).squeeze())
        p20 = p20_numerator / (p20_denominator + self.eps)
        features[:, 8] = p20

        # p21: 频率偏度因子
        p21 = p18 / (p16 + self.eps)
        features[:, 9] = p21

        # p22: 频率偏度
        p22_numerator = torch.sum((f_k - p16.unsqueeze(-1).unsqueeze(-1)) ** 3 * s_k, dim=-1).squeeze()
        p22 = p22_numerator / (K * (p17 ** 3 + self.eps))
        features[:, 10] = p22

        # p23: 频率峰度
        p23_numerator = torch.sum((f_k - p16.unsqueeze(-1).unsqueeze(-1)) ** 4 * s_k, dim=-1).squeeze()
        p23 = p23_numerator / (K * (p17 ** 4 + self.eps))
        features[:, 11] = p23

        # p24: 频率方根因子
        p24_numerator = torch.sum(torch.sqrt(torch.abs(f_k - p16.unsqueeze(-1).unsqueeze(-1))) * s_k, dim=-1).squeeze()
        p24 = p24_numerator / (K * torch.sqrt(p17 + self.eps))
        features[:, 12] = p24

        return features

def extract_signal_features(x, sampling_rate=10000):
    """
    便捷函数：提取信号特征

    参数:
        x: 输入张量, 形状为 [batch_size, 1, seq_len]
        sampling_rate: 采样频率

    返回:
        features: 特征张量, 形状为 [batch_size, 1, 24]
    """
    extractor = SignalFeaturesExtractor(sampling_rate=sampling_rate)
    return extractor.extract_features(x)


mat_data_45Hz = f'.\mat_data\dataset_45Hz.mat'
mat_data_50Hz = f'.\mat_data\dataset_50Hz.mat'
mat_data_60Hz = f'.\mat_data\dataset_60Hz.mat'

def DATA_SETTING(data_setting):
    S1_DATA = None
    S2_DATA = None
    S3_DATA = None
    T_DATA = None
    transfer_task = None

    freq_map = {
        '45Hz': 45.0,
        '50Hz': 50.0,
        '60Hz': 60.0
    }

    if data_setting == 1:
        S1_DATA = mat_data_50Hz
        S2_DATA = mat_data_60Hz
        T_DATA = mat_data_45Hz
        transfer_task = '1-50Hz_60Hz-45Hz'


    S1_DATA_mat = sio.loadmat(S1_DATA)
    S2_DATA_mat = sio.loadmat(S2_DATA)
    T_DATA_mat = sio.loadmat(T_DATA)

    sample_len = S1_DATA_mat['data'].shape[1]

    S1_data = torch.from_numpy((S1_DATA_mat["data"])).reshape((-1, 1, sample_len))
    S1_label = torch.from_numpy((S1_DATA_mat["label"]))
    S2_data = torch.from_numpy((S2_DATA_mat["data"])).reshape((-1, 1, sample_len))
    S2_label = torch.from_numpy((S2_DATA_mat["label"]))
    T_data = torch.from_numpy((T_DATA_mat["data"])).reshape((-1, 1, sample_len))
    T_label = torch.from_numpy((T_DATA_mat["label"]))



    return [S1_data, S1_label, S2_data, S2_label, T_data, T_label, transfer_task]


