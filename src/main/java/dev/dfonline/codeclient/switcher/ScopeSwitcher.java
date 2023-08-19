package dev.dfonline.codeclient.switcher;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.hypercube.item.Scope;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.List;

public class ScopeSwitcher extends GenericSwitcher {
    private String option;

    public ScopeSwitcher(String option) {
        super(Text.literal("Scope"), -1, GLFW.GLFW_KEY_SPACE);
        this.option = option;
    }

    @Override
    protected void init() {
        footer = Text.literal("[ F4 ]").formatted(Formatting.AQUA).append(Text.literal(" Next").formatted(Formatting.WHITE));

        selected = switch (option) {
            case "saved" -> 1;
            case "local" -> 2;
            case "line" -> 3;
            default -> 0;
        };

        super.init();
    }

    @Override
    List<Option> getOptions() {
        return List.of(
                new Option(Text.literal(Scope.unsaved.longName).setStyle(Style.EMPTY.withColor(Scope.unsaved.color)), Items.GRAY_WOOL.getDefaultStack(),     () -> run("unsaved")),
                new Option(Text.literal(Scope.saved.longName  ).setStyle(Style.EMPTY.withColor(Scope.saved.color)),   Items.CHEST.getDefaultStack(),         () -> run("saved")),
                new Option(Text.literal(Scope.local.longName  ).setStyle(Style.EMPTY.withColor(Scope.local.color)),   Items.EMERALD_BLOCK.getDefaultStack(), () -> run("local")),
                new Option(Text.literal(Scope.line.longName   ).setStyle(Style.EMPTY.withColor(Scope.line.color)),    Items.LAPIS_BLOCK.getDefaultStack(),   () -> run("line"))
        );
    }

    private void run(String name) {
        CodeClient.MC.player.sendMessage(Text.of(name));
        CodeClient.MC.player.sendMessage(getSelected().text());
    }

    @Override
    protected boolean checkFinished() {
//        if(this.client == null) return false;
//        if(CodeClient.MC.options.sneakKey.isPressed()) return false;
//        if(!CodeClient.MC.options.sneakKey.isPressed()) {
//            Option selected = getSelected();
//            if(selected != null) selected.run();
//            this.client.setScreen(null);
//            return true;
//        }
        return false;
    }
}
