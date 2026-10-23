package com.tarcitani.solomod.skill;

import com.tarcitani.solomod.core.ElementType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/**
 * "Animações" das habilidades no protótipo: padrões de partículas enviados pelo servidor
 * (todos os jogadores próximos veem). Sem modelos animados ainda.
 */
public final class SkillVfx {
    private SkillVfx() {}

    public static ParticleOptions particleFor(ElementType element) {
        return switch (element) {
            case WATER -> ParticleTypes.SPLASH;
            case FIRE -> ParticleTypes.FLAME;
            case AIR -> ParticleTypes.CLOUD;
            case EARTH -> ParticleTypes.COMPOSTER;
            case NONE -> ParticleTypes.CRIT;
        };
    }

    /** Anel horizontal de partículas. */
    public static void ring(ServerLevel level, Vec3 center, double radius, ParticleOptions particle, int count) {
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            level.sendParticles(particle, center.x + Math.cos(angle) * radius, center.y, center.z + Math.sin(angle) * radius,
                    1, 0, 0, 0, 0);
        }
    }

    /** Linha reta de partículas entre dois pontos (rastro, feixe). */
    public static void line(ServerLevel level, Vec3 from, Vec3 to, ParticleOptions particle, int count) {
        for (int i = 0; i <= count; i++) {
            Vec3 p = from.lerp(to, i / (double) count);
            level.sendParticles(particle, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0);
        }
    }
}
