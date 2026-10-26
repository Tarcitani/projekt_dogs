"""Arte original de apresentação: ícones pixel art 16×16 e flipbooks 32×256 de efeitos de combate.

Rode python tools/gen_player_presentation.py. Sem antialiasing; cada habilidade tem uma silhueta própria.
Os flipbooks têm oito quadros, selecionados por UV no renderer; não dependem de resource pack externo.
--preview salva a prancha de ícones em build/previews/talent_icons.png.
"""
import math
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/solomod/textures"
ICONS = ("crescent", "rising", "cross", "rapier_dash", "rapier_flurry", "rapier_retreat",
         "hammer_leap", "hammer_sweep", "hammer_quake", "throw", "teleport", "dash_strike",
         "ground_slam", "mana_bolt", "elemental_burst", "leap", "stat_for", "stat_agi", "stat_int", "stat_vit")
C = {"steel": "#9aaec4", "white": "#e9ffff", "cyan": "#54d8e0", "gold": "#edbd62",
     "dark": "#263b54", "purple": "#b795f0", "red": "#ee6b69", "green": "#8cda89"}


def icon(name):
    image = Image.new("RGBA", (16, 16))
    d = ImageDraw.Draw(image)

    def line(points, color="cyan", width=1):
        d.line(points, fill=C[color], width=width)

    def sword(x=0, y=0):
        d.polygon([(3+x, 11+y), (11+x, 3+y), (13+x, 2+y), (12+x, 5+y), (5+x, 13+y)], fill=C["steel"])
        line([(4+x, 10+y), (11+x, 3+y)], "white")
        line([(3+x, 9+y), (7+x, 13+y)], "gold")
        line([(4+x, 12+y), (2+x, 14+y)], "dark", 2)

    def hammer():
        line([(4, 13), (9, 7)], "gold", 2)
        d.polygon([(5, 4), (9, 1), (14, 6), (11, 10)], fill=C["steel"])
        line([(6, 4), (9, 2), (13, 6)], "white")
        line([(9, 4), (11, 6)], "cyan", 2)

    if name == "stat_for":
        d.polygon([(3, 6), (4, 3), (6, 3), (7, 2), (10, 2), (13, 5), (13, 10), (10, 13), (5, 13), (2, 10)], fill=C["red"])
        line([(4, 5), (6, 5), (6, 8), (10, 8), (11, 6)], "white")
        line([(5, 14), (11, 14)], "gold", 2)
    elif name == "stat_agi":
        d.polygon([(10, 2), (13, 3), (10, 10), (14, 12), (12, 14), (6, 13), (5, 10)], fill=C["green"])
        for y in (4, 7, 10):
            line([(1, y), (6, y)], "white")
        line([(8, 12), (12, 13)], "gold")
    elif name == "stat_int":
        d.polygon([(1, 3), (7, 4), (14, 2), (14, 12), (8, 14), (1, 12)], fill=C["dark"])
        line([(2, 4), (7, 5), (7, 12)], "cyan")
        line([(9, 5), (13, 4), (13, 10), (9, 11)], "white")
        line([(4, 7), (5, 9), (3, 10)], "purple")
    elif name == "stat_vit":
        d.polygon([(2, 3), (5, 2), (8, 5), (11, 2), (14, 3), (15, 7), (8, 14), (1, 7)], fill=C["gold"])
        line([(4, 4), (6, 4), (8, 7)], "white")
        line([(4, 9), (6, 9), (7, 6), (9, 11), (10, 8), (13, 8)], "red")
    elif name in ("crescent", "rising", "cross"):
        sword()
        if name == "crescent":
            d.polygon([(1, 7), (3, 3), (8, 1), (14, 3), (8, 3), (4, 5)], fill=C["cyan"])
        elif name == "rising":
            line([(2, 9), (2, 2), (0, 4), (2, 2), (4, 4)], "cyan")
        else:
            line([(3, 3), (13, 13)], "cyan", 2)
            line([(4, 2), (14, 12)], "white")
    elif name in ("rapier_dash", "rapier_flurry", "rapier_retreat", "dash_strike"):
        for i in range(3 if name == "rapier_flurry" else 1):
            y = 4 + i * 4 if name == "rapier_flurry" else 8
            line([(3, y), (14, y)], "cyan", 2)
            line([(5, y-1), (14, y-1), (12, y-3)], "white")
        if name == "rapier_retreat":
            line([(8, 13), (1, 13), (4, 10), (1, 13), (4, 15)], "gold")
        else:
            for y in (3, 8, 13):
                line([(0, y), (3, y)], "gold")
            if name == "dash_strike":
                line([(8, 2), (12, 6)], "purple", 2)
    elif name.startswith("hammer_") or name in ("ground_slam", "leap"):
        hammer()
        if name in ("hammer_leap", "leap"):
            line([(2, 10), (2, 2), (0, 4), (2, 2), (4, 4)], "cyan")
        elif name == "hammer_sweep":
            line([(1, 6), (0, 10), (3, 14), (11, 14), (14, 11), (11, 11)], "cyan", 2)
        else:
            line([(0, 14), (4, 12), (7, 14), (10, 11), (15, 14)], "gold")
            line([(8, 10), (7, 13), (9, 15)], "white")
    elif name == "throw":
        sword(1, -1)
        for y in (5, 8, 11):
            line([(0, y), (3, y-2)], "gold")
    elif name == "teleport":
        for x in (2, 12):
            line([(x+1, 2), (x-1, 5), (x-1, 11), (x+1, 14), (x+2, 11), (x+2, 5), (x+1, 2)], "gold")
        line([(4, 9), (8, 5), (7, 10), (12, 6)], "white", 2)
    elif name == "mana_bolt":
        d.polygon([(9, 1), (3, 9), (7, 9), (5, 15), (14, 5), (10, 5)], fill=C["purple"])
        line([(9, 3), (6, 8), (9, 8), (7, 12)], "white")
    else:
        d.polygon([(8, 0), (10, 5), (15, 3), (12, 8), (15, 13), (10, 11),
                   (8, 16), (6, 11), (1, 13), (4, 8), (1, 3), (6, 5)], fill=C["gold"])
        d.polygon([(8, 3), (10, 8), (8, 13), (6, 8)], fill=C["cyan"])
        line([(5, 8), (11, 8)], "white")
    return image


def flipbook(name):
    sheet = Image.new("RGBA", (32, 256))
    for frame in range(8):
        art = Image.new("RGBA", (32, 32))
        d = ImageDraw.Draw(art)
        if name == "rune":
            # Selo em oito segmentos, com runas que acendem em sequência.
            for i in range(8):
                a, b = i * math.pi / 4, (i + 1) * math.pi / 4
                points = [(16 + math.cos(a) * 12, 16 + math.sin(a) * 12),
                          (16 + math.cos(b) * 12, 16 + math.sin(b) * 12)]
                d.line(points, fill=(103, 222, 231, 220 if (i-frame) % 8 < 3 else 100), width=2)
                r = 8 if i % 2 else 6
                d.line([(16 + math.cos(a) * r, 16 + math.sin(a) * r),
                        (16 + math.cos(a) * (r+3), 16 + math.sin(a) * (r+3))], fill=(238, 202, 114, 230), width=1)
        elif name == "streak":
            for x in range(2, 30):
                half = max(1, int(math.sin((x-1) / 29 * math.pi) * (2 + frame % 3)))
                d.line([(x, 16-half), (x, 16+half)], fill=(80, 220, 238, 140), width=1)
                d.point((x, 16), fill=(235, 255, 255, 255))
            for i in range(4):
                x = (frame * 3 + i * 7) % 28 + 2
                d.line([(x, 11), (x+2, 9)], fill=(122, 237, 255, 230))
        else:
            r = 2 + frame
            d.polygon([(16, 16-r), (18, 14), (16+r, 16), (18, 18), (16, 16+r),
                       (14, 18), (16-r, 16), (14, 14)], fill=(236, 255, 255, max(30, 255-frame*25)))
            for dx, dy in ((-1,-1), (-1,1), (1,-1), (1,1)):
                d.rectangle((16+dx*r-1, 16+dy*r-1, 16+dx*r+1, 16+dy*r+1), fill=(79, 213, 234, 190-frame*17))
        sheet.alpha_composite(art, (0, frame * 32))
    return sheet


def generate():
    folder = ASSETS / "gui/talents"
    folder.mkdir(parents=True, exist_ok=True)
    for name in ICONS:
        icon(name).save(folder / f"{name}.png")
    effects = ASSETS / "effect"
    effects.mkdir(parents=True, exist_ok=True)
    for name in ("rune", "streak", "burst"):
        flipbook(name).save(effects / f"combat_{name}.png")


def preview():
    image = Image.new("RGB", (1000, 420), "#142030")
    d = ImageDraw.Draw(image)
    path = Path("C:/Windows/Fonts/segoeui.ttf")
    font = ImageFont.truetype(str(path), 16) if path.exists() else ImageFont.load_default()
    for i, name in enumerate(ICONS):
        x, y = 15 + i % 10 * 98, 18 + i // 10 * 190
        art = icon(name).resize((80, 80), Image.Resampling.NEAREST)
        image.paste(art, (x, y), art)
        words = name.split("_")
        for j, word in enumerate(words):
            d.text((x, y+90+j*20), word, fill="#d6e6ef", font=font)
    target = ROOT / "build/previews/talent_icons.png"
    target.parent.mkdir(parents=True, exist_ok=True)
    image.save(target)
    frames = Image.new("RGB", (920, 380), "#142030")
    d = ImageDraw.Draw(frames)
    for row, name in enumerate(("rune", "streak", "burst")):
        sheet = flipbook(name)
        d.text((8, row * 120 + 8), name, fill="#d6e6ef", font=font)
        for frame in range(8):
            art = sheet.crop((0, frame * 32, 32, (frame+1) * 32)).resize((90, 90), Image.Resampling.NEAREST)
            frames.paste(art, (130 + frame * 98, row * 120 + 15), art)
    frames.save(ROOT / "build/previews/combat_flipbooks.png")


if __name__ == "__main__":
    import sys
    generate()
    if "--preview" in sys.argv:
        preview()
