"""Equipamentos em pixel art extrudada: protótipos das quatro armas e escudo de parry.

Importado por gen_weapon_models.py; também pode rodar sozinho. As quatro armas são geradas
por majestica_weapons.py, com atlas 32×64, fantasia em camadas e espessuras por peça.
Este módulo conserva o escudo e o wrapper separate_transforms (GUI sprite, base volumétrica).
--preview gera uma prancha frontal/oblíqua dos mesmos cubos em build/previews/.
"""
import json
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/solomod"
WEAPONS = ("rapier", "longsword", "dagger", "hammer")
DISPLAY = {
    "thirdperson_righthand": {"rotation": [0, -90, 55], "translation": [0, 4, 0.5], "scale": [0.85] * 3},
    "thirdperson_lefthand": {"rotation": [0, 90, -55], "translation": [0, 4, 0.5], "scale": [0.85] * 3},
    "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [1.13, 3.2, 1.13], "scale": [0.8] * 3},
    "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [1.13, 3.2, 1.13], "scale": [0.8] * 3},
    "ground": {"translation": [0, 2, 0], "scale": [0.5] * 3},
    "fixed": {"rotation": [0, 180, 0], "scale": [0.85] * 3},
}

def shield_sprite():
    image = Image.new("RGBA", (32, 32))
    draw = ImageDraw.Draw(image)
    draw.polygon([(6, 3), (25, 3), (28, 6), (27, 20), (23, 26), (16, 30),
                  (8, 26), (4, 20), (3, 6)], fill="#182134")
    draw.polygon([(7, 4), (24, 4), (27, 7), (26, 20), (22, 25), (16, 29),
                  (9, 25), (5, 20), (4, 7)], fill="#e1aa38")
    draw.line([(7, 4), (24, 4), (26, 7)], fill="#ffedaa", width=1)
    draw.polygon([(8, 7), (23, 7), (24, 9), (23, 19), (20, 23), (16, 26),
                  (11, 23), (8, 19), (7, 9)], fill="#526783")
    draw.polygon([(8, 8), (15, 8), (15, 24), (11, 21), (9, 18)], fill="#8ba8c8")
    draw.line([(9, 9), (9, 17), (12, 21)], fill="#e3f2fa", width=1)
    draw.line([(16, 8), (16, 25)], fill="#182134", width=2)
    draw.polygon([(16, 10), (21, 15), (16, 21), (11, 15)], fill="#e1aa38")
    draw.polygon([(16, 12), (19, 15), (16, 18), (13, 15)], fill="#46dfd2")
    draw.line([(16, 12), (14, 15)], fill="#d6fff4")
    for x, y in ((8, 6), (23, 6), (6, 18), (25, 18), (16, 27)):
        draw.point((x, y), fill="#ffedaa")
    return image


def depth(pixel, shield=False):
    if not shield:
        # Aço, fio e contorno formam uma superfície contínua; só a guarda ganha um relevo discreto.
        return 1.25 if pixel[:3] == (174, 137, 80) else 1.0
    r, g, b, _ = pixel
    if max(r, g, b) < 80:
        return 0.6
    if g > r * 1.35 or r > b * 1.4:
        return 2.0 if shield else 1.25
    return 1.4 if shield else 0.85


def extrude(sprite, texture, shield=False):
    """Mesma malha na prévia e nos assets; UV 1 pixel, sem suavização nem faces internas ocultas."""
    elements = []
    for y in range(32):
        for x in range(32):
            pixel = sprite.getpixel((x, y))
            if not pixel[3]:
                continue
            thickness = depth(pixel, shield)
            face = {"uv": [x / 2, y / 2, (x + 1) / 2, (y + 1) / 2], "texture": "#pixel"}
            faces = {side: dict(face) for side in ("north", "south")}
            for side, dx, dy in (("west", -1, 0), ("east", 1, 0), ("up", 0, -1), ("down", 0, 1)):
                nx, ny = x + dx, y + dy
                if (not 0 <= nx < 32 or not 0 <= ny < 32 or not sprite.getpixel((nx, ny))[3]
                        or depth(sprite.getpixel((nx, ny)), shield) < thickness):
                    faces[side] = dict(face)
            elements.append({"from": [x / 2, (31 - y) / 2, 8 - thickness / 2],
                             "to": [(x + 1) / 2, (32 - y) / 2, 8 + thickness / 2], "faces": faces})
    # Sem parent item/generated/handheld: o marcador builtin/generated descartaria os cubos.
    return {"ambientocclusion": False, "gui_light": "side", "display": DISPLAY,
            "textures": {"pixel": f"solomod:item/{texture}", "particle": f"solomod:item/{texture}"},
            "elements": elements}


def write_model(models, name, base, texture, overrides=None):
    wrapper = {"loader": "neoforge:separate_transforms", "gui_light": "front",
               "textures": {"particle": f"solomod:item/{texture}"}, "base": base,
               "perspectives": {"gui": {"parent": "minecraft:item/generated",
                                        "textures": {"layer0": f"solomod:item/{texture}"}}}}
    if overrides:
        wrapper["overrides"] = overrides
    (models / f"{name}.json").write_text(json.dumps(wrapper, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def write_pixel_assets(textures=None, models=None):
    textures = Path(textures) if textures else ASSETS / "textures/item"
    models = Path(models) if models else ASSETS / "models/item"
    from majestica_weapons import write_majestica_assets
    write_majestica_assets(textures, models)

    sprite = shield_sprite()
    sprite.save(textures / "parry_shield.png")
    base = extrude(sprite, "parry_shield", True)
    # Escudo frontal em primeira pessoa; orientação espelhada para ambas as mãos.
    base["display"] = {
        "thirdperson_righthand": {"rotation": [0, 90, 0], "translation": [0, 2, 0], "scale": [0.8] * 3},
        "thirdperson_lefthand": {"rotation": [0, -90, 0], "translation": [0, 2, 0], "scale": [0.8] * 3},
        "firstperson_righthand": {"rotation": [0, -25, 0], "translation": [-1, 0, 0], "scale": [0.9] * 3},
        "firstperson_lefthand": {"rotation": [0, 25, 0], "translation": [-1, 0, 0], "scale": [0.9] * 3},
        "ground": {"translation": [0, 3, 0], "scale": [0.5] * 3},
        "fixed": {"rotation": [0, 180, 0], "scale": [0.85] * 3}}
    write_model(models, "parry_shield", base, "parry_shield",
                [{"predicate": {"minecraft:blocking": 1}, "model": "solomod:item/parry_shield_blocking"}])
    # Mesmo volume, aproxima a guarda ao centro da câmera (BLOCK vanilla continua animando o braço).
    blocking = json.loads(json.dumps(base))
    for side in ("righthand", "lefthand"):
        blocking["display"][f"firstperson_{side}"]["translation"] = [-2, 1, 1]
    write_model(models, "parry_shield_blocking", blocking, "parry_shield")


def preview():
    """Atalho mantido para a prancha atualizada das quatro armas em camadas."""
    from majestica_weapons import preview as family_preview
    family_preview()
    # Preserva o caminho documentado para quem já usa a prévia anterior.
    source = ROOT / "build/previews/majestica_weapons.png"
    (ROOT / "build/previews/pixel_equipment.png").write_bytes(source.read_bytes())


if __name__ == "__main__":
    import sys
    write_pixel_assets()
    if "--preview" in sys.argv:
        preview()
