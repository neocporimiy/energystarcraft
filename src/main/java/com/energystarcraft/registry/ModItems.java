package com.energystarcraft.registry;

import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems((String)"energystarcraft");
    public static final DeferredItem<BlockItem> ENERGY_FORGE = ITEMS.registerSimpleBlockItem("energy_forge", ModBlocks.ENERGY_FORGE);

    private ModItems() {
    }
}

