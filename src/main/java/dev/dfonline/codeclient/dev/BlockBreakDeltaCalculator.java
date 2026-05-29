package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Just a little insane.
 */
public class BlockBreakDeltaCalculator extends Feature {
    private final RecentBlocks oldBlocks = new RecentBlocks();

    @Override
    public void onBreakBlock(@NotNull Dev dev, @NotNull BlockPos pos, @Nullable BlockPos breakPos) {
        if(breakPos != null) oldBlocks.addBlock(pos);
    }

    @Override
    public boolean enabled() {
        return Config.getConfig().CustomBlockBreaking;
    }

    /**
     * The insanity previously mentioned.
     */
    public float calculateBlockDelta(BlockPos pos) {
        return 0.2F;
//        AtomicReference<Float> speed = new AtomicReference<>(0F);
//        oldBlocks.getBlocks().forEach(recentBlock -> {
//            float age = (float) recentBlock.howLongAgo() / 1000;
//            float recency = Math.max(10.0F - age,0);
//            float score = recency / 100;
//            speed.updateAndGet(v -> (v + score));
//        });
//        return Math.max(speed.get(), 0.1F);
    }

    private static class RecentBlocks {
        private final ArrayList<RecentBlock> blocks = new ArrayList();

        public void addBlock(BlockPos pos) {
            blocks.add(new RecentBlock(pos, new Date()));
        }

        public Stream<RecentBlock> getBlock(BlockPos pos) {
            return blocks.stream().filter(recentBlock -> Objects.equals(recentBlock.pos, pos));
        }

        public List<RecentBlock> getBlocks() {
            return blocks;
        }
    }

    private record RecentBlock(BlockPos pos, Date time) {
        /**
         * Milliseconds
         */
        public long howLongAgo() {
            return new Date().getTime() - time.getTime();
        }
    }
}
