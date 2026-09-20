from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter


ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "public" / "img" / "wel" / "bg.png"
DST = ROOT / "public" / "img" / "wel" / "bg-elevator.png"


def lerp(a, b, t):
    return int(a + (b - a) * t)


def vertical_gradient(size, top_color, bottom_color):
    width, height = size
    img = Image.new("RGBA", size)
    px = img.load()
    for y in range(height):
        t = y / max(1, height - 1)
        color = tuple(lerp(top_color[i], bottom_color[i], t) for i in range(4))
        for x in range(width):
            px[x, y] = color
    return img


def radial_glow(size, center, radius, color):
    width, height = size
    glow = Image.new("RGBA", size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(glow)
    cx, cy = center
    for i in range(radius, 0, -12):
        alpha = int(color[3] * (i / radius) ** 2)
        bbox = (cx - i, cy - i, cx + i, cy + i)
        draw.ellipse(bbox, fill=(color[0], color[1], color[2], alpha))
    return glow.filter(ImageFilter.GaussianBlur(28))


def draw_building(draw, x, y, w, h, fill, outline, window_color, shaft=False):
    draw.rounded_rectangle((x, y - h, x + w, y), radius=8, fill=fill, outline=outline, width=2)

    if shaft:
        shaft_x1 = x + int(w * 0.35)
        shaft_x2 = x + int(w * 0.65)
        draw.rounded_rectangle(
            (shaft_x1, y - h + 24, shaft_x2, y - 18),
            radius=10,
            outline=(80, 220, 255, 90),
            width=2,
        )
        for stop in range(4):
            sy = y - h + 70 + stop * int((h - 120) / 4)
            draw.line((shaft_x1 + 10, sy, shaft_x2 - 10, sy), fill=(90, 235, 255, 70), width=2)
        car_h = max(32, int(h * 0.08))
        car_y = y - int(h * 0.42)
        draw.rounded_rectangle(
            (shaft_x1 + 6, car_y - car_h, shaft_x2 - 6, car_y),
            radius=8,
            fill=(120, 245, 255, 80),
            outline=(140, 255, 255, 120),
            width=2,
        )

    cols = max(2, w // 30)
    rows = max(3, h // 36)
    pad_x = max(10, w // 8)
    pad_y = max(18, h // 10)
    inner_w = max(8, (w - pad_x * 2) // cols - 6)
    inner_h = max(8, (h - pad_y * 2) // rows - 8)
    for row in range(rows):
        for col in range(cols):
            wx = x + pad_x + col * ((w - pad_x * 2) // cols)
            wy = y - h + pad_y + row * ((h - pad_y * 2) // rows)
            alpha = 46 if (row + col) % 3 else 88
            draw.rounded_rectangle(
                (wx, wy, wx + inner_w, wy + inner_h),
                radius=3,
                fill=(window_color[0], window_color[1], window_color[2], alpha),
            )


def main():
    base = Image.open(SRC).convert("RGBA")
    width, height = base.size

    overlay = vertical_gradient(
        (width, height),
        (4, 15, 44, 150),
        (4, 10, 28, 188),
    )

    scene = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    draw = ImageDraw.Draw(scene)

    horizon = int(height * 0.73)

    for line_y, alpha in ((0.62, 40), (0.68, 26), (0.78, 18)):
        y = int(height * line_y)
        draw.line((260, y, width - 260, y), fill=(60, 180, 255, alpha), width=2)

    glow_a = radial_glow((width, height), (int(width * 0.34), int(height * 0.36)), 680, (40, 140, 255, 90))
    glow_b = radial_glow((width, height), (int(width * 0.68), int(height * 0.46)), 820, (60, 110, 255, 76))
    scene = Image.alpha_composite(scene, glow_a)
    scene = Image.alpha_composite(scene, glow_b)
    draw = ImageDraw.Draw(scene)

    building_specs = [
        (220, horizon + 70, 260, 560, False),
        (520, horizon + 40, 220, 430, False),
        (780, horizon + 10, 240, 650, False),
        (1080, horizon - 40, 220, 820, False),
        (1380, horizon - 120, 220, 1180, True),
        (1660, horizon - 30, 210, 760, False),
        (1920, horizon - 80, 220, 980, True),
        (2200, horizon - 5, 220, 680, False),
        (2480, horizon + 30, 260, 520, False),
        (2780, horizon - 10, 220, 700, True),
        (3060, horizon + 45, 240, 500, False),
        (3360, horizon + 70, 200, 380, False),
    ]

    for x, y, w, h, shaft in building_specs:
        draw_building(
            draw,
            x,
            y,
            w,
            h,
            fill=(10, 30, 68, 150),
            outline=(55, 175, 255, 44),
            window_color=(110, 235, 255),
            shaft=shaft,
        )

    # Elevated foreground forms to echo the original sweeping road shapes.
    draw.polygon(
        [
            (0, height),
            (0, int(height * 0.85)),
            (int(width * 0.22), int(height * 0.79)),
            (int(width * 0.40), int(height * 0.83)),
            (int(width * 0.55), int(height * 0.77)),
            (int(width * 0.74), int(height * 0.82)),
            (width, int(height * 0.76)),
            (width, height),
        ],
        fill=(5, 15, 42, 196),
    )
    draw.line(
        [
            (int(width * 0.08), int(height * 0.82)),
            (int(width * 0.28), int(height * 0.76)),
            (int(width * 0.49), int(height * 0.80)),
            (int(width * 0.68), int(height * 0.74)),
            (int(width * 0.88), int(height * 0.78)),
        ],
        fill=(80, 215, 255, 34),
        width=18,
    )

    # Add a few vertical tech scan lines to suggest elevator shafts / monitoring rails.
    for x in (int(width * 0.18), int(width * 0.42), int(width * 0.59), int(width * 0.81)):
        draw.line((x, 180, x, int(height * 0.88)), fill=(70, 220, 255, 22), width=2)
        draw.line((x + 16, 240, x + 16, int(height * 0.84)), fill=(70, 220, 255, 12), width=1)

    # Central elevator cue.
    shaft_left = int(width * 0.47)
    shaft_top = int(height * 0.42)
    shaft_right = int(width * 0.53)
    shaft_bottom = int(height * 0.78)
    draw.rounded_rectangle(
        (shaft_left, shaft_top, shaft_right, shaft_bottom),
        radius=24,
        outline=(90, 235, 255, 80),
        width=3,
        fill=(18, 52, 96, 36),
    )
    for stop in range(5):
        sy = shaft_top + 70 + stop * int((shaft_bottom - shaft_top - 120) / 4)
        draw.line((shaft_left + 24, sy, shaft_right - 24, sy), fill=(110, 245, 255, 46), width=2)
    draw.rounded_rectangle(
        (shaft_left + 18, int(height * 0.60), shaft_right - 18, int(height * 0.66)),
        radius=14,
        fill=(130, 245, 255, 88),
        outline=(180, 255, 255, 120),
        width=2,
    )

    composed = Image.alpha_composite(base, overlay)
    composed = Image.alpha_composite(composed, scene)
    final = Image.blend(composed, base, 0.22)
    final = final.filter(ImageFilter.GaussianBlur(0.2))
    final.save(DST)
    print(DST)


if __name__ == "__main__":
    main()
