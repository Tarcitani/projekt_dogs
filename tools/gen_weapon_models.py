"""
Gera os modelos 3D da rapieira, espada longa, adaga e martelo, ícones pixel art exclusivos da GUI,
o contorno "fantasma" da adaga arremessada e os modelos dos ovos das criaturas.
Também gera o escudo de parry e as cópias experimentais pixel art 3D via pixel_equipment.py.
Juramento do Eco é a única espada adicional de fantasia em camadas, gerada por gen_oathblade.py.
O loader neoforge:separate_transforms troca SOMENTE a perspectiva gui das oito armas pelos
sprites normais detalhados 32×32 de weapon_icons.py; não gera armas elementais. Mão, chão, moldura e projétil conservam o modelo original.
Também chama gen_echo_guide.py para gerar a capa exclusiva do livro inicial.

Os modelos 3D são montados na vertical e inclinados -45° em Z: ficam na mesma diagonal dos sprites
vanilla (cabo embaixo à esquerda, ponta em cima à direita), então reaproveitam as poses de mão da
espada vanilla. Faces com tintindex 1 recebem a cor do elemento (mesmo color handler das outras armas).

Rodar da raiz do projeto:  python tools/gen_weapon_models.py
"""
import json
import math
import os
from itertools import product

from PIL import Image, ImageDraw

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
                # Pixels de brilho e marcas de forja: textura discreta, sem suavização.
                t = max(0, min(1, t + (0.16 if (x + y * 3) % 5 == 0 else -0.08)))
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
    "gui": {"rotation": [12, -25, 0], "translation": [0, 0, 0], "scale": [0.58, 0.58, 0.58]},
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
        el([7.6, 5.8, 7.6], [8.4, 26, 8.4], "steel"),
        el([7.5, 5.8, 7.75], [7.6, 25, 8.25], "edge"),
        el([7.3, 1.2, 7.3], [8.7, 1.6, 8.7], "gold"),
        el([7.3, 3.2, 7.3], [8.7, 3.6, 8.7], "gold"),
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


def dagger():
    """Adaga de aço azulado: lâmina escalonada, nervura, guarda dourada e pomo de cristal."""
    return [
        el([6.8, -2, 6.8], [9.2, 0, 9.2], "gold"),
        el([7.3, -2.3, 6.6], [8.7, -0.5, 9.4], "tint", True),
        el([7.3, 0, 7.3], [8.7, 4.5, 8.7], "dark"),
        *[el([7.1, y, 7.1], [8.9, y + 0.4, 8.9], "leather") for y in (0.5, 1.8, 3.1)],
        el([4.2, 4.5, 7.1], [11.8, 5.7, 8.9], "gold"),
        el([3.7, 4, 7], [5, 5.7, 9], "dark"),
        el([11, 4, 7], [12.3, 5.7, 9], "dark"),
        el([7, 4.3, 6.7], [9, 6.2, 9.3], "tint", True),
        el([6.2, 5.7, 7.5], [9.8, 15, 8.5], "steel"),
        el([5.8, 6.4, 7.7], [6.2, 14.5, 8.3], "edge"),
        el([9.8, 6.4, 7.7], [10.2, 14.5, 8.3], "edge"),
        el([6.8, 15, 7.6], [9.2, 18, 8.4], "steel"),
        el([7.4, 18, 7.7], [8.6, 20, 8.3], "edge"),
        el([7.7, 7, 7.35], [8.3, 16.5, 8.65], "dark"),
    ]


def hammer():
    """Martelo de guerra: cabeça volumétrica, faces de aço chanfradas, aro de ouro e runa incrustada."""
    return [
        el([6.5, -3, 6.5], [9.5, -1, 9.5], "gold"),
        el([7.2, -1, 7.2], [8.8, 13, 8.8], "leather"),
        *[el([7, y, 7], [9, y + 0.5, 9], "wrap") for y in (0, 2, 4, 6, 8, 10)],
        el([6.5, 11.5, 6.5], [9.5, 14, 9.5], "gold"),
        el([1, 14, 4.5], [15, 21, 11.5], "dark"),
        el([0, 14.5, 4], [2, 20.5, 12], "steel"),
        el([14, 14.5, 4], [16, 20.5, 12], "steel"),
        el([-0.5, 15.5, 4.5], [0, 19.5, 11.5], "edge"),
        el([16, 15.5, 4.5], [16.5, 19.5, 11.5], "edge"),
        el([5.8, 13.5, 4.1], [10.2, 21.5, 11.9], "gold"),
        el([6.7, 15, 3.8], [9.3, 20, 12.2], "dark"),
        el([7.4, 15.8, 3.5], [8.6, 19.2, 12.5], "tint", True),
        el([6.8, 17, 3.5], [9.2, 18, 12.5], "tint", True),
        *[el([x, y, 4.2], [x + 0.7, y + 0.7, 11.8], "gold")
          for x in (3, 12.3) for y in (15.2, 19.1)],
        # Molduras salientes de aço: ressaltam a profundidade da cabeça também sem elemento.
        *[el([x, y, 3.6], [x + 1.2, y + 0.7, 12.4], "steel")
          for x in (1.8, 13) for y in (14, 20.3)],
        el([7, 21.5, 6.4], [9, 22.4, 9.6], "gold"),
    ]


def fit_gui(elements):
    """Enquadra todos os cubos em 14 das 16 unidades do slot, inclusive os enfeites elementais."""
    points = []
    for element in elements:
        # Todos os cubos usam a mesma rotação local de -45° em Z.
        for p in product(*zip(element["from"], element["to"])):
            x, y, z = (p[i] - 8 for i in range(3))
            a = math.radians(-45)
            x, y = x * math.cos(a) - y * math.sin(a), x * math.sin(a) + y * math.cos(a)
            # O quaternion XYZ do Minecraft aplica Y antes de X aos vértices (Z da GUI = 0).
            a = math.radians(-25)
            x, z = x * math.cos(a) + z * math.sin(a), -x * math.sin(a) + z * math.cos(a)
            a = math.radians(12)
            y, z = y * math.cos(a) - z * math.sin(a), y * math.sin(a) + z * math.cos(a)
            points.append((x, y))
    bounds = [(min(p[i] for p in points), max(p[i] for p in points)) for i in range(2)]
    scale = 14 / max(b - a for a, b in bounds)
    return {"rotation": [12, -25, 0],
            "translation": [round(-(a + b) * 0.5 * scale, 4) for a, b in bounds] + [0],
            "scale": [round(scale, 4)] * 3}


def model_3d(elements, name=None):
    display = dict(DISPLAY, gui=fit_gui(elements))
    if name == "hammer":
        # O ângulo de espada (-90° em Y) escondia a face da runa. Mostre face, topo e lateral.
        for hand, sign in (("righthand", -1), ("lefthand", 1)):
            display[f"firstperson_{hand}"] = {"rotation": [6, sign * 58, -sign * 25],
                                              "translation": [1.13, 3.2, 1.13], "scale": [0.68] * 3}
            display[f"thirdperson_{hand}"] = {"rotation": [0, sign * 72, -sign * 55],
                                              "translation": [0, 4, 0.5], "scale": [0.85] * 3}
    return {"gui_light": "side" if name == "hammer" else "front",
            "textures": {"parts": "solomod:item/weapon_parts", "particle": "solomod:item/weapon_parts"},
            "display": display, "elements": elements}


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


def write_hammer_icon():
    """Sprite 16×16: cabo diagonal, cabeça de aço e runa; não altera os cubos da arma."""
    base = Image.new("RGBA", (16, 16))
    d = ImageDraw.Draw(base)
    d.line([(2, 14), (11, 5)], fill=(44, 29, 22), width=3)
    d.line([(2, 13), (10, 5)], fill=(153, 105, 59), width=1)
    d.point([(4, 11), (6, 9), (8, 7)], fill=(226, 177, 70))
    d.polygon([(6, 2), (9, 0), (15, 6), (13, 9)], fill=(38, 43, 56))
    d.polygon([(7, 2), (9, 1), (14, 6), (12, 8)], fill=(130, 147, 167))
    d.line([(7, 2), (12, 7)], fill=(228, 238, 246), width=1)
    d.line([(9, 1), (14, 6)], fill=(209, 162, 48), width=1)
    base.save(os.path.join(TEXTURES, "hammer.png"))
    overlay = Image.new("RGBA", (16, 16))
    d = ImageDraw.Draw(overlay)
    d.line([(10, 3), (12, 5)], fill=(255, 255, 255), width=1)
    d.point((11, 5), fill=(222, 234, 255))
    overlay.save(os.path.join(TEXTURES, "hammer_overlay.png"))


def dump(name, data):
    # O ícone é um modelo gerado plano; todas as outras perspectivas preservam o modelo 3D.
    weapon = name.split("_")[0]
    if weapon in ("rapier", "longsword", "dagger", "hammer", "greatsword", "axe", "kunai", "wand"):
        if name == "dagger_thrown":
            gui = {"parent": "minecraft:item/generated", "textures": {"layer0": "solomod:item/dagger_icon_thrown"}}
        elif name == weapon:
            gui = {"parent": "minecraft:item/generated", "textures": {"layer0": f"solomod:item/{weapon}_icon"}}
        else:
            suffix = name[len(weapon):]
            gui = {"parent": "minecraft:item/generated", "textures": {"layer0": f"solomod:item/{weapon}_icon{suffix}"}}
        overrides = data.pop("overrides", [])
        data = {"loader": "neoforge:separate_transforms", "gui_light": "front",
                "textures": {"particle": data["textures"].get("particle", data["textures"].get("layer0"))},
                "base": data, "perspectives": {"gui": gui}}
        if overrides:
            data["overrides"] = overrides
    with open(os.path.join(MODELS, name + ".json"), "w", encoding="utf-8") as fh:
        json.dump(data, fh, indent=2)
        fh.write("\n")


FLAT = ("greatsword", "axe", "kunai", "wand")


def remove_elemental_weapon_assets():
    """Remove apenas saídas de armas descontinuadas; ovos e assets dos mobs são preservados."""
    from pathlib import Path
    for element in ("water", "earth", "fire", "air"):
        for name in ("rapier", "longsword", "dagger", "hammer") + FLAT:
            for folder, filename in ((MODELS, f"{name}_{element}.json"),
                                     (TEXTURES, f"{name}_{element}.png"),
                                     (TEXTURES, f"{name}_icon_{element}.png")):
                target = (Path(folder) / filename).resolve()
                assert target.parent == Path(folder).resolve()
                target.unlink(missing_ok=True)
        target = (Path(TEXTURES) / f"weapon_parts_{element}.png").resolve()
        assert target.parent == Path(TEXTURES).resolve()
        target.unlink(missing_ok=True)


def untinted(elements):
    for e in elements:
        for face in e["faces"].values():
            face.pop("tintindex", None)
    return elements


if __name__ == "__main__":
    import sys
    sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
    ELEMENTS = ("water", "earth", "fire", "air")  # Elementos continuam apenas nos ovos/criaturas.
    from weapon_icons import write_icon_assets
    from pixel_equipment import write_pixel_assets
    from gen_oathblade import write_oathblade
    from gen_echo_guide import write_guide_assets

    remove_elemental_weapon_assets()
    write_parts_texture()
    write_icon_assets(TEXTURES)
    write_pixel_assets(TEXTURES, MODELS)
    write_oathblade(TEXTURES, MODELS)
    write_guide_assets(TEXTURES, MODELS)
    write_hammer_icon()
    write_ghost_dagger()

    # Somente forma normal; a adaga preserva o override de arremesso.
    base_3d = {"rapier": rapier, "longsword": claymore, "dagger": dagger, "hammer": hammer}
    for name, build in base_3d.items():
        model = dict(model_3d(build(), name), overrides=[])
        if name == "dagger":
            model["overrides"].append({"predicate": {"solomod:thrown": 1}, "model": "solomod:item/dagger_thrown"})
        dump(name, model)
    for name in FLAT:
        model = {"parent": "minecraft:item/handheld",
                 "textures": {"layer0": f"solomod:item/{name}", "layer1": f"solomod:item/{name}_overlay"},
                 "overrides": []}
        if name == "dagger":  # adaga arremessada: o fantasma vence qualquer elemento (vem por último)
            model["overrides"].append({"predicate": {"solomod:thrown": 1}, "model": "solomod:item/dagger_thrown"})
        dump(name, model)

    ghost = Image.open(os.path.join(TEXTURES, "weapon_parts.png")).convert("RGBA")
    for y in range(ghost.height):
        for x in range(ghost.width):
            ghost.putpixel((x, y), (170, 235, 255, 65 if ghost.getpixel((x, y))[3] else 0))
    ghost.save(os.path.join(TEXTURES, "weapon_parts_ghost.png"))
    ghost_model = model_3d(untinted(dagger()))
    ghost_model["render_type"] = "minecraft:translucent"
    ghost_model["textures"] = {"parts": "solomod:item/weapon_parts_ghost", "particle": "solomod:item/weapon_parts_ghost"}
    dump("dagger_thrown", ghost_model)
    # Ovos: um por criatura e elemento ("minion_spawn_egg" = sem elemento, "minion_fire_spawn_egg"...).
    for egg in ("minion", "elite", "guardian", "monarch"):
        for suffix in ("",) + tuple("_" + e for e in ELEMENTS):
            dump(f"{egg}{suffix}_spawn_egg", {"parent": "minecraft:item/template_spawn_egg"})
    # O Soberano não tem elemento: um ovo só.
    dump("sovereign_spawn_egg", {"parent": "minecraft:item/template_spawn_egg"})
    print("ok")
