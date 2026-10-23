"""Rostos dos cinco Monarcas e do Soberano em pixel art RGBA 16x16.

As matrizes preservam os traços dos modelos: pedra/coroa, carvão/chamas,
kraken/coral, casca/copa e elmo/asas. Gera também os modelos de item.
Execute da raiz: python tools/gen_boss_icons.py
"""
import json
from pathlib import Path

from gen_sovereign_icons import build_head, build_unknown, paint

ASSETS = Path(__file__).resolve().parents[1] / "src/main/resources/assets/solomod"

# Cada linha é uma fileira de pixels; nenhum filtro ou suavização é aplicado.
FACES = {
    "none": (
        {".": None, "x": (28, 25, 32), "s": (78, 73, 80), "r": (126, 120, 123),
         "l": (170, 163, 155), "g": (195, 145, 44), "h": (255, 222, 115),
         "p": (210, 120, 255), "t": (218, 207, 185)},
        ["................", "...h...hh...h...", "...gh..gh..hg...", "...gggggggggg...",
         "..xghghghghghx..", "..xggggggggggx..", "..xllrrrrrrlsx..", "..xlrrsrrsrrsx..",
         "..xrppsssspprx..", "..xrrlrrlrrrsx..", "..xrslsslsrsrx..", "..xrtsttststrx..",
         "..xrssssssssrx..", "...xrrllllrrx...", "....xxxxxxxx....", "................"]),
    "fire": (
        {".": None, "x": (24, 17, 24), "c": (52, 40, 43), "s": (87, 61, 54),
         "b": (134, 70, 29), "g": (227, 151, 44), "h": (255, 213, 97),
         "r": (220, 48, 18), "f": (255, 111, 20), "y": (255, 235, 142)},
        ["................", ".......r........", "...r...fr...r...", "..rf..ryfr..fr..",
         "..fy..fyyf..yf..", "..gh..ghhg..hg..", ".bghggghhggghgb.", ".bggggggggggggb.",
         "..xxxxxxxxxxxx..", "..xsscccccccsx..", "..xsyfxccxfysx..", "..xccrrccrrccx..",
         "..xcccxssxcccx..", "..xsxrrrrrrxsx..", "...xsfyffyfsx...", "....xxxxxxxx...."]),
    "water": (
        {".": None, "x": (8, 17, 35), "d": (19, 48, 73), "b": (34, 90, 116),
         "l": (59, 139, 153), "c": (206, 85, 101), "h": (255, 157, 150),
         "e": (128, 255, 220), "s": (30, 72, 94), "t": (151, 205, 199)},
        ["................", "..h....hh....h..", "..ch...hc...hc..", "..ccchccccccch..",
         "..chcchcchcchc..", "..xxxxxxxxxxxx..", ".xblbbbbbbbbldx.", ".xbleexxxeebbdx.",
         "..xdbbddbbdddx..", "..xblbbllbbldx..", "..xxlbxttxblxx..", ".xbxlbxddxblxbx.",
         ".xl.xlxddxlx.lx.", "..l.xl.dd.lx.l..", "....ll....ll....", "................"]),
    "earth": (
        {".": None, "x": (29, 22, 14), "b": (93, 58, 32), "l": (143, 97, 49),
         "s": (60, 40, 24), "g": (55, 106, 43), "h": (105, 160, 59),
         "e": (255, 190, 60), "t": (196, 148, 74)},
        [".....gggggg.....", "...gghhhhhhgg...", "..ghhhghhhghhg..", ".ghhghhhghhhghg.",
         ".gggggggggggggg.", "...xbllbllbx....", ".b.xbllbllbx.b..", ".lbxbssbbssxbl..",
         "..lbleebbeelbl..", "..xblbllllblbx..", "..xbslbllblsbx..", "..xblsxssxslbx..",
         "...xblttttblx...", "...xbssssssbx...", "....xbbllbbx....", ".....xxxxxx....."]),
    "air": (
        {".": None, "x": (37, 48, 68), "s": (109, 133, 159), "l": (191, 210, 225),
         "w": (241, 249, 255), "e": (120, 240, 255), "g": (224, 190, 100)},
        [".....eeeeee.....", "...ee......ee...", "..e..........e..", ".w..gggggggg..w.",
         ".ww.gllggllg.ww.", ".wlwxllllllxwlw.", "..wlxwllllwxll..", "..wlxleeeelxlw..",
         "...lxexxxxelx...", "...xllwllwllx...", "...xslwllwlsx...", "....xllwwllx....",
         "....xslwwlsx....", ".....xsllsx.....", "......xxxx......", "................"]),
}


def main():
    textures = ASSETS / "textures/item"
    models = ASSETS / "models/item"
    textures.mkdir(parents=True, exist_ok=True)
    models.mkdir(parents=True, exist_ok=True)
    icons = {f"monarch_{element}_head": paint(rows, palette) for element, (palette, rows) in FACES.items()}
    icons.update(sovereign_head=build_head(), unknown_boss=build_unknown())
    for name, icon in icons.items():
        icon.save(textures / f"{name}.png")
        model = {"parent": "minecraft:item/generated", "textures": {"layer0": f"solomod:item/{name}"}}
        (models / f"{name}.json").write_text(json.dumps(model, indent=2) + "\n", encoding="utf-8")
    print(f"Gerados {len(icons)} ícones e modelos de conquista.")


if __name__ == "__main__":
    main()
