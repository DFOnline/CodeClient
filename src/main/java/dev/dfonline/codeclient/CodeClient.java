package dev.dfonline.codeclient;

import dev.dfonline.codeclient.action.Action;
import dev.dfonline.codeclient.action.None;
import dev.dfonline.codeclient.action.impl.ClearPlot;
import dev.dfonline.codeclient.action.impl.GetActionDump;
import dev.dfonline.codeclient.action.impl.MoveToSpawn;
import dev.dfonline.codeclient.action.impl.PlaceTemplates;
import dev.dfonline.codeclient.dev.NoClip;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


public class CodeClient implements ModInitializer {
    public static final String MOD_NAME = "CodeClient";
    public static final String MOD_ID = "codeclient";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final MinecraftClient MC = MinecraftClient.getInstance();

    @NotNull
    public static Action currentAction = new None();

    public static <T extends PacketListener> boolean handlePacket(Packet<T> packet) {
        String name = packet.getClass().getName().replace("net.minecraft.network.packet.s2c.play.","");
//        if(!List.of("PlayerListS2CPacket","WorldTimeUpdateS2CPacket","GameMessageS2CPacket","KeepAliveS2CPacket", "ChunkDataS2CPacket", "UnloadChunkS2CPacket", "TeamS2CPacket", "ChunkRenderDistanceCenterS2CPacket", "MessageHeaderS2CPacket", "LightUpdateS2CPacket").contains(name)) LOGGER.info(name);
        return false;
    }
    public static <T extends PacketListener> boolean onSendPacket(Packet<T> packet) {
        String name = packet.getClass().getName().replace("net.minecraft.network.packet.c2s.play.","");
//        LOGGER.info(name);
        return false;
    }

    public static void onTick() {
        if(MC.player.getPos().distanceTo(PlotLocation.getAsVec3d()) > 1000) PlotLocation.set(0,0,0);
        if(NoClip.ignoresWalls()) {
            MC.player.noClip = true;
        }
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
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("abort").executes(context -> {
                currentAction = new None();
                return 0;
            }));


            dispatcher.register(ClientCommandManager.literal("getactiondump").executes(context -> {
                currentAction = new GetActionDump(true, () -> MC.player.sendMessage(Text.literal("Done!")));
                currentAction.init();
                return 0;
            }));


            dispatcher.register(ClientCommandManager.literal("getspawn").executes(context -> {
                currentAction = new MoveToSpawn(() -> MC.player.sendMessage(Text.literal("Done!")));
                currentAction.init();
                return 0;
            }));
            dispatcher.register(ClientCommandManager.literal("clearplot").executes(context -> {
                currentAction = new ClearPlot(() -> MC.player.sendMessage(Text.literal("Done!")));
                currentAction.init();
                return 0;
            }));
            dispatcher.register(ClientCommandManager.literal("placetemplate").executes(context -> {
                currentAction = new PlaceTemplates(TemplatesInInventory(), () -> MC.player.sendMessage(Text.literal("Done!")));
                currentAction.init();
                return 0;
            }));

            dispatcher.register(ClientCommandManager.literal("codeforme").executes(context -> {
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
