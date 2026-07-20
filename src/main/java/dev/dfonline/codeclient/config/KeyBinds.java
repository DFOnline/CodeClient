package dev.dfonline.codeclient.config;

import com.mojang.blaze3d.platform.InputConstants;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.command.CommandSender;
import dev.dfonline.codeclient.dev.InteractionManager;
import dev.dfonline.codeclient.dev.menu.devinventory.DevInventoryScreen;
import dev.dfonline.codeclient.location.Dev;
import dev.dfonline.codeclient.location.Play;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class KeyBinds {
    /**
     * Starts the "Code Palette" screen if pressed.
     */
    public static KeyMapping editBind;
    /**
     * If in build mode, holding this will allow you to phase through blocks.
     */
    public static KeyMapping clipBind;

    public static KeyMapping teleportLeft;
    public static KeyMapping teleportRight;
    public static KeyMapping teleportForward;
    public static KeyMapping teleportBackward;

    public static KeyMapping openAction;
    public static KeyMapping editAction;
    public static KeyMapping actionUsages;

    /**
     * Shows tags set with /item tag when held in creative mode.
     * Handled in the Keyboard mixin as keybinds don't get set when pressed while in a screen.
     */
    public static KeyMapping previewItemTags;

    /**
     * Plays all the sounds in a code chest.
     */
    public static KeyMapping previewSounds;

    /**
     * Toggles between Play and Dev modes.
     */
    public static KeyMapping playDev;
    /**
     * Toggles between Play and Build modes.
     */
    public static KeyMapping playBuild;

    public static void init() {
        KeyMapping.Category dev = KeyMapping.Category.register(Identifier.parse("codeclient:dev"));
        KeyMapping.Category navi = KeyMapping.Category.register(Identifier.parse("codeclient:navigation"));

        editBind = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.codeclient.codepalette", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Y, dev));
        clipBind = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.codeclient.phaser", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, dev));
        openAction = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.codeclient.open_action", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), dev));
        editAction = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.codeclient.edit_action", InputConstants.Type.KEYSYM, InputConstants.KEY_PERIOD, dev));
        actionUsages = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.codeclient.action_usages", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), dev));

        teleportLeft = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.codeclient.tp.left", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), navi));
        teleportRight = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.codeclient.tp.right", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), navi));
        teleportForward = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.codeclient.tp.forward", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), navi));
        teleportBackward = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.codeclient.tp.backward", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), navi));

        previewItemTags = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.codeclient.preview_item_tags", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), dev));
        previewSounds = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.codeclient.preview_sounds", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), dev));
        playDev = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.codeclient.playDev", InputConstants.UNKNOWN.getValue(), dev));
        playBuild = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.codeclient.playBuild", InputConstants.UNKNOWN.getValue(), dev));
    }

    public static void tick() {
        LocalPlayer player = CodeClient.MC.player;
        if (player != null) {
            checkTp(teleportLeft, new Vec3(0, 0, -2));
            checkTp(teleportRight, new Vec3(0, 0, 2));
            checkTp(teleportForward, new Vec3(3, 0, 0));
            checkTp(teleportBackward, new Vec3(-3, 0, 0));
        }
        if (CodeClient.location instanceof Dev dev) {
            if (editBind.consumeClick()) {
                CodeClient.MC.gui.setScreen(new DevInventoryScreen(player));
            }

            var mc = CodeClient.MC;
            if (openAction.consumeClick()) {
                if (mc.hitResult instanceof BlockHitResult result) {
                    var pos = result.getBlockPos();
                    var target = InteractionManager.targetedBlockPos(pos);
                    if (!dev.isInDev(pos) || target == null) {
                        return;
                    }
                    if (mc.options.keySprint.isDown()) {
                        var sign = target.offset(-1, 0, 0);
                        if (mc.level.getBlockState(sign).is(Blocks.OAK_WALL_SIGN))
                            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(sign), result.getDirection(), sign, result.isInside()));
                    } else {
                        var chest = target.offset(0, 1, 0);
                        if (mc.level.getBlockState(chest).is(Blocks.CHEST))
                            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(chest), result.getDirection(), chest, result.isInside()));
                    }
                }
            }

            if(editAction.consumeClick()) {
                mc.gui.setScreen(new ChatScreen("/action ", false));
            }

            if(actionUsages.consumeClick()) {
                    if (CodeClient.MC.hitResult instanceof BlockHitResult block) {
                        BlockPos pos = InteractionManager.targetedBlockPos(block.getBlockPos());
                        if (pos == null || (!dev.isInDev(pos)) || CodeClient.MC.level == null) return;
                        if (CodeClient.MC.level.getBlockEntity(pos.west()) instanceof SignBlockEntity sign) {
                            SignText text = sign.getFrontText();

                            //Checks if 2nd line is empty. There probably is a better way to check this, right...?
                            if(text.getMessages(false)[1].getSiblings().contains(Component.empty())) return;

                            String command = String.format("usages %s %s",
                                    text.getMessage(0, false).toFlatList().getFirst().getString()
                                            .toLowerCase()
                                            .replace(" ", "_")
                                            .replace("call_function", "function")
                                            .replace("start_process", "process"),
                                    text.getMessage(1, false).toFlatList().getFirst().getString());
                            mc.getConnection().sendCommand(command);
                        }
                    }
            }
        }

        if(CodeClient.MC.getConnection() == null) return;
        if (playDev.consumeClick())
            CommandSender.queue(CodeClient.location instanceof Play ? "dev" : "play");

        if (playBuild.consumeClick())
            CommandSender.queue(CodeClient.location instanceof Play ? "build" : "play");
    }

    private static void checkTp(KeyMapping keyBinding, Vec3 offset) {
        var player = CodeClient.MC.player;
        if (player == null) return;
        if (keyBinding.consumeClick() && CodeClient.location instanceof Dev dev && dev.isInDevSpace()) {
            var target = player.position().add(offset);
            if (dev.isInDevSpace(target)) player.setPos(target);
        }
    }
}
