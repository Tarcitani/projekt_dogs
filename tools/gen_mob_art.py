"""
Gera a arte das criaturas do mod (os 4 tiers e o Soberano) a partir de uma única especificação:

  * texturas base   -> src/main/resources/assets/solomod/textures/entity/<mob>.png
  * texturas brilho -> src/main/resources/assets/solomod/textures/entity/<mob>_glow.png
                       (camada emissiva; o jogo tinge com a cor do elemento)
  * malhas Java     -> src/main/java/com/tarcitani/solomod/client/model/MobMeshes.java

Como a mesma especificação gera UV e textura, as duas nunca saem do sincronismo.
Rodar da raiz do projeto:  python tools/gen_mob_art.py

Convenções do espaço de modelo do Minecraft: y cresce para BAIXO, o chão fica em y = 24,
a frente do mob é -Z e o lado direito do mob é -X.
"""
import os
import random
from dataclasses import dataclass, field

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX_DIR = os.path.join(ROOT, "src/main/resources/assets/solomod/textures/entity")
JAVA_OUT = os.path.join(ROOT, "src/main/java/com/tarcitani/solomod/client/model/MobMeshes.java")


# ---------------------------------------------------------------------------------------------- spec

@dataclass
class Cube:
    x: float
    y: float
    z: float
    w: int
    h: int
    d: int
    mat: str
    decal: str = None      # desenho especial na face da frente (-Z)
    inflate: float = 0.0
    u: int = 0             # preenchidos pelo empacotador
    v: int = 0


@dataclass
class Part:
    name: str
    parent: str
    pivot: tuple
    cubes: list
    rot: tuple = (0.0, 0.0, 0.0)


@dataclass
class Model:
    name: str
    tex_w: int
    parts: list = field(default_factory=list)
    tex_h: int = 0

    def part(self, name, parent, pivot, *cubes, rot=(0.0, 0.0, 0.0)):
        self.parts.append(Part(name, parent, pivot, list(cubes), rot))


def C(x, y, z, w, h, d, mat, decal=None, inflate=0.0):
    return Cube(x, y, z, w, h, d, mat, decal, inflate)


# ---------------------------------------------------------------------------------------------- mobs

def minion():
    """Lacaio: diabrete-gárgula de cabeça enorme, orelhas de morcego, asas, chifres e rabo de lança."""
    m = Model("minion", 64)
    m.part("body", "root", (0, 18.5, 0),
           C(-3, -7, -2, 6, 7, 4, "imp_skin"),
           C(-2, -6, -2.6, 4, 5, 1, "imp_belly"),
           C(-0.5, -7, 2, 1, 2, 1, "bone"), C(-0.5, -4.5, 2, 1, 2, 1, "bone"), C(-0.5, -2, 2, 1, 2, 1, "bone"),
           rot=(0.45, 0, 0))
    m.part("head", "body", (0, -7, -1),
           C(-4.5, -8, -5, 9, 8, 8, "imp_skin", "imp_face"),
           C(-1, -7.5, -5.6, 2, 2, 1, "gem"))
    m.part("jaw", "head", (0, -1, -1), C(-4, 0, -4.5, 8, 2, 6, "imp_skin", "teeth"))
    m.part("ear_l", "head", (4.5, -5, -1), C(0, -1.5, 0, 7, 3, 1, "membrane"), rot=(0, 0.35, -0.35))
    m.part("ear_r", "head", (-4.5, -5, -1), C(-7, -1.5, 0, 7, 3, 1, "membrane"), rot=(0, -0.35, 0.35))
    m.part("horn_l", "head", (2.5, -8, -2), C(-0.5, -4, -0.5, 1, 4, 1, "bone"), rot=(-0.3, 0, 0.3))
    m.part("horn_l_tip", "horn_l", (0, -4, 0), C(-0.5, -3, -0.5, 1, 3, 1, "bone"), rot=(-0.5, 0, 0.2))
    m.part("horn_r", "head", (-2.5, -8, -2), C(-0.5, -4, -0.5, 1, 4, 1, "bone"), rot=(-0.3, 0, -0.3))
    m.part("horn_r_tip", "horn_r", (0, -4, 0), C(-0.5, -3, -0.5, 1, 3, 1, "bone"), rot=(-0.5, 0, -0.2))
    m.part("arm_r", "body", (-3.5, -6, 0), C(-1.5, -1, -1, 2, 10, 2, "imp_skin"))
    m.part("claw_r", "arm_r", (-0.5, 9, 0), C(-1.5, 0, -1.5, 3, 3, 3, "claw"))
    m.part("arm_l", "body", (3.5, -6, 0), C(-0.5, -1, -1, 2, 10, 2, "imp_skin"))
    m.part("claw_l", "arm_l", (0.5, 9, 0), C(-1.5, 0, -1.5, 3, 3, 3, "claw"))
    m.part("wing_l", "body", (2, -6, 2), C(0, -6, 0, 9, 8, 0, "membrane"), C(0, -6.5, -0.5, 9, 1, 1, "bone"))
    m.part("wing_r", "body", (-2, -6, 2), C(-9, -6, 0, 9, 8, 0, "membrane"), C(-9, -6.5, -0.5, 9, 1, 1, "bone"))
    m.part("tail", "body", (0, -0.5, 1.5), C(-0.5, -0.5, 0, 1, 1, 7, "imp_skin"))
    m.part("tail_tip", "tail", (0, 0, 7), C(-1.5, -0.5, 0, 3, 1, 3, "bone"))
    m.part("leg_r", "root", (-1.7, 18, 0.5), C(-1, 0, -1, 2, 6, 2, "imp_skin"), C(-1.5, 5, -2.5, 3, 1, 3, "claw"))
    m.part("leg_l", "root", (1.7, 18, 0.5), C(-1, 0, -1, 2, 6, 2, "imp_skin"), C(-1.5, 5, -2.5, 3, 1, 3, "claw"))
    return m


def elite():
    """Elite: duelista espectral de armadura negra e ouro, penacho carmesim, capa, espada rúnica e escudo."""
    m = Model("elite", 128)
    m.part("leg_r", "root", (-2.3, 9, 0),
           C(-1.5, 0, -1.5, 3, 15, 3, "steel"), C(-2, 5, -2.2, 4, 3, 4, "gold"), C(-2, 13, -3.5, 4, 2, 5, "black_steel"))
    m.part("leg_l", "root", (2.3, 9, 0),
           C(-1.5, 0, -1.5, 3, 15, 3, "steel"), C(-2, 5, -2.2, 4, 3, 4, "gold"), C(-2, 13, -3.5, 4, 2, 5, "black_steel"))
    m.part("body", "root", (0, 9, 0),
           C(-3.5, -4, -2.5, 7, 5, 5, "black_steel"),
           C(-4.5, -1, -3, 9, 5, 6, "black_steel"),
           C(-5, -14, -3, 10, 10, 6, "black_steel", "chest_sigil"),
           C(-3, -16, -3, 6, 2, 6, "gold"))
    m.part("head", "body", (0, -16, 0), C(-3.5, -8, -3.5, 7, 8, 7, "black_steel", "visor"))
    m.part("plume", "head", (0, -8, 0), C(-0.5, -6, -3, 1, 6, 11, "plume"))
    m.part("plume_tail", "plume", (0, -2, 8), C(-0.5, 0, 0, 1, 12, 2, "plume"), rot=(0.25, 0, 0))
    m.part("horn_l", "head", (3.5, -5, 0), C(0, -1, -1, 5, 2, 2, "gold"), rot=(0, 0, -0.6))
    m.part("horn_r", "head", (-3.5, -5, 0), C(-5, -1, -1, 5, 2, 2, "gold"), rot=(0, 0, 0.6))
    m.part("arm_r", "body", (-6.5, -13, 0), C(-1.5, -1, -1.5, 3, 14, 3, "steel"))
    m.part("pauldron_r", "arm_r", (0, 0, 0),
           C(-3.5, -3, -3.5, 6, 5, 7, "black_steel", inflate=0.2), C(-4, 1, -4, 7, 1, 8, "gold"),
           C(-2.5, -6, -0.5, 1, 3, 1, "bone"), C(-0.5, -5, -0.5, 1, 2, 1, "bone"))
    m.part("gauntlet_r", "arm_r", (0, 9, 0), C(-2, 0, -2, 4, 5, 4, "steel"))
    m.part("sword", "arm_r", (0, 12, -1),
           C(-0.5, -0.5, -1, 1, 1, 4, "leather"), C(-1, -1, 3, 2, 2, 2, "gold"),
           C(-3.5, -1, -2, 7, 2, 1, "gold"), C(-0.5, -1.5, -24, 1, 3, 22, "blade"), C(-0.5, -1, -26, 1, 2, 2, "blade"))
    m.part("arm_l", "body", (6.5, -13, 0), C(-1.5, -1, -1.5, 3, 14, 3, "steel"))
    m.part("pauldron_l", "arm_l", (0, 0, 0),
           C(-2.5, -3, -3.5, 6, 5, 7, "black_steel", inflate=0.2), C(-3, 1, -4, 7, 1, 8, "gold"),
           C(1.5, -6, -0.5, 1, 3, 1, "bone"), C(-0.5, -5, -0.5, 1, 2, 1, "bone"))
    m.part("gauntlet_l", "arm_l", (0, 9, 0), C(-2, 0, -2, 4, 5, 4, "steel"))
    m.part("shield", "arm_l", (2, 7, 0), C(0, -5, -5, 1, 10, 10, "black_steel", "shield_sigil"),
           C(1, -1.5, -1.5, 1, 3, 3, "gold"))
    m.part("cape", "body", (0, -15, 3), C(-5, 0, 0, 10, 22, 1, "cape"))
    return m


def guardian():
    """Guardião: oráculo flutuante de manto, gola-leque dourada, olho gigante no peito, auréola e mãos soltas."""
    m = Model("guardian", 128)
    m.part("body", "root", (0, 0, 0),
           C(-6, -10, -4, 12, 10, 8, "robe"),
           C(-8, -12, -5, 16, 3, 10, "gold"),
           C(-7, -18, 3, 14, 7, 2, "gold", "fan_collar"),
           C(-7, 0, -5, 14, 5, 10, "robe"),
           C(-5, 5, -4, 10, 5, 8, "robe"))
    m.part("eye", "body", (0, -5, -4), C(-4, -4, -1.5, 8, 8, 2, "gold"), C(-3, -3, -2, 6, 6, 1, "eye", "big_eye"))
    for i, (x, z) in enumerate([(-3, -2.5), (3, -2.5), (-3, 2.5), (3, 2.5)], start=1):
        m.part(f"tendril_{i}", "body", (x, 10, z), C(-1, 0, -1, 2, 8, 2, "robe"))
        m.part(f"tendril_{i}_tip", f"tendril_{i}", (0, 8, 0), C(-0.5, 0, -0.5, 1, 5, 1, "gem"))
    m.part("head", "body", (0, -12, 0), C(-4.5, -10, -4.5, 9, 10, 9, "robe", "void_face"))
    m.part("crown", "head", (0, -10, 0),
           C(-5, -1, -5, 10, 2, 10, "gold"),
           C(-4.5, -6, -4.5, 1, 5, 1, "gold"), C(3.5, -6, -4.5, 1, 5, 1, "gold"),
           C(-0.5, -8, -5, 1, 7, 1, "gold"), C(-4.5, -5, 3.5, 1, 4, 1, "gold"), C(3.5, -5, 3.5, 1, 4, 1, "gold"),
           C(-1, -3, -5.5, 2, 2, 1, "gem"))
    m.part("halo", "head", (0, -12, 5),
           C(-6, -6, 0, 12, 1, 1, "gem"), C(-6, 5, 0, 12, 1, 1, "gem"),
           C(-6, -5, 0, 1, 10, 1, "gem"), C(5, -5, 0, 1, 10, 1, "gem"))
    for side, x in (("r", -12), ("l", 12)):
        m.part(f"hand_{side}", "root", (x, -2, -3),
               C(-2, -2, -1.5, 4, 4, 3, "pale_skin"), C(-2.5, -4, -2, 5, 2, 4, "gold"),
               C(-2, 2, -1.5, 1, 4, 1, "claw"), C(-0.5, 2, -1.5, 1, 5, 1, "claw"), C(1, 2, -1.5, 1, 4, 1, "claw"))
    for i in range(1, 4):
        m.part(f"orb_{i}", "root", (0, -6, 0), C(-1.5, -1.5, -1.5, 3, 3, 3, "gem"))
    return m


def monarch():
    """Soberano: colosso-rei de pedra e obsidiana, coroa de espinhos, chifres em espiral, núcleo e cristais nas costas."""
    m = Model("monarch", 256)
    for side, x in (("r", -7), ("l", 7)):
        m.part(f"leg_{side}", "root", (x, 6, 0), C(-4, 0, -4, 8, 10, 8, "rock"))
        m.part(f"shin_{side}", f"leg_{side}", (0, 9, 0),
               C(-4.5, 0, -4.5, 9, 9, 9, "rock"), C(-5, -1, -5.5, 10, 4, 2, "obsidian"), C(-5, 6, -7, 10, 3, 11, "obsidian"))
    m.part("body", "root", (0, 6, 0),
           C(-9, -4, -5, 18, 6, 10, "obsidian"), C(-5, 2, -5.5, 10, 10, 1, "cape"))
    m.part("chest", "body", (0, -4, 0),
           C(-12, -24, -8, 24, 24, 16, "rock"),
           C(-7, -10, -8.5, 14, 8, 1, "obsidian"),
           C(-4, -19, -9, 8, 8, 1, "gem", "core"),
           rot=(0.2, 0, 0))
    for i, (x, rz) in enumerate([(-6, -0.35), (0, 0.0), (6, 0.35)], start=1):
        m.part(f"shard_{i}", "chest", (x, -18, 7), C(-1.5, -12, -1.5, 3, 12, 3, "gem"), rot=(-0.5, 0, rz))
    m.part("cape", "chest", (0, -23, 8), C(-11, 0, 0, 22, 26, 1, "cape"))
    m.part("head", "chest", (0, -24, -6), C(-5, -9, -6, 10, 10, 10, "rock", "colossus_face"))
    m.part("jaw", "head", (0, 0, -1),
           C(-5, 0, -5.5, 10, 3, 9, "rock", "teeth"), C(-5, -3, -6, 1, 4, 1, "bone"), C(4, -3, -6, 1, 4, 1, "bone"))
    m.part("crown", "head", (0, -9, 0),
           C(-6, -2, -7, 12, 3, 12, "gold", "crown_gems"),
           C(-5.5, -9, -6.5, 2, 7, 2, "gold"), C(-1, -11, -7, 2, 9, 2, "gold"), C(3.5, -9, -6.5, 2, 7, 2, "gold"),
           C(-5.5, -7, 3.5, 2, 5, 2, "gold"), C(3.5, -7, 3.5, 2, 5, 2, "gold"), C(-1, -4, -7.5, 2, 2, 1, "gem"))
    for side, sx in (("r", -1), ("l", 1)):
        m.part(f"horn_{side}", "head", (5 * sx, -6, -1), C(-8 if sx < 0 else 0, -2, -2, 8, 4, 4, "bone"), rot=(0, 0, 0.4 * sx))
        m.part(f"horn_{side}2", f"horn_{side}", (8 * sx, 0, 0), C(-1.5, -8, -1.5, 3, 8, 3, "bone"), rot=(0, 0, -0.9 * sx))
        m.part(f"horn_{side}3", f"horn_{side}2", (0, -8, 0), C(-1, -5, -1, 2, 5, 2, "bone"), rot=(0, 0, -0.5 * sx))
        m.part(f"arm_{side}", "chest", (15 * sx, -20, 0), C(-4, -2, -4.5, 8, 16, 9, "rock"))
        m.part(f"pauldron_{side}", f"arm_{side}", (0, 0, 0),
               C(-6 if sx < 0 else -5, -5, -6, 11, 8, 12, "obsidian"),
               C(-3, -10, -1, 2, 5, 2, "bone"), C(1, -9, -1, 2, 4, 2, "bone"))
        m.part(f"forearm_{side}", f"arm_{side}", (0, 14, 0),
               C(-5, 0, -5, 10, 14, 10, "rock"), C(-5.5, 2, -5.5, 11, 4, 11, "gold"))
        m.part(f"fist_{side}", f"forearm_{side}", (0, 14, 0), C(-6, 0, -6, 12, 9, 12, "obsidian"))
    return m


# ---------------------------------------------------------------------------------------------- UV

def regions(c):
    """Retângulos da caixa UV vanilla: (nome, x, y, w, h) relativos a (u, v)."""
    w, h, d = c.w, c.h, c.d
    return [
        ("top", d, 0, w, d), ("bottom", d + w, 0, w, d),
        ("east", 0, d, d, h), ("front", d, d, w, h), ("west", d + w, d, d, h), ("back", 2 * d + w, d, w, h),
    ]


def pack(model):
    """Empacotador em prateleiras: cubos maiores primeiro."""
    cubes = [c for p in model.parts for c in p.cubes]
    order = sorted(cubes, key=lambda c: -(c.d + c.h) * 1000 - (2 * (c.d + c.w)))
    x = y = shelf = 0
    for c in order:
        cw, ch = 2 * (c.d + c.w), c.d + c.h
        if x + cw > model.tex_w:
            x, y, shelf = 0, y + shelf, 0
        c.u, c.v = x, y
        x += cw
        shelf = max(shelf, ch)
    h = y + shelf
    model.tex_h = 1 << (max(h, 1) - 1).bit_length()


# ---------------------------------------------------------------------------------------------- pintura

def rgb(hexstr):
    return tuple(int(hexstr[i:i + 2], 16) for i in (1, 3, 5))


def shade(col, k):
    return tuple(max(0, min(255, int(v * k))) for v in col)


def mix(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))


MATERIALS = {
    # nome: (cor base, cor escura, cor clara)
    "imp_skin": ("#5b2a4a", "#2e1027", "#8a3f6b"),
    "imp_belly": ("#9c5a6e", "#6b3346", "#c58a97"),
    "membrane": ("#7a1f2e", "#3d0c16", "#b5394a"),
    "bone": ("#e6dcc0", "#a69777", "#fffaf0"),
    "claw": ("#2a2a33", "#111116", "#4b4b5a"),
    "steel": ("#7c8796", "#4a525e", "#c3ccd8"),
    "black_steel": ("#262433", "#110f18", "#4d4766"),
    "gold": ("#d4a52a", "#8a6410", "#ffe27a"),
    "plume": ("#b3122e", "#6b0819", "#ff4d63"),
    "leather": ("#5a3a22", "#2f1d10", "#7d5534"),
    "blade": ("#cfd8e3", "#8c99a8", "#ffffff"),
    "cape": ("#3b1a7a", "#1e0b45", "#6a3fc0"),
    "robe": ("#173a4d", "#0a1c27", "#2a6680"),
    "pale_skin": ("#b9c6d6", "#7c8aa0", "#e3ecf7"),
    "eye": ("#f2efe6", "#c9c2b0", "#ffffff"),
    "gem": ("#d0f0ff", "#7fb0c8", "#ffffff"),
    "rock": ("#4a4540", "#2a2622", "#6e6760"),
    "obsidian": ("#1c1424", "#0b0710", "#3a2a4d"),
    # fogo
    "char": ("#2a1a14", "#120a08", "#4a2a1e"),
    "magma": ("#5a1e10", "#2a0c06", "#8a3418"),
    "ember": ("#ff8a1e", "#c4400a", "#ffc061"),
    "flame": ("#ffb21e", "#d2280a", "#fff4a0"),
    "brass": ("#b8862e", "#6e4c12", "#e8c070"),
    # água
    "scales": ("#1f6f7a", "#0d3a42", "#3fa3a8"),
    "fin": ("#57d2e8", "#2a5fa8", "#b8f4ff"),
    "coral": ("#ff6f61", "#b8403a", "#ffb199"),
    "foam": ("#e8fbff", "#a8d8e8", "#ffffff"),
    "pearl": ("#f4f0ff", "#c8c0e8", "#ffffff"),
    "deep": ("#13254a", "#070f22", "#27477a"),
    "kelp": ("#2d5a1e", "#16300c", "#4f8a2e"),
    "jelly": ("#8a7ad8", "#4a3a98", "#c8b8ff"),
    # terra
    "stone": ("#7a7a72", "#4a4a44", "#a2a29a"),
    "moss": ("#4f7a2a", "#2a4a16", "#7aa84a"),
    "crystal": ("#b06cff", "#6a2ab8", "#e4c8ff"),
    "bark": ("#5a3a20", "#2e1c0e", "#7a5432"),
    "leaf": ("#2f8a2a", "#1a5216", "#5cc24a"),
    "amber": ("#ffb02e", "#c47a0a", "#ffe08a"),
    # ar
    "cloud": ("#eef4fa", "#b8c8d8", "#ffffff"),
    "feather": ("#f2f2ee", "#b0b0aa", "#ffffff"),
    "silver": ("#c8d0dc", "#8890a0", "#f4f8ff"),
    "wind": ("#9ff3ff", "#4ab8d8", "#e8ffff"),
    "storm": ("#4a5468", "#262c3a", "#6a7488"),
    "lightning": ("#fff59d", "#ffd21e", "#ffffff"),
    "rain": ("#8ec9ff", "#4a8ad8", "#d8f0ff"),
}

# Materiais que brilham por inteiro (camada emissiva) e com que intensidade.
FULL_GLOW = {"gem": 220, "flame": 255, "ember": 140, "pearl": 230, "crystal": 220, "amber": 255,
             "wind": 200, "lightning": 255, "rain": 120}


def paint_material(img, glow, rect, mat, rnd, face):
    x0, y0, w, h = rect
    base, dark, light = (rgb(c) for c in MATERIALS[mat])
    for yy in range(h):
        for xx in range(w):
            t = yy / max(1, h - 1)
            n = rnd.random()
            col = base
            g = 0  # 0..255 brilho emissivo
            if mat in ("imp_skin", "imp_belly", "pale_skin"):
                col = mix(base, dark, t * 0.5)
                if n < 0.18: col = shade(col, 0.82)
                elif n > 0.92: col = mix(col, light, 0.6)
            elif mat == "membrane":
                col = mix(light, dark, t)
                if (xx + yy * 2) % 5 == 0: col = shade(dark, 0.8)  # veias
            elif mat == "bone":
                col = mix(light, dark, t * 0.8)
                if yy % 3 == 2: col = shade(col, 0.85)  # anéis
            elif mat in ("steel", "black_steel", "obsidian"):
                col = mix(light, base, min(1.0, t * 1.6))
                if xx == 0 or yy == 0 or xx == w - 1 or yy == h - 1:
                    col = light if mat != "obsidian" else mix(light, base, 0.3)
                if (xx in (1, w - 2)) and (yy in (1, h - 2)) and w > 4 and h > 4:
                    col = rgb("#ffe27a")  # rebites dourados
                if mat == "obsidian" and n > 0.94:
                    col = mix(light, (190, 120, 255), 0.5)
                    g = 160
                if mat == "black_steel" and n > 0.97:
                    g = 90
            elif mat == "gold":
                col = mix(light, base, t)
                if (xx + yy) % 4 == 0: col = mix(col, light, 0.6)
                if n < 0.1: col = dark
            elif mat == "plume":
                col = mix(light, dark, (xx % 3) / 3 * 0.5 + t * 0.5)
            elif mat == "leather":
                col = base if (yy % 2) else dark
            elif mat == "blade":
                col = mix(light, dark, abs(yy - h / 2) / max(1, h / 2))
                center = (h // 2 == yy) if face in ("east", "west") else (w // 2 == xx)
                if center and n > 0.35:
                    col = (120, 220, 255)
                    g = 255  # runas
            elif mat == "cape":
                col = mix(light, dark, t * 0.7)
                if xx % 3 == 0: col = shade(col, 0.75)  # dobras
                if yy >= h - 2: col = rgb("#d4a52a")  # barra dourada
                if yy < h - 2 and n > 0.985: g = 120
            elif mat == "robe":
                col = mix(light, dark, t * 0.8)
                if xx % 4 == 0: col = shade(col, 0.8)
                if yy in (0, 1) or yy == h - 1: col = rgb("#d4a52a")
                elif yy == 2 and xx % 3 == 1:
                    col = rgb("#9ff3ff")
                    g = 255  # bordado de runas
            elif mat == "eye":
                col = base
            elif mat == "gem":
                col = mix(light, base, n * 0.6)
                g = 200 + int(n * 55)
            elif mat == "rock":
                col = mix(light, base, t * 0.8)
                if n < 0.25: col = shade(col, 0.85)
                elif n > 0.9: col = mix(col, light, 0.5)
            elif mat == "claw":
                col = mix(light, dark, t)
            elif mat in ("char", "deep", "storm"):
                col = mix(light, base, t * 0.7)
                if n < 0.2: col = shade(col, 0.8)
                if n > 0.95:  # brasas / bioluminescência / faíscas
                    col = {"char": (255, 90, 20), "deep": (90, 220, 255), "storm": (255, 245, 157)}[mat]
                    g = 200
            elif mat == "magma":
                col = mix(light, base, t * 0.8)
                if n < 0.3: col = shade(col, 0.75)
            elif mat in ("ember", "flame", "lightning", "amber", "rain"):
                # Gradiente vertical: claro em cima, escuro embaixo (chama "de pé").
                col = mix(light, dark, t)
                if n > 0.85: col = mix(col, light, 0.6)
            elif mat == "brass":
                col = mix(light, base, t)
                if (xx + yy) % 5 == 0: col = mix(col, light, 0.5)
            elif mat == "scales":
                col = mix(light, base, t * 0.6)
                if (xx + (yy // 2) % 2 * 2) % 4 == 0 or yy % 2 == 1 and (xx + 1) % 4 == 0: col = shade(dark, 1.2)
            elif mat == "fin":
                col = mix(light, base, xx / max(1, w - 1))
                if xx % 2 == 0: col = mix(col, dark, 0.35)
            elif mat == "coral":
                col = base if n > 0.3 else (light if n > 0.15 else dark)
            elif mat in ("foam", "cloud", "pearl"):
                col = mix(light, base, n * 0.7)
            elif mat == "kelp":
                col = mix(light, dark, t)
                if xx % 3 == 1: col = shade(col, 0.8)
            elif mat == "jelly":
                col = mix(light, base, t)
                if n > 0.85:
                    col = mix(col, (240, 220, 255), 0.6)
                    g = 140
            elif mat == "stone":
                col = mix(light, base, n * 0.8)
                if xx % 4 == 0 or yy % 3 == 0: col = shade(base, 0.8)  # junta do calçamento
            elif mat == "moss":
                col = base if n > 0.4 else (light if n > 0.2 else dark)
            elif mat == "crystal":
                col = mix(light, base, (xx + yy) % 4 / 4)
            elif mat == "bark":
                col = base if xx % 3 else dark
                if n > 0.9: col = light
            elif mat == "leaf":
                col = base if n > 0.45 else (light if n > 0.25 else dark)
            elif mat == "feather":
                col = mix(light, dark, t * 0.5)
                if (xx - yy) % 3 == 0: col = shade(col, 0.88)  # barbas da pena
                if yy >= h - 2: col = dark
            elif mat == "silver":
                col = mix(light, base, min(1.0, t * 1.4))
                if xx == 0 or yy == 0 or xx == w - 1 or yy == h - 1: col = light
                if (xx in (1, w - 2)) and (yy in (1, h - 2)) and w > 4 and h > 4:
                    col = rgb("#9ff3ff")
                    g = 180
            elif mat == "wind":
                col = mix(light, base, t)
                if (xx * 2 + yy) % 5 == 0: col = light  # rajadas
            if mat in FULL_GLOW:
                g = max(g, FULL_GLOW[mat])
            img.putpixel((x0 + xx, y0 + yy), col + (255,))
            if g:
                glow.putpixel((x0 + xx, y0 + yy), (g, g, g, 255))
    if mat in ("rock", "magma"):
        # Rachaduras incandescentes: caminhadas aleatórias que brilham.
        for _ in range(max(1, (w * h) // 40)):
            cx, cy = rnd.randrange(w), rnd.randrange(h)
            for _ in range(rnd.randint(3, 7)):
                img.putpixel((x0 + cx, y0 + cy), (255, 160, 60, 255))
                glow.putpixel((x0 + cx, y0 + cy), (255, 255, 255, 255))
                cx = max(0, min(w - 1, cx + rnd.choice((-1, 0, 1))))
                cy = max(0, min(h - 1, cy + rnd.choice((0, 1))))


def put(img, x, y, col):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), col + (255,) if len(col) == 3 else col)


def paint_decal(img, glow, rect, decal):
    x0, y0, w, h = rect
    W = (255, 255, 255)
    if decal == "imp_face":
        # Olhos enormes, oblíquos e brilhantes + sobrancelhas pesadas.
        for (ex, flip) in ((1, 1), (w - 4, -1)):
            for dx in range(3):
                for dy in range(2):
                    put(img, x0 + ex + dx, y0 + 3 + dy + (dx if flip < 0 else 2 - dx) // 2, (255, 230, 90))
                    put(glow, x0 + ex + dx, y0 + 3 + dy + (dx if flip < 0 else 2 - dx) // 2, W)
            for dx in range(3):
                put(img, x0 + ex + dx, y0 + 2, (30, 8, 25))
        put(img, x0 + w // 2, y0 + 5, (20, 5, 15))
        for dx in range(1, w - 1):
            put(img, x0 + dx, y0 + h - 1, (25, 5, 15))
    elif decal == "teeth":
        for dx in range(w):
            put(img, x0 + dx, y0, (240, 235, 210) if dx % 2 == 0 else (60, 10, 20))
        for dx in range(w):
            for dy in range(1, h):
                put(img, x0 + dx, y0 + dy, (60, 10, 20))
    elif decal == "visor":
        for dx in range(w):
            put(img, x0 + dx, y0 + 3, (5, 5, 8))
            put(img, x0 + dx, y0 + 4, (5, 5, 8))
        for ex in (1, w - 3):
            for dx in range(2):
                put(img, x0 + ex + dx, y0 + 3, (140, 255, 240))
                put(glow, x0 + ex + dx, y0 + 3, W)
        for dy in range(5, h):
            put(img, x0 + w // 2, y0 + dy, (212, 165, 42))
    elif decal == "chest_sigil":
        cx, cy = x0 + w // 2, y0 + h // 2
        for (dx, dy) in [(0, -3), (0, -2), (0, -1), (0, 0), (0, 1), (0, 2), (-1, -1), (1, -1), (-2, 0), (2, 0),
                         (-1, 1), (1, 1), (-1, 3), (0, 3)]:
            put(img, cx + dx, cy + dy, (200, 255, 250))
            put(glow, cx + dx, cy + dy, W)
    elif decal == "shield_sigil":
        cx, cy = x0 + w // 2, y0 + h // 2
        for r in range(4):
            for (dx, dy) in ((r, 0), (-r, 0), (0, r), (0, -r)):
                put(img, cx + dx, cy + dy, (230, 200, 90))
        for (dx, dy) in ((2, 2), (-2, 2), (2, -2), (-2, -2)):
            put(img, cx + dx, cy + dy, (200, 255, 250))
            put(glow, cx + dx, cy + dy, W)
    elif decal == "big_eye":
        cx, cy = x0 + w / 2 - 0.5, y0 + h / 2 - 0.5
        for yy in range(h):
            for xx in range(w):
                d = ((x0 + xx - cx) ** 2 + (y0 + yy - cy) ** 2) ** 0.5
                if d < 1.0:
                    put(img, x0 + xx, y0 + yy, (5, 5, 5))
                elif d < 2.3:
                    put(img, x0 + xx, y0 + yy, (255, 255, 255))
                    put(glow, x0 + xx, y0 + yy, W)
    elif decal == "void_face":
        for yy in range(2, h):
            for xx in range(1, w - 1):
                put(img, x0 + xx, y0 + yy, (4, 4, 10) if (xx * 7 + yy * 3) % 11 else (60, 60, 120))
        for ex in (2, w - 4):
            for dx in range(2):
                put(img, x0 + ex + dx, y0 + 5, (255, 255, 255))
                put(glow, x0 + ex + dx, y0 + 5, W)
    elif decal == "fan_collar":
        for xx in range(w):
            if xx % 2 == 0:
                for yy in range(h):
                    put(img, x0 + xx, y0 + yy, (255, 226, 122) if yy < h - 1 else (138, 100, 16))
            if xx % 4 == 1:
                put(img, x0 + xx, y0 + 1, (159, 243, 255))
                put(glow, x0 + xx, y0 + 1, W)
    elif decal == "colossus_face":
        for xx in range(w):
            put(img, x0 + xx, y0 + 3, (30, 26, 22))
        for ex in (2, w - 4):
            for dx in range(2):
                for dy in range(2):
                    put(img, x0 + ex + dx, y0 + 4 + dy, (255, 140, 40))
                    put(glow, x0 + ex + dx, y0 + 4 + dy, W)
        for dy in range(6, h):
            put(img, x0 + w // 2, y0 + dy, (30, 26, 22))
    elif decal == "core":
        cx, cy = x0 + w / 2 - 0.5, y0 + h / 2 - 0.5
        for yy in range(h):
            for xx in range(w):
                d = ((x0 + xx - cx) ** 2 + (y0 + yy - cy) ** 2) ** 0.5
                if d < 3.8:
                    v = int(255 - d * 30)
                    put(img, x0 + xx, y0 + yy, (255, v, 80))
                    put(glow, x0 + xx, y0 + yy, (255, 255, 255))
    elif decal == "crown_gems":
        for xx in range(1, w, 3):
            put(img, x0 + xx, y0 + 1, (200, 240, 255))
            put(glow, x0 + xx, y0 + 1, W)
    elif decal in FACES:
        FACES[decal](img, glow, x0, y0, w, h)


# ---------------------------------------------------------------------------------------------- rostos

def glow_px(img, glow, x, y, col):
    put(img, x, y, col)
    put(glow, x, y, (255, 255, 255))


def eyes(col, ew=2, eh=1, ey=0.4, gap=0.25, dark=None):
    """Dois olhos brilhantes simétricos; {@code dark} pinta uma faixa escura atrás (órbitas/viseira)."""
    def draw(img, glow, x0, y0, w, h):
        y = y0 + int(h * ey)
        if dark:
            for xx in range(w):
                for dy in range(-1, eh + 1):
                    put(img, x0 + xx, y + dy, dark)
        left = x0 + max(0, int(w * gap) - ew // 2)
        right = x0 + w - 1 - max(0, int(w * gap) - ew // 2) - (ew - 1)
        for ex in (left, right):
            for dx in range(ew):
                for dy in range(eh):
                    glow_px(img, glow, ex + dx, y + dy, col)
    return draw


def combo(*parts):
    def draw(img, glow, x0, y0, w, h):
        for p in parts:
            p(img, glow, x0, y0, w, h)
    return draw


def mouth(col, row=0.8, teeth=None):
    def draw(img, glow, x0, y0, w, h):
        y = y0 + int(h * row)
        for xx in range(1, w - 1):
            put(img, x0 + xx, y, teeth if teeth and xx % 2 else col)
    return draw


def vline(col, glowing=True):
    def draw(img, glow, x0, y0, w, h):
        for yy in range(1, h - 1):
            (glow_px if glowing else lambda a, b, x, y, c: put(a, x, y, c))(img, glow, x0 + w // 2, y0 + yy, col)
    return draw


def diamond(col, r=2):
    def draw(img, glow, x0, y0, w, h):
        cx, cy = x0 + w // 2, y0 + h // 2
        for dy in range(-r, r + 1):
            for dx in range(-(r - abs(dy)), r - abs(dy) + 1):
                glow_px(img, glow, cx + dx, cy + dy, col)
    return draw


def runes(col, step=3):
    def draw(img, glow, x0, y0, w, h):
        for yy in range(1, h - 1, step):
            for xx in range(1, w - 1, 2):
                if (xx * 7 + yy * 3) % 5 < 2:
                    glow_px(img, glow, x0 + xx, y0 + yy, col)
    return draw


FACES = {
    "skull_fire": combo(eyes((255, 140, 30), 2, 2, 0.35, 0.28, dark=(10, 4, 2)), mouth((10, 4, 2), 0.85, (230, 220, 200))),
    "fish_face": combo(eyes((140, 255, 240), 2, 2, 0.3, 0.12, dark=None), mouth((5, 10, 25), 0.85)),
    "stone_face": eyes((200, 120, 255), 2, 1, 0.45, 0.25, dark=(40, 40, 36)),
    "bird_face": eyes((120, 230, 255), 2, 2, 0.35, 0.22),
    "visor_fire": combo(eyes((255, 160, 40), 3, 1, 0.4, 0.22, dark=(8, 4, 2)), vline((255, 120, 20))),
    "chest_rune": diamond((255, 170, 60), 3),
    "shell_sigil": diamond((240, 250, 255), 2),
    "crystal_heart": diamond((200, 140, 255), 3),
    "visor_earth": eyes((180, 255, 120), 2, 1, 0.45, 0.25, dark=(30, 30, 26)),
    "crystal_rune": runes((200, 140, 255)),
    "visor_air": combo(eyes((150, 240, 255), 2, 1, 0.4, 0.2, dark=(60, 70, 90)), vline((150, 240, 255))),
    "chest_wind": runes((160, 245, 255), 2),
    "sun_face": combo(eyes((255, 255, 200), 2, 2, 0.4, 0.28), mouth((255, 220, 120), 0.75)),
    "mask_face": combo(eyes((255, 220, 120), 2, 1, 0.4, 0.22, dark=(40, 24, 8)), mouth((40, 24, 8), 0.8)),
    "rune_column": runes((200, 140, 255), 2),
    "kraken_face": combo(eyes((120, 255, 220), 3, 2, 0.35, 0.18, dark=(4, 8, 20))),
    "tree_face": combo(eyes((255, 190, 60), 2, 2, 0.4, 0.25, dark=(20, 10, 4)), mouth((20, 10, 4), 0.8)),
    "ember_face": combo(eyes((255, 200, 60), 3, 2, 0.4, 0.2, dark=(10, 4, 2)), mouth((255, 90, 20), 0.85)),
}


def paint(model):
    img = Image.new("RGBA", (model.tex_w, model.tex_h), (0, 0, 0, 0))
    glow = Image.new("RGBA", (model.tex_w, model.tex_h), (0, 0, 0, 0))
    rnd = random.Random(model.name)
    for p in model.parts:
        for c in p.cubes:
            for face, rx, ry, rw, rh in regions(c):
                if rw <= 0 or rh <= 0:
                    continue
                paint_material(img, glow, (c.u + rx, c.v + ry, rw, rh), c.mat, rnd, face)
                if c.decal and face == "front":
                    paint_decal(img, glow, (c.u + rx, c.v + ry, rw, rh), c.decal)
    os.makedirs(TEX_DIR, exist_ok=True)
    img.save(os.path.join(TEX_DIR, model.name + ".png"))
    glow.save(os.path.join(TEX_DIR, model.name + "_glow.png"))


# ---------------------------------------------------------------------------------------------- Java

def f(v):
    s = ("%.3f" % v).rstrip("0").rstrip(".")
    return (s if s not in ("-0", "") else "0") + "F"


def java_method(model):
    lines = [f"    /** Gerado de tools/gen_mob_art.py ({model.name}). */",
             f"    private static LayerDefinition {camel(model.name)}() {{",
             "        MeshDefinition mesh = new MeshDefinition();",
             "        PartDefinition root = mesh.getRoot();"]
    for p in model.parts:
        cubes = "CubeListBuilder.create()"
        for c in p.cubes:
            deform = f", new CubeDeformation({f(c.inflate)})" if c.inflate else ""
            cubes += (f"\n                .texOffs({c.u}, {c.v}).addBox({f(c.x)}, {f(c.y)}, {f(c.z)}, "
                      f"{f(c.w)}, {f(c.h)}, {f(c.d)}{deform})")
        px, py, pz = p.pivot
        rx, ry, rz = p.rot
        pose = (f"PartPose.offsetAndRotation({f(px)}, {f(py)}, {f(pz)}, {f(rx)}, {f(ry)}, {f(rz)})"
                if any(p.rot) else f"PartPose.offset({f(px)}, {f(py)}, {f(pz)})")
        var = "root" if p.parent == "root" else "p_" + p.parent
        lines.append(f"        PartDefinition p_{p.name} = {var}.addOrReplaceChild(\"{p.name}\", {cubes},\n"
                     f"                {pose});")
    lines.append(f"        return LayerDefinition.create(mesh, {model.tex_w}, {model.tex_h});")
    lines.append("    }")
    return "\n".join(lines)


DECOR_PREFIXES = ("spin_", "roll_", "orbit_", "flick_", "sway_", "bob_", "pulse_", "flap_")


def camel(name):
    head, *rest = name.split("_")
    return head + "".join(w.capitalize() for w in rest)


def write_java(models):
    body = "\n\n".join(java_method(m) for m in models)
    cases = "\n".join(f'            case "{m.name}" -> {camel(m.name)}();' for m in models)
    decor = ",\n".join(
        f'            Map.entry("{m.name}", new String[]{{{", ".join(chr(34) + p.name + chr(34) for p in m.parts if p.name.startswith(DECOR_PREFIXES))}}})'
        for m in models)
    names = ", ".join(chr(34) + m.name + chr(34) for m in models)
    src = f"""package com.tarcitani.solomod.client.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import java.util.Map;

/**
 * Malhas dos mobs do mod: a forma base de cada criatura e uma variante por elemento.
 * ARQUIVO GERADO por tools/gen_mob_art.py — edite o script, não este arquivo,
 * senão as UVs deixam de bater com as texturas.
 */
@SuppressWarnings("unused")
public final class MobMeshes {{
    private MobMeshes() {{}}

    /** Todas as variantes (nome = textura em textures/entity/&lt;nome&gt;.png). */
    public static final String[] VARIANTS = {{{names}}};

    /** Partes decorativas de cada variante, animadas por prefixo em TierMobModel. */
    public static final Map<String, String[]> DECOR = Map.ofEntries(
{decor});

    public static LayerDefinition get(String variant) {{
        return switch (variant) {{
{cases}
            default -> throw new IllegalArgumentException("Variante desconhecida: " + variant);
        }};
    }}

{body}
}}
"""
    os.makedirs(os.path.dirname(JAVA_OUT), exist_ok=True)
    with open(JAVA_OUT, "w", encoding="utf-8") as fh:
        fh.write(src)


if __name__ == "__main__":
    import sys
    sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
    from mob_variants import VARIANTS, sovereign
    models = [minion(), elite(), guardian(), monarch()] + [v() for v in VARIANTS] + [sovereign()]
    for m in models:
        names = [p.name for p in m.parts]
        assert len(names) == len(set(names)), f"nomes repetidos em {m.name}"
        assert all(p.parent == "root" or p.parent in names for p in m.parts), f"pai ausente em {m.name}"
        pack(m)
        paint(m)
        print(f"{m.name}: textura {m.tex_w}x{m.tex_h}, {len(m.parts)} partes")
    write_java(models)
    print("ok")
