"""
Gera os modelos 3D da rapieira e da claymore (espada longa), a textura de peças que eles usam,
o contorno "fantasma" da adaga arremessada e os modelos dos ovos das criaturas.

Os modelos 3D são montados na vertical e inclinados -45° em Z: ficam na mesma diagonal dos sprites
vanilla (cabo embaixo à esquerda, ponta em cima à direita), então reaproveitam as poses de mão da
espada vanilla. Faces com tintindex 1 recebem a cor do elemento (mesmo color handler das outras armas).

Rodar da raiz do projeto:  python tools/gen_weapon_models.py
"""
import json
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src/main/resources/assets/solomod")
MODELS = os.path.join(ASSETS, "models/item")
TEXTURES = os.path.join(ASSETS, "textures/item")

# Amostras 4x4 na textura de peças: nome -> (x, y, cor clara, cor escura)
SWATCHES = {
    "steel": (0, 0, (214, 222, 232), (140, 152, 168)),
    "dark": (4, 0, (70, 72, 86), (34, 34, 44)),
    "gold": (8, 0, (255, 226, 122), (176, 128, 24)),
    "leather": (12, 0, (122, 84, 52), (70, 44, 24)),
    "tint": (0, 4, (255, 255, 255), (220, 220, 220)),
    "edge": (4, 4, (250, 252, 255), (200, 214, 230)),
    "wrap": (8, 4, (52, 30, 20), (30, 16, 10)),
}


def write_parts_texture():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for x0, y0, light, dark in SWATCHES.values():
        for y in range(4):
            for x in range(4):
                t = y / 3
                col = tuple(int(light[i] + (dark[i] - light[i]) * t) for i in range(3))
                img.putpixel((x0 + x, y0 + y), col + (255,))
    img.save(os.path.join(TEXTURES, "weapon_parts.png"))


def el(frm, to, swatch, tint=False):
    x0, y0, _, _ = SWATCHES[swatch]
    face = {"uv": [x0, y0, x0 + 4, y0 + 4], "texture": "#parts"}
    if tint:
        face["tintindex"] = 1
    return {
        "from": frm, "to": to,
        "rotation": {"angle": -45, "axis": "z", "origin": [8, 8, 8]},
        "faces": {d: dict(face) for d in ("north", "south", "east", "west", "up", "down")},
    }


DISPLAY = {
    "thirdperson_righthand": {"rotation": [0, -90, 55], "translation": [0, 4, 0.5], "scale": [0.85, 0.85, 0.85]},
    "thirdperson_lefthand": {"rotation": [0, 90, -55], "translation": [0, 4, 0.5], "scale": [0.85, 0.85, 0.85]},
    "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [1.13, 3.2, 1.13], "scale": [0.68, 0.68, 0.68]},
    "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [1.13, 3.2, 1.13], "scale": [0.68, 0.68, 0.68]},
    "gui": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.58, 0.58, 0.58]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.4, 0.4, 0.4]},
    "fixed": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [0.7, 0.7, 0.7]},
    "head": {"rotation": [0, 180, 0], "translation": [0, 13, 7], "scale": [1, 1, 1]},
}


def rapier():
    """Rapieira: lâmina fina e bem mais comprida que uma espada, guarda em cruz e arco de mão."""
    return [
        el([7, -1.5, 7], [9, 0.5, 9], "gold"),
        el([7.4, 0.5, 7.4], [8.6, 4.5, 8.6], "wrap"),
        el([5.8, 0.5, 7.75], [6.4, 5, 8.25], "gold"),
        el([6.4, 0.5, 7.75], [7.4, 1.1, 8.25], "gold"),
        el([4.5, 4.5, 7.6], [11.5, 5.2, 8.4], "gold"),
        el([4, 4.3, 7.5], [4.7, 5.4, 8.5], "tint", True),
        el([11.3, 4.3, 7.5], [12, 5.4, 8.5], "tint", True),
        el([6.5, 5.2, 6.9], [9.5, 5.8, 9.1], "steel"),
        el([7.5, 4.4, 7.3], [8.5, 5.4, 8.7], "tint", True),
        el([7.7, 5.8, 7.75], [8.3, 26, 8.25], "steel"),
        el([7.88, 7, 7.7], [8.12, 23, 8.3], "tint", True),
        el([7.8, 26, 7.8], [8.2, 28.5, 8.2], "edge"),
    ]


def claymore():
    """Claymore: cabo longo de duas mãos, guarda larga com pontas em trevo e lâmina larga."""
    return [
        el([6.6, -5, 6.6], [9.4, -2.6, 9.4], "gold"),
        el([7.4, -4.4, 6.4], [8.6, -3.2, 9.6], "tint", True),
        el([7.2, -2.6, 7.2], [8.8, 4, 8.8], "leather"),
        el([7.1, -1, 7.1], [8.9, -0.5, 8.9], "wrap"),
        el([7.1, 1.5, 7.1], [8.9, 2, 8.9], "wrap"),
        el([2.5, 4, 7.3], [13.5, 5.6, 8.7], "dark"),
        el([1.5, 3.4, 7.1], [3, 6.2, 8.9], "gold"),
        el([13, 3.4, 7.1], [14.5, 6.2, 8.9], "gold"),
        el([1.8, 4.2, 6.9], [2.7, 5.4, 9.1], "tint", True),
        el([13.3, 4.2, 6.9], [14.2, 5.4, 9.1], "tint", True),
        el([7.2, 4.6, 6.9], [8.8, 6.2, 9.1], "tint", True),
        el([6.9, 5.6, 7.6], [9.1, 9, 8.4], "steel"),
        el([6.6, 9, 7.7], [9.4, 28, 8.3], "steel"),
        el([6.4, 9, 7.75], [6.6, 27, 8.25], "edge"),
        el([9.4, 9, 7.75], [9.6, 27, 8.25], "edge"),
        el([7.7, 9.5, 7.6], [8.3, 25, 8.4], "tint", True),
        el([7, 28, 7.75], [9, 30, 8.25], "steel"),
        el([7.6, 30, 7.8], [8.4, 31.5, 8.2], "edge"),
    ]


def model_3d(elements):
    return {"gui_light": "front", "textures": {"parts": "solomod:item/weapon_parts", "particle": "solomod:item/weapon_parts"},
            "display": DISPLAY, "elements": elements}


def write_ghost_dagger():
    """Contorno translúcido da adaga: o que sobra na mão enquanto ela está arremessada."""
    base = Image.open(os.path.join(TEXTURES, "dagger.png")).convert("RGBA")
    over = Image.open(os.path.join(TEXTURES, "dagger_overlay.png")).convert("RGBA")
    w, h = base.size
    solid = [[base.getpixel((x, y))[3] > 0 or over.getpixel((x, y))[3] > 0 for x in range(w)] for y in range(h)]
    out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    for y in range(h):
        for x in range(w):
            if not solid[y][x]:
                continue
            edge = any(not (0 <= x + dx < w and 0 <= y + dy < h and solid[y + dy][x + dx])
                       for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            out.putpixel((x, y), (170, 235, 255, 170) if edge else (220, 245, 255, 45))
    out.save(os.path.join(TEXTURES, "dagger_thrown.png"))


def dump(name, data):
    with open(os.path.join(MODELS, name + ".json"), "w", encoding="utf-8") as fh:
        json.dump(data, fh, indent=2)
        fh.write("\n")


FLAT = ("greatsword", "axe", "dagger", "kunai", "wand")


def element_overrides(name):
    """Overrides por elemento (solomod:element = 1..4); o último que casa vence, então vão em ordem crescente."""
    from gen_weapon_elements import ELEMENTS
    return [{"predicate": {"solomod:element": i}, "model": f"solomod:item/{name}_{e}"} for i, e in enumerate(ELEMENTS, start=1)]


def untinted(elements):
    for e in elements:
        for face in e["faces"].values():
            face.pop("tintindex", None)
    return elements


if __name__ == "__main__":
    import sys
    sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
    from gen_weapon_elements import ELEMENTS, restyle, parts_texture, extras

    write_parts_texture()
    write_ghost_dagger()

    # Forma base (armas sem elemento: tier Normal ou elemento "nenhum"), com os overrides por elemento.
    base_3d = {"rapier": rapier, "longsword": claymore}
    for name, build in base_3d.items():
        dump(name, dict(model_3d(build()), overrides=element_overrides(name)))
    for name in FLAT:
        model = {"parent": "minecraft:item/handheld",
                 "textures": {"layer0": f"solomod:item/{name}", "layer1": f"solomod:item/{name}_overlay"},
                 "overrides": element_overrides(name)}
        if name == "dagger":  # adaga arremessada: o fantasma vence qualquer elemento (vem por último)
            model["overrides"].append({"predicate": {"solomod:thrown": 1}, "model": "solomod:item/dagger_thrown"})
        dump(name, model)

    # Uma versão de cada arma por elemento.
    for element in ELEMENTS:
        parts_texture(element, os.path.join(TEXTURES, f"weapon_parts_{element}.png"))
        for name, build in base_3d.items():
            weapon = "rapier" if name == "rapier" else "claymore"
            model = model_3d(untinted(build() + extras(weapon, element, el)))
            model["textures"] = {"parts": f"solomod:item/weapon_parts_{element}", "particle": f"solomod:item/weapon_parts_{element}"}
            dump(f"{name}_{element}", model)
        for name in FLAT:
            img = restyle(os.path.join(TEXTURES, f"{name}.png"), os.path.join(TEXTURES, f"{name}_overlay.png"), element, name)
            img.save(os.path.join(TEXTURES, f"{name}_{element}.png"))
            dump(f"{name}_{element}", {"parent": "minecraft:item/handheld",
                                       "textures": {"layer0": f"solomod:item/{name}_{element}"}})

    dump("dagger_thrown", {
        "parent": "minecraft:item/handheld",
        "render_type": "minecraft:translucent",
        "textures": {"layer0": "solomod:item/dagger_thrown"},
    })
    # Ovos: um por criatura e elemento ("minion_spawn_egg" = sem elemento, "minion_fire_spawn_egg"...).
    for egg in ("minion", "elite", "guardian", "monarch"):
        for suffix in ("",) + tuple("_" + e for e in ELEMENTS):
            dump(f"{egg}{suffix}_spawn_egg", {"parent": "minecraft:item/template_spawn_egg"})
    # O Soberano não tem elemento: um ovo só.
    dump("sovereign_spawn_egg", {"parent": "minecraft:item/template_spawn_egg"})
    print("ok")
