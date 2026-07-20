package dev.dfonline.codeclient.switcher;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Plot;
import dev.dfonline.codeclient.location.Spawn;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class SpeedSwitcher extends GenericSwitcher {
    private static int lastSpeed = 3;

    public SpeedSwitcher() {
        super(Component.translatable("codeclient.switcher.speed"), GLFW.GLFW_KEY_F3, GLFW.GLFW_KEY_F5);
    }

    public static class SpeedSwitcherFeature extends Feature {
        @Override
        public boolean enabled() {
            return Config.getConfig().SpeedSwitcher;
        }

        public boolean open() {
            if (CodeClient.location instanceof Plot || CodeClient.location instanceof Spawn) {
                CodeClient.MC.gui.setScreen(new SpeedSwitcher());
                return true;
            }
            return false;
        }
    }

    @Override
    protected void init() {
        footer = Component.translatable("codeclient.switcher.footer.next", Component.translatable("codeclient.switcher.footer.brackets", "F5").withStyle(ChatFormatting.AQUA));
        selected = 0;
        // 0.05 is 100% on df. 1000% is 0.5.
        if (CodeClient.MC.player.getAbilities().getFlyingSpeed() == 0.05f) {
            selected = lastSpeed;
        }
        super.init();
    }

    @Override
    List<Option> getOptions() {
        return List.of(
                new Option(Component.nullToEmpty("100%"), Items.FEATHER.getDefaultInstance(), () -> CodeClient.MC.getConnection().sendCommand("fs 100")),
                new Option(Component.nullToEmpty("200%"), Items.FEATHER.getDefaultInstance().copyWithCount(2), () -> {
                    CodeClient.MC.getConnection().sendCommand("fs 200");
                    lastSpeed = 1;
                }),
                new Option(Component.nullToEmpty("500%"), Items.FEATHER.getDefaultInstance().copyWithCount(5), () -> {
                    CodeClient.MC.getConnection().sendCommand("fs 500");
                    lastSpeed = 2;
                }),
                new Option(Component.nullToEmpty("1000%"), Items.FEATHER.getDefaultInstance().copyWithCount(10), () -> {
                    CodeClient.MC.getConnection().sendCommand("fs 1000");
                    lastSpeed = 3;
                })
        );
    }
}
