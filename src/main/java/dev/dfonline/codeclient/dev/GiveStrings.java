package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GiveStrings extends Feature {

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if (packet instanceof GameMessageS2CPacket message) {
            Text content = message.content();
            String string = content.getString();

            if (CodeClient.location instanceof Dev) {
                if (Config.getConfig().GiveUuidNameStrings) {
                    Pattern pattern = Pattern.compile("^» The (UUID|name) of .*? is ([^ ]+)\\. \\(click to copy\\)$");
                    Matcher matcher = pattern.matcher(string);

                    if(matcher.matches())
                        CodeClient.MC.getNetworkHandler().sendChatCommand("str " + matcher.group(2));
                }
            }
        }

        return false;
    }

}
