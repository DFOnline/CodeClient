package dev.dfonline.codeclient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.dfonline.codeclient.action.impl.GetActionDump;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.hypercube.template.Template;
import net.kyori.adventure.platform.modcommon.impl.NonWrappingComponentSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import net.kyori.adventure.text.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

public class Utility {

    /***
     * A toast message to be used when creating a SystemToast directly is not possible, such as before the game starts.
     * @param title       The title of the toast.
     * @param description The description of the toast.
     */
    public record Toast(net.minecraft.network.chat.Component title, net.minecraft.network.chat.Component description) {

    }

    /**
     * Get the slot id to be used with a creative packet, from a local slot id.
     */
    public static int getRemoteSlot(int slot) {
        if (0 <= slot && slot <= 8) { // this is for the hotbar, which is after the inventory in packets.
            return slot + 36;
        } else return slot;
    }

    /**
     * Be lazy, send your whole inventory!
     */
    public static void sendInventory() {
        if(CodeClient.MC.getConnection() == null || CodeClient.MC.player == null) return;
        for (int i = 0; i <= 35; i++) {
            CodeClient.MC.getConnection().send(new ServerboundSetCreativeModeSlotPacket(getRemoteSlot(i), CodeClient.MC.player.getInventory().getItem(i)));
        }
    }

    /**
     * Ensure the player is holding an item, by holding and setting the first slot.
     *
     * @param item Any item
     */
    public static void makeHolding(ItemStack item) {
        if(CodeClient.MC.player == null) return;
        Inventory inv = CodeClient.MC.player.getInventory();
        Utility.sendHandItem(item);
        inv.setSelectedSlot(0);
        inv.setItem(0, item);
    }

    @SuppressWarnings("unused")
    public static void debug(Object object) {
        debug(Objects.toString(object));
    }

    public static void debug(String message) {
        CodeClient.LOGGER.info("%%% DEBUG: {}", message);
    }

    /**
     * Gets the base64 template data from an item. Null if there is none.
     */
    public static String templateDataItem(ItemStack item) {
        DFItem dfItem = DFItem.of(item);
        Optional<String> codeTemplateData = dfItem.getHypercubeStringValue("codetemplatedata");
        return codeTemplateData.map(s -> JsonParser.parseString(s).getAsJsonObject().get("code").getAsString()).orElse(null);
    }

    public static ItemStack makeTemplate(String code) {
        ItemStack template = new ItemStack(Items.ENDER_CHEST);
        DFItem dfItem = DFItem.of(template);
        dfItem.editData(data -> data.setHypercubeStringValue("codetemplatedata", "{\"author\":\"CodeClient\",\"name\":\"Template to be placed\",\"version\":1,\"code\":\"" + code + "\"}"));
        return dfItem.getItemStack();
    }

    /**
     * Get the parsed Template from an item. None is the is none.
     */
    public static Template templateItem(ItemStack item) {
        String codeTemplateData = templateDataItem(item);
        return Template.parse64(codeTemplateData);
    }

    public static void addLore(ItemStack stack, net.minecraft.network.chat.Component... lore) {
        DFItem item = DFItem.of(stack);
        List<net.minecraft.network.chat.Component> currentLore = item.getLore();
        ArrayList<net.minecraft.network.chat.Component> newLore = new ArrayList<>(currentLore);
        newLore.addAll(List.of(lore));
        item.setLore(newLore);
    }

    public static void sendHandItem(ItemStack item) {
        if(CodeClient.MC.getConnection() == null || CodeClient.MC.player == null) return;
        CodeClient.MC.getConnection().send(new ServerboundSetCreativeModeSlotPacket(36 + CodeClient.MC.player.getInventory().getSelectedSlot(), item));
    }

    /**
     * Gets all templates in the players inventory.
     */
    public static List<ItemStack> templatesInInventory() {
        if(CodeClient.MC.player == null) return null;
        Inventory inv = CodeClient.MC.player.getInventory();
        ArrayList<ItemStack> templates = new ArrayList<>();
        for (int i = 0; i < (27 + 9); i++) {
            ItemStack item = inv.getItem(i);
            DFItem dfItem = DFItem.of(item);
            if (dfItem.hasHypercubeKey("codetemplatedata")) templates.add(item);
        }
        return templates;
    }

    public static String compileTemplate(JsonObject data) throws IOException {
        return compileTemplate(data.getAsString());
    }

    /**
     * GZIPs and base64's data for use in templates.
     *
     * @throws IOException If an I/O error happened with gzip
     */
    public static String compileTemplate(String data) throws IOException {
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(data.getBytes());
        gzip.close();

        return new String(Base64.getEncoder().encode(obj.toByteArray()));
    }

    /**
     * @deprecated This uses literals, use translations when you can.
     */
    @Deprecated
    public static void sendMessage(String message, ChatType type) {
        sendMessage(net.minecraft.network.chat.Component.literal(message), type);
    }

    public static void sendMessage(net.minecraft.network.chat.Component message) {
        sendMessage(message, ChatType.INFO);
    }

    public static void sendMessage(net.minecraft.network.chat.Component message, @Nullable ChatType type) {
        LocalPlayer player = CodeClient.MC.player;
        if (player == null) return;
        if (type == null) {
            player.displayClientMessage(message, false);
        } else {
            player.displayClientMessage(net.minecraft.network.chat.Component.empty()
                    .append(type.getText())
                    .append(net.minecraft.network.chat.Component.literal(" "))
                    .append(message), false);
            if (type == ChatType.FAIL) {
                player.playSound(SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), 2, 0);
            }
        }
    }

    /**
     * Prepares a text object for use in an item's display tag
     *
     * @return Usable in lore and as a name in nbt.
     */
    public static StringTag textToNBT(net.minecraft.network.chat.Component text) {
        JsonElement json = ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, text).getOrThrow();
        if (json.isJsonObject()) {
            JsonObject obj = (JsonObject) json;

            if (!obj.has("color")) obj.addProperty("color", "white");
            if (!obj.has("italic")) obj.addProperty("italic", false);
            if (!obj.has("bold")) obj.addProperty("bold", false);

            return StringTag.valueOf(obj.toString());
        } else return StringTag.valueOf(json.toString());
    }

    /**
     * Parses § formatted strings.
     *
     * @param text § formatted string.
     * @return Text with all parsed text as siblings.
     */
    public static MutableComponent textFromString(String text) {
        MutableComponent output = net.minecraft.network.chat.Component.empty().setStyle(net.minecraft.network.chat.Component.empty().getStyle().withColor(TextColor.fromRgb(0xFFFFFF)).withItalic(false));
        MutableComponent component = net.minecraft.network.chat.Component.empty().withStyle(s -> s.withItalic(false));

        Matcher m = Pattern.compile("§(([0-9a-kfmnolr])|x(§[0-9a-f]){6})|[^§]+").matcher(text);
        while (m.find()) {
            String data = m.group();
            if (data.startsWith("§")) {
                if (data.startsWith("§x")) {
                    component = component.setStyle(component.getStyle().withColor(Integer.valueOf(data.replaceAll("§x|§", ""), 16)));
                } else {
                    component = component.withStyle(ChatFormatting.getByCode(data.charAt(1)));
                }
            } else {
                component.append(data);
                output.append(component);
                component = net.minecraft.network.chat.Component.empty().setStyle(component.getStyle());
            }
        }
        return output;
    }

    public static boolean isGlitchStick(ItemStack item) {
        DFItem dfItem = DFItem.of(item);
        if (!dfItem.hasHypercubeKey("item_instance")) return false;
        return Objects.equals(dfItem.getName(), net.minecraft.network.chat.Component.literal("Glitch Stick").setStyle(Style.EMPTY.withColor(ChatFormatting.RED).withItalic(false)));
    }

    public static HashMap<Integer, String> getBlockTagLines(ItemStack item) {
        // Not migrated to 1.21 due to not being called anywhere.
        /*NbtCompound display = item.getSubNbt("display");
        NbtList lore = (NbtList) display.get("Lore");
        if (lore == null) throw new NullPointerException("Can't get lore.");

        HashMap<Integer, String> options = new HashMap<>();

        for (int index = lore.size() - 1; index >= 0; index--) {
            NbtElement element = lore.get(index);
            Text text = Text.Serialization.fromJson(element.asString());
            var data = text.getString();
            if (data.isBlank() || data.equals("Default Value:")) {
                break;
            }
            options.put(index, data.replaceAll("» ", ""));
        }

        return options;*/
        return null;
    }

    public static void textToString(net.minecraft.network.chat.Component content, StringBuilder build, GetActionDump.ColorMode colorMode) {
        TextColor lastColor = null;
        for (net.minecraft.network.chat.Component text : content.getSiblings()) {
            TextColor color = text.getStyle().getColor();
            if (color != null && (lastColor != color) && (colorMode != GetActionDump.ColorMode.NONE)) {
                lastColor = color;
                if (color.serialize().contains("#")) {
                    build.append(String.join(colorMode.text, color.serialize().split("")).replace("#", colorMode.text + "x").toLowerCase());
                } else {
                    build.append(ChatFormatting.valueOf(String.valueOf(color).toUpperCase()).toString().replace("§", colorMode.text));
                }
            }
            build.append(text.getString());
        }
    }

    public static String textToString(net.minecraft.network.chat.Component content) {
        var builder = new StringBuilder();
        textToString(content, builder, GetActionDump.ColorMode.SECTION);
        return builder.toString();
    }

    /**
     * Generate a string of 32 random A-Z,a-z,0-9 characters that are used for authentication tokens in the API.
     * @return A random authentication token.
     */
    public static String genAuthToken() {
        SecureRandom random = new SecureRandom();
        byte[] randomBytes = new byte[32];
        random.nextBytes(randomBytes);
        return HexFormat.of().formatHex(randomBytes);
    }


    /**
     *
     * Turns trimmed UUID (without dashes) into a UUID with dashes
     * @return A UUID with dashes
     */
    public static String fromTrimmed(String trimmedUUID) {
        if (trimmedUUID == null)
            throw new IllegalArgumentException();

        StringBuilder builder = new StringBuilder(trimmedUUID.trim());
        try {
            builder.insert(20, "-");
            builder.insert(16, "-");
            builder.insert(12, "-");
            builder.insert(8, "-");
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException();
        }

        return builder.toString();
    }

    /**
     * Turns a {@link net.kyori.adventure.text.Component} to an {@link FormattedCharSequence}
     *
     * @param component The component to convert
     * @return The converted component
     */
    public static FormattedCharSequence componentToOrderedText(Component component) {
        return NonWrappingComponentSerializer.INSTANCE.serialize(component).getVisualOrderText();
    }

    /**
     * Turns a {@link net.kyori.adventure.text.Component} to a {@link net.minecraft.network.chat.Component}
     *
     * @param component The component to convert
     * @return The converted component
     */
    public static net.minecraft.network.chat.Component componentToText(Component component) {
        return NonWrappingComponentSerializer.INSTANCE.serialize(component);
    }

}
