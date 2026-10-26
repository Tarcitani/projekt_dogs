# 📜 Lore — A Canção do Eco

A história do Solo Mod: de onde vêm os elementos, os monstros, os Monarcas, o Soberano, a Fauna Pacífica e os Santuários, e quem o jogador realmente é. No fim há as **regras do cânone** para encaixar mobs, chefes e mudanças futuras sem contradizer o que já existe.

Este documento é a fonte da verdade da narrativa. Textos dentro do jogo (falas, conquistas, cinemáticas, créditos, nomes) devem concordar com ele; se um precisar mudar, mude os dois juntos. As falas do Soberano ficam em `lang/*.json` (`dialogue.solomod.sovereign.*`) e são disparadas por `mob/SovereignStageDialogueManager.java`.

O livro inicial **Ecos do Despertar** (`item/StarterGuide`, textos `book.solomod.guide.*`) é um manuscrito anônimo de oito páginas. Apresenta a Canção e os Ecos pela tradição oral e por fragmentos de um relato pessoal, com lacunas e um encerramento em suspense. O autor não é identificado com nenhum personagem do cânone; seu sonho não anuncia um evento de gameplay. Não há tutorial, nomes de chefes, locais, objetivos ou revelações da trama. O jogador descobre esses pontos durante a jornada. A entrega continua única por jogador/mundo, e exemplares antigos no inventário recebem a nova narrativa.

- [O mundo em uma página](#-o-mundo-em-uma-página)
- [Cosmologia: a Canção, o Eco e as Vozes](#-cosmologia-a-canção-o-eco-e-as-vozes)
- [Linha do tempo](#-linha-do-tempo)
- [O Desperto Sem Voz (o jogador)](#-o-desperto-sem-voz-o-jogador)
- [Os cinco Monarcas (os ladrões)](#-os-cinco-monarcas-os-ladrões)
- [As Cortes dos Monarcas (mobs por tier)](#-as-cortes-dos-monarcas-mobs-por-tier)
- [O Soberano (o Primeiro Coro)](#-o-soberano-o-primeiro-coro)
- [A Dissonância e a Era VII](#-a-dissonância-e-a-era-vii)
- [A Fauna Pacífica (Ecos Serenos)](#-a-fauna-pacífica-ecos-serenos)
- [Os Santuários de Eco (os cúmplices)](#️-os-santuários-de-eco-os-cúmplices)
- [Lugares colossais](#️-lugares-colossais)
- [Glossário](#-glossário)
- [Regras do cânone (para conteúdo novo)](#-regras-do-cânone-para-conteúdo-novo)
- [Ganchos em aberto](#-ganchos-em-aberto)

---

## 🌍 O mundo em uma página

Tudo o que vive canta, e o mundo é sustentado por essa música: a **Canção**. Quando algo com vontade morre, a sua parte da Canção fica no mundo como um **Eco**. Os Ecos cantam em quatro **Vozes** (Água, Fogo, Ar e Terra) ou no **Silêncio** entre elas.

No começo, um único ser guardava todas as Vozes e o Silêncio: o **Primeiro Coro**. Ele é quem hoje chamamos de **Soberano**.

A história que os reinos contam diz que os cinco **Monarcas** se uniram para prender um monstro. **É mentira.** Os Monarcas **traíram** o Primeiro Coro, esquartejaram o corpo dele ainda vivo e roubaram um membro e uma Voz cada um para fundar cinco impérios. O que sobrou, uma carcaça mutilada, enlouqueceu de **Fome** tentando recuperar as partes. Para que ela nunca saísse, os Monarcas usaram cinco mestres, os guardiões dos Santuários, para trancá-la no **Selo**.

O jogador, o **Desperto Sem Voz**, não é um herói qualquer. Para impedir que o Soberano abrisse o Selo por dentro, o Arquivista **partiu a alma dele em duas**: trancou a **Fome** no Selo e jogou a **Vontade**, vazia, no mundo. **O jogador é a Vontade amputada do próprio Soberano, reencarnada.**

Por isso ele não tem Voz, engole Ecos de violência como o Soberano engolia e se lembra de golpes que nunca aprendeu. Ao matar os cinco Monarcas, devolve as partes roubadas à cela, o Selo se parte e a Fome desperta inteira. O choque entre a Fome e a Vontade é a **Dissonância Absoluta**: a morte do Soberano rasga a Canção como um código que trava. Pelo rasgo, algo que vive **fora da Canção** escuta o estalo.

---

## 🎶 Cosmologia: a Canção, o Eco e as Vozes

**A Canção** é a física do mundo: enquanto alguém a segura, a realidade fica de pé. **O Eco** é a parte dela que sobra de toda vida com vontade. Ele tem memória (por isso os chefes "leem o seu estilo de luta": escutam o Eco dos seus golpes) e tem tom, que é a **Voz**.

| Voz | Elemento no jogo | Natureza | Vence |
|---|---|---|---|
| 💧 **Água** | `WATER` | paciência, maré, o que cede e volta | Fogo |
| 🔥 **Fogo** | `FIRE` | vontade, forja, o que consome | Ar |
| 🌪️ **Ar** | `AIR` | liberdade, chamado, o que não se prende | Terra |
| 🪨 **Terra** | `EARTH` | memória do corpo, raiz, o que sustenta | Água |
| 🔮 **Silêncio** | `NONE` (Neutro/Arcano) | o intervalo entre as Vozes: ordem, registro, fome | nenhuma |

O ciclo **Água › Fogo › Ar › Terra › Água** é a ordem em que uma Voz abafa a seguinte. Vozes opostas (Água×Ar, Fogo×Terra) não se tocam. O Silêncio não vence nem perde: é a pausa onde todas cabem.

**Ecos de violência e Ecos serenos.** O modo como se morre dá forma ao Eco:
- quem morre lutando, com raiva ou com fome deixa um **Eco de violência**, que procura um corpo e vira **monstro**;
- quem morre em paz deixa um **Eco sereno**, que às vezes toma a forma de uma criatura gentil: a **Fauna Pacífica**.

Só Ecos de violência alimentam. Por isso o jogador só ganha XP Global com os monstros do mod: animais e mobs comuns do Minecraft morrem calados, sem rancor para absorver.

---

## 🕰️ Linha do tempo

| Era | O que aconteceu |
|---|---|
| **I. O Primeiro Coro** | Um único ser guarda as quatro Vozes e o Silêncio e sustenta a Canção. Os Ecos serenos desta era são os que hoje formam a Fauna Pacífica. |
| **II. O Roubo** | Cinco seres traem o Primeiro Coro, esquartejam o corpo vivo e roubam um membro e uma Voz cada. Viram os **Monarcas** e fundam cinco impérios, cada um com a sua **corte** (a origem dos tiers). |
| **III. A Fome** | A carcaça mutilada enlouquece tentando recuperar as partes e devora Ecos para preencher o vazio. Os impérios a chamam de monstro e a si mesmos de salvadores. |
| **IV. O Selo** | Os Monarcas usam cinco mestres: o **Arauto** leva o chamado, **Cinzagrim** forja as correntes, a **Raiz-Mãe** cresce as raízes que as ancoram, a **Carpideira** afoga o caminho de volta e o **Arquivista** escreve o nome verdadeiro para que o Selo o reconheça. Por fim, o Arquivista **parte a alma** do prisioneiro: tranca a **Fome** e joga a **Vontade** vazia no mundo. O **Caçador de Ecos**, único servo fiel, é enterrado vivo. |
| **V. O Longo Sussurro** | As partes roubadas nunca aceitam os novos donos e puxam de volta para a cela (a **Fúria**). Os Monarcas enlouquecem devagar e as cortes se espalham como monstros. Os Santuários são esquecidos, e seus guardiões viram aquilo que fizeram. |
| **VI. O Desperto** *(o jogo)* | A Vontade reencarna sem Voz. Absorve Ecos, lembra o que foi, derruba os Monarcas um a um e devolve cada parte ao Selo ("O Selo Partido"). A Fome desperta inteira, reconhece a própria metade e cai diante dela. |
| **VII. O Trono Vazio** *(depois dos créditos)* | A morte do Soberano é a **Dissonância Absoluta**: a Canção rasga. "Um trono vazio não fica vazio por muito tempo." Algo **fora da Canção** escutou o estalo. |

---

## 🧍 O Desperto Sem Voz (o jogador)

- **Quem é.** A **Vontade amputada do Soberano**, separada da Fome pelo Arquivista e reencarnada sem memória. O jogador não sabe disso no começo; o Soberano descobre no meio da luta final.
- **Sem elemento.** As Vozes foram roubadas junto com o corpo, e a Vontade nasceu sem nenhuma. Por isso **as armas do jogador são sempre Normais e sem elemento**.
- **Absorver Ecos (XP Global).** A Vontade engole Ecos de violência como o Soberano engolia. Cada monstro do mod derrotado vira **XP Global** e, a cada 100, um ponto de **Maestria**.
- **Maestria (FOR · AGI · INT · VIT).** "Você não está aprendendo a lutar. Seu corpo está lembrando de como já foi imenso."
- **Árvore de Habilidades.** Cada habilidade é a **memória de um golpe** do Primeiro Coro. Aprender é lembrar; a **mana** é o fôlego que a Vontade gasta sem o resto do corpo.
- **Ressonância de Alma Única.** Os Ecos serenos da Era I reconhecem a Vontade do ser que guardava a Canção. Sem as Vozes, ela só sustenta uma ressonância por vez: um único companheiro ativo.
- **"Solo".** A metade que não canta, sozinha contra um mundo de vozes, inclusive contra a própria Fome.

---

## ♛ Os cinco Monarcas (os ladrões)

Cada Monarca roubou um membro e uma Voz do Primeiro Coro. O modelo do Soberano, "feito de pedaços dos cinco Monarcas", é na verdade o contrário: são as partes **dele**, de volta ao lugar.

| Monarca | Voz | Roubou | Quem foi | O que a Fúria faz |
|---|---|---|---|---|
| **Colosso Rei** | Silêncio | o **Braço Direito** e a **Coroa de Pedra** | Deu a primeira punhalada. Gigante de pedra que desafia todas as forças elementais. | O Punho Sísmico: o braço roubado batendo sem dono. |
| **Rei Vulcânico** | Fogo | a **Perna Esquerda** e a **Fornalha Peitoral** | Aqueceu o trono com o sangue do peito do Soberano e transformava continentes em caldeiras. | A Genki-dama e as bolas de fogo reais. |
| **Leviatã Coroado** | Água | os **Tentáculos** e a **Pinça Abissal** | O rei-kraken das fossas, cujos maremotos engoliam impérios. | O Maremoto e as Esferas de Água. |
| **Titã Ancestral** | Terra | a **Perna Direita** e o **Sangue de Âmbar** | A árvore colossal de coração de âmbar, cujas raízes sustentavam o mundo. | Raízes, Pisão e Pulão. |
| **Imperador dos Céus** | Ar | as **Asas Celestiais** e a **Auréola** | O senhor das tempestades, que cortou as asas para que o Soberano rastejasse. | Os Tornados. |

Quando um Monarca cai, a parte roubada **volta à cela**. É por isso que o Soberano desperta um poder de Monarca a cada estágio: está costurando de volta os próprios membros.

> Não confunda o **Arauto Solar** (forma de Fogo do Guardião, conselheiro da corte do Rei Vulcânico) com **O Arauto** (guardião da Torre dos Ventos Quebrados, que levou o chamado aos ladrões).

---

## 👹 As Cortes dos Monarcas (mobs por tier)

Os monstros com tier são as **cortes** dos Monarcas: Ecos de violência que juraram servir a um trono e receberam uma forma em cada Voz roubada. Os cavaleiros juraram proteger cinco tronos; nenhum lembra mais de quem os tronos tinham medo.

| Tier | Papel na corte | Forma do Silêncio | 🔥 Fogo | 💧 Água | 🪨 Terra | 🌪️ Ar |
|---|---|---|---|---|---|---|
| **• Lacaio** (`NORMAL`) | crias e mensageiros, caçam em bando | Diabrete | Diabrete Ígneo | Diabrete Abissal | Diabrete de Pedra | Sílfide Travessa |
| **★ Elite** (`OP`) | cavaleiros e duelistas da corte | Duelista Espectral | Executor Infernal | Tritão Corsário | Sentinela de Granito | Valquíria do Vento |
| **✦ Guardião** (`SUB_BOSS`) | conselheiros e videntes, guardam as portas do trono | Oráculo | Arauto Solar | Medusa Abissal | Monólito Ancestral | Olho da Tempestade |
| **♛ Monarca** (`BOSS`) | o próprio trono | Colosso Rei | Rei Vulcânico | Leviatã Coroado | Titã Ancestral | Imperador dos Céus |

A **Fúria** (a 25% de vida) é a Voz roubada tentando fugir de volta para o abismo quando quem a carrega sangra.

O tier que um mob vanilla ganha com `/modtest mob` também tem explicação: um Eco de violência solto **possui** a criatura comum e lhe dá o lugar dele na corte.

---

## ☬ O Soberano (o Primeiro Coro)

- **O que foi.** O guardião completo das quatro Vozes e do Silêncio: quem segurava a Canção.
- **O que virou.** Uma carcaça mutilada e faminta: a **Fome**, trancada no Selo, sem a própria Vontade.
- **Por que tem cinco poderes.** São as partes que voltam: braço, pernas, tentáculos, asas. Cada estágio é uma costura.
- **O que ele descobre.** No fim do estágio 3, reconhece no silêncio do peito do jogador "o pedaço que arrancaram da minha mente". A partir daí tenta **reabsorver** a própria Vontade.
- **Por que ri.** Ao matar o jogador: "Mesmo sem memória, você luta como eu. Mas a Fome sempre vence a Vontade!"
- **O raio do peito.** O canto de todas as Vozes de uma vez ("Cante comigo ou desapareça!"). Sem a Vontade, o peito não aguenta o coro, e ele cai de joelhos.
- **O fim.** "Você venceu a Fome. Mas olhe para as suas mãos, Desperto..." O Selo também mantinha o lado de **fora** trancado.

---

## 💥 A Dissonância e a Era VII

A Canção sustenta a física do mundo. Quando a Fome e a Vontade se enfrentam, a mesma alma canta duas músicas opostas: é a **Dissonância Absoluta**. A morte do Soberano não é uma desintegração comum: a realidade **trava como código**. Quadros congelam, a imagem se separa em canais ciano (`#00E5FF`) e vermelho (`#FF1744`) e o som vira bipes de 8 bits.

Pelo rasgo, algo que vive **fora da Canção** escuta o estalo. Todo o servidor ouve: *"O Trono está vazio. Algo fora da Canção escutou o estalo."*

O mundo lembra (flag `isSovereignDefeated`, salva no mundo por `era/EraSevenState`). Três sinais da Era VII já estão no jogo:
- **Fragmento de Dissonância** (`dissonance_shard`): cai do Soberano. Não vibra em nenhuma Voz nem no Silêncio, então nada o destrói (fogo, lava, cacto, explosão) e o vazio o devolve à superfície. "Está esperando a próxima atualização da realidade."
- **A Cicatriz no Céu**: toda meia-noite (ticks 17500 a 18500 do dia) uma fenda com aberração cromática se abre no zênite do Overworld, soltando estática silenciosa.
- **A reverência do Caçador**: o Caçador de Ecos, servo fiel, reconhece o mestre que agora habita o Desperto por inteiro e se ajoelha por 3 s antes de lutar.

---

## 🌿 A Fauna Pacífica (Ecos Serenos)

Os 15 mobs de suporte são **Ecos serenos da Era I**: criaturas que viveram e morreram em paz sob o Primeiro Coro. Elas **reconhecem a Vontade** do ser que guardava a Canção, e por isso se aproximam do jogador. Cada uma nasce numa das cinco linhagens (Silêncio, Fogo, Água, Terra, Ar), três por linhagem. Juntas, são o som que o peito do Soberano tinha antes do roubo ("O Primeiro Coro").

| Regra de jogo | Explicação na lore |
|---|---|
| **Isolamento**: monstros não os atacam (tag `#solomod:passive_echo`) | Ecos de violência só procuram outros Ecos de violência. Um Eco sereno não tem fome para ser sentida. |
| **Um companheiro ativo** (Ressonância de Alma Única) | A Vontade, sem as Vozes, só sustenta uma ressonância por vez. |
| **Item de vínculo** | Uma oferenda que soa como a canção original daquele Eco (ametista para o Batedor, mel para o Mini-Tank...). |
| **Cargas e Exaustão Elemental** | Usar a habilidade gasta a canção do Eco; exausto, ele precisa de silêncio entre as notas. |
| **Forma Astral** (não morre de dano do ambiente; volta em 60 s) | O mundo não consegue matar o que já morreu. O Eco se desfaz e reaparece junto à sua âncora, o Desperto. |
| **Dano dos chefes é ignorado** | O poder dos chefes vem de Vozes roubadas; um Eco sereno da Era I é anterior ao roubo e não vibra nessa frequência. |
| **Reagem aos ataques dos chefes** (gatilhos) | Ecos serenos escutam o Eco de um golpe antes dele acontecer e protegem a Vontade. |

A única exceção é o **Caçador de Ecos**, que foi feito para caçar justamente o que os outros não enxergam.

---

## 🏚️ Os Santuários de Eco (os cúmplices)

Os Santuários são as **cenas do crime**. Cinco mestres ajudaram os Monarcas a trancar o Soberano; o sexto lugar guarda o único que ficou do lado dele. As eras os fizeram esquecer a missão, e cada guardião virou uma versão distorcida do seu ofício. Derrotá-los é opcional, mas juntos eles revelam **a Mentira dos Cinco Tronos**.

| Santuário | Voz | Guardião | Papel no crime | O que restou |
|---|---|---|---|---|
| 🔥 **Forja Banhada em Cinzas** | Fogo | **Cinzagrim, o Ferreiro** | Forjou as correntes do Selo com o calor do Rei Vulcânico e nunca perguntou para quem. | Ainda martela, mas forja para ninguém. Escórias são metal que nunca virou corrente; os Ferreiros Ressequidos, seus aprendizes; as Fagulhas, a brasa que nunca apagou. |
| 📚 **Biblioteca do Esquecimento** | Silêncio | **O Arquivista** | Escreveu o nome verdadeiro do prisioneiro e **partiu a alma dele em duas**, jogando a Vontade no mundo. | Copiou tanto que perdeu o original de si mesmo: cria cópias, e só a verdadeira projeta sombra. Tomos, Sentinelas de Tinta e Escribas são registros que criaram vida. |
| 🌊 **Santuário das Marés** | Água | **A Carpideira** | Afogou o caminho de volta para o Selo. | Não chorava os afogados: chorava o que ajudou a afogar. O Coro canta com ela; Caranguejos e Águas-vivas são o recife sobre o túmulo. |
| 🌳 **Jardim Petrificado** | Terra | **Raiz-Mãe** | Cresceu as raízes que ancoram as correntes. O Titã Ancestral é semente dela. | Petrificou para não soltar: fecha a casca e aguenta. Brotos, Golems de Musgo e Vespas de Âmbar defendem o jardim. |
| 🌪️ **Torre dos Ventos Quebrados** | Ar | **O Arauto** | Levou o chamado que uniu os cinco ladrões. | A torre quebrou quando a Fome tentou calar o chamado. O Arauto voa em círculos, sem ter a quem chamar. Harpias, Totens e Elementais de Brisa são o vento preso ali. |
| 💀 **Catacumba Sussurrante** | Silêncio | **Caçador de Ecos** | **O único fiel**: servo do Primeiro Coro, enterrado vivo quando o Selo fechou. | Continua caçando Ecos para o mestre, inclusive Ecos serenos (é o único que ataca a Fauna Pacífica). Ossadas, Sombras e Lamentadores são Ecos que ele não terminou de levar. |

Os mobs de santuário não se atacam entre si porque servem ao mesmo lugar, não a uma corte.

---

## 🗺️ Lugares colossais

Onde ficam as partes roubadas, a realidade falha. Estes são os lugares da história. **Os 6 Santuários já existem no jogo, com a própria falha da realidade** (correntes e bigornas paradas no ar na Forja, tinta que sobe e sombras sem corpo na Biblioteca, chuva horizontal e lágrimas paradas nas Marés, chão que respira no Jardim, torre fatiada e sino que silencia na Torre, chama negra na Catacumba; ver GAMEPLAY.md). Os tronos e a arena abaixo também já existem (ver GAMEPLAY.md, "Lugares colossais"); cada trono nasce com o seu Monarca, e o Soberano só aparece no Anfiteatro para quem já partiu o Selo. Os nomes já estão em `lang` como `structure.solomod.<id>`.

| Lugar (`<id>`) | De quem | A falha da realidade |
|---|---|---|
| Cidadela do Pêndulo Invertido (`inverted_pendulum_citadel`) | Colosso Rei | Arquitetura pendurada no teto, detritos parados no ar e o molde vazio de um braço direito colossal. |
| Fornalha Cardíaca (`cardiac_furnace`) | Rei Vulcânico | Vulcão suspenso, lava que cai para cima e um batimento sísmico a cada 10 s. |
| Fossa do Céu Afogado (`drowned_sky_trench`) | Leviatã Coroado | Um cubo seco no fundo do mar, paredes de água contida, navios flutuando e marcas de garra tentando sair do chão. |
| Berço das Horas de Âmbar (`amber_hours_cradle`) | Titã Ancestral | Árvore petrificada com raízes para o céu e esferas de âmbar com criaturas da Era II congeladas. |
| Panteão da Tempestade Estática (`static_tempest_pantheon`) | Imperador dos Céus | Um furacão congelado, pontes de relâmpago cristalizado e as queimaduras onde as asas foram cortadas. |
| Anfiteatro do Selo Partido (`shattered_seal_amphitheater`) | O Soberano | Teatro cirúrgico de obsidiana suspenso no vácuo por cinco correntes rompidas, céu costurado com bordas de glitch. |

---

## 📖 Glossário

| Termo | Significado |
|---|---|
| **Canção** | A música que sustenta a física do mundo. |
| **Eco** | O que fica da Canção quando algo com vontade morre. Tem memória e Voz. |
| **Voz** | O tom de um Eco: Água, Fogo, Ar, Terra (os elementos do jogo). |
| **Silêncio** | O intervalo entre as Vozes; o "sem elemento" (Neutro/Arcano). |
| **Primeiro Coro** | O Soberano antes do roubo: guardião de todas as Vozes. |
| **O Roubo** | A traição dos cinco Monarcas, que esquartejaram o Primeiro Coro. |
| **Fome** | A metade do Soberano trancada no Selo. |
| **Vontade** | A metade do Soberano jogada no mundo: o jogador. |
| **Desperto Sem Voz** | O jogador, a Vontade reencarnada. |
| **Eco de violência** | Eco de quem morreu lutando. Vira monstro; é o que o Desperto absorve como XP. |
| **Eco sereno** | Eco de quem morreu em paz (muitos da Era I). Pode tomar forma como Fauna Pacífica. |
| **Ressonância** | A ligação entre o Desperto e um Eco sereno (companheiro ativo). |
| **Corte** | O séquito de um Monarca: Lacaios, Elites e Guardiões. |
| **Monarca** | Um dos cinco ladrões; carrega um membro e uma Voz roubados. |
| **Selo** | A cela da Fome, feita pelos cinco cúmplices. |
| **Fúria** | A Voz roubada tentando fugir de volta para o dono (a 25% de vida). |
| **Santuário de Eco** | Cena do crime: o lugar de um cúmplice (ou do servo fiel). |
| **Forma Astral** | Estado de um Eco sereno que se desfez e vai se refazer junto ao Desperto. |
| **Dissonância Absoluta** | O rasgo na Canção causado pela morte do Soberano. |
| **Trono Vazio** | A Era VII: a Canção sem guardião e algo de fora escutando. |

---

## 🧭 Regras do cânone (para conteúdo novo)

Antes de criar um mob, chefe, item ou mudança de regra, responda às perguntas abaixo. Se a resposta contradiz algo desta página, ou mude a ideia ou mude esta página no mesmo PR.

1. **Qual a Voz?** Todo ser tem uma das quatro Vozes ou o Silêncio. Uma Voz nova só vem de **fora da Canção** (Era VII).
2. **Que tipo de Eco é?**
   - de violência → hostil, dá XP Global, pode ter tier e corte;
   - sereno → pacífico, ignorado pelos monstros, não dá XP, entra na tag `#solomod:passive_echo`;
   - cúmplice do Selo (ou servo fiel) → mob de Santuário (fora das cortes, sem perfil de tier).
3. **De que lado do Roubo ele está?** Ladrão (Monarcas e cortes), cúmplice (Santuários), fiel ao Primeiro Coro (Caçador, Ecos serenos) ou de **fora** (Era VII).
4. **Por que ele está onde aparece?** Bioma, estrutura ou corte precisam combinar com a Voz e o lado.
5. **O jogador continua sem Voz.** Nada dá elemento às armas do jogador. Poder novo vem de **memória** (árvore), **corpo** (Maestria) ou **ressonância** (companheiro), nunca de uma Voz roubada.
6. **Só Ecos de violência dão XP.** Se um mob novo der XP, ele é um Eco de violência.
7. **Fauna Pacífica nunca é alvo dos monstros.** A única exceção é quem caça Ecos (hoje, só o Caçador de Ecos). Uma nova exceção precisa de razão na lore.
8. **Os Monarcas são cinco e caíram.** Um "Monarca" novo só pode ser um pretendente ao Trono Vazio (Era VII), nunca retroativo.
9. **O jogador é a Vontade do Soberano.** Nenhum conteúdo pode contradizer isso; no máximo, revelar aos poucos.
10. **Nomes.** Em pt-BR, descritivos e com artigo nos títulos únicos ("O Arquivista", "A Carpideira"). Mantenha os nomes já usados no jogo e traduza para `en_us.json` junto.

---

## 🧵 Ganchos em aberto

Fios que a história deixa soltos de propósito, para conteúdo futuro:

- **O lado de fora.** O que vive fora da Canção, e o que quer agora que a porta está aberta?
- **O Trono Vazio.** Sem o Soberano, ninguém segura a Canção. O Desperto, inteiro pela primeira vez, pode ser o próximo guardião, ou o próximo a cair.
- **A Sexta Voz.** "[ERRO: SEXTA VOZ DETECTADA]": uma Voz que não existe na Canção. O Fragmento de Dissonância é a primeira prova dela.
- **O nome verdadeiro.** O Arquivista escreveu o nome do Primeiro Coro. Quem mais o leu?
- **O servo fiel.** O Caçador de Ecos reconhece o mestre no Desperto. Pode virar aliado?
- **Outras dimensões.** O Nether e o End seriam pedaços da Canção que nunca foram cantados, ou já são o lado de fora?
