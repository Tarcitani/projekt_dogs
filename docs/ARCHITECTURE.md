# 🏗️ Arquitetura

Mapa técnico do Solo Mod: o que é cada arquivo, como as peças conversam e onde mexer. As regras de jogo (números, ataques, comportamentos) estão em [GAMEPLAY.md](GAMEPLAY.md).

> Pacote base: `src/main/java/com/tarcitani/solomod/` — os caminhos abaixo são relativos a ele.

---

## Visão geral

```mermaid
flowchart LR
    subgraph Dados
        WD["WeaponData<br/>(Data Component no ItemStack)"]
        PS["PlayerStatsAttachment<br/>(attachment no Player)"]
        MP["MobProfile<br/>(attachment no mob)"]
    end
    subgraph Servidor
        CE["event/CombatEvents"]
        EC["event/ElementalCombat"]
        SL["event/StatsLogic"]
        SK["skill/WeaponSkill<br/>skill/ClassAbility"]
        BR["mob/TierBrain + Brains"]
        HZ["mob/Hazards"]
    end
    subgraph Cliente
        HUD["client/SoloHud<br/>client/StatsScreen"]
        FX["DamageNumbers<br/>MobAnimations<br/>PlayerAnimations<br/>BossCinematic"]
        RD["TierMobRenderer<br/>+ modelos"]
    end
    WD --> CE
    PS --> CE
    MP --> CE
    CE --> EC
    CE --> SL
    SK --> EC
    MP --> BR
    BR --> HZ
    Servidor -- "network/*Payload" --> Cliente
    HUD -- "SpendPoints / TestAction / SpinAttack" --> Servidor
```

### Os três dados centrais

| Dado | Tipo | Onde vive | Para quê |
|---|---|---|---|
| `WeaponData` | `record` imutável (Data Component) | `ItemStack` da arma | tier, classe, stat exigido, valor exigido, elemento, `skillId` |
| `PlayerStatsAttachment` | classe **mutável** (Data Attachment) | entidade `Player` | FOR/AGI/INT/VIT, XP Global, pontos, mana |
| `MobProfile` | `record` (Data Attachment) | qualquer `LivingEntity` | tier + elemento. **Ter perfil = ser "mob do mod"** |

### Dois barramentos de eventos

- **Mod bus**: registros (DeferredRegister), pacotes de rede, setup de cliente. Ligado em `SoloMod.java` e em `client/ClientSetup` (`@EventBusSubscriber(bus = MOD)`).
- **Game bus** (`NeoForge.EVENT_BUS`): dano, morte, tick, comandos. Servidor/comum: registrado em `SoloMod.java` (`CombatEvents`, `PlayerEvents`, `MobEvents`, `ClassAbility`, `ModTestCommand`). Cliente: `@EventBusSubscriber(value = Dist.CLIENT, bus = GAME)`.

---

## Mapa de arquivos

### Raiz

| Arquivo | O que é |
|---|---|
| `SoloMod.java` | Entrada do mod (`@Mod`). Chama os `register(modBus)` e registra as classes de evento no game bus. **Toda classe de evento nova do servidor precisa ser registrada aqui.** |

### `core/` — enums do domínio

| Arquivo | O que é |
|---|---|
| `ElementType.java` | `NONE, WATER, EARTH, FIRE, AIR`, com cor e ciclo de vantagem (`multiplier`). |
| `MobTier.java` | `NORMAL, OP, SUB_BOSS, BOSS`: multiplicador de dano, bônus de stat, potência elemental, nível de habilidade, requisito padrão, XP concedido e cor. |
| `StatType.java` | `FOR, AGI, INT, VIT`. |
| `WeaponClass.java` | 7 classes (`GREATSWORD, AXE, LONGSWORD, DAGGER, RAPIER, KUNAI, WAND`): peso (`HEAVY/MEDIUM/LIGHT/THROWN/CHANNEL`), stat padrão, dano e velocidade base, habilidade de tier padrão. |

### `component/` — dados no item

| Arquivo | O que é |
|---|---|
| `WeaponData.java` | Record gravado no ItemStack. Imutável: use `withRequirement(...)` / `withSkill(...)` e grave com `stack.set(...)`. Tem `CODEC` (save) e `STREAM_CODEC` (rede). **Campo novo = atualizar os dois codecs.** `defaultFor(...)` monta uma arma padrão. |
| `ModDataComponents.java` | Registro do componente `WEAPON_DATA`. |

### `attachment/` — dados em entidades

| Arquivo | O que é |
|---|---|
| `PlayerStatsAttachment.java` | Stats, XP, pontos e mana do jogador. Constantes `XP_PER_POINT=100`, `BASE_MANA=20`, `MANA_PER_INT=5`, `MAX_STAT=999`. Depois de alterar, **sempre** chame `StatsLogic.onStatsChanged(player)`. |
| `MobProfile.java` | Record `(tier, element)` que marca um mob como "do mod". |
| `ModAttachments.java` | Registro de `PLAYER_STATS` (salvo com a entidade e copiado na morte; o sync com o cliente é **manual** via `SyncStatsPayload`) e `MOB_PROFILE` (só existe em mobs marcados: teste com `hasData`). |

### `advancement/` — conquistas

| Arquivo | O que é |
|---|---|
| `ModAdvancements.java` | Concessão das conquistas. `awardBossDefeat(player, element)` dá `defeat_monarch_<elemento>` e o critério `monarch_<elemento>` do desafio mestre `defeat_all_monarchs`; com os cinco, dá a secreta `broken_seal`. `awardSovereignDefeat(player)` dá a secreta `defeat_sovereign` (e o selo, se faltava). Os critérios são `minecraft:impossible` (só o código concede). Também controla o ícone oculto `sovereign_unknown` ("???"): concedido com os 5 Monarcas e **revogado** ao derrotar o Soberano, quando surge `defeat_sovereign` com a cabeça dele. Chamado por `MonarchBrain.onDeath` e `SovereignBrain.onDeath`. |

### `item/` — itens

| Arquivo | O que é |
|---|---|
| `ModularWeaponItem.java` | Item base de todas as armas. Tooltip (verde/vermelho conforme o requisito), clique direito (habilidade de classe ou de tier), `create(...)` para fabricar uma pilha configurada. |
| `ModItems.java` | Registra 1 item por `WeaponClass`, 1 ovo por criatura × elemento (20 ovos), o ovo do Soberano e a aba criativa **Solo Mod** com todas as combinações. |

### `event/` — regras do servidor

| Arquivo | O que é |
|---|---|
| `CombatEvents.java` | **Coração do combate.** Em `LivingIncomingDamageEvent` (antes da armadura) aplica tier, escalonamento de stat e elemento ao golpe do jogador. Também redução de dano por VIT e envio dos números de dano. |
| `ElementalCombat.java` | Regras elementais compartilhadas: elemento de uma entidade, multiplicador, vantagem, passivas (fogo, lentidão etc.) e partículas. |
| `StatsLogic.java` | **Todo o balanceamento numérico** (constantes `FOR_FLAT_DAMAGE`, `AGI_*`, `VIT_*`, `WEAPON_STAT_SCALING`, `LOCKED_HEAVY_SPEED_PENALTY`) e a aplicação dos modificadores de atributo vanilla. `onStatsChanged` reaplica atributos e sincroniza. |
| `PlayerEvents.java` | Ciclo de vida: XP por abate de mob com perfil, regeneração de mana, reaplicar atributos em login, respawn e troca de dimensão. |

### `skill/` — habilidades

| Arquivo | O que é |
|---|---|
| `WeaponSkill.java` | Enum das habilidades de **tier** (`DASH_STRIKE, GROUND_SLAM, MANA_BOLT, ELEMENTAL_BURST`) com custo de mana, recarga e INT mínima. Cada constante implementa `perform(...)`. |
| `ClassAbility.java` | Habilidades de **classe** (sem mana): Estocada da rapieira, Arremesso da adaga, Salto do machado e Giro da espada longa. Tem eventos próprios (tick, queda, logout). |
| `SkillVfx.java` | Padrões de partículas do servidor (anel, linha) usados pelas habilidades. |
| `PlayerAnim.java` | Enum das animações do jogador (`SPIN, THRUST, LEAP_FLIP, SLAM`), tocadas por `client/PlayerAnimations`. |

### `entity/` — entidades próprias

| Arquivo | O que é |
|---|---|
| `TierMob.java` | Criatura própria de cada tier (`extends Monster`). Guarda o elemento em `SynchedEntityData` (o cliente escolhe modelo e textura com ele). Sem perfil ao nascer → recebe um elemento aleatório. O Monarca e o Soberano (tier BOSS) têm a morte de desintegração (`DISSOLVE_TICKS=100`). **O combate não fica aqui**, fica no `TierBrain`. |
| `Sovereign.java` | O Soberano, chefe final (`extends TierMob`, tier BOSS, sempre sem elemento). Atributos base próprios (`BASE_HEALTH`, `BASE_DAMAGE`), `RENDER_SCALE`, hitbox (`HITBOX_WIDTH`/`HITBOX_HEIGHT`/`EYE_HEIGHT`, menor que o modelo de propósito, explicado na Javadoc), sons e imunidade a fogo e a sufocamento (`isInvulnerableTo`). O combate fica em `mob/SovereignBrain`. |
| `SovereignTornado.java` | Tornado 3D do Soberano (`extends Entity`, sem colisão, não salvo): persegue um jogador a 0,13 bloco/tick, dura 11 s e lança quem encosta no pé (~11 blocos: o dano é da queda). Lógica só no servidor. Substituiu o antigo tornado de partículas. |
| `WaterOrb.java` | Esfera de água 3D do Soberano (`extends Entity`, não salva) em três modos: `ORBIT` (gira nele), `HOMING` (persegue o jogador e explode em respingo; um golpe direto a estoura) e `WAVE` (só visual: a crista do Maremoto). |
| `ThrownDagger.java` | Adaga arremessada (`extends AbstractArrow`): voa girando, crava, volta como bumerangue quando o dono chama. |
| `RoyalFireball.java` | Entidade `royal_fireball` (`Projectile`): bola de 2,4 blocos, lenta e rebatível. Colisão volumétrica e reflexão no servidor; devolvida causa dano mágico do jogador e tenta `TierBrain.stunFromFireball(100)` (bloqueado na Fúria). NBT guarda conjurador, reflexão, dano e idade. Modelo próprio em `RoyalFireballRenderer` (cubos no estilo do Minecraft, sem item vanilla). `launchGenki` é a genki-dama do Soberano: nasce acima das mãos, desce sobre o jogador e explode em área (`blastRadius`). Devolvida ao Soberano, só o atordoa (imune a fogo). As bolas do Soberano são removidas fora do estágio 2. |
| `ModEntities.java` | Registra `minion`, `elite`, `guardian`, `monarch`, `sovereign`, `thrown_dagger` e `royal_fireball`, além dos atributos. `forTier(tier)` devolve o tipo da criatura do tier (BOSS = Monarca). |

### `mob/` — IA e identidade dos mobs (servidor)

| Arquivo | O que é |
|---|---|
| `TierIdentity.java` | Aplica ou remove a **identidade** de um tier em qualquer `LivingEntity`: grava o `MobProfile`, escala (só mobs vanilla; as criaturas já têm o tamanho do modelo), atributos, título colorido, brilho (só BOSS) e instala o brain. `apply(entity, tier, element)` é o jeito oficial de transformar um mob; `clear` desfaz tudo. A tabela de números por tier está no Javadoc. |
| `TierBrain.java` | Classe abstrata (`Goal` de prioridade 0) que substitui a IA de combate vanilla enquanto há alvo. Sistema de **`Move`** (ataque com recarga, peso, `usable(dist)` e `tick(t)`), **Fúria** a 1/4 da vida (`addRageMove`), exaustão e invulnerabilidade em rugidos. Ganchos para chefes especiais: `rageThreshold`, `beginRage`, `endlessRage`, `interrupt`. `install(mob)` escolhe o brain pelo tier (BOSS: `SovereignBrain` se for a entidade `Sovereign`, senão `MonarchBrain`) / `uninstall(mob)` / `of(entity)`. |
| `MinionBrain.java` | NORMAL: bando, grito de alerta, fuga, Bote. |
| `EliteBrain.java` | OP: guarda lateral, aparar e contra-atacar, Investida, Golpe Triplo. |
| `GuardianBrain.java` | SUB_BOSS: controle de distância, orbes, Repulsão, Salto, Translocar, Invocação + ataques de Fúria. |
| `MonarchBrain.java` | BOSS: 3 fases, leitura do estilo do jogador (perto/longe), barra de chefe, todos os ataques do chefe + Fúria. Guarda quem causou dano e, na morte, concede as conquistas a esses jogadores. O maior arquivo de IA. |
| `SovereignBrain.java` | O Soberano: 6 estágios de vida, agonia invulnerável com close (`CinematicPayload` `CLOSE_UP`) a cada estágio, um ataque novo por estágio (`StageMove`: Punho, Genki-dama, Maremoto, Raízes, Pisão, Tornados), Fúria sem fim no 6º com o Raio do Peito (`ChestBeam`, prioridade, termina em `kneel`) e os poderes dos Monarcas um por vez, voos curtos (`Hop`: recuo antes do raio, avanço contra kite/preso), **escolha por prioridade** (`Move.urgency`: pisão/punho quando colado, voo, raio) e leitura de como o jogador luta (pressão melee/ranged, combo pós-pisão), trava de vida por estágio em `onHurt`, **estágio restaurado pela vida** ao reinstalar o cérebro (`stageFromHealth`), padrões de vários jogadores (foco rotativo, grupo junto/espalhado, `damageTakenMultiplier` por jogador), movimento direto sem pathfinding (`chase`), risada com cinemática, cinemática final + créditos e conquista na morte. `skipToStage` para o `/modtest`. |
| `Hazards.java` | Ataques que existem no mundo por alguns ticks sem ser entidade: orbes, áreas marcadas (meteoros), ondas de choque, tornados e `QuakeBlocks` (terremoto: blocos do chão viram `FallingBlockEntity` e voltam). Simulados no servidor e desenhados com partículas. Também tem helpers de dano em área (`enemiesIn`, `hit`, `push`) e sons. |
| `MobAnim.java` | Enum das animações de ação dos mobs (`WINDUP, LUNGE, DASH, COMBO, CAST, LEAP, SLAM, ROAR..., AGONY`). O servidor diz **qual** animação e por quanto tempo; o cliente desenha. |
| `MobEvents.java` | Liga tudo ao ciclo de vida: reinstala o brain quando um mob salvo é carregado, tick, dano (Fúria, aparar, invulnerabilidade), morte, envia a identidade a quem começa a ver o mob, tick de `Hazards`. |

### `network/` — pacotes

Todos registrados em `ModNetwork.register` (protocolo `"1"`).

| Pacote | Direção | Para quê |
|---|---|---|
| `SyncStatsPayload` | S → C | cópia dos stats do jogador (HUD, tooltip) |
| `HitFeedbackPayload` | S → C | número de dano flutuante (`kind`: 0 normal, 1 vantagem, 2 desvantagem, 3 habilidade, 4 crítico) |
| `MobAnimPayload` | S → C | identidade do mob (tier, elemento, fase, fúria) + animação de ação |
| `PlayerAnimPayload` | S → C | animação de habilidade de um jogador |
| `CinematicPayload` | S → C | cinemática de chefe: `kind` (`DEFEAT` morte do Monarca, `CLOSE_UP` agonia do Soberano, `FINALE` morte do Soberano + créditos, `LAUGH` risada dele ao matar um jogador), cabeçalho e título |
| `SpendPointsPayload` | C → S | botões +1/+5 da tela de Maestria (o servidor revalida) |
| `TestActionPayload` | C → S | botões da aba Teste (exige OP); também tem `spawnTierMob` e `mark` |
| `ShakePayload` | S → C | tremor de câmera (`client/ScreenShake`): força inicial e duração; `ModNetwork.sendShake` envia a quem está perto, mais fraco com a distância |
| `SpinAttackPayload` | C → S | clique de ataque da espada longa → giro 360° |
| `ModNetwork` | — | registro + helpers `syncStats`, `sendHit`, `sendMobAnim`, `sendPlayerAnim`, `sendCinematic` |

### `command/`

| Arquivo | O que é |
|---|---|
| `ModTestCommand.java` | Árvore do `/modtest` (OP nível 2). O Javadoc no topo lista todos os subcomandos. |

### `client/` — só cliente

| Arquivo | O que é |
|---|---|
| `ClientSetup.java` | Mod bus: tecla **K**, camada do HUD, cores dos itens (tinta por elemento), layers dos modelos e renderers. |
| `ClientGameEvents.java` | Game bus: abrir a tela com K, tick e render dos números de dano. |
| `ClientAccess.java` | Único ponto em que o código comum toca classes de cliente (`localPlayer()`). |
| `StatsScreen.java` | Tela da tecla K (abas Maestria e Teste). |
| `SoloHud.java` | HUD do canto superior esquerdo. |
| `DamageNumbers.java` | Números de dano flutuantes. |
| `MobAnimations.java` | Animações ociosas por tier + animações de ação recebidas do servidor. Guarda o estado (tier, elemento, fase, fúria) de cada mob no cliente. |
| `PlayerAnimations.java` | Animações das habilidades no jogador (giro, estocada, mortal) e troca do ataque da espada longa por `SpinAttackPayload`. |
| `BossCinematic.java` | Cinemáticas de chefe (faixas, câmera, zoom, letreiro). Close forte no rosto no `CLOSE_UP`; no fim do `FINALE` abre os créditos. |
| `CreditsScreen.java` | Créditos do fim da versão de testes: texto rolando (`credits.solomod.*`), música de créditos, ESPAÇO acelera, ESC fecha. |
| `ScreenShake.java` | Tremor de câmera (guinada, inclinação e rolagem) disparado por `ShakePayload`: terremoto do pisão, pouso dos voos, soco do Soberano. Não muda a mira. |

### `client/model/` e `client/render/`

| Arquivo | O que é |
|---|---|
| `TierMobModel.java` | Base dos modelos (`HierarchicalModel`): anima as partes com nomes conhecidos (`body`, `head`, `arm_r`...), as decorativas por prefixo e a desintegração. |
| `MinionModel` / `EliteModel` / `GuardianModel` / `MonarchModel` | Poses e animações de cada tier. Uma classe anima as 5 formas (base + 4 elementos) da criatura. |
| `SovereignModel.java` | O Soberano: herda do `MonarchModel` (mesmos nomes de partes) e acrescenta as poses de agonia, genki-dama (braços unidos no alto), pisão, flutuar com braços abertos (tornados) e raio do peito, além do tremor da coroa. |
| `MobMeshes.java` | ⚠️ **Gerado** por `tools/gen_mob_art.py`. Malhas (cubos + UV) das 20 formas e do Soberano. Não edite à mão. |
| `SovereignTornadoRenderer` / `WaterOrbRenderer` / `EffectMesh` | Renderers procedurais dos efeitos 3D do Soberano **no estilo do Minecraft (só cubos e barras retas, nada redondo)**: o tornado é uma pilha de 14 anéis quadrados girando em degraus; a esfera de água é um cubo dentro de cubo dentro de cubo com cubinhos em órbita. `EffectMesh` tem `cube`, `box` e `squareRing`. Texturas 16x16 de `tools/gen_sovereign_effects.py`. |
| `TierMobRenderer.java` | Troca de modelo e textura conforme o elemento. Camadas: base + brilho emissivo + aura de Fúria. `single(...)` é para criatura sem variantes (o Soberano): malha única, escala maior e **sem a camada de brilho** (só a aura de Fúria). |
| `ThrownDaggerRenderer.java` | Desenha a adaga voando, girando ou cravada. |
| `RoyalFireballRenderer.java` | Malha procedural exclusiva: núcleo facetado, crosta vazada, dois anéis de plasma e seis fragmentos orbitais, com luz máxima e texturas próprias. |

---

## Recursos (`src/main/resources/assets/solomod/`)

| Pasta | Conteúdo | Origem |
|---|---|---|
| `lang/` | `pt_br.json`, `en_us.json` | manual |
| `models/item/` | modelos das armas (2D, 3D, variantes elementais) e dos ovos | parte gerada por `tools/gen_weapon_models.py` |
| `textures/item/` | sprites das armas, `*_overlay` (parte tingida), variantes `_fire/_water/_earth/_air`, `weapon_parts*` (textura dos modelos 3D) | gerada por `tools/gen_weapon_models.py` / `gen_weapon_elements.py` |
| `textures/entity/` | `<mob>[_<elemento>].png` + `<mob>[_<elemento>]_glow.png` (emissiva) | gerada por `tools/gen_mob_art.py` |

Dados do servidor ficam em `src/main/resources/data/solomod/`:

| Pasta | Conteúdo | Origem |
|---|---|---|
| `advancement/` | `root.json` (aba "Solo Mod"), `defeat_monarch_<elemento>.json` (5), `defeat_all_monarchs.json` e as secretas (`"hidden": true`) `broken_seal.json` e `defeat_sovereign.json`. Títulos e descrições em `advancements.solomod.*` no lang. | manual |

`src/main/templates/META-INF/neoforge.mods.toml` é o template de metadados: o Gradle substitui `${...}` com os valores de `gradle.properties`.

## Scripts (`tools/`) — Python 3 + Pillow

| Script | Gera | Observação |
|---|---|---|
| `gen_mob_art.py` | `textures/entity/*.png` e `client/model/MobMeshes.java` | spec das 4 formas base; a mesma spec gera UV e textura, então os dois nunca saem de sincronia |
| `mob_variants.py` | — (importado por `gen_mob_art.py`) | as 16 formas elementais e o Soberano (`sovereign()`) |
| `gen_boss_icons.ps1` | `textures/item/monarch_fire_head.png` | Cabeça do Rei Vulcânico em pixel art 16×16 RGBA para o item-ícone de Cinzas do Vulcão; usa System.Drawing no Windows, sem dependências Python. |
| `gen_royal_fireball.ps1` | `textures/entity/royal_fireball.png` e `royal_fireball_core.png` | Texturas originais 16×16 de pixels grandes (crosta de basalto com lava e núcleo de magma), determinísticas, para a malha de cubos; não reutilizam arte do Ghast. |
| `gen_weapon_models.py` | modelos 3D (rapieira/claymore), textura de peças, contorno fantasma da adaga, modelos dos ovos (incluindo o do Soberano) | ponto de entrada das armas |
| `gen_weapon_elements.py` | — (importado por `gen_weapon_models.py`) | versões elementais das armas 2D e 3D |
| `gen_sovereign_effects.py` | `textures/entity/sovereign_tornado.png`, `water_orb.png`, `water_orb_core.png` | texturas 16x16 de pixels grandes dos efeitos 3D do Soberano (a mesma saída sempre: ruído por hash de x, y) |
| `gen_sovereign_icons.py` | `textures/item/sovereign_head.png` e `unknown_boss.png` | ícones 16x16 das conquistas do Soberano (a cabeça e o "???"); o desenho são as matrizes no próprio script |

Rode da raiz: `python tools/gen_mob_art.py` e `python tools/gen_weapon_models.py`.

Ícone do Rei Vulcânico: `powershell -File tools/gen_boss_icons.ps1`. `ModItems.MONARCH_FIRE_HEAD` registra o item visual usado por `defeat_monarch_fire.json`, com modelo `item/generated`.

Velocidade de ida/volta, diâmetro, alcance mínimo, duração da recuperação, vida do projétil e multiplicador de dano estão nas constantes `TierIdentity.ROYAL_FIREBALL_*`. `MobEvents.onStartTracking` envia pose e duração restante da recuperação ou risada para novos observadores. Gere as texturas do projétil com `powershell -File tools/gen_royal_fireball.ps1`.

**Bolas de fogo e recuperação:** `MonarchBrain.RoyalVolley` só é registrado para Fogo e existe nas fases 1–3 e na Fúria. `SovereignBrain.RoyalVolley` só é utilizável nos estágios 2 e 6. `TierBrain.stun` centraliza a imunidade na Fúria e nas transições invulneráveis, interrompe o ataque e suspende movimento por 100 ticks; os cérebros adiam transições e a aura ofensiva nesse intervalo. A bola devolvida ainda causa dano durante a Fúria. `MobAnim.KNOCKED_DOWN` mantém o ordinal e agora significa recuperação agachada; `MobAnimations` aplica a postura geral, e `TierMobModel` dobra pernas, baixa a cabeça e posa os braços de todos os chefes. A colisão da investida normal usa o mesmo stun (40 ticks). `PlayerAnimations` permite golpe direto da espada longa ao mirar `RoyalFireball`.

**Gritos e risada:** `TierBrain` emite um grito tanto em `RageStart` como em `beginRage` (usado pelo Soberano), repetindo a cada `TierIdentity.RAGE_CRY_INTERVAL`. `MobEvents.onDeath` identifica o chefe que matou um jogador pelo dano, crédito de combate ou dono do magma. `onPlayerDefeated` agenda a risada; `baseTick` só interrompe o ataque após sair de transições invulneráveis, evitando reentrância no `Move.tick`. A risada `LAUGH` dura `TierIdentity.BOSS_LAUGH_TICKS`, roda sem alvo e não pausa a Fúria. Som compartilhado e movimentos de cabeça, mandíbula, braços e tronco estão em `MobAnimations`/`TierMobModel`. Vale para BOSS e SUB_BOSS, incluindo Soberano.

**Magma da investida:** `MonarchBrain.Charge` e `EndlessCharge` chamam `Hazards.magmaTrail` apenas no Fogo. O servidor consulta `canEntityGrief`, substitui trechos expostos de blocos naturais por magma permanente e não toca blocos com dados. Atribuição em memória por dimensão (até 4096 posições, limpa ao parar o servidor) permite rir quando um jogador morre no rastro. Raio e chance ficam em `TierIdentity.MAGMA_TRAIL_*`.

**Escala e hitbox do Soberano:** `Sovereign.RENDER_SCALE=7` (modelo de ~31 blocos); a hitbox é de propósito menor (8 x 24): `EntitySectionStorage` só consulta seções a ±2 blocos da região buscada, então caixas bem mais largas criam pontos cegos de acerto. `getBoundingBoxForCulling` amplia a região visível e `BossCinematic.focusOn` usa frações da altura da caixa (rosto 93%, tronco 70%). Partículas altas ou distantes (genki-dama, raio) usam `Hazards.farParticles`, porque `sendParticles` comum só chega a 32 blocos.

**Morte "bugando":** `TierMob.tickDeath` chama o gancho `dissolveEffects` (o `Sovereign` o sobrescreve com faíscas, estática e bipes). No cliente, `TierMobModel.glitch` (peças saltando, escala e piscar, sorteio determinístico por quadro de 2 ticks, para corpo e camadas coincidirem) e `TierMobRenderer.GlitchLayer` (cópias vermelha e ciano deslocadas). Só vale para `Sovereign`; os Monarcas continuam desintegrando.

**Estágio não é salvo:** `TierBrain` não é persistido, então `SovereignBrain` deriva o estágio da vida ao ser instalado (`stageFromHealth`) e retoma a Fúria com `resumeRage` (sem grito). Sem isso, recarregar o chefe repetia todas as agonias. Qualquer estado novo do Soberano deve poder ser reconstruído da vida ou ser aceito como perdido.

---

## Fluxos principais

### Golpe corpo a corpo do jogador

```mermaid
sequenceDiagram
    participant P as Jogador
    participant CE as CombatEvents
    participant SL as StatsLogic
    participant EC as ElementalCombat
    participant C as Cliente
    P->>CE: LivingIncomingDamageEvent
    CE->>CE: lê WeaponData da mão
    CE->>SL: meetsRequirement / weaponScaling
    alt travada
        CE->>CE: dano × tier
    else destravada
        CE->>EC: multiplier(elemento arma, elemento alvo)
        CE->>EC: applyPassive (se vantagem)
    end
    CE->>C: HitFeedbackPayload (número flutuante)
```

### Mob vira "mob do mod"

1. `/modtest mob`, o botão da aba Teste ou um `TierMob` nascendo chamam `TierIdentity.apply(entity, tier, element)`.
2. `apply` grava o `MobProfile`, aplica escala, atributos, título e brilho e chama `TierBrain.install(mob)`, que escolhe o brain pelo tier e adiciona os goals de alvo (todo mob do mod fica hostil ao jogador).
3. Quando um mob salvo é carregado do mundo, `MobEvents.onJoin` reinstala o brain, porque os goals não são salvos, só o perfil.
4. A cada ataque especial o brain envia `MobAnimPayload` → `client/MobAnimations` anima.
5. Na morte, `PlayerEvents.onKill` dá XP ao jogador conforme o tier. Se for o Monarca, `MonarchBrain.onDeath` dispara a cinemática e `ModAdvancements.awardBossDefeat` para quem causou dano a ele.
6. O Soberano (`SovereignBrain`) é diferente: a cada 1/6 da vida para no limite, agoniza (`CinematicPayload` `CLOSE_UP`) e libera um ataque; no 6º estágio entra em Fúria sem fim e usa só o raio do peito, ajoelhando em seguida (`kneel`). Na morte manda `CinematicPayload` `FINALE`, e o cliente abre `CreditsScreen` quando a cena acaba.

### Stats

1. O cliente aperta +1 → `SpendPointsPayload` → o servidor valida e altera `PlayerStatsAttachment`.
2. `StatsLogic.onStatsChanged(player)` reaplica modificadores de atributo e chama `ModNetwork.syncStats`.
3. O cliente recebe `SyncStatsPayload` → HUD, tela e tooltips se atualizam.
