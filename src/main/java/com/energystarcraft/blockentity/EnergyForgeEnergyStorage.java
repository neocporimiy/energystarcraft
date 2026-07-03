/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.neoforge.energy.EnergyStorage
 */
package com.energystarcraft.blockentity;

import net.neoforged.neoforge.energy.EnergyStorage;

public class EnergyForgeEnergyStorage
extends EnergyStorage {
    public static final int MAX_CAPACITY = 350000000;
    public static final int MAX_RECEIVE = 1000000;
    private final Runnable onChanged;

    public EnergyForgeEnergyStorage(Runnable onChanged) {
        super(350000000, 1000000, 0);
        this.onChanged = onChanged;
    }

    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        if (received > 0 && !simulate) {
            this.onChanged.run();
        }
        return received;
    }

    public int extractInternal(int amount) {
        int extracted = Math.min(this.energy, amount);
        this.energy -= extracted;
        if (extracted > 0) {
            this.onChanged.run();
        }
        return extracted;
    }

    public void setEnergy(int value) {
        this.energy = Math.max(0, Math.min(value, this.capacity));
    }

    public int getEnergy() {
        return this.energy;
    }
}

