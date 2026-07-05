package keormel.newermind.event;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import keormel.newermind.NewermindMobs;
import keormel.newermind.entity.GuestEntity;
import keormel.newermind.registry.ModEntityTypes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = NewermindMobs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GuestTestCommands {
    private static final double NEAREST_GUEST_RANGE = 96.0D;
    private static final DynamicCommandExceptionType UNKNOWN_PHASE = new DynamicCommandExceptionType(value -> Component.literal("Unknown guest phase: " + value + "."));
    private static final DynamicCommandExceptionType UNKNOWN_ANIMATION = new DynamicCommandExceptionType(value -> Component.literal("Unknown guest animation: " + value + "."));

    private GuestTestCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("newermind").requires(source -> source.hasPermission(2))
                .then(Commands.literal("guest")
                        .then(Commands.literal("test")
                                .then(Commands.literal("phase")
                                        .then(Commands.argument("phase_number", IntegerArgumentType.integer(1, 4))
                                                .executes(context -> testPhase(context, IntegerArgumentType.getInteger(context, "phase_number"), 0))
                                                .then(Commands.literal("var")
                                                        .then(Commands.argument("variant", IntegerArgumentType.integer(GuestStalkingPlacements.MIN_VARIANT, GuestStalkingPlacements.MAX_VARIANT))
                                                                .executes(context -> testPhase(context, IntegerArgumentType.getInteger(context, "phase_number"), IntegerArgumentType.getInteger(context, "variant"))))))))
                        .then(Commands.literal("spawn")
                                .executes(context -> spawnGuest(context, GuestEntity.Phase.SEARCHING))
                                .then(Commands.argument("phase", StringArgumentType.word())
                                        .suggests(GuestTestCommands::suggestPhases)
                                        .executes(context -> spawnGuest(context, requirePhase(context)))))
                        .then(Commands.literal("phase_nearest")
                                .then(Commands.argument("phase", StringArgumentType.word())
                                        .suggests(GuestTestCommands::suggestPhases)
                                        .executes(context -> setNearestPhase(context, requirePhase(context)))))
                        .then(Commands.literal("phase")
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .then(Commands.argument("phase", StringArgumentType.word())
                                                .suggests(GuestTestCommands::suggestPhases)
                                                .executes(context -> setPhase(context, EntityArgument.getEntities(context, "targets"), requirePhase(context))))))
                        .then(Commands.literal("animation_nearest")
                                .then(Commands.argument("animation", StringArgumentType.word())
                                        .suggests(GuestTestCommands::suggestAnimations)
                                        .executes(context -> setNearestAnimation(context, requireAnimation(context)))))
                        .then(Commands.literal("animation")
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .then(Commands.argument("animation", StringArgumentType.word())
                                                .suggests(GuestTestCommands::suggestAnimations)
                                                .executes(context -> setAnimation(context, EntityArgument.getEntities(context, "targets"), requireAnimation(context))))))
                        .then(Commands.literal("stalker_nearest")
                                .then(Commands.argument("animation", StringArgumentType.word())
                                        .suggests(GuestTestCommands::suggestAnimations)
                                        .executes(context -> setNearestStalkerAnimation(context, requireAnimation(context)))))
                        .then(Commands.literal("stalker")
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .then(Commands.argument("animation", StringArgumentType.word())
                                                .suggests(GuestTestCommands::suggestAnimations)
                                                .executes(context -> setStalkerAnimation(context, EntityArgument.getEntities(context, "targets"), requireAnimation(context))))))
                        .then(Commands.literal("clear_animation_nearest")
                                .executes(GuestTestCommands::clearNearestAnimation))
                        .then(Commands.literal("clear_animation")
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .executes(context -> clearAnimation(context, EntityArgument.getEntities(context, "targets")))))
                        .then(Commands.literal("list_animations")
                                .executes(GuestTestCommands::listAnimations))));
    }

    private static int spawnGuest(CommandContext<CommandSourceStack> context, GuestEntity.Phase phase) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerLevel level = player.serverLevel();
        GuestEntity guest = ModEntityTypes.GUEST.get().create(level);
        if (guest == null) {
            context.getSource().sendFailure(Component.literal("Could not create newermind_mobs_v2:guest."));
            return 0;
        }

        Vec3 look = player.getLookAngle();
        Vec3 horizontalLook = new Vec3(look.x, 0.0D, look.z);
        if (horizontalLook.lengthSqr() < 1.0E-4D) {
            horizontalLook = Vec3.directionFromRotation(0.0F, player.getYRot());
        } else {
            horizontalLook = horizontalLook.normalize();
        }

        Vec3 spawnPos = player.position().add(horizontalLook.scale(3.0D));
        guest.moveTo(spawnPos.x, player.getY(), spawnPos.z, player.getYRot() + 180.0F, 0.0F);
        guest.finalizeSpawn(level, level.getCurrentDifficultyAt(guest.blockPosition()), MobSpawnType.COMMAND, null, null);
        applyPhase(guest, phase, player);
        if (!level.addFreshEntity(guest)) {
            context.getSource().sendFailure(Component.literal("Could not add newermind_mobs_v2:guest to the level."));
            return 0;
        }

        context.getSource().sendSuccess(() -> Component.literal("Spawned newermind_mobs_v2:guest in phase " + phase.getSerializedName() + "."), true);
        return 1;
    }

    private static int testPhase(CommandContext<CommandSourceStack> context, int phaseNumber, int variant) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerLevel level = player.serverLevel();
        GuestEntity.Phase phase = phaseByTestNumber(phaseNumber);
        if (phase == GuestEntity.Phase.STALKING) {
            return testStalkingPhase(context, level, player, variant);
        }

        GuestEntity guest = ModEntityTypes.GUEST.get().create(level);
        if (guest == null) {
            context.getSource().sendFailure(Component.literal("Could not create newermind_mobs_v2:guest."));
            return 0;
        }

        Vec3 look = player.getLookAngle();
        Vec3 horizontalLook = new Vec3(look.x, 0.0D, look.z);
        if (horizontalLook.lengthSqr() < 1.0E-4D) {
            horizontalLook = Vec3.directionFromRotation(0.0F, player.getYRot());
        } else {
            horizontalLook = horizontalLook.normalize();
        }

        Vec3 spawnPos = player.position().add(horizontalLook.scale(phase == GuestEntity.Phase.JUMPSCARE ? 1.35D : 3.0D));
        BlockPos blockPos = BlockPos.containing(spawnPos.x, player.getY(), spawnPos.z);
        guest.moveTo(spawnPos.x, player.getY(), spawnPos.z, player.getYRot() + 180.0F, 0.0F);
        guest.finalizeSpawn(level, level.getCurrentDifficultyAt(blockPos), MobSpawnType.COMMAND, null, null);
        applyPhase(guest, phase, player);
        if (!level.addFreshEntity(guest)) {
            context.getSource().sendFailure(Component.literal("Could not add newermind_mobs_v2:guest to the level."));
            return 0;
        }

        context.getSource().sendSuccess(() -> Component.literal("Spawned test Guest phase " + phaseNumber + " (" + phase.getSerializedName() + ")."), true);
        return 1;
    }

    private static int testStalkingPhase(CommandContext<CommandSourceStack> context, ServerLevel level, ServerPlayer player, int variant) {
        Optional<GuestStalkingPlacements.Placement> placement = variant == 0 ? GuestStalkingPlacements.findRandom(level, player) : GuestStalkingPlacements.find(level, player, variant);
        if (placement.isEmpty()) {
            context.getSource().sendFailure(Component.literal("Неподходящие условия для этого варианта слежки Гостя!"));
            return 0;
        }

        GuestEntity guest = ModEntityTypes.GUEST.get().create(level);
        if (guest == null) {
            context.getSource().sendFailure(Component.literal("Could not create newermind_mobs_v2:guest."));
            return 0;
        }

        GuestStalkingPlacements.Placement selected = placement.get();
        guest.moveTo(selected.pos().getX() + 0.5D, selected.pos().getY(), selected.pos().getZ() + 0.5D, selected.yRot(), 0.0F);
        guest.finalizeSpawn(level, level.getCurrentDifficultyAt(selected.pos()), MobSpawnType.COMMAND, null, null);
        guest.restartPhase(GuestEntity.Phase.STALKING);
        guest.setStalkingAnimation(selected.animation());
        if (!level.addFreshEntity(guest)) {
            context.getSource().sendFailure(Component.literal("Could not add newermind_mobs_v2:guest to the level."));
            return 0;
        }

        context.getSource().sendSuccess(() -> Component.literal("Spawned Guest stalking test variant " + selected.variant() + "."), true);
        return 1;
    }

    private static int setNearestPhase(CommandContext<CommandSourceStack> context, GuestEntity.Phase phase) {
        GuestEntity guest = findNearestGuest(context.getSource());
        if (guest == null) {
            context.getSource().sendFailure(Component.literal("No newermind_mobs_v2:guest found within " + (int) NEAREST_GUEST_RANGE + " blocks."));
            return 0;
        }

        applyPhase(guest, phase, getSourcePlayer(context.getSource()));
        context.getSource().sendSuccess(() -> Component.literal("Set nearest newermind_mobs_v2:guest phase to " + phase.getSerializedName() + "."), true);
        return 1;
    }

    private static int setPhase(CommandContext<CommandSourceStack> context, Collection<? extends Entity> targets, GuestEntity.Phase phase) {
        List<GuestEntity> guests = filterGuests(targets);
        if (guests.isEmpty()) {
            context.getSource().sendFailure(Component.literal("No newermind_mobs_v2:guest entities in selector."));
            return 0;
        }

        ServerPlayer player = getSourcePlayer(context.getSource());
        guests.forEach(guest -> applyPhase(guest, phase, player));
        context.getSource().sendSuccess(() -> Component.literal("Set phase " + phase.getSerializedName() + " on " + guests.size() + " guest(s)."), true);
        return guests.size();
    }

    private static int setNearestAnimation(CommandContext<CommandSourceStack> context, String animation) {
        GuestEntity guest = findNearestGuest(context.getSource());
        if (guest == null) {
            context.getSource().sendFailure(Component.literal("No newermind_mobs_v2:guest found within " + (int) NEAREST_GUEST_RANGE + " blocks."));
            return 0;
        }

        guest.setTestAnimation(animation);
        context.getSource().sendSuccess(() -> Component.literal("Forced nearest newermind_mobs_v2:guest animation: " + animation + "."), true);
        return 1;
    }

    private static int setAnimation(CommandContext<CommandSourceStack> context, Collection<? extends Entity> targets, String animation) {
        List<GuestEntity> guests = filterGuests(targets);
        if (guests.isEmpty()) {
            context.getSource().sendFailure(Component.literal("No newermind_mobs_v2:guest entities in selector."));
            return 0;
        }

        guests.forEach(guest -> guest.setTestAnimation(animation));
        context.getSource().sendSuccess(() -> Component.literal("Forced animation " + animation + " on " + guests.size() + " guest(s)."), true);
        return guests.size();
    }

    private static int setNearestStalkerAnimation(CommandContext<CommandSourceStack> context, String animation) {
        GuestEntity guest = findNearestGuest(context.getSource());
        if (guest == null) {
            context.getSource().sendFailure(Component.literal("No newermind_mobs_v2:guest found within " + (int) NEAREST_GUEST_RANGE + " blocks."));
            return 0;
        }

        guest.clearTestAnimation();
        guest.setStalkingAnimation(animation);
        guest.restartPhase(GuestEntity.Phase.STALKING);
        guest.setStalkingAnimation(animation);
        context.getSource().sendSuccess(() -> Component.literal("Set nearest newermind_mobs_v2:guest stalker animation: " + animation + "."), true);
        return 1;
    }

    private static int setStalkerAnimation(CommandContext<CommandSourceStack> context, Collection<? extends Entity> targets, String animation) {
        List<GuestEntity> guests = filterGuests(targets);
        if (guests.isEmpty()) {
            context.getSource().sendFailure(Component.literal("No newermind_mobs_v2:guest entities in selector."));
            return 0;
        }

        guests.forEach(guest -> {
            guest.clearTestAnimation();
            guest.setStalkingAnimation(animation);
            guest.restartPhase(GuestEntity.Phase.STALKING);
            guest.setStalkingAnimation(animation);
        });
        context.getSource().sendSuccess(() -> Component.literal("Set stalker animation " + animation + " on " + guests.size() + " guest(s)."), true);
        return guests.size();
    }

    private static int clearNearestAnimation(CommandContext<CommandSourceStack> context) {
        GuestEntity guest = findNearestGuest(context.getSource());
        if (guest == null) {
            context.getSource().sendFailure(Component.literal("No newermind_mobs_v2:guest found within " + (int) NEAREST_GUEST_RANGE + " blocks."));
            return 0;
        }

        guest.clearTestAnimation();
        context.getSource().sendSuccess(() -> Component.literal("Cleared forced animation on nearest newermind_mobs_v2:guest."), true);
        return 1;
    }

    private static int clearAnimation(CommandContext<CommandSourceStack> context, Collection<? extends Entity> targets) {
        List<GuestEntity> guests = filterGuests(targets);
        if (guests.isEmpty()) {
            context.getSource().sendFailure(Component.literal("No newermind_mobs_v2:guest entities in selector."));
            return 0;
        }

        guests.forEach(GuestEntity::clearTestAnimation);
        context.getSource().sendSuccess(() -> Component.literal("Cleared forced animation on " + guests.size() + " guest(s)."), true);
        return guests.size();
    }

    private static int listAnimations(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal(String.join(", ", GuestEntity.TEST_ANIMATION_NAMES)), false);
        return GuestEntity.TEST_ANIMATION_NAMES.length;
    }

    private static void applyPhase(GuestEntity guest, GuestEntity.Phase phase, ServerPlayer player) {
        guest.clearTestAnimation();
        guest.restartPhase(phase);
        if ((phase == GuestEntity.Phase.CHASING || phase == GuestEntity.Phase.JUMPSCARE) && player != null) {
            guest.setTarget(player);
        }
    }

    private static GuestEntity findNearestGuest(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        Vec3 center = source.getPosition();
        AABB searchBox = new AABB(center, center).inflate(NEAREST_GUEST_RANGE);
        return level.getEntitiesOfClass(GuestEntity.class, searchBox).stream()
                .min((left, right) -> Double.compare(left.distanceToSqr(center), right.distanceToSqr(center)))
                .orElse(null);
    }

    private static List<GuestEntity> filterGuests(Collection<? extends Entity> targets) {
        List<GuestEntity> guests = new ArrayList<>();
        for (Entity entity : targets) {
            if (entity instanceof GuestEntity guest) {
                guests.add(guest);
            }
        }
        return guests;
    }

    private static ServerPlayer getSourcePlayer(CommandSourceStack source) {
        try {
            return source.getPlayerOrException();
        } catch (CommandSyntaxException exception) {
            return null;
        }
    }

    private static GuestEntity.Phase phaseByTestNumber(int phaseNumber) {
        return switch (phaseNumber) {
            case 1 -> GuestEntity.Phase.STALKING;
            case 2 -> GuestEntity.Phase.SEARCHING;
            case 3 -> GuestEntity.Phase.CHASING;
            case 4 -> GuestEntity.Phase.JUMPSCARE;
            default -> GuestEntity.Phase.SEARCHING;
        };
    }

    private static GuestEntity.Phase requirePhase(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String phaseName = StringArgumentType.getString(context, "phase");
        GuestEntity.Phase phase = GuestEntity.Phase.byName(phaseName);
        if (phase == null) {
            throw UNKNOWN_PHASE.create(phaseName);
        }
        return phase;
    }

    private static String requireAnimation(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String animation = StringArgumentType.getString(context, "animation");
        if (!GuestEntity.isKnownAnimation(animation)) {
            throw UNKNOWN_ANIMATION.create(animation);
        }
        return animation;
    }

    private static CompletableFuture<Suggestions> suggestPhases(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        List<String> phases = new ArrayList<>();
        for (GuestEntity.Phase phase : GuestEntity.Phase.values()) {
            phases.add(phase.getSerializedName());
        }
        return SharedSuggestionProvider.suggest(phases, builder);
    }

    private static CompletableFuture<Suggestions> suggestAnimations(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(GuestEntity.TEST_ANIMATION_NAMES, builder);
    }
}
