package dev.dfonline.codeclient;

import com.google.gson.Gson;
import dev.dfonline.codeclient.action.Action;
import dev.dfonline.codeclient.action.None;
import dev.dfonline.codeclient.action.impl.*;
import dev.dfonline.codeclient.actiondump.ActionDump;
import dev.dfonline.codeclient.dev.DevInventory.DevInventoryScreen;
import dev.dfonline.codeclient.dev.NoClip;
import dev.dfonline.codeclient.location.Dev;
import dev.dfonline.codeclient.location.Location;
import dev.dfonline.codeclient.websocket.SocketHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.dfonline.codeclient.WorldPlot.Size;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;


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
            MC.player.airStrafingSpeed = .07f * (MC.player.getMovementSpeed() * 10);
        }
        while(editBind.isPressed()) {
            MC.setScreen(new DevInventoryScreen(MC.player));
        }
    }

    @Override
    public void onInitialize() {

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
            dispatcher.register(literal("auth").executes(context -> {
                SocketHandler.setAuthorised(true);
                Utility.sendMessage("The connect app has been authorised,§l it can now do anything to your plot code.", ChatType.SUCCESS);
                Utility.sendMessage("You can remove the app by running §e/auth remove", ChatType.INFO);
                return 0;
            }).then(literal("remove").executes(context -> {
                SocketHandler.setAuthorised(false);
                Utility.sendMessage("The connected app is no longer authorised, which might break it.", ChatType.SUCCESS);
                return 0;
            })).then(literal("disconnect").executes(context -> {
                Utility.sendMessage("Not implemented.", ChatType.FAIL);
                SocketHandler.setConnection(null);
                return 0;
            })));


            dispatcher.register(literal("worldplot").executes(context -> {
                worldPlot = null;
                return 0;
            }).then(literal("basic").executes(context -> {
                worldPlot = Size.BASIC;
                return 0;
            })).then(literal("large").executes(context -> {
                worldPlot = Size.LARGE;
                return 0;
            })).then(literal("massive").executes(context -> {
                worldPlot = Size.MASSIVE;
                return 0;
            })));


            dispatcher.register(literal("fixcc").executes(context -> {
                currentAction = new None();
                worldPlot = null;
                location = null;
                SocketHandler.setConnection(null);
                ActionDump.clear();
                return 0;
            }));


            dispatcher.register(literal("abort").executes(context -> {
                currentAction = new None();
                return 0;
            }));


            dispatcher.register(literal("getactiondump").executes(context -> {
                currentAction = new GetActionDump(false, () -> Utility.sendMessage("Done!", ChatType.SUCCESS));
                currentAction.init();
                return 0;
            }).then(literal("colors").executes(context -> {
                currentAction = new GetActionDump(true, () -> Utility.sendMessage("Done!", ChatType.SUCCESS));
                currentAction.init();
                return 0;
            })));


            dispatcher.register(literal("getspawn").executes(context -> {
                currentAction = new MoveToSpawn(() -> Utility.sendMessage("Done!", ChatType.SUCCESS));
                currentAction.init();
                return 0;
            }));
            dispatcher.register(literal("getsize").executes(context -> {
                currentAction = new GetPlotSize(() -> {
                    currentAction = new None();
                    Utility.sendMessage(Text.literal(worldPlot.name()));
                });
                currentAction.init();
                return 0;
            }));
            dispatcher.register(literal("clearplot").executes(context -> {
                currentAction = new ClearPlot(() -> Utility.sendMessage("Done!", ChatType.SUCCESS));
                currentAction.init();
                return 0;
            }));
            dispatcher.register(literal("placetemplate").executes(context -> {
                currentAction = new PlaceTemplates(Utility.TemplatesInInventory(), () -> Utility.sendMessage("Done!", ChatType.SUCCESS));
                currentAction.init();
                return 0;
            }));

            dispatcher.register(literal("codeforme").executes(context -> {
                currentAction = new ClearPlot(() -> {
                    currentAction = new MoveToSpawn(() -> {
                        currentAction = new PlaceTemplates(Utility.TemplatesInInventory(), () -> {
                            Utility.sendMessage("Done!", ChatType.SUCCESS);
                        });
                        currentAction.init();
                    });
                    currentAction.init();
                });
                currentAction.init();
                return 0;
            }));
        });
    }
}
