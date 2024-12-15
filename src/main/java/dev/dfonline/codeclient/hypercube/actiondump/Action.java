package dev.dfonline.codeclient.hypercube.actiondump;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.hypercube.item.BlockTag;
import dev.dfonline.codeclient.hypercube.template.Argument;
import dev.dfonline.codeclient.hypercube.template.Bracket;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Action implements Searchable {
    public String name;
    public String codeblockName;
    public Tag[] tags;
    public String[] aliases;
    public Icon icon;
    public String[] subActionBlocks;

    @Override
    public List<String> getTerms() {
        ArrayList<String> terms = new ArrayList<>(Arrays.stream(aliases).toList());
        terms.add(name);
        terms.add(icon.name.replace("§.", ""));
        return terms;
    }

    @Nullable
    public CodeBlock getCodeBlock() {
        try {
            return ActionDump.getActionDump().getCodeBlock(codeblockName,false);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public ItemStack getItem() {
        ItemStack item = icon.getItem();
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
        action.addProperty("id", "block");
        action.addProperty("block", codeBlock.identifier);
        action.addProperty("action", name);
        JsonObject args = new JsonObject();
        var items = new JsonArray();
        for (var tag : this.tags) {
            var defaultOption = Arrays.stream(tag.options).filter(tagOption -> tagOption.name.equals(tag.defaultOption)).findFirst();
            if (defaultOption.isPresent()) {
                var blockTag = new BlockTag(tag.defaultOption, tag.name, name, getCodeBlock().identifier);
                items.add(new Argument(blockTag, tag.slot).toJsonObject());
            }
        }
        args.add("items", items);
        action.add("args", args);
        blocks.add(action);
        if (hasBrackets) {
            blocks.add(new Bracket(false, repeat).toJsonObject());
            blocks.add(new Bracket(true, repeat).toJsonObject());
        }
        template.add("blocks", blocks);
        try {
            CodeTemplateData.addProperty("code", Utility.compileTemplate(template.toString()));
        } catch (Exception ignored) {
        }

        DFItem dfItem = DFItem.of(item);
        dfItem.getItemData().setHypercubeStringValue("codetemplatedata", String.valueOf(CodeTemplateData));
        return dfItem.getItemStack();
    }

    public boolean isInvalid() {
        return this.icon.name.isEmpty();
    }
}
