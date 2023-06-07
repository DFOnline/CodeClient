package dev.dfonline.codeclient.switcher;

import dev.dfonline.codeclient.CodeClient;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class SpeedSwitcher extends GenericSwitcher {
    public SpeedSwitcher() {
        super(Text.literal("Speed Switcher"), GLFW.GLFW_KEY_F3, GLFW.GLFW_KEY_F5);
    }

    @Override
    protected void init() {
        footer = Text.literal("[ F5 ]").formatted(Formatting.AQUA).append(Text.literal(" Next").formatted(Formatting.WHITE));
        // TODO: use last selected.
        selected = 0;
        super.init();
    }

    @Override
    List<Option> getOptions() {
        return List.of(
               new Option(Text.of("100%"), Items.FEATHER.getDefaultStack(), () -> CodeClient.MC.getNetworkHandler().sendCommand("fs 100")),
               new Option(Text.of("200%"), Items.FEATHER.getDefaultStack().copyWithCount(2), () -> CodeClient.MC.getNetworkHandler().sendCommand("fs 200")),
               new Option(Text.of("500%"), Items.FEATHER.getDefaultStack().copyWithCount(5), () -> CodeClient.MC.getNetworkHandler().sendCommand("fs 500")),
               new Option(Text.of("1000%"), Items.FEATHER.getDefaultStack().copyWithCount(10), () -> CodeClient.MC.getNetworkHandler().sendCommand("fs 1000"))
        );
    }
}
