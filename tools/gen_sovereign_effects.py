"""
Gera as texturas originais dos efeitos 3D do Soberano em 16x16 RGBA com pixels grandes, no estilo do
Minecraft (cada texel é 1/16 de uma face de cubo). As malhas são CUBOS, em client/render/:

  * sovereign_tornado.png -> nuvem de pixels (cinza-azulado e branco, com buracos transparentes em traços
                             horizontais), SovereignTornadoRenderer
  * water_orb.png         -> casca de água (azuis com espuma branca), WaterOrbRenderer
  * water_orb_core.png    -> núcleo (ciano e branco)

O "ruído" é uma função de hash de (x, y): a mesma saída sempre, sem aleatoriedade de execução. Rodar da raiz do
projeto:  python tools/gen_sovereign_effects.py
"""
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/solomod/textures/entity")
SIZE = 16


def noise(x, y, k):
    """0..1 determinístico."""
    return ((x * 73856093) ^ (y * 19349663) ^ (k * 83492791)) % 1000 / 1000.0


def make(pixel):
    img = Image.new("RGBA", (SIZE, SIZE))
    for y in range(SIZE):
        for x in range(SIZE):
            img.putpixel((x, y), pixel(x, y))
    return img


def tornado(x, y):
    # Traços horizontais de 3 pixels, como o vento em pixel art: ruído por faixa, não por pixel.
    r = noise(x // 3, y, 2)
    if r < 0.22:
        return (0, 0, 0, 0)
    if r < 0.5:
        return (170, 186, 204, 205)
    if r < 0.85:
        return (214, 226, 236, 220)
    return (246, 251, 255, 240)


def water(x, y):
    r = noise(x, y, 1)
    if r < 0.1:
        return (232, 249, 255, 255)  # espuma
    if r < 0.4:
        return (74, 146, 238, 215)
    if r < 0.78:
        return (46, 112, 216, 215)
    return (30, 86, 190, 215)


def core(x, y):
    r = noise(x, y, 3)
    if r < 0.3:
        return (255, 255, 255, 255)
    if r < 0.7:
        return (190, 245, 255, 255)
    return (140, 228, 255, 255)


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    make(tornado).save(os.path.join(OUT, "sovereign_tornado.png"))
    make(water).save(os.path.join(OUT, "water_orb.png"))
    make(core).save(os.path.join(OUT, "water_orb_core.png"))
    print("ok")
