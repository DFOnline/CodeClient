package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.hypercube.item.*;
import dev.dfonline.codeclient.hypercube.item.Number;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.Hand;
import org.apache.commons.lang3.StringUtils;

public class ChatLongValue extends Feature {
    public boolean enabled() {
        return Config.getConfig().ChatLongValue;
    }

    public void onOpenChat(TextFieldWidget chat) {
        var player = CodeClient.MC.player;
        if (player == null) return;
        var item = VarItems.parse(player.getMainHandStack());
        if (item instanceof Text || item instanceof Number || item instanceof Component) {
            chat.setMaxLength(10_000);
        }
    }

    public boolean onSendChat(String chatText) {
        var player = CodeClient.MC.player;
        if (player == null) return false;

        String normalized = StringUtils.normalizeSpace(chatText.trim());

        boolean very_long = normalized.length() > 256;
        boolean trailing_data = !normalized.equals(chatText);

        if (normalized.startsWith("/") && very_long) {
            return true;
        }

        if (VarItems.parse(player.getMainHandStack()) instanceof NamedItem named) {
            if (very_long || trailing_data) {
                named.setName(chatText);
                player.getInventory().setSelectedStack(named.toStack());
                Utility.sendHandItem(named.toStack());
                CodeClient.MC.gameRenderer.firstPersonRenderer.resetEquipProgress(Hand.MAIN_HAND);
                return true;
            }
            return false;
        } else {
            return very_long;
        }
    }
}
