package keormel.newermind;

import com.mojang.logging.LogUtils;
import keormel.newermind.client.GuestRenderer;
import keormel.newermind.network.ModMessages;
import keormel.newermind.registry.ModEntityTypes;
import keormel.newermind.registry.ModItems;
import keormel.newermind.registry.ModSoundEvents;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(NewermindMobs.MODID)
public class NewermindMobs {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "newermind_mobs_v2";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold CreativeModeTabs under the mod namespace.
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<CreativeModeTab> NEWERMIND_TAB = CREATIVE_MODE_TABS.register("newermind_tab", () -> CreativeModeTab.builder().withTabsBefore(CreativeModeTabs.SPAWN_EGGS).title(Component.translatable("itemGroup.newermind_mobs_v2")).icon(() -> ModItems.GUEST_SPAWN_EGG.get().getDefaultInstance()).displayItems((parameters, output) -> output.accept(ModItems.GUEST_SPAWN_EGG.get())).build());

    public NewermindMobs() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        ModEntityTypes.register(modEventBus);
        ModItems.register(modEventBus);
        ModSoundEvents.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        ModMessages.register();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Loaded Guest entity for {}", MODID);
    }

    // Add the spawn egg to the vanilla spawn egg tab too.
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) event.accept(ModItems.GUEST_SPAWN_EGG);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Guest spawn handler is ready");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntityTypes.GUEST.get(), GuestRenderer::new);
        }
    }
}
