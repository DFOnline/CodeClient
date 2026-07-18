package dev.dfonline.codeclient.mixin.render.hud;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.OverlayManager;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.dev.overlay.ChestPeeker;
import dev.dfonline.codeclient.dev.overlay.SignPeeker;
import dev.dfonline.codeclient.hypercube.item.Scope;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;

@Mixin(Gui.class)
public abstract class MInGameHud {

    @Shadow
    public abstract Font getFont();

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHotbarAndDecorations(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
    private void onRender(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        int scaledWidth = context.guiWidth();
        int scaledHeight = context.guiHeight();

        Font textRenderer = getFont();

        List<Component> overlay = new ArrayList<>(List.copyOf(OverlayManager.getOverlayText()));
        Component cpuUsage = OverlayManager.getCpuUsage();
        if (cpuUsage != null && Config.getConfig().CPUDisplayCorner == Config.CPUDisplayCornerOption.TOP_LEFT) {
            if (overlay.isEmpty()) overlay.add(cpuUsage);
            else {
                overlay.add(0, cpuUsage);
                overlay.add(1, Component.empty());
            }
        }
        if (!overlay.isEmpty()) {
            int index = 0;
            for (Component text : overlay) {
                context.drawString(textRenderer, Objects.requireNonNullElseGet(text, () -> Component.literal("NULL")), 30, 30 + (index * 9), -1);
                index++;
            }
        }

        if (cpuUsage != null && Config.getConfig().CPUDisplayCorner != Config.CPUDisplayCornerOption.TOP_LEFT) {
            int margin = 30;
            int x = switch (Config.getConfig().CPUDisplayCorner) {
                case TOP_LEFT, BOTTOM_LEFT -> margin;
                case TOP_RIGHT, BOTTOM_RIGHT -> scaledWidth - margin - textRenderer.width(cpuUsage);
            };
            int y = switch (Config.getConfig().CPUDisplayCorner) {
                case TOP_LEFT, TOP_RIGHT -> margin;
                case BOTTOM_LEFT, BOTTOM_RIGHT -> scaledHeight - margin - 3;
            };
            context.drawString(textRenderer, cpuUsage, x, y, -1);
        }
        int x = (scaledWidth / 2) + Config.getConfig().ChestPeekerX;
        int yOrig = (scaledHeight / 2) + Config.getConfig().ChestPeekerY;
        try {
            List<Component> peeker = CodeClient.getFeature(ChestPeeker.class)
                    .map(ChestPeeker::getOverlayText).orElse(null);
            if (peeker == null || peeker.isEmpty()) peeker = SignPeeker.getOverlayText();
            if (peeker != null && !peeker.isEmpty()) {
                context.renderTooltip(textRenderer, peeker.stream().map(text -> ClientTooltipComponent.create(text.getVisualOrderText())).toList(), x, yOrig, DefaultTooltipPositioner.INSTANCE, null);
//                context.renderTooltip();
            }
        } catch (Exception ignored) {
            context.setTooltipForNextFrame(textRenderer, Component.literal("An error occurred"), x, yOrig);

        }
    }

    @Shadow
    private ItemStack lastToolHighlight;

    @Shadow private int toolHighlightTimer;

    @Inject(method = "renderSelectedItemName", at = @At(value = "HEAD"), cancellable = true)
    public void renderHeldItemTooltip(GuiGraphics context, CallbackInfo ci) {

        if (!Config.getConfig().ShowVariableScopeBelowName) return;
        DFItem dfItem = DFItem.of(lastToolHighlight);
        if (!dfItem.hasHypercubeKey("varitem")) return;
        Optional<String> varItem = dfItem.getHypercubeStringValue("varitem");
        if (varItem.isEmpty()) return;

        int scaledWidth = context.guiWidth();
        int scaledHeight = context.guiHeight();

        try {
            JsonObject varItemJson = JsonParser.parseString(varItem.get()).getAsJsonObject();
            String type = varItemJson.get("id").getAsString();

            JsonElement varName = varItemJson.getAsJsonObject("data").get("name");
            if (varName == null) return;

            if (type.equals("var")) {
                // If an exception is thrown past this point, we can assure that at least
                // the variable name has been drawn.
                ci.cancel();

                // Render variable name.
                Component nameText = Component.literal(varName.getAsString());
                int x1 = (scaledWidth - getFont().width(nameText)) / 2;
                int y1 = scaledHeight - 45;
                context.drawString(getFont(), nameText, x1, y1, 0xffffff);
                context.drawStringWithBackdrop(this.getFont(), nameText, x1, y1, 0xffffff, ARGB.color(255, CommonColors.WHITE));

                // Render variable scope, if this throws an exception it can only
                // mean Scope.valueOf() failed, as such the scope is invalid.
                try {
                    Scope scope = Scope.valueOf(varItemJson.getAsJsonObject("data").get("scope").getAsString());
                    int x2 = (scaledWidth - getFont().width(scope.longName)) / 2;
                    int y2 = scaledHeight - 35;
                    context.drawString(getFont(), Component.literal(scope.longName).withStyle(Style.EMPTY.withColor(scope.color)), x2, y2, 0xffffff);
                    context.drawStringWithBackdrop(this.getFont(),
                            Component.literal(scope.longName).withStyle(Style.EMPTY.withColor(scope.color)),
                            x2,
                            y2,
                            0xffffff,
                            ARGB.color(255, CommonColors.WHITE));

                } catch (Exception ignored2) {
                    // 'data' or 'scope' are invalid, do nothing. (same behavior as scope on variable item)
                }
            }
        } catch (Exception ignored) {
            // invalid varitem tag, do nothing.
        }
    }
}
