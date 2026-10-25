"""
Gera os JSON das 27 conquistas do Solo Mod em data/solomod/advancement/<aba>/, em três abas:

  * echo_song         -> A Canção do Eco (história principal, 14)
  * serene_echoes     -> Ecos Serenos (Fauna Pacífica, 6)
  * seal_accomplices  -> Os Cúmplices do Selo (Santuários, 7)

Só a raiz da aba 1 usa um gatilho vanilla (minecraft:tick). Os demais critérios usam minecraft:impossible e são
concedidos por advancement/ModAdvancements.java; os NOMES dos critérios aqui precisam bater com os de lá
("done", "monarch_<elemento>", a linhagem, o id da entidade). Textos em lang/*.json (advancements.solomod.<id>.*).

Rodar da raiz do projeto (apaga e recria a pasta de conquistas):
  python tools/gen_advancements.py
"""
import json
import os
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/data/solomod/advancement")

ELEMENTS = ["none", "fire", "water", "earth", "air"]
ECHOES = ["spectral_scout", "echo_runic_bat", "relic_automaton", "igneous_salamander", "golden_silica_scarab",
          "volcanic_ash_colt", "liquid_mana_axolotl", "lotus_turtle_crab", "abyssal_trench_remora",
          "sylvan_mini_tank", "runic_boulder_golem", "amber_root_gazelle", "cloud_manta_ray",
          "storm_sentinel_owl", "dimensional_rift_moth"]
SANCTUARY_BOSSES = ["cinzagrim", "archivist", "weeper", "root_mother", "herald", "echo_hunter"]

# (aba, id, pai, ícone, moldura, oculta, critérios, xp, fundo-da-aba)
# Critério None = um único "done". Pai None = raiz da aba.
ADVANCEMENTS = [
    # ---- Aba 1: A Canção do Eco
    ("echo_song", "born_in_silence", None, "minecraft:echo_shard", "task", False, "tick", 0,
     "minecraft:textures/block/chiseled_deepslate.png"),
    ("echo_song", "taste_of_violence", "born_in_silence", "minecraft:redstone", "task", False, None, 0, None),
    ("echo_song", "muscle_memory", "born_in_silence", "solomod:hammer", "task", False, None, 0, None),
    ("echo_song", "broken_guard", "taste_of_violence", "minecraft:shield", "task", False, None, 0, None),
    ("echo_song", "red_whisper", "broken_guard", "minecraft:redstone_torch", "goal", False, None, 50, None),
    ("echo_song", "first_strike", "red_whisper", "solomod:monarch_none_head", "challenge", False, None, 150, None),
    ("echo_song", "returned_fever", "red_whisper", "solomod:monarch_fire_head", "challenge", False, None, 150, None),
    ("echo_song", "empty_tide", "red_whisper", "solomod:monarch_water_head", "challenge", False, None, 150, None),
    ("echo_song", "severed_root", "red_whisper", "solomod:monarch_earth_head", "challenge", False, None, 150, None),
    ("echo_song", "usurper_fall", "red_whisper", "solomod:monarch_air_head", "challenge", False, None, 150, None),
    ("echo_song", "shattered_seal", "red_whisper", "minecraft:crying_obsidian", "goal", False,
     ["monarch_" + e for e in ELEMENTS], 300, None),
    ("echo_song", "mirror_face", "shattered_seal", "solomod:unknown_boss", "goal", True, None, 0, None),
    ("echo_song", "absolute_dissonance", "mirror_face", "solomod:sovereign_head", "challenge", True, None, 500, None),
    ("echo_song", "error_sixth_voice", "absolute_dissonance", "minecraft:barrier", "challenge", True, None, 0, None),
    # ---- Aba 2: Ecos Serenos
    ("serene_echoes", "soul_tuning_fork", None, "minecraft:amethyst_shard", "task", False, None, 0,
     "minecraft:textures/block/moss_block.png"),
    ("serene_echoes", "necessary_silence", "soul_tuning_fork", "minecraft:glass_bottle", "task", False, None, 0, None),
    ("serene_echoes", "what_is_dead_cannot_die", "soul_tuning_fork", "minecraft:phantom_membrane", "task", False, None, 0, None),
    ("serene_echoes", "five_lineages_harmony", "soul_tuning_fork", "minecraft:beacon", "goal", False, ELEMENTS, 100, None),
    ("serene_echoes", "frame_jump", "soul_tuning_fork", "minecraft:ender_pearl", "challenge", False, None, 100, None),
    ("serene_echoes", "the_first_choir", "five_lineages_harmony", "minecraft:note_block", "challenge", False, ECHOES, 300, None),
    # ---- Aba 3: Os Cúmplices do Selo (a raiz é o desafio 0/6, que aparece no primeiro guardião derrotado)
    ("seal_accomplices", "lie_of_five_thrones", None, "minecraft:recovery_compass", "challenge", False,
     SANCTUARY_BOSSES, 400, "minecraft:textures/block/polished_blackstone_bricks.png"),
    ("seal_accomplices", "chains_for_nobody", "lie_of_five_thrones", "minecraft:chain", "task", False, None, 100, None),
    ("seal_accomplices", "weight_of_a_shadow", "lie_of_five_thrones", "minecraft:book", "task", False, None, 100, None),
    ("seal_accomplices", "song_of_guilt", "lie_of_five_thrones", "minecraft:prismarine_shard", "task", False, None, 100, None),
    ("seal_accomplices", "stone_heart", "lie_of_five_thrones", "minecraft:moss_block", "task", False, None, 100, None),
    ("seal_accomplices", "swallowed_scream", "lie_of_five_thrones", "minecraft:bell", "task", False, None, 100, None),
    ("seal_accomplices", "faithful_to_the_end", "lie_of_five_thrones", "minecraft:skeleton_skull", "task", False, None, 100, None),
]


def build(tab, adv_id, parent, icon, frame, hidden, criteria, xp, background):
    data = {}
    if parent:
        data["parent"] = f"solomod:{tab}/{parent}"
    display = {
        "icon": {"id": icon},
        "title": {"translate": f"advancements.solomod.{adv_id}.title"},
        "description": {"translate": f"advancements.solomod.{adv_id}.description"},
    }
    if background:
        display["background"] = background
    # A raiz ganha no primeiro tick: sem aviso nem chat (só faz a aba aparecer).
    announce = criteria != "tick"
    display.update({"frame": frame, "show_toast": announce, "announce_to_chat": announce, "hidden": hidden})
    data["display"] = display
    if criteria == "tick":
        data["criteria"] = {"auto": {"trigger": "minecraft:tick"}}
        data["requirements"] = [["auto"]]
    else:
        names = criteria or ["done"]
        data["criteria"] = {n: {"trigger": "minecraft:impossible"} for n in names}
        # Todos os critérios são obrigatórios: a tela mostra o progresso (ex.: 2/5).
        data["requirements"] = [[n] for n in names]
    if xp:
        data["rewards"] = {"experience": xp}
    return data


def main():
    if os.path.isdir(OUT):
        shutil.rmtree(OUT)
    for entry in ADVANCEMENTS:
        tab, adv_id = entry[0], entry[1]
        folder = os.path.join(OUT, tab)
        os.makedirs(folder, exist_ok=True)
        with open(os.path.join(folder, adv_id + ".json"), "w", encoding="utf-8", newline="") as f:
            f.write(json.dumps(build(*entry), indent=2, ensure_ascii=False) + "\n")
    print(len(ADVANCEMENTS), "conquistas geradas em", OUT)


if __name__ == "__main__":
    main()
