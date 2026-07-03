package com.energystarcraft.registry;

import com.energystarcraft.blockentity.EnergyForgeBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = "energystarcraft")
public final class ModCapabilities {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                ModBlockEntities.ENERGY_FORGE.get(),
                (be, direction) -> be.energyStorage
        );
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.ENERGY_FORGE.get(),
                (be, direction) -> be.outputHandler
        );
    }

    private ModCapabilities() {
    }
}
