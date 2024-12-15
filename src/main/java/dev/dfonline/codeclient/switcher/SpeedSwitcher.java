package dev.dfonline.codeclient.switcher;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Plot;
import dev.dfonline.codeclient.location.Spawn;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class SpeedSwitcher extends GenericSwitcher {
    private static int lastSpeed = 3;

    public SpeedSwitcher() {
        super(Text.translatable("codeclient.switcher.speed"), GLFW.GLFW_KEY_F3, GLFW.GLFW_KEY_F5);
    }

    public static class SpeedSwitcherFeature extends Feature {
        @Override
        public boolean enabled() {
            return Config.getConfig().SpeedSwitcher;
        }

        public boolean open() {
            if (CodeClient.location instanceof Plot || CodeClient.location instanceof Spawn) {
                CodeClient.MC.setScreen(new SpeedSwitcher());
                return true;
            }
            return false;
        }
    }

    @Override
    protected void init() {
        footer = Text.translatable("codeclient.switcher.footer.next", Text.translatable("codeclient.switcher.footer.brackets", "F5").formatted(Formatting.AQUA));
        selected = 0;
        // 0.05 is 100% on df. 1000% is 0.5.
        if (CodeClient.MC.player.getAbilities().getFlySpeed() == 0.05f) {
            selected = lastSpeed;
        }
        super.init();
    }

    @Override
    List<Option> getOptions() {
        return List.of(
                new Option(Text.of("100%"), Items.FEATHER.getDefaultStack(), () -> CodeClient.MC.getNetworkHandler().sendCommand("fs 100")),
                new Option(Text.of("200%"), Items.FEATHER.getDefaultStack().copyWithCount(2), () -> {
                    CodeClient.MC.getNetworkHandler().sendCommand("fs 200");
                    lastSpeed = 1;
                }),
                new Option(Text.of("500%"), Items.FEATHER.getDefaultStack().copyWithCount(5), () -> {
                    CodeClient.MC.getNetworkHandler().sendCommand("fs 500");
                    lastSpeed = 2;
                }),
                new Option(Text.of("1000%"), Items.FEATHER.getDefaultStack().copyWithCount(10), () -> {
                    CodeClient.MC.getNetworkHandler().sendCommand("fs 1000");
                    lastSpeed = 3;
                })
        );
    }
}
