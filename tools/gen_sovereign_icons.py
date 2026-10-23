"""
Gera os dois ícones de conquista do Soberano em pixel art 16x16, sem suavização:

  * sovereign_head.png -> a cabeça dele (rosto de pedra, olhos roxos, dois chifres e a coroa com uma ponta de
                          cada Monarca: fogo, água, ouro, terra e ar), como monarch_fire_head é a do Rei Vulcânico;
  * unknown_boss.png   -> "???" (a conquista do Soberano antes de ele cair).

As matrizes abaixo são o desenho: cada letra é uma cor da paleta. Rodar da raiz do projeto:
    python tools/gen_sovereign_icons.py
"""
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/solomod/textures/item")

HEAD_PALETTE = {
    ".": None,
    "x": (24, 17, 24), "r": (110, 106, 104), "l": (142, 138, 134), "s": (74, 70, 69),
    "b": (232, 224, 200), "g": (227, 181, 44), "h": (255, 226, 122), "p": (200, 120, 255),
    "f": (255, 106, 31), "w": (63, 169, 245), "e": (139, 195, 74), "k": (230, 247, 255),
}
HEAD = [
    ".......gh.......",
    ".....w.gg.e.....",
    "...f.w.gg.e.k...",
    "...f.w.gg.e.k...",
    "..gggggggggggg..",
    "..ghgghgghgghg..",
    ".b.xxxxxxxxxx.b.",
    ".bxrrllllllrrxb.",
    ".bxrppsssspprxb.",
    ".bxrrsrrrrsrrxb.",
    "..xrrrssssrrrx..",
    "..xrshshshshrx..",
    "..xrrssssssrrx..",
    "...xxrrrrrrxx...",
    "....xxxxxxxx....",
    "................",
]

# "?" de 4 x 7; três lado a lado, com sombra deslocada 1 pixel.
GLYPH = [".XX.", "X..X", "...X", "..X.", ".X..", "....", ".X.."]


def build_head():
    return paint(HEAD, HEAD_PALETTE)


def paint(rows, palette):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        assert len(row) == 16, f"linha {y} tem {len(row)} pixels: {row!r}"
        for x, ch in enumerate(row):
            color = palette[ch]
            if color:
                img.putpixel((x, y), color + (255,))
    return img


def build_unknown():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    top = 4
    for pass_color, dx, dy in (((42, 23, 64, 255), 1, 1), ((176, 128, 255, 255), 0, 0)):
        for g in range(3):
            left = 1 + g * 5
            for y, row in enumerate(GLYPH):
                for x, ch in enumerate(row):
                    if ch == "X":
                        px, py = left + x + dx, top + y + dy
                        if 0 <= px < 16 and 0 <= py < 16:
                            img.putpixel((px, py), pass_color)
    return img


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_head().save(os.path.join(OUT, "sovereign_head.png"))
    build_unknown().save(os.path.join(OUT, "unknown_boss.png"))
    print("ok")
