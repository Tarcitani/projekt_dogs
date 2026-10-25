package com.tarcitani.solomod;

import com.mojang.logging.LogUtils;
import com.tarcitani.solomod.attachment.ModAttachments;
import com.tarcitani.solomod.command.ModTestCommand;
import com.tarcitani.solomod.component.ModDataComponents;
import com.tarcitani.solomod.entity.ModEntities;
import com.tarcitani.solomod.event.CombatEvents;
import com.tarcitani.solomod.event.PlayerEvents;
import com.tarcitani.solomod.item.ModItems;
import com.tarcitani.solomod.mob.MobEvents;
import com.tarcitani.solomod.network.ModNetwork;
import com.tarcitani.solomod.skill.ClassAbility;
import com.tarcitani.solomod.skill.DodgeRoll;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

/**
 * Ponto de entrada do mod.
 *
 * <p>Dois barramentos de eventos:
 * <ul>
 *   <li>mod bus ({@code modBus}): registros e eventos de carregamento (DeferredRegister, pacotes, abas).</li>
 *   <li>game bus ({@code NeoForge.EVENT_BUS}): eventos de jogo (dano, morte, tick, comandos).</li>
 * </ul>
 */
@Mod(SoloMod.MOD_ID)
public class SoloMod {
    public static final String MOD_ID = "solomod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SoloMod(IEventBus modBus, ModContainer container) {
        // Registros (a ordem entre eles não importa: o NeoForge registra por tipo de registro).
        ModDataComponents.register(modBus);
        ModAttachments.register(modBus);
        ModItems.register(modBus);
        ModEntities.register(modBus);
        modBus.addListener(ModNetwork::register);

        // Eventos de jogo: classes com métodos estáticos @SubscribeEvent.
        NeoForge.EVENT_BUS.register(CombatEvents.class);
        NeoForge.EVENT_BUS.register(PlayerEvents.class);
        NeoForge.EVENT_BUS.register(MobEvents.class);
        NeoForge.EVENT_BUS.register(ClassAbility.class);
        NeoForge.EVENT_BUS.register(DodgeRoll.class);
        NeoForge.EVENT_BUS.register(ModTestCommand.class);

        LOGGER.info("[{}] MVP de Maestria/Elementos carregado.", MOD_ID);
    }
}
