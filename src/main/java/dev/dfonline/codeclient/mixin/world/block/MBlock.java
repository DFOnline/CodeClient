package dev.dfonline.codeclient.mixin.world.block;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.dev.InteractionManager;
import dev.dfonline.codeclient.hypercube.actiondump.ActionDump;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Mixin(Block.class)
public class MBlock {
    @Inject(method = "getPickStack", at = @At("HEAD"), cancellable = true)
    private void getPickStack(WorldView world, BlockPos pos, BlockState state, CallbackInfoReturnable<ItemStack> cir) {

        if (Config.getConfig().PickFunctionNames) {
            if (CodeClient.location instanceof Dev) {
                ItemStack copiedNameString = tryPickFunctionName(pos);

                // if that was a sign, function/process related one, and we copied its name successfully
                if (copiedNameString != null) {
                    cir.setReturnValue(copiedNameString);
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
            if (CodeClient.MC.world.getBlockEntity(sign) instanceof SignBlockEntity signBlock)
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

    @Unique
    private static final Set<String> CALLABLE_BLOCK_NAMES = Set.of(
            "CALL FUNCTION",
            "START PROCESS",
            "FUNCTION",
            "PROCESS"
    );

    /**
     * Will have changed the return value to an appropriate string if it returns true
     * @param pos Position of the block to try and copy function/process name from
     * @return Null if a name can't be copied from that position, else the ItemStack of the appropriate DF string varitem
     */
    @Unique
    private ItemStack tryPickFunctionName(BlockPos pos) {
        if (CodeClient.MC.world.getBlockEntity(pos) instanceof SignBlockEntity signBlock) {
            try {
                SignText text = signBlock.getFrontText();

                String firstLine = text.getMessage(0, false).getString();

                if (!CALLABLE_BLOCK_NAMES.contains(firstLine))
                    return null;

                DFItem string = new DFItem(new ItemStack(Items.STRING, 1));

                String funcName = text.getMessage(1, false).getString();

                string.setName(
                        // stop it from being italicized like anvil renaming
                        Text.literal(funcName).setStyle(Style.EMPTY.withItalic(false))
                    );

                String varItemJSON = CodeClient.gson.toJson(
                        Map.of(
                                "id", "txt",
                                "data", Map.of("name", funcName)
                        )
                );

                string.getItemData().setHypercubeStringValue(
                        "varitem", varItemJSON
                    );

                return string.getItemStack();
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}