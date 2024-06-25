package dev.dfonline.codeclient;

import com.google.gson.Gson;
import dev.dfonline.codeclient.action.Action;
import dev.dfonline.codeclient.action.None;
import dev.dfonline.codeclient.action.impl.DevForBuild;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.config.KeyBinds;
import dev.dfonline.codeclient.dev.*;
import dev.dfonline.codeclient.dev.Debug.Debug;
import dev.dfonline.codeclient.dev.overlay.ChestPeeker;
import dev.dfonline.codeclient.hypercube.actiondump.ActionDump;
import dev.dfonline.codeclient.location.*;
import dev.dfonline.codeclient.switcher.StateSwitcher;
import dev.dfonline.codeclient.websocket.SocketHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodeClient implements ModInitializer {
    public static final String MOD_NAME = "CodeClient";
    public static final String MOD_ID = "codeclient";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final Gson gson = new Gson();
    public static MinecraftClient MC = MinecraftClient.getInstance();

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
     * For all receiving packet events and debugging.
     *
     * @param <T> Server2Client
     * @return If the packet should be cancelled and not acted on. True to ignore.
     */
    public static <T extends PacketListener> boolean handlePacket(Packet<T> packet) {
        if (currentAction.onReceivePacket(packet)) return true;
        if (Debug.handlePacket(packet)) return true;
        if (BuildPhaser.handlePacket(packet)) return true;
        if (ChestPeeker.handlePacket(packet)) return true;
        Event.handlePacket(packet);
        LastPos.handlePacket(packet);

        String name = packet.getClass().getName().replace("net.minecraft.network.packet.s2c.play.", "");
//        if(!java.util.List.of("PlayerListS2CPacket","WorldTimeUpdateS2CPacket","GameMessageS2CPacket","KeepAliveS2CPacket", "ChunkDataS2CPacket", "UnloadChunkS2CPacket","TeamS2CPacket", "ChunkRenderDistanceCenterS2CPacket", "MessageHeaderS2CPacket", "LightUpdateS2CPacket", "OverlayMessageS2CPacket").contains(name)) LOGGER.info(name);

        if (CodeClient.location instanceof Dev dev &&
                packet instanceof BlockEntityUpdateS2CPacket beu &&
                dev.isInDev(beu.getPos()) &&
                MC.world != null &&
                MC.world.getBlockEntity(beu.getPos()) instanceof SignBlockEntity) {
            dev.clearLineStarterCache();
        }
        return (MC.currentScreen instanceof GameMenuScreen || MC.currentScreen instanceof ChatScreen || MC.currentScreen instanceof StateSwitcher) && packet instanceof CloseScreenS2CPacket;
    }

    /**
     * This needs to be true for NoClip to work.
     * Whilst this should be the first source to check if NoClip is on, keep in mind there is NoClip#isIgnoringWalls
     * Useful for fallback checks and preventing noclip packet spam screwing you over.
     */
    public static boolean noClipOn() {
        if (MC.player == null) return false;
        if (!Config.getConfig().NoClipEnabled) return false;
        if (!(location instanceof Dev)) return false;
        if (!(currentAction instanceof None)) return false;
        if (!MC.player.getAbilities().creativeMode) return false;
        return true;
    }

    /**
     * All outgoing packet events and debugging.
     *
     * @param <T> ClientToServer
     * @return If the packet shouldn't be sent. True to not send.
     */
    public static <T extends PacketListener> boolean onSendPacket(Packet<T> packet) {
        if (CodeClient.currentAction.onSendPacket(packet)) return true;
        if (BuildPhaser.onPacket(packet)) return true;
        Event.onSendPacket(packet);
        String name = packet.getClass().getName().replace("net.minecraft.network.packet.c2s.play.", "");
//        LOGGER.info(name);
        return false;
    }

    /**
     * All tick events and debugging.
     */
    public static void onTick() {

        currentAction.onTick();
        Debug.tick();
        BuildPhaser.tick();
        ChestPeeker.tick();
        RecentChestInsert.tick();
        KeyBinds.tick();
        SlotGhostManager.tick();
        Commands.tick();

        if(!(location instanceof Dev) || !(MC.currentScreen instanceof HandledScreen<?>)) {
            InsertOverlay.reset();
        }

        if (location instanceof Dev dev) {
            if (MC.player == null) return;
            MC.player.getAbilities().allowFlying = true;
            if (NoClip.isIgnoringWalls()) MC.player.noClip = true;
            var pos = new BlockPos(dev.getX() - 1, 49, dev.getZ());
            if (dev.getSize() == null) {
                // TODO wait for plugin messages, or make a fix now.
                var world = CodeClient.MC.world;
                if(world == null) return;
                var FIFTY = world.getBlockState(pos.south(50));
                var FIFTY_ONE = world.getBlockState(pos.south(51));
                var HUNDRED = world.getBlockState(pos.south(100));
                var HUNDRED_ONE = world.getBlockState(pos.south(101));
                var THREE_HUNDRED = world.getBlockState(pos.south(300));
                var THREE_HUNDRED_ONE = world.getBlockState(pos.south(301));
                var MEGA = world.getBlockState(pos.add(-19,0,10));
                var MEGA_ONE = world.getBlockState(pos.add(-20,0,10));
                if(MEGA_ONE.isOf(Blocks.GRASS_BLOCK) && MEGA.isOf(Blocks.GRASS_BLOCK)) {
                    dev.setSize(Plot.Size.MEGA);
                }
                else if (!MEGA.isOf(Blocks.VOID_AIR) && !MEGA_ONE.isOf(Blocks.VOID_AIR) && !MEGA.isOf(Blocks.GRASS_BLOCK) && !MEGA.isOf(Blocks.STONE) && !MEGA_ONE.isOf(Blocks.GRASS_BLOCK)) {
                    dev.setSize(Plot.Size.MEGA);
                }
                else if (!(FIFTY.isOf(Blocks.VOID_AIR) || FIFTY_ONE.isOf(Blocks.VOID_AIR)) && (!FIFTY.isOf(FIFTY_ONE.getBlock())))
                    dev.setSize(Plot.Size.BASIC);
                else if (!(HUNDRED.isOf(Blocks.VOID_AIR) || HUNDRED_ONE.isOf(Blocks.VOID_AIR)) && !HUNDRED.isOf(HUNDRED_ONE.getBlock()))
                    dev.setSize(Plot.Size.LARGE);
                else if (!(THREE_HUNDRED.isOf(Blocks.VOID_AIR) || THREE_HUNDRED_ONE.isOf(Blocks.VOID_AIR)) && !THREE_HUNDRED.isOf(THREE_HUNDRED_ONE.getBlock()))
                    dev.setSize(Plot.Size.MASSIVE);

            }
            var size = dev.assumeSize();
            assert CodeClient.MC.world != null;
            var groundCheck = MC.world.getBlockState(new BlockPos(
                    Math.max(Math.min((int) MC.player.getX(),dev.getX() - 1),dev.getX() - (size.codeWidth)),
                    49,
                    Math.max(Math.min((int) MC.player.getZ(),dev.getZ() + size.codeLength), dev.getZ())
            ));
            if(!groundCheck.isOf(Blocks.VOID_AIR))
                dev.setHasUnderground(!groundCheck.isOf(Blocks.GRASS_BLOCK) && !groundCheck.isOf(Blocks.STONE));
        }
        if (CodeClient.location instanceof Spawn spawn && spawn.consumeHasJustJoined()) {
            if (autoJoin == AutoJoin.PLOT) {
                MC.getNetworkHandler().sendCommand("join " + Config.getConfig().AutoJoinPlotId);
                autoJoin = AutoJoin.NONE;
            } else if (Config.getConfig().AutoFly) {
                MC.getNetworkHandler().sendCommand("fly");
            }
        }
    }

    public static void onRender(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        Debug.render(matrices, vertexConsumers);
        RecentChestInsert.render(matrices, vertexConsumers, cameraX, cameraY, cameraZ);
        if (shouldReload) {
            MC.worldRenderer.reload();
            shouldReload = false;
        }
    }

    /**
     * Remove all state from being on DF.
     */
    public static void clean() {
        CodeClient.currentAction = new None();
        CodeClient.location = null;
        BuildPhaser.disableClipping();
        Commands.confirm = null;
        Commands.screen = null;
        Debug.clean();
        SlotGhostManager.reset();
        InsertOverlay.reset();
    }

    /**
     * As much as possible, set CodeClient to its startup state.
     */
    public static void reset() {
        clean();
        SocketHandler.setConnection(null);
        ActionDump.clear();
        Config.clear();
    }

    public static void onModeChange(Location location) {
        if (Config.getConfig().DevForBuild && (currentAction instanceof None || currentAction instanceof DevForBuild) && location instanceof Build) {
            currentAction = new DevForBuild(() -> currentAction = new None());
            currentAction.init();
        }
        currentAction.onModeChange(location);
    }

    /**
     * Gets the mod container needed for registering resources like data packs and resource packs to CodeClient.
     * @return the mod container.
     * @throws NullPointerException the mod's container was not found.
     */
    public static ModContainer getModContainer() throws NullPointerException {
        var container = FabricLoader.getInstance().getModContainer(CodeClient.MOD_ID);
        if (container.isEmpty()) throw new NullPointerException("Could not get mod container.");
        return container.get();
    }

    /**
     * Registers a resource pack with a normal activation type.
     * @param id the resource id
     * @param name the resource pack's display name
     * @return if the resource pack was created
     * @throws NullPointerException if the mod container is not found
     */
    private boolean registerResourcePack(String id, Text name) throws NullPointerException {
        return registerResourcePack(id, name, ResourcePackActivationType.NORMAL);
    }

    /**
     * Registers a resource pack with the given activation type.
     * @param id the resource id
     * @param name the resource pack's display name
     * @param type the resource pack's activation type
     * @return if the resource pack was created
     * @throws NullPointerException if the mod container is not found
     */
    private boolean registerResourcePack(String id, Text name, ResourcePackActivationType type) throws NullPointerException {
        var prefix = String.format("[%s] ", MOD_NAME);
        return ResourceManagerHelper.registerBuiltinResourcePack(
                new Identifier(CodeClient.MOD_ID, id),
                getModContainer(),
                Text.literal(prefix).formatted(Formatting.GRAY).append(name),
                type
        );
    }

    /**
     * Registers barriers as visible.
     * Starts the API.
     * Sets up auto join if enabled.
     * Setups the edit bind.
     * Registers command callback.
     * Registers dark mode resource pack.
     */
    @Override
    public void onInitialize() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (MC.player == null || MC.world == null) clean();
        });

        MC = MinecraftClient.getInstance();
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.BARRIER, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.STRUCTURE_VOID, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.LIGHT, RenderLayer.getTranslucent());

        ClientLifecycleEvents.CLIENT_STOPPING.register(new Identifier(MOD_ID, "close"), client -> SocketHandler.stop());

        if (Config.getConfig().CodeClientAPI) {
            try {
                SocketHandler.start();
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
        if (Config.getConfig().AutoJoin) {
            autoJoin = AutoJoin.GAME;
        }

        KeyBinds.init();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            Commands.register(dispatcher);
        });

        try {
            registerResourcePack("dark_mode", Text.literal("Dark Mode").formatted(Formatting.WHITE));
        } catch (NullPointerException exception) {
            CodeClient.LOGGER.warn("Could not load dark mode resource pack!");
        }

        CodeClient.LOGGER.info("CodeClient, making it easier to wipe your plot and get banned for hacks since 2022");
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
