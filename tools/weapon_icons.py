"""
Ícones GUI de armas em pixel art 32×32, desenhados separadamente dos modelos na mão.

Silhuetas normais com aço facetado, ouro, couro e gemas. Não gera variantes elementais.
Importado por gen_weapon_models.py; não sobrescreve texturas usadas pelas armas legadas na mão.
Sem antialiasing: rotação e enquadramento usam apenas vizinho mais próximo.
"""
import os
from PIL import Image, ImageDraw, ImageFilter


PALETTE = {"metal": (139, 168, 200), "edge": (227, 242, 250), "dark": (39, 49, 73),
           "gold": (225, 170, 56), "leather": (105, 49, 51), "gem": (70, 223, 210)}
WEAPONS = ("rapier", "longsword", "dagger", "hammer", "greatsword", "axe", "kunai", "wand")


def weapon_icon(name):
    """Desenha no eixo vertical, gira em pixels e enquadra a silhueta num slot 32×32."""
    palette = PALETTE
    art = Image.new("RGBA", (48, 48))
    draw = ImageDraw.Draw(art)

    def color(role):
        return palette[role] + (255,)

    def polygon(points, role):
        # Facetas com duas faixas de brilho e marcas discretas de forja.
        mask = Image.new("L", art.size)
        ImageDraw.Draw(mask).polygon(points, fill=255)
        bounds = mask.getbbox()
        if not bounds:
            return
        x0, y0, x1, y1 = bounds
        base = palette[role]
        for y in range(y0, y1):
            for x in range(x0, x1):
                if not mask.getpixel((x, y)):
                    continue
                side = (x - x0) / max(1, x1 - x0 - 1)
                light = 1.17 if side < 0.27 else 0.97 if side < 0.62 else 0.70
                if (x * 3 + y * 5) % 19 == 0:
                    light += 0.08
                art.putpixel((x, y), tuple(min(255, int(c * light)) for c in base) + (255,))

    def line(points, role, width=1):
        draw.line(points, fill=color(role), width=width)

    def rect(box, role):
        polygon([(box[0], box[1]), (box[2], box[1]), (box[2], box[3]), (box[0], box[3])], role)

    def gem(x, y, size=3):
        polygon([(x, y - size), (x + size, y), (x, y + size), (x - size, y)], "dark")
        polygon([(x, y - size + 1), (x + size - 1, y), (x, y + size - 1), (x - size + 1, y)], "gem")
        draw.point((x - 1, y - 1), fill=color("edge"))

    def grip(top, bottom, width=2):
        rect((24 - width, top, 24 + width, bottom), "leather")
        for y in range(top + 1, bottom, 3):
            line([(24 - width, y), (24 + width, y + 1)], "dark")
            draw.point((24 - width, y - 1), fill=color("gold"))
        rect((23 - width, bottom, 25 + width, bottom + 2), "gold")

    if name in ("longsword", "greatsword"):
        wide = name == "greatsword"
        half = 6 if wide else 4
        polygon([(24, 3), (24 + half, 10), (24 + half, 29), (24 - half, 29), (24 - half, 10)], "metal")
        line([(24, 4), (24 - half + 1, 11), (24 - half + 1, 28)], "edge")
        line([(24, 9), (24, 28)], "dark", 2 if wide else 1)
        line([(25, 11), (25, 27)], "gem")
        for y in (15, 20, 25):
            line([(22, y), (24, y + 1), (26, y - 1)], "gold")
        polygon([(12, 28), (16, 29), (32, 29), (36, 28), (36, 32), (32, 33), (26, 31), (22, 31), (16, 33), (12, 32)], "gold")
        line([(13, 29), (18, 30), (30, 30), (35, 29)], "edge")
        grip(32, 41, 2)
        gem(24, 31, 3)
        gem(24, 44, 2)
    elif name == "rapier":
        polygon([(24, 2), (25, 7), (25, 30), (23, 30), (23, 7)], "metal")
        line([(24, 3), (24, 29)], "edge")
        line([(23, 18), (23, 29)], "gem")
        grip(33, 42, 1)
        polygon([(16, 30), (21, 29), (28, 29), (33, 31), (30, 33), (18, 33)], "gold")
        line([(17, 30), (22, 30), (29, 30), (32, 31)], "edge")
        line([(28, 32), (32, 34), (32, 39), (28, 42), (25, 42)], "gold", 2)
        line([(30, 34), (30, 38), (28, 40)], "edge")
        gem(24, 31, 2)
        gem(24, 44, 2)
    elif name == "dagger":
        polygon([(24, 9), (29, 17), (28, 27), (26, 30), (22, 30), (20, 27), (19, 17)], "metal")
        line([(24, 10), (20, 18), (21, 26)], "edge")
        polygon([(24, 13), (25, 21), (24, 27), (23, 21)], "dark")
        line([(24, 17), (24, 24)], "gem")
        polygon([(16, 28), (20, 29), (28, 29), (32, 28), (32, 32), (28, 33), (20, 33), (16, 32)], "gold")
        grip(33, 41, 2)
        gem(24, 30, 3)
        gem(24, 44, 2)
    elif name == "hammer":
        grip(22, 43, 2)
        for y in (26, 34, 40):
            rect((21, y, 27, y + 1), "gold")
        polygon([(10, 9), (14, 6), (35, 6), (38, 10), (38, 21), (34, 24), (13, 24), (10, 21)], "dark")
        rect((13, 9, 35, 21), "metal")
        polygon([(10, 10), (13, 8), (15, 9), (15, 21), (12, 22), (10, 20)], "edge")
        polygon([(33, 9), (36, 9), (38, 11), (38, 20), (35, 22), (33, 21)], "metal")
        line([(16, 9), (31, 9)], "edge")
        rect((21, 6, 27, 24), "gold")
        rect((22, 10, 26, 20), "dark")
        line([(24, 11), (24, 19)], "gem", 2)
        line([(22, 15), (26, 15)], "gem")
        for x in (17, 31):
            for y in (12, 18):
                draw.point((x, y), fill=color("gold"))
    elif name == "axe":
        grip(14, 43, 2)
        rect((21, 10, 27, 18), "gold")
        polygon([(26, 8), (32, 10), (37, 7), (40, 10), (40, 22), (36, 27), (29, 24), (26, 20)], "metal")
        line([(37, 8), (39, 11), (39, 21), (36, 25), (30, 23)], "edge", 2)
        line([(29, 12), (30, 20), (35, 22)], "gold")
        polygon([(21, 10), (17, 8), (15, 11), (16, 17), (21, 19)], "dark")
        gem(25, 15, 3)
    elif name == "kunai":
        polygon([(24, 5), (31, 22), (27, 29), (21, 29), (17, 22)], "metal")
        line([(24, 6), (18, 22), (22, 27)], "edge")
        polygon([(24, 9), (27, 22), (24, 28), (22, 22)], "dark")
        line([(24, 13), (24, 24)], "gem")
        rect((21, 29, 27, 31), "gold")
        grip(32, 37, 1)
        draw.ellipse((20, 39, 28, 46), outline=color("gold"), width=2)
        draw.point((22, 40), fill=color("edge"))
    else:
        grip(19, 43, 1)
        for y in (23, 32, 39):
            rect((21, y, 27, y + 1), "gold")
        polygon([(24, 2), (32, 9), (30, 18), (26, 22), (22, 22), (18, 18), (16, 9)], "gold")
        polygon([(24, 5), (29, 10), (27, 17), (24, 20), (21, 17), (19, 10)], "dark")
        gem(24, 12, 6)
        line([(24, 7), (21, 11), (22, 14)], "edge")
        line([(17, 10), (16, 15), (20, 20)], "edge")
        gem(24, 43, 2)

    # Contorno de um pixel, sem preencher recortes da guarda e do anel da kunai.
    outline = art.getchannel("A").filter(ImageFilter.MaxFilter(3))
    back = Image.new("RGBA", art.size, (15, 20, 33, 0))
    back.putalpha(outline)
    back.alpha_composite(art)
    rotated = back.rotate(-45, resample=Image.Resampling.NEAREST, expand=True)
    rotated = rotated.crop(rotated.getbbox())
    rotated.thumbnail((30, 30), Image.Resampling.NEAREST)
    result = Image.new("RGBA", (32, 32))
    result.alpha_composite(rotated, ((32 - rotated.width) // 2, (32 - rotated.height) // 2))
    return result


def write_icon_assets(textures):
    for name in WEAPONS:
        weapon_icon(name).save(os.path.join(textures, f"{name}_icon.png"))
    ghost = weapon_icon("dagger")
    for y in range(32):
        for x in range(32):
            if ghost.getpixel((x, y))[3]:
                ghost.putpixel((x, y), (156, 227, 255, 135))
    ghost.save(os.path.join(textures, "dagger_icon_thrown.png"))
