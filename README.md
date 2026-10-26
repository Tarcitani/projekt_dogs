<div align="center">

# ⚔️ Solo Mod

### Um RPG de ação dentro do Minecraft

**Maestria · Armas Modulares · Combate Elemental · Chefes com IA própria · Santuários · Fauna Pacífica**

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-62B47A?style=for-the-badge&logo=minecraft&logoColor=white)
![NeoForge](https://img.shields.io/badge/NeoForge-21.1.x-E04E14?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Status](https://img.shields.io/badge/status-MVP-8A2BE2?style=for-the-badge)

[Sobre](#-sobre) · [Lore](#-a-história) · [Destaques](#-destaques) · [Bestiário](#bestiário) · [Começando](#️-como-instalar-para-desenvolver-setup-do-ambiente) · [Documentação](#-documentação) · [Comandos](#-comandos-de-teste) · [Colaborar](#-colaborando)

</div>

---

## 📖 Sobre

O **Solo Mod** transforma o Minecraft num RPG de progressão. O jogador fica mais forte distribuindo pontos de **Maestria**, e aprende habilidades numa árvore compartilhada pelas quatro armas normais: rapieira, espada longa, adaga e martelo.

As armas são sempre Normal e sem elemento. Cumprir o requisito de Maestria libera o escalonamento de dano; habilidades são aprendidas na árvore e gastam mana. Caçar monstros concede XP para evoluir atributos e desbloqueios.

Os inimigos acompanham: cada **tier** de monstro tem corpo, IA e ataques próprios. Lacaios que caçam em bando, Elites que aparam golpes, Guardiões que teleportam e Monarcas com três fases que **leem o seu estilo de luta**.

E acima deles, o **Soberano**: aquele de quem os cinco Monarcas roubaram tudo. Cada Monarca derrotado devolve uma parte dele, e no fim ele desperta inteiro. É o chefe final desta versão de testes, com **6 estágios dublados por falas** e uma morte que quebra a realidade.

Fora da história principal, o mundo guarda **seis Santuários de Eco**: masmorras opcionais com mini-chefes e guarnição própria. E nem tudo é hostil: **15 criaturas da Fauna Pacífica** podem ser vinculadas como companheiras de suporte.

> 🧪 **Status:** MVP jogável. Os sistemas centrais estão prontos; há uma tela de testes (tecla **K**) e o comando `/modtest` para experimentar tudo no modo criativo.

### Efeitos e árvore unificada

Movimentação e saque usam as animações vanilla. As animações de combate das habilidades, da guarda e do rolamento continuam ativas.

Os efeitos das armas combinam geometria em pixels com flipbooks originais: selos giratórios, agulhas múltiplas, rastros de lâmina, clarões e impactos fragmentados. A rapieira tem carga, dash, rajada, recuo e técnica com escudo com efeitos próprios. As quatro técnicas do Z agora enviam sequências visuais sincronizadas; os efeitos não mudam dano nem hitbox. Cliente/servidor usam protocolo **7**.

**K → Habilidades** agora reúne a árvore e os quatro atributos ao redor do jogador. Selecione Força, Agilidade, Inteligência ou Vitalidade e clique em **+1 atributo** (Shift + clique: até +5). Pontos de atributos e de habilidades continuam separados e aparecem no topo. Habilidades e atributos têm ícones pixel art próprios. Gere a arte com `python tools/gen_player_presentation.py --preview`; prévias em `build/previews/talent_icons.png` e `combat_flipbooks.png`.

### Escudo e protótipos pixel art 3D

Na aba criativa **Solo Mod: Armas**, o **Escudo de Aparada** (`solomod:parry_shield`) funciona na mão principal ou secundária, inclusive para canhotos. Levante com clique direito mirando o ataque do chefe: os primeiros **0,3 s** permitem um parry que anula o dano e deixa o chefe em recuperação por **1,5 s**. Durante Fúria/transições o golpe é defendido, mas o chefe não é interrompido. Segurar além da janela mantém o bloqueio comum; levantar/soltar/aparar inicia uma recarga de **1,2 s** para levantar de novo.

Com **qualquer escudo equipado**, as técnicas comuns, R e Shift + direito ficam bloqueados. **Z** usa uma única técnica por arma: **Corte de Baluarte** (espada), **Estocada de Guarda** (rapieira), **Picada de Recuo** (adaga) ou **Quebra-Guarda** (martelo). Funciona com escudo/arma em qualquer ordem de mãos, exige Maestria, mana e recarga e baixa a guarda durante o golpe. Z está livre no vanilla 1.21.1 e pode ser remapeado em Controles; HUD/tooltips mostram a tecla configurada. Retire o escudo para voltar às habilidades da árvore. Cliente e servidor usam o protocolo **7**.

A aba criativa **Solo Mod: Pixel Art 3D** traz rapieira, espada longa, adaga e martelo experimentais (`solomod:pixel_rapier`, `pixel_longsword`, `pixel_dagger`, `pixel_hammer`) no estilo de fantasia em camadas da Juramento do Eco, inspirado em Blades of Majestica. Usam aço biselado, canais ciano e bronze: rapieira fina com proteção de mão, espada longa com guarda em ganchos e martelo maciço com núcleo encaixado. A adaga reconstrói a silhueta e as proporções da [Twilight Dagger de Minecraft Dungeons II](https://dungeons2.minecraft-yutakura.com/en/weapons/equipment-0032/), com lâmina curva estreita, guarda curta e cabo inclinado; os materiais seguem a família do mod. Atlas 32×64 e espessuras reais por peça; ícones GUI 32×32. Atributos, talentos e recargas continuam os das mesmas classes normais. As armas originais permanecem na aba Armas. Gere a arte com `python tools/gen_weapon_models.py`; `python tools/majestica_weapons.py --preview` salva a prancha em `build/previews/majestica_weapons.png`. O atalho anterior `python tools/pixel_equipment.py --preview` também continua funcionando.

**Uma arma adicional para comparação:** **Juramento do Eco** (`solomod:echo_oathblade`), na mesma aba Pixel Art 3D. É uma espada longa de fantasia em camadas, inspirada visualmente na galeria [Blades of Majestica, de Eftann](https://realm-of-majestica.webflow.io/), com arte e modelo originais: fio biselado, canal ciano, guarda assimétrica de bronze e cabo escuro. Usa atributos, talentos, Z com escudo e recargas compartilhadas da espada longa. Para obter diretamente: `/give @s solomod:echo_oathblade`. Gerador: `python tools/gen_oathblade.py --preview` (também integrado ao gerador geral).

## 📜 A história

Ao entrar no mundo, cada jogador recebe **Ecos do Despertar**, um manuscrito anônimo de oito páginas sobre a Canção e os Ecos. A narrativa deixa lacunas e termina em suspense; mecânicas, locais, objetivos e revelações futuras ficam para o jogador descobrir. A entrega é única por jogador/mundo, inclusive em saves existentes; inventário cheio aguarda espaço. Cópias da antiga edição são atualizadas no inventário, sem entregar outro exemplar. Páginas em pt-BR/en-US acompanham o idioma do cliente.

O livro tem capa pixel art própria: couro escuro, cantoneiras de bronze, marcador e selo de Eco ciano. As cópias já existentes recebem a aparência automaticamente, inclusive após renomear/copiar o livro. Gere com `python tools/gen_echo_guide.py --preview` (também integrado a `gen_weapon_models.py`); prévia em `build/previews/echo_guide.png`.

> *Tudo o que vive canta. Quando algo morre, a sua canção fica no mundo como um **Eco**.*

Os Ecos cantam em quatro **Vozes** (Água, Fogo, Ar e Terra) ou no **Silêncio** entre elas, e juntos sustentam a **Canção**, a física do mundo. No começo, um único ser guardava todas: o **Primeiro Coro**. Os cinco **Monarcas** o traíram, esquartejaram o corpo dele ainda vivo e fundaram cinco impérios com os membros e as Vozes roubados. O que sobrou enlouqueceu de **Fome** e foi trancado no **Selo** por cinco cúmplices, os guardiões dos Santuários. Hoje o chamam de **Soberano**.

Você é o **Desperto Sem Voz**, e não por acaso: para que o Soberano não abrisse o Selo por dentro, o Arquivista partiu a alma dele em duas e jogou a **Vontade** no mundo. **Você é essa Vontade.** Por isso não tem elemento, absorve os Ecos que derrota (XP, Maestria) e lembra golpes que nunca aprendeu (a árvore de habilidades). Cada Monarca que você mata devolve uma parte roubada ao Selo, até a Fome despertar inteira. O confronto final é a **Dissonância Absoluta**, e a morte do Soberano rasga a Canção.

➡️ A lore completa, a linha do tempo e as regras para criar conteúdo novo sem contradizer a história: **[docs/LORE.md](docs/LORE.md)**

## ✨ Destaques

<table>
<tr>
<td width="50%" valign="top">

### 📊 Maestria
Quatro atributos (**FOR · AGI · INT · VIT**) que mudam dano, velocidade, mana e vida. Pontos vêm do **XP Global**, ganho só com monstros do mod.

</td>
<td width="50%" valign="top">

### 🗡️ Armas Modulares
**4 classes no catálogo**: rapieira, espada longa, adaga e martelo, todas com **ícones pixel art nos menus e modelos 3D em cubos na mão**. Aba criativa **Solo Mod: Armas** com uma de cada, sem duplicatas; a aba Teste gera somente as armas normais; tier e elemento ficam exclusivos dos mobs. Carregue o dash da rapieira com rastros de velocidade, alterne três técnicas da espada longa, escolha entre chamar a adaga de volta ou teleportar num relâmpago dourado e mergulhe com o martelo: mais altura gera mais dano e blocos erguidos no impacto.

</td>
</tr>
<tr>
<td valign="top">

### 🔥 Combate Elemental
**Água › Fogo › Ar › Terra › Água**. Vantagem dá **1.5x** de dano e aplica a passiva (queimar, desacelerar...). Errar o elemento corta o dano pela metade.

</td>
<td valign="top">

### 👹 Monstros por Tier
**Lacaio → Elite → Guardião → Monarca**, cada um com IA própria, ataques telegrafados e **Fúria** a 25% de vida. **20 criaturas** com modelo e textura exclusivos por elemento.

Cada Monarca usa os poderes do seu elemento que aparecem no Soberano: **Genki-dama de fogo**, **Maremoto e Esferas de Água**, **Raízes, Pisão e Pulão de terra** e **Tornados de ar**. O Colosso Rei usa o Punho Sísmico. Os poderes continuam disponíveis na Fúria. O **Rei Vulcânico** também mantém bolas de fogo rebatíveis e investidas que deixam magma (`mobGriefing`). As conquistas de derrota usam o **rosto do respectivo chefe em pixel art**.

</td>
</tr>
<tr>
<td valign="top">

### 👑 O Soberano
Chefe final gigante, feito de pedaços dos cinco Monarcas. **6 estágios**: a cada um ele agoniza num **close cinemático** e desperta o poder de um Monarca; nos estágios 1–5 usa só o poder correspondente. No último entra em **Fúria até morrer**, reúne todos os poderes e ganha o **raio do peito**, após o qual fica ajoelhado, cansado. Adapta-se a **vários jogadores**. Derrotá-lo encerra o mod com **créditos**.

</td>
<td valign="top">

### 🎬 Espetáculo e Conquistas
Números de dano flutuantes, partículas por habilidade, animações, brilho emissivo, **cinemáticas** dos chefes e aba de **Conquistas do Minecraft** com desafios narrativos (e duas secretas).

</td>
</tr>
<tr>
<td valign="top">

### 🏚️ Santuários de Eco
**6 masmorras opcionais** geradas no mundo, cada uma com paleta, guarnição e um **mini-chefe** de mecânica única: as cópias sem sombra do Arquivista, a casca da Raiz-Mãe, a emboscada invisível do Caçador de Ecos. **24 mobs** com barra de vida, 3 fases, golpes telegrafados e saque próprio. A Catacumba é **enterrada**, com poço de escada até a superfície.

</td>
<td valign="top">

### 🌿 Fauna Pacífica
**15 companheiros de suporte**, 3 por linhagem (Silêncio, Fogo, Água, Terra, Ar). Vincule com um item, mantenha **um ativo** por vez e deixe-o reagir aos ataques dos chefes: desviar orbes, segurar empurrões, anular quedas. Os monstros os ignoram e eles **não morrem** de dano do ambiente (Forma Astral).

</td>
</tr>
<tr>
<td valign="top">

### 🧰 Feito para testar
Tecla **K**: distribua pontos ou gere qualquer arma ou monstro com um clique. Comando `/modtest` para controle total. Todos os ovos ficam na aba criativa **Solo Mod**.

</td>
<td valign="top">

### 📜 Lore conectada
Elementos, tiers, Monarcas, Soberano, Fauna e Santuários seguem uma única história, com regras do cânone para os próximos mobs. Veja [docs/LORE.md](docs/LORE.md).

</td>
</tr>
</table>

### Bestiário

| | Sem elemento | 🔥 Fogo | 💧 Água | 🪨 Terra | 🌪️ Ar |
|---|---|---|---|---|---|
| **• Lacaio** | Diabrete | Diabrete Ígneo | Diabrete Abissal | Diabrete de Pedra | Sílfide Travessa |
| **★ Elite** | Duelista Espectral | Executor Infernal | Tritão Corsário | Sentinela de Granito | Valquíria do Vento |
| **✦ Guardião** | Oráculo | Arauto Solar | Medusa Abissal | Monólito Ancestral | Olho da Tempestade |
| **♛ Monarca** | Colosso Rei | Rei Vulcânico | Leviatã Coroado | Titã Ancestral | Imperador dos Céus |
| **☬ Chefe final** | Soberano (todos os elementos) | | | | |

#### Santuários de Eco

| Santuário | Onde | Mini-chefe | Guarnição |
|---|---|---|---|
| 🔥 Forja Banhada em Cinzas | desertos, badlands, savanas | Cinzagrim, o Ferreiro | Escória Viva, Ferreiro Ressequido, Fagulha da Forja |
| 📚 Biblioteca do Esquecimento | planícies, florestas | O Arquivista | Sentinela de Tinta, Escriba Espectral, Tomo Voador |
| 🌊 Santuário das Marés | praias, pântanos, mangues | A Carpideira | Afogado do Coro, Caranguejo de Coral, Água-viva de Coral |
| 🌳 Jardim Petrificado | taigas, selvas | Raiz-Mãe | Broto Espinhoso, Golem de Musgo, Vespa de Âmbar |
| 🌪️ Torre dos Ventos Quebrados | colinas, picos | O Arauto | Harpia Cinzenta, Totem do Vendaval, Elemental de Brisa |
| 💀 Catacumba Sussurrante | enterrada (planícies, florestas, taigas) | Caçador de Ecos | Ossada Sussurrante, Sombra Rastejante, Lamentador |

#### Lugares colossais
Cinco tronos gigantes, um por Monarca (com ele esperando lá dentro), e o **Anfiteatro do Selo Partido** no vazio do End, onde o Soberano aparece para quem já derrubou os cinco. Detalhes em [GAMEPLAY](docs/GAMEPLAY.md#-lugares-colossais-tronos-e-arena).

#### Fauna Pacífica

| 🔮 Silêncio | 🔥 Fogo | 💧 Água | 🪨 Terra | 🌪️ Ar |
|---|---|---|---|---|
| Batedor Espectral | Salamandra de Núcleo Ígneo | Axolote de Mana Líquida | Mini-Tank Silvestre | Arraia das Nuvens |
| Morcego Eco-Rúnico | Escaravelho de Sílica Dourada | Caranguejo-Tartaruga de Lótus | Golem Pedregulho Rúnico | Coruja Tempestuosa de Sentinela |
| Autômato Relíquia | Poldro de Cinzas Vulcânicas | Rêmora de Fossa Abissal | Gazela das Raízes de Âmbar | Mariposa da Fenda Dimensional |

➡️ Todas as regras, números e ataques: **[docs/GAMEPLAY.md](docs/GAMEPLAY.md)**

## 🛠️ Como Instalar para Desenvolver (Setup do Ambiente)

Guia passo a passo para configurar o ambiente de desenvolvimento em minutos.

### 1. Pré-requisitos

| Ferramenta | Versão Recomendada | Instruções / Download |
|---|---|---|
| ☕ **Java (JDK)** | **JDK 21** | Instale o [Eclipse Temurin 21](https://adoptium.net/) ou deixe a Toolchain do Gradle baixar automaticamente via Foojay resolver. |
| 🌿 **Git** | Recente | [git-scm.com](https://git-scm.com/) |
| 💻 **IDE** | **IntelliJ IDEA** (recomendado) ou **VS Code** | Veja as instruções de cada IDE abaixo. |
| 🐍 **Python + Pillow** | 3.10+ *(opcional)* | Necessário **apenas** se for alterar os scripts geradores de texturas/modelos em `tools/`: `pip install pillow` |

> 💡 **Nota:** Você **não precisa** ter Minecraft nem Gradle instalados globalmente. O wrapper incluído (`./gradlew` ou `gradlew.bat`) e o plugin NeoForge baixam o Forge, as dependências e os assets do Minecraft sozinhos.

---

### 2. Clonando o Repositório

Abra seu terminal / PowerShell e clone o projeto:

```bash
git clone https://github.com/Tarcitani/projekt_dogs.git
cd projekt_dogs
```

---

### 3. Configurando a sua IDE

#### Opção A: IntelliJ IDEA (Recomendado)
1. Abra o IntelliJ IDEA e selecione **Open** (ou *File → Open*).
2. Selecione a pasta raiz `projekt_dogs` (onde está o arquivo `build.gradle`).
3. O IntelliJ detectará o Gradle automaticamente e iniciará a indexação e download dos pacotes.
4. *(Opcional)* Instale o plugin **Minecraft Development** no IntelliJ (*Settings → Plugins*).
5. As tarefas de execução do NeoForge (*Client*, *Server*) aparecerão automaticamente no menu de Run/Debug no canto superior direito.

#### Opção B: Visual Studio Code
1. Tenha instalado o pacote **Extension Pack for Java** e a extensão **Gradle for Java**.
2. Abra a pasta do projeto no VS Code: `code .`
3. Aguarde o VS Code e o plugin do NeoForge sincronizarem as dependências.
4. Um arquivo de inicialização `.vscode/launch.json` será gerado automaticamente para você debugar o Client e Server diretamente pelo painel *Run & Debug* (F5).

---

### 4. Rodando o Mod em Desenvolvimento

Você pode rodar diretamente pelo terminal ou pelos botões de Play da sua IDE:

```bash
# No Linux / macOS:
./gradlew runClient      # Abre o Minecraft com o Solo Mod carregado
./gradlew runServer      # Inicia o servidor dedicado de testes (--nogui)

# No Windows (PowerShell / CMD):
.\gradlew.bat runClient
.\gradlew.bat runServer
```

> ⏳ *A primeira execução pode levar de 3 a 7 minutos*, pois o NeoForge irá descompactar e mapear o código do Minecraft vanilla automaticamente.

---

### 5. Compilando o Pacote Final (.jar)

Para gerar o arquivo `.jar` distribuível do mod:

```bash
./gradlew build        # Windows: .\gradlew.bat build
```
O arquivo compilado estará disponível em: `build/libs/solomod-0.1.0.jar`.

---

### 6. Teste Rápido no Jogo (2 minutos)

1. Entre em um mundo **Singleplayer Criativo** com **Cheats Ativados** (permite comandos).
2. Pressione **K**, vá à aba **Teste** e gere uma **Adaga** na mão (as armas são sempre Normais e sem elemento).
3. Invoque um mob pela aba Teste e ataque-o: sem o requisito de Agilidade, o golpe não escala com a Maestria.
4. Clique em **+10 AGI** e ataque de novo: os números flutuantes mostram o dano maior.
5. Pegue um ovo da **Fauna Pacífica** na aba **Solo Mod**, ofereça o item de vínculo e veja o companheiro seguir você.
6. Rode `/place structure solomod:ash_forge` (ou outro santuário) e enfrente a guarnição e o Cinzagrim no modo Sobrevivência. 🔥

## 📚 Documentação

| Documento | Conteúdo |
|---|---|
| 📜 [**LORE.md**](docs/LORE.md) | A história do mod: Eco e Vozes, Monarcas, Soberano, Fauna Pacífica, Santuários e as regras do cânone para conteúdo novo |
| 🎮 [**GAMEPLAY.md**](docs/GAMEPLAY.md) | Regras de jogo: stats, elementos, tiers, armas, habilidades, IA dos mobs, Fúria, Fauna Pacífica, Santuários, roteiros de teste |
| 🏗️ [**ARCHITECTURE.md**](docs/ARCHITECTURE.md) | O que é cada arquivo, diagramas de fluxo, pacotes de rede, recursos e scripts |
| 🤝 [**CONTRIBUTING.md**](CONTRIBUTING.md) | Fluxo de Git, branches, convenções, checklist de PR |
| 🤖 [**AGENTS.md**](AGENTS.md) | Guia para IAs: regras obrigatórias, receitas passo a passo e armadilhas |

### Estrutura em 10 segundos

```
src/main/java/com/tarcitani/solomod/
├── core/        enums do domínio (elemento, tier, stat, classe de arma)
├── component/   dados da arma no item
├── attachment/  dados do jogador e dos mobs
├── item/        armas, ovos e aba criativa
├── event/       combate e balanceamento
├── skill/       habilidades das armas
├── entity/      criaturas, adaga arremessada, passive/ (Fauna Pacífica) e sanctuary/ (mobs dos Santuários)
├── worldgen/    estruturas dos Santuários de Eco
├── mob/         IA dos monstros por tier
├── network/     pacotes cliente ⇄ servidor
├── command/     /modtest
└── client/      tela, HUD, animações, modelos e renderers
tools/           scripts Python que geram texturas, modelos e MobMeshes.java
```

## 🧪 Comandos de teste

Todos exigem OP (nível 2), ou seja, cheats ligados no singleplayer.

| Comando | Efeito |
|---|---|
| `/modtest stats` | mostra stats, XP, pontos e mana |
| `/modtest set <for\|agi\|int\|vit> <valor>` | define um stat direto |
| `/modtest spend <stat> <qtd>` | gasta pontos disponíveis |
| `/modtest addxp <qtd>` | soma XP Global (1 ponto a cada 100) |
| `/modtest points <qtd>` | define os pontos disponíveis |
| `/modtest mana` | enche a mana |
| `/modtest reset` | zera tudo |
| `/modtest weapon <classe>` | cria uma das quatro armas normais, sem elemento |
| `/modtest mob <alvos> <tier> <elemento>` | transforma mobs vanilla em mobs do mod |
| `/modtest unmark <alvos>` | remove o perfil do mob |
| `/modtest spawn <tier> <elemento>` | invoca a criatura própria do tier |
| `/modtest sovereign [estágio]` | invoca o Soberano (chefe final), opcionalmente já no estágio 1–6 |
| `/modtest era <true\|false>` | liga/desliga a Era VII (pós-jogo) sem lutar: Cicatriz no Céu e reverência do Caçador |
| `/place structure solomod:<santuário>` | gera um Santuário de Eco onde você está (comando vanilla) |
| `/locate structure solomod:<santuário>` | mostra o Santuário mais próximo (comando vanilla) |

<sub>Santuários: `ash_forge` `forgotten_library` `tide_sanctuary` `petrified_garden` `broken_wind_tower` `whispering_catacomb`. Tronos e arena: `inverted_pendulum_citadel` `cardiac_furnace` `drowned_sky_trench` `amber_hours_cradle` `static_tempest_pantheon` `shattered_seal_amphitheater` (no End).</sub>

<sub>Classes atuais: `rapier` `longsword` `dagger` `hammer` · Tiers: `normal` `op` `sub_boss` `boss` · Elementos: `none` `fire` `water` `earth` `air`. Tiers e elementos são exclusivos dos mobs. Classes legadas permanecem registradas para carregar saves antigos.</sub>

No criativo, **Solo Mod: Armas** contém uma arma de cada classe atual; os ovos ficam em **Solo Mod**. Para conferir os novos ataques, use `/modtest spawn boss fire` (ou `water`, `earth`, `air`, `none`) e passe para Sobrevivência. Os ícones são regenerados por `python tools/gen_boss_icons.py` e os modelos das armas por `python tools/gen_weapon_models.py`.

As quatro armas são exclusivamente **Normal/sem elemento** (na lore, o Desperto não tem Voz), inclusive na aba Teste e em `/modtest weapon <classe>`. Armas antigas são normalizadas ao carregar; desbloqueios da árvore são preservados.

**Árvores de habilidades:** abra **K → Habilidades**: um mapa único no estilo das conquistas vanilla, com o rosto do jogador no centro, as quatro armas normais da aba Armas em círculo (rapieira, espada longa, adaga e martelo) e habilidades ramificadas como raízes. Não há filtros por arma; arraste para navegar e use a roda para zoom. Há 1 ponto inicial + 1 a cada 100 XP Global, separado dos atributos; todas as ativações de habilidades cobram mana (recolher a adaga é gratuito). Desbloqueios persistem no jogador. Habilidades avançadas aprendidas funcionam na arma normal por Shift + direito; a suprema substitui a anterior nesse atalho. Detalhes e custos em [GAMEPLAY](docs/GAMEPLAY.md#árvores-de-habilidades--k--habilidades).

**Apresentação:** os ícones de GUI das oito armas são pixel art detalhada 32×32, com aço, ouro e gemas, sem variantes elementais; os modelos 3D na mão e no mundo foram preservados. A barra amarela sob a mana mostra a recarga do rolamento. O XP Global preenche o contorno do escudo de nível; uma volta completa indica o próximo nível. Chefes novos e salvos não usam contorno branco.

Controles das armas (após aprender): **espada longa → R** alterna **Meia-Lua, Ascendente e Cruzado**, **botão direito** executa (esquerdo é ataque comum); **adaga → botão direito** para arremessar/retornar e **Shift + botão direito com ela arremessada** para teleportar até ela; **rapieira → segurar botão direito e soltar**, com distância aproximada de 3–14 blocos indicada no HUD; **martelo → botão direito** para salto no chão ou mergulho imediato no ar. A queda aumenta dano/raio até 24 blocos. A habilidade avançada aprendida fica em **Shift + botão direito** (na adaga, depois de recolhê-la). Animações e roteiro de teste em [docs/GAMEPLAY.md](docs/GAMEPLAY.md#habilidades-e-animações-das-quatro-armas).

**Duplo Ctrl → rolamento** na direção de WASD (sem direção, para a frente), com cambalhota, poeira, breve janela de esquiva e recuperação entre usos. Ctrl isolado mantém a corrida normal. O **Corte em Meia-Lua** agora tem preparação de duas mãos, poses dos braços e mangas, lâmina de luz 3D com cor elemental, clarões e som pesado no impacto.

**R também alterna as técnicas da rapieira e do martelo**, mantendo a escolha de cada arma: rapieira tem **Estocada Relâmpago**, **Chuva de Agulhas** (três estocadas) e **Passo do Vento** (estocada + recuo); martelo tem **Salto Esmagador**, **Giro Demolidor** e **Ruptura Sísmica** (onda frontal). Botão direito usa a escolhida; apenas o dash da rapieira requer segurar/soltar.

Velocidades base: **adaga 2,5**, **rapieira 2,2**, **espada longa 1,6** e **martelo 0,8 ataques/s**, antes de AGI e penalidades. A HUD agora é compacta, com **brasão rúnico, coroa, filigranas e trilhos de vida/mana/XP**: aproximadamente 164×62 pixels de GUI com arma. Ela substitui os corações vanilla e mostra também absorção, veneno/wither e dano; fome/armadura/oxigênio/montaria continuam normais. O painel **K** guarda os detalhes dos atributos e a técnica selecionada.

## 🤝 Colaborando

1. `git pull` na `main`, depois `git checkout -b feat/minha-tarefa`.
2. Programe e teste com `./gradlew runClient`.
3. `git push` e abra um **Pull Request**.

Detalhes, convenções e as áreas com risco de conflito: **[CONTRIBUTING.md](CONTRIBUTING.md)**.

> ⚠️ `MobMeshes.java` e as texturas de `textures/entity/` (incluindo `passive/` e `sanctuary/`) são **gerados** pelos scripts de `tools/` (`gen_mob_art.py`, `gen_passive_echo.py`, `gen_sanctuary.py`). Edite o script, não o arquivo.
>
> 📜 Mob, chefe ou estrutura novos devem seguir as **[regras do cânone](docs/LORE.md#-regras-do-cânone-para-conteúdo-novo)**.

---

<div align="center">
<sub>Feito com ☕ e muitos blocos por <b>Tarcitani</b> e amigos · NeoForge 1.21.1</sub>
</div>
