"""Juramento do Eco: uma espada longa experimental com arte/modelo originais.

Referência visual: galeria de Eftann, https://realm-of-majestica.webflow.io/ (Blades of Majestica).
Interpreta proporções de fantasia, canal da lâmina e peças em camadas; não importa assets do pacote.
Atlas 32×64, ícone GUI 32×32 e malha com espessuras por peça, bisel e guarda assimétrica.
model(...) e render_view(...) também são reutilizados pela família de quatro armas experimentais.
Chamado por gen_weapon_models.py. --preview renderiza os JSON gerados em build/previews/.
"""
import json
import math
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont
from pixel_equipment import DISPLAY, write_model

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/solomod"
NAME = "echo_oathblade"
PALETTE = {"outline": "#243344", "steel": "#849aab", "shadow": "#536a7d", "edge": "#d6e4e6",
           "channel": "#224b59", "rune": "#67d2cc", "bronze": "#ae8950", "gold": "#dfbe78",
           "leather": "#3c3540", "wrap": "#64515a"}


def atlas():
    art = Image.new("RGBA", (32, 64))
    draw = ImageDraw.Draw(art)

    def poly(points, material):
        draw.polygon(points, fill=PALETTE[material])

    def line(points, material, width=1):
        draw.line(points, fill=PALETTE[material], width=width)

    # Ponta angular, dois ombros recortados e canal estreito: corpo sólido, sem peças flutuantes.
    poly([(15, 1), (20, 7), (20, 34), (22, 37), (21, 42), (10, 42),
          (9, 37), (11, 34), (11, 7)], "outline")
    poly([(15, 3), (19, 8), (19, 34), (20, 37), (19, 40), (12, 40),
          (11, 37), (12, 34), (12, 8)], "steel")
    line([(15, 3), (12, 8), (12, 33), (11, 37), (12, 39)], "edge")
    line([(18, 9), (18, 34), (19, 37), (18, 39)], "shadow")
    poly([(15, 7), (16, 9), (16, 38), (14, 38), (14, 9)], "channel")
    line([(15, 10), (15, 32)], "rune")
    # Três pequenas interrupções no canal sugerem uma inscrição de Eco, sem espalhar ornamentos.
    for y in (15, 24, 32):
        line([(15, y), (16, y + 1)], "steel")
    line([(13, 36), (15, 38), (17, 36)], "rune")

    # Guarda de duas alturas: um ramo ascendente aberto e outro curto, voltado para baixo.
    poly([(2, 36), (5, 37), (8, 41), (13, 42), (17, 41), (22, 40),
          (27, 43), (29, 47), (26, 47), (23, 44), (18, 45), (12, 46),
          (7, 44), (4, 41)], "outline")
    poly([(3, 37), (5, 38), (8, 42), (13, 43), (17, 42), (22, 41),
          (26, 44), (27, 46), (25, 45), (23, 43), (18, 44), (12, 45),
          (8, 43), (5, 40)], "bronze")
    line([(4, 38), (8, 42), (12, 43)], "gold")
    line([(19, 42), (22, 41), (25, 43)], "gold")
    poly([(13, 40), (17, 40), (19, 43), (17, 47), (13, 47), (11, 43)], "outline")
    poly([(14, 41), (16, 41), (18, 43), (16, 46), (14, 46), (12, 43)], "bronze")
    line([(14, 42), (16, 42)], "gold")
    line([(15, 43), (15, 44)], "rune")

    # Cabo escuro com três voltas largas e pomo vazado por cor (não uma gema extra).
    poly([(12, 47), (18, 47), (18, 59), (20, 60), (18, 63), (12, 63),
          (10, 60), (12, 59)], "outline")
    poly([(13, 48), (17, 48), (17, 59), (13, 59)], "leather")
    for y in (49, 53, 57):
        line([(13, y), (17, y + 1)], "wrap")
    line([(13, 47), (17, 47)], "gold")
    poly([(12, 60), (14, 59), (17, 60), (18, 61), (16, 62), (13, 62)], "bronze")
    line([(12, 60), (13, 61), (16, 61)], "gold")
    return art


def thickness(pixel):
    rgb = pixel[:3]
    color = {name: tuple(bytes.fromhex(value[1:])) for name, value in PALETTE.items()}
    if rgb == color["edge"]:
        return 0.6
    if rgb in (color["bronze"], color["gold"]):
        return 2.8
    if rgb in (color["leather"], color["wrap"]):
        return 2.0
    if rgb in (color["channel"], color["rune"]):
        return 1.15
    return 1.5 if rgb in (color["steel"], color["shadow"]) else 0.85


def model(sprite, texture_name=NAME, depth_at=None):
    """Malha compartilhada; depth_at(x,y,pixel) permite espessuras próprias por peça de cada arma."""
    depth_at = depth_at or (lambda x, y, pixel: thickness(pixel))
    elements = []
    for y in range(64):
        for x in range(32):
            pixel = sprite.getpixel((x, y))
            if not pixel[3]:
                continue
            depth = depth_at(x, y, pixel)
            face = {"uv": [x / 2, y / 4, (x + 1) / 2, (y + 1) / 4], "texture": "#pixel"}
            faces = {side: dict(face) for side in ("north", "south")}
            for side, dx, dy in (("west", -1, 0), ("east", 1, 0), ("up", 0, -1), ("down", 0, 1)):
                nx, ny = x + dx, y + dy
                if (not 0 <= nx < 32 or not 0 <= ny < 64 or not sprite.getpixel((nx, ny))[3]
                        or depth_at(nx, ny, sprite.getpixel((nx, ny))) < depth):
                    faces[side] = dict(face)
            elements.append({"from": [x / 2, -4 + (63 - y) / 2, 8 - depth / 2],
                             "to": [(x + 1) / 2, -4 + (64 - y) / 2, 8 + depth / 2],
                             "rotation": {"angle": -45, "axis": "z", "origin": [8, 8, 8]},
                             "faces": faces})
    return {"gui_light": "side", "ambientocclusion": False, "display": DISPLAY,
            "textures": {"pixel": f"solomod:item/{texture_name}_atlas", "particle": f"solomod:item/{texture_name}_atlas"},
            "elements": elements}


def write_oathblade(textures=None, models=None):
    textures = Path(textures) if textures else ASSETS / "textures/item"
    models = Path(models) if models else ASSETS / "models/item"
    sprite = atlas()
    sprite.save(textures / f"{NAME}_atlas.png")
    icon = sprite.rotate(-45, resample=Image.Resampling.NEAREST, expand=True)
    icon = icon.crop(icon.getbbox())
    icon.thumbnail((30, 30), Image.Resampling.NEAREST)
    gui = Image.new("RGBA", (32, 32))
    gui.alpha_composite(icon, ((32 - icon.width) // 2, (32 - icon.height) // 2))
    gui.save(textures / f"{NAME}.png")
    write_model(models, NAME, model(sprite), NAME)


def render_view(draw, base, sprite, bounds, degrees, scale=None):
    """Projeção compartilhada dos cubos reais; aceita atlas de qualquer tamanho e rotação Z por peça."""
    faces = {"south": (4, 5, 6, 7), "north": (3, 2, 1, 0), "west": (0, 4, 7, 3),
             "east": (5, 1, 2, 6), "up": (3, 7, 6, 2), "down": (0, 1, 5, 4)}
    transformed_faces = []
    angle = math.radians(degrees)
    for element in base["elements"]:
        x0, y0, z0 = element["from"]
        x1, y1, z1 = element["to"]
        vertices = [(x0, y0, z0), (x1, y0, z0), (x1, y1, z0), (x0, y1, z0),
                    (x0, y0, z1), (x1, y0, z1), (x1, y1, z1), (x0, y1, z1)]
        rotation = element.get("rotation", {"angle": 0, "origin": [8, 8, 8]})
        spin = math.radians(rotation["angle"])
        ox, oy, oz = rotation["origin"]
        projected = []
        for x, y, z in vertices:
            x, y = x - ox, y - oy
            x, y = x * math.cos(spin) - y * math.sin(spin) + ox - 8, x * math.sin(spin) + y * math.cos(spin) + oy - 8
            z -= 8
            x, z = x * math.cos(angle) + z * math.sin(angle), -x * math.sin(angle) + z * math.cos(angle)
            projected.append((x, -y + z * 0.15, z))
        for side, face in element["faces"].items():
            points = [projected[i] for i in faces[side]]
            ax, ay = points[1][0] - points[0][0], points[1][1] - points[0][1]
            bx, by = points[2][0] - points[0][0], points[2][1] - points[0][1]
            if ax * by - ay * bx >= 0:
                continue
            u, v = face["uv"][:2]
            color = sprite.getpixel((min(sprite.width - 1, round(u * sprite.width / 16)),
                                     min(sprite.height - 1, round(v * sprite.height / 16))))[:3]
            shade = 1.0 if side == "south" else 0.65 if side in ("west", "east") else 0.85
            transformed_faces.append((sum(p[2] for p in points) / 4, points, tuple(int(c * shade) for c in color)))
    all_points = [p for _, points, _ in transformed_faces for p in points]
    minx, maxx = min(p[0] for p in all_points), max(p[0] for p in all_points)
    miny, maxy = min(p[1] for p in all_points), max(p[1] for p in all_points)
    left, top, right, bottom = bounds
    fit = min((right - left) / (maxx - minx), (bottom - top) / (maxy - miny))
    scale = min(scale, fit) if scale is not None else fit
    ox, oy = (left + right) / 2 - (minx + maxx) * scale / 2, (top + bottom) / 2 - (miny + maxy) * scale / 2
    for _, points, color in sorted(transformed_faces, key=lambda p: p[0]):
        draw.polygon([(ox + x * scale, oy + y * scale) for x, y, _ in points], fill=color)


def preview():
    """Renderiza o modelo real, incluindo rotação de cada cubo, em duas vistas ortográficas."""
    base = json.loads((ASSETS / f"models/item/{NAME}.json").read_text(encoding="utf-8"))["base"]
    sprite = Image.open(ASSETS / f"textures/item/{NAME}_atlas.png").convert("RGBA")
    canvas = Image.new("RGB", (1200, 740), "#111925")
    draw = ImageDraw.Draw(canvas)
    font_path = Path("C:/Windows/Fonts/segoeui.ttf")
    font = ImageFont.truetype(str(font_path), 21) if font_path.exists() else ImageFont.load_default()
    title = ImageFont.truetype(str(font_path), 34) if font_path.exists() else font
    draw.text((35, 22), "JURAMENTO DO ECO", fill="#e3eaf0", font=title)
    draw.text((35, 72), "Espada longa experimental / fantasia em camadas / arte original do Solo Mod", fill="#9cafc3", font=font)
    for col, degrees in enumerate((0, 55)):
        left = 25 + col * 585
        draw.rounded_rectangle((left, 125, left + 565, 690), radius=16, fill="#1b2838")
        draw.text((left + 22, 140), "SILHUETA FRONTAL" if col == 0 else "VOLUME / 55°", fill="#9cafc3", font=font)
        render_view(draw, base, sprite, (left + 52, 200, left + 512, 660), degrees)
    draw.text((35, 704), "Aba Solo Mod: Pixel Art 3D  •  /give @s solomod:echo_oathblade", fill="#9cafc3", font=font)
    target = ROOT / "build/previews/echo_oathblade.png"
    target.parent.mkdir(parents=True, exist_ok=True)
    canvas.save(target)
    print(target)


if __name__ == "__main__":
    import sys
    write_oathblade()
    if "--preview" in sys.argv:
        preview()
