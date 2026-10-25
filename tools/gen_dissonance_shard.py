"""
Gera o ícone 16x16 do Fragmento de Dissonância (textures/item/dissonance_shard.png) e o modelo de item.

Um caco de cristal escuro e fino, com o núcleo branco e os dois canais da Dissonância desalinhados: ciano
(#00E5FF) para a esquerda e vermelho (#FF1744) para a direita, como na morte do Soberano. Rodar da raiz:
  python tools/gen_dissonance_shard.py
"""
import json
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEXTURE = os.path.join(ROOT, "src/main/resources/assets/solomod/textures/item/dissonance_shard.png")
MODEL = os.path.join(ROOT, "src/main/resources/assets/solomod/models/item/dissonance_shard.json")

OUTLINE = (20, 8, 28, 255)
BODY = (46, 22, 64, 255)
CORE = (240, 240, 255, 255)
CYAN = (0, 229, 255, 255)
RED = (255, 23, 68, 255)

# Silhueta do caco (diagonal, de baixo-esquerda a cima-direita): para cada linha, a coluna inicial e final.
ROWS = {1: (11, 12), 2: (10, 13), 3: (9, 13), 4: (8, 12), 5: (7, 11), 6: (6, 11), 7: (5, 10), 8: (5, 9),
        9: (4, 8), 10: (3, 8), 11: (3, 7), 12: (2, 6), 13: (2, 5), 14: (3, 4)}


def main():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    inside = {(x, y) for y, (a, b) in ROWS.items() for x in range(a, b + 1)}
    for x, y in inside:
        img.putpixel((x, y), BODY)
    # Contorno: cada pixel de dentro que encosta no vazio.
    for x, y in inside:
        if any((x + dx, y + dy) not in inside for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
            img.putpixel((x, y), OUTLINE)
    # Núcleo: a diagonal do meio do caco; os canais ficam um pixel para cada lado, com falhas.
    for y in range(2, 14):
        a, b = ROWS[y]
        mid = (a + b) // 2
        img.putpixel((mid, y), CORE)
        if (mid - 1, y) in inside and y % 3 != 0:
            img.putpixel((mid - 1, y), CYAN)
        if (mid + 1, y) in inside and y % 4 != 1:
            img.putpixel((mid + 1, y), RED)
    # Estilhaços soltos fora do caco (o glitch "vazando").
    for x, y, c in ((14, 4, RED), (1, 10, CYAN), (13, 8, CYAN), (6, 15, RED)):
        img.putpixel((x, y), c)
    os.makedirs(os.path.dirname(TEXTURE), exist_ok=True)
    img.save(TEXTURE)
    with open(MODEL, "w", encoding="utf-8", newline="") as f:
        f.write(json.dumps({"parent": "minecraft:item/generated",
                            "textures": {"layer0": "solomod:item/dissonance_shard"}}, indent=2) + "\n")
    print("fragmento gerado em", TEXTURE)


if __name__ == "__main__":
    main()
