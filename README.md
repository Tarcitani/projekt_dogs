<div align="center">

# ⚔️ Solo Mod

### Um RPG de ação dentro do Minecraft

**Maestria · Armas Modulares · Combate Elemental · Chefes com IA própria**

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-62B47A?style=for-the-badge&logo=minecraft&logoColor=white)
![NeoForge](https://img.shields.io/badge/NeoForge-21.1.x-E04E14?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Status](https://img.shields.io/badge/status-MVP-8A2BE2?style=for-the-badge)

[Sobre](#-sobre) · [Destaques](#-destaques) · [Começando](#-começando) · [Documentação](#-documentação) · [Comandos](#-comandos-de-teste) · [Colaborar](#-colaborando)

</div>

---

## 📖 Sobre

O **Solo Mod** transforma o Minecraft num RPG de progressão. O jogador fica mais forte distribuindo pontos de **Maestria**, e as armas valem pelo que **são**: de que tier de monstro vieram, que elemento carregam e que habilidade guardam.

Uma arma poderosa não serve a qualquer um. Sem o stat exigido, o elemento e a habilidade ficam **adormecidos** e ela bate como uma arma comum. Para despertá-la, é preciso caçar monstros mais fortes, ganhar XP, subir de Maestria e acertar o elemento certo no inimigo certo.

Os inimigos acompanham: cada **tier** de monstro tem corpo, IA e ataques próprios. Lacaios que caçam em bando, Elites que aparam golpes, Guardiões que teleportam e Monarcas com três fases que **leem o seu estilo de luta**.

E acima deles, o **Soberano**: há eras os cinco Monarcas se uniram para derrotá-lo, e ele voltou com o poder de todos. É o chefe final desta versão de testes.

> 🧪 **Status:** MVP jogável. Os sistemas centrais estão prontos; há uma tela de testes (tecla **K**) e o comando `/modtest` para experimentar tudo no modo criativo.

## ✨ Destaques

<table>
<tr>
<td width="50%" valign="top">

### 📊 Maestria
Quatro atributos (**FOR · AGI · INT · VIT**) que mudam dano, velocidade, mana e vida. Pontos vêm do **XP Global**, ganho só com monstros do mod.

</td>
<td width="50%" valign="top">

### 🗡️ Armas Modulares
**7 classes** (espadão, machado, espada longa, adaga, rapieira, kunai, varinha) × **4 tiers** × **5 elementos**. Cada arma tem habilidade própria: arremessar a adaga e chamá-la de volta, girar a claymore em 360°, mergulhar com o machado.

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

</td>
</tr>
<tr>
<td valign="top">

### 👑 O Soberano
Chefe final gigante, feito de pedaços dos cinco Monarcas. **6 estágios**: a cada um ele agoniza num **close cinemático** e desperta um ataque novo; no último entra em **Fúria até morrer**. Derrotá-lo encerra o mod com **créditos**.

</td>
<td valign="top">

### 🎬 Espetáculo e Conquistas
Números de dano flutuantes, partículas por habilidade, animações, brilho emissivo, **cinemáticas** dos chefes e aba de **Conquistas do Minecraft** com desafios narrativos (e duas secretas).

</td>
</tr>
<tr>
<td valign="top">

### 🧰 Feito para testar
Tecla **K**: distribua pontos ou gere qualquer arma ou monstro com um clique. Comando `/modtest` para controle total.

</td>
<td valign="top">
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

1. Entre em um mundo **Singleplayer Criativo** com **Cheats Ativados** (Permite comandos).
2. Pressione a tecla **K** para abrir a **Interface de Maestria** e navegue até a aba **Teste**.
3. Selecione: *Classe: Adaga*, *Tier: S. Chefe*, *Elemento: Fogo* → Clique em **Gerar arma na mão**.
4. Repare que o HUD indica que ela está **Adormecida** (exige Agilidade).
5. Invoque um mob de *Ar* pelo botão da aba Teste e ataque-o para ver o dano neutro/básico.
6. Clique três vezes no botão **+10 AGI** e ataque novamente: note os números flutuantes alaranjados com **"!"** e o mob recebendo o efeito elemental! 🔥

## 📚 Documentação

| Documento | Conteúdo |
|---|---|
| 🎮 [**GAMEPLAY.md**](docs/GAMEPLAY.md) | Regras de jogo: stats, elementos, tiers, armas, habilidades, IA dos mobs, Fúria, roteiros de teste |
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
├── entity/      criaturas e adaga arremessada
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
| `/modtest weapon <classe> <tier> [elemento] [requisito] [habilidade]` | cria a arma na mão |
| `/modtest mob <alvos> <tier> <elemento>` | transforma mobs vanilla em mobs do mod |
| `/modtest unmark <alvos>` | remove o perfil do mob |
| `/modtest spawn <tier> <elemento>` | invoca a criatura própria do tier |
| `/modtest sovereign [estágio]` | invoca o Soberano (chefe final), opcionalmente já no estágio 1–6 |

<sub>Classes: `greatsword` `axe` `longsword` `dagger` `rapier` `kunai` `wand` · Tiers: `normal` `op` `sub_boss` `boss` · Elementos: `none` `fire` `water` `earth` `air`</sub>

## 🤝 Colaborando

1. `git pull` na `main`, depois `git checkout -b feat/minha-tarefa`.
2. Programe e teste com `./gradlew runClient`.
3. `git push` e abra um **Pull Request**.

Detalhes, convenções e as áreas com risco de conflito: **[CONTRIBUTING.md](CONTRIBUTING.md)**.

> ⚠️ `MobMeshes.java` e as texturas de `textures/entity/` são **gerados** pelos scripts de `tools/`. Edite o script, não o arquivo.

---

<div align="center">
<sub>Feito com ☕ e muitos blocos por <b>Tarcitani</b> e amigos · NeoForge 1.21.1</sub>
</div>
