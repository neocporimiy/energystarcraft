package com.energystarcraft.block;

import com.energystarcraft.blockentity.EnergyForgeBlockEntity;
import com.energystarcraft.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class EnergyForgeBlock
extends BaseEntityBlock {
    public static final MapCodec<EnergyForgeBlock> CODEC = EnergyForgeBlock.simpleCodec(EnergyForgeBlock::new);

    public EnergyForgeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyForgeBlockEntity(pos, state);
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EnergyForgeBlockEntity) {
                EnergyForgeBlockEntity energyForge = (EnergyForgeBlockEntity)be;
                serverPlayer.openMenu((MenuProvider)energyForge, pos);
            }
        }
        return InteractionResult.sidedSuccess((boolean)level.isClientSide());
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return EnergyForgeBlock.createTickerHelper(type, ModBlockEntities.ENERGY_FORGE.get(), EnergyForgeBlockEntity::serverTick);
    }

    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        BlockEntity be;
        if (!state.is(newState.getBlock()) && (be = level.getBlockEntity(pos)) instanceof EnergyForgeBlockEntity) {
            EnergyForgeBlockEntity energyForge = (EnergyForgeBlockEntity)be;
            energyForge.dropContents(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}

