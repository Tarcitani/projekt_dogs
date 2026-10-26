"""Capa original em pixel art de Ecos do Despertar; não altera livros vanilla.

Couro azul-escuro, cantoneiras de bronze e selo ciano da Canção/Eco, sem símbolos de chefes.
Gera sprite 32×32 e modelo adicional usado por client/GuideBookModel. Integrado ao gerador
geral das armas; --preview amplia os pixels sem suavização em build/previews/echo_guide.png.
"""
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/solomod"


def cover():
    art = Image.new("RGBA", (32, 32))
    d = ImageDraw.Draw(art)
    ink, leather, light = "#182231", "#293c50", "#3d5367"
    bronze, gold, shadow = "#ae8950", "#dfbe78", "#715735"
    cyan, glow, page = "#67d2cc", "#ccf3e9", "#c8bd9e"
    # Espessura do volume e borda de papel na direita/embaixo.
    d.polygon([(7, 2), (25, 5), (29, 8), (28, 27), (24, 30), (3, 26), (3, 7)], fill=ink)
    d.polygon([(25, 7), (28, 8), (27, 25), (24, 28), (24, 10)], fill=page)
    d.line([(27, 10), (26, 24)], fill="#e8dfc5")
    d.line([(25, 12), (25, 23)], fill="#928974")
    d.polygon([(5, 24), (24, 27), (27, 26), (24, 29), (5, 26)], fill=page)
    d.line([(6, 25), (23, 28)], fill="#928974")
    # Capa e lombada, com áreas grandes de couro para manter a leitura na hotbar.
    d.polygon([(7, 3), (25, 6), (24, 26), (4, 23), (4, 7)], fill=leather)
    d.polygon([(4, 7), (7, 4), (7, 23), (4, 23)], fill="#202d40")
    d.line([(8, 5), (23, 7), (22, 24), (9, 22)], fill=light)
    d.line([(8, 7), (8, 21)], fill="#192a3a")
    for y in (9, 19):
        d.line([(4, y), (7, y + 1)], fill=bronze)
    # Cantoneiras e uma borda inferior; sem letras ou ilustrações que revelem a trama.
    for points in ([(8, 4), (12, 5), (12, 6), (9, 6), (9, 8), (8, 8)],
                   [(21, 6), (25, 6), (24, 11), (23, 11), (23, 8), (21, 8)],
                   [(9, 19), (10, 19), (10, 22), (13, 22), (13, 24), (8, 23)],
                   [(21, 21), (23, 21), (23, 25), (19, 25), (19, 24), (21, 24)]):
        d.polygon(points, fill=bronze)
    for x, y in ((8, 4), (24, 6), (9, 22), (22, 24)):
        d.point((x, y), fill=gold)
    # Selo de Eco: losango central e duas ondas quebradas, com relevo de bronze discreto.
    d.polygon([(16, 9), (21, 15), (16, 21), (11, 15)], fill=shadow)
    d.polygon([(16, 10), (20, 15), (16, 20), (12, 15)], fill=bronze)
    d.polygon([(16, 12), (18, 15), (16, 18), (14, 15)], fill="#224b59")
    d.line([(16, 12), (14, 15), (16, 18), (18, 15), (16, 12)], fill=cyan)
    d.point((16, 14), fill=glow)
    d.line([(11, 10), (10, 12), (10, 16)], fill=cyan)
    d.line([(22, 14), (22, 18), (20, 20)], fill=cyan)
    # Marcador em tecido que sai pela borda, mantendo o fundo transparente.
    d.polygon([(17, 25), (20, 26), (20, 30), (18, 29), (17, 30)], fill="#337d83")
    d.line([(18, 26), (18, 28)], fill=cyan)
    return art


def write_guide_assets(textures=None, models=None):
    textures = Path(textures) if textures else ASSETS / "textures/item"
    models = Path(models) if models else ASSETS / "models/item"
    cover().save(textures / "echo_guide.png")
    model = {"parent": "minecraft:item/generated", "textures": {"layer0": "solomod:item/echo_guide"}}
    (models / "echo_guide.json").write_text(json.dumps(model, indent=2) + "\n", encoding="utf-8")


def preview():
    canvas = Image.new("RGB", (900, 560), "#111925")
    d = ImageDraw.Draw(canvas)
    font = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 24)
    d.text((30, 24), "ECOS DO DESPERTAR / CAPA", font=font, fill="#dfe8ec")
    d.text((30, 70), "Couro escuro • bronze • selo de Eco ciano", font=font, fill="#9cafc3")
    sprite = cover()
    large = sprite.resize((384, 384), Image.Resampling.NEAREST)
    canvas.paste(large, (55, 140), large)
    d.text((500, 155), "32 × 32 pixels", font=font, fill="#dfe8ec")
    d.text((500, 200), "Inventário, mão e mundo", font=font, fill="#9cafc3")
    canvas.paste(sprite, (510, 260), sprite)
    target = ROOT / "build/previews/echo_guide.png"
    target.parent.mkdir(parents=True, exist_ok=True)
    canvas.save(target)
    print(target)


if __name__ == "__main__":
    import sys
    write_guide_assets()
    if "--preview" in sys.argv:
        preview()
