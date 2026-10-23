# 🎮 Gameplay — Regras e mecânicas

Referência completa do que o Solo Mod faz dentro do jogo. Para saber **onde** cada coisa está no código, veja [ARCHITECTURE.md](ARCHITECTURE.md).

- [Maestria (stats)](#-maestria-stats)
- [Elementos](#-elementos)
- [Tiers](#-tiers)
- [Armas](#️-armas)
- [Mobs por tier](#-mobs-por-tier)
- [Criaturas próprias](#-criaturas-próprias)
- [Interface](#️-interface)
- [Roteiros de teste](#-roteiros-de-teste)

---

## 📊 Maestria (stats)

O jogador tem 4 atributos de **Maestria**. Os pontos vêm do **XP Global**: cada **100 XP = 1 ponto** para distribuir.

| Stat | Efeito no jogador | Requisito de |
|---|---|---|
| **FOR** (Força) | +0.25 de dano por ponto | armas Pesadas / Médias |
| **AGI** (Agilidade) | +1% vel. de ataque e +0.5% vel. de movimento por ponto | armas Leves / Lançáveis |
| **INT** (Inteligência) | mana máx. = 20 + 5×INT · regenera 1 + INT/10 por segundo | habilidades ativas e varinhas |
| **VIT** (Vitalidade) | +1 de vida e −1% de dano recebido por ponto (máx. 50%) | — |

- **XP Global**: só mobs com perfil (mobs do mod) dão XP. Por tier: **10 / 40 / 150 / 500**.
- Valores de balanceamento ficam em `event/StatsLogic.java` e `attachment/PlayerStatsAttachment.java`.

## 🔥 Elementos

```
Água ──▶ Fogo ──▶ Ar ──▶ Terra ──▶ Água      (A ──▶ B = A tem vantagem sobre B)
```

| Situação | Multiplicador |
|---|---|
| Vantagem | **1.5x** + passiva do elemento |
| Desvantagem ou mesmo elemento | **0.5x** |
| Pares opostos (Água×Ar, Fogo×Terra) ou sem elemento | **1.0x** |

## 🏅 Tiers

O tier é a "qualidade" do mob, e portanto do material e da arma que sai dele.

| Tier | Nome | Dano | Bônus de stat | Elemento | Habilidade |
|---|---|---|---|---|---|
| `NORMAL` | Normal | 1x | — | nenhum | nenhuma |
| `OP` | Elite | 1.25x | +3 | passiva leve | — |
| `SUB_BOSS` | S. Chefe | 1.5x | +5 | **puro** (nunca cai abaixo de 1.0x) | habilidade menor |
| `BOSS` | Chefe | 2x | +10 | passiva máxima | Explosão Elemental (suprema) |

**Travamento**: se o jogador não tem o stat exigido pela arma, o golpe é básico (só o multiplicador do tier). Elemento, escalonamento (+2% por ponto no stat) e habilidade ficam **adormecidos**. Armas Pesadas e Médias ainda perdem 40% da velocidade de ataque.

Fórmula do golpe com arma modular (`event/CombatEvents.java`):

```
travada:    dano_vanilla × tier
destravada: dano_vanilla × tier × (1 + 2% × stat_efetivo) × multiplicador_elemental
```

## ⚔️ Armas

7 classes de arma. Cada uma tem um item próprio (`solomod:<classe>`), e o tier, elemento, requisito e habilidade de cada exemplar ficam gravados no item.

| Classe | Peso | Stat | Habilidade de tier padrão | Habilidade de classe |
|---|---|---|---|---|
| Espadão (`greatsword`) | Pesada | FOR | Impacto Sísmico | — |
| Machado (`axe`) | Pesada | FOR | Impacto Sísmico | **Salto Esmagador** (clique direito) |
| Espada longa (`longsword`) | Média | FOR | Investida | **Giro da Claymore** (todo ataque) |
| Adaga (`dagger`) | Leve | AGI | Investida | **Arremesso** (clique direito) |
| Rapieira (`rapier`) | Leve | AGI | Investida | **Estocada Relâmpago** (clique direito) |
| Kunai (`kunai`) | Lançável | AGI | Investida | — |
| Varinha (`wand`) | Canalizador | INT | Disparo de Mana | — |

### Habilidades de tier (gastam mana)

| Habilidade | Mana | Recarga | INT mínima |
|---|---|---|---|
| Investida (`dash_strike`) | 15 | 3 s | 5 |
| Impacto Sísmico (`ground_slam`) | 25 | 5 s | 10 |
| Disparo de Mana (`mana_bolt`) | 10 | 1,5 s | 5 |
| Explosão Elemental (`elemental_burst`) | 40 | 10 s | 20 |

Exigem Maestria + INT mínima + mana. A potência escala com o tier (habilidade menor = 1x, suprema = 2x). Nas armas com habilidade de classe, a de tier passa para **agachar + clique direito**.

### Habilidades de classe (sem mana, qualquer tier)

| Arma | Habilidade |
|---|---|
| Rapieira | **Estocada Relâmpago**: dispara para a frente perfurando quem estiver no caminho |
| Adaga | **Arremesso**: crava no chão ou no alvo; na mão fica um contorno fantasma. Clique de novo e ela volta girando, ferindo quem estiver no caminho (passar por cima também recolhe) |
| Espada longa | **Giro da Claymore**: todo golpe vira um giro de 360° que acerta todos em volta |
| Machado | **Salto Esmagador**: salta, mergulha e explode o chão num CRÍTICO em área, com a câmera tremendo |

### Visual das armas

- Texturas em pixel art. Cada arma tem **textura própria por elemento**: Fogo (lâmina em brasa), Água (ondas e gotas), Terra (pedra, musgo e cristais) e Ar (prata, penas e vento).
- Rapieira e claymore têm **modelo 3D**, com paleta e peças extras por elemento.
- Armas de S. Chefe e Chefe brilham.
- Todas as combinações (classe × tier × elemento) ficam na aba **Solo Mod** do criativo.

## 👹 Mobs por tier

Qualquer mob vira "mob do mod" ao receber um **perfil** (tier + elemento). O perfil dá tamanho, atributos, título colorido, aura, animações e um **cérebro de combate** que substitui a IA vanilla enquanto o mob tem alvo.

| Tier | Título | Tamanho / vida / dano | Ociosa | IA | Ataques |
|---|---|---|---|---|---|
| NORMAL | • Lacaio | 0.85x / 1x / 1x | corre curvado, trote | **bando**: grita chamando aliados, fica mais rápido com 2+ aliados, foge com <30% de vida | **Bote** |
| OP | ★ Elite | 1.2x / 2.5x / 1.5x | balança os ombros | **duelista**: circula o alvo enquanto recarrega; **apara** 30% dos golpes de frente e contra-ataca | **Investida** telegrafada · **Golpe Triplo** em leque |
| SUB_BOSS | ✦ Guardião | 1.6x / 6x / 2x, barra roxa | flutua, 3 motes orbitando | **mago**: recua se colam, aproxima se fogem, teleporta às costas de quem foge | **Rajada** de 3 orbes com mira preditiva · **Repulsão** · **Salto Esmagador** · **Translocar** · **Invocação** a 50% |
| BOSS | ♛ Soberano | 2.3x / 14x / 2.8x, barra dourada, brilho | respiração pesada | **3 fases** (60% / 25%) com rugido invulnerável; lê se o jogador luta de perto ou de longe e escolhe ataques contra isso | **Golpe Sísmico** (pule) · **Chuva Elemental** · **Investida** 1/2/3x (parede = atordoado) · **Vórtice** · **Sopro** em cone · convoca Elites e Lacaios |

Todo ataque especial é **telegrafado** (agachar/tremer + partículas na cor do elemento) e aplica a passiva do elemento do mob.

### 😡 Fúria (Guardião e Soberano)

Com 1/4 da vida, o chefe ruge (invulnerável), arremessa quem está perto e chama Lacaios. Durante **20 s** ele fica com aura vermelha, causa +35% de dano, recebe −35% de dano e usa ataques **totalmente diferentes**:

- **Guardião**: Tempestade de Orbes, Olhar Fulminante (raio girando 360°) e Translocação Frenética. Para de fugir e parte para cima.
- **Soberano**: Marcha Sísmica, Meteoro Colossal, Investida Sem Fim (ricocheteia nas paredes) e Anel de Erupções. A Fúria é a fase 3.

Quando a Fúria acaba, o chefe fica **exausto por 3 s** e recebe +25% de dano.

### 💀 Morte do Soberano

O Soberano se **desintegra** por 5 s. Os jogadores por perto entram numa **cinemática**: faixas pretas, HUD escondido, câmera virando para o chefe e o letreiro "SOBERANO DERROTADO".

## 🐉 Criaturas próprias

Cada tier tem uma criatura com modelo próprio, e cada criatura tem uma forma **completamente diferente por elemento**: são 20 modelos e texturas. Elas nascem pelo botão **Invocar mob** da aba Teste, por `/modtest spawn <tier> <elemento>` ou pelos ovos da aba **Solo Mod** do criativo.

| | Sem elemento | 🔥 Fogo | 💧 Água | 🪨 Terra | 🌪️ Ar |
|---|---|---|---|---|---|
| **Lacaio** | Diabrete | Diabrete Ígneo | Diabrete Abissal | Diabrete de Pedra | Sílfide Travessa |
| **Elite** | Duelista Espectral | Executor Infernal | Tritão Corsário | Sentinela de Granito | Valquíria do Vento |
| **Guardião** | Oráculo | Arauto Solar | Medusa Abissal | Monólito Ancestral | Olho da Tempestade |
| **Soberano** | Colosso Rei | Rei Vulcânico | Leviatã Coroado | Titã Ancestral | Imperador dos Céus |

<details>
<summary>Descrição de cada forma</summary>

| Criatura | Aparência |
|---|---|
| Diabrete | gárgula de cabeça enorme e asas |
| Diabrete Ígneo | crânio com coroa de chamas, jatos de fogo |
| Diabrete Abissal | peixe-goblin com isca luminosa |
| Diabrete de Pedra | pedregulho com cristais e seixos em órbita |
| Sílfide Travessa | espírito-pássaro sem pernas, anel de vento |
| Duelista Espectral | cavaleiro, espada e escudo |
| Executor Infernal | cutelo em brasa, mangual em chamas |
| Tritão Corsário | cauda de serpente, tridente, escudo de concha |
| Sentinela de Granito | martelo de rocha, escudo-torre, chifres de galho |
| Valquíria do Vento | asas de penas, glaive, elmo alado |
| Oráculo | manto, olho no peito, auréola |
| Arauto Solar | sol com máscara e coroa de raios |
| Medusa Abissal | água-viva gigante com tentáculos-fita |
| Monólito Ancestral | obelisco rúnico com pedregulhos |
| Olho da Tempestade | nuvem com raios, chuva e anéis de vento |
| Colosso Rei | gigante de pedra coroado |
| Rei Vulcânico | tronco-vulcão com pluma de lava |
| Leviatã Coroado | rei-kraken com pinças de caranguejo |
| Titã Ancestral | árvore colossal com copa e frutos de âmbar |
| Imperador dos Céus | armadura prateada, asas imensas, trono de nuvem |

</details>

Olhos, gemas, chamas e rachaduras ficam numa **camada emissiva** que pulsa na cor do elemento. As partes decorativas (chamas, nadadeiras, anéis, asas) se mexem sozinhas.

## 🖥️ Interface

- **Tecla K**: tela de Maestria.
  - Aba **Maestria**: distribui pontos (+1/+5) e mostra o efeito de cada stat.
  - Aba **Teste**: atalhos dos comandos `/modtest` (gerar arma, invocar ou marcar mob, ±10 stats, +100 XP, mana, reset). Exige cheats/OP.
- **HUD** (canto superior esquerdo): nível, XP até o próximo ponto, barra de mana animada, aviso de pontos livres, estado da arma na mão (ativa/adormecida) e recarga da habilidade.
- **Feedback de combate**: números de dano flutuantes (na cor do elemento, maiores e com "!" na vantagem), som de crítico, partículas por habilidade e animações do jogador (giro, estocada, mortal do machado).

## 🧪 Roteiros de teste

### Com interface

1. Crie um mundo Criativo **com cheats** e aperte **K** → aba **Teste**.
2. Classe *Adaga*, Tier *S. Chefe*, Elemento *Fogo* → **Gerar arma na mão**. O HUD mostra "Adormecida (requer 30 AGI)".
3. Troque o Elemento para *Ar* → **Invocar mob**. Bata nele: números brancos, sem elemento.
4. **+10 AGI** três vezes (e +10 INT uma vez). O HUD fica "Fogo · Ativa"; bata de novo: números laranja com "!" e o alvo pegando fogo.
5. **Agachar + clique direito**: Investida com rastro de partículas; a barra de recarga esvazia e enche.
6. **+100 XP** e volte à aba **Maestria** para gastar o ponto.

### Com comandos

```mcfunction
/modtest weapon dagger sub_boss fire          # adaga de S. Chefe, Fogo, requer 30 AGI
/summon zombie ~ ~ ~3
/modtest mob @e[type=zombie,limit=1,sort=nearest] op air   # zumbi Elite de Ar
/modtest set agi 30
/modtest set int 10                           # INT mínima da Investida é 5
/modtest weapon greatsword boss water         # espadão de Chefe, requer 50 FOR
/modtest spawn boss fire                      # Rei Vulcânico
/modtest stats
```

Lista completa de comandos: [README](../README.md#-comandos-de-teste).
