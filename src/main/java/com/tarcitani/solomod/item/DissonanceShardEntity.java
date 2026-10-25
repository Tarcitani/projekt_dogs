package com.tarcitani.solomod.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * O {@link DissonanceShardItem} no chão. Usa o tipo de entidade de item vanilla (não precisa de registro): o NeoForge
 * troca o item comum por esta classe sempre que ele entra no mundo ({@code hasCustomEntity}).
 *
 * <ul>
 *   <li>Nenhum dano o destrói (fogo, lava, cacto, explosão...): {@link #hurt} recusa tudo.</li>
 *   <li>Nunca some por tempo ({@code setUnlimitedLifetime}).</li>
 *   <li>No vazio, em vez de sumir, reaparece em cima do bloco mais alto da coluna (ou no spawn do mundo).</li>
 * </ul>
 */
public class DissonanceShardEntity extends ItemEntity {
    public static final int PICKUP_DELAY = 40;

    public DissonanceShardEntity(Level level, double x, double y, double z, ItemStack stack) {
        super(level, x, y, z, stack);
        setUnlimitedLifetime();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    /** O vazio não o apaga: volta para a superfície da mesma coluna, parado. */
    @Override
    protected void onBelowWorld() {
        Level level = level();
        BlockPos column = BlockPos.containing(getX(), level.getMinBuildHeight(), getZ());
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, column.getX(), column.getZ());
        Vec3 safe = top > level.getMinBuildHeight()
                ? new Vec3(getX(), top + 1, getZ())
                : Vec3.atBottomCenterOf(level.getSharedSpawnPos()).add(0, 1, 0);
        teleportTo(safe.x, safe.y, safe.z);
        setDeltaMovement(Vec3.ZERO);
    }
}
