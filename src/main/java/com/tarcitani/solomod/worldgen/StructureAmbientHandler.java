package com.tarcitani.solomod.worldgen;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.advancement.ModAdvancements;
import com.tarcitani.solomod.entity.ModEntities;
import com.tarcitani.solomod.entity.Sovereign;
import com.tarcitani.solomod.network.ModNetwork;
import com.tarcitani.solomod.skill.SkillVfx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3f;

/**
 * A "falha da realidade" viva de cada Santuário de Eco (lore: docs/LORE.md). A cada {@link #INTERVAL} ticks, para
 * cada jogador dentro de um santuário (achado pela própria estrutura do mundo), aplica o efeito daquele lugar. As
 * partículas soltas vão só para o jogador em questão; os anéis do Jardim e da Torre usam {@code SkillVfx.ring}, que
 * todos por perto veem (com dois jogadores dentro, o anel aparece duas vezes). A parte "física" (correntes,
 * estantes, lágrimas, árvores, torre, costelas) é construída em {@link SanctuaryPiece}.
 *
 * <ul>
 *   <li><b>Forja</b>: cinza caindo e chama de alma nos braseiros; perto deles, <b>geada sem dano</b> (a camada de
 *       congelamento sobe, mas fica abaixo do ponto em que o frio fere).</li>
 *   <li><b>Biblioteca</b>: gotas de tinta que <b>sobem</b> do chão e três <b>sombras sem corpo</b> andando em
 *       círculo pelo pátio.</li>
 *   <li><b>Marés</b>: <b>chuva horizontal</b> atravessando a arena e gotejamento sob as lágrimas paradas no ar.</li>
 *   <li><b>Jardim</b>: o chão <b>respira</b>: um anel de esporos abre e fecha a cada 4 s, com o som do fôlego.</li>
 *   <li><b>Torre</b>: ondas de choque visuais saem do sino a cada 3 s, e quem está perto ouve o <b>silêncio</b>
 *       (o cliente corta ambiente, clima e música: {@code AmbientSilencePayload}).</li>
 *   <li><b>Catacumba</b>: <b>chama negra</b> (fumaça e tinta) sobre os braseiros; perto deles, a escuridão fecha a
 *       visão.</li>
 * </ul>
 */
@EventBusSubscriber(modid = SoloMod.MOD_ID)
public final class StructureAmbientHandler {
    private StructureAmbientHandler() {}

    /** Ticks entre duas aplicações para o mesmo jogador. */
    private static final int INTERVAL = 10;
    /** Distância (horizontal) dos braseiros em que a geada da Forja e a escuridão da Catacumba agem. */
    private static final double BRAZIER_RANGE = 4.0;
    /** Geada da Forja: quanto sobe por aplicação e o teto (o frio só fere a partir de 140). */
    private static final int FROST_STEP = 28, FROST_CAP = 120;
    /** Silêncio da Torre: raio a partir do centro e duração de cada renovação (maior que o intervalo). */
    private static final double SILENCE_RADIUS = 18.0;
    private static final int SILENCE_TICKS = 25;

    private static final DustParticleOptions SHADOW = new DustParticleOptions(new Vector3f(0.02F, 0.02F, 0.03F), 3.0F);
    private static final DustParticleOptions RAIN = new DustParticleOptions(new Vector3f(0.55F, 0.78F, 0.95F), 0.8F);

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % INTERVAL != 0
                || player.isSpectator()) return;
        ServerLevel level = player.serverLevel();
        StructureStart start = level.structureManager().getStructureWithPieceAt(player.blockPosition(),
                holder -> holder.value() instanceof SanctuaryStructure);
        if (!start.isValid() || !(start.getStructure() instanceof SanctuaryStructure sanctuary)) {
            StructureStart colossal = level.structureManager().getStructureWithPieceAt(player.blockPosition(),
                    holder -> holder.value() instanceof ColossalStructure);
            if (colossal.isValid() && colossal.getStructure() instanceof ColossalStructure structure) {
                BoundingBox cbox = colossal.getBoundingBox();
                ColossalKind kind = structure.kind();
                Vec3 base = new Vec3(cbox.getCenter().getX() + 0.5, cbox.minY() + kind.below(), cbox.getCenter().getZ() + 0.5);
                colossus(level, player, kind, base);
            }
            return;
        }
        BoundingBox box = start.getBoundingBox();
        Vec3 floor = new Vec3(box.getCenter().getX() + 0.5, box.minY(), box.getCenter().getZ() + 0.5);
        SanctuaryKind kind = sanctuary.kind();
        switch (kind) {
            case ASH_FORGE -> forge(level, player, floor, kind);
            case FORGOTTEN_LIBRARY -> library(level, player, floor);
            case TIDE_SANCTUARY -> tides(level, player, floor);
            case PETRIFIED_GARDEN -> garden(level, player, floor);
            case BROKEN_WIND_TOWER -> tower(level, player, floor);
            case WHISPERING_CATACOMB -> catacomb(level, player, floor, kind);
        }
    }

    private static void forge(ServerLevel level, ServerPlayer player, Vec3 floor, SanctuaryKind kind) {
        burst(level, player, ParticleTypes.WHITE_ASH, player.position().add(0, 4, 0), 18, 6, 2, 6, 0.02);
        for (Vec3 top : brazierTops(floor, kind)) {
            burst(level, player, ParticleTypes.SOUL_FIRE_FLAME, top, 4, 0.2, 0.3, 0.2, 0.01);
            burst(level, player, ParticleTypes.SNOWFLAKE, top, 3, 0.4, 0.3, 0.4, 0.01);
        }
        if (nearBrazier(player, floor, kind)) {
            // Fogo frio: geada na tela, sem chegar ao congelamento que machuca.
            player.setTicksFrozen(Math.min(player.getTicksFrozen() + FROST_STEP, FROST_CAP));
        }
    }

    private static void library(ServerLevel level, ServerPlayer player, Vec3 floor) {
        RandomSource random = level.random;
        for (int i = 0; i < 6; i++) {
            Vec3 p = floor.add((random.nextDouble() - 0.5) * 20, 1.1, (random.nextDouble() - 0.5) * 20);
            // count 0: os três números viram a velocidade da partícula (a tinta sobe).
            level.sendParticles(player, ParticleTypes.SQUID_INK, true, p.x, p.y, p.z, 0, 0, 0.6, 0, 0.2);
        }
        float time = level.getGameTime() * 0.02F;
        for (int i = 0; i < 3; i++) {
            float a = time + i * Mth.TWO_PI / 3;
            Vec3 at = floor.add(Mth.cos(a) * 9.5, 1.05, Mth.sin(a) * 9.5);
            burst(level, player, SHADOW, at, 6, 0.35, 0.0, 0.35, 0);
        }
    }

    private static void tides(ServerLevel level, ServerPlayer player, Vec3 floor) {
        RandomSource random = level.random;
        for (int i = 0; i < 24; i++) {
            Vec3 p = floor.add(-12 + random.nextDouble() * 6, 2 + random.nextDouble() * 6, (random.nextDouble() - 0.5) * 22);
            level.sendParticles(player, RAIN, true, p.x, p.y, p.z, 0, 1.0, -0.03, 0.12, 0.9);
        }
        for (int[] tear : SanctuaryPiece.TEARS) {
            burst(level, player, ParticleTypes.DRIPPING_WATER, floor.add(tear[0], 6.9, tear[1]), 2, 0.6, 0, 0.6, 0);
        }
    }

    private static void garden(ServerLevel level, ServerPlayer player, Vec3 floor) {
        long period = 80;
        float phase = (level.getGameTime() % period) / (float) period;
        double radius = 3 + 9 * Mth.sin(phase * Mth.PI);
        SkillVfx.ring(level, floor.add(0, 1.15, 0), radius, ParticleTypes.COMPOSTER, 28);
        if (level.getGameTime() % period < INTERVAL) {
            level.playSound(null, floor.x, floor.y + 1, floor.z, SoundEvents.SNIFFER_SNIFFING, SoundSource.AMBIENT, 1.2F, 0.4F);
        }
    }

    private static void tower(ServerLevel level, ServerPlayer player, Vec3 floor) {
        Vec3 bell = floor.add(0, SanctuaryPiece.BELL_Y + 0.5, 0);
        // A onda anda 3 blocos a cada aplicação e recomeça a cada 3 s.
        long step = (level.getGameTime() / INTERVAL) % 6;
        SkillVfx.ring(level, bell, 1.5 + step * 3, ParticleTypes.CLOUD, 20 + (int) step * 8);
        if (step == 0) level.playSound(null, bell.x, bell.y, bell.z, SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 1.5F, 0.5F);
        if (player.position().distanceTo(floor) < SILENCE_RADIUS) ModNetwork.sendAmbientSilence(player, SILENCE_TICKS);
    }

    private static void catacomb(ServerLevel level, ServerPlayer player, Vec3 floor, SanctuaryKind kind) {
        for (Vec3 top : brazierTops(floor, kind)) {
            burst(level, player, ParticleTypes.LARGE_SMOKE, top, 3, 0.15, 0.3, 0.15, 0.01);
            burst(level, player, ParticleTypes.SQUID_INK, top, 2, 0.15, 0.2, 0.15, 0.02);
        }
        if (nearBrazier(player, floor, kind)) {
            // A chama negra come a luz em volta.
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, true, false));
        }
    }

    // ------------------------------------------------------------------ lugares colossais

    /** Raio (horizontal, a partir do centro) em que quem partiu o Selo chama o Soberano no Anfiteatro. */
    private static final double SUMMON_RADIUS = 24.0;
    /** Não chama outro Soberano se já houver um a esta distância. */
    private static final double SOVEREIGN_SEARCH = 160.0;
    /** Fornalha Cardíaca: um batimento a cada 10 s. */
    private static final int HEARTBEAT = 200;
    private static final DustParticleOptions DEBRIS = new DustParticleOptions(new Vector3f(0.45F, 0.45F, 0.48F), 1.6F);

    /** A falha da realidade em volta de cada membro roubado (e a arena do Soberano). */
    private static void colossus(ServerLevel level, ServerPlayer player, ColossalKind kind, Vec3 base) {
        RandomSource random = level.random;
        switch (kind) {
            case INVERTED_PENDULUM_CITADEL -> {
                // Detritos parados no ar: pó sem velocidade nem gravidade, espalhado entre o chão e o teto.
                for (int i = 0; i < 14; i++) {
                    Vec3 p = base.add((random.nextDouble() - 0.5) * 60, 4 + random.nextDouble() * 48, (random.nextDouble() - 0.5) * 60);
                    level.sendParticles(player, DEBRIS, true, p.x, p.y, p.z, 0, 0, 0, 0, 0);
                }
            }
            case CARDIAC_FURNACE -> {
                // Lava e cinza que caem para cima, saindo da barriga do vulcão.
                for (int i = 0; i < 10; i++) {
                    Vec3 p = base.add((random.nextDouble() - 0.5) * 40, 6 + random.nextDouble() * 14, (random.nextDouble() - 0.5) * 40);
                    level.sendParticles(player, random.nextBoolean() ? ParticleTypes.FLAME : ParticleTypes.LARGE_SMOKE,
                            true, p.x, p.y, p.z, 0, 0, 0.35, 0, 0.6);
                }
                if (level.getGameTime() % HEARTBEAT < INTERVAL) {
                    player.playNotifySound(SoundEvents.WARDEN_HEARTBEAT, SoundSource.AMBIENT, 3.0F, 0.45F);
                    ModNetwork.sendShake(player, 0.45F, 12);
                }
            }
            case DROWNED_SKY_TRENCH -> {
                for (int i = 0; i < 8; i++) {
                    Vec3 p = base.add((random.nextDouble() - 0.5) * 58, 27.5, (random.nextDouble() - 0.5) * 58);
                    level.sendParticles(player, ParticleTypes.DRIPPING_WATER, true, p.x, p.y, p.z, 1, 0, 0, 0, 0);
                }
            }
            case AMBER_HOURS_CRADLE -> {
                for (int[] s : ColossalShapes.AMBER_SPHERES) {
                    burst(level, player, ParticleTypes.FALLING_HONEY, base.add(s[0], s[1] - 6.5, s[2]), 2, 2, 0.2, 2, 0);
                    burst(level, player, ParticleTypes.WAX_ON, base.add(s[0], s[1], s[2]), 2, 3, 3, 3, 0);
                }
            }
            case STATIC_TEMPEST_PANTHEON -> {
                for (int i = 0; i < 12; i++) {
                    Vec3 p = base.add((random.nextDouble() - 0.5) * 60, 4 + random.nextDouble() * 50, (random.nextDouble() - 0.5) * 60);
                    level.sendParticles(player, ParticleTypes.ELECTRIC_SPARK, true, p.x, p.y, p.z, 0, 0, 0, 0, 0);
                }
                if (random.nextInt(40) == 0) player.playNotifySound(SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 0.6F, 1.4F);
            }
            case SHATTERED_SEAL_AMPHITHEATER -> amphitheater(level, player, base);
        }
    }

    /**
     * Anfiteatro: o céu costurado vale enquanto o jogador estiver na arena. Quem já partiu o Selo (os cinco Monarcas)
     * e chega perto do centro chama o Soberano, se ele não estiver por perto.
     */
    private static void amphitheater(ServerLevel level, ServerPlayer player, Vec3 base) {
        ModNetwork.sendArenaSky(player, SILENCE_TICKS);
        double dx = player.getX() - base.x, dz = player.getZ() - base.z;
        if (dx * dx + dz * dz > SUMMON_RADIUS * SUMMON_RADIUS || !ModAdvancements.hasShatteredSeal(player)) return;
        AABB search = new AABB(base, base).inflate(SOVEREIGN_SEARCH);
        if (!level.getEntitiesOfClass(Sovereign.class, search).isEmpty()) return;
        Sovereign sovereign = ModEntities.SOVEREIGN.get().create(level);
        if (sovereign == null) return;
        BlockPos home = BlockPos.containing(base.x, base.y + 1, base.z);
        sovereign.moveTo(base.x, base.y + 1, base.z, (float) Math.toDegrees(Math.atan2(-dx, dz)) + 180F, 0F);
        sovereign.finalizeSpawn(level, level.getCurrentDifficultyAt(home), MobSpawnType.EVENT, null);
        sovereign.setHome(home);
        level.addFreshEntity(sovereign);
        level.playSound(null, base.x, base.y, base.z, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 3.0F, 0.6F);
    }

    // ------------------------------------------------------------------ util

    /** Onde fica o "fogo" de cada um dos quatro pilares das diagonais (ver SanctuaryPiece.buildPillars). */
    private static Vec3[] brazierTops(Vec3 floor, SanctuaryKind kind) {
        double y = kind.palette().pillarHeight() + 2.3;
        return new Vec3[] {floor.add(-8, y, -8), floor.add(8, y, -8), floor.add(-8, y, 8), floor.add(8, y, 8)};
    }

    private static boolean nearBrazier(ServerPlayer player, Vec3 floor, SanctuaryKind kind) {
        for (Vec3 top : brazierTops(floor, kind)) {
            double dx = player.getX() - top.x, dz = player.getZ() - top.z;
            if (dx * dx + dz * dz < BRAZIER_RANGE * BRAZIER_RANGE && Math.abs(player.getY() - top.y) < 6) return true;
        }
        return false;
    }

    private static void burst(ServerLevel level, ServerPlayer player, ParticleOptions type, Vec3 at, int count,
                              double dx, double dy, double dz, double speed) {
        level.sendParticles(player, type, true, at.x, at.y, at.z, count, dx, dy, dz, speed);
    }
}
