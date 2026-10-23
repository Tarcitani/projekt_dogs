# AGENTS.md — Guia para agentes de IA

Este arquivo é para qualquer IA (Antigravity, Claude, Copilot, Cursor, Codex...) que for mexer neste repositório. Leia inteiro antes de editar. Humanos também podem usar.

## 1. O que é o projeto

**Solo Mod**: mod de Minecraft **1.21.1** para **NeoForge** (plugin `net.neoforged.moddev`), em **Java 21**. É um RPG com:

- **Maestria**: 4 stats do jogador (FOR/AGI/INT/VIT), XP Global e pontos.
- **Armas modulares**: 7 classes; tier, elemento, requisito e habilidade gravados no ItemStack.
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
├── advancement/          ModAdvancements (conquistas dos chefes e desafio mestre)
├── item/                 ModularWeaponItem, ModItems (itens, ovos, aba criativa)
├── event/                CombatEvents (dano), ElementalCombat, StatsLogic (balanceamento), PlayerEvents
├── skill/                WeaponSkill (habilidades de tier), ClassAbility (de classe), SkillVfx, PlayerAnim
├── entity/               TierMob (criaturas), ThrownDagger, ModEntities
├── mob/                  TierIdentity, TierBrain + 4 Brains, Hazards, MobAnim, MobEvents
├── network/              ModNetwork + *Payload
├── command/              ModTestCommand (/modtest)
└── client/               SÓ CLIENTE: tela, HUD, animações, cinemática, model/, render/
src/main/resources/assets/solomod/   lang/, models/item/, textures/{item,entity}/
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
   - Adicione constantes novas **no fim**.
7. **Textos visíveis**: chave em `lang/pt_br.json` **e** `lang/en_us.json`. Prefira `Component.translatable`.
8. **Balanceamento** fica nas constantes de `event/StatsLogic`, `attachment/PlayerStatsAttachment`, `core/MobTier`, `core/WeaponClass`, `skill/WeaponSkill` e na tabela de `mob/TierIdentity`. Não espalhe números mágicos.
9. **Classe nova com `@SubscribeEvent` estático no game bus (servidor)** → registre em `SoloMod.java` (`NeoForge.EVENT_BUS.register(...)`).
10. **Mantenha a documentação em dia.** Mudou comando, mecânica, arquivo ou estrutura? Atualize o Javadoc da classe e o doc correspondente (README / GAMEPLAY / ARCHITECTURE / este arquivo).

## 6. Receitas

### Nova classe de arma
1. Constante no **fim** de `core/WeaponClass` (nome serializado, peso, stat, dano, velocidade, habilidade padrão).
2. `ModItems` e `ClientSetup` iteram sobre `WeaponClass.values()`: o item, a cor e os predicates vêm automaticamente.
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
- **Ataque da espada longa** é cancelado no cliente e vira `SpinAttackPayload`; o giro é validado no servidor (`ClassAbility.spinAttack`).
- **O Soberano não morre na hora**: desintegra por `TierMob.DISSOLVE_TICKS` (100) e dispara `CinematicPayload`.
- **`.vscode/launch.json` não vai para o Git** (caminhos absolutos da máquina). `run/`, `build/`, `bin/` e `.gradle/` também são ignorados.
- **Encoding**: os arquivos são UTF-8 com acentos. No PowerShell, leia com `-Encoding utf8`, senão o texto aparece corrompido. Nunca regrave um arquivo com outro encoding.
- **Versão do NeoForge**: `neo_version` em `gradle.properties` (`21.1.x`). Se o build falhar ao resolver, tente uma `21.1.x` mais recente.
