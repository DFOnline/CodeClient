package dev.dfonline.codeclient.config;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.InteractionManager;
import dev.dfonline.codeclient.dev.menu.devinventory.DevInventoryScreen;
import dev.dfonline.codeclient.location.Dev;
import dev.dfonline.codeclient.location.Play;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class KeyBinds {
    /**
     * Starts the "Code Palette" screen if pressed.
     */
    public static KeyBinding editBind;
    /**
     * If in build mode, holding this will allow you to phase through blocks.
     */
    public static KeyBinding clipBind;

    public static KeyBinding teleportLeft;
    public static KeyBinding teleportRight;
    public static KeyBinding teleportForward;
    public static KeyBinding teleportBackward;

    public static KeyBinding openAction;

    /**
     * Shows tags set with /item tag when held in creative mode.
     * Handled in the Keyboard mixin as keybinds don't get set when pressed while in a screen.
     */
    public static KeyBinding previewItemTags;

    /**
     * Toggles between Play and Dev modes.
     */
    public static KeyBinding playDev;
    /**
     * Toggles between Play and Build modes.
     */
    public static KeyBinding playBuild;

    public static void init() {
        editBind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.codeclient.codepalette", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Y, "category.codeclient.dev"));
        clipBind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.codeclient.phaser", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "category.codeclient.dev"));
        openAction = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.codeclient.open_action", InputUtil.Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.codeclient.dev"));

        teleportLeft = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.codeclient.tp.left", InputUtil.Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.codeclient.navigation"));
        teleportRight = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.codeclient.tp.right", InputUtil.Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.codeclient.navigation"));
        teleportForward = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.codeclient.tp.forward", InputUtil.Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.codeclient.navigation"));
        teleportBackward = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.codeclient.tp.backward", InputUtil.Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.codeclient.navigation"));

        previewItemTags = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.codeclient.preview_item_tags", InputUtil.Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.codeclient.dev"));
        playDev = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.codeclient.playDev", InputUtil.UNKNOWN_KEY.getCode(), "category.codeclient.dev"));
        playBuild = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.codeclient.playBuild", InputUtil.UNKNOWN_KEY.getCode(), "category.codeclient.dev"));
    }

    public static void tick() {
        ClientPlayerEntity player = CodeClient.MC.player;
        if (player != null) {
            checkTp(teleportLeft, new Vec3d(0, 0, -2));
            checkTp(teleportRight, new Vec3d(0, 0, 2));
            checkTp(teleportForward, new Vec3d(3, 0, 0));
            checkTp(teleportBackward, new Vec3d(-3, 0, 0));
        }
        if (CodeClient.location instanceof Dev dev) {
            if (editBind.wasPressed()) {
                CodeClient.MC.setScreen(new DevInventoryScreen(player));
            }

            var mc = CodeClient.MC;
            if (openAction.wasPressed()) {
                if (mc.crosshairTarget instanceof BlockHitResult result) {
                    var pos = result.getBlockPos();
                    var target = InteractionManager.targetedBlockPos(pos);
                    if (!dev.isInDev(pos) || target == null) {
                        return;
                    }
                    if (mc.options.sprintKey.isPressed()) {
                        var sign = target.add(-1, 0, 0);
                        if (mc.world.getBlockState(sign).isOf(Blocks.OAK_WALL_SIGN))
                            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(sign.toCenterPos(), result.getSide(), sign, result.isInsideBlock()));
                    } else {
                        var chest = target.add(0, 1, 0);
                        if (mc.world.getBlockState(chest).isOf(Blocks.CHEST))
                            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(chest.toCenterPos(), result.getSide(), chest, result.isInsideBlock()));
                    }
                }
            }
        }

        if(CodeClient.MC.getNetworkHandler() == null) return;
        if (playDev.wasPressed())
            CodeClient.MC.getNetworkHandler().sendCommand(CodeClient.location instanceof Play ? "dev" : "play");

        if (playBuild.wasPressed())
            CodeClient.MC.getNetworkHandler().sendCommand(CodeClient.location instanceof Play ? "build" : "play");
    }

    private static void checkTp(KeyBinding keyBinding, Vec3d offset) {
        var player = CodeClient.MC.player;
        if (player == null) return;
        if (keyBinding.wasPressed() && CodeClient.location instanceof Dev dev && dev.isInDev(player.getPos())) {
            var target = player.getPos().add(offset);
            if (dev.isInDev(target)) player.setPosition(target);
        }
    }
}
