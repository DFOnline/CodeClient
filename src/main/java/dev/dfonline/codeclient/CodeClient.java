package dev.dfonline.codeclient;

import dev.dfonline.codeclient.action.Action;
import dev.dfonline.codeclient.action.None;
import dev.dfonline.codeclient.action.impl.ClearPlot;
import dev.dfonline.codeclient.action.impl.GetActionDump;
import dev.dfonline.codeclient.action.impl.MoveToSpawn;
import dev.dfonline.codeclient.action.impl.PlaceTemplates;
import dev.dfonline.codeclient.dev.AddCodeScreen;
import dev.dfonline.codeclient.dev.NoClip;
import dev.dfonline.codeclient.websocket.SocketHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static dev.dfonline.codeclient.WorldPlot.Size;


public class CodeClient implements ModInitializer {
    public static final String MOD_NAME = "CodeClient";
    public static final String MOD_ID = "codeclient";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final MinecraftClient MC = MinecraftClient.getInstance();
    private static KeyBinding editBind;


    @NotNull
    public static Action currentAction = new None();
    public static Size worldPlot = null;

    public static <T extends PacketListener> boolean handlePacket(Packet<T> packet) {
        String name = packet.getClass().getName().replace("net.minecraft.network.packet.s2c.play.","");
//        if(!List.of("PlayerListS2CPacket","WorldTimeUpdateS2CPacket","GameMessageS2CPacket","KeepAliveS2CPacket", "ChunkDataS2CPacket", "UnloadChunkS2CPacket", "TeamS2CPacket", "ChunkRenderDistanceCenterS2CPacket", "MessageHeaderS2CPacket", "LightUpdateS2CPacket", "OverlayMessageS2CPacket").contains(name)) LOGGER.info(name);
        return false;
    }
    public static <T extends PacketListener> boolean onSendPacket(Packet<T> packet) {
        String name = packet.getClass().getName().replace("net.minecraft.network.packet.c2s.play.","");
//        LOGGER.info(name);
        return false;
    }

    public static void onTick() {
        if(NoClip.ignoresWalls()) {
            MC.player.noClip = true;
            MC.player.airStrafingSpeed = 0.07f;
        }
        while(editBind.isPressed()) {
            MC.setScreen(new CodeClientScreen(new AddCodeScreen()));
        }
//        if(MC.keyboard)
    }

    private static ArrayList<ItemStack> TemplatesInInventory() {
        PlayerInventory inv = MC.player.getInventory();
        ArrayList<ItemStack> templates = new ArrayList<>();
        for (int i = 0; i < (27 + 9); i++) {
            ItemStack item = inv.getStack(i);
            if (!item.hasNbt()) continue;
            NbtCompound nbt = item.getNbt();
            if (!nbt.contains("PublicBukkitValues")) continue;
            LOGGER.info(String.valueOf(item));
            NbtCompound publicBukkit = nbt.getCompound("PublicBukkitValues");
            if (!publicBukkit.contains("hypercube:codetemplatedata")) continue;
            templates.add(item);
        }
        return templates;
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
                MC.player.sendMessage(Text.literal("§eThe connect app has been authorised,§l it can now do anything to your plot code."));
                MC.player.sendMessage(Text.literal("§eYou can remove the app by running §c/auth remove"));
                return 0;
            }).then(literal("remove").executes(context -> {
                SocketHandler.setAuthorised(false);
                MC.player.sendMessage(Text.literal("§eThe connected app is no longer authorised, which might break it."));
                return 0;
            })).then(literal("disconnect").executes(context -> {
                MC.player.sendMessage(Text.of("§cNot implemented."));
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


            dispatcher.register(literal("abort").executes(context -> {
                currentAction = new None();
                return 0;
            }));


            dispatcher.register(literal("getactiondump").executes(context -> {
                currentAction = new GetActionDump(true, () -> MC.player.sendMessage(Text.literal("Done!")));
                currentAction.init();
                return 0;
            }));


            dispatcher.register(literal("getspawn").executes(context -> {
                currentAction = new MoveToSpawn(() -> MC.player.sendMessage(Text.literal("Done!")));
                currentAction.init();
                return 0;
            }));
            dispatcher.register(literal("clearplot").executes(context -> {
                currentAction = new ClearPlot(() -> MC.player.sendMessage(Text.literal("Done!")));
                currentAction.init();
                return 0;
            }));
            dispatcher.register(literal("placetemplate").executes(context -> {
                currentAction = new PlaceTemplates(TemplatesInInventory(), () -> MC.player.sendMessage(Text.literal("Done!")));
                currentAction.init();
                return 0;
            }));

            dispatcher.register(literal("codeforme").executes(context -> {
                currentAction = new ClearPlot(() -> {
                    currentAction = new MoveToSpawn(() -> {
                        currentAction = new PlaceTemplates(CodeClient.TemplatesInInventory(), () -> {
                            MC.player.sendMessage(Text.literal("Done!"));
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
