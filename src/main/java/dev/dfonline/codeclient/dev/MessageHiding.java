package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Dev;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

public class MessageHiding extends Feature {

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if (packet instanceof ClientboundSystemChatPacket message) {
            Component content = message.content();
            String string = content.getString();

            // Nested for future dev-only features.
            if (CodeClient.location instanceof Dev) {
                if (Config.getConfig().HideScopeChangeMessages) {
                    Pattern pattern = Pattern.compile("^Scope set to (GAME|SAVE|LOCAL|LINE) \\((\\w ?)+\\)\\.$");
                    return pattern.matcher(string).matches();
                }
            }
        }

        return false;
    }

}
