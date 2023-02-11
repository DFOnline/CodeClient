package dev.dfonline.codeclient;

import dev.dfonline.codeclient.location.Plot;
import net.minecraft.util.math.BlockPos;

public class WorldPlot {
    public static boolean shouldNotRender(BlockPos pos) {
        return
                CodeClient.worldPlot != null && CodeClient.location instanceof Plot plot &&
                    ((pos.getZ() < plot.getZ() || pos.getZ() > plot.getZ() + CodeClient.worldPlot.size)
                        ||
                    (pos.getX() < plot.getX() - 21 || (pos.getX() < plot.getX() - 20 && pos.getY() < 50) || pos.getX() > plot.getX() + CodeClient.worldPlot.size));
    }

    enum Size {
        BASIC(50),
        LARGE(100),
        MASSIVE(300);

        public final int size;
        Size(int size) {
            this.size = size;
        }
    }
}
