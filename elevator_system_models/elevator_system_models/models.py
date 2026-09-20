import torch
import torch.nn as nn

class MaskedEPTNet(nn.Module):
    def __init__(self, seq_len=1024, patch_size=64, stride=8, d_model=256):
        super().__init__()
        self.patch_size = patch_size
        self.stride = stride
        self.num_patches = (seq_len - patch_size) // stride + 1
        self.patch_embed = nn.Conv1d(1, d_model, kernel_size=patch_size, stride=stride)
        self.pos_embed = nn.Parameter(torch.randn(1, self.num_patches, d_model) * 0.02)

        self.encoder = nn.TransformerEncoder(nn.TransformerEncoderLayer(d_model=d_model, nhead=8, batch_first=True),
                                             num_layers=2)
        self.mask_token = nn.Parameter(torch.zeros(1, 1, d_model))
        self.dec_pos_embed = nn.Parameter(torch.randn(1, self.num_patches, d_model) * 0.02)
        self.decoder = nn.TransformerEncoder(nn.TransformerEncoderLayer(d_model=d_model, nhead=4, batch_first=True),
                                             num_layers=0)
        self.pred_head = nn.Linear(d_model, patch_size)

    def forward(self, x, mask_ratio=0.5):
        target = x.squeeze(1).unfold(1, self.patch_size, self.stride).to(x.device)
        x_patch = self.patch_embed(x).transpose(1, 2) + self.pos_embed

        if mask_ratio > 0:
            N, L, D = x_patch.shape
            len_keep = int(L * (1 - mask_ratio))
            device = x.device
            ids_shuffle = torch.argsort(torch.rand(N, L, device=device), dim=1)
            ids_restore = torch.argsort(ids_shuffle, dim=1)
            ids_keep = ids_shuffle[:, :len_keep]
            x_masked = torch.gather(x_patch, dim=1, index=ids_keep.unsqueeze(-1).repeat(1, 1, D))
            mask = torch.cat([torch.zeros(N, len_keep, device=device), torch.ones(N, L - len_keep, device=device)], dim=1)
            mask = torch.gather(mask, dim=1, index=ids_restore)
        else:
            x_masked = x_patch
            mask = torch.zeros(x_patch.shape[0], x_patch.shape[1], device=x.device)
            ids_restore = None

        latent = self.encoder(x_masked)

        if mask_ratio > 0:
            mask_tokens = self.mask_token.repeat(latent.shape[0], L - len_keep, 1)
            x_ = torch.cat([latent, mask_tokens], dim=1)
            x_ = torch.gather(x_, dim=1, index=ids_restore.unsqueeze(-1).repeat(1, 1, x_.shape[2]))
        else:
            x_ = latent

        pred = self.pred_head(x_ + self.dec_pos_embed)
        return latent.mean(dim=1), pred, target, mask