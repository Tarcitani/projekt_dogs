# Solo Mod (MVP) — NeoForge 1.21.1 / Java 21

Núcleo de **Maestria (FOR/AGI/INT/VIT)**, **armas modulares por Tier**, **combate elemental** e **mobs por tier** com modelos, IA e ataques próprios.

Repositório: https://github.com/Tarcitani/projekt_dogs · Como colaborar: [CONTRIBUTING.md](CONTRIBUTING.md)

## Primeiros passos (para quem vai colaborar)

### Requisitos

| Ferramenta | Versão | Para quê |
|---|---|---|
| JDK | **21** (ex.: Temurin 21) | compilar e rodar o mod — o Gradle baixa sozinho se faltar (foojay) |
| Git | qualquer recente | versionamento |
| IDE | IntelliJ IDEA ou VS Code (Extension Pack for Java) | edição e debug |
| Python + Pillow | 3.10+ / `pip install pillow` | **opcional**: só para regerar texturas/modelos em `tools/` |

Não precisa instalar Gradle nem Minecraft: o wrapper (`gradlew`) e o NeoForge baixam tudo na primeira execução (demora alguns minutos).

### Clonar e rodar

```bash
git clone https://github.com/Tarcitani/projekt_dogs.git
cd projekt_dogs
./gradlew runClient      # abre o Minecraft com o mod (Windows: gradlew.bat runClient)
./gradlew runServer      # servidor dedicado sem GUI
./gradlew build          # gera build/libs/solomod-0.1.0.jar
```

- **IntelliJ**: *File → Open* na pasta do projeto; ele importa o Gradle e cria as configurações *Client*/*Server*.
- **VS Code**: abra a pasta; o plugin do NeoForge gera `.vscode/launch.json` na sincronização do Gradle (o arquivo tem caminhos da sua máquina, por isso **não** vai para o Git).
- O mundo de teste e configs ficam em `run/` (ignorado pelo Git — cada um tem o seu).
- Se o Gradle reclamar da versão do NeoForge, troque `neo_version` em `gradle.properties` por qualquer `21.1.x` atual.

## Estrutura do código

Pacote base: `src/main/java/com/tarcitani/solomod/` (entrada: `SoloMod.java`, que registra tudo).

| Pacote | Responsabilidade | Principais arquivos |
|---|---|---|
| `core/` | Enums do domínio | `ElementType`, `MobTier`, `StatType`, `WeaponClass` |
| `component/` | Dados da arma no ItemStack (Data Component) | `WeaponData`, `ModDataComponents` |
| `attachment/` | Dados anexados a entidades | `PlayerStatsAttachment` (stats/XP/mana), `MobProfile` (tier/elemento do mob), `ModAttachments` |
| `item/` | Item de arma modular, tooltip, aba criativa | `ModularWeaponItem`, `ModItems` |
| `skill/` | Habilidades de tier e de classe + efeitos | `WeaponSkill`, `ClassAbility`, `SkillVfx`, `PlayerAnim` |
| `event/` | Combate e lógica de stats | `CombatEvents`, `ElementalCombat`, `StatsLogic`, `PlayerEvents` |
| `entity/` | Entidades próprias | `TierMob` (criaturas), `ThrownDagger` (adaga arremessada), `ModEntities` |
| `mob/` | IA e identidade dos mobs por tier (servidor) | `TierBrain` + `MinionBrain`/`EliteBrain`/`GuardianBrain`/`SovereignBrain`, `TierIdentity`, `Hazards`, `MobEvents`, `MobAnim` |
| `network/` | Pacotes cliente⇄servidor | `ModNetwork` (registro), `*Payload` |
| `command/` | Comando de testes `/modtest` | `ModTestCommand` |
| `client/` | Tudo que é só do cliente | `ClientSetup` (tecla K, renderers), `StatsScreen`, `SoloHud`, `DamageNumbers`, `MobAnimations`, `PlayerAnimations`, `BossCinematic` |
| `client/model/` | Modelos das criaturas | `TierMobModel` + um por tier; `MobMeshes` é **gerado** por script |
| `client/render/` | Renderers | `TierMobRenderer`, `ThrownDaggerRenderer` |

Outros diretórios:

- `src/main/resources/assets/solomod/` — `lang/` (`pt_br.json` e `en_us.json`), `models/item/`, `textures/entity/`, `textures/item/`.
- `src/main/templates/META-INF/neoforge.mods.toml` — metadados do mod; as variáveis `${...}` vêm de `gradle.properties`.
- `tools/` — scripts Python que **geram** arte e código (veja abaixo).

> **Arquivos gerados**: `client/model/MobMeshes.java`, as texturas de `textures/entity/` e as texturas/modelos elementais das armas saem dos scripts de `tools/`. Não edite à mão: altere o script e rode de novo, senão a próxima geração apaga sua mudança.

### Scripts de arte (`tools/`)

Rode sempre da raiz do projeto:

```bash
python tools/gen_mob_art.py        # mobs: texturas base + brilho e client/model/MobMeshes.java (usa mob_variants.py)
python tools/gen_weapon_models.py  # armas: modelos 3D (rapieira/claymore), versões elementais (usa gen_weapon_elements.py), ovos
```

## Comandos de teste (`/modtest`, exige OP nível 2)

| Comando | Efeito |
|---|---|
| `/modtest stats` | mostra stats, XP, pontos e mana |
| `/modtest set <for\|agi\|int\|vit> <valor>` | define um stat direto |
| `/modtest spend <stat> <qtd>` | gasta pontos disponíveis (fluxo "real") |
| `/modtest addxp <qtd>` | soma XP Global (1 ponto a cada 100) |
| `/modtest points <qtd>` | define os pontos disponíveis |
| `/modtest mana` | enche a mana |
| `/modtest reset` | zera tudo |
| `/modtest weapon <classe> <tier> [elemento] [requisito] [habilidade]` | cria a arma na mão principal |
| `/modtest mob <alvos> <tier> <elemento>` | transforma mobs vanilla em mobs do mod |
| `/modtest unmark <alvos>` | remove o perfil do mob |
| `/modtest spawn <tier> <elemento>` | invoca a criatura própria do tier |

## Interface (camada visual de testes)

- **Tecla K**: tela de Maestria. Aba *Maestria* distribui pontos (+1/+5) e mostra o efeito de cada stat; aba *Teste* tem os atalhos dos comandos (gerar arma por classe/tier/elemento, invocar ou marcar mob, ±10 em stats, +100 XP, encher mana, resetar). A aba Teste exige cheats/OP.
- **HUD** (canto superior esquerdo): nível, XP até o próximo ponto, barra de mana animada, aviso piscando de pontos livres, estado da arma na mão (ativa/adormecida) e recarga da habilidade.
- **Texturas**: 7 armas em pixel art 16x16; o gume/gema é tingido pela cor do elemento. Armas S. Chefe/Chefe brilham.
- **Animações**: números de dano flutuantes (cor do elemento, maiores com "!" na vantagem), som de crítico na vantagem, partículas por habilidade (rastro da Investida, ondas do Impacto Sísmico, feixe do Disparo de Mana, anéis da Explosão Elemental) e golpe do braço ao usar habilidade.

## Mobs por tier (identidade, ataques e IA)

Marcar um mob (`/modtest mob ...` ou aba Teste) agora dá a ele a identidade completa do tier: tamanho (atributo `scale`), atributos, título colorido, aura, animações próprias e um cérebro de combate que substitui a IA vanilla enquanto há alvo. Código em `mob/` (servidor) e `client/MobAnimations` (animações).

| Tier | Título | Tamanho / vida / dano | Ociosa | IA | Ataques (som) |
|---|---|---|---|---|---|
| NORMAL | • Lacaio | 0.85x / 1x / 1x | corre curvado, trote | bando: grita chamando aliados, fica mais rápido com 2+ aliados, foge com <30% de vida | **Bote**: agacha rosnando e salta mordendo (lobo/raposa) |
| OP | ★ Elite | 1.2x / 2.5x / 1.5x | balança os ombros | duelista: circula o alvo enquanto recarrega; **apara** 30% dos golpes de frente e contra-ataca | **Investida** com linha telegrafada (tridente) · **Golpe Triplo** em leque (varredura) |
| SUB_BOSS | ✦ Guardião | 1.6x / 6x / 2x, barra roxa | flutua, 3 motes orbitando | mago: recua se colam, aproxima se fogem, teleporta às costas de quem foge | **Rajada** de 3 orbes com mira preditiva · **Repulsão** · **Salto Esmagador** com área marcada · **Translocar** · **Invocação** de 2 Lacaios a 50% |
| BOSS | ♛ Soberano | 2.3x / 14x / 2.8x, barra dourada, brilho | respiração pesada | 3 fases (60%/25%) com rugido invulnerável; lê se o jogador luta de perto ou de longe e escolhe ataques contra isso; acelera por fase | **Golpe Sísmico** (onda no chão — pule) · **Chuva Elemental** (círculos que explodem) · **Investida** 1/2/3x (parede = atordoado) · **Vórtice** (fase 2+) · **Sopro** em cone · fase 2 convoca 2 Elites, fase 3 tem aura que queima |

Todo ataque especial é telegrafado (agachar/tremer + partículas na cor do elemento) e aplica a passiva do elemento do mob. Sons são vanilla com tom ajustado.

### Criaturas próprias

Cada tier tem uma criatura própria, e cada criatura tem uma forma **completamente diferente por elemento** (20 modelos e texturas no total). Elas nascem pelo botão **Invocar mob** da aba Teste, por `/modtest spawn <tier> <elemento>` ou pelos ovos da aba **Solo Mod** do criativo (um ovo por criatura e elemento). `/modtest mob` continua marcando mobs vanilla.

| | Sem elemento | Fogo | Água | Terra | Ar |
|---|---|---|---|---|---|
| **Lacaio** | Diabrete (gárgula de cabeça enorme e asas) | Diabrete Ígneo (crânio com coroa de chamas, jatos de fogo) | Diabrete Abissal (peixe-goblin com isca luminosa) | Diabrete de Pedra (pedregulho com cristais e seixos em órbita) | Sílfide Travessa (espírito-pássaro sem pernas, anel de vento) |
| **Elite** | Duelista Espectral (cavaleiro, espada e escudo) | Executor Infernal (cutelo em brasa, mangual em chamas) | Tritão Corsário (cauda de serpente, tridente, escudo de concha) | Sentinela de Granito (martelo de rocha, escudo-torre, chifres de galho) | Valquíria do Vento (asas de penas, glaive, elmo alado) |
| **Guardião** | Oráculo (manto, olho no peito, auréola) | Arauto Solar (sol com máscara e coroa de raios) | Medusa Abissal (água-viva gigante com tentáculos-fita) | Monólito Ancestral (obelisco rúnico com pedregulhos) | Olho da Tempestade (nuvem com raios, chuva e anéis de vento) |
| **Soberano** | Colosso Rei (gigante de pedra coroado) | Rei Vulcânico (tronco-vulcão com pluma de lava) | Leviatã Coroado (rei-kraken com pinças de caranguejo) | Titã Ancestral (árvore colossal com copa e frutos de âmbar) | Imperador dos Céus (armadura prateada, asas imensas, trono de nuvem) |

Olhos, gemas, chamas e rachaduras ficam numa camada emissiva que pulsa na cor do elemento; partes decorativas (chamas, nadadeiras, anéis, asas) se mexem sozinhas. Ao morrer, o Soberano se **desintegra** por 5 s e os jogadores por perto entram numa **cinemática**: faixas pretas, HUD escondido, câmera virando para o chefe e o letreiro "SOBERANO DERROTADO".

**Fúria (Guardião e Soberano)**: ao chegar a 1/4 da vida o chefe ruge (invulnerável), arremessa quem está perto e chama Lacaios. Por 20 s fica com aura vermelha, +35% de dano, -35% de dano recebido e um conjunto de ataques totalmente diferente:
- Guardião: Tempestade de Orbes, Olhar Fulminante (raio girando 360°) e Translocação Frenética; para de fugir e parte para cima.
- Soberano: Marcha Sísmica, Meteoro Colossal, Investida Sem Fim (ricocheteia nas paredes) e Anel de Erupções. A Fúria também é a fase 3.

Quando a Fúria acaba, o chefe fica exausto por 3 s e recebe +25% de dano. O Soberano também **convoca 3 Lacaios** do seu elemento como ataque normal (até 6 vivos por perto).

A arte é gerada por `python tools/gen_mob_art.py` (formas base) + `tools/mob_variants.py` (variantes por elemento), que escrevem as texturas e `client/model/MobMeshes.java`; para mudar um modelo, edite os scripts e rode de novo.

## Habilidades das armas

| Arma | Habilidade | Como usar |
|---|---|---|
| Rapieira (modelo 3D alongado) | Estocada Relâmpago: dispara para a frente perfurando quem estiver no caminho | clique direito |
| Adaga | Arremesso: crava no chão ou no alvo; na mão fica um contorno fantasma. Clique de novo e ela volta girando, ferindo quem estiver no caminho (passar por cima também recolhe) | clique direito |
| Espada longa (modelo 3D de claymore) | Giro da Claymore: todo golpe vira um giro de 360° que acerta todos em volta | ataque |
| Machado | Salto Esmagador: salta, mergulha e explode o chão num CRÍTICO em área, com a câmera tremendo | clique direito |

Essas habilidades valem em qualquer tier e não gastam mana. A habilidade de tier (Investida, Impacto, Explosão...) dessas armas passa para **agachar + clique direito**.

Cada arma também tem uma **textura própria por elemento**: Fogo (lâmina em brasa com línguas de fogo), Água (ondas, espuma e gotas), Terra (pedra serrilhada, musgo e cristais) e Ar (prata, penas na guarda e rastros de vento); a rapieira e a claymore 3D ganham paleta e peças extras por elemento. Tudo é gerado por `python tools/gen_weapon_models.py`.

Todas as armas prontas (cada classe em cada tier e elemento) e os ovos ficam na aba **Solo Mod** do inventário criativo.

## Roteiro de teste rápido (com interface)

1. Crie um mundo Criativo com cheats ligados e aperte **K** → aba **Teste**.
2. Seletores: Classe *Adaga*, Tier *S. Chefe*, Elemento *Fogo* → **Gerar arma na mão**. Note no HUD: "Adormecida (requer 30 AGI)".
3. Troque o Elemento para *Ar* → **Invocar mob**. Bata nele: números brancos, sem elemento.
4. **+10 AGI** três vezes (e +10 INT uma vez). HUD fica "Fogo · Ativa"; bata de novo: números laranja com "!" e alvo pegando fogo.
5. Clique direito: Investida com rastro de partículas; a barra roxa de recarga esvazia e enche.
6. **+100 XP** e volte à aba **Maestria** para gastar o ponto.

## Roteiro de teste rápido (comandos)

```
/modtest weapon dagger sub_boss fire          → adaga de S. Chefe, Fogo, requer 30 AGI, habilidade Investida
(passe o mouse: requisito em vermelho, elemento/habilidade "Adormecidos")
/summon zombie ~ ~ ~3
/modtest mob @e[type=zombie,limit=1,sort=nearest] op air   → zumbi Elite de Ar
(bata nele: dano básico, sem elemento)
/modtest set agi 30
/modtest set int 10                           → INT mínima da Investida é 5
(tooltip fica verde; bater no zumbi de Ar dá 1.5x + queimadura; clique direito = Investida)
/modtest weapon greatsword boss water         → espadão de Chefe, requer 50 FOR: sem FOR fica 40% mais lento
/modtest stats
```

## Regras implementadas (resumo)

- **Elemento**: Água > Fogo > Ar > Terra > Água. Vantagem = 1.5x + passiva; desvantagem ou mesmo elemento = 0.5x; pares opostos (Água×Ar, Fogo×Terra) = 1.0x.
- **Tier**: NORMAL (sem elemento/habilidade) · OP (1.25x, +3 no stat, passiva leve) · SUB_BOSS (1.5x, +5, elemento **puro** que nunca cai abaixo de 1.0x, habilidade menor) · BOSS (2x, +10, passiva máxima, Explosão Elemental).
- **Travamento**: sem o requisito, o golpe é básico (só multiplicador do tier); elemento, escalonamento (+2% por ponto no stat) e habilidade ficam adormecidos; armas Pesadas/Médias perdem 40% da velocidade de ataque.
- **Habilidade** (clique direito): requer Maestria + INT mínima + mana; mana máx. = 20 + 5×INT; regenera 1 + INT/10 por segundo.
- **XP Global**: só mobs com perfil (`/modtest mob`) dão XP (10/40/150/500 por tier); cada 100 XP = 1 ponto (`/modtest spend <stat> <qtd>`).
- **Stats → atributos**: FOR +0.25 de dano; AGI +1% vel. de ataque e +0.5% de movimento; VIT +1 de vida e −1% de dano recebido (máx. 50%).
