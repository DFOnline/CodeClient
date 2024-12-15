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
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mixin(InGameHud.class)
public abstract class MInGameHud {

    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        int scaledWidth = context.getScaledWindowWidth();
        int scaledHeight = context.getScaledWindowHeight();

        TextRenderer textRenderer = getTextRenderer();

        List<Text> overlay = new ArrayList<>(List.copyOf(OverlayManager.getOverlayText()));
        Text cpuUsage = OverlayManager.getCpuUsage();
        if (cpuUsage != null && Config.getConfig().CPUDisplayCorner == Config.CPUDisplayCornerOption.TOP_LEFT) {
            if (overlay.isEmpty()) overlay.add(cpuUsage);
            else {
                overlay.add(0, cpuUsage);
                overlay.add(1, Text.empty());
            }
        }
        if (!overlay.isEmpty()) {
            int index = 0;
            for (Text text : overlay) {
                context.drawTextWithShadow(textRenderer, Objects.requireNonNullElseGet(text, () -> Text.literal("NULL")), 30, 30 + (index * 9), -1);
                index++;
            }
        }

        if (cpuUsage != null && Config.getConfig().CPUDisplayCorner != Config.CPUDisplayCornerOption.TOP_LEFT) {
            int margin = 30;
            int x = switch (Config.getConfig().CPUDisplayCorner) {
                case TOP_LEFT, BOTTOM_LEFT -> margin;
                case TOP_RIGHT, BOTTOM_RIGHT -> scaledWidth - margin - textRenderer.getWidth(cpuUsage);
            };
            int y = switch (Config.getConfig().CPUDisplayCorner) {
                case TOP_LEFT, TOP_RIGHT -> margin;
                case BOTTOM_LEFT, BOTTOM_RIGHT -> scaledHeight - margin - 3;
            };
            context.drawTextWithShadow(textRenderer, cpuUsage, x, y, -1);
        }
        int x = (scaledWidth / 2) + Config.getConfig().ChestPeekerX;
        int yOrig = (scaledHeight / 2) + Config.getConfig().ChestPeekerY;
        try {
            List<Text> peeker = CodeClient.getFeature(ChestPeeker.class)
                    .map(ChestPeeker::getOverlayText).orElse(null);
            if (peeker == null) peeker = SignPeeker.getOverlayText();
            if (peeker != null && !peeker.isEmpty()) {
                context.drawTooltip(textRenderer, peeker, x, yOrig);

            }
        } catch (Exception ignored) {
            context.drawTooltip(textRenderer, Text.literal("An error occurred"), x, yOrig);

        }
    }

    @Shadow
    private ItemStack currentStack;

    @Inject(method = "renderHeldItemTooltip", at = @At(value = "HEAD"), cancellable = true)
    public void renderHeldItemTooltip(DrawContext context, CallbackInfo ci) {

        if (!Config.getConfig().ShowVariableScopeBelowName) return;
        DFItem dfItem = DFItem.of(currentStack);
        if (!dfItem.hasHypercubeKey("varitem")) return;
        String varItem = dfItem.getHypercubeStringValue("varitem");

        int scaledWidth = context.getScaledWindowWidth();
        int scaledHeight = context.getScaledWindowHeight();

        try {
            JsonObject varItemJson = JsonParser.parseString(varItem).getAsJsonObject();
            String type = varItemJson.get("id").getAsString();

            JsonElement varName = varItemJson.getAsJsonObject("data").get("name");
            if (varName == null) return;

            if (type.equals("var")) {
                // If an exception is thrown past this point, we can assure that at least
                // the variable name has been drawn.
                ci.cancel();

                // Render variable name.
                Text nameText = Text.literal(varName.getAsString());
                int x1 = (scaledWidth - getTextRenderer().getWidth(nameText)) / 2;
                int y1 = scaledHeight - 45;
                context.drawTextWithShadow(getTextRenderer(), nameText, x1, y1, 0xffffff);

                // Render variable scope, if this throws an exception it can only
                // mean Scope.valueOf() failed, as such the scope is invalid.
                try {
                    Scope scope = Scope.valueOf(varItemJson.getAsJsonObject("data").get("scope").getAsString());
                    int x2 = (scaledWidth - getTextRenderer().getWidth(scope.longName)) / 2;
                    int y2 = scaledHeight - 35;
                    context.drawTextWithShadow(getTextRenderer(), Text.literal(scope.longName).fillStyle(Style.EMPTY.withColor(scope.color)), x2, y2, 0xffffff);
                } catch (Exception ignored2) {
                    // 'data' or 'scope' are invalid, do nothing. (same behavior as scope on variable item)
                }
            }
        } catch (Exception ignored) {
            // invalid varitem tag, do nothing.
        }
    }
}
