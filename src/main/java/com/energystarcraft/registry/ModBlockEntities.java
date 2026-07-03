package com.energystarcraft.registry;

import com.energystarcraft.blockentity.EnergyForgeBlockEntity;
import com.energystarcraft.registry.ModBlocks;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create((ResourceKey)Registries.BLOCK_ENTITY_TYPE, (String)"energystarcraft");
    public static final Supplier<BlockEntityType<EnergyForgeBlockEntity>> ENERGY_FORGE = BLOCK_ENTITIES.register("energy_forge", () -> BlockEntityType.Builder.of(EnergyForgeBlockEntity::new, (Block[])new Block[]{(Block)ModBlocks.ENERGY_FORGE.get()}).build(null));

    private ModBlockEntities() {
    }
}

