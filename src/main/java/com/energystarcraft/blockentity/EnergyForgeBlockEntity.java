package com.energystarcraft.blockentity;

import com.energystarcraft.EnergyStarcraft;
import com.energystarcraft.menu.EnergyForgeMenu;
import com.energystarcraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnergyForgeBlockEntity extends BlockEntity implements MenuProvider {
    private static final int OUTPUT_SLOT = 0;
    private final NonNullList<ItemStack> outputStacks = NonNullList.withSize(1, ItemStack.EMPTY);
    public final ItemStacksResourceHandler outputHandler = new ItemStacksResourceHandler(outputStacks) {
        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            EnergyForgeBlockEntity.this.setChanged();
            EnergyForgeBlockEntity.this.syncToClient();
        }
    };
    public final EnergyForgeEnergyStorage energyStorage = new EnergyForgeEnergyStorage(() -> {
        this.setChanged();
        this.syncToClient();
    });
    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> EnergyForgeBlockEntity.this.energyStorage.getAmountAsInt() & 0xFFFF;
                case 1 -> EnergyForgeBlockEntity.this.energyStorage.getAmountAsInt() >> 16 & 0xFFFF;
                case 2 -> EnergyForgeBlockEntity.this.isCrafting() ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public EnergyForgeBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ENERGY_FORGE.get(), pos, blockState);
    }

    public ItemStacksResourceHandler getOutputHandler() {
        return this.outputHandler;
    }

    private ItemStack getOutputStack() {
        return this.outputHandler.getResource(OUTPUT_SLOT).toStack(this.outputHandler.getAmountAsInt(OUTPUT_SLOT));
    }

    public boolean canPlaceNetherStar() {
        ItemStack output = getOutputStack();
        return output.isEmpty() || output.is(Items.NETHER_STAR) && output.getCount() < output.getMaxStackSize();
    }

    public ContainerData getContainerData() {
        return this.containerData;
    }

    public boolean isCrafting() {
        return this.energyStorage.getAmountAsInt() >= EnergyStarcraft.NETHER_STAR_COST && this.canPlaceNetherStar();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EnergyForgeBlockEntity blockEntity) {
        if (blockEntity.energyStorage.getAmountAsInt() >= EnergyStarcraft.NETHER_STAR_COST && blockEntity.canPlaceNetherStar()) {
            blockEntity.performCraft();
        }
    }

    private void performCraft() {
        if (this.energyStorage.getAmountAsInt() < EnergyStarcraft.NETHER_STAR_COST || !this.canPlaceNetherStar()) {
            return;
        }

        int extracted = this.energyStorage.extractInternal(EnergyStarcraft.NETHER_STAR_COST);
        if (extracted < EnergyStarcraft.NETHER_STAR_COST) {
            return;
        }

        ItemStack output = getOutputStack();
        if (output.isEmpty()) {
            this.outputHandler.set(OUTPUT_SLOT, ItemResource.of(Items.NETHER_STAR), 1);
        } else {
            this.outputHandler.set(OUTPUT_SLOT, ItemResource.of(Items.NETHER_STAR), output.getCount() + 1);
        }

        this.setChanged();
        this.syncToClient();
        EnergyStarcraft.LOGGER.debug("Energy Forge at {} crafted a Nether Star!", this.worldPosition);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.outputHandler.serialize(output);
        this.energyStorage.serialize(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.outputHandler.deserialize(input);
        this.energyStorage.deserialize(input);
    }

    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @NotNull
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    private void syncToClient() {
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void dropContents(LevelAccessor level, BlockPos pos) {
        if (level instanceof Level serverLevel && !serverLevel.isClientSide()) {
            for (int i = 0; i < this.outputHandler.size(); ++i) {
                ItemStack stack = this.outputHandler.getResource(i).toStack(this.outputHandler.getAmountAsInt(i));
                if (stack.isEmpty()) {
                    continue;
                }
                Block.popResource(serverLevel, pos, stack);
                this.outputHandler.set(i, ItemResource.EMPTY, 0);
            }
        }
    }

    @NotNull
    public Component getDisplayName() {
        return Component.translatable("gui.energystarcraft.energy_forge");
    }

    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        if (player instanceof ServerPlayer) {
            return new EnergyForgeMenu(containerId, playerInventory, this);
        }
        return null;
    }
}
