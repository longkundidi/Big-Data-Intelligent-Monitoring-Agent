import os
import scipy.io as sio
import numpy as np
import torch
from torch.utils.data import Dataset, DataLoader

class SignalDataset(Dataset):
    def __init__(self, data, labels, domain_labels):
        self.data = torch.tensor(data, dtype=torch.float32).unsqueeze(1)
        self.labels = torch.tensor(labels, dtype=torch.long)
        self.domain_labels = torch.tensor(domain_labels, dtype=torch.long)

    def __len__(self): return len(self.data)
    def __getitem__(self, idx): return self.data[idx], self.labels[idx], self.domain_labels[idx]

def get_1v1_task_loaders(data_dir, all_faults, train_speeds=['50hz, 60hz'], test_speed='45hz', seq_len=1024, batch_size=128):

    train_x, train_y, train_domain = [], [], []
    for d_idx, speed in enumerate(train_speeds):
        s_num = speed.replace('hz', '')
        path = os.path.join(data_dir, speed, f"Normal_{s_num}.mat")
        if not os.path.exists(path): continue
        mat = sio.loadmat(path)
        sigs = mat[[k for k in mat.keys() if not k.startswith('__')][0]]
        if sigs.shape[1] != seq_len: sigs = sigs.T
        train_x.extend(sigs)
        train_y.extend([0] * len(sigs))  # 0=正常
        train_domain.extend([d_idx] * len(sigs))

    train_loader = DataLoader(SignalDataset(np.array(train_x), np.array(train_y), np.array(train_domain)),
                              batch_size=batch_size, shuffle=True)

    test_s_num = test_speed.replace('hz', '')
    normal_test_path = os.path.join(data_dir, test_speed, f"Normal_{test_s_num}.mat")
    mat = sio.loadmat(normal_test_path)
    base_x = mat[[k for k in mat.keys() if not k.startswith('__')][0]]
    if base_x.shape[1] != seq_len: base_x = base_x.T

    for fault_name in [f for f in all_faults if f != 'Normal']:
        fault_path = os.path.join(data_dir, test_speed, f"{fault_name}_{test_s_num}.mat")
        if not os.path.exists(fault_path): continue
        mat = sio.loadmat(fault_path)
        fault_x = mat[[k for k in mat.keys() if not k.startswith('__')][0]]
        if fault_x.shape[1] != seq_len: fault_x = fault_x.T

        task_x = np.concatenate([base_x, fault_x], axis=0)
        task_y = np.array([0] * len(base_x) + [1] * len(fault_x))  # 0=正常, 1=故障
        test_loader = DataLoader(SignalDataset(task_x, task_y, np.zeros(len(task_y))), batch_size=batch_size,
                                 shuffle=False)
        yield fault_name, train_loader, test_loader

