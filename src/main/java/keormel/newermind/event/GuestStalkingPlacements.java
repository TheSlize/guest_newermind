package keormel.newermind.event;

import keormel.newermind.entity.GuestEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class GuestStalkingPlacements {
    public static final int MIN_VARIANT = 1;
    public static final int MAX_VARIANT = 4;

    private static final int MIN_FAR_DISTANCE = 18;
    private static final int MAX_FAR_DISTANCE = 38;
    private static final int MIN_CLOSE_DISTANCE = 5;
    private static final int MAX_CLOSE_DISTANCE = 16;
    private static final int MAX_ATTEMPTS = 96;

    private GuestStalkingPlacements() {
    }

    public static Optional<Placement> findRandom(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.random;
        int start = MIN_VARIANT + random.nextInt(MAX_VARIANT - MIN_VARIANT + 1);
        for (int offset = 0; offset <= MAX_VARIANT - MIN_VARIANT; offset++) {
            int variant = MIN_VARIANT + (start - MIN_VARIANT + offset) % (MAX_VARIANT - MIN_VARIANT + 1);
            Optional<Placement> placement = find(level, player, variant);
            if (placement.isPresent()) {
                return placement;
            }
        }
        return Optional.empty();
    }

    public static Optional<Placement> find(ServerLevel level, ServerPlayer player, int variant) {
        return switch (variant) {
            case 1 -> findFar(level, player, variant, GuestEntity.AFAR_V1_STALKER_ANIMATION, true);
            case 2 -> findFar(level, player, variant, GuestEntity.AFAR_V2_STALKER_ANIMATION, false);
            case 3 -> findPassage(level, player);
            case 4 -> findCorner(level, player);
            default -> Optional.empty();
        };
    }

    private static Optional<Placement> findFar(ServerLevel level, ServerPlayer player, int variant, String animation, boolean preferVisible) {
        RandomSource random = level.random;
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            int distance = MIN_FAR_DISTANCE + random.nextInt(MAX_FAR_DISTANCE - MIN_FAR_DISTANCE + 1);
            BlockPos spawnPos = findSpawnPositionNear(level, player, angle, distance, 10);
            if (spawnPos == null) {
                continue;
            }

            boolean visible = hasClearLine(level, player.getEyePosition(), eyePosition(spawnPos));
            if (preferVisible != visible && attempt < MAX_ATTEMPTS / 2) {
                continue;
            }

            return Optional.of(new Placement(spawnPos, yawFacingPlayer(spawnPos, player), animation, variant));
        }
        return Optional.empty();
    }

    private static Optional<Placement> findPassage(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.random;
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            int distance = MIN_CLOSE_DISTANCE + random.nextInt(MAX_CLOSE_DISTANCE - MIN_CLOSE_DISTANCE + 1);
            BlockPos spawnPos = findSpawnPositionNear(level, player, angle, distance, 6);
            if (spawnPos == null) {
                continue;
            }

            Direction towardPlayer = horizontalDirection(spawnPos, player.blockPosition());
            Direction left = towardPlayer.getCounterClockWise();
            Direction right = towardPlayer.getClockWise();
            if (isSolidWall(level, spawnPos.relative(left)) && isSolidWall(level, spawnPos.relative(right)) && isOpen(level, spawnPos.relative(towardPlayer)) && isOpen(level, spawnPos.relative(towardPlayer.getOpposite()))) {
                return Optional.of(new Placement(spawnPos, yawFacingPlayer(spawnPos, player), GuestEntity.PASSAGE1X2_V1_STALKER_ANIMATION, 3));
            }
        }
        return Optional.empty();
    }

    private static Optional<Placement> findCorner(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.random;
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            int distance = MIN_CLOSE_DISTANCE + random.nextInt(MAX_CLOSE_DISTANCE - MIN_CLOSE_DISTANCE + 1);
            BlockPos spawnPos = findSpawnPositionNear(level, player, angle, distance, 6);
            if (spawnPos == null) {
                continue;
            }

            Direction towardPlayer = horizontalDirection(spawnPos, player.blockPosition());
            Direction left = towardPlayer.getCounterClockWise();
            Direction right = towardPlayer.getClockWise();
            boolean sideWall = isSolidWall(level, spawnPos.relative(left)) || isSolidWall(level, spawnPos.relative(right));
            boolean frontOpen = isOpen(level, spawnPos.relative(towardPlayer));
            boolean hidden = !hasClearLine(level, player.getEyePosition(), eyePosition(spawnPos));
            if (sideWall && frontOpen && hidden) {
                return Optional.of(new Placement(spawnPos, yawFacingPlayer(spawnPos, player), GuestEntity.CORNER_V1_STALKER_ANIMATION, 4));
            }
        }
        return Optional.empty();
    }

    private static BlockPos findSpawnPositionNear(ServerLevel level, ServerPlayer player, double angle, int distance, int yRange) {
        int x = Mth.floor(player.getX() + Math.cos(angle) * distance);
        int z = Mth.floor(player.getZ() + Math.sin(angle) * distance);
        int y = player.getBlockY() + level.random.nextInt(yRange * 2 + 1) - yRange;
        return findSpawnPosition(level, x, y, z);
    }

    public static BlockPos findSpawnPosition(ServerLevel level, int x, int startY, int z) {
        int minY = level.getMinBuildHeight() + 1;
        int maxY = level.getMaxBuildHeight() - 5;
        int clampedY = Math.max(minY, Math.min(maxY, startY));

        for (int offset = 0; offset <= 16; offset++) {
            BlockPos down = new BlockPos(x, clampedY - offset, z);
            if (isSpawnSpaceClear(level, down)) {
                return down;
            }

            BlockPos up = new BlockPos(x, clampedY + offset, z);
            if (isSpawnSpaceClear(level, up)) {
                return up;
            }
        }

        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos surface = new BlockPos(x, surfaceY, z);
        return isSpawnSpaceClear(level, surface) ? surface : null;
    }

    public static boolean isSpawnSpaceClear(ServerLevel level, BlockPos pos) {
        if (pos.getY() < level.getMinBuildHeight() + 1 || pos.getY() > level.getMaxBuildHeight() - 5 || !level.getWorldBorder().isWithinBounds(pos)) {
            return false;
        }

        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        if (!below.isFaceSturdy(level, belowPos, Direction.UP)) {
            return false;
        }

        AABB box = EntityDimensions.scalable(0.75F, 4.375F).makeBoundingBox(Vec3.atBottomCenterOf(pos));
        return level.noCollision(box);
    }

    private static boolean isOpen(ServerLevel level, BlockPos pos) {
        return level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above());
    }

    private static boolean isSolidWall(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isFaceSturdy(level, pos, Direction.UP) && !state.getCollisionShape(level, pos).isEmpty();
    }

    private static boolean hasClearLine(ServerLevel level, Vec3 from, Vec3 to) {
        return level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getType() == HitResult.Type.MISS;
    }

    private static Vec3 eyePosition(BlockPos pos) {
        return Vec3.atBottomCenterOf(pos).add(0.0D, 2.2D, 0.0D);
    }

    private static Direction horizontalDirection(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dz = to.getZ() - from.getZ();
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        }
        return dz > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    private static float yawFacingPlayer(BlockPos spawnPos, ServerPlayer player) {
        Vec3 delta = player.position().subtract(Vec3.atBottomCenterOf(spawnPos));
        return Mth.wrapDegrees((float) (Mth.atan2(delta.z, delta.x) * Mth.RAD_TO_DEG) - 90.0F);
    }

    public record Placement(BlockPos pos, float yRot, String animation, int variant) {
    }
}
