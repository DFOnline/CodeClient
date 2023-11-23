package dev.dfonline.codeclient.hypercube.template;

import org.jetbrains.annotations.Nullable;

public class TemplateBlock {
    /**
     * block | bracket
     */
    public String id;
    @Nullable public String block;
    @Nullable public String data;
    @Nullable public String action;

    public enum Block {
        CALL_FUNC("CALL FUNCTION"),
        CONTROL("CONTROL"),
        ELSE("ELSE"),
        ENTITY_ACTION("ENTITY ACTION"),
        ENTITY_EVENT("ENTITY EVENT"),
        EVENT("PLAYER EVENT"),
        FUNC("FUNCTION"),
        GAME_ACTION("GAME ACTION"),
        IF_ENTITY("IF PLAYER"),
        IF_GAME("IF GAME"),
        IF_PLAYER("IF PLAYER"),
        IF_VAR("IF VARIABLE"),
        PLAYER_ACTION("PLAYER ACTION"),
        PROCESS("PROCESS"),
        REPEAT("REPEAT"),
        SELECT_OBJ("SELECT OBJECT"),
        SET_VAR("SET VARIABLE"),
        START_PROCESS("START PROCESS");

        public final String name; // TODO: check I got all these names right.

        Block(String name) {
            this.name = name;
        }
    }
}
