import torch
import torch.nn as nn
import torch.nn.functional as F


class FourierUnit1D(nn.Module):
    def __init__(self, in_channels, out_channels, ratio=0.5):
        super().__init__()
        self.l_channels = int(in_channels * ratio)
        self.g_channels = in_channels - self.l_channels

        self.local_conv = nn.Conv1d(
            self.l_channels, self.l_channels, kernel_size=3, stride=1, padding=1
        )
        self.fourier_conv = nn.Conv1d(
            self.g_channels * 2, self.g_channels * 2, kernel_size=1
        )
        self.output_conv = nn.Conv1d(in_channels, out_channels, kernel_size=1)
        self.bn_local = nn.BatchNorm1d(self.l_channels)
        self.bn_global = nn.BatchNorm1d(self.g_channels)
        self.bn_output = nn.BatchNorm1d(out_channels)
        self.relu = nn.ReLU(inplace=True)

    def forward(self, x):
        batch_size, _, sequence_length = x.shape
        local_input = x[:, : self.l_channels, :]
        global_input = x[:, self.l_channels :, :]

        local_output = self.relu(self.bn_local(self.local_conv(local_input)))
        if self.g_channels > 0:
            global_fft = torch.fft.rfft(global_input, dim=2, norm="ortho")
            frequency_features = torch.cat([global_fft.real, global_fft.imag], dim=1)
            frequency_output = self.fourier_conv(frequency_features)
            real_output, imaginary_output = torch.chunk(frequency_output, 2, dim=1)
            global_output = torch.fft.irfft(
                torch.complex(real_output, imaginary_output),
                n=sequence_length,
                dim=2,
                norm="ortho",
            )
            global_output = self.relu(self.bn_global(global_output))
        else:
            global_output = torch.zeros(
                batch_size, 0, sequence_length, device=x.device, dtype=x.dtype
            )

        combined = torch.cat([local_output, global_output], dim=1)
        return self.relu(self.bn_output(self.output_conv(combined)))


class FFCBlock(nn.Module):
    def __init__(self, channels, ratio=0.5):
        super().__init__()
        self.ffc1 = FourierUnit1D(channels, channels, ratio)
        self.ffc2 = FourierUnit1D(channels, channels, ratio)
        self.shortcut = nn.Identity()

    def forward(self, x):
        residual = x
        x = self.ffc1(x)
        x = self.ffc2(x)
        return F.relu(x + self.shortcut(residual))


class FFCNet(nn.Module):
    def __init__(
        self,
        input_channels=1,
        num_classes=8,
        seq_length=1024,
        base_channels=32,
        num_blocks=4,
        ffc_ratio=0.5,
    ):
        super().__init__()
        self.initial_conv = nn.Sequential(
            nn.Conv1d(input_channels, base_channels, kernel_size=7, stride=2, padding=3),
            nn.BatchNorm1d(base_channels),
            nn.ReLU(inplace=True),
            nn.MaxPool1d(kernel_size=2, stride=2),
        )

        self.ffc_blocks = nn.ModuleList()
        current_channels = base_channels
        for block_index in range(num_blocks):
            self.ffc_blocks.append(FFCBlock(current_channels, ffc_ratio))
            if block_index in (1, 3):
                self.ffc_blocks.append(nn.MaxPool1d(kernel_size=2, stride=2))

        self.adaptive_pool = nn.AdaptiveAvgPool1d(16)
        self.classifier = nn.Sequential(
            nn.Linear(current_channels * 16, 256),
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
            nn.Linear(64, num_classes),
        )

    def forward(self, x):
        x = self.initial_conv(x)
        for block in self.ffc_blocks:
            x = block(x)
        x = self.adaptive_pool(x)
        x = x.reshape(x.size(0), -1)
        return self.classifier(x)
