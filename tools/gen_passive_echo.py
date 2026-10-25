"""
Gera as texturas 64x64 da Fauna Pacífica do Neutro (Arcana) em textures/entity/passive/:

  * spectral_scout.png  -> névoa preta semitransparente, máscara de marfim e fenda ciano
  * echo_runic_bat.png  -> pelagem cinza-ardósia, anéis de sonar azuis
  * relic_automaton.png -> pedra monolítica, placa de cobre e runas douradas

O ruído é uma função de hash das coordenadas: a saída é sempre a mesma. Rodar da raiz do projeto:
  python tools/gen_passive_echo.py            # gera as texturas
  python tools/gen_passive_echo.py --repack   # antes, se mudou a malha: reempacota os texOffs sem sobreposição
"""
import math
import os
import re
import sys

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/solomod/textures/entity/passive")
SIZE = 64


def noise(x, y, k):
    """0..1 determinístico."""
    return ((x * 73856093) ^ (y * 19349663) ^ (k * 83492791)) % 1000 / 1000.0


def hex_rgba(value, alpha=255):
    return ((value >> 16) & 0xFF, (value >> 8) & 0xFF, value & 0xFF, alpha)


def make(pixel):
    img = Image.new("RGBA", (SIZE, SIZE))
    for y in range(SIZE):
        for x in range(SIZE):
            img.putpixel((x, y), pixel(x, y))
    return img


def scout(x, y):
    # Névoa: preto azulado com alguns pixels translúcidos; a máscara (faixa superior) é marfim.
    if y < 16:
        return hex_rgba(0xF2EBD9) if noise(x, y, 1) > 0.15 else hex_rgba(0xD9CDB0)
    if x in (0, 63) or y == 63:
        return hex_rgba(0x00E5FF, 200)
    r = noise(x, y, 2)
    if r < 0.12:
        return (0, 0, 0, 0)
    return hex_rgba(0x1A1A24 if r > 0.5 else 0x14141C, 220)


def bat(x, y):
    base = 0x3B4249
    # Anéis de sonar azuis em pontos fixos e pontilhado de pelagem.
    cx, cy = 16, 32
    ring = abs(((x - cx) ** 2 + (y - cy) ** 2) ** 0.5 - 9) < 0.7
    if ring:
        return hex_rgba(0x1E88E5)
    if noise(x, y, 3) > 0.8:
        return hex_rgba(0x2D3339)
    return hex_rgba(base)


def automaton(x, y):
    # Pedra monolítica com marmoreado; faixas de cobre oxidado nas bordas e runas douradas no meio.
    stone = 0x5A5D60 if noise(x // 2, y // 2, 4) > 0.3 else 0x4F5256
    if x < 2 or x > 61 or y < 2 or y > 61:
        return hex_rgba(0x8A5A3B)
    if (x + y) % 11 == 0 and 20 < y < 44:
        return hex_rgba(0xD4AF37)
    return hex_rgba(stone)


def salamander(x, y):
    # Escamas poligonais de basalto com fendas de magma.
    if (x + y) % 9 == 0 or (x * y) % 17 == 0:
        return hex_rgba(0xFF5722)
    return hex_rgba(0x211A1E if noise(x // 3, y // 3, 5) > 0.4 else 0x2A2228)


def scarab(x, y):
    # Vidro dourado com raios solares diagonais em âmbar.
    if (x + y) % 8 == 0 or (x - y) % 8 == 0:
        return hex_rgba(0xFF8F00)
    return hex_rgba(0xFFD54F if noise(x, y, 6) > 0.2 else 0xFFE082)


def colt(x, y):
    # Escamas cinza-carvão com veios de brasa.
    if (x * 3 + y) % 13 == 0:
        return hex_rgba(0xFF3D00)
    return hex_rgba(0x423D3B if noise(x // 2, y // 2, 7) > 0.35 else 0x383331)


def axolotl(x, y):
    # Pele translúcida azul-celeste com brânquias pérola nas laterais.
    if x in (0, 1, 62, 63) and 14 < y < 50:
        return hex_rgba(0xE0F7FA, 230)
    return hex_rgba(0x80DEEA, 200 if noise(x, y, 8) > 0.2 else 170)


def turtle(x, y):
    # Casco azul-petróleo com escamas e flor rosa bioluminescente nas costas.
    if 26 < x < 38 and 26 < y < 38 and (x + y) % 3 == 0:
        return hex_rgba(0xE040FB)
    return hex_rgba(0x004D40 if noise(x // 2, y // 2, 9) > 0.3 else 0x00695C)


def remora(x, y):
    # Escamas prateadas com marcas de ventosa ciano.
    if (x * y) % 19 == 0 or ((x - 32) ** 2 + (y - 32) ** 2) % 97 < 6:
        return hex_rgba(0x00B8D4)
    return hex_rgba(0x90A4AE if noise(x // 2, y // 2, 10) > 0.35 else 0xB0BEC5)


def tank(x, y):
    # Pelagem marrom-escura com placas de granito musgoso no dorso.
    if y < 10 and (x // 4 + y // 4) % 2 == 0:
        return hex_rgba(0x689F38 if noise(x, y, 11) > 0.5 else 0x558B2F)
    return hex_rgba(0x3E2723 if noise(x, y, 12) > 0.3 else 0x4E342E)


def golem(x, y):
    # Pedra entalhada com runas âmbar.
    if (x * 5 + y * 3) % 23 == 0:
        return hex_rgba(0xFFB300)
    return hex_rgba(0x4E342E if noise(x // 3, y // 3, 13) > 0.35 else 0x5D4037)


def gazelle(x, y):
    # Madeira petrificada com seiva âmbar translúcida.
    if (x + 2 * y) % 15 == 0:
        return hex_rgba(0xFFA000, 230)
    return hex_rgba(0x8D6E63 if noise(x // 2, y // 2, 14) > 0.4 else 0x795548)


def manta(x, y):
    # Pele branco-azulada de nuvem com bordas prateadas nas asas.
    if x < 6 or x > 57 or y < 4 or y > 60:
        return hex_rgba(0xB0BEC5)
    return hex_rgba(0xE1F5FE if noise(x // 3, y // 3, 15) > 0.25 else 0xD4EEF9)


def owl(x, y):
    # Plumagem cinza-chumbo com arcos voltaicos roxos.
    if (x + 3 * y) % 17 == 0 or (x * y) % 29 == 0:
        return hex_rgba(0x7C4DFF)
    return hex_rgba(0x37474F if noise(x // 2, y // 2, 16) > 0.35 else 0x2F3D45)


def moth(x, y):
    # Céu estrelado roxo-ciano com pixels de falha digital magenta.
    if noise(x, y, 17) > 0.93:
        return hex_rgba(0x00E5FF)
    if (x * 7 + y * 3) % 31 == 0:
        return hex_rgba(0xD500F9)
    return hex_rgba(0x120024 if noise(x // 4, y // 4, 18) > 0.3 else 0x1E0033)


# Camada emissiva: tons de cinza (preto = apagado), tingida pela cor do elemento no render.
# O brilho é derivado da malha: lê os cubos de PassiveEchoModels.java, calcula a área de UV de cada um e acende só os
# cubos listados abaixo. Cada entrada é (parte, índice do cubo na parte, modo): "full" acende o cubo inteiro e "dots"
# acende um pontilhado (veios, runas, penas). Mudou a malha? Rode este script de novo.
MODELS = os.path.join(ROOT, "src/main/java/com/tarcitani/solomod/client/model/PassiveEchoModels.java")

GLOW_CUBES = {
    "spectral_scout": [("head", 2, "full"), ("head", 3, "full"), ("wisp_1", 0, "dots"), ("wisp_2", 0, "full"),
                       ("wisp_3", 0, "full")],
    "echo_runic_bat": [("body", 1, "full"), ("wing_l", 0, "dots"), ("wing_r", 0, "dots")],
    "relic_automaton": [("head", 1, "full"), ("head", 2, "full"), ("head", 3, "full"), ("body", 2, "full"),
                        ("body", 3, "full"), ("body", 1, "dots")],
    "igneous_salamander": [("body", 2, "full"), ("body", 3, "full"), ("body", 4, "full"), ("body", 6, "full"),
                           ("head", 3, "full"), ("head", 4, "full"), ("head", 5, "full")],
    "golden_silica_scarab": [("body", 1, "full"), ("body", 0, "dots"), ("wing_l", 1, "full"), ("wing_r", 1, "full"),
                             ("head", 1, "full"), ("head", 2, "full")],
    "volcanic_ash_colt": [("body", 2, "full"), ("head", 2, "full"), ("head", 3, "full"), ("tail", 1, "full"),
                          ("wing_l", 0, "dots"), ("wing_r", 0, "dots")],
    "liquid_mana_axolotl": [("gill_l", 0, "full"), ("gill_l", 1, "full"), ("gill_r", 0, "full"),
                            ("gill_r", 1, "full"), ("head", 1, "full"), ("tail", 1, "full"), ("body", 0, "dots")],
    "lotus_turtle_crab": [("lotus", 0, "full"), ("lotus", 1, "full"), ("lotus", 2, "full"), ("lotus", 3, "full"),
                          ("shell", 2, "full"), ("shell", 3, "full")],
    "abyssal_trench_remora": [("antenna", 0, "full"), ("antenna", 1, "full"), ("sucker", 0, "full"),
                              ("body", 2, "full")],
    "sylvan_mini_tank": [("body", 2, "full"), ("body", 3, "full"), ("body", 4, "full"), ("head", 4, "full")],
    "runic_boulder_golem": [("head", 0, "full"), ("head", 1, "full"), ("body", 1, "dots"), ("body", 2, "dots"),
                            ("body", 3, "full"), ("pebble_1", 0, "full"), ("pebble_2", 0, "full"),
                            ("pebble_3", 0, "full"), ("pebble_4", 0, "full")],
    "amber_root_gazelle": [("head", i, "dots") for i in range(2, 8)] + [("neck", 0, "dots")],
    "cloud_manta_ray": [("wing_l", 1, "full"), ("wing_l", 2, "full"), ("wing_r", 1, "full"), ("wing_r", 2, "full"),
                        ("tail", 1, "full"), ("tail", 2, "full")],
    "storm_sentinel_owl": [("head", 1, "full"), ("head", 2, "full"), ("wing_l", 0, "dots"), ("wing_r", 0, "dots"),
                           ("body", 1, "dots")],
    "dimensional_rift_moth": [("wing_l", 0, "dots"), ("wing_l", 1, "dots"), ("wing_l", 2, "full"),
                              ("wing_r", 0, "dots"), ("wing_r", 1, "dots"), ("wing_r", 2, "full"),
                              ("head", 1, "full"), ("head", 2, "full"), ("body", 1, "full")],
}
GLOW = list(GLOW_CUBES)

FLOAT = r"(-?[0-9.]+)F?"
CUBE_RE = re.compile(r"texOffs\((\d+),\s*(\d+)\)\.addBox\(" + r",\s*".join([FLOAT] * 6) + r"\)")


def snake(camel):
    return re.sub(r"(?<!^)(?=[A-Z])", "_", camel).lower()


def read_cubes():
    """{forma: {parte: [(u, v, w, h, d), ...]}} lido de PassiveEchoModels.java."""
    src = open(MODELS, encoding="utf-8").read()
    out = {}
    for m in re.finditer(r"public static LayerDefinition (\w+)\(\) \{(.*?)\n    \}", src, re.S):
        parts = {}
        for chunk in re.split(r'addOrReplaceChild\("', m.group(2))[1:]:
            name = chunk.split('"', 1)[0]
            parts[name] = [(int(c[0]), int(c[1]), float(c[5]), float(c[6]), float(c[7]))
                           for c in CUBE_RE.findall(chunk)]
        out[snake(m.group(1))] = parts
    return out


def net(cube):
    """Retângulo de UV (x0, y0, x1, y1) ocupado por um cubo: 2*(d+w) por d+h."""
    u, v, w, h, d = cube
    return u, v, min(SIZE, math.ceil(u + 2 * (d + w))), min(SIZE, math.ceil(v + d + h))


def repack_models():
    """Redistribui os texOffs de PassiveEchoModels.java para que nenhuma rede de UV se sobreponha (empacotamento em
    prateleiras, do cubo mais alto para o mais baixo). Idempotente; a malha em si não muda, só onde ela lê a textura."""
    src = open(MODELS, encoding="utf-8", newline="").read()
    nl = "\r\n" if "\r\n" in src else "\n"
    src = src.replace("\r\n", "\n")

    # reescreve cada texOffs(u, v) na ordem dos cubos de cada método
    def rewrite(m):
        body = m.group(2)
        found = list(CUBE_RE.finditer(body))
        sizes = [(math.ceil(2 * (float(c.group(8)) + float(c.group(6)))),
                  math.ceil(float(c.group(8)) + float(c.group(7)))) for c in found]
        spots = [None] * len(found)
        x = y = shelf = 0
        for i in sorted(range(len(found)), key=lambda i: (-sizes[i][1], -sizes[i][0])):
            w, h = sizes[i]
            if x + w > SIZE:
                x, y, shelf = 0, y + shelf, 0
            if y + h > SIZE:
                raise SystemExit(f"{m.group(1)}: as redes de UV não cabem em {SIZE}x{SIZE}")
            spots[i] = (x, y)
            x += w
            shelf = max(shelf, h)
        out, last = [], 0
        for c, (u, v) in zip(found, spots):
            out.append(body[last:c.start()])
            out.append(f"texOffs({u}, {v}).addBox" + c.group(0)[c.group(0).index(".addBox") + len(".addBox"):])
            last = c.end()
        out.append(body[last:])
        return m.group(0).replace(body, "".join(out))

    new = re.sub(r"public static LayerDefinition (\w+)\(\) \{(.*?)\n    \}", rewrite, src, flags=re.S)
    open(MODELS, "w", encoding="utf-8", newline="").write(new.replace("\n", nl))


def glow_map(name, cubes):
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 255))
    for part, idx, mode in GLOW_CUBES[name]:
        x0, y0, x1, y1 = net(cubes[name][part][idx])
        for y in range(y0, y1):
            for x in range(x0, x1):
                if mode == "full" or (x * 3 + y * 5) % 4 == 0:
                    img.putpixel((x, y), (255, 255, 255, 255))
    return img


def warn_overlaps(cubes):
    """Avisa se as redes de UV de dois cubos se sobrepõem (o brilho de um vazaria no outro)."""
    for name, parts in cubes.items():
        rects = [(p, i, net(c)) for p, cs in parts.items() for i, c in enumerate(cs)]
        for a in range(len(rects)):
            for b in range(a + 1, len(rects)):
                (pa, ia, ra), (pb, ib, rb) = rects[a], rects[b]
                if ra[0] < rb[2] and rb[0] < ra[2] and ra[1] < rb[3] and rb[1] < ra[3]:
                    print(f"  aviso: {name}: {pa}[{ia}] sobrepõe {pb}[{ib}]")


def save_glows():
    cubes = read_cubes()
    warn_overlaps(cubes)
    for name in GLOW:
        glow_map(name, cubes).save(os.path.join(OUT, name + "_glow.png"))


def save_egg_models():
    """Modelo de item dos 15 ovos: sem ele o ovo aparece como a textura roxa e preta de "modelo ausente"."""
    models = os.path.join(ROOT, "src/main/resources/assets/solomod/models/item")
    for name in GLOW:
        with open(os.path.join(models, name + "_spawn_egg.json"), "w", encoding="utf-8", newline="") as f:
            f.write('{\n  "parent": "minecraft:item/template_spawn_egg"\n}\n')


def main():
    if "--repack" in sys.argv:
        repack_models()
    os.makedirs(OUT, exist_ok=True)
    make(scout).save(os.path.join(OUT, "spectral_scout.png"))
    make(bat).save(os.path.join(OUT, "echo_runic_bat.png"))
    make(automaton).save(os.path.join(OUT, "relic_automaton.png"))
    make(salamander).save(os.path.join(OUT, "igneous_salamander.png"))
    make(scarab).save(os.path.join(OUT, "golden_silica_scarab.png"))
    make(colt).save(os.path.join(OUT, "volcanic_ash_colt.png"))
    make(axolotl).save(os.path.join(OUT, "liquid_mana_axolotl.png"))
    make(turtle).save(os.path.join(OUT, "lotus_turtle_crab.png"))
    make(remora).save(os.path.join(OUT, "abyssal_trench_remora.png"))
    make(tank).save(os.path.join(OUT, "sylvan_mini_tank.png"))
    make(golem).save(os.path.join(OUT, "runic_boulder_golem.png"))
    make(gazelle).save(os.path.join(OUT, "amber_root_gazelle.png"))
    make(manta).save(os.path.join(OUT, "cloud_manta_ray.png"))
    make(owl).save(os.path.join(OUT, "storm_sentinel_owl.png"))
    make(moth).save(os.path.join(OUT, "dimensional_rift_moth.png"))
    save_glows()
    save_egg_models()
    print("texturas geradas em", OUT)


if __name__ == "__main__":
    main()
