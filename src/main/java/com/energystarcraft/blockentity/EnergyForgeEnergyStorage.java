package com.energystarcraft.blockentity;

import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

public class EnergyForgeEnergyStorage extends SimpleEnergyHandler {
    public static final int MAX_CAPACITY = 350_000_000;
    public static final int MAX_RECEIVE = 1_000_000;
    private final Runnable onChanged;

    public EnergyForgeEnergyStorage(Runnable onChanged) {
        super(MAX_CAPACITY, MAX_RECEIVE, 0);
        this.onChanged = onChanged;
    }

    @Override
    protected void onEnergyChanged(int previousAmount) {
        this.onChanged.run();
    }

    public int extractInternal(int amount) {
        int extracted = Math.min(getAmountAsInt(), amount);
        if (extracted > 0) {
            set(getAmountAsInt() - extracted);
        }
        return extracted;
    }
}
