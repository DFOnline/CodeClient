package dev.dfonline.codeclient.switcher;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.*;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class StateSwitcher extends GenericSwitcher {
    public StateSwitcher() {
        super(Text.translatable("codeclient.switcher.state"), GLFW.GLFW_KEY_F3, GLFW.GLFW_KEY_F4);
    }

    public static class StateSwitcherFeature extends Feature {
        @Override
        public boolean enabled() {
            return Config.getConfig().StateSwitcher;
        }

        public boolean open() {
            if (CodeClient.location instanceof Plot) {
                CodeClient.MC.setScreen(new StateSwitcher());
                return true;
            }
            return false;
        }
    }

    @Override
    protected void init() {
        footer = Text.translatable("codeclient.switcher.footer.next", Text.translatable("codeclient.switcher.footer.brackets", "F4").formatted(Formatting.AQUA));
        selected = 0;
        if (CodeClient.lastLocation instanceof Plot) {
            if (CodeClient.lastLocation instanceof Play) selected = 0;
            if (CodeClient.lastLocation instanceof Build) selected = 1;
            if (CodeClient.lastLocation instanceof Dev) selected = 2;
        }
        if (CodeClient.lastLocation instanceof Spawn) {
            if (CodeClient.location instanceof Creator) selected = 0;
            if (CodeClient.location instanceof Play) selected = 2;
        }
        super.init();
    }

    @Override
    List<Option> getOptions() {
        ArrayList<Option> options = new ArrayList<>();
        options.add(new Option(Text.literal("Play"), Items.DIAMOND.getDefaultStack(), () -> joinMode("play")));
        options.add(new Option(Text.literal("Build"), Items.GRASS_BLOCK.getDefaultStack(), () -> joinMode("build")));
        options.add(new Option(Text.literal("Code"), Items.COMMAND_BLOCK.getDefaultStack(), () -> joinMode("dev")));
        return options;
    }

    private void joinMode(String mode) {
        CodeClient.MC.getNetworkHandler().sendCommand(mode);
    }
}
