package keormel.newermind.registry;

import keormel.newermind.NewermindMobs;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, NewermindMobs.MODID);

    public static final RegistryObject<Item> GUEST_SPAWN_EGG = ITEMS.register("guest_spawn_egg", () -> new ForgeSpawnEggItem(ModEntityTypes.GUEST, 0x111014, 0xd7d3c8, new Item.Properties()));

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
