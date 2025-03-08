package dev.dfonline.codeclient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dfonline.codeclient.action.Action;
import dev.dfonline.codeclient.action.None;
import dev.dfonline.codeclient.action.impl.DevForBuild;
import dev.dfonline.codeclient.command.CommandManager;
import dev.dfonline.codeclient.command.CommandSender;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.config.KeyBinds;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.data.ItemData;
import dev.dfonline.codeclient.data.PublicBukkitValues;
import dev.dfonline.codeclient.data.value.DataValue;
import dev.dfonline.codeclient.data.value.NumberDataValue;
import dev.dfonline.codeclient.data.value.StringDataValue;
import dev.dfonline.codeclient.dev.*;
import dev.dfonline.codeclient.dev.debug.Debug;
import dev.dfonline.codeclient.dev.highlighter.ExpressionHighlighter;
import dev.dfonline.codeclient.dev.menu.AdvancedMiddleClickFeature;
import dev.dfonline.codeclient.dev.menu.InsertOverlayFeature;
import dev.dfonline.codeclient.dev.menu.RecentValues;
import dev.dfonline.codeclient.dev.menu.SlotGhostManager;
import dev.dfonline.codeclient.dev.overlay.ActionViewer;
import dev.dfonline.codeclient.dev.overlay.CPUDisplay;
import dev.dfonline.codeclient.dev.overlay.ChestPeeker;
import dev.dfonline.codeclient.hypercube.actiondump.ActionDump;
import dev.dfonline.codeclient.location.*;
import dev.dfonline.codeclient.switcher.ScopeSwitcher;
import dev.dfonline.codeclient.switcher.SpeedSwitcher;
import dev.dfonline.codeclient.switcher.StateSwitcher;
import dev.dfonline.codeclient.websocket.SocketHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class CodeClient implements ClientModInitializer {
    public static final String MOD_NAME = "CodeClient";
    public static final String MOD_ID = "codeclient";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final Gson gson = new Gson();
    public static MinecraftClient MC = MinecraftClient.getInstance();

    public static AutoJoin autoJoin = AutoJoin.NONE;
    public static Utility.Toast startupToast;

    @NotNull
    public static Action currentAction = new None();
    public static Action confirmingAction = null;
    public static Location lastLocation = null;
    public static Location location = null;
    /**
     * Used to open a screen on the next tick.
     */
    public static Screen screenToOpen = null;
    public static boolean shouldReload = false;
    public static boolean isPreviewingItemTags = false;

    private static final HashMap<Class<? extends Feature>, Feature> features = new HashMap<>();
    private static boolean isCodeChest = false;

    public static SocketHandler API = new SocketHandler();

    @Override
    public void onInitializeClient() {
        MC = MinecraftClient.getInstance();

        loadFeatures();

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (MC.player == null || MC.world == null) clean();
            if (screenToOpen != null) {
                MC.setScreen(screenToOpen);
                screenToOpen = null;
            }
        });

        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.BARRIER, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.STRUCTURE_VOID, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.LIGHT, RenderLayer.getTranslucent());

        ClientLifecycleEvents.CLIENT_STOPPING.register(Identifier.of(MOD_ID, "close"), client -> API.stop());

        ClientTickEvents.END_CLIENT_TICK.register(client -> CommandSender.tick());

        if (Config.getConfig().CodeClientAPI) {
            try {
                API.start();
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
        if (Config.getConfig().AutoJoin) {
            autoJoin = AutoJoin.GAME;
        }

        KeyBinds.init();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> CommandManager.init(dispatcher, registryAccess));

        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (isPreviewingItemTags) {
                DFItem item = DFItem.of(stack);
                ItemData itemData = item.getItemData();
                if (itemData == null) return;
                PublicBukkitValues publicBukkit = itemData.getPublicBukkitValues();
                if (publicBukkit == null) return;
                for (var key : publicBukkit.getHypercubeKeys()) {
                    DataValue element = publicBukkit.getHypercubeValue(key);

                    // Any type = yellow, number = red, string = aqua.
                    Formatting formatting = Formatting.GREEN;
                    String stringElement = element.getValue() == null ? "?" : element.getValue().toString();
                    if (element instanceof StringDataValue) {
                        formatting = Formatting.AQUA;
                    }
                    if (element instanceof NumberDataValue numberDataValue) {
                        formatting = Formatting.RED;
                        stringElement = String.valueOf(numberDataValue.getValue());
                    }

                    lines.add(
                            Text.literal(key)
                                    .withColor(0xAAFF55)
                                    .append(Text.literal(" = ").formatted(Formatting.DARK_GRAY))
                                    .append(Text.literal(stringElement).formatted(formatting))
                    );
                }
            }
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenKeyboardEvents.allowKeyPress(screen).register((screen1, key, scancode, modifiers) -> !CodeClient.onKeyPressed(key, scancode, modifiers));
            ScreenKeyboardEvents.allowKeyRelease(screen).register((screen1, key, scancode, modifiers) -> !CodeClient.onKeyReleased(key, scancode,  modifiers));
            ScreenMouseEvents.allowMouseClick(screen).register((screen1, mouseX, mouseY, button) -> !CodeClient.onMouseClicked(mouseX, mouseY, button));
            ScreenMouseEvents.allowMouseScroll(screen).register((screen1, mouseX, mouseY, horizontalAmount, verticalAmount) -> !CodeClient.onMouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount));
        });

        try {
            registerResourcePack("dark_mode", Text.literal("Dark Mode").formatted(Formatting.WHITE));
        } catch (NullPointerException exception) {
            LOGGER.warn("Could not load dark mode resource pack!");
        }

        LOGGER.info("CodeClient, making it easier to wipe your plot and get banned for hacks since 2022");
    }

    private static void feat(Feature feature) {
        features.put(feature.getClass(), feature);
    }

    private static void loadFeatures() {
        features.clear();

        feat(new BuildPhaser());
        feat(new ChestPeeker());
        feat(new Debug());
        feat(new RecentChestInsert());
        feat(new BlockBreakDeltaCalculator());
        feat(new Navigation());
        feat(new NoClip());
        feat(new ReportBrokenBlocks());
        feat(new InsertOverlayFeature());
        feat(new SlotGhostManager());
        feat(new ActionViewer());
        feat(new RecentValues());
        feat(new ValueDetails());
        feat(new ChatAutoEdit());
        feat(new CPUDisplay());
        feat(new MessageHiding());
        feat(new ExpressionHighlighter());
        feat(new PreviewSoundChest());
        feat(new AdvancedMiddleClickFeature());
        feat(new StateSwitcher.StateSwitcherFeature());
        feat(new SpeedSwitcher.SpeedSwitcherFeature());
        feat(new ScopeSwitcher.ScopeSwitcherFeature());
    }

    /**
     * Get all active features.
     */
    private static Stream<Feature> features() {
        return features.values().stream().filter(Feature::enabled);
    }

    /**
     * Get all active chest features.
     */
    private static Stream<ChestFeature> chestFeatures() {
        return features().map(Feature::getChest).filter(Optional::isPresent).map(Optional::get);
    }

    /**
     * Get an identifier using the mod id as the namespace.
     *
     * @param path The path to the resource.
     * @return Identifier under the mod id's namespace and the provided path as the path.
     */
    public static Identifier getId(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static void isCodeChest() {
        isCodeChest = true;
    }

    public static <T extends Feature> Optional<T> getFeature(Class<T> clazz) {
        var feat = features.get(clazz);
        if (feat != null && feat.enabled()) return Optional.of(clazz.cast(feat));
        return Optional.empty();
    }

    public static <T extends PacketListener> boolean handlePacket(Packet<T> packet) {
        if (packet instanceof BundleS2CPacket bundle) {
            bundle.getPackets().forEach(CodeClient::handlePacket);
            return false;
        }


        if (currentAction.onReceivePacket(packet)) return true;
        for (var feature : features().toList()) {
            if (feature.onReceivePacket(packet)) return true;
        }
        Event.handlePacket(packet);
        LastPos.handlePacket(packet);

        //noinspection unused
        String name = packet.getClass().getName().replace("net.minecraft.network.packet.s2c.play.", "");
//        if(!java.util.List.of("PlayerListS2CPacket","WorldTimeUpdateS2CPacket","GameMessageS2CPacket","KeepAliveS2CPacket", "ChunkDataS2CPacket", "UnloadChunkS2CPacket","TeamS2CPacket", "ChunkRenderDistanceCenterS2CPacket", "MessageHeaderS2CPacket", "LightUpdateS2CPacket", "OverlayMessageS2CPacket").contains(name)) LOGGER.info(name);

        if (CodeClient.location instanceof Dev dev) {
            try {
                if (packet instanceof BlockEntityUpdateS2CPacket beu && dev.isInDev(beu.getPos()) && beu.getBlockEntityType() == BlockEntityType.SIGN) {
                    dev.clearLineStarterCache();
                }
            } catch (ConcurrentModificationException exception) {
                // Not sure how this comes to happen. My guess it's the getBlockEntity call.
                // Unfortunately, I don't know what state the game has to be in to make it fail, maybe an unloaded chunk?
                // It's hard to check for that, apparently.
                dev.clearLineStarterCache();
            }
        }
        return (MC.currentScreen instanceof GameMenuScreen || MC.currentScreen instanceof ChatScreen || MC.currentScreen instanceof StateSwitcher) && packet instanceof CloseScreenS2CPacket;
    }

    /**
     * All outgoing packet events and debugging.
     *
     * @param <T> ClientToServer
     * @return If the packet shouldn't be sent. True to not send.
     */
    public static <T extends PacketListener> boolean onSendPacket(Packet<T> packet) {
        if (CodeClient.currentAction.onSendPacket(packet)) return true;
        for (var feature : features().toList()) if (feature.onSendPacket(packet)) return true;
        Event.onSendPacket(packet);
        //noinspection unused
        String name = packet.getClass().getName().replace("net.minecraft.network.packet.c2s.play.", "");
//        LOGGER.info(name);
        return false;
    }

    public static void onTick() {
        currentAction.tick();
        features().forEach(Feature::tick);
        KeyBinds.tick();

//        System.out.println(location.name());

        if (!(location instanceof Dev) || !(MC.currentScreen instanceof HandledScreen<?>)) {
            isCodeChest = false;
            features().forEach(Feature::closeChest);
        }

        if (location instanceof Dev dev) {
            if (MC.player == null) return;
            var pos = new BlockPos(dev.getX() - 1, 49, dev.getZ());
            if (dev.getSize() == null) {
                // TODO wait for plugin messages, or make a fix now.
                var world = CodeClient.MC.world;
                if (world == null) return;
                var FIFTY = world.getBlockState(pos.south(50));
                var FIFTY_ONE = world.getBlockState(pos.south(51));
                var HUNDRED = world.getBlockState(pos.south(100));
                var HUNDRED_ONE = world.getBlockState(pos.south(101));
                var THREE_HUNDRED = world.getBlockState(pos.south(300));
                var THREE_HUNDRED_ONE = world.getBlockState(pos.south(301));
                var MEGA = world.getBlockState(pos.add(-19, 0, 10));
                var MEGA_ONE = world.getBlockState(pos.add(-20, 0, 10));
                if (MEGA_ONE.isOf(Blocks.GRASS_BLOCK) && MEGA.isOf(Blocks.GRASS_BLOCK)) {
                    dev.setSize(Plot.Size.MEGA);
                } else if (!MEGA.isOf(Blocks.VOID_AIR) && !MEGA_ONE.isOf(Blocks.VOID_AIR) && !MEGA.isOf(Blocks.GRASS_BLOCK) && !MEGA.isOf(Blocks.STONE) && !MEGA_ONE.isOf(Blocks.GRASS_BLOCK)) {
                    dev.setSize(Plot.Size.MEGA);
                } else if (!(FIFTY.isOf(Blocks.VOID_AIR) || FIFTY_ONE.isOf(Blocks.VOID_AIR)) && (!FIFTY.isOf(FIFTY_ONE.getBlock()))) {
                    dev.setSize(Plot.Size.BASIC);
                } else if (!(HUNDRED.isOf(Blocks.VOID_AIR) || HUNDRED_ONE.isOf(Blocks.VOID_AIR)) && !HUNDRED.isOf(HUNDRED_ONE.getBlock())) {
                    dev.setSize(Plot.Size.LARGE);
                } else if (!(THREE_HUNDRED.isOf(Blocks.VOID_AIR) || THREE_HUNDRED_ONE.isOf(Blocks.VOID_AIR)) && !THREE_HUNDRED.isOf(THREE_HUNDRED_ONE.getBlock())) {
                    dev.setSize(Plot.Size.MASSIVE);
                }
            }
            var size = dev.assumeSize();
            assert CodeClient.MC.world != null;
            var groundCheck = MC.world.getBlockState(new BlockPos(
                    Math.max(Math.min((int) MC.player.getX(), dev.getX() - 1), dev.getX() - (size.codeWidth)),
                    49,
                    Math.max(Math.min((int) MC.player.getZ(), dev.getZ() + size.codeLength), dev.getZ())
            ));
            if (!groundCheck.isOf(Blocks.VOID_AIR))
                dev.setHasUnderground(!groundCheck.isOf(Blocks.GRASS_BLOCK) && !groundCheck.isOf(Blocks.STONE));
        }
        if (CodeClient.location instanceof Spawn spawn && MC.getNetworkHandler() != null && spawn.consumeHasJustJoined()) {
            if (autoJoin == AutoJoin.PLOT) {
                MC.getNetworkHandler().sendCommand("join " + Config.getConfig().AutoJoinPlotId);
                autoJoin = AutoJoin.NONE;
            } else if (Config.getConfig().AutoFly) {
                MC.getNetworkHandler().sendCommand("fly");
            }
        }
    }

    public static void onRender(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        features().forEach(feature -> feature.render(matrices, vertexConsumers, cameraX, cameraY, cameraZ));

        if (shouldReload) {
            MC.worldRenderer.reload();
            shouldReload = false;
        }
    }

    public static void onClickChest(BlockHitResult hitResult) {
        features().forEach(feature -> feature.onClickChest(hitResult));
    }

    public static void onBreakBlock(Dev dev, BlockPos pos, BlockPos breakPos) {
        features().forEach(feature -> feature.onBreakBlock(dev, pos, breakPos));
    }

    public static void onScreenInit(HandledScreen<?> screen) {
        features().forEach(Feature::closeChest);
        if (!isCodeChest) return;
        features().forEach(feat -> feat.openChest(screen));
    }

    public static void onScreenClosed() {
        isCodeChest = false;
        features().forEach(Feature::closeChest);
    }

    public static void onRender(DrawContext context, int mouseX, int mouseY, int x, int y, float delta) {
        chestFeatures().forEach(feat -> feat.render(context, mouseX, mouseY, x, y, delta));
    }

    public static void onDrawSlot(DrawContext context, Slot slot) {
        chestFeatures().forEach(feat -> feat.drawSlot(context, slot));
    }

    public static ItemStack onGetHoverStack(Slot instance) {
        return chestFeatures().map(feat -> feat.getHoverStack(instance)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public static boolean onMouseClicked(double mouseX, double mouseY, int button) {
        return chestFeatures().anyMatch(feature -> feature.mouseClicked(mouseX, mouseY, button));
    }

    public static boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        return chestFeatures().anyMatch(feature -> feature.keyPressed(keyCode, scanCode, modifiers));
    }

    public static boolean onKeyReleased(int keyCode, int scanCode, int modifiers) {
        return chestFeatures().anyMatch(feature -> feature.keyReleased(keyCode, scanCode, modifiers));
    }

    public static boolean onCharTyped(char chr, int modifiers) {
        return chestFeatures().anyMatch(feature -> feature.charTyped(chr, modifiers));
    }

    public static boolean onClickSlot(Slot slot, int button, SlotActionType actionType, int syncId, int revision) {
        return chestFeatures().anyMatch(feature -> feature.clickSlot(slot, button, actionType, syncId, revision));
    }

    public static boolean onMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return chestFeatures().anyMatch(feature -> feature.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount));
    }

    public static boolean noClipOn() {
        if (MC.player == null) return false;
        if (!Config.getConfig().NoClipEnabled) return false;
        if (!(location instanceof Dev)) return false;
        if (!(currentAction instanceof None)) return false;
        return MC.player.getAbilities().creativeMode;
    }

    /**
     * Remove all state from being on DF.
     */
    public static void clean() {
        for (var feature : features.values()) {
            feature.reset();
        }
        CodeClient.currentAction = new None();
        CodeClient.confirmingAction = null;
        CodeClient.location = null;
        CodeClient.screenToOpen = null;
    }

    /**
     * As much as possible, set CodeClient to its startup state.
     */
    public static void reset() {
        clean();
        loadFeatures();
        API.setConnection(null);
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
     *
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
     *
     * @param id   the resource id
     * @param name the resource pack's display name
     * @return if the resource pack was created
     * @throws NullPointerException if the mod container is not found
     */
    private boolean registerResourcePack(String id, Text name) throws NullPointerException {
        return registerResourcePack(id, name, ResourcePackActivationType.NORMAL);
    }

    /**
     * Registers a resource pack with the given activation type.
     *
     * @param id   the resource id
     * @param name the resource pack's display name
     * @param type the resource pack's activation type
     * @return if the resource pack was created
     * @throws NullPointerException if the mod container is not found
     */
    private boolean registerResourcePack(String id, Text name, ResourcePackActivationType type) throws NullPointerException {
        var prefix = String.format("[%s] ", MOD_NAME);
        return ResourceManagerHelper.registerBuiltinResourcePack(
                getId(id),
                getModContainer(),
                Text.literal(prefix).formatted(Formatting.GRAY).append(name),
                type
        );
    }

    public static void parseVersionInfo(String response) {
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();

        var versionNumber = json.get("version_number").getAsString();

        if (Config.getConfig().AutoUpdateOption != Config.AutoUpdate.UPDATE) {
            startupToast = new Utility.Toast(Text.translatable("toast.codeclient.update_available.title", versionNumber), Text.translatable("toast.codeclient.update_available"));
            return;
        }
        var files = json.getAsJsonArray("files");
        files.forEach(file -> {
                    var fileObject = file.getAsJsonObject();
                    if (fileObject.get("primary").getAsBoolean()) {
                        var url = fileObject.get("url").getAsString();
                        LOGGER.info("Updated mod URL: {}", url);
                        // Download the file.
                        try (
                                InputStream inputStream = new URI(url).toURL().openStream();
                                ReadableByteChannel rbc = Channels.newChannel(inputStream);
                                FileOutputStream fos = new FileOutputStream("mods/" + CodeClient.MOD_NAME + "-" + versionNumber + ".jar")
                        ) {
                            LOGGER.info("Starting download...");
                            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                        } catch (Exception e) {
                            LOGGER.error("Failed to download the file: {}", e.getMessage());
                        } finally {
                            LOGGER.info("Download complete.");
                            startupToast = new Utility.Toast(Text.translatable("toast.codeclient.update.title", versionNumber), Text.translatable("toast.codeclient.update"));
                        }
                    }
                }
        );
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
