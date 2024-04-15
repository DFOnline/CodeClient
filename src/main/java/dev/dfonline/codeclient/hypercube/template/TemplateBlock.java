package dev.dfonline.codeclient.hypercube.template;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TemplateBlock {
    /**
     * "block" | "bracket"
     */
    public String id;
    /**
     * "open" | "close"
     */
    @Nullable
    public String direct;
    /**
     * "norm" | "repeat"
     */
    @Nullable
    public String type;
    @Nullable
    public String block;
    @Nullable
    public String data;
    @Nullable
    public String action;

    public int getLength() {
        return id.equals("block")
                ? Block.valueOf(block.toUpperCase()).hasStone ? 2 : 1
                : Objects.equals(direct, "open") ? 1 : 2;
    }

    public JsonObject toJsonObject() {
        var obj = new JsonObject();
        obj.addProperty("id", id);
        return obj;
    }

    public enum Block {
        CALL_FUNC("CALL FUNCTION", true),
        CONTROL("CONTROL", true),
        ELSE("ELSE", false),
        ENTITY_ACTION("ENTITY ACTION", true),
        ENTITY_EVENT("ENTITY EVENT", true),
        EVENT("PLAYER EVENT", true),
        FUNC("FUNCTION", true),
        GAME_ACTION("GAME ACTION", true),
        IF_ENTITY("IF PLAYER", false),
        IF_GAME("IF GAME", false),
        IF_PLAYER("IF PLAYER", false),
        IF_VAR("IF VARIABLE", false),
        PLAYER_ACTION("PLAYER ACTION", true),
        PROCESS("PROCESS", true),
        REPEAT("REPEAT", false),
        SELECT_OBJ("SELECT OBJECT", true),
        SET_VAR("SET VARIABLE", true),
        START_PROCESS("START PROCESS", true);

        public final String name; // TODO: check I got all these names right.
        public final boolean hasStone;

        Block(String name, boolean hasStone) {
            this.name = name;
            this.hasStone = hasStone;
        }

    }
}
