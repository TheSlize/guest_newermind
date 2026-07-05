package keormel.newermind.event;

import keormel.newermind.NewermindMobs;
import keormel.newermind.entity.GuestEntity;
import keormel.newermind.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = NewermindMobs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GuestSpawnHandler {
    private static final String TAG_SPAWN_TIMER = "NewermindGuestSpawnTimer";
    private static final String TAG_SPAWN_COOLDOWN = "NewermindGuestSpawnCooldown";

    private static final int SPAWN_CHECK_INTERVAL_TICKS = 900 * 20;
    private static final int POST_SPAWN_COOLDOWN_TICKS = 600 * 20;

    private GuestSpawnHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.CLIENT || event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        if (player.tickCount % 20 != 0 || player.isCreative() || player.isSpectator()) {
            return;
        }

        ServerLevel level = player.serverLevel();
        CompoundTag data = player.getPersistentData();
        int cooldown = data.getInt(TAG_SPAWN_COOLDOWN);
        if (cooldown > 0) {
            data.putInt(TAG_SPAWN_COOLDOWN, Math.max(0, cooldown - 20));
            return;
        }

        if (!isEveningOrNight(level)) {
            data.putInt(TAG_SPAWN_TIMER, 0);
            return;
        }

        int timer = data.getInt(TAG_SPAWN_TIMER) + 20;
        if (timer < SPAWN_CHECK_INTERVAL_TICKS) {
            data.putInt(TAG_SPAWN_TIMER, timer);
            return;
        }

        data.putInt(TAG_SPAWN_TIMER, 0);
        if (level.random.nextFloat() >= 0.25F || hasGuestNearPlayer(level, player)) {
            return;
        }

        if (spawnGuestNearPlayer(level, player)) {
            data.putInt(TAG_SPAWN_COOLDOWN, POST_SPAWN_COOLDOWN_TICKS);
        }
    }

    private static boolean isEveningOrNight(ServerLevel level) {
        long dayTime = level.getDayTime() % 24000L;
        return dayTime >= 12000L && dayTime <= 23999L;
    }

    private static boolean hasGuestNearPlayer(ServerLevel level, ServerPlayer player) {
        AABB searchBox = player.getBoundingBox().inflate(96.0D);
        return !level.getEntitiesOfClass(GuestEntity.class, searchBox).isEmpty();
    }

    private static boolean spawnGuestNearPlayer(ServerLevel level, ServerPlayer player) {
        Optional<GuestStalkingPlacements.Placement> placement = GuestStalkingPlacements.findRandom(level, player);
        if (placement.isEmpty()) {
            return false;
        }

        GuestEntity guest = ModEntityTypes.GUEST.get().create(level);
        if (guest == null) {
            return false;
        }

        GuestStalkingPlacements.Placement selected = placement.get();
        guest.moveTo(selected.pos().getX() + 0.5D, selected.pos().getY(), selected.pos().getZ() + 0.5D, selected.yRot(), 0.0F);
        guest.restartPhase(GuestEntity.Phase.STALKING);
        guest.setStalkingAnimation(selected.animation());
        guest.finalizeSpawn(level, level.getCurrentDifficultyAt(selected.pos()), MobSpawnType.NATURAL, null, null);
        return level.addFreshEntity(guest);
    }
}
