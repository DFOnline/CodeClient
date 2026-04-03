package dev.dfonline.codeclient.mixin;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.location.Plot;
import net.minecraft.client.gui.hud.debug.DebugHudLines;
import net.minecraft.client.gui.hud.debug.PlayerPositionDebugHudEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Locale;

@Mixin(PlayerPositionDebugHudEntry.class)
public class PlayerPosDebugMixin {
    @Shadow @Final public static Identifier SECTION_ID;

    @Inject(method = "render", at = @At("TAIL"))
    private void render(DebugHudLines lines, World world, WorldChunk clientChunk, WorldChunk chunk, CallbackInfo ci) {
        if (CodeClient.location instanceof Plot plot && plot.getX() != null) {
            var plotLocation = CodeClient.MC.getCameraEntity().getEntityPos().subtract(plot.getPos());
            String size = plot.getSize() == null ? "UNKNOWN" : plot.getSize().name();
            lines.addLinesToSection(SECTION_ID, List.of(String.format(
                    Locale.ROOT,
                    "Plot: %.3f / %.5f / %.3f (%s)",
                    plotLocation.getX(),
                    plotLocation.getY(),
                    plotLocation.getZ(),
                    size
            )));
        }
    }
}
