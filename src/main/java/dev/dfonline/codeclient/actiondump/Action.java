package dev.dfonline.codeclient.actiondump;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;

public class Action {
    public String name;
    public String codeblockName;
    public String[] aliases;
    public Item icon;
    public String[] subActionBlocks;

    public CodeBlock getCodeBlock() {
        try {
            for (CodeBlock codeBlock: ActionDump.getActionDump().codeblocks) {
                if(codeblockName.equals(codeBlock.name)) return codeBlock;
            }
        }
        catch (Exception ignored) {}
        return null;
    }

    public ItemStack getItem() {
        ItemStack item = icon.getItem();
        NbtCompound nbt = item.getNbt();
        assert nbt != null;
        NbtCompound PublicBukkitValues = new NbtCompound();
        JsonObject codetemplatedata = new JsonObject();
        codetemplatedata.addProperty("author", CodeClient.MC.getSession().getUsername());
        codetemplatedata.addProperty("name", icon.name);
        codetemplatedata.addProperty("version", 1);

        JsonObject template = new JsonObject();
        JsonArray blocks = new JsonArray();
        JsonObject action = new JsonObject();
        action.addProperty("id","block");
        action.addProperty("block", getCodeBlock().identifier);
        action.addProperty("action", name);
        JsonObject args = new JsonObject();
        args.add("items",new JsonArray());
        action.add("args",args);
        blocks.add(action);
        template.add("blocks",blocks);
        try {
            codetemplatedata.addProperty("code", Utility.compileTempate(template.toString()));
        } catch (Exception ignored) {}

        PublicBukkitValues.put("hypercube:codetemplatedata", NbtString.of(String.valueOf(codetemplatedata)));
        nbt.put("PublicBukkitValues",PublicBukkitValues);
        item.setNbt(nbt);
        return item;
    }
}
