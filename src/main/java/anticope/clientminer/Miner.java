package anticope.clientminer;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShapes;

import java.util.*;

public class Miner {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private final static List<BlockPos> blockOffsets = Arrays.asList(
            new BlockPos(0, 1, 0),
            new BlockPos(1, 0, 0),
            new BlockPos(0, 0, 1),
            new BlockPos(-1, 0, 0),
            new BlockPos(0, 0, -1),
            new BlockPos(0, -1, 0)
    );

    private final Queue<BlockPos> blocks = new ArrayDeque<>();

    private Block blockType = null;
    private Direction direction = null;
    private ClientWorld world = null;

    public BlockPos currentBlock = null;
    private float targetYaw = 0;
    private float targetPitch = 0;

    public boolean working = false;

    public void onStopMining() {
        blocks.clear();
        currentBlock = null;
        blockType = null;
        direction = null;
        working = false;
    }

    private boolean isValidDist(BlockPos blockPos) {
        double dx = (mc.player.getX() - 0.5) - (blockPos.getX() + direction.getOffsetX());
        double dy = (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose())) - (blockPos.getY() + direction.getOffsetY());
        double dz = (mc.player.getZ() - 0.5) - (blockPos.getZ() + direction.getOffsetZ());
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        return distance <= mc.interactionManager.getReachDistance();
    }

    private boolean canBreak(BlockPos blockPos) {
        var state = world.getBlockState(blockPos);
        if (!mc.player.isCreative() && state.getHardness(mc.world, blockPos) < 0) return false;
        return state.getOutlineShape(mc.world, blockPos) != VoxelShapes.empty();
    }

    private void addConnected(BlockPos pos) {
        var toAdd = new ArrayList<BlockPos>();
        for (BlockPos offset : blockOffsets) {
            var newPos = pos.add(offset);
            if (world.getBlockState(newPos).getBlock() != blockType) continue;
            if (!isValidDist(newPos)) continue;
            if (!canBreak(newPos)) continue;
            if (blocks.contains(newPos)) continue;
            toAdd.add(newPos);
        }

        blocks.addAll(toAdd);

        for (BlockPos newPos : toAdd) { addConnected(newPos); }
    }

    public void onStartMining(BlockPos block, Direction direction, ClientWorld world) {
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

        if (Math.abs(yawDiff) > 3f || Math.abs(pitchDiff) > 3f) {
            yawDiff = MathHelper.clamp(yawDiff, -100f, 100f);
            pitchDiff = MathHelper.clamp(pitchDiff, -100f, 100f);
            if (Math.abs(yawDiff) > 3f) mc.player.setYaw(mc.player.getYaw() - yawDiff*0.2f);
            if (Math.abs(pitchDiff) > 3f) mc.player.setPitch(mc.player.getPitch() - pitchDiff*0.2f);
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
}
