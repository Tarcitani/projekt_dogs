# AGENTS.md — Guia para agentes de IA

Este arquivo é para qualquer IA (Antigravity, Claude, Copilot, Cursor, Codex...) que for mexer neste repositório. Leia inteiro antes de editar. Humanos também podem usar.

## 1. O que é o projeto

**Solo Mod**: mod de Minecraft **1.21.1** para **NeoForge** (plugin `net.neoforged.moddev`), em **Java 21**. É um RPG com:

- **Maestria**: 4 stats do jogador (FOR/AGI/INT/VIT), XP Global e pontos.
- **Armas modulares**: catálogo com rapieira, espada longa, adaga e martelo; 4 classes legadas preservadas. Tier, elemento, requisito e habilidade gravados no ItemStack.
- **Combate elemental**: ciclo Água > Fogo > Ar > Terra > Água.
- **Mobs por tier**: 4 tiers com IA própria, ataques telegrafados, Fúria e chefes com fases. Há 4 criaturas com 5 formas cada (base + 4 elementos).

Mod id: `solomod` · Pacote: `com.tarcitani.solomod` · Idioma do jogo, dos comentários e dos commits: **português (pt-BR)**.

## 2. Onde ler mais

| Precisa de... | Leia |
|---|---|
| O que cada arquivo faz, fluxos, pacotes de rede | [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) |
| Regras de jogo, números, ataques dos mobs | [docs/GAMEPLAY.md](docs/GAMEPLAY.md) |
| Fluxo de Git, branches, convenções | [CONTRIBUTING.md](CONTRIBUTING.md) |
| Visão geral e setup | [README.md](README.md) |

O **Javadoc no topo de cada classe** é detalhado e atualizado. Leia o da classe antes de mexer nela.

## 3. Mapa rápido

```
src/main/java/com/tarcitani/solomod/
├── SoloMod.java          entrada: registros (mod bus) + classes de evento (game bus)
├── core/                 enums: ElementType, MobTier, StatType, WeaponClass
├── component/            WeaponData (record no ItemStack) + registro
├── attachment/           PlayerStatsAttachment (jogador), MobProfile (mob) + registro
├── advancement/          ModAdvancements (conquistas dos Monarcas, do Soberano e as secretas)
├── item/                 ModularWeaponItem, ModItems (itens, ovos, aba criativa)
├── event/                CombatEvents (dano), ElementalCombat, StatsLogic (balanceamento), PlayerEvents
├── skill/                WeaponSkill (habilidades de tier), ClassAbility (de classe), SkillVfx, PlayerAnim
├── entity/               TierMob (criaturas), Sovereign (chefe final), ThrownDagger, RoyalFireball, ModEntities
├── mob/                  TierIdentity, TierBrain + 4 Brains + SovereignBrain, Hazards, MobAnim, MobEvents
├── network/              ModNetwork + *Payload
├── command/              ModTestCommand (/modtest)
└── client/               SÓ CLIENTE: tela, HUD, animações, cinemática, model/, render/
src/main/resources/assets/solomod/   lang/, models/item/, textures/{item,entity}/
src/main/resources/data/solomod/     advancement/ (JSON das conquistas)
src/main/templates/META-INF/neoforge.mods.toml   metadados (variáveis de gradle.properties)
tools/                    scripts Python que GERAM arte e código
```

## 4. Comandos

```bash
./gradlew build          # compila e gera build/libs/solomod-<versão>.jar (use para validar mudanças)
./gradlew runClient      # abre o jogo (interativo, não use em automação)
./gradlew compileJava    # checagem rápida de compilação
python tools/gen_mob_art.py        # regera texturas de mobs + MobMeshes.java
python tools/gen_weapon_models.py  # regera modelos/texturas das armas e ovos
python tools/gen_boss_icons.py     # rostos pixel art dos seis chefes + modelos de item
powershell -File tools/gen_royal_fireball.ps1 # texturas originais da bola de fogo 3D (Windows)
```

No Windows use `gradlew.bat`. Não há testes automatizados: valide com `./gradlew build` e, quando possível, peça ao usuário para testar no jogo com `/modtest`.

## 5. Regras obrigatórias

1. **Não edite arquivos gerados à mão.** `client/model/MobMeshes.java`, `textures/entity/*.png` e as texturas e modelos elementais das armas saem de `tools/`. Mude o script e rode-o.
2. **Separação cliente/servidor.** Nada fora de `client/` pode importar `net.minecraft.client.*` ou classes de `client/`, senão o servidor dedicado quebra. Para acessar o cliente a partir de código comum, use `client/ClientAccess` depois de checar `FMLEnvironment.dist == Dist.CLIENT`. Classes de cliente usam `@EventBusSubscriber(value = Dist.CLIENT)`.
3. **O servidor é a autoridade.** Pacotes cliente→servidor só pedem; o servidor revalida tudo (permissão, pontos, item na mão).
4. **Stats mudaram → `StatsLogic.onStatsChanged(player)`.** Ela reaplica atributos e sincroniza. O sync do attachment é manual (`SyncStatsPayload`), não automático.
5. **`WeaponData` é um record imutável.** Para alterar, crie uma cópia e `stack.set(ModDataComponents.WEAPON_DATA, ...)`. Campo novo → atualize o `CODEC` **e** o `STREAM_CODEC`, com default em `optionalFieldOf` para não quebrar mundos salvos.
6. **Não reordene enums.** Ordinais são usados no protocolo e nos assets:
   - `ElementType`: o predicate `solomod:element` nos modelos de item usa o ordinal (1=water, 2=earth, 3=fire, 4=air).
   - `MobTier`, `WeaponClass`, `StatType`: ordinais vão nos argumentos de `TestActionPayload`, e `TierMob` usa `tier.ordinal()`.
   - `MobAnim`, `CinematicPayload.Kind`: vão pela rede como ordinal.
   - Adicione constantes novas **no fim**.
7. **Textos visíveis**: chave em `lang/pt_br.json` **e** `lang/en_us.json`. Prefira `Component.translatable`.
8. **Balanceamento** fica nas constantes de `event/StatsLogic`, `attachment/PlayerStatsAttachment`, `core/MobTier`, `core/WeaponClass`, `skill/WeaponSkill`, na tabela de `mob/TierIdentity` e, para o chefe final, em `entity/Sovereign` (atributos base) e `mob/SovereignBrain` (estágios). Não espalhe números mágicos.
9. **Classe nova com `@SubscribeEvent` estático no game bus (servidor)** → registre em `SoloMod.java` (`NeoForge.EVENT_BUS.register(...)`).
10. **Mantenha a documentação em dia.** Mudou comando, mecânica, arquivo ou estrutura? Atualize o Javadoc da classe e o doc correspondente (README / GAMEPLAY / ARCHITECTURE / este arquivo).

## 6. Receitas

### Nova classe de arma
1. Constante no **fim** de `core/WeaponClass` (nome serializado, peso, stat, dano, velocidade, habilidade padrão).
2. `ModItems` e `ClientSetup` iteram sobre `WeaponClass.values()`: o item, a cor e os predicates vêm automaticamente.
   O catálogo visível da aba Armas e da tela Teste vem de `WeaponClass.AVAILABLE`; são quatro itens, uma unidade neutra de cada. Índice da lista não é ordinal de rede.
3. Assets: `models/item/<nome>.json` (+ `_fire/_water/_earth/_air` e overrides de `solomod:element`), `textures/item/<nome>.png` + `<nome>_overlay.png` (camada tingida, `tintindex 1`). Gere as variantes pelos scripts de `tools/`.
4. Lang: `weapon_class.solomod.<nome>` nos dois idiomas.
5. Habilidade de classe (opcional): `skill/ClassAbility`.

### Nova habilidade de tier
1. Constante no fim de `skill/WeaponSkill` (`id`, mana, recarga em ticks, INT mínima) e implemente `perform(...)`.
2. Use `ElementalCombat` para dano e passiva e `SkillVfx` para partículas. Mande o número de dano com `ModNetwork.sendHit`.
3. Lang: `skill.solomod.<id>`.

### Novo ataque de mob
1. No Brain do tier (`mob/*Brain.java`), crie uma classe interna `extends Move` (recarga, peso; sobrescreva `usable(dist)`, `begin()`, `tick(t)` → `false` quando terminar).
2. Registre no construtor com `addMove(...)`, ou `addRageMove(...)` se for exclusivo da Fúria.
3. **Telegrafe** (animação `MobAnim` + partículas na cor do elemento) antes de causar dano.
4. Ataques que ficam no mundo (orbes, áreas, ondas): use `mob/Hazards`.
5. Animação nova → constante em `mob/MobAnim` + tratamento em `client/MobAnimations` e/ou no `TierMobModel` do tier.

### Novo pacote de rede
1. `record XPayload(...) implements CustomPacketPayload` em `network/`, com `TYPE`, `STREAM_CODEC` e `handle` (siga um existente, ex.: `HitFeedbackPayload`).
2. Registre em `ModNetwork.register` (`playToClient` ou `playToServer`).
3. Handler S→C: **não importe** a classe de cliente no topo. Use o nome completo dentro do `enqueueWork`, como em `HitFeedbackPayload`: `context.enqueueWork(() -> com.tarcitani.solomod.client.DamageNumbers.onHit(payload));`.

### Novo subcomando de teste
1. Adicione em `ModTestCommand.register`.
2. Atualize o Javadoc do topo da classe e a tabela de comandos do README.
3. Se fizer sentido, um botão na aba Teste (`client/StatsScreen` + `TestActionPayload.Action`).

### Mudar a aparência de uma criatura
1. Forma base: `tools/gen_mob_art.py`. Variante elemental: `tools/mob_variants.py`.
2. Rode `python tools/gen_mob_art.py` e faça commit do script **e** dos arquivos gerados.
3. Partes com nome conhecido (`body`, `head`, `arm_r`...) recebem animação do tier em `client/model/<Tier>Model`. Partes decorativas animam pelo prefixo do nome (veja `TierMobModel`).

## 7. Armadilhas conhecidas

- **`CombatEvents` usa `LivingIncomingDamageEvent`** (antes da armadura). Não mude para `LivingDamageEvent` sem entender que o multiplicador elemental passaria a agir depois da armadura.
- **Brains não são salvos**: só o `MobProfile` persiste. `MobEvents.onJoin` reinstala o brain quando o mob é carregado.
- **Ter `MobProfile` = ser mob do mod**: dá XP, é alvo dos Hazards como "aliado" (não leva dano deles) e fica hostil ao jogador. Teste com `hasData(ModAttachments.MOB_PROFILE)`.
- **Armas e controles**: R alterna `WeaponTechnique` por classe (espada: Meia-Lua/Ascendente/Cruzado; rapieira: Dash/Chuva de Agulhas/Passo do Vento; martelo: Salto/Giro/Ruptura). `WeaponControls` só mantém confirmação do servidor; seleções são independentes, por sessão, e compartilhar técnica não reinicia recarga do item. R não alterna durante uso/ação/menu. Ataque esquerdo é vanilla. `SpinAttackPayload`/`ClassAbility.spinAttack` são compatibilidade validada. Só o dash da rapieira usa `startUsingItem`/`releaseUsing`; outras técnicas usam clique simples. Na adaga arremessada, Shift + direito teleporta/recolhe e direito chama de volta; tier com Shift após recolher. Só o Salto do martelo inicia no ar/escala com a queda (teto 24 blocos); Giro/Ruptura exigem chão. A Ruptura para em paredes, líquidos, bordas e chunks não carregados; fragmentos são visuais. Balanceamento em `StatsLogic.WeaponAbilities`; `WeaponTechnique` e `PlayerAnim` só recebem constantes no fim.
- **HUD**: `SoloHud`/`HudArt` desenham brasão e trilhos compactos. `ClientGameEvents` cancela só a camada `PLAYER_HEALTH` enquanto a HUD está visível; absorção, veneno/wither e dano são representados na vida personalizada. Não cancele fome, armadura, ar ou vida da montaria junto com os corações.
- **Rolamento**: duplo Ctrl é detectado em `client/DodgeInput`; `DodgeRollPayload` só pede eixos locais. `skill/DodgeRoll` (registrado em `SoloMod`) valida chão, estado e recarga; movimento e janela curta de esquiva são do servidor (`StatsLogic.Dodge`). Não combine ações de arma com o rolamento. `RollAnimationPayload` envia a direção real. O protocolo de rede atual é `4`.
- **Poses e efeitos das armas**: `client/mixin/HumanoidModelMixin` aplica poses após `HumanoidModel.setupAnim`, antes de `PlayerModel` copiar mangas; configuração só na seção `client` de `solomod.client.mixins.json`. Não mova esse mixin para o lado comum. `WeaponEffects` desenha os três cortes 3D a partir de `CrescentSlashPayload`; `MotionEffects`/`FxGeometry` desenham velocidade, raios dourados e impacto a partir de `WeaponMotionPayload`. Tudo visual e com teste de profundidade. O martelo usa poses de mão e iluminação próprias em `tools/gen_weapon_models.py`, incluindo as variantes elementais.
- **Bolas de Fogo Reais** têm modelo 3D próprio (`RoyalFireballRenderer`, cubos no estilo do Minecraft; texturas 16x16 de `tools/gen_royal_fireball.ps1`), não usam fire charge/Ghast. A espada longa permite golpe direto ao mirá-las. Devolvê-las ao conjurador causa dano e tenta `TierBrain.stunFromFireball(100)`: Fúria e transições bloqueiam stun. No Soberano só existem nos estágios 2/6. O ícone de Cinzas do Vulcão é gerado por `tools/gen_boss_icons.ps1`.
- **Chefes (BOSS e SUB_BOSS)**: `KNOCKED_DOWN` preserva o ordinal, mas agora é recuperação agachada (nunca deitada). `LAUGH` foi acrescentado ao fim do enum; `MobEvents.onDeath` agenda a risada do chefe vencedor por 80 ticks, mesmo sem alvo. Fúria tem grito compartilhado de entrada e a cada 100 ticks; nenhuma aplicação de stun passa por ela.
- **Investidas do Rei Vulcânico** convertem partes do piso natural exposto em magma permanente via `Hazards.magmaTrail`, respeitando `mobGriefing`; a autoria do magma para risadas é só da sessão atual. **Soberano**: modelo 5× maior (`RENDER_SCALE=7`), com caixa física independente, foco cinemático e culling ajustados.
- **Monarca e Soberano não morrem na hora**: desintegram por `TierMob.DISSOLVE_TICKS` (100) e disparam `CinematicPayload`.
- **Soberano ≠ Monarca.** No código, `monarch` são os 5 chefes elementais (tier BOSS, `MonarchBrain`) e `sovereign` é o chefe final (entidade `Sovereign`, também tier BOSS, `SovereignBrain`). `TierBrain.install` escolhe pelo tipo da entidade.
- **O estágio do Soberano vem da vida, não de um campo salvo**: o cérebro (`TierBrain`) não persiste e é reinstalado ao carregar o chunk, então `SovereignBrain` recalcula o estágio (`stageFromHealth`) e retoma a Fúria (`resumeRage`) no construtor. Não guarde estado de luta só no cérebro sem pensar no reload: foi o que repetia todas as cinemáticas.
- **Hitbox do Soberano (8 x 24) é menor que o modelo (escala 7)** de propósito: a busca de entidades do Minecraft só olha seções a ±2 blocos, e caixas largas demais viram pontos cegos de acerto. Não aumente sem testar. Ele anda sem pathfinding (`SovereignBrain.chase`) e é imune a fogo e a sufocamento.
- **A IA do Soberano escolhe por prioridade**: `TierBrain.Move#urgency` (só os ataques prontos de maior urgência entram no sorteio). Pisão colado nos pés (6) > voo curto (5) > punho colado (4) > raio do peito (3) > sorteio. Ataque novo que "precisa vir antes" ganha `urgency`, não um peso enorme.
- **`noGravity` é salvo no NBT**: voos curtos (`Hop`) ligam e desligam; o construtor do cérebro sempre desliga, para um chunk descarregado no meio do voo não deixar o chefe flutuando.
- **Efeitos 3D do Soberano são entidades** (`SovereignTornado`, `WaterOrb`): lógica só no servidor, `noSave`, sem colisão. Não use `Hazards` com partículas para efeitos que precisam ser vistos de longe e de perto: faça entidade + renderer procedural (`EffectMesh`) + textura gerada em `tools/`. **Estilo Minecraft: cubos e barras retas, textura 16x16 de pixels grandes; nada de esferas ou cones lisos.**
- **Regra dos poderes do Soberano**: estágios 1 a 5 usam só os poderes do próprio elemento (`StageMove.ownStage`); só o 6 libera todos (um por vez) mais o raio. Poder novo precisa declarar o estágio a que pertence.
- **A morte do Soberano é glitch, não cinzas**: o sorteio visual vem do número do quadro (`TierMobModel.glitchFrame`); mantenha determinístico ou o corpo e as camadas de cor tremem fora de sincronia.
- **Terremoto de blocos**: `Hazards.QuakeBlocks` usa `FallingBlockEntity` (o bloco volta ao pousar), só em terra/pedra/areia/cascalho expostos e com `mobGriefing`. Tremor de câmera: `ModNetwork.sendShake` (`ShakePayload`).
- **Ícones de conquista são itens**: `monarch_<elemento>_head` para os cinco Monarcas, `unknown_boss` ("???") e `sovereign_head` (`ModItems`, `tools/gen_boss_icons.py`, que reutiliza `tools/gen_sovereign_icons.py`). `sovereign_unknown` é oculta e concedida/revogada pelo código (`ModAdvancements`).
- **Poderes dos Monarcas**: `MonarchBrain` acrescenta os poderes correspondentes do Soberano (fogo: genki-dama; água: maremoto e esferas; terra: raízes, pisão e pulão; ar: tornados), também na Fúria. O neutro usa o Golpe Sísmico com empurrão. Balanceamento em `TierIdentity.MonarchPowers`. `MonarchModel` compartilha poses com `SovereignModel`; as restrições de estágio de `WaterOrb`/`SovereignTornado` só valem para o Soberano.
- **Partículas altas ou distantes** (mais de 32 blocos): use `Hazards.farParticles`, senão só quem está colado vê.
- **O Soberano não pula estágio**: `SovereignBrain.onHurt` devolve a vida ao limite do estágio, inclusive em golpe fatal. Só dano com a tag `bypasses_invulnerability` (`/kill`, void) passa direto.
- **`.vscode/launch.json` não vai para o Git** (caminhos absolutos da máquina). `run/`, `build/`, `bin/` e `.gradle/` também são ignorados.
- **Encoding**: os arquivos são UTF-8 com acentos. No PowerShell, leia com `-Encoding utf8`, senão o texto aparece corrompido. Nunca regrave um arquivo com outro encoding.
- **Versão do NeoForge**: `neo_version` em `gradle.properties` (`21.1.x`). Se o build falhar ao resolver, tente uma `21.1.x` mais recente.
