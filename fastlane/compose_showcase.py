"""Compose branded App Store showcase screenshots for SplitNow.

Visual language matches the Devara studio reference (GoingOut / TimeFor):
  • Saturated brand-color background (coral)
  • Paper white headline + optional italic eyebrow + subhead
  • Phone anchored at BOTTOM edge of canvas, top 60-70% visible only
  • Phone rendered as just-the-screen with rounded corners + soft shadow
  • Optional radial bloom behind headline

Three device classes per shot:
  • 6.9" iPhone (1320 × 2868) — required
  • 6.1" iPhone (1206 × 2622) — older device freshness
  • 13" iPad   (2064 × 2752) — required even for iPhone-only apps
"""
from __future__ import annotations
from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter, ImageFont

# ── SplitNow brand palette ────────────────────────────────────────────────
CORAL       = (242,  90,  43, 255)   # F25A2B — primary brand accent
CORAL_LIGHT = (255, 130,  90, 255)   # for radial bloom
INK         = ( 15,  14,  12, 255)   # 0F0E0C
PAPER       = (250, 250, 247, 255)   # FAFAF7
PAPER_DIM   = (250, 250, 247, 210)
DARK_BG     = ( 31,  31,  34, 255)   # 1F1F22 — for scan/camera shot

REPO   = Path(__file__).resolve().parent.parent
RAW    = REPO / "raw-captures"
OUT    = REPO / "fastlane" / "screenshots" / "en-US"

# Reuse TimeFor's Plus Jakarta Sans (close to SF Pro feel). SplitNow uses
# system fonts at runtime, but for marketing we want a controlled face.
TIMEFOR_FONT_DIR = Path.home() / "dev/TimeFor/composeApp/src/commonMain/composeResources/font"
FONT_R = TIMEFOR_FONT_DIR / "plus_jakarta_sans.ttf"
FONT_I = TIMEFOR_FONT_DIR / "plus_jakarta_sans_italic.ttf"

CLASSES = {
    "iphone-6_9": (1320, 2868),
    "iphone-6_1": (1206, 2622),
    "ipad-13":    (2064, 2752),
}

# Each shot = (slug, bg_color, eyebrow|None, headline, subhead|None, source-capture)
SHOTS = [
    ("01-split",   CORAL,   None,                "Split the bill,\nkeep it simple.",   "One app, no accounts, no IOUs.",                "00-onboarding.png"),
    ("02-scan",    DARK_BG, None,                "Scan a receipt.\nOr type freely.",    "On-device OCR + plain-English describe.",       "02-scan-or-skip.png"),
    ("03-ai",      CORAL,   '"Anna had pasta…"', "AI does the math.",                   "Just say who ordered what. The app sorts it.",  "01-home.png"),
    ("04-share",   CORAL,   None,                "Share a card.\nDone.",                "Receipt-card image + your payment details.",    "01-home.png"),
    ("05-private", CORAL,   None,                "No accounts.\nNothing tracked.",      "Receipt photo never leaves your phone.",        "01-home.png"),
]


# ── Helpers ───────────────────────────────────────────────────────────────
def _wrap_lines(draw, text, font, max_w):
    out = []
    for paragraph in text.split("\n"):
        words = paragraph.split()
        line = ""
        for w in words:
            test = (line + " " + w).strip()
            bbox = draw.textbbox((0, 0), test, font=font)
            if bbox[2] - bbox[0] <= max_w:
                line = test
            else:
                if line:
                    out.append(line)
                line = w
        if line:
            out.append(line)
    return out


def _rounded_corners(img, radius):
    mask = Image.new("L", img.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, *img.size), radius=radius, fill=255)
    out = img.copy()
    out.putalpha(mask)
    return out


def _drop_shadow(im, offset=(0, 40), blur=70, opacity=130):
    w, h = im.size
    pad = blur * 3
    canvas = Image.new("RGBA", (w + pad * 2, h + pad * 2 + offset[1]), (0, 0, 0, 0))
    silhouette = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    silhouette.paste(im, (pad + offset[0], pad + offset[1]), im)
    r, g, b, a = silhouette.split()
    black = Image.new("L", silhouette.size, 0)
    silhouette = Image.merge("RGBA", (black, black, black, a.point(lambda v: int(v * opacity / 255))))
    silhouette = silhouette.filter(ImageFilter.GaussianBlur(blur))
    canvas.alpha_composite(silhouette)
    canvas.paste(im, (pad, pad), im)
    return canvas


def _radial_bloom(canvas, center, radius, color, opacity=80):
    w, h = canvas.size
    bloom_size = radius * 2
    bloom = Image.new("RGBA", (bloom_size, bloom_size), (0, 0, 0, 0))
    d = ImageDraw.Draw(bloom)
    steps = 30
    for i in range(steps, 0, -1):
        r = int((i / steps) * radius)
        alpha = int((1 - i / steps) ** 2 * opacity)
        d.ellipse(
            (radius - r, radius - r, radius + r, radius + r),
            fill=(*color[:3], alpha),
        )
    bloom = bloom.filter(ImageFilter.GaussianBlur(radius // 6))
    canvas.alpha_composite(bloom, (center[0] - radius, center[1] - radius))


def _draw_text_block(canvas, eyebrow, headline, subhead, w, h, text_color=PAPER, dim_color=PAPER_DIM, eyebrow_color=None, top_pad_ratio=0.07):
    draw = ImageDraw.Draw(canvas)
    pad_x = int(w * 0.07)
    pad_top = int(h * top_pad_ratio)

    headline_size = int(w * 0.085)
    subhead_size = int(w * 0.030)
    eyebrow_size = int(w * 0.032)

    h_font = ImageFont.truetype(str(FONT_R), headline_size)
    s_font = ImageFont.truetype(str(FONT_R), subhead_size)
    e_font = ImageFont.truetype(str(FONT_I), eyebrow_size)

    y = pad_top

    if eyebrow:
        e_lines = _wrap_lines(draw, eyebrow, e_font, w - pad_x * 2)
        e_col = eyebrow_color or PAPER_DIM
        for line in e_lines:
            draw.text((pad_x, y), line, fill=e_col, font=e_font)
            y += int(eyebrow_size * 1.35)
        y += int(eyebrow_size * 0.20)

    h_lines = _wrap_lines(draw, headline, h_font, w - pad_x * 2)
    line_h = int(headline_size * 1.02)
    for line in h_lines:
        draw.text((pad_x, y), line, fill=text_color, font=h_font)
        y += line_h

    if subhead:
        y += int(headline_size * 0.25)
        sub_lines = _wrap_lines(draw, subhead, s_font, w - pad_x * 2)
        for sline in sub_lines:
            draw.text((pad_x, y), sline, fill=dim_color, font=s_font)
            y += int(subhead_size * 1.40)


def _phone_screen(screen_path, target_w, corner_ratio=0.064):
    raw = Image.open(screen_path).convert("RGBA")
    aspect = raw.height / raw.width
    target_h = int(target_w * aspect)
    screen = raw.resize((target_w, target_h), Image.LANCZOS)
    radius = int(target_w * corner_ratio)
    screen = _rounded_corners(screen, radius)
    return _drop_shadow(screen, offset=(0, 30), blur=60, opacity=140)


def _compose(canvas_w, canvas_h, bg_color, eyebrow, headline, subhead, screen_path, is_ipad=False):
    canvas = Image.new("RGBA", (canvas_w, canvas_h), bg_color)

    # Soft radial bloom near top-right
    bloom_color = CORAL_LIGHT if bg_color == CORAL else (90, 90, 110, 255)
    _radial_bloom(
        canvas,
        center=(int(canvas_w * 0.72), int(canvas_h * 0.18)),
        radius=int(canvas_w * 0.55),
        color=bloom_color,
        opacity=70,
    )

    # Phone anchored at bottom — 78% iPhone, 42% iPad
    phone_w = int(canvas_w * (0.42 if is_ipad else 0.78))
    phone = _phone_screen(screen_path, phone_w)
    sw, sh = phone.size

    phone_bottom_y = int(canvas_h * 1.15)
    px = (canvas_w - sw) // 2
    py = phone_bottom_y - sh
    canvas.alpha_composite(phone, (px, py))

    # Headline last so it sits on top of any bloom but never under the phone
    _draw_text_block(canvas, eyebrow, headline, subhead, canvas_w, canvas_h)

    return canvas


def main():
    assert FONT_R.exists(), f"Font missing: {FONT_R}"
    assert FONT_I.exists(), f"Font missing: {FONT_I}"
    OUT.mkdir(parents=True, exist_ok=True)

    for old in OUT.glob("*.png"):
        old.unlink()

    rendered = []
    for slug, bg, eyebrow, headline, subhead, src in SHOTS:
        src_path = RAW / src
        assert src_path.exists(), f"Capture missing: {src_path}"
        for class_slug, (w, h) in CLASSES.items():
            is_ipad = class_slug.startswith("ipad")
            out_name = f"{slug}_{class_slug}.png"
            out_path = OUT / out_name
            img = _compose(w, h, bg, eyebrow, headline, subhead, src_path, is_ipad=is_ipad)
            img.convert("RGB").save(out_path, "PNG", optimize=False)
            rendered.append(out_path)

    print(f"✓ Rendered {len(rendered)} showcase PNGs to {OUT}")
    for p in rendered:
        print(f"  {p.name}  {p.stat().st_size // 1024} KiB")


if __name__ == "__main__":
    main()
