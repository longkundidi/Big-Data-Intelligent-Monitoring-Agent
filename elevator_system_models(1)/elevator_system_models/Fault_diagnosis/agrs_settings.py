# coding:utf-8
'''
@Time    : 2025/3/2 23:35
@Author  : dingkun-swjtu
@FileName: agrs_settings.py
@Desc    : 功能描述
'''
import argparse
parser = argparse.ArgumentParser()

def get_args():
    parser.add_argument('--c', type=int, default=8, help="class")
    parser.add_argument('--lr', type=float, default=0.001)
    parser.add_argument('--epochs', type=int, default=100)
    parser.add_argument('--num_workers', type=int, default=0, help='how many subprocesses to use for data loading')
    parser.add_argument('--gpu', type=int, help='ind of gpu', default=0)
    parser.add_argument('--seed', type=int, default=1)
    parser.add_argument('--batch_size', type=int, help='batch_size', default=64)
    parser.add_argument('--input_channel', type=int, help='input_channel', default=1)
    parser.add_argument('--beita', type=int, help='balance coefficient beita', default=1)
    parser.add_argument('--gama', type=int, help='weight coefficient gama', default=1)
    parser.add_argument('--alpha', type=int, help='weight coefficient alpha', default=1)
    parser.add_argument('--sample_dim', type=int, help='sample_dim', default=896)
    parser.add_argument('--hidden_size', type=int, help='hidden_size', default=500)

    # 新增
    parser.add_argument('--test_logs_save_path', type=str, default='./test_logs_saved', help='测试数据特征保存路径')
    parser.add_argument('--Confusion_matrix_save_path', type=str, default='./Confusion_matrix_saved', help='混淆矩阵图片保存路径')
    parser.add_argument('--AUC_ROC_save_path', type=str, default='./AUC_ROC_saved', help='AUC_ROC图片保存路径')
    parser.add_argument('--Attention_weight_save_path', type=str, default='./Attention_weight_saved', help='Attention_weight图片保存路径')
    parser.add_argument('--t_SNE_save_path', type=str, default='./t_SNE_saved', help='t_SNE图片保存路径')
    parser.add_argument('--Model_log_path', type=str, default='./Model_log_saved', help='日志保存路径')