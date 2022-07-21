package anticope.clientminer;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShapes;

import java.util.*;

public class Miner {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private final static BlockPos[] blockOffsets = {
            new BlockPos(0, 1, 0),
            new BlockPos(1, 0, 0),
            new BlockPos(0, 0, 1),
            new BlockPos(-1, 0, 0),
            new BlockPos(0, 0, -1),
            new BlockPos(0, -1, 0)
    };

    private final Queue<BlockPos> blocks = new PriorityQueue<>(Miner::blockCompare);

    private Block blockType = null;
    private Direction direction = null;
    private ClientWorld world = null;

    public BlockPos currentBlock = null;
    private float targetYaw = 0;
    private float targetPitch = 0;

    private float startYaw = 0;
    private float startPitch = 0;

    public boolean working = false;

    public void onStopMining() {
        if (!working) return;

        blocks.clear();
        currentBlock = null;
        blockType = null;
        direction = null;
        working = false;

        if (Constants.config.snapBack && mc.player != null) {
            mc.player.setYaw(startYaw);
            mc.player.setPitch(startPitch);
        }

        if (Constants.config.sound && world != null && mc.player != null)
            world.playSoundFromEntity(mc.player, mc.player, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 1f, 1f);

        if (Constants.config.automine)
            mc.options.attackKey.setPressed(false);
    }

    private boolean isValidDist(BlockPos blockPos) {
        double dx = (mc.player.getX() - 0.5) - (blockPos.getX() + direction.getOffsetX());
        double dy = (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose())) - (blockPos.getY() + direction.getOffsetY());
        double dz = (mc.player.getZ() - 0.5) - (blockPos.getZ() + direction.getOffsetZ());
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        return distance < mc.interactionManager.getReachDistance();
    }

    private boolean canBreak(BlockPos blockPos) {
        var state = world.getBlockState(blockPos);
        if (!mc.player.isCreative() && state.getHardness(mc.world, blockPos) < 0) return false;
        return state.getOutlineShape(mc.world, blockPos) != VoxelShapes.empty();
    }

    private void addConnected(BlockPos pos) {
        for (BlockPos offset : blockOffsets) {
            var newPos = pos.add(offset);
            if (world.getBlockState(newPos).getBlock() != blockType) continue;
            if (!isValidDist(newPos)) continue;
            if (!canBreak(newPos)) continue;
            if (blocks.contains(newPos)) continue;
            blocks.add(newPos);
            addConnected(newPos);
        }    
    }

    public void onStartMining(BlockPos block, Direction direction, ClientWorld world) {
        startYaw = mc.player.getYaw();
        startPitch = mc.player.getPitch();
        onStopMining();
        working = true;
        this.direction = direction;
        this.blockType = world.getBlockState(block).getBlock();
        this.world = world;
        blocks.add(block);
        addConnected(block);
    }

    public void onTick() {
        if (!working) return;

        if (mc.player == null || world == null) {
            onStopMining();
            return;
        }

        if (currentBlock == null) {
            currentBlock = blocks.poll();

            if (currentBlock == null) {
                onStopMining();
                return;
            }
        } else {
            if (world.getBlockState(currentBlock).getBlock() != blockType
                    || !isValidDist(currentBlock)
                    || !canBreak(currentBlock)) {
                currentBlock = null;
                return;
            }

        }

        //world.addParticle(ParticleTypes.SMALL_FLAME, currentBlock.getX() + 0.5, currentBlock.getY() + 1.1, currentBlock.getZ()+ 0.5, 0, 0, 0);

        targetYaw = getYaw(currentBlock);
        targetPitch = getPitch(currentBlock);

        float yawDiff = mc.player.getYaw() - targetYaw;
        float pitchDiff = mc.player.getPitch() - targetPitch;

        float rotationSpeed = MathHelper.clamp(Constants.config.rotationSpeed, 0.001f, 1f);

        if (Math.abs(yawDiff) > 3f || Math.abs(pitchDiff) > 3f) {
            yawDiff = MathHelper.clamp(yawDiff, -100f, 100f);
            pitchDiff = MathHelper.clamp(pitchDiff, -100f, 100f);

            if (Math.abs(yawDiff) > 3f)
                mc.player.setYaw(mc.player.getYaw() - yawDiff * rotationSpeed);
            if (Math.abs(pitchDiff) > 3f)
                mc.player.setPitch(mc.player.getPitch() - pitchDiff * rotationSpeed);

            return;
        }

        if (Constants.config.raycast) {
            if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == Type.BLOCK) {
                BlockHitResult hit = (BlockHitResult)mc.crosshairTarget;
                if (world.getBlockState(hit.getBlockPos()).getBlock() != blockType) {
                    currentBlock = null;
                    if (Constants.config.automine)
                        mc.options.attackKey.setPressed(false);
                    return;
                }
            }
        }
    }

    public static float getYaw(BlockPos pos) {
        return mc.player.getYaw() + MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(pos.getZ() + 0.5 - mc.player.getZ(), pos.getX() + 0.5 - mc.player.getX())) - 90f - mc.player.getYaw());
    }

    public static float getPitch(BlockPos pos) {
        double diffX = pos.getX() + 0.5 - mc.player.getX();
        double diffY = pos.getY() + 0.5 - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double diffZ = pos.getZ() + 0.5 - mc.player.getZ();

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        return mc.player.getPitch() + MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)) - mc.player.getPitch());
    }

    public static int blockCompare(BlockPos p1, BlockPos p2) {
        var mc = MinecraftClient.getInstance();
        return Integer.compare(
            mc.player.getBlockPos().getManhattanDistance(p1),
            mc.player.getBlockPos().getManhattanDistance(p2)
        );
    }
}
