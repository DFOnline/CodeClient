package dev.dfonline.codeclient.switcher;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.*;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class StateSwitcher extends GenericSwitcher {
    public StateSwitcher() {
        super(Component.translatable("codeclient.switcher.state"), GLFW.GLFW_KEY_F3, GLFW.GLFW_KEY_F4);
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
        footer = Component.translatable("codeclient.switcher.footer.next", Component.translatable("codeclient.switcher.footer.brackets", "F4").withStyle(ChatFormatting.AQUA));
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
        options.add(new Option(Component.literal("Play"), Items.DIAMOND.getDefaultInstance(), () -> joinMode("play")));
        options.add(new Option(Component.literal("Build"), Items.GRASS_BLOCK.getDefaultInstance(), () -> joinMode("build")));
        options.add(new Option(Component.literal("Code"), Items.COMMAND_BLOCK.getDefaultInstance(), () -> joinMode("dev")));
        return options;
    }

    private void joinMode(String mode) {
        CodeClient.MC.getConnection().sendCommand(mode);
    }
}
