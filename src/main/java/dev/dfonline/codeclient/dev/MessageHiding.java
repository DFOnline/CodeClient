package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;

import java.util.regex.Pattern;

public class MessageHiding extends Feature {

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if (packet instanceof GameMessageS2CPacket message) {
            Text content = message.content();
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
