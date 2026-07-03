/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.inventory.MenuType
 *  net.neoforged.neoforge.common.extensions.IMenuTypeExtension
 *  net.neoforged.neoforge.registries.DeferredRegister
 */
package com.energystarcraft.registry;

import com.energystarcraft.menu.EnergyForgeMenu;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create((ResourceKey)Registries.MENU, (String)"energystarcraft");
    public static final Supplier<MenuType<EnergyForgeMenu>> ENERGY_FORGE_MENU = MENUS.register("energy_forge", () -> IMenuTypeExtension.create(EnergyForgeMenu::new));

    private ModMenuTypes() {
    }
}

