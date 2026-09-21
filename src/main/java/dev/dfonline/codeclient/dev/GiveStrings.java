package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Dev;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

public class GiveStrings extends Feature {

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if (packet instanceof ClientboundSystemChatPacket message) {
            Component content = message.content();
            String string = content.getString();

            if (CodeClient.location instanceof Dev) {
                if (Config.getConfig().GiveUuidNameStrings) {
                    Pattern pattern = Pattern.compile("^» The (UUID|name) of .*? is ([^ ]+)\\. \\(click to copy\\)$");
                    Matcher matcher = pattern.matcher(string);

                    if(matcher.matches())
                        CodeClient.MC.getConnection().sendCommand("str " + matcher.group(2));
                }
            }
        }

        return false;
    }

}
