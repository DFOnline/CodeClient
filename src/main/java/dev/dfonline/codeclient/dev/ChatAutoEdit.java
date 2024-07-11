package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.hypercube.item.*;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public class ChatAutoEdit extends Feature {
    @Override
    public boolean enabled() {
        return Config.getConfig().ChatEditsVars;
    }

    public void onOpenChat(TextFieldWidget chat) {
        var player = CodeClient.MC.player;
        if(player == null || !(CodeClient.location instanceof Dev) || !chat.getText().isEmpty()) return;

        var item = VarItems.parse(player.getMainHandStack());
        DecimalFormat format = new DecimalFormat("0.#");
        format.setMinimumFractionDigits(0);

        if (item instanceof NamedItem named) {
            chat.setText(named.getName().replaceAll("§", "&"));
        } else if (item instanceof Vector vec) {
            chat.setText("%s %s %s".formatted(vec.getX(), vec.getY(), vec.getZ()));
        } else if (item instanceof Sound sound) {
            var note = sound.getNote();
            var pitch = sound.getPitch();
            @NotNull var display = note == null || pitch == 0.5 || pitch == 1 || pitch == 2 ? format.format(pitch) : note;
            if (sound.getVolume() != 2)
                chat.setText("%s %s".formatted(sound.getPitch(), display));
            else chat.setText(display);
        } else if (item instanceof Potion pot) {
            if(pot.getDuration() == Potion.INFINITE) chat.setText(Integer.toString(pot.getAmplifier() + 1));
            else chat.setText("%o %s".formatted(pot.getAmplifier() + 1, pot.duration().replaceAll(" ticks","")));
        }
        else return;
        chat.setCursorToStart(false);
        chat.setCursorToEnd(true);
    }
}
