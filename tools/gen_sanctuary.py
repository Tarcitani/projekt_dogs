"""
Gera as texturas dos mobs dos Santuários de Eco em textures/entity/sanctuary/ (<id>.png e <id>_glow.png) e os modelos
de item dos ovos.

As malhas são empacotadas em tempo de execução por SanctuaryModels.Atlas (shelf packing). Este script repete o mesmo
empacotamento lendo, em ordem, as chamadas `a.add(...)`, `a.glow(...)` e `legs(...)` de cada método do Java. Só os
cubos criados com `a.glow` acendem na camada de brilho. A textura base é um padrão de cor por criatura (PALETTES).
Mudou uma malha? Rode o script de novo.

O ruído é uma função de hash das coordenadas: a saída é sempre a mesma. Rodar da raiz do projeto:
  python tools/gen_sanctuary.py
"""
import os
import re

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MODELS = os.path.join(ROOT, "src/main/java/com/tarcitani/solomod/client/model/SanctuaryModels.java")
OUT = os.path.join(ROOT, "src/main/resources/assets/solomod/textures/entity/sanctuary")
MODEL_ITEMS = os.path.join(ROOT, "src/main/resources/assets/solomod/models/item")

# id: (método no Java, cor base 1, cor base 2, cor de detalhe, padrão do detalhe)
PALETTES = {
    # Forja Banhada em Cinzas
    "cinzagrim": ("cinzagrim", 0x2B2625, 0x35302E, 0xFF6A00, "veins"),
    "living_slag": ("livingSlag", 0xFF9A1F, 0xE2711A, 0x3A1F14, "crust"),
    "forge_spark": ("forgeSpark", 0xFFD36B, 0xFF8A1F, 0xFFF2C0, "dots"),
    "withered_smith": ("witheredSmith", 0xCFC4A8, 0xB9AD90, 0x2A2420, "crust"),
    # Biblioteca do Esquecimento
    "archivist": ("archivist", 0x2A2238, 0x352C47, 0xB38CFF, "runes"),
    "flying_tome": ("flyingTome", 0x5A3A22, 0x6B4630, 0xD9C9A3, "bands"),
    "ink_sentinel": ("inkSentinel", 0x1B1B26, 0x24243A, 0x4A3AA8, "dots"),
    "spectral_scribe": ("spectralScribe", 0x6E6A8F, 0x807BA3, 0xE6E0FF, "runes"),
    # Santuário das Marés
    "weeper": ("weeper", 0x1F4E5F, 0x2A6275, 0xFF8FA3, "dots"),
    "choir_drowned": ("choirDrowned", 0x2E6B63, 0x3A7D72, 0xF07A8A, "crust"),
    "coral_jelly": ("coralJelly", 0xFF9EC4, 0xF58AB5, 0xFFE0EE, "dots"),
    "coral_crab": ("coralCrab", 0xD9534F, 0xC4433F, 0xF7D08A, "bands"),
    # Jardim Petrificado
    "root_mother": ("rootMother", 0x4A3A2A, 0x5A4632, 0x6E9B3A, "bands"),
    "thorn_sprout": ("thornSprout", 0x4C7A2B, 0x5B8C34, 0x2E4A18, "dots"),
    "moss_golem": ("mossGolem", 0x6B6B6B, 0x5E5E5E, 0x5E8C31, "crust"),
    "amber_wasp": ("amberWasp", 0xFFB300, 0xF2A000, 0x2A1E0A, "bands"),
    # Torre dos Ventos Quebrados
    "herald": ("herald", 0xCFD8DC, 0xB8C4C9, 0x4FC3F7, "veins"),
    "gray_harpy": ("grayHarpy", 0x78808A, 0x6A727C, 0xE8E0D0, "bands"),
    "gale_totem": ("galeTotem", 0x8A8F99, 0x7A7F88, 0x9FE8FF, "runes"),
    "breeze_elemental": ("breezeElemental", 0xD6F3FF, 0xBFE9FA, 0x6FC8E8, "veins"),
    # Catacumba Sussurrante
    "echo_hunter": ("echoHunter", 0x1A1A1F, 0x24242B, 0x5A4A2A, "bands"),
    "wailing_shade": ("wailingShade", 0x3A3F55, 0x464C66, 0xA8F0E6, "dots"),
    "whispering_bones": ("whisperingBones", 0xC8C0AA, 0xB5AD97, 0x5A6B7A, "crust"),
    "creeping_shadow": ("creepingShadow", 0x101014, 0x18181F, 0x7A2BFF, "veins"),
}


def noise(x, y, k):
    """0..1 determinístico."""
    return ((x * 73856093) ^ (y * 19349663) ^ (k * 83492791)) % 1000 / 1000.0


def rgb(value):
    return ((value >> 16) & 0xFF, (value >> 8) & 0xFF, value & 0xFF, 255)


def detail(pattern, x, y, k):
    if pattern == "veins":
        return (x * 3 + y * 2) % 23 == 0 or (x - y) % 31 == 0
    if pattern == "crust":
        return noise(x // 3, y // 3, k + 50) > 0.78
    if pattern == "dots":
        return noise(x, y, k + 60) > 0.94
    if pattern == "bands":
        return (y // 3) % 4 == 0
    if pattern == "runes":
        return (x % 7 == 3 and y % 5 < 3) or (y % 9 == 4 and x % 6 < 4)
    return False


def pixel(name, x, y):
    _, a, b, accent, pattern = PALETTES[name]
    k = sum(map(ord, name))
    if detail(pattern, x, y, k):
        return rgb(accent)
    return rgb(a if noise(x // 2, y // 2, k) > 0.4 else b)


CALL_RE = re.compile(
    r"a\.(add|glow)\(\w+,\s*(-?[0-9.]+)F?,\s*(-?[0-9.]+)F?,\s*(-?[0-9.]+)F?,\s*(\d+),\s*(\d+),\s*(\d+)\)"
    r"|legs\(root,\s*a,\s*[0-9.]+F?,\s*(\d+),\s*(\d+),\s*(\d+)\)")


def cubes_of(body):
    """Cubos (w, h, d, brilha) na ordem em que o Atlas os recebe; legs(...) vira os 4 cubos que cria."""
    out = []
    for c in CALL_RE.finditer(body):
        if c.group(1):
            out.append((int(c.group(5)), int(c.group(6)), int(c.group(7)), c.group(1) == "glow"))
        else:
            h, w, foot = int(c.group(8)), int(c.group(9)), int(c.group(10))
            out += [(w, h, w, False), (w, foot, w + 2, False)] * 2
    return out


def read_rects():
    """{método: (tamanho, [(x0, y0, x1, y1, brilha), ...])}: o mesmo shelf packing de SanctuaryModels.Atlas."""
    src = open(MODELS, encoding="utf-8").read()
    out = {}
    for m in re.finditer(r"public static LayerDefinition (\w+)\(\) \{(.*?)\n    \}", src, re.S):
        size = int(re.search(r"new Atlas\((\d+)\)", m.group(2)).group(1))
        x = y = shelf = 0
        rects = []
        for w, h, d, glow in cubes_of(m.group(2)):
            cw, ch = 2 * (d + w), d + h
            if x + cw > size:
                x, y, shelf = 0, y + shelf, 0
            if y + ch > size:
                raise SystemExit(f"{m.group(1)}: as redes de UV não cabem em {size}x{size}")
            rects.append((x, y, x + cw, y + ch, glow))
            x += cw
            shelf = max(shelf, ch)
        out[m.group(1)] = (size, rects)
    return out


def main():
    os.makedirs(OUT, exist_ok=True)
    rects = read_rects()
    for name, (method, *_) in PALETTES.items():
        size, cubes = rects[method]
        base = Image.new("RGBA", (size, size))
        for y in range(size):
            for x in range(size):
                base.putpixel((x, y), pixel(name, x, y))
        base.save(os.path.join(OUT, name + ".png"))
        glow = Image.new("RGBA", (size, size), (0, 0, 0, 255))
        for x0, y0, x1, y1, lit in cubes:
            if not lit:
                continue
            for y in range(y0, y1):
                for x in range(x0, x1):
                    glow.putpixel((x, y), (255, 255, 255, 255))
        glow.save(os.path.join(OUT, name + "_glow.png"))
        with open(os.path.join(MODEL_ITEMS, name + "_spawn_egg.json"), "w", encoding="utf-8", newline="") as f:
            f.write('{\n  "parent": "minecraft:item/template_spawn_egg"\n}\n')
    print(len(PALETTES), "criaturas geradas em", OUT)


if __name__ == "__main__":
    main()
