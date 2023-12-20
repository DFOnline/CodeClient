package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockTag extends VarItem {
    /**
     * Selected option.
     */
    @NotNull
    private String option;
    /**
     * The name of the given tag.
     */
    @NotNull
    private String tag;
    /**
     * The action it is on.
     */
    @NotNull
    private String action;
    /**
     * ID, like `event` or `player_action`
     */
    @NotNull
    private String block;
    /**
     * The active var tag.
     */
    @Nullable
    private Variable variable;

    private static JsonObject makeVar(String option, String tag, String action, String block) {
        var var = new JsonObject();
        var.addProperty("id","bl_tag");
        var data = new JsonObject();
        data.addProperty("option",option);
        data.addProperty("tag",tag);
        data.addProperty("action",action);
        data.addProperty("block",block);
        var.add("data",data);
        return var;
    }

    public BlockTag(String material, String option, String tag, String action, String block) {
        this(Registries.ITEM.get(new Identifier("minecraft",material.toLowerCase())),makeVar(option, tag, action, block));
    }

    public BlockTag(Item material, JsonObject var) {
        super(material, var);
        option = this.data.get("option").getAsString();
        tag = this.data.get("tag").getAsString();
        action = this.data.get("action").getAsString();
        block = this.data.get("block").getAsString();
        if(this.data.has("variable")) variable = new Variable(Items.MAGMA_CREAM,this.data.get("variable").getAsJsonObject());
    }

    public @NotNull String getOption() {
        return this.option;
    }
    public void setOption(@NotNull String option) {
        this.option = option;
        this.data.addProperty("option",option);
    }
    public @NotNull String getTag() {
        return this.tag;
    }
    public void setTag(@NotNull String tag) {
        this.tag = tag;
        this.data.addProperty("tag",tag);
    }
    public @NotNull String getAction() {
        return this.action;
    }
    public void setAction(@NotNull String action) {
        this.action = action;
        this.data.addProperty("action",action);
    }
    public @NotNull String getBlock() {
        return this.block;
    }
    public void setBlock(@NotNull String block) {
        this.block = block;
        this.data.addProperty("block",block);
    }

    public @Nullable Variable getVariable() {
        return variable;
    }

    public void setVariable(@Nullable Variable variable) {
        this.variable = variable;
        this.data.add("variable",variable.getData());
    }
}
