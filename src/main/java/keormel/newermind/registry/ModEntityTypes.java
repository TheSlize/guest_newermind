package keormel.newermind.registry;

import keormel.newermind.NewermindMobs;
import keormel.newermind.entity.GuestEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NewermindMobs.MODID);

    public static final RegistryObject<EntityType<GuestEntity>> GUEST = ENTITY_TYPES.register("guest", () -> EntityType.Builder.of(GuestEntity::new, MobCategory.MONSTER).sized(0.75F, 4.375F).fireImmune().clientTrackingRange(10).updateInterval(3).build(new ResourceLocation(NewermindMobs.MODID, "guest").toString()));

    private ModEntityTypes() {
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
