// EnergyForgeMenu.java
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

    // ── Размеры панели ──────────────────────────────────────────────
    public static final int GUI_W        = 176;
    public static final int GUI_H        = 166;

    // ── Энергетический бар ──────────────────────────────────────────
    public static final int BAR_X        = 8;
    public static final int BAR_Y        = 17;
    public static final int BAR_W        = 16;
    public static final int BAR_H        = 52;

    // ── Разделитель ─────────────────────────────────────────────────
    public static final int DIVIDER_Y    = 75;

    // ── Выходной слот ───────────────────────────────────────────────
    // Центр верхней панели по вертикали: (3 + 75) / 2 = 39, слот 16px → y = 31
    public static final int OUTPUT_SLOT_X = 110;
    public static final int OUTPUT_SLOT_Y = 31;

    // ── Инвентарь игрока ────────────────────────────────────────────
    // Ставим ближе к разделителю: divider на y=75, отступ 8px → y=83
    public static final int INV_START_X  = 8;
    public static final int INV_START_Y  = 83;   // было 90
    public static final int HOTBAR_Y     = 141;  // INV_START_Y + 3*18 + 4 = 141

    // ── Максимум энергии ────────────────────────────────────────────
    public static final int MAX_ENERGY   = 350_000_000;

    // ── Внутреннее состояние ────────────────────────────────────────
    private final EnergyForgeBlockEntity blockEntity;
    private final ContainerData          containerData;

    // ════════════════════════════════════════════════════════════════
    //  Конструктор (сервер → клиент через BlockEntity)
    // ════════════════════════════════════════════════════════════════
    public EnergyForgeMenu(int containerId,
                           Inventory playerInventory,
                           EnergyForgeBlockEntity blockEntity) {
        super(ModMenuTypes.ENERGY_FORGE_MENU.get(), containerId);
        this.blockEntity    = blockEntity;
        this.containerData  = blockEntity.getContainerData();

        // Слот 0 — выход (только забрать)
        this.addSlot(new OutputOnlySlot(
                blockEntity.getOutputSlot(), 0,
                OUTPUT_SLOT_X, OUTPUT_SLOT_Y));

        // Слоты 1–27 — основной инвентарь игрока
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(
                        (Container) playerInventory,
                        col + row * 9 + 9,
                        INV_START_X + col * 18,
                        INV_START_Y + row * 18));
            }
        }

        // Слоты 28–36 — хотбар
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(
                    (Container) playerInventory,
                    col,
                    INV_START_X + col * 18,
                    HOTBAR_Y));
        }

        this.addDataSlots(this.containerData);
    }

    // ════════════════════════════════════════════════════════════════
    //  Конструктор (клиент — читает BlockPos из пакета)
    // ════════════════════════════════════════════════════════════════
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

    // ════════════════════════════════════════════════════════════════
    //  Данные энергии (ContainerData хранит только short-диапазон)
    //  slot 0 = low 16 бит, slot 1 = high 16 бит, slot 2 = статус
    // ════════════════════════════════════════════════════════════════
    public int getEnergyStored() {
        int low  = containerData.get(0) & 0xFFFF;
        int high = containerData.get(1) & 0xFFFF;
        return low | (high << 16);
    }

    public int getMaxEnergyStored() {
        return MAX_ENERGY;
    }

    /** 0–100 */
    public int getEnergyPercent() {
        int max = getMaxEnergyStored();
        if (max == 0) return 0;
        return (int) ((long) getEnergyStored() * 100L / (long) max);
    }

    /** Масштабирует текущую энергию в пиксели (0..height) */
    public int getScaledEnergy(int height) {
        int max = getMaxEnergyStored();
        if (max == 0) return 0;
        return (int) ((long) getEnergyStored() * (long) height / (long) max);
    }

    /** 0 = зарядка, 1 = крафт идёт */
    public int getCraftingStatus() {
        return containerData.get(2);
    }

    // ════════════════════════════════════════════════════════════════
    //  Стандартные переопределения
    // ════════════════════════════════════════════════════════════════
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

        // slot 0  → инвентарь/хотбар (1–36)
        // slot 1–27 (инв) → хотбар (28–36) или обратно
        // slot 28–36 (хот) → инвентарь (1–27)
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

    // ════════════════════════════════════════════════════════════════
    //  Вложенный класс — слот только для вывода
    // ════════════════════════════════════════════════════════════════
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