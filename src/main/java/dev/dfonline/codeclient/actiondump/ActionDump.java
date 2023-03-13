package dev.dfonline.codeclient.actiondump;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.FileManager;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.util.List;

public class ActionDump {
    private static ActionDump instance = null;

    public CodeBlock[] codeblocks;
    public Action[] actions;
    public GameValue[] gameValues;
    public Sound[] sounds;
    public Particle[] particles;
    public Potion[] potions;


    public static ActionDump getActionDump() throws IOException {
        if(instance == null) {
            instance = CodeClient.gson.fromJson(FileManager.readFile("actiondump.json"),ActionDump.class);
        }
        return instance;
    }

    public static void clear() {
        instance = null;
    }
}
