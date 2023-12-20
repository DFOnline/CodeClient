package dev.dfonline.codeclient.hypercube.actiondump;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.hypercube.template.Bracket;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Action implements Searchable {
    public String name;
    public String codeblockName;
    public String[] aliases;
    public Icon icon;
    public String[] subActionBlocks;

    public CodeBlock getCodeBlock() {
        try {
            for (CodeBlock codeBlock: ActionDump.getActionDump().codeblocks) {
                if(codeblockName.equals(codeBlock.name)) return codeBlock;
            }
        }
        catch (IOException exception) {
            CodeClient.LOGGER.error(exception.toString());
        }
        return null;
    }

    @Override
    public List<String> getTerms() {
        ArrayList<String> terms = new ArrayList<>(Arrays.stream(aliases).toList());
        terms.add(name);
        terms.add(icon.name.replace("§.",""));
        return terms;
    }

    @Override
    public ItemStack getItem() {
        ItemStack item = icon.getItem();
        NbtCompound nbt = item.getNbt();
        assert nbt != null;
        NbtCompound PublicBukkitValues = new NbtCompound();
        JsonObject CodeTemplateData = new JsonObject();
        CodeTemplateData.addProperty("author", CodeClient.MC.getSession().getUsername());
        CodeTemplateData.addProperty("name", icon.name);
        CodeTemplateData.addProperty("version", 1);

        CodeBlock codeBlock = getCodeBlock();

        JsonObject template = new JsonObject();
        JsonArray blocks = new JsonArray();
        // TODO: brackets for IFs
        boolean repeat = codeBlock.identifier.equals("repeat");
        boolean hasBrackets = codeBlock.identifier.contains("if") || repeat || codeBlock.identifier.equals("else");
        JsonObject action = new JsonObject();
        action.addProperty("id","block");
        action.addProperty("block", codeBlock.identifier);
        action.addProperty("action", name);
        JsonObject args = new JsonObject();
        args.add("items",new JsonArray());
        action.add("args",args);
        blocks.add(action);
        if (hasBrackets) {
            blocks.add(new Bracket(false, repeat).toJsonObject());
            blocks.add(new Bracket(true, repeat).toJsonObject());
        }
        template.add("blocks",blocks);
        try {
            CodeTemplateData.addProperty("code", Utility.compileTemplate(template.toString()));
        } catch (Exception ignored) {}

        PublicBukkitValues.put("hypercube:codetemplatedata", NbtString.of(String.valueOf(CodeTemplateData)));
        nbt.put("PublicBukkitValues",PublicBukkitValues);
        item.setNbt(nbt);
        return item;
    }

    public boolean isInvalid() {
        return this.icon.name.equals("");
    }
}
