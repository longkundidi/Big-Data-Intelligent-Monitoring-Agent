import torch
import torch.optim as optim
import numpy as np
from sklearn.metrics import roc_auc_score, roc_curve, accuracy_score, precision_score, recall_score, f1_score

from models import MaskedEPTNet
from data_loader import get_1v1_task_loaders
from utils import set_seed, mmd_loss

DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")


def run_experiments():

    seeds = [42]

    task_names = ['Distance', 'All', 'Lowforce', 'Half', 'Carbon', 'Gap', 'Oil']
    # task_names = ['Distance']

    final_results = {t: [] for t in task_names}

    print("开始训练")

    for seed in seeds:

        set_seed(seed)
        print(f"\n SEED {seed} \n")

        task_gen = get_1v1_task_loaders(
            r'.\datasets',
            task_names,
            train_speeds=['50hz', '60hz'],   # ✅ 双源域恢复
            test_speed='45hz',
        )

        for fault_name, train_loader, test_loader in task_gen:

            print(f"\nSeed {seed} | Task: Normal vs {fault_name} ")

            model = MaskedEPTNet().to(DEVICE)
            optimizer = optim.AdamW(model.parameters(), lr=1e-6)

            epoch_loss_log = []

            model.train()
            for epoch in range(80):

                total_recon = 0.0
                total_mmd = 0.0

                for sigs, _, doms in train_loader:

                    sigs, doms = sigs.to(DEVICE), doms.to(DEVICE)
                    optimizer.zero_grad()

                    latent, pred, target, mask = model(sigs, mask_ratio=0.5)


                    loss_recon = (((pred - target) ** 2) * mask.unsqueeze(-1)).sum() / (
                        mask.sum() * 64 + 1e-5
                    )

                    if (doms == 0).sum() > 0 and (doms == 1).sum() > 0:
                        loss_mmd = mmd_loss(
                            latent[doms == 0],
                            latent[doms == 1]
                        )
                    else:
                        loss_mmd = torch.tensor(0.0, device=DEVICE)


                    loss = loss_recon + 10 * loss_mmd

                    loss.backward()
                    optimizer.step()

                    total_recon += loss_recon.item()
                    total_mmd += loss_mmd.item() if torch.is_tensor(loss_mmd) else 0.0

                epoch_loss_log.append((
                    total_recon / len(train_loader),
                    total_mmd / len(train_loader)
                ))

                if (epoch + 1) % 10 == 0:
                    print(f"[Seed {seed}] Epoch {epoch+1}/80 | "
                          f"Recon: {total_recon/len(train_loader):.4f} | "
                          f"MMD: {total_mmd/len(train_loader):.6f}")

            model.eval()
            raw_scores, trues = [], []

            with torch.no_grad():
                for sigs, labels, _ in test_loader:

                    sigs = sigs.to(DEVICE)

                    _, pred, target, _ = model(sigs, mask_ratio=0.0)

                    score = ((pred - target) ** 2).mean(dim=[1, 2])

                    raw_scores.extend(score.cpu().numpy())
                    trues.extend(labels.numpy())

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

            acc = accuracy_score(trues, preds)
            prec = precision_score(trues, preds, zero_division=0)
            rec = recall_score(trues, preds, zero_division=0)
            f1 = f1_score(trues, preds, zero_division=0)

            print(f"Seed {seed} | {fault_name} | AUC: {auc:.4f} | F1: {f1:.4f}")

            final_results[fault_name].append({
                "auc": auc,
                "f1": f1,
                "acc": acc,
                "prec": prec,
                "rec": rec,
                "loss_curve": epoch_loss_log
            })


            # === 新增：保存模型 ===
            save_path = f"model_saved.pth"
            torch.save({
                'model_state_dict': model.state_dict(),
                'optimizer_state_dict': optimizer.state_dict(),
            }, save_path)
            print(f"✅ 模型已保存至: {save_path}")
            # =======================


        for fault in task_names:

            auc_list = [x["auc"] for x in final_results[fault]]
            f1_list = [x["f1"] for x in final_results[fault]]
            acc_list = [x["acc"] for x in final_results[fault]]
            rec_list = [x["rec"] for x in final_results[fault]]

            print(f"\n----- {fault} -----")

            print(f"AUC : {np.mean(auc_list):.4f} ± {np.std(auc_list):.4f} "
                  f"[{np.min(auc_list):.4f}, {np.max(auc_list):.4f}]")

            print(f"F1  : {np.mean(f1_list):.4f} ± {np.std(f1_list):.4f} "
                  f"[{np.min(f1_list):.4f}, {np.max(f1_list):.4f}]")

            print(f"ACC : {np.mean(acc_list):.4f} ± {np.std(acc_list):.4f} "
                  f"[{np.min(acc_list):.4f}, {np.max(acc_list):.4f}]")

            print(f"REC : {np.mean(rec_list):.4f} ± {np.std(rec_list):.4f} "
                  f"[{np.min(rec_list):.4f}, {np.max(rec_list):.4f}]")




if __name__ == "__main__":
    run_experiments()