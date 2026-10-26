package com.tarcitani.solomod.network;

import com.tarcitani.solomod.SoloMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** S→C: efeitos visuais de armas e quatro técnicas com escudo; sem dano, raio real ou movimento. */
public record WeaponMotionPayload(Kind kind, int entityId, Vec3 from, Vec3 to, float power) implements CustomPacketPayload {
    public enum Kind { RAPIER, DAGGER_THROW, DAGGER_RECALL, DAGGER_BLINK, HAMMER_IMPACT,
        RAPIER_FLURRY, RAPIER_RETREAT, HAMMER_SWEEP, HAMMER_QUAKE,
        SHIELD_SWORD, SHIELD_RAPIER, SHIELD_DAGGER, SHIELD_HAMMER }
    public static final Type<WeaponMotionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "weapon_motion"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WeaponMotionPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override public WeaponMotionPayload decode(RegistryFriendlyByteBuf b) {
            int kind = b.readVarInt();
            Kind k = Kind.values()[Math.floorMod(kind, Kind.values().length)];
            return new WeaponMotionPayload(k, b.readVarInt(), new Vec3(b.readDouble(), b.readDouble(), b.readDouble()),
                    new Vec3(b.readDouble(), b.readDouble(), b.readDouble()), b.readFloat());
        }
        @Override public void encode(RegistryFriendlyByteBuf b, WeaponMotionPayload p) {
            b.writeVarInt(p.kind().ordinal()); b.writeVarInt(p.entityId());
            b.writeDouble(p.from().x); b.writeDouble(p.from().y); b.writeDouble(p.from().z);
            b.writeDouble(p.to().x); b.writeDouble(p.to().y); b.writeDouble(p.to().z); b.writeFloat(p.power());
        }
    };
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(WeaponMotionPayload p, IPayloadContext context) {
        context.enqueueWork(() -> com.tarcitani.solomod.client.MotionEffects.start(p));
    }
}
