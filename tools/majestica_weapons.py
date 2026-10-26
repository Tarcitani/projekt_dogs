"""Quatro armas originais na linguagem de fantasia em camadas da Juramento do Eco.

Referência de estilo: Blades of Majestica, de Eftann, https://realm-of-majestica.webflow.io/.
Desenhos próprios; não incorpora arquivos do resource pack. Substitui só os assets pixel_*.
Atlas 32×64, ícones GUI 32×32, biséis/canais/guardas com espessuras físicas e martelo volumoso.
Adaga baseada na silhueta da Twilight Dagger de Minecraft Dungeons II:
lâmina curva estreita, guarda curta e cabo inclinado, com materiais da família do mod.
Referência visual: https://dungeons2.minecraft-yutakura.com/en/weapons/equipment-0032/.
Chamado por pixel_equipment.write_pixel_assets; --preview cria prancha dos JSON reais.
"""
import json
import math
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont
from gen_oathblade import ASSETS, PALETTE, ROOT, model
from pixel_equipment import write_model

WEAPONS = ("rapier", "longsword", "dagger", "hammer")
RGB = {name: tuple(bytes.fromhex(value[1:])) for name, value in PALETTE.items()}
# A forma vem da referência; aço, bronze, couro e Eco seguem a paleta das demais armas.
DAGGER_PALETTE = dict(PALETTE)
DAGGER_SCALE = 0.9


def weapon_atlas(name):
    image = Image.new("RGBA", (32, 64))
    draw = ImageDraw.Draw(image)
    palette = DAGGER_PALETTE if name == "dagger" else PALETTE

    def poly(points, role):
        draw.polygon(points, fill=palette[role])

    def line(points, role, width=1):
        draw.line(points, fill=palette[role], width=width)

    def grip(top, bottom, half=2):
        poly([(14 - half, top), (17 + half, top), (17 + half, bottom), (14 - half, bottom)], "outline")
        poly([(15 - half, top + 1), (16 + half, top + 1), (16 + half, bottom - 1), (15 - half, bottom - 1)], "leather")
        for y in range(top + 3, bottom - 1, 4):
            line([(15 - half, y), (16 + half, y + 1)], "wrap")
        line([(14 - half, top), (17 + half, top)], "bronze")
        poly([(14 - half, bottom - 1), (17 + half, bottom - 1), (18 + half, bottom + 1),
              (16 + half, bottom + 3), (15 - half, bottom + 3), (13 - half, bottom + 1)], "outline")
        line([(14 - half, bottom + 1), (17 + half, bottom + 1)], "bronze", 2)
        line([(14 - half, bottom), (17 + half, bottom)], "gold")

    if name == "rapier":
        # Lâmina estreita e longa, com cup guard facetado e arco que protege a mão.
        poly([(15, 1), (18, 7), (18, 41), (13, 41), (13, 7)], "outline")
        poly([(15, 3), (17, 8), (17, 40), (14, 40), (14, 8)], "steel")
        line([(15, 4), (14, 8), (14, 39)], "edge")
        line([(16, 10), (16, 35)], "channel")
        line([(16, 20), (16, 34)], "rune")
        grip(47, 59, 1)
        poly([(6, 39), (10, 40), (15, 41), (21, 40), (25, 38), (24, 44),
              (21, 47), (10, 47), (7, 44)], "outline")
        poly([(8, 41), (12, 42), (20, 42), (23, 40), (22, 44), (20, 46),
              (11, 46), (9, 44)], "bronze")
        line([(8, 41), (12, 42), (20, 42), (23, 40)], "gold")
        poly([(12, 43), (19, 43), (20, 45), (16, 47), (11, 45)], "steel")
        line([(14, 44), (17, 44)], "rune")
        line([(22, 44), (26, 47), (27, 52), (24, 58), (19, 60)], "outline", 3)
        line([(23, 45), (25, 48), (26, 52), (23, 57), (19, 59)], "bronze")
        line([(25, 49), (25, 52)], "gold")
    elif name == "longsword":
        # Espada de corte: lâmina mais larga, ombros angulares e guarda em dois ganchos.
        poly([(15, 2), (21, 9), (21, 30), (23, 34), (21, 40), (10, 40),
              (8, 34), (10, 30), (10, 9)], "outline")
        poly([(15, 4), (20, 10), (20, 30), (21, 34), (19, 39), (12, 39),
              (10, 34), (11, 30), (11, 10)], "steel")
        line([(15, 4), (11, 10), (11, 30), (10, 34), (12, 38)], "edge")
        line([(19, 11), (19, 30), (20, 34), (18, 38)], "shadow")
        poly([(15, 8), (17, 11), (17, 35), (15, 38), (13, 35), (13, 11)], "channel")
        line([(15, 12), (15, 33)], "rune")
        for y in (18, 26):
            line([(15, y), (16, y + 1)], "steel")
        grip(46, 59)
        poly([(2, 37), (5, 38), (9, 42), (22, 42), (26, 38), (29, 37),
              (28, 43), (24, 46), (19, 46), (16, 48), (12, 46), (7, 46), (3, 43)], "outline")
        poly([(3, 39), (5, 40), (9, 43), (22, 43), (26, 40), (28, 39),
              (26, 43), (23, 45), (18, 45), (16, 47), (12, 45), (8, 45), (5, 43)], "bronze")
        line([(4, 39), (9, 43), (12, 43)], "gold")
        line([(19, 43), (22, 43), (27, 39)], "gold")
        poly([(15, 40), (19, 44), (15, 48), (11, 44)], "outline")
        poly([(15, 41), (18, 44), (15, 47), (12, 44)], "bronze")
        line([(15, 43), (15, 45)], "rune")
    elif name == "dagger":
        # Proporções reconstruídas a partir da vista da referência (256×256).
        # Converte a diagonal para o atlas vertical; o modelo a gira de volta.
        # Não lê nem distribui a imagem externa: geometria/texturas são geradas aqui.
        def trace(points, role):
            divisor = math.sqrt(2) * 6
            poly([(round(15 + (x - 119 + y - 149) / divisor),
                   round(46 + (-x + 119 + y - 149) / divisor)) for x, y in points], role)

        # Cabo com curva discreta e pomo metálico; sem os chifres/serrilhas da versão anterior.
        trace([(70, 143), (98, 143), (114, 156), (96, 166), (82, 185),
               (68, 204), (65, 225), (35, 225), (35, 210), (43, 191),
               (56, 173), (64, 155)], "outline")
        trace([(72, 149), (94, 149), (104, 155), (89, 164), (76, 183),
               (62, 202), (59, 219), (42, 219), (49, 192), (62, 176)], "leather")
        for points in ([(68, 157), (86, 158), (84, 168), (62, 168)],
                       [(57, 178), (77, 179), (72, 188), (51, 188)],
                       [(47, 198), (64, 199), (61, 208), (43, 208)]):
            trace(points, "wrap")
        trace([(42, 207), (61, 209), (60, 221), (39, 221)], "steel")
        trace([(40, 218), (59, 218), (59, 224), (38, 224)], "edge")

        # Lâmina arqueada, lisa e afilada, com bisel no lado convexo.
        trace([(205, 29), (224, 28), (225, 57), (219, 81), (218, 101),
               (208, 112), (202, 129), (188, 140), (183, 154), (167, 163),
               (158, 176), (144, 181), (137, 195), (110, 192), (108, 180),
               (125, 173), (138, 154), (150, 141), (159, 124), (164, 110),
               (175, 99), (178, 84), (188, 72), (191, 56), (202, 44)], "outline")
        trace([(209, 33), (219, 32), (218, 59), (212, 81), (211, 99),
               (201, 109), (195, 126), (181, 137), (176, 151), (161, 160),
               (152, 172), (138, 177), (131, 188), (115, 186), (130, 175),
               (143, 156), (156, 142), (166, 124), (171, 110), (182, 98),
               (185, 82), (195, 70), (198, 54), (208, 43)], "edge")
        trace([(208, 39), (211, 38), (207, 65), (201, 88), (198, 104),
               (188, 116), (181, 132), (168, 144), (160, 158), (145, 167),
               (134, 179), (123, 182), (139, 162), (153, 147), (164, 128),
               (172, 113), (183, 101), (188, 84), (197, 72), (202, 53)], "steel")
        trace([(198, 66), (201, 64), (195, 91), (187, 108),
               (178, 123), (175, 119), (183, 102), (188, 84)], "channel")
        trace([(197, 71), (198, 71), (191, 94), (183, 109), (181, 108)], "rune")

        # Guarda curta angular da Twilight: acabamento em bronze e um pequeno olho de Eco.
        trace([(111, 113), (128, 113), (133, 132), (149, 129), (159, 145),
               (149, 159), (134, 168), (115, 166), (110, 156), (96, 155),
               (97, 141), (109, 141)], "outline")
        trace([(115, 118), (124, 118), (128, 137), (146, 135), (152, 145),
               (144, 153), (130, 161), (118, 160), (115, 150), (102, 150),
               (102, 145), (114, 145)], "bronze")
        trace([(115, 118), (123, 118), (126, 137), (119, 138)], "gold")
        trace([(128, 144), (139, 141), (143, 147), (133, 152)], "rune")
    elif name == "hammer":
        # Cabeça maciça com faces de impacto, nervuras de bronze e núcleo encaixado em baixo-relevo.
        grip(25, 59, 1)
        for y in (29, 43):
            line([(13, y), (18, y)], "bronze", 2)
            line([(13, y), (18, y)], "gold")
        poly([(5, 4), (25, 4), (30, 9), (30, 21), (25, 26), (5, 26), (1, 22), (1, 8)], "outline")
        poly([(6, 6), (24, 6), (28, 10), (28, 20), (24, 24), (6, 24), (3, 21), (3, 9)], "steel")
        poly([(5, 7), (8, 7), (8, 23), (5, 23), (3, 20), (3, 10)], "edge")
        poly([(24, 7), (27, 10), (27, 20), (24, 23), (22, 23), (22, 7)], "shadow")
        line([(9, 7), (21, 7)], "edge")
        line([(9, 23), (21, 23)], "shadow")
        poly([(11, 5), (19, 5), (20, 8), (20, 23), (18, 25), (12, 25), (10, 23), (10, 8)], "bronze")
        line([(12, 6), (18, 6)], "gold")
        poly([(13, 9), (17, 9), (18, 12), (18, 20), (16, 22), (12, 20), (12, 12)], "channel")
        line([(15, 11), (15, 20)], "rune", 2)
        line([(13, 16), (17, 16)], "rune")
    else:
        raise ValueError(name)
    return image


def piece_depth(name, x, y, pixel):
    rgb = pixel[:3]
    if name == "dagger":
        materials = {role: tuple(bytes.fromhex(color[1:])) for role, color in DAGGER_PALETTE.items()}
        if rgb == materials["edge"]:
            return 0.55
        if rgb in (materials["steel"], materials["shadow"]):
            return 1.4 if rgb == materials["steel"] else 1.1
    if name == "hammer" and y <= 26:
        if rgb in (RGB["bronze"], RGB["gold"]):
            return 7.0
        if rgb in (RGB["channel"], RGB["rune"]):
            return 5.2
        if rgb == RGB["outline"]:
            return 4.8
        return 6.0 if rgb != RGB["edge"] else 5.5
    if name == "rapier" and y < 40:
        return 0.5 if rgb == RGB["edge"] else 0.95
    if name == "rapier" and 40 <= y <= 47 and rgb in (RGB["steel"], RGB["shadow"]):
        return 2.2
    if rgb == RGB["edge"]:
        return 0.65
    if rgb in (RGB["bronze"], RGB["gold"]):
        return 3.0 if name == "longsword" else 2.6
    if rgb in (RGB["channel"], RGB["rune"]):
        return 1.25
    if rgb in (RGB["leather"], RGB["wrap"]):
        return 2.0
    return 1.65 if rgb in (RGB["steel"], RGB["shadow"]) else 1.0


def validate(sprite, name):
    opaque = {(x, y) for y in range(64) for x in range(32) if sprite.getpixel((x, y))[3]}
    visited, pending = set(), [next(iter(opaque))]
    while pending:
        x, y = pending.pop()
        if (x, y) in visited:
            continue
        visited.add((x, y))
        pending.extend((x + dx, y + dy) for dx in (-1, 0, 1) for dy in (-1, 0, 1)
                       if (x + dx, y + dy) in opaque and (x + dx, y + dy) not in visited)
    assert opaque == visited, f"Peça desconectada: {name}"


def icon(sprite):
    rotated = sprite.rotate(-45, resample=Image.Resampling.NEAREST, expand=True)
    rotated = rotated.crop(rotated.getbbox())
    rotated.thumbnail((30, 30), Image.Resampling.NEAREST)
    result = Image.new("RGBA", (32, 32))
    result.alpha_composite(rotated, ((32 - rotated.width) // 2, (32 - rotated.height) // 2))
    return result


def compact_dagger(base):
    """Ajusta a malha real em torno do centro do cabo já rotacionado: preserva o encaixe na mão.

    Silhueta/proporções da Twilight Dagger, com escala 0,9 e encaixe do cabo preservado.
    Fantasma e projétil herdam a mesma geometria; a GUI continua com sprite legível.
    """
    angle = math.radians(-45)
    pivot = (8 + 7 * math.sin(angle), 8 - 7 * math.cos(angle), 8)

    def shrink(point):
        return [round(anchor + (value - anchor) * DAGGER_SCALE, 6) for value, anchor in zip(point, pivot)]

    for element in base["elements"]:
        element["from"] = shrink(element["from"])
        element["to"] = shrink(element["to"])
        element["rotation"]["origin"] = shrink(element["rotation"]["origin"])


def write_majestica_assets(textures=None, models=None):
    textures = Path(textures) if textures else ASSETS / "textures/item"
    models = Path(models) if models else ASSETS / "models/item"
    for name in WEAPONS:
        item = f"pixel_{name}"
        sprite = weapon_atlas(name)
        validate(sprite, name)
        sprite.save(textures / f"{item}_atlas.png")
        icon(sprite).save(textures / f"{item}.png")
        base = model(sprite, item, lambda x, y, pixel: piece_depth(name, x, y, pixel))
        if name == "dagger":
            compact_dagger(base)
        if name == "hammer":
            base["display"] = json.loads(json.dumps(base["display"]))
            for hand, sign in (("righthand", -1), ("lefthand", 1)):
                base["display"][f"firstperson_{hand}"] = {"rotation": [6, sign * 58, -sign * 25],
                    "translation": [1.13, 3.2, 1.13], "scale": [0.68] * 3}
                base["display"][f"thirdperson_{hand}"] = {"rotation": [0, sign * 72, -sign * 55],
                    "translation": [0, 4, 0.5], "scale": [0.85] * 3}
        overrides = ([{"predicate": {"solomod:thrown": 1}, "model": "solomod:item/pixel_dagger_thrown"}]
                     if name == "dagger" else None)
        write_model(models, item, base, item, overrides)
        if name == "dagger":
            ghost = sprite.copy()
            for y in range(64):
                for x in range(32):
                    if ghost.getpixel((x, y))[3]:
                        ghost.putpixel((x, y), (156, 227, 255, 135))
            ghost.save(textures / "pixel_dagger_thrown_atlas.png")
            icon(ghost).save(textures / "pixel_dagger_thrown.png")
            ghost_base = json.loads(json.dumps(base))
            ghost_base["textures"] = {"pixel": "solomod:item/pixel_dagger_thrown_atlas",
                                      "particle": "solomod:item/pixel_dagger_thrown_atlas"}
            ghost_base["render_type"] = "minecraft:translucent"
            write_model(models, "pixel_dagger_thrown", ghost_base, "pixel_dagger_thrown")


def preview():
    """Comparação frontal/oblíqua da mesma malha/textura que o jogo carrega, em escala comum."""
    from gen_oathblade import render_view
    canvas = Image.new("RGB", (1600, 1060), "#111925")
    draw = ImageDraw.Draw(canvas)
    font_path = Path("C:/Windows/Fonts/segoeui.ttf")
    font = ImageFont.truetype(str(font_path), 22) if font_path.exists() else ImageFont.load_default()
    title = ImageFont.truetype(str(font_path), 32) if font_path.exists() else font
    draw.text((30, 20), "SOLO MOD / ARMAS DE FANTASIA", fill="#e3eaf0", font=title)
    draw.text((30, 70), "Arte original inspirada em Blades of Majestica • aço biselado, bronze e canais de Eco", fill="#9cafc3", font=font)
    for col, (name, label) in enumerate((("rapier", "Rapieira"), ("longsword", "Espada longa"),
                                       ("dagger", "Adaga"), ("hammer", "Martelo"))):
        left = 20 + col * 395
        draw.rounded_rectangle((left, 120, left + 375, 1035), radius=14, fill="#1b2838")
        draw.text((left + 18, 136), label, fill="#e3eaf0", font=font)
        base = json.loads((ASSETS / f"models/item/pixel_{name}.json").read_text(encoding="utf-8"))["base"]
        sprite = Image.open(ASSETS / f"textures/item/pixel_{name}_atlas.png").convert("RGBA")
        draw.text((left + 18, 180), "FRENTE", fill="#9cafc3", font=font)
        render_view(draw, base, sprite, (left + 15, 220, left + 360, 580), 0, scale=11.5)
        draw.text((left + 18, 615), "VOLUME / 55°", fill="#9cafc3", font=font)
        render_view(draw, base, sprite, (left + 15, 660, left + 360, 1020), 55, scale=11.5)
    target = ROOT / "build/previews/majestica_weapons.png"
    target.parent.mkdir(parents=True, exist_ok=True)
    canvas.save(target)
    print(target)


if __name__ == "__main__":
    import sys
    write_majestica_assets()
    if "--preview" in sys.argv:
        preview()
