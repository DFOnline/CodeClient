package dev.dfonline.codeclient;

import com.google.gson.JsonObject;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

public class Utility {
    /**
     * Ensure the player is holding an item, by holding and setting the first slot.
     * @param template Any item
     */
    public static void makeHolding(ItemStack template) {
        PlayerInventory inv = CodeClient.MC.player.getInventory();
        CodeClient.MC.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(36, template));
        inv.selectedSlot = 0;
        inv.setStack(0, template);
    }

    /**
     * Gets all templates in the players inventory.
     */
    public static List<ItemStack> TemplatesInInventory() {
        PlayerInventory inv = CodeClient.MC.player.getInventory();
        ArrayList<ItemStack> templates = new ArrayList<>();
        for (int i = 0; i < (27 + 9); i++) {
            ItemStack item = inv.getStack(i);
            if (!item.hasNbt()) continue;
            NbtCompound nbt = item.getNbt();
            if (!nbt.contains("PublicBukkitValues")) continue;
            NbtCompound publicBukkit = nbt.getCompound("PublicBukkitValues");
            if (!publicBukkit.contains("hypercube:codetemplatedata")) continue;
            templates.add(item);
        }
        return templates;
    }

    public static String compileTemplate(JsonObject data) throws IOException {
        return compileTemplate(data.getAsString());
    }

    /**
     * GZIPs and base64's data for use in templates.
     * @throws IOException If an I/O error happened with gzip
     */
    public static String compileTemplate(String data) throws IOException {
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(data.getBytes());
        gzip.close();

        return new String(Base64.getEncoder().encode(obj.toByteArray()));
    }

    public static void sendMessage(String message, ChatType type) {
        sendMessage(Text.of(message), type);
    }
    public static void sendMessage(String message) {
        sendMessage(Text.of(message), ChatType.INFO);
    }
    public static void sendMessage(Text message) {
        sendMessage(message, ChatType.INFO);
    }

    public static void sendMessage(Text message, @Nullable ChatType type) {
        ClientPlayerEntity player = CodeClient.MC.player;
        if (player == null) return;
        if (type == null) {
            player.sendMessage(message, false);
        } else {
            player.sendMessage(Text.literal(type.getString() + " ").append(message).setStyle(Style.EMPTY.withColor(type.getTrailing())), false);
            if (type == ChatType.FAIL) {
                player.playSound(SoundEvent.of(new Identifier("minecraft:block.note_block.didgeridoo")), SoundCategory.PLAYERS, 2, 0);
            }
        }
    }

    /**
     * Prepares a text object for use in an item's display tag
     * @return Usable in lore and as a name in nbt.
     */
    public static NbtString nbtify(Text text) {
        JsonObject json = Text.Serializer.toJsonTree(text).getAsJsonObject();

        if(!json.has("color")) json.addProperty("color","white");
        if(!json.has("italic")) json.addProperty("italic",false);
        if(!json.has("bold")) json.addProperty("bold",false);

        return NbtString.of(json.toString());
    }

    /**
     * Parses § formatted strings.
     * @param text § formatted string.
     * @return Text with all parsed text as siblings.
     */
    public static MutableText textFromString(String text) {
        MutableText output = Text.empty().setStyle(Text.empty().getStyle().withColor(TextColor.fromRgb(0xFFFFFF)).withItalic(false));
        MutableText component = Text.empty();

        Matcher m = Pattern.compile("§(([0-9a-kfmnolr])|x(§[0-9a-f]){6})|[^§]+").matcher(text);
        while (m.find()) {
            String data = m.group();
            if(data.startsWith("§")) {
                if(data.startsWith("§x")) {
                    component = component.setStyle(component.getStyle().withColor(Integer.valueOf(data.replaceAll("§x|§",""), 16)));
                }
                else {
                    component = component.formatted(Formatting.byCode(data.charAt(1)));
                }
            }
            else {
                component.append(data);
                output.append(component);
                component = Text.empty().setStyle(component.getStyle());
            }
        }
        return output;
    }
}

