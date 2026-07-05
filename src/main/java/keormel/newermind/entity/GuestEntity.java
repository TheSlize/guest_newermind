package keormel.newermind.entity;

import keormel.newermind.network.GuestJumpscareEffectPacket;
import keormel.newermind.network.ModMessages;
import keormel.newermind.registry.ModSoundEvents;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.util.Mth;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GuestEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(GuestEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_TEST_ANIMATION = SynchedEntityData.defineId(GuestEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_STALKING_ANIMATION = SynchedEntityData.defineId(GuestEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_CHASE_ANIMATION = SynchedEntityData.defineId(GuestEntity.class, EntityDataSerializers.STRING);

    public static final String WINDOW_LOOK_ANIMATION = "window_look_animation";
    public static final String AFAR_V1_STALKER_ANIMATION = "afar_v1_stalker_animation";
    public static final String AFAR_V2_STALKER_ANIMATION = "afar_v2_stalker_animation";
    public static final String PASSAGE1X2_V1_STALKER_ANIMATION = "passage1x2_v1_stalker_animation";
    public static final String WINDOW_V1_STALKER_ANIMATION = "window_v1_stalker_animation";
    public static final String WINDOW_V2_STALKER_ANIMATION = "window_v2_stalker_animation";
    public static final String WINDOW_V3_STALKER_ANIMATION = "window_v3_stalker_animation";
    public static final String WINDOW_V4_STALKER_ANIMATION = "window_v4_stalker_animation";
    public static final String WINDOW_V5_STALKER_ANIMATION = "window_v5_stalker_animation";
    public static final String CORNER_V1_STALKER_ANIMATION = "corner_v1_stalker_animation";
    public static final String CORNER_V2_STALKER_ANIMATION = "corner_v2_stalker_animation";
    public static final String CORNER_V3_STALKER_ANIMATION = "corner_v3_stalker_animation";
    public static final String CEILING_V1_STALKER_ANIMATION = "ceiling_v1_stalker_animation";
    public static final String BED_V1_STALKER_ANIMATION = "bed_v1_stalker_animation";
    public static final String UNDER_BED_V1_STALKER_ANIMATION = "under_bed_v1_stalker_animation";
    public static final String SPAWN_ANIMATION_NAME = "spawn_animation";
    public static final String IDLE_ANIMATION_NAME = "idle_animation";
    public static final String AGRO_ANIMATION_NAME = "agro_animation";
    public static final String IDLE_AGRO_ANIMATION_NAME = "idle_agro_animation";
    public static final String WALKING_ANIMATION_NAME = "walking_animation";
    public static final String RUNNING_ANIMATION_NAME = "running_animation";
    public static final String PASSAGE1X2_WALKING_ANIMATION = "passage1x2_walking_animation";
    public static final String PASSAGE1X1_WALKING_ANIMATION = "passage1x1_walking_animation";
    public static final String CLIMBING_ANIMATION = "climbing_animation";
    public static final String SWIMMING_ANIMATION = "swimming_animation";
    public static final String JUMPSCARE_ANIMATION_NAME = "jumpscare_animation";

    public static final String[] TEST_ANIMATION_NAMES = {
            WINDOW_LOOK_ANIMATION,
            AFAR_V1_STALKER_ANIMATION,
            AFAR_V2_STALKER_ANIMATION,
            PASSAGE1X2_V1_STALKER_ANIMATION,
            WINDOW_V1_STALKER_ANIMATION,
            WINDOW_V2_STALKER_ANIMATION,
            WINDOW_V3_STALKER_ANIMATION,
            WINDOW_V4_STALKER_ANIMATION,
            WINDOW_V5_STALKER_ANIMATION,
            CORNER_V1_STALKER_ANIMATION,
            CORNER_V2_STALKER_ANIMATION,
            CORNER_V3_STALKER_ANIMATION,
            CEILING_V1_STALKER_ANIMATION,
            BED_V1_STALKER_ANIMATION,
            UNDER_BED_V1_STALKER_ANIMATION,
            SPAWN_ANIMATION_NAME,
            IDLE_ANIMATION_NAME,
            AGRO_ANIMATION_NAME,
            IDLE_AGRO_ANIMATION_NAME,
            WALKING_ANIMATION_NAME,
            RUNNING_ANIMATION_NAME,
            PASSAGE1X2_WALKING_ANIMATION,
            PASSAGE1X1_WALKING_ANIMATION,
            CLIMBING_ANIMATION,
            SWIMMING_ANIMATION,
            JUMPSCARE_ANIMATION_NAME
    };

    private static final String[] STALKER_ANIMATION_NAMES = {
            AFAR_V1_STALKER_ANIMATION,
            AFAR_V2_STALKER_ANIMATION,
            PASSAGE1X2_V1_STALKER_ANIMATION,
            WINDOW_V1_STALKER_ANIMATION,
            WINDOW_V2_STALKER_ANIMATION,
            WINDOW_V3_STALKER_ANIMATION,
            WINDOW_V4_STALKER_ANIMATION,
            WINDOW_V5_STALKER_ANIMATION,
            CORNER_V1_STALKER_ANIMATION,
            CORNER_V2_STALKER_ANIMATION,
            CORNER_V3_STALKER_ANIMATION,
            CEILING_V1_STALKER_ANIMATION,
            BED_V1_STALKER_ANIMATION,
            UNDER_BED_V1_STALKER_ANIMATION
    };

    private static final Map<String, RawAnimation> LOOPING_ANIMATIONS = Arrays.stream(TEST_ANIMATION_NAMES).collect(Collectors.toUnmodifiableMap(Function.identity(), name -> RawAnimation.begin().thenLoop(name)));
    private static final RawAnimation SPAWN_ANIMATION = RawAnimation.begin().thenPlay("spawn_animation");
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("idle_animation");
    private static final RawAnimation AGRO_ANIMATION = RawAnimation.begin().thenPlay("agro_animation");
    private static final RawAnimation IDLE_AGRO_ANIMATION = RawAnimation.begin().thenLoop("idle_agro_animation");
    private static final RawAnimation JUMPSCARE_ANIMATION = RawAnimation.begin().thenPlayAndHold("jumpscare_animation");

    private static final int SEARCH_TIMEOUT_TICKS = 600 * 20;
    private static final int CHASE_TIMEOUT_TICKS = 600 * 20;
    private static final int JUMPSCARE_ANIMATION_TICKS = 39;
    private static final int JUMPSCARE_EFFECT_TICK = JUMPSCARE_ANIMATION_TICKS - 1;
    private static final int JUMPSCARE_DISCARD_TICKS = JUMPSCARE_ANIMATION_TICKS + 2;
    private static final int JUMPSCARE_INVERSION_TICKS = 20;
    private static final int SPAWN_ANIMATION_TICKS = 75;
    private static final double SEARCH_RANGE = 32.0D;
    private static final double CHASE_RANGE = 96.0D;
    private static final double CATCH_DISTANCE_SQR = 4.0D;
    private static final double BASE_MOVEMENT_SPEED = 0.38D;
    private static final double WALK_SPEED_MODIFIER = 0.65D;
    private static final double RUN_SPEED_MODIFIER = 1.55D;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int phaseTicks;
    private boolean jumpscareDamageDone;
    private Phase lastClientPhase = Phase.SEARCHING;

    public GuestEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1.0D).add(Attributes.FOLLOW_RANGE, CHASE_RANGE).add(Attributes.MOVEMENT_SPEED, BASE_MOVEMENT_SPEED).add(Attributes.ATTACK_DAMAGE, 6.0D).add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_PHASE, Phase.SEARCHING.id);
        this.entityData.define(DATA_TEST_ANIMATION, "");
        this.entityData.define(DATA_STALKING_ANIMATION, AFAR_V1_STALKER_ANIMATION);
        this.entityData.define(DATA_CHASE_ANIMATION, IDLE_AGRO_ANIMATION_NAME);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            tickClientPhaseCounter();
        }
    }

    private void tickClientPhaseCounter() {
        Phase phase = getPhase();
        if (this.lastClientPhase != phase) {
            this.lastClientPhase = phase;
            this.phaseTicks = 0;
            this.jumpscareDamageDone = false;
            return;
        }

        this.phaseTicks++;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        keepImmuneStateClean();
        this.phaseTicks++;

        switch (getPhase()) {
            case STALKING -> tickStalking();
            case SEARCHING -> tickSearching();
            case CHASING -> tickChasing();
            case JUMPSCARE -> tickJumpscare();
        }
    }

    private void tickStalking() {
        this.getNavigation().stop();
        if (this.phaseTicks >= getAnimationTicks(getStalkingAnimation())) {
            setPhase(Phase.SEARCHING);
        }
    }

    private void tickSearching() {
        this.getNavigation().stop();
        this.setTarget(null);

        if (this.phaseTicks == 1) {
            playGuestSound(ModSoundEvents.GUEST_SPAWN_SOUND.get(), 1.0F, 1.0F);
        }

        if (this.phaseTicks % 100 == 0) {
            playGuestSound(ModSoundEvents.GUEST_IDLE_NOICE.get(), 0.55F, 1.0F);
        }

        if (this.phaseTicks >= SEARCH_TIMEOUT_TICKS) {
            this.discard();
            return;
        }

        if (this.phaseTicks % 20 != 0) {
            return;
        }

        Player player = findNearestPlayer(SEARCH_RANGE);
        if (player != null && this.hasLineOfSight(player)) {
            this.setTarget(player);
            playGuestSound(ModSoundEvents.GUEST_PLAYER_FOUND.get(), 1.0F, 1.0F);
            setPhase(Phase.CHASING);
        }
    }

    private void tickChasing() {
        LivingEntity target = getTarget();
        if (!canChase(target)) {
            target = findNearestPlayer(CHASE_RANGE);
            this.setTarget(target);
        }

        if (!canChase(target)) {
            this.getNavigation().stop();
            setChaseAnimation(IDLE_AGRO_ANIMATION_NAME);
            if (this.phaseTicks >= CHASE_TIMEOUT_TICKS) {
                setPhase(Phase.SEARCHING);
            }
            return;
        }

        faceTargetHorizontally(target);
        boolean seenByTarget = target instanceof Player player && isPlayerLookingAtGuest(player);
        double speed = seenByTarget ? WALK_SPEED_MODIFIER : RUN_SPEED_MODIFIER;
        setChaseAnimation(seenByTarget ? WALKING_ANIMATION_NAME : RUNNING_ANIMATION_NAME);
        this.getNavigation().moveTo(target, speed);

        if (this.phaseTicks % 80 == 0) {
            playGuestSound(seenByTarget ? ModSoundEvents.GUEST_IDLE_NOICE.get() : ModSoundEvents.GUEST_FOOTSTEP.get(), 0.65F, 1.0F);
        }

        if (this.distanceToSqr(target) <= CATCH_DISTANCE_SQR) {
            setPhase(Phase.JUMPSCARE);
            this.setTarget(target);
            playGuestSound(ModSoundEvents.GUEST_JUMPSCARE.get(), 1.0F, 1.0F);
            return;
        }

        if (this.phaseTicks >= CHASE_TIMEOUT_TICKS) {
            setPhase(Phase.SEARCHING);
        }
    }

    private void tickJumpscare() {
        this.getNavigation().stop();
        LivingEntity target = getTarget();
        if (target != null) {
            if (this.phaseTicks == 1) {
                moveIntoJumpscarePose(target);
            }
            this.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
            if (target instanceof ServerPlayer serverPlayer && !serverPlayer.isCreative() && !serverPlayer.isSpectator()) {
                holdPlayerInHands(serverPlayer);
            }
        }

        if (!this.jumpscareDamageDone && this.phaseTicks >= JUMPSCARE_EFFECT_TICK) {
            this.jumpscareDamageDone = true;
            if (target instanceof Player player && !player.isCreative() && !player.isSpectator()) {
                player.hurt(this.damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE));
                if (player instanceof ServerPlayer serverPlayer) {
                    ModMessages.sendToPlayer(serverPlayer, new GuestJumpscareEffectPacket(JUMPSCARE_INVERSION_TICKS));
                }
            }
        }

        if (this.phaseTicks >= JUMPSCARE_DISCARD_TICKS) {
            this.discard();
        }
    }

    private void moveIntoJumpscarePose(LivingEntity target) {
        Vec3 targetLook = target.getLookAngle();
        Vec3 horizontalLook = new Vec3(targetLook.x, 0.0D, targetLook.z);
        if (horizontalLook.lengthSqr() < 1.0E-4D) {
            horizontalLook = Vec3.directionFromRotation(0.0F, target.getYRot());
        } else {
            horizontalLook = horizontalLook.normalize();
        }

        Vec3 targetPos = target.position().add(horizontalLook.scale(1.2D));
        this.moveTo(targetPos.x, target.getY(), targetPos.z, target.getYRot() + 180.0F, 0.0F);
        faceTargetHorizontally(target);
    }

    private void holdPlayerInHands(ServerPlayer player) {
        Vec3 guestLook = Vec3.directionFromRotation(0.0F, this.getYRot()).normalize();
        Vec3 holdPos = this.position().add(guestLook.scale(1.05D));
        float playerYaw = yawFromTo(holdPos, this.position());
        player.teleportTo(holdPos.x, this.getY(), holdPos.z);
        player.setDeltaMovement(Vec3.ZERO);
        player.setYRot(playerYaw);
        player.setYHeadRot(playerYaw);
        player.setYBodyRot(playerYaw);
        player.setXRot(-22.0F);
        player.hurtMarked = true;
    }

    private static float yawFromTo(Vec3 from, Vec3 to) {
        Vec3 delta = to.subtract(from);
        return Mth.wrapDegrees((float) (Mth.atan2(delta.z, delta.x) * Mth.RAD_TO_DEG) - 90.0F);
    }

    private Player findNearestPlayer(double range) {
        TargetingConditions conditions = TargetingConditions.forCombat().range(range).ignoreLineOfSight().selector(livingEntity -> livingEntity instanceof Player player && !player.isCreative() && !player.isSpectator());
        return this.level().getNearestPlayer(conditions, this);
    }

    private boolean canChase(LivingEntity target) {
        return target instanceof Player player && target.isAlive() && !player.isCreative() && !player.isSpectator() && this.distanceToSqr(target) <= CHASE_RANGE * CHASE_RANGE;
    }

    private boolean isPlayerLookingAtGuest(Player player) {
        Vec3 playerLook = player.getLookAngle().normalize();
        Vec3 toGuest = this.getEyePosition().subtract(player.getEyePosition()).normalize();
        return playerLook.dot(toGuest) > 0.72D && player.hasLineOfSight(this);
    }

    private void faceTargetHorizontally(LivingEntity target) {
        Vec3 delta = target.position().subtract(this.position());
        if (delta.horizontalDistanceSqr() < 1.0E-4D) {
            return;
        }

        float yaw = (float) (Mth.atan2(delta.z, delta.x) * Mth.RAD_TO_DEG) - 90.0F;
        yaw = Mth.wrapDegrees(yaw);
        this.setYRot(yaw);
        this.setYHeadRot(yaw);
        this.setYBodyRot(yaw);
    }

    private void playGuestSound(SoundEvent soundEvent, float volume, float pitch) {
        this.playSound(soundEvent, volume, pitch);
    }

    private void keepImmuneStateClean() {
        this.clearFire();
        this.setAirSupply(this.getMaxAirSupply());
        if (this.getHealth() < this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "guest_controller", 0, this::selectAnimation));
    }

    private PlayState selectAnimation(AnimationState<GuestEntity> state) {
        String testAnimation = getTestAnimation();
        if (!testAnimation.isEmpty()) {
            return state.setAndContinue(getLoopingAnimation(testAnimation));
        }

        RawAnimation animation = switch (getPhase()) {
            case STALKING -> getLoopingAnimation(getStalkingAnimation());
            case SEARCHING -> this.phaseTicks < SPAWN_ANIMATION_TICKS ? SPAWN_ANIMATION : IDLE_ANIMATION;
            case CHASING -> this.phaseTicks < 10 ? AGRO_ANIMATION : getLoopingAnimation(getChaseAnimation());
            case JUMPSCARE -> JUMPSCARE_ANIMATION;
        };

        return state.setAndContinue(animation);
    }

    private RawAnimation getLoopingAnimation(String animationName) {
        return LOOPING_ANIMATIONS.getOrDefault(animationName, IDLE_ANIMATION);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    public Phase getPhase() {
        return Phase.byId(this.entityData.get(DATA_PHASE));
    }

    public int getPhaseTicks() {
        return this.phaseTicks;
    }

    public void setPhase(Phase phase) {
        if (getPhase() != phase) {
            restartPhase(phase);
        }
    }

    public void restartPhase(Phase phase) {
        this.entityData.set(DATA_PHASE, phase.id);
        this.phaseTicks = 0;
        this.jumpscareDamageDone = false;
        if (phase == Phase.STALKING) {
            setStalkingAnimation(STALKER_ANIMATION_NAMES[this.random.nextInt(STALKER_ANIMATION_NAMES.length)]);
        } else if (phase == Phase.CHASING) {
            setChaseAnimation(IDLE_AGRO_ANIMATION_NAME);
        }
    }

    public String getTestAnimation() {
        return this.entityData.get(DATA_TEST_ANIMATION);
    }

    public void setTestAnimation(String animationName) {
        this.entityData.set(DATA_TEST_ANIMATION, isKnownAnimation(animationName) ? animationName : "");
    }

    public void clearTestAnimation() {
        this.entityData.set(DATA_TEST_ANIMATION, "");
    }

    public String getStalkingAnimation() {
        return this.entityData.get(DATA_STALKING_ANIMATION);
    }

    public void setStalkingAnimation(String animationName) {
        if (isKnownAnimation(animationName)) {
            this.entityData.set(DATA_STALKING_ANIMATION, animationName);
        }
    }

    public String getChaseAnimation() {
        return this.entityData.get(DATA_CHASE_ANIMATION);
    }

    private void setChaseAnimation(String animationName) {
        if (isKnownAnimation(animationName) && !animationName.equals(getChaseAnimation())) {
            this.entityData.set(DATA_CHASE_ANIMATION, animationName);
        }
    }

    public boolean shouldLockRootYawRoll() {
        if (!getTestAnimation().isEmpty()) {
            return false;
        }

        return getPhase() == Phase.SEARCHING || getPhase() == Phase.CHASING;
    }

    public static boolean isKnownAnimation(String animationName) {
        return LOOPING_ANIMATIONS.containsKey(animationName);
    }

    private static int getAnimationTicks(String animationName) {
        return switch (animationName) {
            case AFAR_V1_STALKER_ANIMATION -> 107;
            case AFAR_V2_STALKER_ANIMATION -> 100;
            case PASSAGE1X2_V1_STALKER_ANIMATION, CORNER_V3_STALKER_ANIMATION -> 80;
            case WINDOW_V1_STALKER_ANIMATION -> 45;
            case WINDOW_V2_STALKER_ANIMATION, WINDOW_V5_STALKER_ANIMATION, CORNER_V2_STALKER_ANIMATION -> 50;
            case WINDOW_V3_STALKER_ANIMATION, WINDOW_V4_STALKER_ANIMATION -> 75;
            case CORNER_V1_STALKER_ANIMATION -> 25;
            case CEILING_V1_STALKER_ANIMATION -> 61;
            case BED_V1_STALKER_ANIMATION -> 220;
            case UNDER_BED_V1_STALKER_ANIMATION -> 161;
            default -> 75;
        };
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        return false;
    }

    @Override
    public void knockback(double strength, double x, double z) {
    }

    @Override
    public void push(double x, double y, double z) {
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean startRiding(Entity vehicle, boolean force) {
        return false;
    }

    @Override
    public boolean startRiding(Entity vehicle) {
        return false;
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    @Override
    public void lavaHurt() {
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, net.minecraft.world.entity.EntityDimensions size) {
        return 2.62F;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setPhase(Phase.byId(tag.getInt("Phase")));
        this.phaseTicks = tag.getInt("PhaseTicks");
        this.jumpscareDamageDone = tag.getBoolean("JumpscareDamageDone");
        setTestAnimation(tag.getString("TestAnimation"));
        setStalkingAnimation(tag.getString("StalkingAnimation"));
        setChaseAnimation(tag.getString("ChaseAnimation"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Phase", getPhase().id);
        tag.putInt("PhaseTicks", this.phaseTicks);
        tag.putBoolean("JumpscareDamageDone", this.jumpscareDamageDone);
        tag.putString("TestAnimation", getTestAnimation());
        tag.putString("StalkingAnimation", getStalkingAnimation());
        tag.putString("ChaseAnimation", getChaseAnimation());
    }

    public enum Phase {
        STALKING(0),
        SEARCHING(1),
        CHASING(2),
        JUMPSCARE(3);

        private final int id;

        Phase(int id) {
            this.id = id;
        }

        public String getSerializedName() {
            return name().toLowerCase();
        }

        public static Phase byId(int id) {
            for (Phase phase : values()) {
                if (phase.id == id) {
                    return phase;
                }
            }
            return SEARCHING;
        }

        public static Phase byName(String name) {
            for (Phase phase : values()) {
                if (phase.getSerializedName().equals(name)) {
                    return phase;
                }
            }
            return null;
        }
    }
}
