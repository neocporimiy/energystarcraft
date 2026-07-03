package com.energystarcraft.registry;

import com.energystarcraft.registry.ModBlockEntities;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid="energystarcraft")
public final class ModCapabilities {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.ENERGY_FORGE.get(), (be, direction) -> be.getEnergyStorage((Direction)direction));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.ENERGY_FORGE.get(), (be, direction) -> be.getOutputSlot());
    }

    private ModCapabilities() {
    }
}

