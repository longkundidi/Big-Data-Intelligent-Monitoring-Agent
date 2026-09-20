# coding:utf-8
'''
@Time    : 2026/7/15 15:47
@Author  : dingkun-swjtu
@FileName: model_test.py
@Desc    : 功能描述
'''
import torch
import os
import numpy as np
import scipy.io as sio
from models import MaskedEPTNet
from sklearn.metrics import roc_auc_score, roc_curve, accuracy_score, precision_score, recall_score, f1_score
DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")


def load_and_validate(model_path, input_x, device=DEVICE):
    """
    加载.pth模型并对测试数据进行验证
    """

    # 加载checkpoint
    checkpoint = torch.load(model_path, map_location=device)

    # 重建模型并加载权重
    model = MaskedEPTNet().to(device)
    model.load_state_dict(checkpoint['model_state_dict'])
    model.eval()

    print(f"📂 已加载模型: {model_path} | Fault: {checkpoint.get('fault_name', 'Unknown')}")

    # 推理 & 计算异常分数
    raw_scores, trues = [], []
    with torch.no_grad():
        input_x = torch.from_numpy(input_x).to(device).float()
        input_x = input_x.reshape([input_x.shape[0], 1, input_x.shape[1]])
        _, pred, target, _ = model(input_x, mask_ratio=0.0)

        return pred, target



# 正常数据
data_dir = r'.\datasets\45hz'
normal_test_path = os.path.join(data_dir, f"Normal_45.mat")
mat = sio.loadmat(normal_test_path)
base_x = mat[[k for k in mat.keys() if not k.startswith('__')][0]]


all_fault_pred = []
faults_name = ['Normal', 'Distance', 'All', 'Lowforce', 'Half', 'Carbon', 'Gap', 'Oil']
for fault_name in faults_name:
    fault_path = os.path.join(data_dir, f"{fault_name}_45.mat")
    mat = sio.loadmat(fault_path)
    fault_x = mat[[k for k in mat.keys() if not k.startswith('__')][0]]

    fault_x = fault_x[:100]

    fault_x_num = fault_x.shape[0]
    task_x = np.concatenate([base_x, fault_x], axis=0)
    task_y = np.array([0] * len(base_x) + [1] * len(fault_x))  # 0=正常, 1=故障

    pred, target = load_and_validate(f"model_saved.pth", task_x)

    score = ((pred - target) ** 2).mean(dim=[1, 2])

    raw_scores = score.cpu().numpy()
    trues = task_y

    raw_scores = np.array(raw_scores)
    trues = np.array(trues)

    normal_scores = raw_scores[trues == 0]

    mu = normal_scores.mean()
    sigma = normal_scores.std() + 1e-8

    scores = np.abs(raw_scores - mu) / sigma

    auc = roc_auc_score(trues, scores)

    fpr, tpr, thresholds = roc_curve(trues, scores)
    j_scores = tpr - fpr
    best_idx = np.argmax(j_scores)

    preds = (scores > thresholds[best_idx]).astype(int)

    test_sample_pred = preds[300:]
    all_fault_pred.append(test_sample_pred)
    pass


pass