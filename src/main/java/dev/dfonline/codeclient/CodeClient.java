package dev.dfonline.codeclient;

import com.google.gson.Gson;
import dev.dfonline.codeclient.action.Action;
import dev.dfonline.codeclient.action.None;
import dev.dfonline.codeclient.dev.DevInventory.DevInventoryScreen;
import dev.dfonline.codeclient.dev.NoClip;
import dev.dfonline.codeclient.location.Dev;
import dev.dfonline.codeclient.location.Location;
import dev.dfonline.codeclient.websocket.SocketHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.dfonline.codeclient.WorldPlot.Size;


public class CodeClient implements ModInitializer {
    public static final String MOD_NAME = "CodeClient";
    public static final String MOD_ID = "codeclient";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final MinecraftClient MC = MinecraftClient.getInstance();
    public static final Gson gson = new Gson();
    private static KeyBinding editBind;


    @NotNull
    public static Action currentAction = new None();
    public static Size worldPlot = null;
    public static Location location = null;

    public static <T extends PacketListener> boolean handlePacket(Packet<T> packet) {
        String name = packet.getClass().getName().replace("net.minecraft.network.packet.s2c.play.","");
//        if(!List.of("PlayerListS2CPacket","WorldTimeUpdateS2CPacket","GameMessageS2CPacket","KeepAliveS2CPacket", "ChunkDataS2CPacket", "UnloadChunkS2CPacket","TeamS2CPacket", "ChunkRenderDistanceCenterS2CPacket", "MessageHeaderS2CPacket", "LightUpdateS2CPacket", "OverlayMessageS2CPacket").contains(name)) LOGGER.info(name);
        if((MC.currentScreen instanceof GameMenuScreen || MC.currentScreen instanceof ChatScreen) && packet instanceof CloseScreenS2CPacket) {
            return true;
        }

        return false;
    }
    public static <T extends PacketListener> boolean onSendPacket(Packet<T> packet) {
        String name = packet.getClass().getName().replace("net.minecraft.network.packet.c2s.play.","");
//        LOGGER.info(name);
        return false;
    }

    public static void onTick() {
        if(NoClip.ignoresWalls() && location instanceof Dev) {
            MC.player.noClip = true;
        }
        while(editBind.isPressed()) {
            MC.setScreen(new DevInventoryScreen(MC.player));
        }
    }

    @Override
    public void onInitialize() {
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.BARRIER, RenderLayer.getTranslucent());

        try {
            SocketHandler.start();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        editBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.codeclient.actionpallete",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                "category.codeclient.dev"
        ));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            Commands.register(dispatcher);
        });
    }
}
