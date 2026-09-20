# coding:utf-8
'''
@Time    : 2026/7/15 22:53
@Author  : dingkun-swjtu
@FileName: model_test.py
@Desc    : 功能描述
'''
import torch
import os
import numpy as np
import scipy.io as sio
from main import FFCNet
from sklearn.metrics import roc_auc_score, roc_curve, accuracy_score, precision_score, recall_score, f1_score
DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")


def load_and_validate(model_path, input_x, device=DEVICE):
    """
    加载.pth模型并对测试数据进行验证
    """

    # 加载checkpoint
    checkpoint = torch.load(model_path, map_location=device)

    # 重建模型并加载权重
    model = FFCNet(
        input_channels=1,
        num_classes=8,
        seq_length=1024,
        base_channels=32,
        num_blocks=4,
        ffc_ratio=0.5
    ).to(device)

    model.load_state_dict(checkpoint['model_state_dict'])
    model.eval()

    print(f"📂 已加载模型: {model_path}")

    with torch.no_grad():
        input_x = torch.from_numpy(input_x).to(device).float()
        input_x = input_x.reshape([input_x.shape[0], 1, input_x.shape[1]])
        y_pred = model(input_x)

        return y_pred



data_dir = r'.\datasets\45hz'
all_fault_pred = []
faults_name = ['Normal', 'Half', 'All',  'Oil', 'Carbon', 'Lowforce', 'Gap', 'Distance']
for fault_name in faults_name:
    fault_path = os.path.join(data_dir, f"{fault_name}_45.mat")
    mat = sio.loadmat(fault_path)
    fault_x = mat[[k for k in mat.keys() if not k.startswith('__')][0]]

    fault_x1 = fault_x[:100]

    y_pred = load_and_validate(f"model_saved.pth", fault_x1)
    y_class_pred = torch.argmax(y_pred, dim=1)  #  对应预测标签，0表示“正常”，1表示“闸瓦表面部分磨损”，2表示“闸瓦表面全磨损”，3表示“闸瓦接触面存在油污”
                                                #  4表示“闸瓦接触面存在异物”，5表示“弹簧提供的制动力不足”，6表示“闸瓦和制动轮间隙过大”，7表示“闸瓦和制动轮未紧密贴合”
    all_fault_pred.append(y_class_pred)

pass