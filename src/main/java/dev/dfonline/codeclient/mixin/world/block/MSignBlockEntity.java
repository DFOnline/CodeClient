package dev.dfonline.codeclient.mixin.world.block;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.hypercube.Target;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SignBlockEntity.class)
public abstract class MSignBlockEntity {
    @Shadow private SignText frontText;

    @Inject(method = "getFrontText", at = @At("HEAD"), cancellable = true)
    public void getFrontText(CallbackInfoReturnable<SignText> cir) {
        if(CodeClient.location instanceof Dev plot) {
            if(plot.isInDev(((BlockEntity)(Object)this).getPos())) {
                SignText orig = this.frontText;
                Text blockName = orig.getMessage(0,false);
                Text actOrDat = orig.getMessage(1,false);
                Text subOrSel = orig.getMessage(2,false);
                Text inverted = orig.getMessage(3,false);

                TextColor color2;
                try {
                    color2 = Target.valueOf(subOrSel.getString()).color;
                } catch (Exception ignored) {
                    color2 = TextColor.fromRgb(0xAAFFAA);
                }

                cir.setReturnValue(
                        orig
                                .withMessage(0, Text.empty().append(blockName).formatted(Formatting.GRAY,Formatting.BOLD)) // TODO: block based color?
                                .withMessage(1, Text.empty().append(actOrDat).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xC5C5C5))))
                                .withMessage(2, Text.empty().append(subOrSel).setStyle(Style.EMPTY.withColor(color2)))
                                .withMessage(3, Text.empty().append(inverted).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFF8800))))
                );
            }
        }
    }
}
