package dev.dfonline.codeclient.mixin;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.location.Plot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.components.debug.DebugEntryPosition;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

@Mixin(DebugEntryPosition.class)
public class PlayerPosDebugMixin {
    @Shadow @Final public static Identifier GROUP;

    @Inject(method = "display", at = @At("TAIL"))
    private void render(DebugScreenDisplayer lines, Level world, LevelChunk clientChunk, LevelChunk chunk, CallbackInfo ci) {
        if (CodeClient.location instanceof Plot plot && plot.getX() != null) {
            var plotLocation = CodeClient.MC.getCameraEntity().position().subtract(plot.getPos());
            String size = plot.getSize() == null ? "UNKNOWN" : plot.getSize().name();
            lines.addToGroup(GROUP, List.of(String.format(
                    Locale.ROOT,
                    "Plot: %.3f / %.5f / %.3f (%s)",
                    plotLocation.x(),
                    plotLocation.y(),
                    plotLocation.z(),
                    size
            )));
        }
    }
}
