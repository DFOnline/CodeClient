package dev.dfonline.codeclient;

import com.google.gson.Gson;
import dev.dfonline.codeclient.action.Action;
import dev.dfonline.codeclient.action.None;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.dev.BuildClip;
import dev.dfonline.codeclient.dev.Debug.Debug;
import dev.dfonline.codeclient.dev.LastPos;
import dev.dfonline.codeclient.dev.NoClip;
import dev.dfonline.codeclient.dev.RecentChestInsert;
import dev.dfonline.codeclient.dev.menu.DevInventory.DevInventoryScreen;
import dev.dfonline.codeclient.dev.overlay.ChestPeeker;
import dev.dfonline.codeclient.location.Dev;
import dev.dfonline.codeclient.location.Location;
import dev.dfonline.codeclient.location.Plot;
import dev.dfonline.codeclient.location.Spawn;
import dev.dfonline.codeclient.switcher.StateSwitcher;
import dev.dfonline.codeclient.websocket.SocketHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CodeClient implements ModInitializer {
    public static final String MOD_NAME = "CodeClient";
    public static final String MOD_ID = "codeclient";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static MinecraftClient MC = MinecraftClient.getInstance();
    public static final Gson gson = new Gson();
    /**
     * Starts the "Code Palette" screen if pressed.
     */
    public static KeyBinding editBind;
    /**
     * If in build mode, holding this will allow you to phase through blocks.
     */
    public static KeyBinding clipBind;
    public static AutoJoin autoJoin = AutoJoin.NONE;

    /**
     * One at a time actions to do things like placing templates, or clearing a plot.
     */
    @NotNull
    public static Action currentAction = new None();
    public static Location lastLocation = null;
    public static Location location = null;
    public static boolean shouldReload = false;

    /**
     * For all recieving packet events and debugging.
     * @return If the packet should be cancelled and not acted on. True to ignore.
     * @param <T> Server2Client
     */
    public static <T extends PacketListener> boolean handlePacket(Packet<T> packet) {

        if(currentAction.onReceivePacket(packet)) return true;
        if(Debug.handlePacket(packet)) return true;
        if(BuildClip.handlePacket(packet)) return true;
        if(ChestPeeker.handlePacket(packet)) return true;
        Event.handlePacket(packet);
        LastPos.handlePacket(packet);

        String name = packet.getClass().getName().replace("net.minecraft.network.packet.s2c.play.","");
//        if(!List.of("PlayerListS2CPacket","WorldTimeUpdateS2CPacket","GameMessageS2CPacket","KeepAliveS2CPacket", "ChunkDataS2CPacket", "UnloadChunkS2CPacket","TeamS2CPacket", "ChunkRenderDistanceCenterS2CPacket", "MessageHeaderS2CPacket", "LightUpdateS2CPacket", "OverlayMessageS2CPacket").contains(name)) LOGGER.info(name);
        if((MC.currentScreen instanceof GameMenuScreen || MC.currentScreen instanceof ChatScreen || MC.currentScreen instanceof StateSwitcher) && packet instanceof CloseScreenS2CPacket) {
            return true;
        }
        return false;
    }

    /**
     * This needs to be true for NoClip to work.
     * Whilst this should be the first source to check if NoClip is on, keep in mind there is NoClip#isIgnoringWalls
     * Useful for fallback checks and preventing noclip packet spam screwing you over.
     */
    public static boolean noClipOn() {
        if(MC.player == null) return false;
        if(!Config.getConfig().NoClipEnabled) return false;
        if(!(location instanceof Dev)) return false;
        if(!(currentAction instanceof None)) return false;
        if(!MC.player.getAbilities().creativeMode) return false;
        return true;
    }

    /**
     * All outgoing packet events and debugging.
     * @return If the packet shouldn't be sent. True to not send.
     * @param <T> ClientToServer
     */
    public static <T extends PacketListener> boolean onSendPacket(Packet<T> packet) {
        if(CodeClient.currentAction.onSendPacket(packet)) return true;
        if(BuildClip.onPacket(packet)) return true;
        Event.onSendPacket(packet);
        String name = packet.getClass().getName().replace("net.minecraft.network.packet.c2s.play.","");
        if(packet instanceof CommandExecutionC2SPacket commandExecutionC2SPacket) {
            LOGGER.info(commandExecutionC2SPacket.command());
        }
//        LOGGER.info(name);
        return false;
    }

    /**
     * All tick events and debugging.
     */
    public static void onTick() {

        currentAction.onTick();
        Debug.tick();
        BuildClip.tick();
        ChestPeeker.tick();
        RecentChestInsert.tick();

        if(location instanceof Dev dev) {
            if(MC.player == null) return;
            MC.player.getAbilities().allowFlying = true;
            if(NoClip.isIgnoringWalls()) MC.player.noClip = true;
            if(editBind.wasPressed()) {
                MC.setScreen(new DevInventoryScreen(MC.player));
            }
            if(dev.getSize() == null) {
                var pos = new BlockPos(dev.getX() - 1,49,dev.getZ());
                if(CodeClient.MC.world.getBlockState(pos.south(50)).isOf(Blocks.STONE)) dev.setSize(Plot.Size.BASIC);
                if(CodeClient.MC.world.getBlockState(pos.south(100)).isOf(Blocks.STONE)) dev.setSize(Plot.Size.LARGE);
                if(CodeClient.MC.world.getBlockState(pos.south(300)).isOf(Blocks.STONE)) dev.setSize(Plot.Size.MASSIVE);
            }
        }
        if(CodeClient.location instanceof Spawn spawn && spawn.consumeHasJustJoined()) {
            if(autoJoin == AutoJoin.PLOT) {
                MC.getNetworkHandler().sendCommand("join " + Config.getConfig().AutoJoinPlotId);
                autoJoin = AutoJoin.NONE;
            } else if(Config.getConfig().AutoFly) {
                MC.getNetworkHandler().sendCommand("fly");
            }
        }
    }
    public static void onRender(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        Debug.render(matrices, vertexConsumers);
        RecentChestInsert.render(matrices, vertexConsumers, cameraX, cameraY, cameraZ);
        if(shouldReload) {
            MC.worldRenderer.reload();
            shouldReload = false;
        }
    }

    /**
     * Registers barriers as visible.
     * Starts the API.
     * Sets up auto join if enabled.
     * Setups the edit bind.
     * Registers command callback.
     */
    @Override
    public void onInitialize() {
        MC = MinecraftClient.getInstance();
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.BARRIER, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.STRUCTURE_VOID, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.LIGHT, RenderLayer.getTranslucent());

        ClientLifecycleEvents.CLIENT_STOPPING.register(new Identifier(MOD_ID,"close"), client -> SocketHandler.stop());

        if(Config.getConfig().CodeClientAPI) {
            try {
                SocketHandler.start();
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
        if(Config.getConfig().AutoJoin) {
            autoJoin = AutoJoin.GAME;
        }

        editBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.codeclient.actionpallete",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                "category.codeclient.dev"
        ));
        clipBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.codeclient.phaser",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.codeclient.dev"
        ));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            Commands.register(dispatcher);
        });
    }

    public enum AutoJoin {
        /**
         * Done or nothing to act on.
         */
        NONE,
        /**
         * If the main menu should take us to the server.
         */
        GAME,
        /**
         * If we need to automatically join the plot.
         */
        PLOT
    }
}
