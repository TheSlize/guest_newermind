package keormel.newermind.event;

import keormel.newermind.NewermindMobs;
import keormel.newermind.entity.GuestEntity;
import keormel.newermind.registry.ModEntityTypes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NewermindMobs.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModEventBusEvents {
    private ModEventBusEvents() {
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.GUEST.get(), GuestEntity.createAttributes().build());
    }
}
