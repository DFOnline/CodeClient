package dev.dfonline.codeclient.mixin.render.hud;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.hypercube.item.Scope;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class MDrawContext {
    @Shadow
    @Final
    private MatrixStack matrices;

    @Shadow
    public abstract int drawText(TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow);

    @Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER))
    private void additionalItemRendering(TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return;
        NbtCompound pbv = (NbtCompound) nbt.get("PublicBukkitValues");
        if (pbv == null) return;
        NbtString varItem = (NbtString) pbv.get("hypercube:varitem");
        if (varItem == null) return;
        JsonObject var = JsonParser.parseString(varItem.asString()).getAsJsonObject();
        JsonObject data = var.get("data").getAsJsonObject();
        Text text = null;
        switch (var.get("id").getAsString()) {
            case "var": {
                try {
                    Scope scope = Scope.valueOf(data.get("scope").getAsString());
                    text = Text.literal((Config.getConfig().UseIForLineScope && scope == Scope.line) ? "I" : scope.shortName).setStyle(Style.EMPTY.withColor(scope.color));
                } catch (Exception ignored) {
                    text = Text.literal("?").formatted(Formatting.DARK_RED);
                }
                break;
            }
            case "num": {
                String name = data.get("name").getAsString();
                if (textRenderer.getWidth(Text.of(name)) > 16) {
                    var avail = textRenderer.trimToWidth(name, 16 - 2);
                    text = Text.literal(avail).formatted(Formatting.RED).append(Text.literal(".".repeat((16 - textRenderer.getWidth(Text.of(avail))) / 2)).formatted(Formatting.WHITE));
                } else text = Text.literal(name).formatted(Formatting.RED);
                break;
            }
            default: {
                return;
            }
        }
        if (text == null) return;
        this.matrices.translate(0.0F, 0.0F, 200.0F);
        this.drawText(textRenderer, text, x, y, 0xFFFFFF, true);
        matrices.translate(0.0F, 0.0F, -200.0F);
    }
}
