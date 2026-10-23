"""
Variantes elementais das 4 criaturas e o modelo do Soberano (usados por gen_mob_art.py).

Cada variante é um modelo COMPLETAMENTE diferente (silhueta, peças e materiais próprios), mas reaproveita
os nomes das partes que a animação do tier conhece (body, head, arm_r, leg_l, tail, sword, cape, orb_1...).
Partes que a variante não tem simplesmente não existem — a animação ignora.

Prefixos de partes decorativas (animadas automaticamente por TierMobModel):
  spin_  gira em Y        roll_  gira em Z (de frente)   orbit_ gira em Y devagar (órbita)
  flick_ tremula (chama)  sway_  balança              bob_   sobe e desce
  pulse_ pulsa de tamanho flap_  bate como asa (lado pelo sufixo _l/_r)
"""
from gen_mob_art import Model, C


def ring(r, mat, y=0.0, thick=1):
    """Quatro barras formando um quadrado horizontal de raio r (anel girando)."""
    s = 2 * r
    return [C(-r, y, -r, s, thick, 1, mat), C(-r, y, r - 1, s, thick, 1, mat),
            C(-r, y, -r + 1, 1, thick, s - 2, mat), C(r - 1, y, -r + 1, 1, thick, s - 2, mat)]


def vring(r, mat):
    """Quatro barras formando um quadrado vertical (plano XY) de raio r."""
    s = 2 * r
    return [C(-r, -r, -0.5, s, 1, 1, mat), C(-r, r - 1, -0.5, s, 1, 1, mat),
            C(-r, -r + 1, -0.5, 1, s - 2, 1, mat), C(r - 1, -r + 1, -0.5, 1, s - 2, 1, mat)]


# ============================================================================================ LACAIO

def minion_fire():
    """Diabrete Ígneo: crânio de carvão com uma coroa de chamas, jatos de fogo nas costas, cauda de brasa."""
    m = Model("minion_fire", 64)
    for side, x in (("r", -1.7), ("l", 1.7)):
        m.part(f"leg_{side}", "root", (x, 18, 0.5), C(-1, 0, -1, 2, 6, 2, "char"), C(-1.5, 5, -2.5, 3, 1, 3, "ember"))
    m.part("body", "root", (0, 18.5, 0), C(-3, -7, -2, 6, 7, 4, "char"), C(-2, -6, -2.6, 4, 4, 1, "magma"), rot=(0.35, 0, 0))
    m.part("head", "body", (0, -7, -1), C(-4, -7, -4.5, 8, 7, 7, "char", "skull_fire"))
    m.part("jaw", "head", (0, -0.5, -1), C(-3.5, 0, -4, 7, 2, 5, "char", "teeth"))
    m.part("flick_crest_1", "head", (0, -7, 0), C(-3, -4, -3, 6, 4, 6, "flame"))
    m.part("flick_crest_2", "flick_crest_1", (0, -4, 0.5), C(-2, -4, -2, 4, 4, 4, "flame"))
    m.part("flick_crest_3", "flick_crest_2", (0, -4, 0.5), C(-1, -3, -1, 2, 3, 2, "flame"))
    m.part("ear_l", "head", (4, -5, -1), C(0, -1, -0.5, 3, 2, 1, "ember"), rot=(0, 0, -0.5))
    m.part("ear_r", "head", (-4, -5, -1), C(-3, -1, -0.5, 3, 2, 1, "ember"), rot=(0, 0, 0.5))
    m.part("arm_r", "body", (-3.5, -6, 0), C(-1.5, -1, -1, 2, 9, 2, "char"))
    m.part("claw_r", "arm_r", (-0.5, 8, 0), C(-1.5, 0, -1.5, 3, 3, 3, "ember"))
    m.part("arm_l", "body", (3.5, -6, 0), C(-0.5, -1, -1, 2, 9, 2, "char"))
    m.part("claw_l", "arm_l", (0.5, 8, 0), C(-1.5, 0, -1.5, 3, 3, 3, "ember"))
    m.part("flick_jet_l", "body", (2, -6, 2), C(-1, -5, -1, 2, 5, 2, "flame"), rot=(-0.6, 0, 0.3))
    m.part("flick_jet_r", "body", (-2, -6, 2), C(-1, -5, -1, 2, 5, 2, "flame"), rot=(-0.6, 0, -0.3))
    m.part("tail", "body", (0, -0.5, 1.5), C(-0.5, -0.5, 0, 1, 1, 6, "ember"))
    m.part("tail_tip", "tail", (0, 0, 6), C(-1.5, -1.5, 0, 3, 3, 3, "flame"))
    return m


def minion_water():
    """Diabrete Abissal: peixe-goblin de cabeça bulbosa, isca luminosa, nadadeiras e cauda de peixe."""
    m = Model("minion_water", 64)
    for side, x in (("r", -2), ("l", 2)):
        m.part(f"leg_{side}", "root", (x, 18, 0.5), C(-1.5, 0, -1.5, 3, 6, 3, "scales"), C(-2, 5, -3.5, 4, 1, 4, "fin"))
    m.part("body", "root", (0, 18.5, 0), C(-3.5, -6, -2.5, 7, 6, 5, "scales"), C(-2.5, -5, -3, 5, 4, 1, "foam"),
           rot=(0.3, 0, 0))
    m.part("sway_dorsal", "body", (0, -6, 2), C(0, -5, -1, 0, 5, 6, "fin"))
    m.part("head", "body", (0, -6, -1), C(-5, -8, -6, 10, 8, 9, "deep", "fish_face"))
    m.part("jaw", "head", (0, 0, -2), C(-4.5, 0, -4.5, 9, 2, 6, "deep", "teeth"))
    m.part("ear_l", "head", (5, -5, 0), C(0, -3, 0, 6, 6, 0, "fin"), rot=(0, 0.4, 0))
    m.part("ear_r", "head", (-5, -5, 0), C(-6, -3, 0, 6, 6, 0, "fin"), rot=(0, -0.4, 0))
    m.part("sway_lure", "head", (0, -8, -3), C(-0.5, -6, -0.5, 1, 6, 1, "deep"), rot=(-0.6, 0, 0))
    m.part("pulse_bulb", "sway_lure", (0, -6, 0), C(-1.5, -1.5, -1.5, 3, 3, 3, "pearl"))
    m.part("arm_r", "body", (-4, -5, 0), C(-1, -1, -1, 2, 8, 2, "scales"))
    m.part("claw_r", "arm_r", (0, 7, 0), C(-2, 0, -1.5, 3, 3, 1, "fin"))
    m.part("arm_l", "body", (4, -5, 0), C(-1, -1, -1, 2, 8, 2, "scales"))
    m.part("claw_l", "arm_l", (0, 7, 0), C(-1, 0, -1.5, 3, 3, 1, "fin"))
    m.part("tail", "body", (0, -1, 2), C(-1, -1, 0, 2, 2, 5, "scales"))
    m.part("tail_tip", "tail", (0, 0, 5), C(0, -3, 0, 0, 6, 5, "fin"))
    return m


def minion_earth():
    """Diabrete de Pedra: pedregulho atarracado com musgo, cristais nas costas, punhos de rocha e seixos em órbita."""
    m = Model("minion_earth", 64)
    for side, x in (("r", -2.2), ("l", 2.2)):
        m.part(f"leg_{side}", "root", (x, 20, 0), C(-1.5, 0, -1.5, 3, 4, 3, "stone"))
    m.part("body", "root", (0, 20, 0), C(-4.5, -8, -3.5, 9, 8, 7, "stone"), C(-4, -9, -3, 8, 1, 6, "moss"),
           rot=(0.15, 0, 0))
    m.part("pulse_crystal_1", "body", (-2, -8, 1), C(-1, -5, -1, 2, 5, 2, "crystal"), rot=(-0.3, 0, -0.3))
    m.part("pulse_crystal_2", "body", (1.5, -8, 2), C(-1, -7, -1, 2, 7, 2, "crystal"), rot=(-0.2, 0, 0.2))
    m.part("pulse_crystal_3", "body", (0, -6, 3), C(-1, -4, -1, 2, 4, 2, "crystal"), rot=(0.4, 0, 0))
    m.part("head", "body", (0, -6, -3), C(-3, -4, -3, 6, 5, 5, "stone", "stone_face"))
    m.part("jaw", "head", (0, 1, -1), C(-2.5, 0, -2.5, 5, 1, 4, "stone"))
    m.part("arm_r", "body", (-5, -6, 0), C(-2, -1, -2, 3, 8, 3, "stone"))
    m.part("claw_r", "arm_r", (-0.5, 7, 0), C(-2.5, 0, -2.5, 4, 4, 4, "stone"), C(-2.5, -0.5, -2.5, 4, 1, 4, "moss"))
    m.part("arm_l", "body", (5, -6, 0), C(-1, -1, -2, 3, 8, 3, "stone"))
    m.part("claw_l", "arm_l", (0.5, 7, 0), C(-1.5, 0, -2.5, 4, 4, 4, "stone"), C(-1.5, -0.5, -2.5, 4, 1, 4, "moss"))
    m.part("orbit_pebbles", "root", (0, 12, 0),
           C(7, -1, -1, 2, 2, 2, "stone"), C(-9, 0, 0, 2, 2, 2, "crystal"), C(0, -2, 7, 2, 2, 2, "moss"))
    return m


def minion_air():
    """Sílfide Travessa: espírito-pássaro sem pernas, bico de prata, crista de penas, asas enormes e anel de vento."""
    m = Model("minion_air", 64)
    m.part("body", "root", (0, 12, 0), C(-3, -6, -2, 6, 6, 4, "cloud"), C(-2, -5, -2.6, 4, 4, 1, "feather"),
           rot=(0.2, 0, 0))
    m.part("tail", "body", (0, 0, 0), C(-2, 0, -1.5, 4, 4, 3, "cloud"))
    m.part("tail_tip", "tail", (0, 4, 0), C(-1, 0, -1, 2, 5, 2, "wind"))
    m.part("head", "body", (0, -6, -1), C(-3.5, -7, -3.5, 7, 7, 7, "cloud", "bird_face"))
    m.part("jaw", "head", (0, -2, -3.5), C(-1, -1, -3, 2, 2, 3, "silver"))
    m.part("sway_crest", "head", (0, -7, 0), C(-0.5, -5, -1, 1, 5, 4, "feather"), rot=(-0.4, 0, 0))
    m.part("ear_l", "head", (3.5, -5, 0), C(0, -1, -1, 4, 2, 2, "feather"), rot=(0, 0, -0.3))
    m.part("ear_r", "head", (-3.5, -5, 0), C(-4, -1, -1, 4, 2, 2, "feather"), rot=(0, 0, 0.3))
    m.part("wing_l", "body", (2, -5, 1.5), C(0, -8, 0, 12, 10, 0, "feather"), C(0, -8.5, -0.5, 12, 1, 1, "silver"))
    m.part("wing_r", "body", (-2, -5, 1.5), C(-12, -8, 0, 12, 10, 0, "feather"), C(-12, -8.5, -0.5, 12, 1, 1, "silver"))
    m.part("arm_r", "body", (-3.5, -5, 0), C(-1, -1, -1, 2, 7, 2, "cloud"))
    m.part("claw_r", "arm_r", (0, 6, 0), C(-1.5, 0, -1.5, 3, 2, 3, "silver"))
    m.part("arm_l", "body", (3.5, -5, 0), C(-1, -1, -1, 2, 7, 2, "cloud"))
    m.part("claw_l", "arm_l", (0, 6, 0), C(-1.5, 0, -1.5, 3, 2, 3, "silver"))
    m.part("spin_ring", "root", (0, 14, 0), *ring(6, "wind"))
    return m


# ============================================================================================ ELITE

def elite_fire():
    """Executor Infernal: elmo de chifres com coroa de fogo, cutelo em brasa, mangual em chamas e capa de fogo."""
    m = Model("elite_fire", 128)
    for side, x in (("r", -2.5), ("l", 2.5)):
        m.part(f"leg_{side}", "root", (x, 9, 0), C(-2, 0, -2, 4, 15, 4, "char"), C(-2.5, 4, -2.7, 5, 3, 5, "brass"),
               C(-2.5, 13, -3.5, 5, 2, 6, "char"))
    m.part("body", "root", (0, 9, 0), C(-4, -4, -2.5, 8, 5, 5, "char"), C(-5, -1, -3, 10, 6, 6, "magma"),
           C(-6, -15, -3.5, 12, 11, 7, "char", "chest_rune"), C(-2, -12, -4, 4, 4, 1, "flame"))
    m.part("head", "body", (0, -15, 0), C(-4, -9, -4, 8, 9, 8, "char", "visor_fire"))
    m.part("horn_l", "head", (4, -7, 0), C(0, -1.5, -1.5, 4, 3, 3, "brass"), C(3, -6, -1, 2, 5, 2, "brass"))
    m.part("horn_r", "head", (-4, -7, 0), C(-4, -1.5, -1.5, 4, 3, 3, "brass"), C(-5, -6, -1, 2, 5, 2, "brass"))
    m.part("flick_crown", "head", (0, -9, 0), C(-3, -5, -3, 6, 5, 6, "flame"))
    m.part("flick_crown2", "flick_crown", (0, -5, 0), C(-1.5, -4, -1.5, 3, 4, 3, "flame"))
    for side, x in (("r", -7.5), ("l", 7.5)):
        m.part(f"arm_{side}", "body", (x, -13, 0), C(-2, -1, -2, 4, 14, 4, "char"))
        m.part(f"pauldron_{side}", f"arm_{side}", (0, 0, 0), C(-4 if x < 0 else -3, -4, -4, 7, 6, 8, "magma"),
               C(-3, -8, -1, 2, 4, 2, "brass"), C(0, -7, -1, 2, 3, 2, "brass"))
    m.part("sword", "arm_r", (0, 12, -1), C(-0.5, -0.5, -1, 1, 1, 5, "char"), C(-2, -1.5, -2, 4, 3, 1, "brass"),
           C(-0.5, -3, -20, 1, 6, 18, "magma"), C(-0.5, -4, -19, 1, 1, 16, "flame"), C(-0.5, -3, -22, 1, 4, 2, "magma"))
    m.part("sway_chain", "arm_l", (0, 12, 0), C(-0.5, 0, -0.5, 1, 4, 1, "brass"))
    m.part("sway_chain2", "sway_chain", (0, 4, 0), C(-0.5, 0, -0.5, 1, 4, 1, "brass"))
    m.part("sway_chain3", "sway_chain2", (0, 4, 0), C(-1.5, 0, -1.5, 3, 3, 3, "flame"))
    m.part("cape", "body", (0, -15, 3.5), C(-5, 0, 0, 10, 20, 1, "flame"))
    return m


def elite_water():
    """Tritão Corsário: corpo de serpente marinha em vez de pernas, crista de barbatana, tridente e escudo de concha."""
    m = Model("elite_water", 128)
    m.part("body", "root", (0, 8, 0), C(-4.5, -13, -2.5, 9, 13, 5, "scales"), C(-5, -13, -3, 10, 6, 6, "coral"),
           C(-4.5, -1, -3, 9, 2, 6, "pearl"))
    m.part("sway_tail_1", "body", (0, 0, 0), C(-3.5, 0, -2.5, 7, 6, 5, "scales"))
    m.part("sway_tail_2", "sway_tail_1", (0, 6, 0.5), C(-2.5, 0, -2, 5, 6, 4, "scales"))
    m.part("sway_tail_3", "sway_tail_2", (0, 6, 1), C(-1.5, 0, -1.5, 3, 4, 3, "scales"))
    m.part("sway_fluke", "sway_tail_3", (0, 4, 0), C(-4, 0, -1, 8, 1, 6, "fin"))
    m.part("head", "body", (0, -13, 0), C(-3.5, -8, -3.5, 7, 8, 7, "scales", "fish_face"),
           C(3.5, -6, 0, 3, 5, 0, "fin"), C(-6.5, -6, 0, 3, 5, 0, "fin"),
           C(-3, -10, -3, 1, 2, 1, "coral"), C(2, -10, -3, 1, 2, 1, "coral"), C(-0.5, -11, -3.5, 1, 3, 1, "pearl"))
    m.part("sway_crest", "head", (0, -8, 0), C(0, -4, -3, 0, 6, 9, "fin"))
    for side, x in (("r", -6), ("l", 6)):
        m.part(f"arm_{side}", "body", (x, -11, 0), C(-1.5, -1, -1.5, 3, 13, 3, "scales"))
        m.part(f"pauldron_{side}", f"arm_{side}", (0, 0, 0), C(-3.5 if x < 0 else -2.5, -3, -3.5, 6, 4, 7, "coral"))
    m.part("sword", "arm_r", (0, 11, -1), C(-0.5, -0.5, -24, 1, 1, 30, "brass"), C(-3, -0.5, -26, 6, 1, 2, "brass"),
           C(-3, -0.5, -30, 1, 1, 4, "pearl"), C(2, -0.5, -30, 1, 1, 4, "pearl"), C(-0.5, -0.5, -31, 1, 1, 5, "pearl"))
    m.part("shield", "arm_l", (2, 7, 0), C(0, -5, -5, 1, 10, 10, "coral", "shell_sigil"), C(1, -2, -2, 1, 4, 4, "pearl"))
    m.part("cape", "body", (0, -12, 3), C(-4, 0, 0, 8, 18, 1, "kelp"))
    return m


def elite_earth():
    """Sentinela de Granito: cavaleiro de pedra com chifres de galho, martelo de rocha e escudo-torre de laje."""
    m = Model("elite_earth", 128)
    for side, x in (("r", -3), ("l", 3)):
        m.part(f"leg_{side}", "root", (x, 9, 0), C(-2.5, 0, -2.5, 5, 15, 5, "stone"), C(-3, 13, -4, 6, 2, 7, "stone"),
               C(-3, 4, -3, 6, 3, 6, "moss"))
    m.part("body", "root", (0, 9, 0), C(-5, -4, -3, 10, 5, 6, "stone"), C(-5.5, -1, -3.5, 11, 5, 7, "bark"),
           C(-7, -16, -4, 14, 12, 8, "stone", "crystal_heart"), C(-7, -17, -4, 14, 1, 8, "moss"))
    m.part("head", "body", (0, -16, -1), C(-3.5, -7, -4, 7, 7, 7, "stone", "visor_earth"))
    m.part("antler_l", "head", (3, -7, 0), C(0, -5, -0.5, 1, 5, 1, "bark"), C(0, -4, -0.5, 3, 1, 1, "bark"),
           C(2, -7, -0.5, 1, 3, 1, "bark"), rot=(0, 0, -0.3))
    m.part("antler_r", "head", (-3, -7, 0), C(-1, -5, -0.5, 1, 5, 1, "bark"), C(-3, -4, -0.5, 3, 1, 1, "bark"),
           C(-3, -7, -0.5, 1, 3, 1, "bark"), rot=(0, 0, 0.3))
    for side, x in (("r", -8.5), ("l", 8.5)):
        m.part(f"arm_{side}", "body", (x, -14, 0), C(-2.5, -1, -2.5, 5, 14, 5, "stone"))
        m.part(f"pauldron_{side}", f"arm_{side}", (0, 0, 0), C(-5 if x < 0 else -3, -4, -4, 8, 6, 8, "stone"))
        m.part(f"pulse_shoulder_{side}", f"arm_{side}", (0, -4, 0), C(-1, -5, -1, 2, 5, 2, "crystal"))
    m.part("sword", "arm_r", (0, 12, -1), C(-0.5, -0.5, -16, 1, 1, 20, "bark"), C(-3, -3, -21, 6, 6, 6, "stone"),
           C(-3.2, -1, -19, 1, 2, 2, "crystal"))
    m.part("shield", "arm_l", (2.5, 6, 0), C(0, -9, -6, 2, 18, 12, "stone", "crystal_rune"), C(0, -10, -6, 2, 1, 12, "moss"))
    m.part("cape", "body", (0, -16, 4), C(-6, 0, 0, 12, 22, 1, "leaf"))
    return m


def elite_air():
    """Valquíria do Vento: armadura de prata, elmo alado, grandes asas de penas, glaive de vento e cachecol."""
    m = Model("elite_air", 128)
    for side, x in (("r", -2), ("l", 2)):
        m.part(f"leg_{side}", "root", (x, 9, 0), C(-1.5, 0, -1.5, 3, 15, 3, "silver"), C(-2, 9, -2, 4, 4, 4, "steel"))
    m.part("body", "root", (0, 9, 0), C(-3, -4, -2, 6, 5, 4, "silver"), C(-4, -1, -2.5, 8, 6, 5, "feather"),
           C(-4.5, -13, -2.5, 9, 9, 5, "silver", "chest_wind"))
    m.part("head", "body", (0, -13, 0), C(-3, -7, -3, 6, 7, 6, "silver", "visor_air"))
    m.part("flap_helm_l", "head", (3, -5, 0), C(0, -6, -1, 1, 6, 5, "feather"), rot=(0, 0, -0.4))
    m.part("flap_helm_r", "head", (-3, -5, 0), C(-1, -6, -1, 1, 6, 5, "feather"), rot=(0, 0, 0.4))
    m.part("flap_wing_l", "body", (2, -11, 2.5), C(0, -10, 0, 16, 14, 0, "feather"), C(0, -10.5, -0.5, 16, 1, 1, "silver"))
    m.part("flap_wing_r", "body", (-2, -11, 2.5), C(-16, -10, 0, 16, 14, 0, "feather"), C(-16, -10.5, -0.5, 16, 1, 1, "silver"))
    m.part("arm_r", "body", (-5.5, -12, 0), C(-1, -1, -1, 2, 13, 2, "silver"))
    m.part("arm_l", "body", (5.5, -12, 0), C(-1, -1, -1, 2, 13, 2, "silver"))
    m.part("sword", "arm_r", (0, 11, -1), C(-0.5, -0.5, -18, 1, 1, 24, "silver"), C(-0.5, -3, -27, 1, 4, 9, "wind"),
           C(-0.5, -0.5, 6, 1, 1, 2, "wind"))
    m.part("cape", "body", (0, -13, 2.5), C(-2, 0, 0, 4, 16, 1, "wind"))
    return m


# ============================================================================================ GUARDIÃO

def guardian_fire():
    """Arauto Solar: um sol flutuante com máscara de bronze, coroa de raios girando e chamas pendentes."""
    m = Model("guardian_fire", 128)
    m.part("body", "root", (0, -2, 0), C(-6, -6, -6, 12, 12, 12, "ember"), C(-4, -4, -6.5, 8, 8, 1, "flame"))
    m.part("roll_corona", "body", (0, 0, 0),
           C(-1, -12, -1, 2, 6, 2, "flame"), C(-1, 6, -1, 2, 6, 2, "flame"), C(-12, -1, -1, 6, 2, 2, "flame"),
           C(6, -1, -1, 6, 2, 2, "flame"), C(-10, -10, -1, 3, 3, 2, "ember"), C(7, -10, -1, 3, 3, 2, "ember"),
           C(-10, 7, -1, 3, 3, 2, "ember"), C(7, 7, -1, 3, 3, 2, "ember"))
    m.part("head", "body", (0, 0, -6), C(-4, -4, -1.5, 8, 8, 1, "brass", "mask_face"))
    m.part("halo", "body", (0, -2, 5), *vring(9, "flame"))
    for i, (x, z) in enumerate([(-3, -3), (3, -3), (-3, 3), (3, 3)], start=1):
        m.part(f"tendril_{i}", "body", (x, 6, z), C(-1, 0, -1, 2, 6, 2, "flame"))
        m.part(f"tendril_{i}_tip", f"tendril_{i}", (0, 6, 0), C(-0.5, 0, -0.5, 1, 4, 1, "ember"))
    for side, x in (("r", -12), ("l", 12)):
        m.part(f"hand_{side}", "root", (x, -2, -3), C(-2, -2, -2, 4, 4, 4, "flame"), C(-1.5, -4, -1.5, 3, 2, 3, "ember"))
    for i in range(1, 4):
        m.part(f"orb_{i}", "root", (0, -6, 0), C(-2, -2, -2, 4, 4, 4, "flame"))
    return m


def guardian_water():
    """Medusa Abissal: água-viva gigante de sino translúcido, núcleo de pérola e tentáculos-fita compridos."""
    m = Model("guardian_water", 128)
    m.part("body", "root", (0, -6, 0), C(-8, -6, -8, 16, 6, 16, "jelly"), C(-6, -9, -6, 12, 3, 12, "jelly"),
           C(-3, -11, -3, 6, 2, 6, "jelly"), C(-9, 0, -9, 18, 1, 18, "foam"))
    m.part("eye", "body", (0, -3, -8), C(-2, -2, -1, 4, 4, 1, "pearl", "big_eye"))
    m.part("pulse_core", "body", (0, -3, 0), C(-3, -3, -3, 6, 6, 6, "pearl"))
    for i, (x, z) in enumerate([(-5, -5), (5, -5), (-5, 5), (5, 5)], start=1):
        m.part(f"tendril_{i}", "body", (x, 1, z), C(-1, 0, -1, 2, 10, 2, "jelly"))
        m.part(f"tendril_{i}_tip", f"tendril_{i}", (0, 10, 0), C(-0.5, 0, -0.5, 1, 8, 1, "pearl"))
    for i, (x, z) in enumerate([(-2, -2), (2, -2), (-2, 2), (2, 2)], start=5):
        m.part(f"sway_tent_{i}", "body", (x, 1, z), C(-1.5, 0, 0, 3, 18, 0, "fin"))
    for i in range(1, 4):
        m.part(f"orb_{i}", "root", (0, -6, 0), C(-1.5, -1.5, -1.5, 3, 3, 3, "foam"))
    return m


def guardian_earth():
    """Monólito Ancestral: obelisco de pedra rúnica flutuando, capitel de cristal, punhos de rocha e pedregulhos."""
    m = Model("guardian_earth", 128)
    m.part("body", "root", (0, -4, 0), C(-5, -10, -5, 10, 20, 10, "stone", "rune_column"),
           C(-6, 10, -6, 12, 3, 12, "stone"), C(-6, -12, -6, 12, 2, 12, "moss"))
    m.part("head", "body", (0, -12, 0), C(-4, -4, -4, 8, 4, 8, "stone"), C(-2.5, -7, -2.5, 5, 3, 5, "stone"),
           C(-1, -9, -1, 2, 2, 2, "crystal"))
    m.part("eye", "body", (0, -4, -5), C(-3, -3, -1, 6, 6, 1, "crystal", "big_eye"))
    m.part("spin_ring", "body", (0, 4, 0), C(-9, -1, -0.5, 2, 4, 1, "crystal"), C(7, -1, -0.5, 2, 4, 1, "crystal"),
           C(-0.5, -1, -9, 1, 4, 2, "crystal"), C(-0.5, -1, 7, 1, 4, 2, "crystal"))
    for side, x in (("r", -12), ("l", 12)):
        m.part(f"hand_{side}", "root", (x, -2, -3), C(-3, -3, -3, 6, 6, 6, "stone"), C(-3, -4, -3.5, 6, 1, 1, "crystal"))
    for i in range(1, 4):
        m.part(f"orb_{i}", "root", (0, -6, 0), C(-2.5, -2.5, -2.5, 5, 5, 5, "stone"))
    return m


def guardian_air():
    """Olho da Tempestade: nuvem de tempestade com um olho, raios pendurados, chuva e dois anéis de vento."""
    m = Model("guardian_air", 128)
    m.part("body", "root", (0, -4, 0), C(-7, -5, -6, 14, 9, 12, "storm"), C(-10, -3, -4, 5, 6, 8, "storm"),
           C(5, -3, -4, 5, 6, 8, "storm"), C(-5, -8, -4, 10, 4, 8, "cloud"), C(-4, 4, -4, 8, 3, 8, "storm"))
    m.part("eye", "body", (0, -1, -6), C(-3, -3, -1, 6, 6, 1, "wind", "big_eye"))
    m.part("head", "body", (0, -8, 0), C(-3, -4, -3, 6, 4, 6, "cloud"))
    for i, x in enumerate((-4, 0, 4), start=1):
        m.part(f"flick_bolt_{i}", "body", (x, 7, -1), C(-0.5, 0, -0.5, 1, 5, 1, "lightning"), C(0.5, 4, -0.5, 1, 4, 1, "lightning"))
    for i, (x, z) in enumerate([(-5, -3), (5, -3), (-5, 3), (5, 3)], start=1):
        m.part(f"tendril_{i}", "body", (x, 7, z), C(-0.5, 0, -0.5, 1, 8, 1, "rain"))
        m.part(f"tendril_{i}_tip", f"tendril_{i}", (0, 8, 0), C(-0.5, 0, -0.5, 1, 4, 1, "rain"))
    m.part("spin_ring_a", "root", (0, -4, 0), *ring(13, "wind"))
    m.part("roll_ring_b", "root", (0, -4, 0), *vring(11, "wind"))
    for side, x in (("r", -12), ("l", 12)):
        m.part(f"hand_{side}", "root", (x, -2, -3), C(-2.5, -2.5, -2.5, 5, 5, 5, "cloud"))
    for i in range(1, 4):
        m.part(f"orb_{i}", "root", (0, -6, 0), C(-1.5, -1.5, -1.5, 3, 3, 3, "wind"))
    return m


# ============================================================================================ SOBERANO

def monarch_fire():
    """Rei Vulcânico: o tronco é um vulcão com pluma de lava, coroa derretida, punhos de magma e pilares de fogo."""
    m = Model("monarch_fire", 256)
    for side, x in (("r", -7), ("l", 7)):
        m.part(f"leg_{side}", "root", (x, 6, 0), C(-4.5, 0, -4.5, 9, 10, 9, "magma"))
        m.part(f"shin_{side}", f"leg_{side}", (0, 9, 0), C(-5, 0, -5, 10, 9, 10, "char"), C(-6, 6, -7, 12, 3, 12, "char"))
    m.part("body", "root", (0, 6, 0), C(-10, -4, -6, 20, 6, 12, "char"), C(-6, 2, -6.5, 12, 8, 1, "flame"))
    m.part("chest", "body", (0, -4, 0), C(-13, -12, -9, 26, 12, 18, "magma"), C(-11, -20, -8, 22, 8, 16, "magma"),
           C(-9, -26, -7, 18, 6, 14, "char"), C(-4, -16, -9.5, 8, 8, 1, "flame", "core"), rot=(0.2, 0, 0))
    m.part("flick_plume", "chest", (0, -26, 2), C(-4, -8, -4, 8, 8, 8, "flame"))
    m.part("flick_plume2", "flick_plume", (0, -8, 0), C(-2.5, -7, -2.5, 5, 7, 5, "flame"))
    m.part("flick_plume3", "flick_plume2", (0, -7, 0), C(-1, -5, -1, 2, 5, 2, "ember"))
    m.part("head", "chest", (0, -20, -8), C(-4.5, -8, -5, 9, 9, 9, "char", "ember_face"))
    m.part("jaw", "head", (0, 1, -1), C(-4, 0, -4.5, 8, 2, 8, "char", "teeth"))
    m.part("crown", "head", (0, -8, 0), C(-5, -2, -5.5, 10, 2, 11, "brass"), C(-4.5, -6, -5, 2, 4, 2, "flame"),
           C(-1, -7, -5.5, 2, 5, 2, "flame"), C(2.5, -6, -5, 2, 4, 2, "flame"))
    for i, (x, rz) in enumerate([(-7, -0.35), (0, 0.0), (7, 0.35)], start=1):
        m.part(f"shard_{i}", "chest", (x, -16, 8), C(-1.5, -12, -1.5, 3, 12, 3, "flame"), rot=(-0.5, 0, rz))
    for side, sx in (("r", -1), ("l", 1)):
        m.part(f"arm_{side}", "chest", (16 * sx, -18, 0), C(-5, -2, -5, 10, 16, 10, "magma"),
               C(-7 if sx < 0 else -5, -6, -6, 12, 8, 12, "char"))
        m.part(f"flick_shoulder_{side}", f"arm_{side}", (0, -6, 0), C(-2, -6, -2, 4, 6, 4, "flame"))
        m.part(f"forearm_{side}", f"arm_{side}", (0, 14, 0), C(-5.5, 0, -5.5, 11, 14, 11, "magma"))
        m.part(f"fist_{side}", f"forearm_{side}", (0, 14, 0), C(-6.5, 0, -6.5, 13, 9, 13, "char"))
        m.part(f"flick_drip_{side}", f"fist_{side}", (0, 9, 0), C(-1, 0, -1, 2, 4, 2, "flame"))
    return m


def monarch_water():
    """Leviatã Coroado: rei-kraken de escamas, barba de tentáculos, coroa de coral, pinças de caranguejo e capa de algas."""
    m = Model("monarch_water", 256)
    for side, x in (("r", -7), ("l", 7)):
        m.part(f"leg_{side}", "root", (x, 6, 0), C(-4, 0, -4, 8, 10, 8, "scales"))
        m.part(f"shin_{side}", f"leg_{side}", (0, 9, 0), C(-4.5, 0, -4.5, 9, 9, 9, "scales"), C(-5.5, 6, -8, 11, 3, 13, "fin"))
    m.part("body", "root", (0, 6, 0), C(-9, -4, -5, 18, 6, 10, "deep"), C(-6, 2, -5.5, 12, 10, 1, "kelp"))
    m.part("chest", "body", (0, -4, 0), C(-12, -24, -8, 24, 24, 16, "scales"), C(-8, -18, -8.5, 16, 14, 1, "foam"),
           C(-3, -20, -9, 6, 6, 1, "pearl", "core"), rot=(0.2, 0, 0))
    for i, (x, rz) in enumerate([(-6, -0.4), (0, 0.0), (6, 0.4)], start=1):
        m.part(f"shard_{i}", "chest", (x, -18, 7), C(-2, -12, -2, 4, 12, 4, "coral"), rot=(-0.5, 0, rz))
    m.part("cape", "chest", (0, -23, 8), C(-10, 0, 0, 20, 26, 1, "kelp"))
    m.part("head", "chest", (0, -24, -6), C(-6, -10, -7, 12, 11, 11, "deep", "kraken_face"))
    m.part("jaw", "head", (0, 1, -1), C(-5, 0, -6, 10, 2, 9, "deep"))
    for i, x in enumerate((-4.5, -1.5, 1.5, 4.5), start=1):
        m.part(f"sway_beard_{i}", "jaw", (x, 2, -5), C(-1, 0, -1, 2, 12, 2, "deep"), C(-1, 12, -1, 2, 2, 2, "pearl"))
    m.part("crown", "head", (0, -10, 0), C(-6, -2, -7, 12, 3, 12, "coral"), C(-5.5, -7, -6.5, 2, 5, 2, "coral"),
           C(-1, -9, -7, 2, 7, 2, "pearl"), C(3.5, -7, -6.5, 2, 5, 2, "coral"))
    for side, sx in (("r", -1), ("l", 1)):
        m.part(f"arm_{side}", "chest", (15 * sx, -20, 0), C(-4, -2, -4.5, 8, 16, 9, "scales"),
               C(-6 if sx < 0 else -5, -5, -6, 11, 7, 12, "coral"))
        m.part(f"forearm_{side}", f"arm_{side}", (0, 14, 0), C(-5, 0, -5, 10, 12, 10, "scales"))
        m.part(f"fist_{side}", f"forearm_{side}", (0, 12, 0), C(-5, 0, -10, 10, 5, 13, "coral"))
        m.part(f"sway_pincer_{side}", f"fist_{side}", (0, 5, 0), C(-4, 0, -9, 8, 3, 11, "coral"))
    return m


def monarch_earth():
    """Titã Ancestral: árvore colossal de casca e musgo, copa frondosa com frutos de âmbar e coração de cristal."""
    m = Model("monarch_earth", 256)
    for side, x in (("r", -7), ("l", 7)):
        m.part(f"leg_{side}", "root", (x, 6, 0), C(-4, 0, -4, 8, 10, 8, "bark"))
        m.part(f"shin_{side}", f"leg_{side}", (0, 9, 0), C(-4.5, 0, -4.5, 9, 9, 9, "bark"), C(-6, 6, -8, 12, 3, 14, "bark"))
    m.part("body", "root", (0, 6, 0), C(-9, -4, -6, 18, 6, 12, "bark"), C(-9.5, -4.5, -6.5, 19, 2, 13, "moss"))
    m.part("chest", "body", (0, -4, 0), C(-11, -24, -8, 22, 24, 16, "bark"), C(-11.5, -24, -8.5, 23, 4, 17, "moss"),
           C(-4, -17, -9, 8, 8, 1, "crystal", "core"), rot=(0.15, 0, 0))
    for i, (x, rz) in enumerate([(-6, -0.4), (0, 0.0), (6, 0.4)], start=1):
        m.part(f"shard_{i}", "chest", (x, -18, 7), C(-2, -12, -2, 4, 12, 4, "crystal"), rot=(-0.5, 0, rz))
    m.part("cape", "chest", (0, -23, 8), C(-10, 0, 0, 20, 24, 1, "moss"))
    m.part("head", "chest", (0, -24, -5), C(-5, -9, -5, 10, 10, 10, "bark", "tree_face"))
    m.part("jaw", "head", (0, 0, -1), C(-4.5, 0, -4.5, 9, 2, 8, "bark"))
    m.part("canopy", "head", (0, -9, 0), C(-10, -8, -9, 20, 8, 18, "leaf"), C(-7, -12, -7, 14, 4, 14, "leaf"),
           C(-9, -3, -9.5, 2, 2, 1, "amber"), C(6, -5, -9.5, 2, 2, 1, "amber"), C(-2, -7, -9.5, 2, 2, 1, "amber"))
    m.part("branch_l", "head", (5, -6, -1), C(0, -1.5, -1.5, 8, 3, 3, "bark"), C(6, -7, -1, 2, 6, 2, "bark"), rot=(0, 0, -0.4))
    m.part("branch_r", "head", (-5, -6, -1), C(-8, -1.5, -1.5, 8, 3, 3, "bark"), C(-8, -7, -1, 2, 6, 2, "bark"), rot=(0, 0, 0.4))
    for side, sx in (("r", -1), ("l", 1)):
        m.part(f"arm_{side}", "chest", (15 * sx, -20, 0), C(-4, -2, -4.5, 8, 16, 9, "bark"), C(-5, -3, -5.5, 10, 3, 11, "moss"))
        m.part(f"forearm_{side}", f"arm_{side}", (0, 14, 0), C(-4.5, 0, -4.5, 9, 14, 9, "bark"))
        m.part(f"fist_{side}", f"forearm_{side}", (0, 14, 0), C(-5, 0, -5, 10, 4, 10, "bark"),
               C(-5, 4, -5, 2, 6, 2, "bark"), C(-1, 4, -5, 2, 7, 2, "bark"), C(3, 4, -5, 2, 6, 2, "bark"),
               C(-1, 4, 3, 2, 5, 2, "bark"))
    m.part("orbit_pollen", "root", (0, -20, 0), C(17, 0, 0, 2, 2, 2, "amber"), C(-18, 4, 3, 2, 2, 2, "amber"),
           C(2, -3, 16, 2, 2, 2, "leaf"), C(-4, 2, -18, 2, 2, 2, "amber"))
    return m


def monarch_air():
    """Imperador dos Céus: gigante de armadura prateada com asas imensas, elmo alado, auréola de vento e trono de nuvem."""
    m = Model("monarch_air", 256)
    for side, x in (("r", -6), ("l", 6)):
        m.part(f"leg_{side}", "root", (x, 6, 0), C(-3.5, 0, -3.5, 7, 10, 7, "silver"))
        m.part(f"shin_{side}", f"leg_{side}", (0, 9, 0), C(-4, 0, -4, 8, 9, 8, "silver"), C(-4.5, 6, -6, 9, 3, 10, "steel"))
    m.part("bob_cloud", "root", (0, 21, 0), C(-14, 0, -10, 28, 3, 20, "cloud"))
    m.part("body", "root", (0, 6, 0), C(-8, -4, -5, 16, 6, 10, "silver"), C(-7, 2, -5.5, 14, 8, 1, "wind"))
    m.part("chest", "body", (0, -4, 0), C(-10, -22, -7, 20, 22, 14, "silver", "chest_wind"),
           C(-6, -24, -6, 12, 2, 12, "steel"), C(-3, -16, -7.5, 6, 6, 1, "lightning", "core"), rot=(0.1, 0, 0))
    m.part("flap_wing_l", "chest", (6, -20, 7), C(0, -16, 0, 28, 22, 0, "feather"), C(0, -16.5, -1, 28, 2, 2, "silver"))
    m.part("flap_wing_r", "chest", (-6, -20, 7), C(-28, -16, 0, 28, 22, 0, "feather"), C(-28, -16.5, -1, 28, 2, 2, "silver"))
    m.part("cape", "chest", (0, -21, 7), C(-9, 0, 0, 18, 24, 1, "wind"))
    m.part("head", "chest", (0, -24, -4), C(-4.5, -9, -5, 9, 9, 9, "silver", "visor_air"))
    m.part("jaw", "head", (0, 0, -1), C(-3, 0, -3.5, 6, 1, 5, "silver"))
    m.part("flap_helm_l", "head", (4.5, -6, 0), C(0, -8, -1, 1, 8, 6, "feather"), rot=(0, 0, -0.3))
    m.part("flap_helm_r", "head", (-4.5, -6, 0), C(-1, -8, -1, 1, 8, 6, "feather"), rot=(0, 0, 0.3))
    m.part("spin_halo", "head", (0, -12, 0), *ring(8, "wind"))
    for side, sx in (("r", -1), ("l", 1)):
        m.part(f"arm_{side}", "chest", (13 * sx, -20, 0), C(-3.5, -2, -4, 7, 16, 8, "silver"),
               C(-6 if sx < 0 else -4, -5, -5, 10, 6, 10, "steel"), C(-0.5, -9, -1, 1, 5, 6, "feather"))
        m.part(f"forearm_{side}", f"arm_{side}", (0, 14, 0), C(-4, 0, -4, 8, 13, 8, "silver"))
        m.part(f"fist_{side}", f"forearm_{side}", (0, 13, 0), C(-4.5, 0, -4.5, 9, 7, 9, "steel"),
               C(-1, 7, -1, 2, 3, 2, "lightning"))
    return m


# ============================================================================================ O SOBERANO

def sovereign():
    """O Soberano: o gigante que tomou o poder dos cinco Monarcas. Cada pedaço do corpo vem de um deles:
    braço de pedra e chifres do Colosso Rei, perna de magma e pluma de vulcão do Rei Vulcânico, pinça e
    barba de tentáculos do Leviatã Coroado, perna de raízes e copa do Titã Ancestral, asas e auréola do
    Imperador dos Céus. A coroa tem uma ponta de cada Monarca e cinco fragmentos orbitam em volta dele.
    Não tem variante elemental (o renderer desenha o brilho trocando de cor entre os elementos)."""
    m = Model("sovereign", 256)
    # Pernas: direita de magma (Rei Vulcânico), esquerda de casca e raízes (Titã Ancestral).
    m.part("leg_r", "root", (-7, 6, 0), C(-4.5, 0, -4.5, 9, 10, 9, "magma"))
    m.part("shin_r", "leg_r", (0, 9, 0), C(-5, 0, -5, 10, 9, 10, "char"), C(-6, 6, -7, 12, 3, 12, "char"))
    m.part("flick_lava_r", "shin_r", (0, 2, -5), C(-1.5, 0, -1, 3, 5, 1, "flame"))
    m.part("leg_l", "root", (7, 6, 0), C(-4.5, 0, -4.5, 9, 10, 9, "bark"), C(-5, -1, -5, 10, 3, 10, "moss"))
    m.part("shin_l", "leg_l", (0, 9, 0), C(-4.5, 0, -4.5, 9, 9, 9, "bark"), C(-6, 6, -8, 12, 3, 14, "bark"),
           C(4, 7, -2, 4, 2, 2, "bark"))
    # Cintura de obsidiana com cinto de ouro e saiote de algas (Leviatã Coroado).
    m.part("body", "root", (0, 6, 0), C(-10, -4, -6, 20, 6, 12, "obsidian"), C(-10.5, -4.5, -6.5, 21, 2, 13, "gold"),
           C(-7, 2, -6.5, 14, 10, 1, "kelp"))
    # Peito: obsidiana com o núcleo e um fragmento de cada Monarca elemental cravado em volta dele.
    m.part("chest", "body", (0, -4, 0),
           C(-13, -26, -9, 26, 26, 18, "obsidian"),
           C(-9, -12, -9.5, 18, 8, 1, "rock"),
           C(-4, -22, -10, 8, 8, 1, "gem", "core"),
           C(-9, -24, -9.5, 3, 3, 1, "flame"), C(6, -24, -9.5, 3, 3, 1, "pearl"),
           C(-9, -15, -9.5, 3, 3, 1, "amber"), C(6, -15, -9.5, 3, 3, 1, "lightning"),
           C(-13, -30, -1, 7, 4, 7, "char"),
           rot=(0.15, 0, 0))
    # Ombro direito: o vulcão do Rei Vulcânico. Ombro esquerdo: a copa do Titã Ancestral.
    m.part("flick_plume", "chest", (-9.5, -30, 2.5), C(-3, -6, -3, 6, 6, 6, "flame"))
    m.part("flick_plume2", "flick_plume", (0, -6, 0), C(-2, -5, -2, 4, 5, 4, "flame"))
    m.part("flick_plume3", "flick_plume2", (0, -5, 0), C(-1, -4, -1, 2, 4, 2, "ember"))
    m.part("canopy", "chest", (9, -26, 2), C(-6, -7, -5, 12, 7, 10, "leaf"), C(-4, -10, -3, 8, 3, 6, "leaf"),
           C(-5, -4, -5.5, 2, 2, 1, "amber"), C(3, -6, -5.5, 2, 2, 1, "amber"), C(-1, 0, -1, 2, 4, 2, "bark"))
    # Asas do Imperador dos Céus e capa.
    m.part("flap_wing_l", "chest", (7, -22, 9), C(0, -20, 0, 34, 26, 0, "feather"), C(0, -20.5, -1, 34, 2, 2, "silver"))
    m.part("flap_wing_r", "chest", (-7, -22, 9), C(-34, -20, 0, 34, 26, 0, "feather"), C(-34, -20.5, -1, 34, 2, 2, "silver"))
    m.part("cape", "chest", (0, -25, 9), C(-11, 0, 0, 22, 28, 1, "cape"))
    # Cabeça do Colosso com a barba de tentáculos do Leviatã.
    m.part("head", "chest", (0, -26, -6), C(-5.5, -10, -6, 11, 11, 11, "rock", "colossus_face"))
    m.part("jaw", "head", (0, 1, -1), C(-5, 0, -5.5, 10, 3, 9, "rock", "teeth"))
    for i, x in enumerate((-4, -1.5, 1.5, 4), start=1):
        m.part(f"sway_beard_{i}", "jaw", (x, 3, -4.5), C(-1, 0, -1, 2, 10, 2, "deep"), C(-1, 10, -1, 2, 2, 2, "pearl"))
    # Coroa das cinco coroas: uma ponta de cada Monarca (fogo, coral, ouro, âmbar, raio).
    m.part("crown", "head", (0, -10, 0),
           C(-6.5, -2, -7, 13, 3, 13, "gold", "crown_gems"),
           C(-6, -8, -6.5, 2, 6, 2, "flame"), C(-3.5, -10, -7, 2, 8, 2, "coral"), C(-1, -12, -7, 2, 10, 2, "gold"),
           C(1.5, -10, -7, 2, 8, 2, "amber"), C(4, -8, -6.5, 2, 6, 2, "lightning"))
    m.part("spin_halo", "head", (0, -17, 0), *ring(9, "wind"))
    for side, sx in (("r", -1), ("l", 1)):
        m.part(f"horn_{side}", "head", (5.5 * sx, -7, -1), C(-8 if sx < 0 else 0, -2, -2, 8, 4, 4, "bone"), rot=(0, 0, 0.4 * sx))
        m.part(f"horn_{side}2", f"horn_{side}", (8 * sx, 0, 0), C(-1.5, -8, -1.5, 3, 8, 3, "bone"), rot=(0, 0, -0.9 * sx))
    # Braço direito: pedra e ouro do Colosso Rei.
    m.part("arm_r", "chest", (-16, -22, 0), C(-5, -2, -5, 10, 17, 10, "rock"))
    m.part("pauldron_r", "arm_r", (0, 0, 0), C(-7, -5, -6, 12, 8, 12, "obsidian"),
           C(-4, -10, -1, 2, 5, 2, "bone"), C(0, -9, -1, 2, 4, 2, "bone"))
    m.part("forearm_r", "arm_r", (0, 15, 0), C(-5.5, 0, -5.5, 11, 14, 11, "rock"), C(-6, 2, -6, 12, 4, 12, "gold"))
    m.part("fist_r", "forearm_r", (0, 14, 0), C(-6.5, 0, -6.5, 13, 10, 13, "obsidian"))
    # Braço esquerdo: escamas e pinça do Leviatã Coroado.
    m.part("arm_l", "chest", (16, -22, 0), C(-4.5, -2, -4.5, 9, 17, 9, "scales"))
    m.part("pauldron_l", "arm_l", (0, 0, 0), C(-5, -5, -6, 12, 7, 12, "coral"))
    m.part("forearm_l", "arm_l", (0, 15, 0), C(-5, 0, -5, 10, 13, 10, "scales"))
    m.part("fist_l", "forearm_l", (0, 13, 0), C(-5, 0, -11, 10, 5, 14, "coral"))
    m.part("sway_pincer_l", "fist_l", (0, 5, 0), C(-4, 0, -10, 8, 3, 12, "coral"))
    # Os cinco fragmentos dos Monarcas orbitando o corpo.
    m.part("orbit_shards", "root", (0, -12, 0), C(22, 0, 0, 3, 3, 3, "flame"), C(-25, 4, 4, 3, 3, 3, "pearl"),
           C(4, -3, 22, 3, 3, 3, "amber"), C(-6, 2, -25, 3, 3, 3, "lightning"), C(15, 6, -18, 3, 3, 3, "gem"))
    return m


VARIANTS = [
    minion_fire, minion_water, minion_earth, minion_air,
    elite_fire, elite_water, elite_earth, elite_air,
    guardian_fire, guardian_water, guardian_earth, guardian_air,
    monarch_fire, monarch_water, monarch_earth, monarch_air,
]
