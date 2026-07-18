package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.hypercube.item.*;
import dev.dfonline.codeclient.location.Dev;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import net.minecraft.client.gui.components.EditBox;

public class ChatAutoEdit extends Feature {
    @Override
    public boolean enabled() {
        return Config.getConfig().ChatEditsVars;
    }

    public void onOpenChat(EditBox chat) {
        var player = CodeClient.MC.player;
        if(player == null || !(CodeClient.location instanceof Dev) || !chat.getValue().isEmpty()) return;

        var item = VarItems.parse(player.getMainHandItem());
        DecimalFormat format = new DecimalFormat("0.#");
        format.setMinimumFractionDigits(0);

        if (item instanceof BucketVar bvar) {
            chat.setValue("%s %s".formatted(bvar.getKey(), bvar.getName()));
            chat.moveCursorTo(bvar.getKey().length() + 1, false);
            chat.moveCursorToEnd(true);
            return;
        } else if (item instanceof NamedItem named) {
            chat.setValue(named.getName().replaceAll("§", "&"));
        } else if (item instanceof Vector vec) {
            chat.setValue("%s %s %s".formatted(vec.getX(), vec.getY(), vec.getZ()));
        } else if (item instanceof Sound sound) {
            var note = sound.getNote();
            var pitch = sound.getPitch();
            @NotNull var display = note == null || pitch == 0.5 || pitch == 1 || pitch == 2 ? format.format(pitch) : note;
            if (sound.getVolume() != 2)
                chat.setValue("%s %s".formatted(sound.getPitch(), display));
            else chat.setValue(display);
        } else if (item instanceof Potion pot) {
            if(pot.getDuration() == Potion.INFINITE) chat.setValue(Integer.toString(pot.getAmplifier() + 1));
            else chat.setValue("%o %s".formatted(pot.getAmplifier() + 1, pot.duration().replaceAll(" ticks","")));
        }
        else return;
        chat.moveCursorToStart(false);
        chat.moveCursorToEnd(true);
    }
}
