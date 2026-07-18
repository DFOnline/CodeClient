package dev.dfonline.codeclient.mixin.world.block;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.hypercube.Target;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SignBlockEntity.class)
public abstract class MSignBlockEntity {
    @Shadow
    private SignText frontText;

    @Inject(method = "getFrontText", at = @At("HEAD"), cancellable = true)
    public void getFrontText(CallbackInfoReturnable<SignText> cir) {
        if (CodeClient.location instanceof Dev plot) {
            if (plot.isInDev(((BlockEntity) (Object) this).getBlockPos())) {
                SignText orig = this.frontText;
                Component line1 = orig.getMessage(0, false);
                Component line2 = orig.getMessage(1, false);
                Component line3 = orig.getMessage(2, false);
                Component line4 = orig.getMessage(3, false);

                Config config = Config.getConfig();
                TextColor color3 = null;
                if (Config.getConfig().UseSelectionColor) try {
                    color3 = Target.valueOf(line3.getString()).color;
                } catch (Exception ignored) {
                }
                if (color3 == null && config.Line3Color != 0) color3 = TextColor.fromRgb(config.Line3Color);
                if (config.Line1Color != 0)
                    line1 = Component.empty().append(line1).setStyle(Style.EMPTY.withColor(config.Line1Color));
                if (config.Line2Color != 0)
                    line2 = Component.empty().append(line2).setStyle(Style.EMPTY.withColor(config.Line2Color));
                if (color3 != null) line3 = Component.empty().append(line3).setStyle(Style.EMPTY.withColor(color3));
                if (config.Line4Color != 0)
                    line4 = Component.empty().append(line4).setStyle(Style.EMPTY.withColor(config.Line4Color));


                cir.setReturnValue(
                        orig
                                .setMessage(0, line1)
                                .setMessage(1, line2)
                                .setMessage(2, line3)
                                .setMessage(3, line4)
                );
            }
        }
    }
}
