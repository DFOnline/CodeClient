package dev.dfonline.codeclient.mixin.world.block;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.data.PassthroughBlockHitResult;
import dev.dfonline.codeclient.dev.InteractionManager;
import dev.dfonline.codeclient.hypercube.actiondump.ActionDump;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

import static dev.dfonline.codeclient.CodeClient.MC;

@Mixin(Block.class)
public class MBlock {




    @Inject(method = "getPickStack", at = @At("HEAD"), cancellable = true)
    private void getPickStack(WorldView world, BlockPos pos, BlockState state, CallbackInfoReturnable<ItemStack> cir) {
        if (Config.getConfig().ShiftRCBackwarp) {
            if (CodeClient.location instanceof Dev) {

                if (!(MC.world.getBlockEntity(pos) instanceof SignBlockEntity)) {
                    pos = pos.subtract(new Vec3i(1, 0, 0));
                }

                if (MC.world.getBlockEntity(pos) instanceof SignBlockEntity signBlock) {
                    if (MC.player.isSneaking() && MC.crosshairTarget instanceof BlockHitResult result) {

                        SignText text = signBlock.getFrontText();

                        // actually sure, if you have the setting on let middle clicks act the same for functions and events
                        if (true || text.getMessage(0, false).getString().trim().equals("FUNCTION") || text.getMessage(0, false).getString().trim().equals("PROCESS")) {

                            var interactionManager = MC.interactionManager;

                            if (interactionManager == null) return;

                            interactionManager.interactBlock(MC.player, Hand.MAIN_HAND, new PassthroughBlockHitResult(pos.toCenterPos(), result.getSide(), pos, result.isInsideBlock()));

                            cir.setReturnValue(ItemStack.EMPTY);
                            cir.cancel();
                            return;

                        }
                    }
                }
            }
        }


        if (Config.getConfig().PickFunctionNames) {
            if (CodeClient.location instanceof Dev) {
                if (MC.world.getBlockEntity(pos) instanceof SignBlockEntity signBlock) {
                    try {
                        SignText text = signBlock.getFrontText();

                        if (!text.getMessage(0, false).getString().contains("FUNCTION") && !text.getMessage(0, false).getString().contains("PROCESS"))
                            return;

                        ItemStack stringStack = new ItemStack(Items.STRING, 1);

                        DFItem string = new DFItem(stringStack);

                        String funcName = text.getMessage(1, false).getString();

                        string.setName(Text.literal(funcName).setStyle(Style.EMPTY.withItalic(false)));

                        string.getItemData().setHypercubeStringValue("varitem", "{\"id\":\"txt\",\"data\":{\"name\":\"" + funcName + "\"}}");

                        ItemStack toGive = string.getItemStack();

                        cir.setReturnValue(toGive);
                    } catch (Exception ignored) {
                        CodeClient.LOGGER.info("Pick sign block ignored due to error");
                    }
                    return;
                }
            }
        }


        if (!Config.getConfig().PickAction) return;
        if (CodeClient.location instanceof Dev dev) {
            if (!dev.isInDev(pos)) return;
            var breakable = InteractionManager.isBlockBreakable(pos);
            if (breakable == null) return;
            var sign = breakable.west(1);
            if (MC.world.getBlockEntity(sign) instanceof SignBlockEntity signBlock)
                try {
                    SignText text = signBlock.getFrontText();
                    var actiondump = ActionDump.getActionDump();
                    var action = Arrays.stream(actiondump.actions).filter(a ->
                            a.codeblockName.equals(text.getMessage(0, false).getString()) &&
                                    a.name.equals(text.getMessage(1, false).getString())).findFirst();
                    if (action.isEmpty()) return;
                    cir.setReturnValue(action.get().getItem());
                } catch (Exception ignored) {
                }
        }
    }
}