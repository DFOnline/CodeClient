package dev.dfonline.codeclient.command;

import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.action.None;

public abstract class ActionCommand extends Command {
    protected void actionCallback() {
        CodeClient.currentAction = new None();
        Utility.sendMessage(DONE, ChatType.SUCCESS);
    }
}
