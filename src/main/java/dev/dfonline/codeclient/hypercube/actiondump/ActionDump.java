package dev.dfonline.codeclient.hypercube.actiondump;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.FileManager;
import net.minecraft.block.entity.SignText;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class ActionDump {
    private static ActionDump instance = null;

    public CodeBlock[] codeblocks;
    public Action[] actions;
    public GameValue[] gameValues;
    public Sound[] sounds;
    public Particle[] particles;
    public Potion[] potions;

    @Nullable
    public CodeBlock getCodeBlock(String name, boolean useIdentifier) {
        try {
            for (CodeBlock codeBlock : ActionDump.getActionDump().codeblocks) {
                if (name.equals(useIdentifier ? codeBlock.identifier : codeBlock.name)) return codeBlock;
            }
        } catch (IOException exception) {
            CodeClient.LOGGER.error(exception.toString());
        }
        return null;
    }

    @Nullable
    public Action findAction(SignText text) {
        return findAction(text.getMessage(0,false).getString(),text.getMessage(1,false).getString());
    }

    public Action findAction(String codeBlockName, String actionName) {
        for (var action: actions) {
            if(action.codeblockName.equals(codeBlockName) && action.name.equals(actionName)) return action;
        }
        return null;
    }

    public static ActionDump getActionDump() throws IOException {
        if (instance == null) {
            instance = CodeClient.gson.fromJson(FileManager.readFile("actiondump.json"), ActionDump.class);
        }
        return instance;
    }

    public static void clear() {
        instance = null;
    }
}
