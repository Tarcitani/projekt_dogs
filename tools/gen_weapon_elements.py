"""
Versões elementais das armas (chamado por gen_weapon_models.py).

* Armas 2D legadas (espadão, machado, kunai, varinha): o sprite 16x16 é ampliado para 32x32 e redesenhado —
  cores, forma e detalhes mudam por elemento:
    Fogo  lâmina em brasa com línguas de fogo subindo das bordas, cabo de carvão com brasas
    Água  lâmina de água com ondas, espuma nas bordas e gotas pingando, cabo de coral
    Terra lâmina de pedra serrilhada com rachaduras, musgo no topo e cristais incrustados, cabo de casca
    Ar    lâmina prateada com rastros de vento e penas saindo da guarda, cabo claro enrolado
* Armas 3D (rapieira, espada longa, adaga, martelo): paleta própria por elemento.
  Rapieira e espada longa também recebem peças extras (chamas, barbatanas, rochas, penas).
"""
import os
import random

from PIL import Image

ELEMENTS = ("water", "earth", "fire", "air")  # mesma ordem do ElementType (1..4)


def classify(base, over):
    """16x16 -> grade de classes: 'b' lâmina (metal), 'h' cabo, 'g' gema/gume tingido, None vazio."""
    import colorsys
    grid = [[None] * 16 for _ in range(16)]
    for y in range(16):
        for x in range(16):
            o, p = over.getpixel((x, y)), base.getpixel((x, y))
            if o[3] > 0:
                grid[y][x] = "g"
            elif p[3] > 0:
                h, l, s = colorsys.rgb_to_hls(*[c / 255 for c in p[:3]])
                grid[y][x] = "b" if s < 0.25 else "h"
    return grid


def upscale(grid):
    return [[grid[y // 2][x // 2] for x in range(32)] for y in range(32)]


def mix(a, b, t):
    t = max(0.0, min(1.0, t))
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))


N4 = ((1, 0), (-1, 0), (0, 1), (0, -1))


def empty(g, x, y):
    return not (0 <= x < 32 and 0 <= y < 32) or g[y][x] is None


def restyle(base_path, over_path, element, seed):
    base = Image.open(base_path).convert("RGBA")
    over = Image.open(over_path).convert("RGBA")
    g = upscale(classify(base, over))
    rnd = random.Random(seed + element)
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))

    def put(x, y, col, a=255):
        if 0 <= x < 32 and 0 <= y < 32:
            img.putpixel((x, y), tuple(col) + (a,))

    def along(x, y):  # 0 no cabo (embaixo à esquerda) -> 1 na ponta (em cima à direita)
        return (x + (31 - y)) / 62

    if element == "earth":
        # Pedra serrilhada: a lâmina cresce 1 px em pontos aleatórios da borda.
        grow = [(x + dx, y + dy) for y in range(32) for x in range(32) if g[y][x] == "b"
                for dx, dy in N4 if empty(g, x + dx, y + dy) and rnd.random() < 0.35]
        for x, y in grow:
            if 0 <= x < 32 and 0 <= y < 32:
                g[y][x] = "b"

    blade = [(x, y) for y in range(32) for x in range(32) if g[y][x] == "b"]
    for y in range(32):
        for x in range(32):
            c = g[y][x]
            if c is None:
                continue
            t = along(x, y)
            edge = any(empty(g, x + dx, y + dy) for dx, dy in N4)
            n = rnd.random()
            if element == "fire":
                if c == "b":
                    col = (255, 220, 80) if edge else mix((120, 20, 10), (255, 120, 20), t)
                    if not edge and n < 0.12: col = (60, 14, 8)
                elif c == "h":
                    col = (255, 90, 20) if (x + y) % 3 == 0 else (40, 22, 16)
                else:
                    col = (255, 240, 160)
            elif element == "water":
                if c == "b":
                    col = mix((20, 70, 140), (80, 200, 240), t)
                    if ((x - y) // 2 + (x + y) // 5) % 4 == 0: col = (170, 240, 255)
                    if edge: col = (230, 250, 255)
                elif c == "h":
                    col = (255, 110, 95) if (x + y) % 4 < 2 else (180, 60, 55)
                else:
                    col = (245, 240, 255)
            elif element == "earth":
                if c == "b":
                    col = mix((80, 72, 64), (160, 152, 140), n)
                    if empty(g, x, y - 1) and n < 0.6: col = (90, 140, 50)
                elif c == "h":
                    col = (90, 58, 32) if x % 2 else (60, 38, 20)
                else:
                    col = (90, 230, 120)
            else:  # air
                if c == "b":
                    col = (170, 240, 255) if edge else mix((200, 215, 230), (250, 252, 255), t)
                elif c == "h":
                    col = (230, 230, 235) if (x + y) % 3 else (170, 175, 185)
                else:
                    col = (120, 240, 255)
            put(x, y, col)

    if element == "fire":
        # Línguas de fogo sobem das bordas superiores (fora do cabo).
        for x, y in blade:
            if along(x, y) > 0.2 and empty(g, x, y - 1) and rnd.random() < 0.55:
                length = rnd.randint(1, 4)
                fx = x
                for k in range(1, length + 1):
                    fx += rnd.choice((0, 0, 1))
                    if not empty(g, fx, y - k):
                        break
                    col = mix((255, 120, 20), (255, 250, 180), k / length)
                    put(fx, y - k, col, 255 if k < length else 170)
    elif element == "water":
        # Gotas pingando de baixo da lâmina.
        for x, y in blade:
            if empty(g, x, y + 1) and empty(g, x - 1, y + 1) and rnd.random() < 0.12:
                put(x - 1, y + 3, (120, 210, 255), 230)
                put(x - 1, y + 2, (220, 245, 255), 200)
    elif element == "earth":
        # Rachaduras escuras e cristais incrustados.
        for _ in range(3):
            x, y = rnd.choice(blade)
            for _ in range(4):
                if not empty(g, x, y): put(x, y, (50, 44, 38))
                x += rnd.choice((-1, 0, 1)); y += rnd.choice((-1, 0, 1))
        inner = [(x, y) for x, y in blade if all(not empty(g, x + dx, y + dy) for dx, dy in N4)]
        for x, y in rnd.sample(inner, min(3, len(inner))):
            put(x, y, (200, 140, 255)); put(x + 1, y, (150, 90, 230)); put(x, y - 1, (240, 220, 255))
    else:
        # Rastros de vento paralelos à lâmina e penas saindo da guarda.
        for x, y in blade:
            t = along(x, y)
            if 0.3 < t < 0.95 and (x + y) % 3 == 0:
                for off, a in ((3, 150), (6, 90)):
                    if empty(g, x - off, y + off):
                        put(x - off, y + off, (180, 240, 255), a)
        joint = [(x, y) for y in range(32) for x in range(32) if g[y][x] == "h"
                 and any(0 <= x + dx < 32 and 0 <= y + dy < 32 and g[y + dy][x + dx] == "b" for dx, dy in N4)]
        if joint:
            jx, jy = joint[0]
            for k in range(1, 6):  # duas penas, uma para cada lado da guarda
                for dx, dy in ((-k, -k // 2), (k // 2, k)):
                    px, py = jx + dx - 1, jy + dy
                    if empty(g, px, py):
                        put(px, py, (250, 250, 250) if k < 5 else (170, 175, 185))
    return img


# ---------------------------------------------------------------------------------------------- 3D

PALETTES = {
    #          steel                          dark            gold                          leather                     tint                         edge             wrap
    "fire":  ((200, 80, 30), (120, 30, 10), (40, 20, 14), (232, 192, 112), (110, 76, 18), (60, 30, 20), (40, 20, 12), (255, 200, 60), (255, 110, 20), (255, 230, 120), (60, 20, 10)),
    "water": ((90, 190, 230), (30, 90, 150), (20, 50, 90), (255, 120, 100), (180, 60, 50), (30, 70, 110), (20, 50, 80), (240, 245, 255), (190, 220, 245), (230, 250, 255), (20, 40, 70)),
    "earth": ((150, 145, 135), (90, 85, 78), (70, 60, 50), (110, 160, 60), (60, 100, 30), (90, 58, 32), (60, 38, 20), (200, 140, 255), (130, 70, 220), (190, 185, 175), (60, 40, 20)),
    "air":   ((235, 242, 250), (170, 185, 200), (150, 160, 175), (250, 250, 250), (200, 205, 210), (220, 225, 232), (170, 175, 185), (160, 240, 255), (90, 200, 240), (255, 255, 255), (140, 150, 165)),
}


def parts_texture(element, path):
    st1, st2, dark, go1, go2, le1, le2, ti1, ti2, edge, wrap = PALETTES[element]
    sw = {(0, 0): (st1, st2), (4, 0): (dark, dark), (8, 0): (go1, go2), (12, 0): (le1, le2),
          (0, 4): (ti1, ti2), (4, 4): (edge, edge), (8, 4): (wrap, wrap)}
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for (x0, y0), (a, b) in sw.items():
        for y in range(4):
            for x in range(4):
                img.putpixel((x0 + x, y0 + y), mix(a, b, y / 3) + (255,))
    img.save(path)


def extras(weapon, element, el):
    """Peças a mais por elemento (mesma inclinação das outras), para mudar a silhueta."""
    out = []
    if weapon == "rapier":
        if element == "fire":
            for i, y in enumerate((8, 12, 16, 20, 23)):
                x = 8.3 if i % 2 == 0 else 6.7
                out.append(el([x, y, 7.85], [x + 1, y + 2, 8.15], "tint"))
        elif element == "water":
            out += [el([3, 3, 7.9], [4.5, 7, 8.1], "gold"), el([11.5, 3, 7.9], [13, 7, 8.1], "gold"),
                    el([7.2, -3.5, 7.2], [8.8, -1.5, 8.8], "tint")]
        elif element == "earth":
            out += [el([6, 3.8, 6.8], [10, 6, 9.2], "dark"), el([7.5, -4, 7.5], [8.5, -1.5, 8.5], "tint"),
                    el([8.3, 10, 7.8], [9, 12, 8.2], "dark"), el([7, 15, 7.8], [7.7, 17, 8.2], "dark")]
        else:
            out += [el([1.5, 5, 7.95], [4.5, 6.5, 8.05], "gold"), el([11.5, 5, 7.95], [14.5, 6.5, 8.05], "gold"),
                    el([2.5, 6.5, 7.95], [4.5, 7.5, 8.05], "gold"), el([11.5, 6.5, 7.95], [13.5, 7.5, 8.05], "gold")]
    else:  # claymore
        if element == "fire":
            for i, y in enumerate((11, 15, 19, 23, 26)):
                x = 9.4 if i % 2 == 0 else 5.6
                out.append(el([x, y, 7.8], [x + 1, y + 2.5, 8.2], "tint"))
        elif element == "water":
            out += [el([0.5, 2.4, 7.8], [2, 7.2, 8.2], "gold"), el([14, 2.4, 7.8], [15.5, 7.2, 8.2], "gold"),
                    el([9.6, 12, 7.9], [10.6, 20, 8.1], "tint")]
        elif element == "earth":
            out += [el([5.5, 6, 6.5], [10.5, 10, 9.5], "dark"), el([6.8, -7, 6.8], [9.2, -5, 9.2], "tint"),
                    el([9.4, 14, 7.7], [10.4, 16, 8.3], "dark"), el([5.6, 20, 7.7], [6.6, 22, 8.3], "dark")]
        else:
            out += [el([-1, 4.5, 7.95], [2.5, 6, 8.05], "gold"), el([13.5, 4.5, 7.95], [17, 6, 8.05], "gold"),
                    el([0, 6, 7.95], [2.5, 7.5, 8.05], "gold"), el([13.5, 6, 7.95], [16, 7.5, 8.05], "gold")]
    return out
