package dev.dfonline.codeclient;

import net.minecraft.util.math.BlockPos;

public class WorldPlot {
    public static boolean shouldNotRender(BlockPos pos) {
        return
                CodeClient.worldPlot != null &&
                    ((pos.getZ() < PlotLocation.getZ() || pos.getZ() > PlotLocation.getZ() + CodeClient.worldPlot.size)
                        ||
                    (pos.getX() < PlotLocation.getX() - 21 || (pos.getX() < PlotLocation.getX() - 20 && pos.getY() < 50) || pos.getX() > PlotLocation.getX() + CodeClient.worldPlot.size));
    }

    public enum Size {
        BASIC(50),
        LARGE(100),
        MASSIVE(300);

        public final int size;
        Size(int size) {
            this.size = size;
        }
    }
}
