# 🎮 Gameplay — Regras e mecânicas

Referência completa do que o Solo Mod faz dentro do jogo. Para saber **onde** cada coisa está no código, veja [ARCHITECTURE.md](ARCHITECTURE.md).

- [Maestria (stats)](#-maestria-stats)
- [Elementos](#-elementos)
- [Tiers](#-tiers)
- [Armas](#️-armas)
- [Mobs por tier](#-mobs-por-tier)
- [Criaturas próprias](#-criaturas-próprias)
- [O Soberano (chefe final)](#-o-soberano-chefe-final)
- [Conquistas](#-conquistas)
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

O catálogo atual tem **4 classes de arma**. Cada uma tem um item próprio (`solomod:<classe>`), e o tier, elemento, requisito e habilidade de cada exemplar ficam gravados no item. A aba criativa **Solo Mod: Armas** contém exatamente uma rapieira, uma espada longa, uma adaga e um martelo, neutros e de tier Normal. A aba Teste e o autocomplete de `/modtest weapon` também oferecem só essas quatro classes; tiers e elementos continuam configuráveis ali.

| Classe | Peso | Stat | Habilidade de tier padrão | Habilidade de classe |
|---|---|---|---|---|
| Espada longa (`longsword`) | Média | FOR | Investida | **Meia-Lua / Ascendente / Cruzado** (R alterna, clique direito usa) |
| Adaga (`dagger`) | Leve | AGI | Investida | **Arremesso** (clique direito) |
| Rapieira (`rapier`) | Leve | AGI | Investida | **Estocada Relâmpago** (clique direito) |
| Martelo (`hammer`) | Pesada | FOR | Impacto Sísmico | **Salto Esmagador** (clique direito) |

Velocidades base, antes de AGI e penalidades de requisito: **adaga 2,5 ataques/s**, **rapieira 2,2**, **espada longa 1,6** (igual às espadas vanilla) e **martelo 0,8**. O martelo tem +9 de dano base. Espadão, machado, kunai e varinha permanecem registrados para compatibilidade com mundos salvos e podem ser obtidos por comando, mas saíram do catálogo visível.

### Habilidades de tier (gastam mana)

| Habilidade | Mana | Recarga | INT mínima |
|---|---|---|---|
| Investida (`dash_strike`) | 15 | 3 s | 5 |
| Impacto Sísmico (`ground_slam`) | 25 | 5 s | 10 |
| Disparo de Mana (`mana_bolt`) | 10 | 1,5 s | 5 |
| Explosão Elemental (`elemental_burst`) | 40 | 10 s | 20 |

Exigem Maestria + INT mínima + mana. A potência escala com o tier (habilidade menor = 1x, suprema = 2x). Nas armas com habilidade de classe, a de tier passa para **agachar + clique direito**. Com a adaga arremessada, essa combinação escolhe o teleporte: recolha a adaga para voltar a usar a habilidade de tier.

### Habilidades de classe (sem mana, qualquer tier)

| Arma | Habilidade |
|---|---|
| Rapieira | **Estocada Relâmpago carregada**: segure botão direito para escolher a distância e solte para avançar perfurando; a carga varia de aproximadamente **3 a 14 blocos**, atingindo o máximo em **1,5 s**. HUD mostra distância e barra; paredes interrompem o dash. Linhas 3D brancas/ciano seguem o trajeto real, com faixas de velocidade, braço em estocada e recuperação rápida da arma. Recarga de 1,5 s após soltar. |
| Adaga | **Arremesso**: na mão fica um fantasma. **Botão direito** chama de volta, girando e ferindo no caminho; **Shift + botão direito** teleporta até a lâmina e a recolhe. Arremesso/retorno têm rastro elétrico dourado e poses do braço; o teleporte tem raios segmentados, clarão e selo angular nas duas pontas, sem gerar um raio real nem fogo. Funciona durante o voo ou cravada. O servidor verifica dono, mesma dimensão, chunk carregado, destino livre e alcance de **64 blocos**; junto de paredes procura espaço próximo, considerando a altura do jogador em pé. Em local bloqueado mantém a adaga para poder chamá-la. O teleporte tem recarga de 1,5 s; passar por cima também recolhe. |
| Espada longa | **Corte em Meia-Lua**: botão direito prepara por **6 ticks**, com a arma nos ombros e mão de apoio, e corta um semicírculo horizontal de **180° à frente**, alcançando **4,5 blocos**, com dano de 1,2× e empurrão. Uma **lâmina de luz volumétrica** revela o arco do golpe, com halo elemental, fio branco e dissipação na recuperação; acertos produzem clarão, faíscas, som pesado e tremor leve. Braços/mangas acompanham o corte em terceira pessoa; a arma tem preparação, varredura ampla e recuperação em primeira pessoa. A sequência dura **18 ticks**. Cada inimigo recebe um acerto; alvos atrás ou através de paredes não entram. Recarga de **1,2 s**. Clique esquerdo é ataque vanilla, inclusive para rebater bolas de fogo. |
| Martelo | **Salto Esmagador**: botão direito no chão salta e mergulha; **no ar mergulha imediatamente**. O servidor registra a maior altura e considera a distância já caída ao ativar: multiplicador **2 + 0,12 × altura**, com teto de **24 blocos** (máximo **4,88×**). O raio começa em **4,5** e aumenta **0,08 por bloco** (máximo **6,42**). Poses de arma erguida até pousar, rastro de queda, poeira, anéis 3D, ondas elementais e tremor. Até **36 + 2 × altura** blocos naturais expostos saltam e voltam ao pousar, respeitando `mobGriefing`; com a regra desligada ficam os efeitos visuais. Recarga de **3,5 s** e proteção de queda durante a habilidade. Água/lava, voo criativo e elytra bloqueiam o início; limite de duração de 12 s. |

Com **espada longa, rapieira ou martelo** equipado, **R** alterna a técnica selecionada (tecla configurável em Controles). A seleção aparece na faixa compacta da HUD, no tooltip e no painel K; **botão direito** executa. A seleção é independente para cada classe. Todas as técnicas de uma arma compartilham a recarga do item, sem reiniciá-la ao trocar. A seleção vale para a sessão; ao reconectar, volta à Meia-Lua, Estocada Relâmpago e Salto Esmagador. R durante uma habilidade, carga, rolamento ou menu não troca a seleção.

| Técnica da rapieira | Mecânica | Sequência / recarga |
|---|---|---|
| **Estocada Relâmpago** | Dash original: segure/solte botão direito para carregar 3–14 blocos | Dash variável / 30 ticks |
| **Chuva de Agulhas** | Três estocadas em cone estreito frontal, alcance 4 blocos, 0,55× por acerto nos ticks 4/14/24; desacelera o deslocamento durante a técnica | 32 / 48 ticks |
| **Passo do Vento** | Estocada de 1× no tick 4, alcance 4 blocos, seguida de recuo de até 3,2 blocos em 4 ticks. Colisões bloqueiam o recuo; não concede invulnerabilidade | 18 / 38 ticks |

As duas técnicas novas usam clique simples. Braços em pose de esgrima, agulhas de luz branca/ciano em 3D e rastros de velocidade acompanham os acertos/recuo. Os intervalos de 10 ticks da Chuva de Agulhas respeitam a invulnerabilidade vanilla, sem removê-la dos inimigos.

| Técnica do martelo | Mecânica | Sequência / recarga |
|---|---|---|
| **Salto Esmagador** | Salto/mergulho original, inclusive ativação no ar e bônus de altura limitado a 24 blocos | Até pousar / 70 ticks |
| **Giro Demolidor** | Preparação de 10 ticks e golpe circular de 1,6×, raio 4,4 blocos, empurrão forte e rastro dourado volumétrico | 28 / 54 ticks |
| **Ruptura Sísmica** | Preparação de 10 ticks e onda frontal de até 8 blocos, avançando 0,7 bloco/tick; 1,8× uma vez por alvo, com impulso vertical | 30 / 64 ticks |

Giro e Ruptura iniciam no chão e desaceleram o movimento. A Ruptura mede o terreno no servidor, para em paredes, líquidos, bordas e chunks não carregados, e exige piso aproximadamente plano (desnível máximo de 0,6 bloco). Os fragmentos 3D da onda são visuais e não removem o piso; os blocos físicos do Salto Esmagador continuam obedecendo `mobGriefing`. As duas técnicas têm poses de duas mãos, preparação/recuperação e efeitos próprios; alvos através de paredes não são atingidos.

| Técnica da espada longa | Acerto | Sequência / recarga |
|---|---|---|
| **Meia-Lua** | Semicírculo frontal, 4,5 blocos, 1,2× de dano e empurrão | 18 ticks / 24 ticks |
| **Ascendente** | Corte vertical de luz, alcance frontal de 5 blocos, 1,5× de dano e impulso para cima nos alvos | 22 ticks / 34 ticks |
| **Cruzado** | Dois cortes diagonais formando um X, alcance frontal de 4,5 blocos, 0,8× por corte; acertos nos ticks 6 e 16 | 30 ticks / 42 ticks |

As três técnicas preparam por 6 ticks, têm lâminas de luz volumétricas segmentadas e poses de duas mãos, com fio branco, halo elemental, faíscas, clarão e recuperação. A seleção e os acertos são validados no servidor; alvos atrás ou através de paredes não entram. O clique esquerdo mantém o ataque vanilla.

As poses de arma são visíveis em primeira pessoa e os movimentos do corpo em terceira pessoa. Os valores ficam em `StatsLogic.WeaponAbilities`; carga, dano, posse da adaga e destino são decididos pelo servidor. Dash, salto e meia-lua são cancelados ao trocar de arma/dimensão, morrer ou desconectar.

### Rolamento de esquiva

Pressione **o mesmo Ctrl duas vezes em até 280 ms** (esquerdo ou direito). Segurar a tecla não repete o gesto. **WASD escolhe a direção** relativa à visão, incluindo diagonais; sem direção, rola para a frente. Um Ctrl isolado continua com seu comportamento normal de corrida.

O rolamento começa somente no chão, fora de água/lava, sem voo, veículo, item em uso ou outra habilidade em andamento. Tem **8 ticks de impulso e 4 de recuperação**, com cambalhota na direção real do movimento em terceira pessoa e mãos recolhidas/balanço curto da câmera em primeira pessoa. A hitbox não encolhe e paredes bloqueiam o avanço. O impulso perde força no final e deixa poeira do piso.

Nos **ticks 2–7**, esquiva de golpes de combate, projéteis e explosões. **Queda, void, ambiente e dano que ignora invulnerabilidade não são anulados**. Não permite atacar ou ativar armas durante a rolagem. A recarga total é de **26 ticks (1,3 s)** desde o início; não gasta mana, mas custa 0,4 de exaustão de comida. Movimento, condições e janela de esquiva são controlados pelo servidor; constantes em `StatsLogic.Dodge`.

### Visual das armas

- Texturas em pixel art. Cada arma tem **textura própria por elemento**: Fogo (lâmina em brasa), Água (ondas e gotas), Terra (pedra, musgo e cristais) e Ar (prata, penas e vento).
- As quatro armas do catálogo têm **modelo 3D de cubos** com textura pixel art 16×16: rapieira com guarda dourada e arco de mão, espada longa com cabo de duas mãos, adaga com lâmina escalonada e martelo volumétrico com runa, aros dourados e molduras de aço salientes. O martelo usa iluminação lateral no inventário e poses próprias de mão para mostrar a face e a profundidade da cabeça. As variantes elementais têm paleta própria; rapieira e espada longa também ganham peças extras. O fantasma da adaga arremessada acompanha seu modelo 3D.
- Armas de S. Chefe e Chefe brilham.
- A aba **Solo Mod** contém os ovos; a aba **Solo Mod: Armas** mostra só os quatro exemplares, sem duplicatas. Para variantes, use **K → Teste** ou `/modtest weapon`.

## 👹 Mobs por tier

Qualquer mob vira "mob do mod" ao receber um **perfil** (tier + elemento). O perfil dá tamanho, atributos, título colorido, aura, animações e um **cérebro de combate** que substitui a IA vanilla enquanto o mob tem alvo.

| Tier | Título | Tamanho / vida / dano | Ociosa | IA | Ataques |
|---|---|---|---|---|---|
| NORMAL | • Lacaio | 0.85x / 1x / 1x | corre curvado, trote | **bando**: grita chamando aliados, fica mais rápido com 2+ aliados, foge com <30% de vida | **Bote** |
| OP | ★ Elite | 1.2x / 2.5x / 1.5x | balança os ombros | **duelista**: circula o alvo enquanto recarrega; **apara** 30% dos golpes de frente e contra-ataca | **Investida** telegrafada · **Golpe Triplo** em leque |
| SUB_BOSS | ✦ Guardião | 1.6x / 6x / 2x, barra roxa | flutua, 3 motes orbitando | **mago**: recua se colam, aproxima se fogem, teleporta às costas de quem foge | **Rajada** de 3 orbes com mira preditiva · **Repulsão** · **Salto Esmagador** · **Translocar** · **Invocação** a 50% |
| BOSS | ♛ Monarca | 2.3x / 14x / 2.8x, barra dourada, brilho | respiração pesada | **3 fases** (60% / 25%) com rugido invulnerável; lê se o jogador luta de perto ou de longe e escolhe ataques contra isso | **Golpe Sísmico** (pule) · **Chuva Elemental** · **Investida** 1/2/3x (parede = atordoado) · **Vórtice** · **Sopro** em cone · convoca Elites e Lacaios |

Todo ataque especial é **telegrafado** (agachar/tremer + partículas na cor do elemento) e aplica a passiva do elemento do mob.

### 😡 Fúria (Guardião e Monarca)

Com 1/4 da vida, o chefe ruge (invulnerável), arremessa quem está perto e chama Lacaios. Durante **20 s** ele fica com aura vermelha, causa +35% de dano, recebe −35% de dano e usa ataques **totalmente diferentes**:

- **Guardião**: Tempestade de Orbes, Olhar Fulminante (raio girando 360°) e Translocação Frenética. Para de fugir e parte para cima.
- **Monarca**: Marcha Sísmica, Meteoro Colossal, Investida Sem Fim (ricocheteia nas paredes) e Anel de Erupções. A Fúria é a fase 3.

Quando a Fúria acaba, o chefe fica **exausto por 3 s** e recebe +25% de dano.

**Regras de todos os chefes (Guardiões, Monarcas e Soberano)**: durante a Fúria, nenhum stun pode ser aplicado (inclui bola devolvida e colisão da investida). A única exceção é o **cansaço planejado do Soberano** depois do raio do peito (ele ajoelha sozinho, ver [O Soberano](#-o-soberano-chefe-final)). Eles emitem um **grito** ao entrar na Fúria e repetem o grito a cada 5 s. Fora dela, o stun usa uma pose **agachada, de cabeça baixa e ofegante**, como recuperação, também usada na exaustão.

Quando um chefe mata um jogador, para por **4 s para rir**, jogando a cabeça para trás, abrindo a mandíbula e sacudindo os ombros, olhando para o local da derrota. Há som de risada; funciona mesmo sem outro alvo vivo. Se estiver numa transição invulnerável, termina a transição antes de rir. A risada não pausa o relógio da Fúria. Dano direto, projéteis e mortes atribuídas ao chefe por queimadura/ambiente também contam; o magma da investida registra o dono durante a sessão (até os últimos 4096 blocos por dimensão). Quando é o **Soberano** que ri, há também uma **cinemática curta** no rosto dele ("O SOBERANO RI DE VOCÊ") para quem está por perto e para o jogador que caiu.

### 💀 Morte do Monarca

O Monarca se **desintegra** por 5 s. Os jogadores por perto entram numa **cinemática**: faixas pretas, HUD escondido, câmera virando para o chefe e o letreiro "MONARCA DERROTADO". Quem causou dano a ele ganha a conquista da forma derrotada (ver [Conquistas](#-conquistas)).

### Poderes dos cinco Monarcas

Os poderes que o Soberano representa nos estágios 1–5 também pertencem aos respectivos reis. Estão disponíveis desde a fase 1 e continuam na Fúria, junto dos ataques que eles já tinham; apenas um ataque do cérebro executa por vez. Projéteis e áreas já lançados continuam ativos.

| Monarca | Poderes característicos |
|---|---|
| Colosso Rei (neutro) | Punho do Colosso: seu Golpe Sísmico agora empurra quem está perto e sacode a câmera; também pode usá-lo na Fúria. |
| Rei Vulcânico (fogo) | Genki-dama: carrega por 3,5 s acima das mãos e lança uma bola rebatível, com explosão de raio 4. Mantém as Bolas de Fogo Reais. |
| Leviatã Coroado (água) | Maremoto: faixa de 7×24 blocos, telegrafada por 1 s, com crista de esferas 3D que arrasta as vítimas. Esferas de Água: cinco orbitam o rei por 2,5 s e depois perseguem os jogadores; um golpe direto as estoura. |
| Titã Ancestral (terra) | Raízes Ancestrais: duas levas sob as posições atuais dos jogadores, com círculos de aviso. Pisão: terremoto de raio 10; Pulão: salta até perto do alvo e causa terremoto de raio 14 ao pousar. Ambos arremessam e levantam blocos respeitando `mobGriefing`. O Titã não sofre dano de queda. |
| Imperador dos Céus (ar) | Conjura quatro tornados 3D após 2 s de preparação, distribuídos entre os jogadores; perseguem e lançam para o alto. |

Alcance, recarga e multiplicadores dessas versões ficam em `TierIdentity.MonarchPowers`. O Soberano conserva suas versões gigantes e o Raio do Peito exclusivo.

### 🔥 Bolas de Fogo Reais do Rei Vulcânico

Desde a **fase 1**, o Monarca do Fogo lança **Bolas de Fogo Reais**: prepara por 1 s e dispara 1/2/3 bolas conforme a fase, com 0,9 s entre disparos, a alvos entre 6 e 32 blocos com linha de visão. O ataque continua disponível na Fúria. O modelo 3D próprio tem **2,4 blocos**, no estilo do Minecraft (só cubos): um núcleo de magma incandescente dentro de uma casca de basalto com lava, dois anéis quadrados de chama e seis blocos de magma em órbita; não usa a bola de fogo do Ghast nem o item fire charge. Elas voam em linha reta, sem acelerar, a **7 blocos/s** (0,35 bloco/tick), com espiral de chamas, fumaça e faíscas. Explodem ao tocar um alvo ou bloco, sem destruir o terreno; desaparecem após 10 s ou se o conjurador morrer/sair do mundo.

O jogador pode **rebater com um ataque direto**, inclusive de mão vazia ou espada longa: mire na bola e golpeie. Ela passa a seguir a direção da mira a **9 blocos/s**. Se acertar o conjurador fora da Fúria, causa o dano da bola atribuído ao jogador e o deixa **agachado se recuperando por 5 s (100 ticks)**, sem andar, atacar ou causar dano pela aura. Uma nova bola devolvida renova esse tempo. Durante a Fúria, a bola ainda causa dano, mas **não causa stun**. Ataques já lançados continuam no mundo. Rugidos e agonia invulneráveis bloqueiam o contra-ataque. Trocas de fase/estágio aguardam o fim da recuperação, preservando os limites de vida do Soberano.

**Investida de Fogo**: nas investidas normais e nas cinco investidas da Fúria, partes do piso natural exposto (terra, pedra, areia ou cascalho) viram **blocos reais e permanentes de magma**, com partículas de lava. A conversão ocorre num raio de 2 blocos, por amostragem de 28% a cada 2 ticks; preserva blocos processados e recipientes e respeita `mobGriefing=false`. Materiais naturais colocados pelo jogador também podem ser convertidos. Na investida normal, bater em parede causa 2 s de recuperação agachada fora da Fúria; na Fúria continua ricocheteando.

## 🐉 Criaturas próprias

Cada tier tem uma criatura com modelo próprio, e cada criatura tem uma forma **completamente diferente por elemento**: são 20 modelos e texturas. Elas nascem pelo botão **Invocar mob** da aba Teste, por `/modtest spawn <tier> <elemento>` ou pelos ovos da aba **Solo Mod** do criativo.

| | Sem elemento | 🔥 Fogo | 💧 Água | 🪨 Terra | 🌪️ Ar |
|---|---|---|---|---|---|
| **Lacaio** | Diabrete | Diabrete Ígneo | Diabrete Abissal | Diabrete de Pedra | Sílfide Travessa |
| **Elite** | Duelista Espectral | Executor Infernal | Tritão Corsário | Sentinela de Granito | Valquíria do Vento |
| **Guardião** | Oráculo | Arauto Solar | Medusa Abissal | Monólito Ancestral | Olho da Tempestade |
| **Monarca** | Colosso Rei | Rei Vulcânico | Leviatã Coroado | Titã Ancestral | Imperador dos Céus |

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

## 👑 O Soberano (chefe final)

**História**: há eras, os cinco Monarcas se uniram para derrotar o Soberano. Ele voltou com o poder de todos eles e é o chefe final desta versão de testes. No futuro ele vai morar numa dimensão própria; por enquanto nasce pelo **ovo** da aba Solo Mod ou por `/modtest sovereign [estágio]`.

**Corpo**: gigante (escala de renderização 7: cerca de 31 blocos de altura, 36 com os braços erguidos), com um pedaço de cada Monarca: braço de pedra e chifres do Colosso Rei, perna de magma e vulcão no ombro do Rei Vulcânico, pinça e barba de tentáculos do Leviatã Coroado, perna de raízes e copa do Titã Ancestral, asas e auréola do Imperador dos Céus. A coroa tem uma ponta de cada Monarca e cinco fragmentos orbitam o corpo. **Sem brilho**: não tem contorno luminoso atravessando paredes nem camada emissiva (só a aura vermelha de Fúria no estágio 6). Vida 700, dano 11 por golpe.

**Hitbox**: 8 blocos de largura por 24 de altura, **menor que o modelo** (18 blocos de largura, 36 com as asas): braços, asas e coroa não recebem golpes. O limite é do Minecraft, não uma escolha: a busca de entidades só olha seções a 2 blocos da região consultada, então caixas bem mais largas que isso fazem o jogador errar golpes e flechas em certos ângulos. Os olhos ficam a 22 blocos do chão.

**Imunidades**: fogo, lava e queimadura (inclusive as bolas de fogo dele: devolvidas, só o atordoam) e sufocamento em paredes (a caixa gigante atravessa tetos e morros).

**Movimento**: anda em linha reta até o alvo (sem o pathfinding do Minecraft, que com essa caixa seria muito caro e quase nunca acharia caminho). Feito para arena aberta; a futura dimensão dele deve ser plana e espaçosa.

**Estágios**: a barra de chefe tem 6 marcas. Cada vez que o jogador termina uma, o Soberano **agoniza** por 4 s: fica invulnerável e parado, ajoelha se contorcendo, e todos por perto ganham um **close cinemático** no rosto dele com a fala do estágio. No fim da agonia ele explode numa onda que empurra todos e desperta o poder de mais um Monarca, que vira um ataque novo. Nenhum golpe pula estágio: a vida para no limite da faixa, mesmo num golpe fatal (só `/kill` ignora). O estágio sai da **vida atual**: se o chefe for recarregado (o jogador morreu e reapareceu longe, por exemplo), ele volta no estágio certo e **não repete as cinemáticas** dos anteriores.

| Estágio | Vida | Elemento | Ataques (só estes neste estágio) |
|---|---|---|---|
| 1 | 100–83% | — | **Punho do Colosso**: ergue o punho e esmaga; onda de choque no chão (duas a partir do estágio 4) — pule |
| 2 | 83–67% | Rei Vulcânico | **Genki-dama**: une os braços acima da cabeça, carrega uma esfera de fogo por 3,5 s (cresce e se vê de longe) e a joga **para baixo, sobre o jogador focado** a 18 blocos/s, mirando onde ele vai estar; explode em área (raio 5). Dá para rebater a bola: devolvida ao Soberano, o atordoa por 5 s (ele é imune a fogo, então sem dano), **inclusive na Fúria** |
| 3 | 67–50% | Leviatã Coroado | **Maremoto**: marca uma faixa reta no chão e lança uma onda (uma **crista de 5 esferas de água 3D** rolando) que arrasta quem estiver nela · **Esferas de Água**: ergue os braços e 5 esferas de água 3D (em cubos: um cubo dentro de outro dentro de outro, no estilo do cristal do End) giram em volta dele; 2,5 s depois cada uma **persegue um jogador** (curvando e acelerando, até 11 blocos/s) e explode em respingo (raio 3, lentidão). Dá para desviar de lado ou **estourar a esfera com um golpe direto** |
| 4 | 50–33% | Titã Ancestral | **Pulão**: agacha e salta (só o bastante para sair do chão, ~3 blocos) **até perto do jogador focado**, caindo num **terremoto de 22 blocos** (o pisão, mais forte): arremessa todos para longe, a tela treme forte e até 260 blocos do chão saltam; só a certa distância (colado, ele pisa) · **Raízes Ancestrais**: duas levas de raízes sob cada jogador (a segunda onde ele estiver) · **Pisão** (de perto): ergue a perna e esmaga o chão. **Terremoto de 14 blocos**: a tela de todos treme, quem está perto é **arremessado para longe** (até 2 blocos/tick nos pés dele, menos na borda), o anel de choque causa dano (pule) e os **blocos do chão por onde o anel passa saltam e voltam ao lugar** (terra, pedra, areia e cascalho expostos; até 180 blocos; respeita `mobGriefing`) |
| 5 | 33–17% | Imperador dos Céus | **Tornados**: flutua de braços abertos e chama **4 tornados 3D** (no estilo do Minecraft: pilha de 14 anéis quadrados girando em degraus, com blocos de terra subindo) que perseguem os jogadores (mais lentos que quem anda) e jogam para o alto quem encosta; o dano vem da **queda** (~11 blocos, uns 8 de dano) |
| 6 | 17–0% | todos + só dele | **todos os poderes acima, um por vez** + **Raio do Peito** (prioridade) · **FÚRIA até morrer** |

**Regra dos poderes**: cada estágio de 1 a 5 representa um elemento e **só usa os poderes dele, nada se acumula** (no estágio 3 não há punho nem genki-dama; no 4, nem maremoto). **Só o estágio 6 libera todos os poderes**, **um por vez, nunca juntos**, com **prioridade para o Raio do Peito**, que é só dele: sempre que o raio está pronto, vem antes de qualquer outro. A genki-dama em voo some se ele sair do estágio de fogo e não for o 6. O raio carrega 1,5 s (energia converge no peito) e dispara 6 s um raio que desce até o jogador focado; o ponto de mira persegue o alvo a 4 blocos/s (**quem corre escapa, quem anda não**) e troca de jogador a cada 2 s. Contato com o raio causa dano a cada 0,2 s. Depois o Soberano **cai ajoelhado, cansado, por 4 s**, parado e com **+40% de dano recebido**. Na Fúria: +35% de dano, −35% de dano recebido (fora do ajoelhar), gritos e nenhum stun, **exceto o ajoelhar do raio e o da bola de fogo devolvida**.

**Corrida Frenética** (só na Fúria): se o jogador focado vem **correndo** (mais de 5 blocos/s) há 1,5 s, o Soberano o persegue numa **corrida exagerada** a ~10 blocos/s (mais rápida que um sprint), com o corpo todo inclinado, quicando e braços e pernas batendo em exagero; cada pegada treme a tela. Ao alcançá-lo, desfere um golpe pesado que o joga longe. Desiste se não alcança em 4,5 s ou empaca no terreno; recarga de 18 s. Tem prioridade sobre o sorteio (só o pisão e o voo curto passam na frente).

**Voos curtos** (movimento, não poder: o de avanço vale em qualquer estágio; o recuo, só no 6): o Soberano às vezes **voa uns segundos** em vez de andar. Na Fúria, se o jogador está perto e o raio vai ficar pronto, ele **recua uns 18 blocos de costas** para ter espaço (recarga de 22 s, nem sempre acontece). Em qualquer estágio, se o alvo fica a mais de 22 blocos por 5 s ou ele está **preso no terreno** por 2 s, ele **voa até perto** (recarga de 12 s). O pouso treme a tela e solta um anel de choque pequeno. Ele não leva dano de queda.

**IA**: o Soberano escolhe por **prioridade**, não só por sorteio. 1) Jogador **colado nos pés** (menos de 9 blocos): **pisão** (estágios 4 e 6; joga o jogador para longe) ou **punho** (estágios 1 e 6; também empurra). Nos estágios 2, 3 e 5 ele só tem os poderes do próprio elemento. 2) **Voo curto** quando as condições acima valem. 3) **Raio do Peito** sempre que pronto. 4) O resto por sorteio ponderado: **quem luta de perto** atrai ataques em área e **quem luta de longe** (flechas, magia) atrai ataques de longe, com a leitura "esquecendo" aos poucos; logo depois do pisão os ataques de longe pesam o dobro (o jogador foi lançado para longe). 5) **Mira à frente**: genki-dama e maremoto apontam para onde o jogador estará. 6) Em grupo, os padrões abaixo.

**Vários jogadores** (padrões do Soberano): 1) **foco rotativo**: cada ataque escolhe quem está há mais tempo sem ser alvo, marca o jogador com chamas na cabeça e o avisa na tela ("O Soberano escolheu você!"); 2) **grupo junto** (todos a menos de 8 blocos) atrai ataques em área (Punho, Pisão); 3) **grupo espalhado** (alguém a mais de 24 blocos) atrai ataques de longe (genki-dama, Maremoto) e o foco vai para o mais afastado: nenhum canto é seguro; 4) o dano recebido cai **20% por jogador extra** (até 4 extras); 5) tornados e raízes miram todos (os 4 tornados se dividem entre os jogadores). Sozinho, nada disso muda: o único jogador é sempre o foco.

**Morte (bugando)**: o Soberano **não vira cinzas como os Monarcas: ele buga**. Por 5 s o modelo trava em quadros (congela a cada 2 ticks), as peças saltam de lugar, giram e mudam de escala, ele pisca (some quase por completo em alguns quadros) e as cores se separam em vermelho e ciano; faíscas elétricas e estática roxa saem do corpo, com bipes de 8 bits picotados e teleportes. No fim some de vez num estouro.

**Fim**: o Soberano morre bugando, quem estava perto ou lutou vê a cinemática "O SOBERANO CAIU" e, em seguida, os **créditos** (música de créditos do Minecraft; ESPAÇO acelera, ESC sai e volta ao mundo).

## 🏆 Conquistas

O mod adiciona uma aba própria **Solo Mod** na tela de conquistas (Advancements) do Minecraft (tecla **L**), com uma conquista narrativa para a derrota de cada forma do Monarca e duas **secretas** (só aparecem quando ganhas):

| Conquista | Chefe | Elemento | Natureza e contexto narrativo | Tipo / Recompensa |
|---|---|---|---|---|
| **A Queda da Coroa Pétrea** | Colosso Rei | Neutro | Monarca primordial de rocha pura e força bruta indestrutível que desafia todas as forças elementais. | Desafio · 150 XP |
| **Cinzas do Vulcão** | Rei Vulcânico | Fogo | Monarca do magma em tronco-vulcão vivo que expele erupções e transforma terras em caldeiras ardentes. | Desafio · 150 XP |
| **Calmaria no Abismo** | Leviatã Coroado | Água | Rei-kraken com pinças titânicas que comanda maremotos devastadores e os abismos oceânicos. | Desafio · 150 XP |
| **Raízes Milenares Desfeitas** | Titã Ancestral | Terra | Árvore colossal com copa impenetrável e coração de âmbar cujas raízes sustentavam o peso do mundo. | Desafio · 150 XP |
| **O Trono Despedaçado** | Imperador dos Céus | Ar | Senhor alado das tempestades que comanda vendavais cortantes e relâmpagos em seu trono celestial. | Desafio · 150 XP |
| **Cinco Coroas Caídas** | Todos os 5 Monarcas | Mestre | Derrotar os cinco Monarcas e dominar todos os elementos. | Desafio Mestre · 1000 XP |
| **???** *(oculta, ícone de três interrogações)* | — | — | Aparece junto com o Selo Partido, quando os 5 Monarcas caem: "Algo está acordando...". **Some** quando o Soberano é derrotado, dando lugar à conquista dele. | Objetivo |
| **O Selo Partido** *(secreta)* | — | — | Vem junto com a anterior. "Há eras, os cinco Monarcas se uniram contra algo maior que todos eles. Agora que caíram... nada mais o prende." Insinua que não é o fim. | Objetivo |
| **Soberania Absoluta** *(secreta, ícone: a cabeça do Soberano)* | Soberano | Todos | Derrotar o Soberano, que tomou para si o poder dos cinco Monarcas. Também revela o Selo Partido, se faltava. | Desafio · 2000 XP |

A recompensa é **XP do Minecraft** (a barra verde), não o XP Global da Maestria.

Cada conquista de derrota usa o **rosto do respectivo chefe em pixel art 16×16**: pedra e coroa do Colosso, carvão e chamas do Rei Vulcânico, kraken com coroa de coral do Leviatã, casca e copa do Titã, elmo alado do Imperador e coroa dos cinco poderes do Soberano. Os itens `monarch_<elemento>_head` e `sovereign_head` são gerados por `python tools/gen_boss_icons.py`; o ícone secreto `???` permanece oculto até sua revelação.

- **Quem ganha**: todo jogador que causou dano ao chefe durante a luta e está online quando ele morre. Quem só assistiu, espectadores e mortes sem jogador (lava, `/kill`) não ganham nada.
- **Só a criatura conta**: um mob vanilla marcado como Monarca por `/modtest mob boss ...` não dá conquista.
- **Progresso do desafio mestre**: "Cinco Coroas Caídas" mostra o progresso (0/5) conforme cada forma diferente é derrotada.
- A lista de quem lutou não é salva: se o chunk do chefe descarregar no meio da luta, ela recomeça.

## 🖥️ Interface

- **Tecla K**: tela de Maestria.
  - Aba **Maestria**: distribui pontos (+1/+5) e mostra o efeito de cada stat.
  - Aba **Teste**: atalhos dos comandos `/modtest` (gerar arma, invocar ou marcar mob, ±10 stats, +100 XP, mana, reset). Exige cheats/OP.
- **HUD** (canto superior esquerdo): brasão facetado com coroa e runas orbitais, filigranas douradas, barras biseladas de vida/mana/XP suavizadas e reflexo animado. Área de aproximadamente **164×62 pixels de GUI com arma**, antes 182×139 ou mais; carga acrescenta só 12 pixels. Faixa da arma mostra ícone, técnica, tecla R e recarga; pontos livres viram um selo junto ao brasão. O brasão pulsa com vida baixa e brilha ao subir de nível. Informações extensas ficam em K/tooltip.
- **Vida padrão**: só a camada dos **corações do jogador** é ocultada enquanto a HUD personalizada está visível. Absorção aparece como trilho dourado e número extra; veneno/wither mudam a cor da vida, e dano gera clarão curto. Armadura, fome, oxigênio e vida da montaria continuam nas camadas vanilla. F1 e espectador escondem a HUD normalmente.
- **Painel K**: cartões de FOR/AGI/INT/VIT em duas colunas com efeitos e botões +1/+5, XP, mana, pontos e resumo da arma. Abas e botões seguem a mesma paleta da HUD, com foco de teclado visível.
- **Feedback de combate**: números de dano flutuantes (na cor do elemento, maiores e com "!" na vantagem), som de crítico, partículas e efeitos 3D por habilidade: cortes da espada, velocidade da rapieira, raios dourados da adaga e salto/impacto do martelo.

## 🧪 Roteiros de teste

### Com interface

1. Crie um mundo Criativo **com cheats** e aperte **K** → aba **Teste**.
2. Classe *Adaga*, Tier *S. Chefe*, Elemento *Fogo* → **Gerar arma na mão**. O HUD mostra "Adormecida (requer 30 AGI)".
3. Troque o Elemento para *Ar* → **Invocar mob**. Bata nele: números brancos, sem elemento.
4. **+10 AGI** três vezes (e +10 INT uma vez). O estado da arma no HUD muda de adormecida para Fogo; bata de novo: números laranja com "!" e o alvo pegando fogo.
5. **Agachar + clique direito**: Investida com rastro de partículas; a barra de recarga esvazia e enche.
6. **+100 XP** e volte à aba **Maestria** para gastar o ponto.

### Com comandos

```mcfunction
/modtest weapon dagger sub_boss fire          # adaga de S. Chefe, Fogo, requer 30 AGI
/summon zombie ~ ~ ~3
/modtest mob @e[type=zombie,limit=1,sort=nearest] op air   # zumbi Elite de Ar
/modtest set agi 30
/modtest set int 10                           # INT mínima da Investida é 5
/modtest weapon hammer boss water             # martelo de Chefe, requer 50 FOR
/modtest spawn boss fire                      # Rei Vulcânico
/modtest stats
/modtest sovereign                            # Soberano no estágio 1
/modtest sovereign 5                          # já no estágio 5: falta 1 agonia para a Fúria
```

Para ver o Soberano lutar, saia do Criativo (`/gamemode survival`): como todo mob do mod, ele ignora jogadores no Criativo. As agonias e o close acontecem mesmo no Criativo. Para ver o fim e os créditos sem lutar: `/modtest sovereign 6` e bata até ele morrer, ou `/kill @e[type=solomod:sovereign]`.

### Habilidades e animações das quatro armas

1. Em terreno aberto, gere cada classe por **K → Teste**, ou `/modtest weapon <classe> normal none`. Confira em primeira pessoa e com **F5**.
2. **Espada longa**: coloque alvos à frente, atrás e separados por uma parede. Botão direito deve acertar vários à frente uma vez na Meia-Lua. Use **R** para Ascendente (impulso vertical) e Cruzado (dois acertos em sequência). Nenhuma técnica deve acertar os de trás ou atravessar a parede. Alterne durante a recarga para confirmar que ela não é zerada. Teste ataque esquerdo comum.
3. **Adaga**: arremesse e chame com botão direito. Arremesse de novo e use Shift + botão direito para ir até ela, inclusive no voo. Teste parede/teto baixo, destino ocupado, água/lava, fora de 64 blocos e adaga em chunk descarregado: a falha não deve consumir a adaga; o botão direito continua recuperando.
4. **Rapieira**: compare toque curto, meia carga e 1,5 s de carga; a distância e a barra devem aumentar até o limite. Segure por mais tempo para conferir o teto, teste contra uma parede e troque de item durante a carga/dash.
5. **Martelo**: use sobre terra/pedra expostas e observe salto, mergulho, blocos e impacto; repita com `/gamerule mobGriefing false` para verificar que ficam apenas efeitos visuais. Compare ativações já no ar com quedas de 4, 12 e 24 blocos contra alvos iguais; 24 e 32 blocos devem ter o mesmo bônus máximo. Trocar a arma no ar cancela o golpe. Em grupo, confira se os demais veem efeitos/animações e sentem o tremor perto do impacto. Confira o volume/runa do modelo no inventário, em primeira pessoa e em F5, com e sem elemento.
6. **Espada longa em F5**: confira mão de apoio, preparação, varredura e recuperação; as mangas e a armadura devem acompanhar os braços. Em primeira pessoa, veja o arco 3D revelado pelo corte. Teste as cinco formas elementais, alvos múltiplos e oclusão por parede.
7. **Rolamento**: compare Ctrl isolado, Ctrl segurado e duplo Ctrl; só o último rola. Teste W/A/S/D, diagonais e sem direção, em primeira pessoa/F5 e com outro jogador observando. Teste parede, água, voo, menus e spam durante a recarga. Um golpe durante os ticks centrais deve ser evitado; golpes na recuperação e dano de queda/ambiente continuam valendo. Não deve ser possível combinar ataque/arma com a rolagem.
8. **Efeitos em movimento**: na rapieira, veja faixas brancas/ciano acompanhando o caminho e parando junto à parede. Na adaga, veja rastro dourado no arremesso e retorno, raios e selo nas duas pontas do teleporte. Um observador junto ao destino também deve enxergar o clarão. Confira com a mão esquerda principal e em F5.
9. **Interface e velocidade**: abra K nas duas abas, confira navegação por Tab e textos em pt-BR/en-US nas escalas de GUI. Adicione XP para ver a subida de nível, gaste mana e receba dano para ver a suavização/clarão das barras. Confira que somem só os corações vanilla: fome, armadura, oxigênio e montaria permanecem. Use maçã dourada/absorção, veneno e wither; confira trilho dourado, números e cores. Teste F1/espectador e GUI 320×240. Com AGI 0 e armas Normal sem elemento, confira os atributos de 2,5 / 2,2 / 1,6 / 0,8 ataques/s (adaga / rapieira / espada / martelo).
10. **Rapieira com R**: alterne as três técnicas, confira seleção no tooltip/K/HUD. Compare segurar/soltar do dash com clique simples nas outras. Chuva de Agulhas deve acertar nos três tempos em inimigo frontal, sem atingir atrás ou por paredes. Passo do Vento deve estocar e recuar; teste parede atrás, água, borda e troca de item. Compare em F5 e com jogador observador.
11. **Martelo com R**: no chão, compare Giro (área circular com empurrão) e Ruptura (onda frontal). Teste inimigos atrás/através de paredes, vários alvos, desníveis, penhasco e água. Apenas o Salto deve ativar no ar e escalar com a altura. Troque de arma, morra, mude de dimensão e tente rolar durante as sequências: ações devem cancelar/bloquear corretamente.
12. **Seleção independente**: escolha Chuva na rapieira, Cruzado na espada e Giro no martelo, alterne os itens e confira que cada seleção é lembrada. R não deve zerar recargas nem funcionar durante carga/ação/menu. Reconectar deve restaurar os três padrões.

Lista completa de comandos: [README](../README.md#-comandos-de-teste).
