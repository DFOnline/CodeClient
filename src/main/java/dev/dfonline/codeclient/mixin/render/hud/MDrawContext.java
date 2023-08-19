package dev.dfonline.codeclient.mixin.render.hud;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dfonline.codeclient.hypercube.item.Scope;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class MDrawContext {
    @Shadow @Final private MatrixStack matrices;

    @Shadow public abstract int drawText(TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow);

    @Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER))
    private void additionalItemRendering(TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci) {
        NbtCompound nbt = stack.getNbt();
        if(nbt == null) return;
        NbtCompound pbv = (NbtCompound) nbt.get("PublicBukkitValues");
        if(pbv == null) return;
        NbtString varItem = (NbtString) pbv.get("hypercube:varitem");
        if(varItem == null) return;
        JsonObject var = JsonParser.parseString(varItem.asString()).getAsJsonObject();
        if(!var.get("id").getAsString().equals("var")) return;
        JsonObject data = var.get("data").getAsJsonObject();
        String scopeName = data.get("scope").getAsString();

        this.matrices.translate(0.0F, 0.0F, 200.0F);
        Scope scope = Scope.valueOf(scopeName);
        this.drawText(textRenderer,Text.literal(scope.shortName).setStyle(Style.EMPTY.withColor(scope.color)),x,y,0xFFFFFF,true);
        matrices.translate(0.0F, 0.0F, -200.0F);
    }
}
