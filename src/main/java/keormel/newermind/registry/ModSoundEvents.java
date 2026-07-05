package keormel.newermind.registry;

import keormel.newermind.NewermindMobs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, NewermindMobs.MODID);

    public static final RegistryObject<SoundEvent> GUEST_AFAR_NOICE = register("guest.afar_noice");
    public static final RegistryObject<SoundEvent> GUEST_WINDOW_NOICE = register("guest.window_noice");
    public static final RegistryObject<SoundEvent> GUEST_NOICE = register("guest.noice");
    public static final RegistryObject<SoundEvent> GUEST_SPAWN_SOUND = register("guest.spawn_sound");
    public static final RegistryObject<SoundEvent> GUEST_IDLE_NOICE = register("guest.idle_noice");
    public static final RegistryObject<SoundEvent> GUEST_PLAYER_FOUND = register("guest.player_found");
    public static final RegistryObject<SoundEvent> GUEST_FOOTSTEP = register("guest.footstep");
    public static final RegistryObject<SoundEvent> GUEST_JUMPSCARE = register("guest.jumpscare");

    private ModSoundEvents() {
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(NewermindMobs.MODID, name)));
    }
}
