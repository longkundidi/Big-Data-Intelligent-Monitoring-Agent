import torch
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns

def mmd_loss(x, y):
    x_mean = x.mean(0)
    y_mean = y.mean(0)
    return torch.sum((x_mean - y_mean) ** 2)

def set_seed(seed):
    np.random.seed(seed)
    torch.manual_seed(seed)

def plot_reconstruction_error(scores, labels, threshold, fault_name):
    plt.figure(figsize=(8, 5))
    sns.kdeplot(scores[labels == 0], label='Normal', fill=True, color='green')
    sns.kdeplot(scores[labels == 1], label='Fault', fill=True, color='orange')
    plt.axvline(threshold, color='red', linestyle='--', label=f'Threshold: {threshold:.4f}')
    plt.title(f'Reconstruction Error Distribution ({fault_name})')
    plt.legend()
    # 每个任务保存为单独的图片，防止覆盖
    # plt.savefig(f"result_{fault_name}.png")
    plt.close()