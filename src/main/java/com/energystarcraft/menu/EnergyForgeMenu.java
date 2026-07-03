package com.energystarcraft.menu;

import com.energystarcraft.blockentity.EnergyForgeBlockEntity;
import com.energystarcraft.registry.ModBlocks;
import com.energystarcraft.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class EnergyForgeMenu extends AbstractContainerMenu {
    public static final int GUI_W        = 176;
    public static final int GUI_H        = 166;
    public static final int BAR_X        = 8;
    public static final int BAR_Y        = 17;
    public static final int BAR_W        = 16;
    public static final int BAR_H        = 52;
    public static final int DIVIDER_Y    = 75;
    public static final int OUTPUT_SLOT_X = 110;
    public static final int OUTPUT_SLOT_Y = 31;
    public static final int INV_START_X  = 8;
    public static final int INV_START_Y  = 83;
    public static final int HOTBAR_Y     = 141;
    public static final int MAX_ENERGY   = 350_000_000;
    private final EnergyForgeBlockEntity blockEntity;
    private final ContainerData          containerData;
    public EnergyForgeMenu(int containerId,
                           Inventory playerInventory,
                           EnergyForgeBlockEntity blockEntity) {
        super(ModMenuTypes.ENERGY_FORGE_MENU.get(), containerId);
        this.blockEntity    = blockEntity;
        this.containerData  = blockEntity.getContainerData();
        this.addSlot(new OutputOnlySlot(
                blockEntity.getOutputSlot(), 0,
                OUTPUT_SLOT_X, OUTPUT_SLOT_Y));
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(
                        (Container) playerInventory,
                        col + row * 9 + 9,
                        INV_START_X + col * 18,
                        INV_START_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(
                    (Container) playerInventory,
                    col,
                    INV_START_X + col * 18,
                    HOTBAR_Y));
        }

        this.addDataSlots(this.containerData);
    }
    public EnergyForgeMenu(int containerId,
                           Inventory playerInventory,
                           FriendlyByteBuf buf) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, buf));
    }

    private static EnergyForgeBlockEntity getBlockEntity(Inventory inv,
                                                          FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        BlockEntity be = inv.player.level().getBlockEntity(pos);
        if (be instanceof EnergyForgeBlockEntity forge) {
            return forge;
        }
        throw new IllegalStateException(
                "Not a EnergyForgeBlockEntity at " + pos);
    }

    public int getEnergyStored() {
        int low  = containerData.get(0) & 0xFFFF;
        int high = containerData.get(1) & 0xFFFF;
        return low | (high << 16);
    }

    public int getMaxEnergyStored() {
        return MAX_ENERGY;
    }
    public int getEnergyPercent() {
        int max = getMaxEnergyStored();
        if (max == 0) return 0;
        return (int) ((long) getEnergyStored() * 100L / (long) max);
    }

    public int getScaledEnergy(int height) {
        int max = getMaxEnergyStored();
        if (max == 0) return 0;
        return (int) ((long) getEnergyStored() * (long) height / (long) max);
    }

    public int getCraftingStatus() {
        return containerData.get(2);
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(
                ContainerLevelAccess.create(
                        (Level) blockEntity.getLevel(),
                        (BlockPos) blockEntity.getBlockPos()),
                player,
                (Block) ModBlocks.ENERGY_FORGE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return result;
        ItemStack stack = slot.getItem();
        result = stack.copy();
        boolean moved;
        if (index == 0) {
            moved = this.moveItemStackTo(stack, 1, 37, true);
        } else if (index < 28) {
            moved = this.moveItemStackTo(stack, 28, 37, false);
        } else {
            moved = this.moveItemStackTo(stack, 1, 28, false);
        }

        if (!moved) return ItemStack.EMPTY;

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == result.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return result;
    }
    private static class OutputOnlySlot extends SlotItemHandler {
        OutputOnlySlot(ItemStackHandler handler, int index, int x, int y) {
            super((IItemHandler) handler, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}