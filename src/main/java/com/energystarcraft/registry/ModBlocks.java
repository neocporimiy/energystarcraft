package com.energystarcraft.registry;

import com.energystarcraft.block.EnergyForgeBlock;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks((String)"energystarcraft");
    public static final DeferredBlock<EnergyForgeBlock> ENERGY_FORGE = BLOCKS.registerBlock(
            "energy_forge",
            EnergyForgeBlock::new,
            props -> props
                    .strength(3.5f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 7)
    );

    private ModBlocks() {
    }
}

