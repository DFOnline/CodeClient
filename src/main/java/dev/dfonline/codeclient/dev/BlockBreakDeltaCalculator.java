package dev.dfonline.codeclient.dev;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Just a little insane.
 */
public class BlockBreakDeltaCalculator {
    private static RecentBlocks oldBlocks = new RecentBlocks();

    /**
     * Only invoked when a codeblock is broken.
     */
    public static void breakBlock(BlockPos pos) {
        if (pos == null) return;
        oldBlocks.addBlock(pos);
    }

    /**
     * The insanity previously mentioned.
     */
    public static float calculateBlockDelta(BlockPos pos) {
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
        private ArrayList<RecentBlock> blocks = new ArrayList();

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
