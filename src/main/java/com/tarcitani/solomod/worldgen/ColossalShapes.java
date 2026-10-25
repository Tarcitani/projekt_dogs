package com.tarcitani.solomod.worldgen;

import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Formas dos lugares colossais ({@link ColossalKind}), bloco a bloco. Cada função recebe a posição relativa à base
 * da estrutura ({@code dx}, {@code dz} a partir do centro; {@code dy} a partir do chão, que é {@code dy = 0}) e
 * devolve o bloco daquele ponto, ou {@code null} para deixar o mundo como está. {@code AIR} escava.
 *
 * <p>É tudo matemática sem estado (cones, toros, cápsulas, espirais) com um ruído de hash para variar os blocos,
 * então a mesma posição sempre dá o mesmo bloco, em qualquer ordem em que os pedaços de mundo sejam gerados.
 * Os pontos de nascimento (Monarca, criaturas congeladas) ficam aqui também, para combinar com as formas.
 */
public final class ColossalShapes {
    private ColossalShapes() {}

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    /** Bloco da posição relativa para aquele tipo (null = não mexe). */
    @Nullable
    public static BlockState at(ColossalKind kind, int dx, int dy, int dz) {
        return switch (kind) {
            case INVERTED_PENDULUM_CITADEL -> citadel(dx, dy, dz);
            case CARDIAC_FURNACE -> furnace(dx, dy, dz);
            case DROWNED_SKY_TRENCH -> trench(dx, dy, dz);
            case AMBER_HOURS_CRADLE -> cradle(dx, dy, dz);
            case STATIC_TEMPEST_PANTHEON -> pantheon(dx, dy, dz);
            case SHATTERED_SEAL_AMPHITHEATER -> amphitheater(dx, dy, dz);
        };
    }

    /** Onde nasce o Monarca do trono (relativo à base). */
    public static int[] monarchSpot(ColossalKind kind) {
        return switch (kind) {
            case INVERTED_PENDULUM_CITADEL -> new int[] {0, 1, 24};
            case CARDIAC_FURNACE -> new int[] {0, 1, 18};
            case AMBER_HOURS_CRADLE -> new int[] {0, 1, 16};
            default -> new int[] {0, 1, 0};
        };
    }

    /** Centros das esferas de âmbar do Berço (cada uma guarda uma criatura congelada da Era II). */
    public static final int[][] AMBER_SPHERES = {{18, 24, 0}, {-18, 28, 0}, {0, 34, 18}, {0, 38, -18}};
    private static final int AMBER_RADIUS = 6;

    // ================================================================== Colosso Rei

    /** Cidadela do Pêndulo Invertido: praça, teto flutuante com torres penduradas e o molde do braço direito. */
    @Nullable
    private static BlockState citadel(int dx, int dy, int dz) {
        double r = Math.sqrt(dx * dx + dz * dz);
        if (dy <= 0) return r <= 50 ? ground(dy, pick(dx, dy, dz, Blocks.POLISHED_ANDESITE, Blocks.STONE_BRICKS,
                Blocks.CRACKED_STONE_BRICKS), Blocks.STONE.defaultBlockState()) : null;
        BlockState mold = armMold(dx, dy, dz);
        if (mold != null) return mold;
        // O teto: um disco de pedra solto no ar, como o chão de um castelo virado de cabeça para baixo.
        if (dy >= 56 && dy <= 60 && r <= 46) {
            return r > 44 ? Blocks.POLISHED_DEEPSLATE.defaultBlockState()
                    : pick(dx, dy, dz, Blocks.DEEPSLATE_TILES, Blocks.CRACKED_DEEPSLATE_TILES, Blocks.DEEPSLATE_BRICKS);
        }
        // Torre de menagem invertida no centro (pende do teto), oca, com janelas.
        if (dy >= 38 && dy < 56 && r <= 10) {
            if (r >= 8.6) return (dy % 6 == 0 && Math.abs(dx) <= 1) ? AIR : Blocks.DEEPSLATE_BRICKS.defaultBlockState();
            return AIR;
        }
        // Seis torres penduradas, finas na ponta de baixo e largas junto ao teto (ameias embaixo).
        for (int k = 0; k < 6; k++) {
            double a = k * Math.PI / 3;
            double d = Math.hypot(dx - 28 * Math.cos(a), dz - 28 * Math.sin(a));
            if (dy >= 24 && dy < 56) {
                double radius = 1.2 + (dy - 24) * 0.12;
                if (d <= radius) return dy == 24 ? Blocks.POLISHED_DEEPSLATE.defaultBlockState()
                        : pick(dx, dy, dz, Blocks.DEEPSLATE_BRICKS, Blocks.POLISHED_DEEPSLATE, Blocks.DEEPSLATE_TILES);
            }
        }
        return r <= 50 && dy < 56 ? AIR : null;
    }

    /** Monumento no centro: um bloco de pedra com a marca vazia de um braço direito colossal, aberta para o céu. */
    @Nullable
    private static BlockState armMold(int dx, int dy, int dz) {
        if (Math.abs(dx) > 14 || Math.abs(dz) > 8 || dy < 1 || dy > 12) return null;
        boolean forearm = dx >= -14 && dx <= 4 && Math.abs(dz) <= 3;
        boolean palm = dx > 4 && dx <= 8 && Math.abs(dz) <= 5;
        boolean finger = dx > 8 && dx <= 13 && (Math.abs(dz + 3.5) < 0.9 || Math.abs(dz + 1.2) < 0.9
                || Math.abs(dz - 1.2) < 0.9 || Math.abs(dz - 3.5) < 0.9);
        boolean thumb = dx > 3 && dx <= 7 && dz >= 5 && dz <= 7;
        if ((forearm || palm || finger || thumb) && dy >= 6) return AIR;
        return dy == 12 && (Math.abs(dx) == 14 || Math.abs(dz) == 8) ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState()
                : pick(dx, dy, dz, Blocks.SMOOTH_STONE, Blocks.STONE, Blocks.STONE_BRICKS);
    }

    // ================================================================== Rei Vulcânico

    /** Fornalha Cardíaca: praça de basalto e um vulcão suspenso com a câmara do coração e a cratera de lava. */
    @Nullable
    private static BlockState furnace(int dx, int dy, int dz) {
        double r = Math.sqrt(dx * dx + dz * dz);
        if (dy <= 0) {
            return r <= 42 ? ground(dy, pick(dx, dy, dz, Blocks.BASALT, Blocks.BLACKSTONE, Blocks.POLISHED_BLACKSTONE,
                    Blocks.MAGMA_BLOCK), Blocks.BLACKSTONE.defaultBlockState()) : null;
        }
        if (dy >= 22 && dy <= 60) {
            double outer = 32 - (dy - 22) * 0.58;
            if (r <= outer) {
                if (dy >= 52 && r < 6) return dy == 52 ? Blocks.MAGMA_BLOCK.defaultBlockState()
                        : dy <= 55 ? Blocks.LAVA.defaultBlockState() : AIR;
                double heart = dx * dx + (dy - 36) * (dy - 36) + dz * dz;
                if (heart < 9) return Blocks.MAGMA_BLOCK.defaultBlockState();
                if (heart < 49) return AIR;
                return hash(dx, dy, dz) < 0.12 ? Blocks.MAGMA_BLOCK.defaultBlockState()
                        : pick(dx, dy, dz, Blocks.BASALT, Blocks.BLACKSTONE, Blocks.TUFF);
            }
        }
        // Barriga do vulcão: um cone para baixo, de onde a "lava" (partículas) sobe.
        if (dy >= 8 && dy < 22) {
            double underside = (dy - 8) * 32.0 / 14.0;
            if (r <= underside) return pick(dx, dy, dz, Blocks.BLACKSTONE, Blocks.BASALT, Blocks.GILDED_BLACKSTONE);
        }
        return r <= 42 && dy < 62 ? AIR : null;
    }

    // ================================================================== Leviatã Coroado

    /** Fossa do Céu Afogado: cubo seco no fundo do mar, cercado de vidro, com navios no ar e garras saindo do chão. */
    @Nullable
    private static BlockState trench(int dx, int dy, int dz) {
        int ax = Math.abs(dx), az = Math.abs(dz);
        if (ax > 31 || az > 31 || dy < -3 || dy > 29) return null;
        boolean groove = dx >= -24 && dx <= 14 && (Math.abs(dz + 8) < 1.3 || Math.abs(dz) < 1.3 || Math.abs(dz - 8) < 1.3);
        if (ax == 31 || az == 31 || dy == 29) {
            return (dx + dy + dz) % 5 == 0 ? Blocks.BLUE_STAINED_GLASS.defaultBlockState()
                    : Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState();
        }
        if (dy < 0) return groove && dy >= -2 ? AIR : Blocks.DARK_PRISMARINE.defaultBlockState();
        if (dy == 0) {
            if (groove) return AIR;
            return dx % 8 == 0 && dz % 8 == 0 ? Blocks.SEA_LANTERN.defaultBlockState()
                    : pick(dx, dy, dz, Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE, Blocks.DARK_PRISMARINE);
        }
        BlockState claw = claws(dx, dy, dz);
        if (claw != null) return claw;
        BlockState ship = ship(dx - 10, dy - 17, dz + 14);
        if (ship == null) ship = ship(-(dx + 12), dy - 21, dz - 13);
        return ship != null ? ship : AIR;
    }

    /** Três garras curvas saindo do chão no fim de cada sulco. */
    @Nullable
    private static BlockState claws(int dx, int dy, int dz) {
        if (dy > 14) return null;
        for (int g : new int[] {-8, 0, 8}) {
            double t = dy / 14.0;
            double cx = 16 + 5 * t * t, radius = 3.0 * (1 - t) + 0.5;
            if (Math.hypot(dx - cx, dz - g) <= radius) {
                return dy >= 12 ? Blocks.BONE_BLOCK.defaultBlockState()
                        : pick(dx, dy, dz, Blocks.PRISMARINE, Blocks.DARK_PRISMARINE, Blocks.PRISMARINE_BRICKS);
            }
        }
        return null;
    }

    /** Navio naufragado (eixo x): casco curvo, convés quebrado, mastro e vela. Coordenadas relativas ao centro dele. */
    @Nullable
    private static BlockState ship(int u, int w, int v) {
        if (Math.abs(u) > 9 || Math.abs(v) > 4 || w < -3 || w > 8) return null;
        if (u == 0 && v == 0 && w >= 1) return Blocks.SPRUCE_FENCE.defaultBlockState();
        if (u == 1 && Math.abs(v) <= 2 && w >= 4 && w <= 7) return Blocks.WHITE_WOOL.defaultBlockState();
        double edge = 1 - (u / 9.0) * (u / 9.0);
        if (edge < 0) return null;
        double half = 3.2 * Math.sqrt(edge);
        if (Math.abs(v) > half || w > 0) return null;
        double bottom = -3 * edge;
        if (w < bottom) return null;
        boolean shell = Math.abs(v) >= half - 1 || w <= bottom + 1;
        if (w == 0) return hash(u, w, v) < 0.7 ? Blocks.SPRUCE_PLANKS.defaultBlockState() : null;
        return shell ? Blocks.SPRUCE_PLANKS.defaultBlockState() : null;
    }

    // ================================================================== Titã Ancestral

    /** Berço das Horas de Âmbar: árvore de pedra com raízes para o céu, esferas de âmbar e a artéria no subsolo. */
    @Nullable
    private static BlockState cradle(int dx, int dy, int dz) {
        double r = Math.sqrt(dx * dx + dz * dz);
        // Artéria fossilizada: um tubo oco sob o chão, saindo do pé da árvore.
        if (dy < 0 && dy >= -9 && dx >= -2 && dx <= 40) {
            double tube = Math.hypot(dy + 5, dz);
            if (tube <= 3.2) return tube >= 2.2 ? pick(dx, dy, dz, Blocks.RED_NETHER_BRICKS, Blocks.BONE_BLOCK, Blocks.RED_NETHER_BRICKS) : AIR;
        }
        // Raízes descendo até a artéria (sugando).
        for (int k = 0; k < 4; k++) {
            double a = k * Math.PI / 2 + 0.4;
            if (capsule(dx, dy, dz, 4 * Math.cos(a), 0, 4 * Math.sin(a), 3 + k * 2, -4, 0, 1.2)) {
                return Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState();
            }
        }
        if (dy <= 0) return r <= 40 ? ground(dy, pick(dx, dy, dz, Blocks.MOSS_BLOCK, Blocks.PODZOL, Blocks.ROOTED_DIRT),
                Blocks.DIRT.defaultBlockState()) : null;
        // Esferas de âmbar: casca de vidro laranja, oca por dentro, presas ao tronco por um galho.
        for (int[] s : AMBER_SPHERES) {
            double d = Math.sqrt(sq(dx - s[0]) + sq(dy - s[1]) + sq(dz - s[2]));
            if (d <= AMBER_RADIUS) return d >= AMBER_RADIUS - 1 ? Blocks.ORANGE_STAINED_GLASS.defaultBlockState() : AIR;
            if (capsule(dx, dy, dz, 0, s[1] - 4, 0, s[0] * 0.7, s[1], s[2] * 0.7, 1.3)) return petrified(dx, dy, dz);
        }
        // Tronco petrificado que afina para cima.
        if (dy <= 62 && r <= 6.5 - dy * 0.035) return petrified(dx, dy, dz);
        // Raízes para o céu: seis cápsulas abrindo do topo do tronco, cada uma com um ramo.
        for (int k = 0; k < 6; k++) {
            double a = k * Math.PI / 3;
            double ex = 24 * Math.cos(a), ez = 24 * Math.sin(a);
            if (capsule(dx, dy, dz, 0, 60, 0, ex, 86, ez, 2.2)) return petrified(dx, dy, dz);
            if (capsule(dx, dy, dz, ex * 0.5, 73, ez * 0.5, ex * 0.5 + 8 * Math.cos(a + 1), 88, ez * 0.5 + 8 * Math.sin(a + 1), 1.2)) {
                return petrified(dx, dy, dz);
            }
        }
        return r <= 40 && dy < 90 ? AIR : null;
    }

    private static BlockState petrified(int dx, int dy, int dz) {
        return hash(dx, dy, dz) < 0.06 ? Blocks.HONEYCOMB_BLOCK.defaultBlockState()
                : pick(dx, dy, dz, Blocks.DRIPSTONE_BLOCK, Blocks.PACKED_MUD, Blocks.TUFF);
    }

    // ================================================================== Imperador dos Céus

    /** Panteão da Tempestade Estática: anéis de mármore, furacão congelado, pontes de relâmpago e queimaduras de asas. */
    @Nullable
    private static BlockState pantheon(int dx, int dy, int dz) {
        double r = Math.sqrt(dx * dx + dz * dz);
        double theta = Math.atan2(dz, dx);
        if (dy <= 0) {
            if (r > 45) return null;
            if (dy == 0 && wingScar(r, theta)) return hash(dx, dy, dz) < 0.5
                    ? Blocks.BLACK_CONCRETE.defaultBlockState() : Blocks.COAL_BLOCK.defaultBlockState();
            return ground(dy, pick(dx, dy, dz, Blocks.POLISHED_DIORITE, Blocks.CALCITE, Blocks.QUARTZ_BRICKS),
                    Blocks.STONE.defaultBlockState());
        }
        // Seis colunas de mármore.
        for (int k = 0; k < 6; k++) {
            double a = k * Math.PI / 3;
            double d = Math.hypot(dx - 34 * Math.cos(a), dz - 34 * Math.sin(a));
            if (dy <= 50 && d <= 2.0) return Blocks.QUARTZ_PILLAR.defaultBlockState();
            if (dy >= 50 && dy <= 51 && d <= 3.0) return Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState();
        }
        // Três anéis (toros) em volta do furacão.
        int[][] rings = {{30, 20}, {26, 35}, {22, 50}};
        for (int[] ring : rings) {
            if (Math.hypot(r - ring[0], dy - ring[1]) <= 2.2) {
                return hash(dx, dy, dz) < 0.3 ? Blocks.CALCITE.defaultBlockState() : Blocks.QUARTZ_BRICKS.defaultBlockState();
            }
        }
        // Pontes de relâmpago cristalizado do anel do meio até o furacão.
        BlockState bolt = lightningBridge(r, theta, dy);
        if (bolt != null) return bolt;
        // O furacão congelado: casca em espiral que alarga para cima.
        if (dy <= 72) {
            double funnel = 4 + dy * 0.3;
            if (r >= funnel && r <= funnel + 1.6) {
                double band = Mth.positiveModulo(theta + dy * 0.12, Math.PI * 2 / 3);
                if (band < 1.4) return band < 0.5 ? Blocks.PACKED_ICE.defaultBlockState()
                        : Blocks.WHITE_STAINED_GLASS.defaultBlockState();
            }
        }
        return r <= 45 && dy < 75 ? AIR : null;
    }

    /** Duas faixas curvas de queimado no chão, onde as asas do Soberano foram cortadas. */
    private static boolean wingScar(double r, double theta) {
        if (r < 10 || r > 42) return false;
        for (double base : new double[] {Math.PI / 2 - 0.7, Math.PI / 2 + 0.7}) {
            double expected = base + 0.18 * Math.sin(r / 5);
            double width = 0.05 + 0.06 * (1 - r / 42);
            if (Math.abs(Mth.wrapDegrees(Math.toDegrees(theta - expected))) < Math.toDegrees(width)) return true;
        }
        return false;
    }

    /** Quatro raios em zigue-zague: núcleo de luz roxa (froglight perolado) e borda de ametista. */
    @Nullable
    private static BlockState lightningBridge(double r, double theta, int dy) {
        if (r < 15 || r > 26) return null;
        double t = (26 - r) / 11;
        for (int k = 0; k < 4; k++) {
            double base = k * Math.PI / 2 + Math.PI / 4;
            double expectedTheta = base + 0.12 * triangle(4 * t);
            double expectedY = 35 + 3 * triangle(6 * t);
            double side = Math.abs(Mth.wrapDegrees(Math.toDegrees(theta - expectedTheta))) * Math.PI / 180 * r;
            double up = Math.abs(dy - expectedY);
            if (side <= 1.2 && up <= 1.0) {
                return side < 0.6 && up < 0.6 ? Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState()
                        : Blocks.AMETHYST_BLOCK.defaultBlockState();
            }
        }
        return null;
    }

    // ================================================================== O Soberano

    /** Anfiteatro do Selo Partido: teatro cirúrgico de obsidiana no vazio, com cinco correntes rompidas. */
    @Nullable
    private static BlockState amphitheater(int dx, int dy, int dz) {
        double r = Math.sqrt(dx * dx + dz * dz);
        BlockState chain = brokenChains(dx, dy, dz, r);
        if (chain != null) return chain;
        if (r > 47.5) return null;
        if (dy < 0) {
            // Base rochosa em cone, pendurada no vazio.
            double radius = 36 * (1 + dy / 15.0);
            return dy >= -15 && r <= radius ? pick(dx, dy, dz, Blocks.OBSIDIAN, Blocks.BLACKSTONE, Blocks.CRYING_OBSIDIAN) : null;
        }
        if (r <= 36) {
            if (dy == 0) return r >= 11.5 && r <= 12.5 ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : Blocks.OBSIDIAN.defaultBlockState();
            if (dy == 1 && Math.abs(dx) <= 4 && Math.abs(dz) <= 2) return Blocks.POLISHED_BLACKSTONE.defaultBlockState();
            return dy <= 40 ? AIR : null;
        }
        // Arquibancada em degraus (o teatro cirúrgico) e o muro de fora.
        int steps = (int) ((r - 36) / 2.0);
        if (r <= 46) return dy <= 1 + steps ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : dy <= 40 ? AIR : null;
        if (dy <= 9) return hash(dx, dy, dz) < 0.15 ? AIR : Blocks.BLACKSTONE.defaultBlockState();
        return dy <= 40 ? AIR : null;
    }

    /** Cinco correntes saindo da borda para o vazio, subindo, com elos de ferro e pontas partidas. */
    @Nullable
    private static BlockState brokenChains(int dx, int dy, int dz, double r) {
        if (r < 44) return null;
        for (int k = 0; k < 5; k++) {
            double a = k * Math.PI * 2 / 5 + Math.PI / 10;
            double ax = Math.cos(a), az = Math.sin(a);
            // Eixo: de (r=46, y=6) até (r=74, y=36).
            double sx = 46 * ax, sz = 46 * az, sy = 6;
            double ex = 74 * ax, ez = 74 * az, ey = 36;
            double lx = ex - sx, ly = ey - sy, lz = ez - sz;
            double len2 = lx * lx + ly * ly + lz * lz;
            double s = ((dx - sx) * lx + (dy - sy) * ly + (dz - sz) * lz) / len2;
            double end = 0.72 + 0.18 * hash(k, 0, 7);
            if (s < 0 || s > end) continue;
            double px = sx + lx * s - dx, py = sy + ly * s - dy, pz = sz + lz * s - dz;
            double dist = Math.sqrt(px * px + py * py + pz * pz);
            int link = (int) (s * Math.sqrt(len2) / 2.5);
            if (s > 0.55 && hash(k, link, 3) < 0.3) continue;
            if (dist <= 0.7) return Blocks.CHAIN.defaultBlockState();
            if (dist <= 1.6 && link % 2 == 0) return Blocks.IRON_BLOCK.defaultBlockState();
        }
        return null;
    }

    // ================================================================== util

    /** Chão da praça (dy = 0) e fundação até 8 abaixo; abaixo disso, não mexe. */
    @Nullable
    private static BlockState ground(int dy, BlockState floor, BlockState fill) {
        if (dy == 0) return floor;
        return dy >= -8 ? fill : null;
    }

    private static boolean capsule(int x, int y, int z, double ax, double ay, double az, double bx, double by, double bz,
                                   double radius) {
        double lx = bx - ax, ly = by - ay, lz = bz - az;
        double len2 = lx * lx + ly * ly + lz * lz;
        double t = Mth.clamp(((x - ax) * lx + (y - ay) * ly + (z - az) * lz) / len2, 0, 1);
        return sq(ax + lx * t - x) + sq(ay + ly * t - y) + sq(az + lz * t - z) <= radius * radius;
    }

    /** Onda triangular de período 1 entre -1 e 1. */
    private static double triangle(double x) {
        double f = x - Math.floor(x);
        return 4 * Math.abs(f - 0.5) - 1;
    }

    private static double sq(double v) {
        return v * v;
    }

    /** 0..1 determinístico por posição. */
    private static double hash(int x, int y, int z) {
        int h = x * 73856093 ^ y * 19349663 ^ z * 83492791;
        h ^= h >>> 13;
        h *= 0x5bd1e995;
        h ^= h >>> 15;
        return (h & 0xFFFF) / 65535.0;
    }

    /** Um dos blocos da lista, escolhido pelo hash da posição. */
    private static BlockState pick(int x, int y, int z, net.minecraft.world.level.block.Block... blocks) {
        return blocks[Math.min(blocks.length - 1, (int) (hash(x, y, z) * blocks.length))].defaultBlockState();
    }
}
