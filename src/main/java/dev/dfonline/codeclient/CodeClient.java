package dev.dfonline.codeclient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
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
import dev.dfonline.codeclient.config.preset.ConfigPresetScreen;
import dev.dfonline.codeclient.hypercube.actiondump.ActionDump;
import dev.dfonline.codeclient.location.*;
import dev.dfonline.codeclient.switcher.ScopeSwitcher;
import dev.dfonline.codeclient.switcher.SpeedSwitcher;
import dev.dfonline.codeclient.switcher.StateSwitcher;
import dev.dfonline.codeclient.websocket.SocketHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.phys.BlockHitResult;
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
    public static Minecraft MC = Minecraft.getInstance();

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
        MC = Minecraft.getInstance();

        loadFeatures();

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (MC.player == null || MC.level == null) clean();
            if (screenToOpen != null) {
                MC.setScreen(screenToOpen);
                screenToOpen = null;
            }
        });


        BlockRenderLayerMap.putBlock(Blocks.BARRIER, ChunkSectionLayer.TRANSLUCENT);
        BlockRenderLayerMap.putBlock(Blocks.STRUCTURE_VOID, ChunkSectionLayer.TRANSLUCENT);
        BlockRenderLayerMap.putBlock(Blocks.LIGHT, ChunkSectionLayer.TRANSLUCENT);

        ClientLifecycleEvents.CLIENT_STOPPING.register(Identifier.fromNamespaceAndPath(MOD_ID, "close"), client -> API.stop());

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
            if (isPreviewingItemTags && ((location instanceof Plot plot && plot.getHasDev().orElse(false)) || location instanceof Creator)) {
                DFItem item = DFItem.of(stack);
                ItemData itemData = item.getItemData();
                if (itemData == null) return;
                PublicBukkitValues publicBukkit = itemData.getPublicBukkitValues();
                if (publicBukkit == null) return;
                for (var key : publicBukkit.getHypercubeKeys()) {
                    DataValue element = publicBukkit.getHypercubeValue(key);

                    // Any type = yellow, number = red, string = aqua.
                    ChatFormatting formatting = ChatFormatting.GREEN;
                    String stringElement = element.getValue() == null ? "?" : element.getValue().toString();
                    if (element instanceof StringDataValue) {
                        formatting = ChatFormatting.AQUA;
                    }
                    if (element instanceof NumberDataValue numberDataValue) {
                        formatting = ChatFormatting.RED;
                        stringElement = String.valueOf(numberDataValue.getValue());
                    }

                    lines.add(
                            Component.literal(key)
                                    .withColor(0xAAFF55)
                                    .append(Component.literal(" = ").withStyle(ChatFormatting.DARK_GRAY))
                                    .append(Component.literal(stringElement).withStyle(formatting))
                    );
                }
            }
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenKeyboardEvents.allowKeyPress(screen).register((screen1, key) -> !CodeClient.onKeyPressed(key));
            ScreenKeyboardEvents.allowKeyRelease(screen).register((screen1, key) -> !CodeClient.onKeyReleased(key));
            ScreenMouseEvents.allowMouseClick(screen).register((screen1, click) -> !CodeClient.onMouseClicked(click));
            ScreenMouseEvents.allowMouseScroll(screen).register((screen1, mouseX, mouseY, horizontalAmount, verticalAmount) -> !CodeClient.onMouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount));

            if (!Config.getConfig().HasSelectedPreset && screen instanceof TitleScreen) {
                client.setScreen(new ConfigPresetScreen(screen));
            }
        });

        try {
            registerResourcePack("dark_mode", Component.literal("Dark Mode").withStyle(ChatFormatting.WHITE));
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
        feat(new GiveStrings());
        feat(new ChatLongValue());
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
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
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
        if (packet instanceof ClientboundBundlePacket bundle) {
            bundle.subPackets().forEach(CodeClient::handlePacket);
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
//        if(!java.util.List.of("PlayerListS2CPacket","WorldTimeUpdateS2CPacket","GameMessageS2CPacket","KeepAliveS2CPacket", "ChunkDataS2CPacket", "UnloadChunkS2CPacket","TeamS2CPacket", "ChunkRenderDistanceCenterS2CPacket", "MessageHeaderS2CPacket", "LightUpdateS2CPacket", "OverlayMessageS2CPacket", "DebugSampleS2CPacket").contains(name)) LOGGER.info(name);

        if (CodeClient.location instanceof Dev dev) {
            try {
                if (packet instanceof ClientboundBlockEntityDataPacket beu && dev.isInDev(beu.getPos()) && beu.getType() == BlockEntityType.SIGN) {
                    CompoundTag compound = beu.getTag();
                    if(compound.contains("front_text")) {
                        SignText text = SignText.DIRECT_CODEC.decode(NbtOps.INSTANCE, beu.getTag().get("front_text")).getOrThrow().getFirst();
                        if (Plot.lineStarterPattern.matcher(text.getMessage(0, false).getString()).matches()) {
                            dev.getLineStartCache().put(beu.getPos(), text);
                        }
                    } else {
                        dev.clearLineStarterCache();
                    }
                }
            } catch (ConcurrentModificationException exception) {
                // Not sure how this comes to happen. My guess it's the getBlockEntity call.
                // Unfortunately, I don't know what state the game has to be in to make it fail, maybe an unloaded chunk?
                // It's hard to check for that, apparently.
                dev.clearLineStarterCache();
            } catch (IllegalStateException exception) {
                dev.clearLineStarterCache();
            }

            if (packet instanceof ClientboundSectionBlocksUpdatePacket update) {
                update.runUpdates((blockPos, blockState) -> dev.getLineStartCache().remove(blockPos));
            }

            if (dev.getSize() == null) {
                if (packet instanceof ClientboundPlayerPositionPacket pos) {
                    var tp = BlockPos.containing(pos.change().position().x, pos.change().position().y, pos.change().position().z);
                    if (dev.isInPlot(tp, Plot.Size.MEGA) && !dev.isInPlot(tp, Plot.Size.MASSIVE)) {
                        dev.setSize(Plot.Size.MEGA);

                    }
                }
            }
        }
        return (MC.screen instanceof PauseScreen || MC.screen instanceof ChatScreen || MC.screen instanceof StateSwitcher) && packet instanceof ClientboundContainerClosePacket;
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
//        if (!name.equals("ClientTickEndC2SPacket")) LOGGER.info(name);
        return false;
    }

    public static void onTick() {
        currentAction.tick();
        features().forEach(Feature::tick);
        KeyBinds.tick();

//        System.out.println(location.name());

        if (!(location instanceof Dev) || !(MC.screen instanceof AbstractContainerScreen<?>)) {
            isCodeChest = false;
            features().forEach(Feature::closeChest);
        }

        if (location instanceof Dev dev) {
            if (MC.player == null) return;
            var pos = new BlockPos(dev.getX() - 1, 49, dev.getZ());
            if (dev.getSize() == null) {
                // TODO wait for plugin messages, or make a fix now.
                var world = CodeClient.MC.level;
                if (world == null) return;
                var FIFTY = world.getBlockState(pos.south(50));
                var FIFTY_ONE = world.getBlockState(pos.south(51));
                var HUNDRED = world.getBlockState(pos.south(100));
                var HUNDRED_ONE = world.getBlockState(pos.south(101));
                var THREE_HUNDRED = world.getBlockState(pos.south(300));
                var THREE_HUNDRED_ONE = world.getBlockState(pos.south(301));
                var MEGA = world.getBlockState(pos.offset(-19, 0, 10));
                var MEGA_ONE = world.getBlockState(pos.offset(-20, 0, 10));
                if (MEGA_ONE.is(Blocks.GRASS_BLOCK) && MEGA.is(Blocks.GRASS_BLOCK)) {
                    dev.setSize(Plot.Size.MEGA);
                } else if (!MEGA.is(Blocks.VOID_AIR) && !MEGA_ONE.is(Blocks.VOID_AIR) && !MEGA.is(Blocks.GRASS_BLOCK) && !MEGA.is(Blocks.STONE) && !MEGA_ONE.is(Blocks.GRASS_BLOCK)) {
                    dev.setSize(Plot.Size.MEGA);
                } else if (!(FIFTY.is(Blocks.VOID_AIR) || FIFTY_ONE.is(Blocks.VOID_AIR)) && (!FIFTY.is(FIFTY_ONE.getBlock()))) {
                    dev.setSize(Plot.Size.BASIC);
                } else if (!(HUNDRED.is(Blocks.VOID_AIR) || HUNDRED_ONE.is(Blocks.VOID_AIR)) && !HUNDRED.is(HUNDRED_ONE.getBlock())) {
                    dev.setSize(Plot.Size.LARGE);
                } else if (!(THREE_HUNDRED.is(Blocks.VOID_AIR) || THREE_HUNDRED_ONE.is(Blocks.VOID_AIR)) && !THREE_HUNDRED.is(THREE_HUNDRED_ONE.getBlock())) {
                    dev.setSize(Plot.Size.MASSIVE);
                }
            }
            var size = dev.assumeSize();
            assert CodeClient.MC.level != null;
            var groundCheck = MC.level.getBlockState(new BlockPos(
                    Math.max(Math.min((int) MC.player.getX(), dev.getX() - 1), dev.getX() - (size.codeWidth)),
                    49,
                    Math.max(Math.min((int) MC.player.getZ(), dev.getZ() + size.codeLength), dev.getZ())
            ));
            if (!groundCheck.is(Blocks.VOID_AIR))
                dev.setHasUnderground(!groundCheck.is(Blocks.GRASS_BLOCK) && !groundCheck.is(Blocks.STONE));
        }
        if (CodeClient.location instanceof Spawn spawn && MC.getConnection() != null && spawn.consumeHasJustJoined()) {
            if (autoJoin == AutoJoin.PLOT) {
                MC.getConnection().sendCommand("join " + Config.getConfig().AutoJoinPlotId);
                autoJoin = AutoJoin.NONE;
            } else if (Config.getConfig().AutoFly) {
                MC.getConnection().sendCommand("fly");
            }
        }
    }

    public static void onRender(PoseStack matrices, MultiBufferSource.BufferSource vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        features().forEach(feature -> feature.render(matrices, vertexConsumers, cameraX, cameraY, cameraZ));

        if (shouldReload) {
            MC.levelRenderer.allChanged();
            shouldReload = false;
        }
    }

    public static void onClickChest(BlockHitResult hitResult) {
        features().forEach(feature -> feature.onClickChest(hitResult));
    }

    public static void onBreakBlock(Dev dev, BlockPos pos, BlockPos breakPos) {
        features().forEach(feature -> feature.onBreakBlock(dev, pos, breakPos));
    }

    public static void onScreenInit(AbstractContainerScreen<?> screen) {
        features().forEach(Feature::closeChest);
        if (!isCodeChest) return;
        features().forEach(feat -> feat.openChest(screen));
    }

    public static void onScreenClosed() {
        isCodeChest = false;
        features().forEach(Feature::closeChest);
    }

    public static void onRender(GuiGraphics context, int mouseX, int mouseY, int x, int y, float delta) {
        chestFeatures().forEach(feat -> feat.render(context, mouseX, mouseY, x, y, delta));
    }

    public static void onDrawSlot(GuiGraphics context, Slot slot) {
        chestFeatures().forEach(feat -> feat.drawSlot(context, slot));
    }

    public static ItemStack onGetHoverStack(Slot instance) {
        return chestFeatures().map(feat -> feat.getHoverStack(instance)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public static boolean onMouseClicked(MouseButtonEvent click) {
        return chestFeatures().anyMatch(feature -> feature.mouseClicked(click));
    }

    public static boolean onKeyPressed(KeyEvent key) {
        return chestFeatures().anyMatch(feature -> feature.keyPressed(key));
    }

    public static boolean onKeyReleased(KeyEvent key) {
        return chestFeatures().anyMatch(feature -> feature.keyReleased(key));
    }

    public static boolean onCharTyped(CharacterEvent charInput) {
        return chestFeatures().anyMatch(feature -> feature.charTyped(charInput));
    }

    public static boolean onClickSlot(Slot slot, int button, ClickType actionType, int syncId, int revision) {
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
        return MC.player.getAbilities().instabuild;
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
        if (location instanceof Dev dev) {
            dev.clearLineStarterCache();
        }
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
    private boolean registerResourcePack(String id, Component name) throws NullPointerException {
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
    private boolean registerResourcePack(String id, Component name, ResourcePackActivationType type) throws NullPointerException {
        var prefix = String.format("[%s] ", MOD_NAME);
        return ResourceManagerHelper.registerBuiltinResourcePack(
                getId(id),
                getModContainer(),
                Component.literal(prefix).withStyle(ChatFormatting.GRAY).append(name),
                type
        );
    }

    public static void parseVersionInfo(String response) {
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();

        var versionNumber = json.get("version_number").getAsString();

        if (Config.getConfig().AutoUpdateOption != Config.AutoUpdate.UPDATE) {
            startupToast = new Utility.Toast(Component.translatable("toast.codeclient.update_available.title", versionNumber), Component.translatable("toast.codeclient.update_available"));
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
                            startupToast = new Utility.Toast(Component.translatable("toast.codeclient.update.title", versionNumber), Component.translatable("toast.codeclient.update"));
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
