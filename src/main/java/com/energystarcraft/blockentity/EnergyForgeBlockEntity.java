/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ContainerData
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.energy.IEnergyStorage
 *  net.neoforged.neoforge.items.ItemStackHandler
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.energystarcraft.blockentity;

import com.energystarcraft.EnergyStarcraft;
import com.energystarcraft.blockentity.EnergyForgeEnergyStorage;
import com.energystarcraft.menu.EnergyForgeMenu;
import com.energystarcraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnergyForgeBlockEntity
extends BlockEntity
implements MenuProvider {
    private static final int OUTPUT_SLOT = 0;
    private final ItemStackHandler itemHandler = new ItemStackHandler(1){

        protected void onContentsChanged(int slot) {
            EnergyForgeBlockEntity.this.setChanged();
            EnergyForgeBlockEntity.this.syncToClient();
        }
    };
    public final EnergyForgeEnergyStorage energyStorage = new EnergyForgeEnergyStorage(() -> ((EnergyForgeBlockEntity)this).setChanged());
    private final ContainerData containerData = new ContainerData(){

        public int get(int index) {
            return switch (index) {
                case 0 -> EnergyForgeBlockEntity.this.energyStorage.getEnergyStored() & 0xFFFF;
                case 1 -> EnergyForgeBlockEntity.this.energyStorage.getEnergyStored() >> 16 & 0xFFFF;
                case 2 -> {
                    if (EnergyForgeBlockEntity.this.isCrafting()) {
                        yield 1;
                    }
                    yield 0;
                }
                default -> 0;
            };
        }

        public void set(int index, int value) {
        }

        public int getCount() {
            return 3;
        }
    };

    public EnergyForgeBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ENERGY_FORGE.get(), pos, blockState);
    }

    public ItemStackHandler getOutputSlot() {
        return this.itemHandler;
    }

    public boolean canPlaceNetherStar() {
        ItemStack output = this.itemHandler.getStackInSlot(0);
        return output.isEmpty() || output.is(Items.NETHER_STAR) && output.getCount() < output.getMaxStackSize();
    }

    public ContainerData getContainerData() {
        return this.containerData;
    }

    public boolean isCrafting() {
        return this.energyStorage.getEnergyStored() >= 350000000 && this.canPlaceNetherStar();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EnergyForgeBlockEntity blockEntity) {
        boolean hasEnoughEnergy = blockEntity.energyStorage.getEnergyStored() >= 350000000;
        boolean canOutput = blockEntity.canPlaceNetherStar();
        if (hasEnoughEnergy && canOutput) {
            blockEntity.performCraft();
        }
    }

    private void performCraft() {
        if (this.energyStorage.getEnergyStored() < 350000000) {
            return;
        }
        if (!this.canPlaceNetherStar()) {
            return;
        }
        int extracted = this.energyStorage.extractInternal(350000000);
        if (extracted < 350000000) {
            this.energyStorage.receiveEnergy(extracted, false);
            return;
        }
        ItemStack output = this.itemHandler.getStackInSlot(0);
        if (output.isEmpty()) {
            this.itemHandler.setStackInSlot(0, new ItemStack((ItemLike)Items.NETHER_STAR));
        } else {
            output.grow(1);
        }
        this.setChanged();
        this.syncToClient();
        EnergyStarcraft.LOGGER.debug("Energy Forge at {} crafted a Nether Star!", (Object)this.worldPosition);
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", (Tag)this.itemHandler.serializeNBT(registries));
        tag.putInt("Energy", this.energyStorage.getEnergyStored());
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Items")) {
            this.itemHandler.deserializeNBT(registries, tag.getCompound("Items"));
        }
        if (tag.contains("Energy")) {
            this.energyStorage.setEnergy(tag.getInt("Energy"));
        }
    }

    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    @NotNull
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    private void syncToClient() {
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void dropContents(Level level, BlockPos pos) {
        if (level != null && !level.isClientSide()) {
            for (int i = 0; i < this.itemHandler.getSlots(); ++i) {
                ItemStack stack = this.itemHandler.getStackInSlot(i);
                if (stack.isEmpty()) continue;
                Block.popResource((Level)level, (BlockPos)pos, (ItemStack)stack);
            }
        }
    }

    @NotNull
    public Component getDisplayName() {
        return Component.translatable((String)"gui.energystarcraft.energy_forge");
    }

    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        if (player instanceof ServerPlayer) {
            return new EnergyForgeMenu(containerId, playerInventory, this);
        }
        return null;
    }

    public IEnergyStorage getEnergyStorage(@Nullable Direction side) {
        return this.energyStorage;
    }
}

