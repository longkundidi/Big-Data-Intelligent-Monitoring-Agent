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

        encoder_layer = nn.TransformerEncoderLayer(d_model=d_model, nhead=8, batch_first=True)
        self.encoder = nn.TransformerEncoder(encoder_layer, num_layers=2)
        self.mask_token = nn.Parameter(torch.zeros(1, 1, d_model))
        self.dec_pos_embed = nn.Parameter(torch.randn(1, self.num_patches, d_model) * 0.02)
        decoder_layer = nn.TransformerEncoderLayer(d_model=d_model, nhead=4, batch_first=True)
        self.decoder = nn.TransformerEncoder(decoder_layer, num_layers=0)
        self.pred_head = nn.Linear(d_model, patch_size)

    def forward(self, x, mask_ratio=0.5):
        target = x.squeeze(1).unfold(1, self.patch_size, self.stride).to(x.device)
        x_patch = self.patch_embed(x).transpose(1, 2) + self.pos_embed

        if mask_ratio > 0:
            batch_size, patch_count, feature_size = x_patch.shape
            keep_count = int(patch_count * (1 - mask_ratio))
            ids_shuffle = torch.argsort(torch.rand(batch_size, patch_count, device=x.device), dim=1)
            ids_restore = torch.argsort(ids_shuffle, dim=1)
            ids_keep = ids_shuffle[:, :keep_count]
            x_masked = torch.gather(
                x_patch,
                dim=1,
                index=ids_keep.unsqueeze(-1).repeat(1, 1, feature_size),
            )
            mask = torch.cat(
                [
                    torch.zeros(batch_size, keep_count, device=x.device),
                    torch.ones(batch_size, patch_count - keep_count, device=x.device),
                ],
                dim=1,
            )
            mask = torch.gather(mask, dim=1, index=ids_restore)
        else:
            x_masked = x_patch
            mask = torch.zeros(x_patch.shape[0], x_patch.shape[1], device=x.device)
            ids_restore = None

        latent = self.encoder(x_masked)

        if mask_ratio > 0:
            mask_tokens = self.mask_token.repeat(latent.shape[0], patch_count - keep_count, 1)
            decoded = torch.cat([latent, mask_tokens], dim=1)
            decoded = torch.gather(
                decoded,
                dim=1,
                index=ids_restore.unsqueeze(-1).repeat(1, 1, decoded.shape[2]),
            )
        else:
            decoded = latent

        prediction = self.pred_head(decoded + self.dec_pos_embed)
        return latent.mean(dim=1), prediction, target, mask
