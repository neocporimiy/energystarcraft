package com.energystarcraft.registry;

import com.energystarcraft.blockentity.EnergyForgeBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "energystarcraft");
    public static final Supplier<BlockEntityType<EnergyForgeBlockEntity>> ENERGY_FORGE = BLOCK_ENTITIES.register(
            "energy_forge",
            () -> new BlockEntityType<>(EnergyForgeBlockEntity::new, ModBlocks.ENERGY_FORGE.get())
    );

    private ModBlockEntities() {
    }
}
