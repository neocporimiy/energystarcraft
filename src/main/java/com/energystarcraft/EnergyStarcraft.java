package com.energystarcraft;

import com.energystarcraft.registry.ModBlockEntities;
import com.energystarcraft.registry.ModBlocks;
import com.energystarcraft.registry.ModCreativeTabs;
import com.energystarcraft.registry.ModItems;
import com.energystarcraft.registry.ModMenuTypes;
import com.energystarcraft.screen.EnergyForgeScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value="energystarcraft")
public class EnergyStarcraft {
    public static final String MOD_ID = "energystarcraft";
    public static final Logger LOGGER = LoggerFactory.getLogger((String)"energystarcraft");
    public static final int NETHER_STAR_COST = 350000000;

    public EnergyStarcraft(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);
    }

    @EventBusSubscriber(modid="energystarcraft", value={Dist.CLIENT})
    public static class ClientEvents {
        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.ENERGY_FORGE_MENU.get(), EnergyForgeScreen::new);
        }
    }
}

