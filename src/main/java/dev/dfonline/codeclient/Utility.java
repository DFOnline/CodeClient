package dev.dfonline.codeclient;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;

public class Utility {
    public static void makeHolding(ItemStack template) {
        PlayerInventory inv = CodeClient.MC.player.getInventory();
        CodeClient.MC.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(36, template));
        inv.selectedSlot = 0;
        inv.setStack(0, template);
    }

    public static ArrayList<ItemStack> TemplatesInInventory() {
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
        return compileTempate(data.getAsString());
    }
    public static String compileTempate(String data) throws IOException {
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(data.getBytes());
        gzip.close();

        return Base64.getEncoder().encode(obj.toByteArray()).toString();
    }
}
