package dev.dfonline.codeclient.mixin.world.block;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.dev.InteractionManager;
import dev.dfonline.codeclient.hypercube.actiondump.ActionDump;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(Block.class)
public class MBlock {
    @Inject(method = "getPickStack", at = @At("HEAD"), cancellable = true)
    private void getPickStack(WorldView world, BlockPos pos, BlockState state, CallbackInfoReturnable<ItemStack> cir) {
        if(!Config.getConfig().PickAction) return;
        if(CodeClient.location instanceof Dev dev) {
            if(!dev.isInDev(pos)) return;
            var breakable = InteractionManager.isBlockBreakable(pos);
            if(breakable == null) return;
            var sign = breakable.west(1);
            if(CodeClient.MC.world.getBlockEntity(sign) instanceof SignBlockEntity signBlock)
                try {
                    SignText text = signBlock.getFrontText();
                    var actiondump = ActionDump.getActionDump();
                    var action = Arrays.stream(actiondump.actions).filter(a ->
                            a.codeblockName.equals(text.getMessage(0, false).getString()) &&
                            a.name.equals(text.getMessage(1, false).getString())).findFirst();
                    if(action.isEmpty()) return;
                    cir.setReturnValue(action.get().getItem());
                }
            catch (Exception ignored) {}
        }
    }
}