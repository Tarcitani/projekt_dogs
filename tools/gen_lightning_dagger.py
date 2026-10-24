"""
Gera os sprites 16x16 da Adaga Relâmpago (protótipo de tema raio):

  * lightning_dagger.png         -> item: lâmina azul-escura com fio amarelo e faíscas, ponta em cima à direita
                                    (mesma orientação das outras adagas, que o renderer da adaga arremessada espera)
  * lightning_dagger_thrown.png  -> contorno fantasma que fica na mão enquanto ela está arremessada

Rodar da raiz do projeto:  python tools/gen_lightning_dagger.py
"""
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/solomod/textures/item")

DARK = (22, 24, 58, 255)
BLADE = (58, 70, 150, 255)
LIGHT = (120, 160, 255, 255)
BOLT = (255, 232, 70, 255)
WHITE = (255, 255, 235, 255)
GUARD = (210, 170, 40, 255)
GRIP = (70, 50, 40, 255)
POMMEL = (255, 232, 70, 255)

# (x, y) -> cor. A lâmina vai da diagonal (6,9) até a ponta (14,1).
PIXELS = {}


def put(x, y, c):
    PIXELS[(x, y)] = c


for i in range(9):  # lâmina: três pixels de espessura na diagonal
    x, y = 6 + i, 9 - i
    put(x, y, BLADE)
    put(x + 1, y, DARK)
    put(x, y - 1, LIGHT)
put(14, 1, WHITE)
put(15, 0, WHITE)
put(15, 1, DARK)
# fio de raio em zigue-zague pelo meio da lâmina
for x, y in ((7, 8), (8, 7), (8, 6), (9, 6), (10, 5), (10, 4), (11, 4), (12, 3), (12, 2), (13, 2)):
    put(x, y, BOLT)
for x, y in ((9, 7), (11, 5), (13, 3)):
    put(x, y, WHITE)
# guarda, cabo e pomo
for x, y in ((4, 8), (5, 9), (6, 10), (7, 11), (5, 7), (8, 10)):
    put(x, y, GUARD)
for x, y in ((5, 10), (4, 11), (3, 12)):
    put(x, y, GRIP)
put(2, 13, POMMEL)
put(1, 14, BOLT)
# faíscas soltas ao redor
for x, y in ((11, 8), (3, 5), (13, 6), (2, 10)):
    put(x, y, BOLT)


def main():
    os.makedirs(OUT, exist_ok=True)
    item = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for (x, y), color in PIXELS.items():
        if 0 <= x < 16 and 0 <= y < 16:
            item.putpixel((x, y), color)
    item.save(os.path.join(OUT, "lightning_dagger.png"))

    solid = {(x, y) for x in range(16) for y in range(16) if item.getpixel((x, y))[3] > 0}
    ghost = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for x, y in solid:
        edge = any((x + dx, y + dy) not in solid for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        ghost.putpixel((x, y), (255, 240, 130, 170) if edge else (255, 250, 200, 45))
    ghost.save(os.path.join(OUT, "lightning_dagger_thrown.png"))
    print("ok")


if __name__ == "__main__":
    main()
