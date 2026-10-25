# 🎮 Gameplay — Regras e mecânicas

Referência completa do que o Solo Mod faz dentro do jogo. Para saber **onde** cada coisa está no código, veja [ARCHITECTURE.md](ARCHITECTURE.md); para o **porquê** na história, [LORE.md](LORE.md).

- [Maestria (stats)](#-maestria-stats)
- [Elementos](#-elementos)
- [Tiers](#-tiers)
- [Armas](#️-armas)
- [Mobs por tier](#-mobs-por-tier)
- [Criaturas próprias](#-criaturas-próprias)
- [Fauna Pacífica](#-fauna-pacífica-15-mobs-de-suporte)
- [Santuários de Eco](#️-santuários-de-eco-conteúdo-opcional)
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

Os tiers **NORMAL / OP / SUB_BOSS / BOSS** continuam sendo usados pelos mobs, com os mesmos atributos, ataques, elementos, XP e fases. Chefes e subchefes não foram alterados. Armas não têm variantes de tier: todas são **Normal e sem elemento**.

Sem o stat exigido (5 FOR ou AGI, conforme a classe), o golpe não ganha o escalonamento de Maestria; armas pesadas/médias mantêm a penalidade de velocidade. Com o requisito, o dano escala com o stat como antes. Não há multiplicador nem passiva elemental da arma.

## ⚔️ Armas

O catálogo atual tem **4 classes de arma**. Cada uma tem um item próprio (`solomod:<classe>`), todos os exemplares são Normal/sem elemento. As habilidades pertencem à árvore do jogador. A aba criativa **Solo Mod: Armas** contém exatamente uma rapieira, uma espada longa, uma adaga e um martelo, neutros e de tier Normal. A aba Teste e o autocomplete de `/modtest weapon` também oferecem só essas quatro classes; o botão de gerar arma e `/modtest weapon <classe>` só geram Normal. Os seletores **Tier do mob** e **Elemento do mob** continuam afetando somente invocar/marcar mobs.

| Classe | Peso | Stat | Habilidade avançada da árvore | Habilidade de classe |
|---|---|---|---|---|
| Espada longa (`longsword`) | Média | FOR | Investida | **Meia-Lua / Ascendente / Cruzado** (R alterna, clique direito usa) |
| Adaga (`dagger`) | Leve | AGI | Investida | **Arremesso** (clique direito) |
| Rapieira (`rapier`) | Leve | AGI | Investida | **Estocada Relâmpago** (clique direito) |
| Martelo (`hammer`) | Pesada | FOR | Impacto Sísmico | **Salto Esmagador** (clique direito) |

Velocidades base, antes de AGI e penalidades de requisito: **adaga 2,5 ataques/s**, **rapieira 2,2**, **espada longa 1,6** (igual às espadas vanilla) e **martelo 0,8**. O martelo tem +9 de dano base. Espadão, machado, kunai e varinha permanecem registrados para compatibilidade com mundos salvos mas não são oferecidos pelo comando de teste nem pelo catálogo visível.

### Habilidades avançadas da árvore (gastam mana)

| Habilidade | Mana | Recarga | INT mínima |
|---|---|---|---|
| Investida (`dash_strike`) | 15 | 3 s | 5 |
| Impacto Sísmico (`ground_slam`) | 25 | 5 s | 10 |
| Disparo de Mana (`mana_bolt`) | 10 | 1,5 s | 5 |
| Explosão de Maestria (`elemental_burst`) | 40 | 10 s | 20 |

Exigem desbloqueio na árvore da classe + Maestria + INT mínima + mana. A potência depende da habilidade (avançada = 1x, suprema = 2x), sem depender de tier da arma. Nas armas com habilidade de classe, a avançada fica em **agachar + clique direito**. Com a adaga arremessada, essa combinação escolhe o teleporte: recolha a adaga para voltar a usar a habilidade avançada.

### Árvores de habilidades — K → Habilidades

A árvore é **um único mapa radial**, sem abas nem filtros por arma. O rosto/skin do jogador fica no centro; somente as quatro armas da aba **Solo Mod: Armas** — rapieira, espada longa, adaga e martelo — formam um círculo ao redor dele, com ícones de tier Normal e sem elemento. De cada arma saem suas habilidades, com ramificações em raízes que crescem até as supremas. A tela mantém pedra, molduras, conexões e estados visuais das conquistas vanilla.

Selecione um nó para consultar efeito, custo, mana e pré-requisito; clique em **Aprender** para liberar. Arraste nos dois eixos, use a roda ou **+ / −** para zoom, as quatro setas para navegar e **Home / Centralizar** para voltar ao jogador. O zoom acompanha o cursor. Selecionar uma arma não oculta as outras. Moldura dourada indica habilidade aprendida; marcador verde indica compra disponível.

O jogador começa com **1 ponto de habilidade**, e ganha **mais 1 a cada 100 XP Global**. Esses pontos são separados dos pontos de atributos: aprender não retira FOR/AGI/INT/VIT nem XP. A árvore começa vazia também em mundos antigos; o XP que já existe concede os pontos correspondentes. Desbloqueios são por jogador/classe, compartilhados entre exemplares da mesma arma, salvos ao sair e preservados na morte. Resetar a Maestria também limpa a árvore.

| Arma | Início (1 ponto) | Ramos (2 pontos cada) | Habilidade avançada (3 pontos) | Final (5 pontos) |
|---|---|---|---|---|
| Espada longa | Meia-Lua | Ascendente / Cruzado | Investida | Explosão de Maestria |
| Rapieira | Estocada Relâmpago | Chuva de Agulhas / Passo do Vento | Investida | Explosão de Maestria |
| Adaga | Arremesso | Passo Relâmpago (teleporte) | Investida | Explosão de Maestria |
| Martelo | Salto Esmagador | Giro Demolidor / Ruptura Sísmica | Impacto no Chão | Explosão de Maestria |

Ramos e habilidade avançada exigem o nó inicial; a suprema exige a habilidade avançada. Não é preciso comprar todos os ramos para alcançar a suprema. Espadão, machado, kunai e varinha legados não aparecem na árvore; seus dados permanecem preservados para compatibilidade com mundos antigos. Aprender uma habilidade avançada a disponibiliza na arma normal da classe por **Shift + direito**. Ao aprender a suprema, ela substitui a avançada nesse atalho; Maestria/INT, mana e recarga continuam validados no servidor.

| Ativação | Mana |
|---|---|
| Meia-Lua / Estocada Relâmpago | 5 |
| Ascendente / Passo do Vento | 7 |
| Cruzado / Chuva de Agulhas / Giro Demolidor | 8 |
| Salto Esmagador / Ruptura Sísmica | 10 |
| Arremesso da adaga | 3 |
| Teleporte da adaga | 8 |
| Recolher a adaga | 0 (operação de recuperação) |

As habilidades avançadas mantêm os custos da tabela anterior. Cada ativação válida cobra **uma vez**, mesmo sem acertar um alvo; múltiplos golpes da mesma técnica não cobram de novo. Carga cancelada da rapieira, bloqueio por recarga/estado, teleporte sem destino seguro e habilidade bloqueada não gastam mana. O dash cobra ao soltar, antes de avançar. Sem mana suficiente, o servidor recusa a ação, inclusive no Criativo. Trocar técnica não elimina a recarga.

### Ícones das armas e modelos na mão

**Somente a perspectiva GUI** (inventário, hotbar, tooltips com ícone, HUD e menus) usa pixel art: sprites detalhados **32×32** em todas as oito classes, somente normais, sem suavização. Aço facetado, guarda, cabo envolto, ouro, gemas e runas distinguem as armas; as variantes elementais e seus assets foram removidos. A rapieira, espada longa, adaga e martelo **continuam em 3D na mão**, em primeira/terceira pessoa, no chão, em molduras e no projétil da adaga. O fantasma da adaga arremessada também tem ícone separado. As classes legadas também recebem os novos ícones somente na GUI e preservam os modelos que já usavam fora dela. Os sprites vêm de `tools/weapon_icons.py`, chamado por `python tools/gen_weapon_models.py`; não editar os JSONs gerados.

### Habilidades de classe (aprendidas, com mana, armas normais)

| Arma | Habilidade |
|---|---|
| Rapieira | **Estocada Relâmpago carregada**: segure botão direito para escolher a distância e solte para avançar perfurando; a carga varia de aproximadamente **3 a 14 blocos**, atingindo o máximo em **1,5 s**. HUD mostra distância e barra; paredes interrompem o dash. Linhas 3D brancas/ciano seguem o trajeto real, com faixas de velocidade, braço em estocada e recuperação rápida da arma. Recarga de 1,5 s após soltar. |
| Adaga | **Arremesso**: na mão fica um fantasma. **Botão direito** chama de volta, girando e ferindo no caminho; **Shift + botão direito** teleporta até a lâmina e a recolhe. Arremesso/retorno têm rastro elétrico dourado e poses do braço; o teleporte tem raios segmentados, clarão e selo angular nas duas pontas, sem gerar um raio real nem fogo. Funciona durante o voo ou cravada. O servidor verifica dono, mesma dimensão, chunk carregado, destino livre e alcance de **64 blocos**; junto de paredes procura espaço próximo, considerando a altura do jogador em pé. Em local bloqueado mantém a adaga para poder chamá-la. O teleporte tem recarga de 1,5 s; passar por cima também recolhe. |
| Espada longa | **Corte em Meia-Lua**: botão direito prepara por **6 ticks**, com a arma nos ombros e mão de apoio, e corta um semicírculo horizontal de **180° à frente**, alcançando **4,5 blocos**, com dano de 1,2× e empurrão. Uma **lâmina de luz volumétrica** revela o arco do golpe, com halo elemental, fio branco e dissipação na recuperação; acertos produzem clarão, faíscas, som pesado e tremor leve. Braços/mangas acompanham o corte em terceira pessoa; a arma tem preparação, varredura ampla e recuperação em primeira pessoa. A sequência dura **18 ticks**. Cada inimigo recebe um acerto; alvos atrás ou através de paredes não entram. Recarga de **1,2 s**. Clique esquerdo é ataque vanilla, inclusive para rebater bolas de fogo. |
| Martelo | **Salto Esmagador**: botão direito no chão salta e mergulha; **no ar mergulha imediatamente**. O servidor registra a maior altura e considera a distância já caída ao ativar: multiplicador **2 + 0,12 × altura**, com teto de **24 blocos** (máximo **4,88×**). O raio começa em **4,5** e aumenta **0,08 por bloco** (máximo **6,42**). Poses de arma erguida até pousar, rastro de queda, poeira, anéis 3D, ondas elementais e tremor. Até **36 + 2 × altura** blocos naturais expostos saltam e voltam ao pousar, respeitando `mobGriefing`; com a regra desligada ficam os efeitos visuais. Recarga de **3,5 s** e proteção de queda durante a habilidade. Água/lava, voo criativo e elytra bloqueiam o início; limite de duração de 12 s. |

Com **espada longa, rapieira ou martelo** equipado, **R** alterna apenas as técnicas aprendidas (tecla configurável em Controles). A seleção aparece na faixa compacta da HUD, no tooltip e no painel K; **botão direito** executa. A seleção é independente para cada classe. Todas as técnicas de uma arma compartilham a recarga do item, sem reiniciá-la ao trocar. A seleção vale para a sessão; ao reconectar, volta à Meia-Lua, Estocada Relâmpago e Salto Esmagador. R durante uma habilidade, carga, rolamento ou menu não troca a seleção.

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

- Texturas normais em pixel art, sem variantes elementais de armas.
- As quatro armas do catálogo têm **modelo 3D de cubos** com textura pixel art 16×16: rapieira com guarda dourada e arco de mão, espada longa com cabo de duas mãos, adaga com lâmina escalonada e martelo volumétrico com runa, aros dourados e molduras de aço salientes. O martelo usa iluminação lateral no inventário e poses próprias de mão para mostrar a face e a profundidade da cabeça. As variantes elementais têm paleta própria; rapieira e espada longa também ganham peças extras. O fantasma da adaga arremessada acompanha seu modelo 3D.
- Não há armas de S. Chefe/Chefe nem aura de tier; encantamentos vanilla mantêm seu brilho.
- A aba **Solo Mod** contém os ovos; a aba **Solo Mod: Armas** mostra só os quatro exemplares, sem duplicatas. **K → Teste** e `/modtest weapon <classe>` também geram somente armas normais.

## 👹 Mobs por tier

Qualquer mob vira "mob do mod" ao receber um **perfil** (tier + elemento). O perfil dá tamanho, atributos, título colorido, aura, animações e um **cérebro de combate** que substitui a IA vanilla enquanto o mob tem alvo.

| Tier | Título | Tamanho / vida / dano | Ociosa | IA | Ataques |
|---|---|---|---|---|---|
| NORMAL | • Lacaio | 0.85x / 1x / 1x | corre curvado, trote | **bando**: grita chamando aliados, fica mais rápido com 2+ aliados, foge com <30% de vida | **Bote** |
| OP | ★ Elite | 1.2x / 2.5x / 1.5x | balança os ombros | **duelista**: circula o alvo enquanto recarrega; **apara** 30% dos golpes de frente e contra-ataca | **Investida** telegrafada · **Golpe Triplo** em leque |
| SUB_BOSS | ✦ Guardião | 1.6x / 6x / 2x, barra roxa | flutua, 3 motes orbitando | **mago**: recua se colam, aproxima se fogem, teleporta às costas de quem foge | **Rajada** de 3 orbes com mira preditiva · **Repulsão** · **Salto Esmagador** · **Translocar** · **Invocação** a 50% |
| BOSS | ♛ Monarca | 2.3x / 14x / 2.8x, barra dourada, sem contorno luminoso | respiração pesada | **3 fases** (60% / 25%) com rugido invulnerável; lê se o jogador luta de perto ou de longe e escolhe ataques contra isso | **Golpe Sísmico** (pule) · **Chuva Elemental** · **Investida** 1/2/3x (parede = atordoado) · **Vórtice** · **Sopro** em cone · convoca Elites e Lacaios |

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

## 🌿 Fauna Pacífica (15 mobs de suporte)

Quinze mobs que não atacam: três por elemento, para equilibrar os chefes. Eles nascem dos **ovos da aba Solo Mod** do criativo (não aparecem na natureza). Para vincular um, o jogador segura o item indicado e clica no mob; o item é consumido, exceto quando o mob já é seu e você só o reativa.

**Regras comuns**
- **Ressonância de Alma Única:** cada jogador tem um companheiro **ativo**. Vincular ou reativar um mob o torna ativo; os outros vinculados ficam parados, em espera. Só o ativo segue o dono e só o ativo reage aos gatilhos.
- **Cargas e exaustão:** cada mob tem de 1 a 3 cargas. Usar a habilidade gasta uma. Ao gastar a última, entra em **Exaustão Elemental** pelo tempo indicado, e ao fim recarrega tudo.
- **Forma Astral:** dano ambiental que mataria o mob (queda, lava, fogo, afogamento) deixa-o com 1 de vida, invisível e sem IA por 60 s. Depois ele reaparece ao lado do dono.
- **Chefes não o atingem:** todo dano de Guardião, Monarca e Soberano é ignorado pelos mobs da fauna, inclusive corpo a corpo.

| Mob | Linhagem | Vínculo | Habilidade (cargas / exaustão) | Gatilho |
|---|---|---|---|---|
| Batedor Espectral | Neutro | Fragmento de Ametista | Pulso de Afastamento (2 / 35 s) | Bote de Lacaio a até 10 blocos: derruba o Lacaio por 8 s |
| Morcego Eco-Rúnico | Neutro | Glow Berries, agachado | Sonar Arcano (3 / 30 s) | Elite contra-atacando ou Guardião prestes a Translocar (até 16 / 22 blocos): silhueta azul por 6 s |
| Autômato Relíquia | Neutro | Lápis-lazúli (bloco) ou Olho do Fim; nasce parado | Égide Cinética (1 / 50 s) | Rajada Elemental do Guardião: escudo de 3 s que absorve até 3 orbes |
| Salamandra de Núcleo Ígneo | Fogo | Creme de Magma | Dreno Térmico (3 / 25 s) | Jogador em chamas com Diabrete ou Executor de fogo a até 16 blocos: apaga o fogo e dá 2 corações de Absorção |
| Escaravelho de Sílica Dourada | Fogo | Pepita de Ouro, só entre 5000 e 7000 ticks do dia | Espelho Solar (2 / 40 s) | Projétil ígneo a 3 blocos: rebate ao conjurador e o atordoa, se não estiver em Fúria |
| Poldro de Cinzas Vulcânicas | Fogo | Fungo Carmesim | Passo de Cinzas (2 / 45 s) | Pisar em magma ou lava: Resistência ao Fogo por 6 s, com a lava carregando o jogador |
| Axolote de Mana Líquida | Água | Peixe Tropical | Purificação Abissal (3 / 30 s) | Enquanto tem cargas, dá respiração debaixo d'água. Com Lentidão, Cegueira ou Náusea: remove e dá Graça do Golfinho por 5 s |
| Caranguejo-Tartaruga de Lótus | Água | Bloco de Musgo ou Erva Marinha | Bolha de Ancoragem (2 / 40 s) | Maremoto criado a até 24 blocos: bolha de 4 s que corta o empurrão em 80% |
| Rêmora de Fossa Abissal | Água | Fragmento de Prismarinho | Intercepção Marinha (3 / 25 s) | Esfera perseguidora do Leviatã a 2,5 blocos do jogador: estourada |
| Mini-Tank Silvestre | Terra | Favo de Mel ou Maçã Dourada | Amortecedor Sísmico (2 / 40 s) | Golpe Sísmico ou Pisão de chefe de terra a até 20 blocos: cúpula de 5 blocos por 4 s que corta o lançamento vertical em 85% |
| Golem Pedregulho Rúnico | Terra | Quartzo do Nether ou Ferro Bruto | Gravidade de Chumbo (2 / 35 s) | Repulsão de Guardião a até 16 blocos ou golpe pesado de Elite de Terra: imune a empurrões por 3 s |
| Gazela das Raízes de Âmbar | Terra | Muda de árvore, agachado | Seiva Libertadora (2 / 30 s) | Raízes Ancestrais do Titã sob o jogador: a raiz não brota, some a Fadiga de Mineração e dá Velocidade II por 3 s |
| Arraia das Nuvens | Ar | Membrana de Fantasma | Corrente Ascendente (2 / 40 s) | Queda maior que 8 blocos: zera o dano e dá Queda Lenta por 4 s |
| Coruja Tempestuosa de Sentinela | Ar | Carne de Coelho (crua ou cozida) ou Pena | Evasão de Rajada (3 / 25 s) | Investida telegrafada de Elite a até 16 blocos: empurra o jogador 3 blocos para o lado |
| Mariposa da Fenda Dimensional | Ar | Fruta do Coro Assada | Passo de Fenda (1 / 60 s) | Dano de Pérola do End anulado; no estágio 6 do Soberano, o Raio do Peito leva o jogador 6 blocos para trás dele |

**Limitações conhecidas**
- Os mobs ainda não aparecem na natureza, e o Autômato não tem os 18 slots de baú.
- A cúpula do Mini-Tank não cancela o tremor de câmera. O Golem não rola. A Gazela só intercepta as raízes do Titã.
- A Coruja não exige o contato visual de 3 s para vincular. A Arraia reage a qualquer queda longa, não só aos Tornados.
- A Mariposa usa a mesma carga para a Pérola e para o Raio do Peito.

## 🏚️ Santuários de Eco (conteúdo opcional)

Seis masmorras opcionais que nascem no mundo, fora da linha Monarcas → Soberano. Não há missões nem NPC: o jogador acha a estrutura, enfrenta a guarnição e o mini-chefe e leva o saque.

**Planta comum** (29×29, redonda): arena central com 4 enfeites, anel (fosso com 4 pontes, ou seco), pátio, muralha com 4 portões e 4 pilares nas diagonais. Baú no pátio com o saque menor; o grande sai do chefe. **Guarnição** (nasce com a estrutura, persistente): o chefe no centro, 3 capangas principais e 2 secundários no pátio e 2 voadores. Os mobs de santuário não atacam uns aos outros nem sofrem as ondas e explosões do próprio chefe.

**Raridade**: um de cada a cada ~34 chunks nos biomas listados. Teste com `/locate structure solomod:<nome>` ou `/place structure solomod:<nome>`.

**Chefes**: barra de vida própria, 3 fases por vida (acima de 66%, acima de 33%, resto) e golpes sempre telegrafados (anel, linha, partículas ou som). Na fase 3 o intervalo entre golpes cai para 1,5 s e o chefe chama reforços (até 4 a 6 por perto).

| Santuário (`<nome>`) | Biomas | Chefe | Capangas |
|---|---|---|---|
| 🔥 **Forja Banhada em Cinzas** (`ash_forge`) | desertos, badlands, savanas | **Cinzagrim, o Ferreiro** (260) | Escória Viva, Ferreiro Ressequido, Fagulha da Forja |
| 📚 **Biblioteca do Esquecimento** (`forgotten_library`) | planícies, florestas | **O Arquivista** (220) | Sentinela de Tinta, Escriba Espectral, Tomo Voador |
| 🌊 **Santuário das Marés** (`tide_sanctuary`) | praias, pântanos, mangues | **A Carpideira** (240) | Afogado do Coro, Caranguejo de Coral, Água-viva de Coral |
| 🌳 **Jardim Petrificado** (`petrified_garden`) | taigas, selvas | **Raiz-Mãe** (300) | Broto Espinhoso, Golem de Musgo, Vespa de Âmbar |
| 🌪️ **Torre dos Ventos Quebrados** (`broken_wind_tower`) | colinas, picos, bosques de cerejeira | **O Arauto** (220, voa) | Harpia Cinzenta, Totem do Vendaval, Elemental de Brisa |
| 💀 **Catacumba Sussurrante** (`whispering_catacomb`) | planícies, florestas, taigas (enterrada) | **Caçador de Ecos** (240) | Ossada Sussurrante, Sombra Rastejante, Lamentador |

A Catacumba fica 22 blocos abaixo da superfície: uma sala fechada com teto, sem portões, e um poço com escada no lado norte (lanterna de alma na boca do poço).

### A falha da realidade
Cada santuário é uma "cena do crime" onde a realidade falha (ver [LORE.md](LORE.md)). Parte é construída com a estrutura; o resto é efeito vivo enquanto você está dentro (`worldgen/StructureAmbientHandler`, a cada 0,5 s).

| Santuário | Na construção | Enquanto você está dentro |
|---|---|---|
| 🔥 Forja | 4 correntes descem do céu sobre a arena segurando bigornas paradas no ar; os braseiros queimam fogo de alma (azul). | Cinza caindo. Perto dos braseiros, **geada na tela sem dano** (o congelamento para abaixo do ponto em que fere). |
| 📚 Biblioteca | 4 ilhas de estantes penduradas de cabeça para baixo, com lanternas. | Gotas de tinta **subindo** do chão e 3 **sombras sem corpo** andando em círculo pelo pátio. |
| 🌊 Marés | 3 lágrimas gigantes de vidro paradas no ar, com luz no núcleo. | **Chuva horizontal** atravessando a arena e gotejamento sob as lágrimas. |
| 🌳 Jardim | 4 árvores de pedra em forma de nervo (tronco de tufo e dendritos). | O chão **respira**: um anel de esporos abre e fecha a cada 4 s, com o som do fôlego. |
| 🌪️ Torre | 7 anéis desalinhados flutuando sobre a arena, com o sino no topo; pilares altos com para-raios. | Ondas de choque visuais saem do sino a cada 3 s; a até 18 blocos do centro, **silêncio total** de ambiente, clima e música. |
| 💀 Catacumba | 3 costelas de osso cruzando a sala e uma parede de lâminas (barras de ferro) com crânios. | **Chama negra** (fumaça e tinta) nos braseiros; perto deles, **escuridão**. |

### Golpes dos chefes
- **Cinzagrim** (fogo; imune a fogo e lava): *Martelada* (onda de 9 blocos, pule) · *Investida* (fase 2+, linha marcada por 0,8 s; para na parede) · *Erupção* (fase 2+, 4 brasas em volta do alvo) · *Fagulhas* (fase 3).
- **O Arquivista** (arcano; flutua a 3 blocos): *Rajada de Páginas* (leque de 5 runas) · *Selo Rúnico* (3 selos no chão, um sob o alvo) · *Cópias* (fase 2+: 2 ilusões por 20 s que morrem com um golpe e **não têm sombra**) · *Salto de Página* (fase 3: reaparece atrás do alvo já atirando).
- **A Carpideira** (água): *Maré* (onda de 10 blocos, pule) · *Lágrimas* (6 gotas em volta do alvo) · *Canto* (fase 2+: dois anéis de 10 bolhas em todas as direções) · *Coro* (fase 3: Afogados do Coro).
- **Raiz-Mãe** (terra; lenta, armadura 10): *Raízes* (fileira de 7 raízes até o alvo, uma atrás da outra) · *Tremor* (onda de 10 blocos) · *Casca* (fase 2+: 4 s sofrendo só 25% do dano; recue) · *Brotos* (fase 3: 3 Brotos Espinhosos).
- **O Arauto** (ar; voa 5 blocos acima): *Rajada* (empurra para longe quem estiver a 9 blocos) · *Mergulho* (mira 0,7 s e desce em linha reta) · *Tempestade* (fase 2+: 3 descargas em volta do alvo) · *Brisas* (fase 3).
- **Caçador de Ecos** (arcano): *Lâminas* (3 facas em leque) · *Sumir* (fica invisível 2 s; pegadas de alma aparecem atrás do alvo meio segundo antes do golpe) · *Rede de Ecos* (fase 2+: 5 marcas em cruz) · *Uivo* (fase 3: Lamentadores). **Único do mod que ataca a Fauna Pacífica.**

### Capangas
| Capanga | Vida | Comportamento |
|---|---|---|
| Escória Viva | 30 | Golpe queima 4 s; ao morrer estoura em clarão (aviso de 0,8 s). |
| Ferreiro Ressequido | 44 | A cada 6 s, com o alvo perto, martelada com onda de fogo curta. |
| Fagulha da Forja | 12 | Voa a 5–10 blocos e atira bolas de fogo (2,5 s). |
| Sentinela de Tinta | 36 | Golpe cega por 3 s. |
| Escriba Espectral | 20 | Flutua longe e escreve uma runa sob o alvo que explode em 1,5 s. |
| Tomo Voador | 14 | Voa e atira runas arcanas (2 s). |
| Afogado do Coro | 28 | Golpe deixa lento por 3 s. |
| Caranguejo de Coral | 40 | Golpes de frente perdem 60% do dano; ataque pelos lados. |
| Água-viva de Coral | 16 | Paira alto e atira bolhas (2,25 s). |
| Broto Espinhoso | 16 | Rápido; quem o acerta corpo a corpo leva 2 de espinhos. |
| Golem de Musgo | 50 | A cada 6 s, tremor de terra de 5 blocos. |
| Vespa de Âmbar | 12 | Voa perto e atira ferrões (1,75 s). |
| Harpia Cinzenta | 18 | Circula alto e mergulha no alvo a cada 4 s. |
| Totem do Vendaval | 40 | Não se move; rajada em anel de 6 blocos que empurra (4 s). |
| Elemental de Brisa | 14 | Voa e atira rajadas de ar (2 s). |
| Ossada Sussurrante | 26 | Golpe enfraquece por 4 s. |
| Sombra Rastejante | 20 | Invisível a mais de 4 blocos do alvo (só a fumaça denuncia). |
| Lamentador | 16 | Voa e atira lamentos arcanos (2,75 s). |

### Saque dos chefes
- **Cinzagrim**: bastões de blaze, ouro, obsidiana, creme de magma, diamantes, 35% restos de netherite.
- **Arquivista**: 2–3 livros encantados, frascos de experiência, lápis-lazúli, ametista, diamantes, 25% totem da imortalidade.
- **Carpideira**: coração do mar, prismarinho, conchas de náutilo, diamantes, 30% tridente.
- **Raiz-Mãe**: esmeraldas, maçãs douradas, favos, ametista, diamantes, 20% maçã dourada encantada.
- **Arauto**: membrana de phantom, penas, cargas de vento, bastões de breeze, diamantes, 15% élitros.
- **Caçador de Ecos**: fragmentos de eco, pérolas do End, frascos de experiência, ossos, diamantes, 30% bússola de recuperação.

## 👑 O Soberano (chefe final)

**História** (ver [LORE.md](LORE.md)): o Soberano é o Primeiro Coro, de quem os cinco Monarcas roubaram membros e Vozes. Cada Monarca derrotado devolve uma parte ao Selo, e ele desperta inteiro como chefe final desta versão de testes.

**Falas** (`mob/SovereignStageDialogueManager.java`, textos em `dialogue.solomod.sovereign.*`): intro no primeiro alvo e fala do estágio 1 cinco segundos depois; a cada agonia, a fala de transição no letreiro do close; ao fim da agonia, a fala de início do novo estágio na barra de ação; no estágio 6, uma fala ao disparar o Raio do Peito e outra na exaustão. A agonia que leva à Fúria vaza partículas de glitch roxo. Ao matar um jogador ele ri com uma de três falas. Na morte, o letreiro mostra a fala 1, troca para a fala 2 aos 2,5 s (sem reiniciar a cena, com separação de canais ciano/vermelho) e, aos 5 s, todo o servidor recebe o aviso da Era VII. No futuro ele vai morar numa dimensão própria; por enquanto nasce pelo **ovo** da aba Solo Mod ou por `/modtest sovereign [estágio]`.

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

## 🗿 Lugares colossais (tronos e arena)

Onde ficam as partes roubadas do Soberano, a realidade falha em escala gigante (ver [LORE.md](LORE.md)). São cinco tronos, um por Monarca, cada um com o seu Monarca já esperando, e o Anfiteatro do Selo Partido no End. Formas construídas em código (`worldgen/ColossalShapes`), cerca de 70 a 150 blocos de largura; raros (um a cada ~64 chunks nos biomas listados; o Anfiteatro a cada ~40). Teste com `/place structure solomod:<nome>` ou `/locate structure solomod:<nome>`.

| Lugar (`<nome>`) | Onde | Monarca | O que se vê | Efeito vivo |
|---|---|---|---|---|
| Cidadela do Pêndulo Invertido (`inverted_pendulum_citadel`) | planícies, prados | Colosso Rei | Praça de pedra, um teto flutuante com 6 torres e uma torre de menagem penduradas de cabeça para baixo, e um monumento com a marca vazia de um braço direito colossal. | Detritos parados no ar. |
| Fornalha Cardíaca (`cardiac_furnace`) | badlands, planalto de savana | Rei Vulcânico | Vulcão suspenso sobre uma praça de basalto, com a câmara do coração (núcleo de magma) e uma cratera de lava contida no topo. | Fogo e fumaça caindo **para cima**; um batimento a cada 10 s que treme a tela. |
| Fossa do Céu Afogado (`drowned_sky_trench`) | oceanos profundos (32+ de água) | Leviatã Coroado | Um cubo **seco** de 60 blocos no fundo do mar, cercado de vidro com o mar do lado de fora, dois navios naufragados flutuando e três garras saindo de sulcos no chão. | Gotas caindo do teto de vidro. |
| Berço das Horas de Âmbar (`amber_hours_cradle`) | florestas escuras, taigas antigas | Titã Ancestral | Árvore de pedra com raízes abrindo para o céu, quatro esferas de âmbar com criaturas da Era II congeladas e uma artéria fossilizada sob o chão, com raízes descendo até ela. | Mel escorrendo das esferas. |
| Panteão da Tempestade Estática (`static_tempest_pantheon`) | picos e colinas | Imperador dos Céus | Três anéis de mármore e seis colunas em volta de um furacão congelado em espiral, pontes de relâmpago cristalizado (luz roxa) e as queimaduras das asas cortadas no chão. | Faíscas paradas no ar e trovões distantes. |
| Anfiteatro do Selo Partido (`shattered_seal_amphitheater`) | ilhas externas do End (sobre o vazio) | — | Teatro cirúrgico de obsidiana suspenso no vazio, com arquibancada em degraus, a mesa no centro e cinco correntes rompidas. | O céu vira **retalhos** dos cinco lugares roubados com bordas de falha ciano/vermelho. |

**O Soberano no Anfiteatro**: quem já partiu o Selo (conquista "O Selo Partido", os cinco Monarcas) e chega a até 24 blocos do centro chama o Soberano, se não houver outro por perto. Se ele cair no vazio, volta ao centro da arena em vez de morrer.

**Monarcas dos tronos**: nascem com a estrutura, já com o elemento do trono, e não reaparecem depois de derrotados. As criaturas no âmbar não têm IA e são invulneráveis.

## 💥 Era VII (pós-jogo)

Depois da primeira morte do Soberano, o mundo guarda a flag `isSovereignDefeated` (`era/EraSevenState`, salva na pasta `data/` do mundo e enviada aos clientes ao entrar). Para testar sem a luta: `/modtest era true` (ou `false`).

| Sinal | O que faz |
|---|---|
| **Fragmento de Dissonância** | O Soberano solta 1 ao morrer (também na aba Solo Mod). Item épico com brilho, tooltip piscando ciano/vermelho. No chão é indestrutível: fogo, lava, cacto, explosão e qualquer dano não o afetam, nunca some por tempo e, se cair no vazio, reaparece no topo da mesma coluna (ou no spawn). Ainda não tem uso. |
| **Cicatriz no Céu** | No Overworld, entre os ticks 17500 e 18500 do dia (por volta da meia-noite), uma fenda fina aparece no zênite: núcleo branco e canais ciano e vermelho desalinhados, tremendo, entrando e saindo em 7,5 s. Caem fagulhas de estática sem som bem acima do jogador. Teste com `/time set 17600`. |
| **Reverência do Caçador** | Com a flag ligada, o Caçador de Ecos, na primeira vez que mira um jogador, ajoelha por 3 s sem atacar (mensagem na barra de ação) e só depois luta. Vale uma vez por Caçador (fica salvo). |

## 🏆 Conquistas

São **27 conquistas em três abas** na tela de conquistas do Minecraft (tecla **L**), todas contadas pela lore ([LORE.md](LORE.md)). A recompensa é **XP do Minecraft** (a barra verde), não o XP Global da Maestria. Os JSON são gerados por `python tools/gen_advancements.py`; os textos ficam em `advancements.solomod.<id>.*`.

**Quem ganha**: as de chefe vão para todo jogador que causou dano a ele e está online quando ele morre (quem só assistiu não ganha). A lista de quem lutou não é salva: se o chunk descarregar no meio da luta, ela recomeça. Um mob vanilla marcado como Monarca por `/modtest mob` não conta.

### 📜 A Canção do Eco (história)
| Conquista | Como ganhar | Tipo / XP |
|---|---|---|
| **Nascido no Silêncio** | Entrar no mundo (raiz da aba). | Tarefa |
| **Gosto de Violência** | Matar o primeiro Lacaio. | Tarefa |
| **Memória Muscular** | Gastar o primeiro ponto de Maestria. | Tarefa |
| **Guarda Quebrada** | Matar uma Elite. | Tarefa |
| **O Sussurro Vermelho** | Matar um Guardião depois que ele entrou em Fúria. | Objetivo · 50 |
| **O Primeiro Golpe** | Matar o Colosso Rei. | Desafio · 150 |
| **Febre Devolvida** | Matar o Rei Vulcânico tendo rebatido (ou desviado com o Escaravelho) uma Bola de Fogo Real na mesma sessão. | Desafio · 150 |
| **Maré Vazia** | Matar o Leviatã Coroado. | Desafio · 150 |
| **Raiz Cortada** | Matar o Titã Ancestral. | Desafio · 150 |
| **Queda do Usurpador** | Matar o Imperador dos Céus. | Desafio · 150 |
| **O Selo Partido** | Matar os 5 Monarcas (progresso 0/5; o do Fogo conta mesmo sem rebater a bola). | Objetivo · 300 |
| **Cara a Cara no Espelho** *(oculta)* | Ser mirado pelo Soberano ou feri-lo. | Objetivo |
| **Dissonância Absoluta** *(oculta)* | Matar o Soberano. | Desafio · 500 |
| **[ERRO: SEXTA VOZ DETECTADA]** *(oculta)* | Chega sozinha 10 s depois de matar o Soberano. | Desafio |

### 🌿 Ecos Serenos (Fauna Pacífica)
A aba aparece no primeiro vínculo.

| Conquista | Como ganhar | Tipo / XP |
|---|---|---|
| **Diapasão da Alma** | Vincular (ou reativar) um companheiro (raiz da aba). | Tarefa |
| **Silêncio Necessário** | Gastar a última carga de um companheiro (Exaustão). | Tarefa |
| **O Que Já Morreu Não Morre** | Ver um companheiro entrar na Forma Astral. | Tarefa |
| **Harmonia das Cinco Linhagens** | Vincular um de cada linhagem (progresso 0/5). | Objetivo · 100 |
| **Salto Entre Quadros** | Ser tirado do Raio do Peito pela Mariposa da Fenda Dimensional. | Desafio · 100 |
| **O Primeiro Coro** | Vincular as 15 formas (progresso 0/15). | Desafio · 300 |

### 🏚️ Os Cúmplices do Selo (Santuários)
A aba aparece no primeiro guardião derrotado; a raiz é o desafio que junta os seis.

| Conquista | Como ganhar | Tipo / XP |
|---|---|---|
| **A Mentira dos Cinco Tronos** | Derrotar os 6 guardiões de Santuário (progresso 0/6). | Desafio · 400 |
| **Correntes para Ninguém** | Derrotar Cinzagrim. | Tarefa · 100 |
| **O Peso de Uma Sombra** | Derrotar o Arquivista verdadeiro (cópias não contam). | Tarefa · 100 |
| **Canção da Culpa** | Derrotar a Carpideira. | Tarefa · 100 |
| **Coração Empedrado** | Derrotar a Raiz-Mãe. | Tarefa · 100 |
| **O Grito Engolido** | Derrotar o Arauto. | Tarefa · 100 |
| **Fiel Até o Fim** | Derrotar o Caçador de Ecos. | Tarefa · 100 |

Os ícones dos Monarcas e do Soberano são os rostos em pixel art (`monarch_<elemento>_head`, `unknown_boss`, `sovereign_head`), gerados por `python tools/gen_boss_icons.py`.

## 🖥️ Interface

- **Tecla K**: tela de Maestria.
  - **Habilidades**: abre o mapa radial único em estilo conquistas vanilla, com jogador ao centro e armas/raízes ao redor, com compra validada no servidor.
  - Aba **Maestria**: distribui pontos (+1/+5) e mostra o efeito de cada stat.
  - Aba **Teste**: atalhos dos comandos `/modtest` (gerar arma, invocar ou marcar mob, ±10 stats, +100 XP, mana, reset). Exige cheats/OP.
- **HUD** (canto superior esquerdo): brasão facetado com coroa e runas orbitais, filigranas douradas, barras biseladas de vida/mana suavizadas, rolamento amarelo sob a mana e XP no contorno do escudo e reflexo animado. Área de aproximadamente **164×62 pixels de GUI com arma**, antes 182×139 ou mais; carga acrescenta só 12 pixels. Faixa da arma mostra ícone, técnica, tecla R e recarga; pontos livres viram um selo junto ao brasão. O brasão pulsa com vida baixa e brilha ao subir de nível. Informações extensas ficam em K/tooltip.
- **Recarga do rolamento**: a barra amarela de 2 pixels **sob a mana**, no canto superior esquerdo, esvazia ao rolar e enche em 1,3 s. Só inicia após confirmação do servidor; morte/troca de dimensão limpam a recarga. Não há barra de rolamento sobre o XP vanilla.
- **Progresso de nível**: XP Global preenche o **contorno do escudo do nível**, a partir do topo em sentido horário. Uma volta completa representa o próximo nível; o contorno completo pisca ao subir e recomeça com o progresso restante.
- **Vida padrão**: só a camada dos **corações do jogador** é ocultada enquanto a HUD personalizada está visível. Absorção aparece como trilho dourado e número extra; veneno/wither mudam a cor da vida, e dano gera clarão curto. Armadura, fome, oxigênio e vida da montaria continuam nas camadas vanilla. F1 e espectador escondem a HUD normalmente.
- **Painel K**: cartões de FOR/AGI/INT/VIT em duas colunas com efeitos e botões +1/+5, XP, mana, pontos e resumo da arma. Abas e botões seguem a mesma paleta da HUD, com foco de teclado visível.
- **Feedback de combate**: números de dano flutuantes (na cor do elemento, maiores e com "!" na vantagem), som de crítico, partículas e efeitos 3D por habilidade: cortes da espada, velocidade da rapieira, raios dourados da adaga e salto/impacto do martelo.

## 🧪 Roteiros de teste

### Com interface

1. Crie um mundo Criativo **com cheats** e aperte **K** → aba **Teste**. Antes de usar técnicas, ganhe XP com **+100 XP** e libere os nós em **Habilidades**. As habilidades de tier também exigem desbloqueio.
2. Classe *Adaga* → **Gerar arma normal**. Trocar Tier do mob/Elemento do mob não muda a arma gerada, que exige 5 AGI para escalonamento.
3. Troque o Elemento para *Ar* → **Invocar mob**. Bata nele: números brancos, sem elemento.
4. **+10 AGI** e **+10 INT**. Aprenda a Investida na árvore: Shift + direito a usa na adaga normal, consumindo mana. O golpe da arma continua neutro.
5. **Agachar + clique direito**: Investida com rastro de partículas; a barra de recarga esvazia e enche.
6. **+100 XP** e volte à aba **Maestria** para gastar o ponto.

### Com comandos

```mcfunction
/modtest weapon dagger                        # adaga Normal, sem elemento
/summon zombie ~ ~ ~3
/modtest mob @e[type=zombie,limit=1,sort=nearest] op air   # zumbi Elite de Ar
/modtest set agi 30
/modtest set int 10                           # INT mínima da Investida é 5
/modtest weapon hammer                        # martelo Normal, sem elemento
/modtest spawn boss fire                      # Rei Vulcânico
/modtest stats
/modtest sovereign                            # Soberano no estágio 1
/modtest sovereign 5                          # já no estágio 5: falta 1 agonia para a Fúria
```

Para ver o Soberano lutar, saia do Criativo (`/gamemode survival`): como todo mob do mod, ele ignora jogadores no Criativo. As agonias e o close acontecem mesmo no Criativo. Para ver o fim e os créditos sem lutar: `/modtest sovereign 6` e bata até ele morrer, ou `/kill @e[type=solomod:sovereign]`.

### Habilidades e animações das quatro armas

1. Em **K → Habilidades**, aprenda os nós que serão testados; use **+100 XP** e **Encher mana** na aba Teste para preparar. Em terreno aberto, gere cada classe por **K → Teste**, ou `/modtest weapon <classe>`. Confira em primeira pessoa e com **F5**.
2. **Espada longa**: coloque alvos à frente, atrás e separados por uma parede. Botão direito deve acertar vários à frente uma vez na Meia-Lua. Use **R** para Ascendente (impulso vertical) e Cruzado (dois acertos em sequência). Nenhuma técnica deve acertar os de trás ou atravessar a parede. Alterne durante a recarga para confirmar que ela não é zerada. Teste ataque esquerdo comum.
3. **Adaga**: arremesse e chame com botão direito. Arremesse de novo e use Shift + botão direito para ir até ela, inclusive no voo. Teste parede/teto baixo, destino ocupado, água/lava, fora de 64 blocos e adaga em chunk descarregado: a falha não deve consumir a adaga; o botão direito continua recuperando.
4. **Rapieira**: compare toque curto, meia carga e 1,5 s de carga; a distância e a barra devem aumentar até o limite. Segure por mais tempo para conferir o teto, teste contra uma parede e troque de item durante a carga/dash.
5. **Martelo**: use sobre terra/pedra expostas e observe salto, mergulho, blocos e impacto; repita com `/gamerule mobGriefing false` para verificar que ficam apenas efeitos visuais. Compare ativações já no ar com quedas de 4, 12 e 24 blocos contra alvos iguais; 24 e 32 blocos devem ter o mesmo bônus máximo. Trocar a arma no ar cancela o golpe. Em grupo, confira se os demais veem efeitos/animações e sentem o tremor perto do impacto. Confira o volume/runa do modelo no inventário, em primeira pessoa e em F5, sempre sem elemento.
6. **Espada longa em F5**: confira mão de apoio, preparação, varredura e recuperação; as mangas e a armadura devem acompanhar os braços. Em primeira pessoa, veja o arco 3D revelado pelo corte. Teste a forma normal, alvos múltiplos e oclusão por parede.
7. **Rolamento**: compare Ctrl isolado, Ctrl segurado e duplo Ctrl; só o último rola. Teste W/A/S/D, diagonais e sem direção, em primeira pessoa/F5 e com outro jogador observando. Teste parede, água, voo, menus e spam durante a recarga. Um golpe durante os ticks centrais deve ser evitado; golpes na recuperação e dano de queda/ambiente continuam valendo. Não deve ser possível combinar ataque/arma com a rolagem.
8. **Efeitos em movimento**: na rapieira, veja faixas brancas/ciano acompanhando o caminho e parando junto à parede. Na adaga, veja rastro dourado no arremesso e retorno, raios e selo nas duas pontas do teleporte. Um observador junto ao destino também deve enxergar o clarão. Confira com a mão esquerda principal e em F5.
9. **Interface e velocidade**: abra K nas duas abas, confira navegação por Tab e textos em pt-BR/en-US nas escalas de GUI. Adicione XP para ver o contorno do escudo completar a volta e a subida de nível, gaste mana e receba dano para ver a suavização/clarão das barras. Confira que somem só os corações vanilla: fome, armadura, oxigênio e montaria permanecem. Use maçã dourada/absorção, veneno e wither; confira trilho dourado, números e cores. Teste F1/espectador e GUI 320×240. Com AGI 0 e armas Normal sem elemento, confira os atributos de 2,5 / 2,2 / 1,6 / 0,8 ataques/s (adaga / rapieira / espada / martelo).
10. **Rapieira com R**: alterne as três técnicas, confira seleção no tooltip/K/HUD. Compare segurar/soltar do dash com clique simples nas outras. Chuva de Agulhas deve acertar nos três tempos em inimigo frontal, sem atingir atrás ou por paredes. Passo do Vento deve estocar e recuar; teste parede atrás, água, borda e troca de item. Compare em F5 e com jogador observador.
11. **Martelo com R**: no chão, compare Giro (área circular com empurrão) e Ruptura (onda frontal). Teste inimigos atrás/através de paredes, vários alvos, desníveis, penhasco e água. Apenas o Salto deve ativar no ar e escalar com a altura. Troque de arma, morra, mude de dimensão e tente rolar durante as sequências: ações devem cancelar/bloquear corretamente.
12. **Seleção independente**: escolha Chuva na rapieira, Cruzado na espada e Giro no martelo, alterne os itens e confira que cada seleção é lembrada. R não deve zerar recargas nem funcionar durante carga/ação/menu. Reconectar deve restaurar os três padrões.

Lista completa de comandos: [README](../README.md#-comandos-de-teste).

### Árvores, mana, ícones e rolamento

1. Com jogador novo, tente usar cada arma sem aprender: a ação deve ser recusada, sem mana/recarga. Gaste o ponto inicial numa raiz; tente comprar um filho sem pontos, comprar duas vezes e comprar a suprema antes do pai. As falhas não gastam pontos. Ganhe XP e confira que pontos de atributos e de habilidade crescem separadamente.
2. Aprenda as técnicas, alterne com R e compare mana antes/depois, inclusive em golpes sem alvo e técnicas com vários acertos. Sem mana, nenhuma começa. Segurar a rapieira não cobra; soltar cobra uma vez. Cancelar trocando de item não cobra. Com a adaga, retorno é grátis, arremesso cobra 3 e teleporte seguro cobra 8; tente teleporte contra destino bloqueado ou fora de alcance.
3. Reconecte, morra e troque de dimensão: as árvores devem persistir. Abra um mundo antigo com XP: árvores vazias, pontos de habilidade correspondentes ao nível e atributos preservados. Reset deve apagar os desbloqueios. Verifique também em servidor com dois jogadores: um não compartilha desbloqueios com o outro.
4. Compare os quatro ícones normais no inventário/hotbar/K com os modelos em primeira pessoa/F5. Adaga voando e arma no chão continuam com geometria 3D. Confira o fantasma arremessado. Teste pt-BR/en-US e GUI 320×240; confira o rosto do jogador no centro e somente rapieira, espada longa, adaga e martelo normais no mesmo mapa, sem filtros; explore arrastando nos dois eixos, com zoom pela roda/+ /−, setas e Home e selecione nós usando Tab/Enter.
5. Na Sobrevivência/Aventura, role com duplo Ctrl: a barra amarela sob a mana esvazia e recupera em 1,3 s; spam/ação recusada não reinicia a barra. Confira armadura, fome, montaria, F1, morte e troca de dimensão. Chefes novos e já salvos não devem exibir contorno branco através de paredes; aura de Fúria e detalhes emissivos próprios continuam.

6. Com XP Global em 0/25/50/75/99 de cada centena, confira o contorno do escudo vazio e preenchendo em sentido horário. Ao completar 100, o número do nível sobe, o contorno completo pisca e a volta recomeça. O ganho de vários níveis de uma vez termina com o nível e restante corretos. Reset não deve simular uma subida. Rolamento nunca altera esse progresso.

### Migração para armas normais

Armas de saves antigos são lidas como Normal/sem elemento, com requisito normal da classe e sem habilidade gravada no item; os IDs de item e o formato dos codecs são mantidos para não perder as pilhas. Pontos e nós já aprendidos na árvore são preservados. Teste uma antiga arma Chefe/Fogo: deve aparecer como arma normal, sem bônus/aura/passiva elemental. Os mobs, ovos, chefes e subchefes continuam com seus tiers e elementos originais.
