package dev.dfonline.codeclient.switcher;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.data.PublicBukkitValues;
import dev.dfonline.codeclient.hypercube.item.Scope;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ScopeSwitcher extends GenericSwitcher {
    private final String option;

    public ScopeSwitcher(String option) {
        super(Component.translatable("codeclient.switcher.scope"), -1, GLFW.GLFW_KEY_SPACE);
        this.option = option;
    }

    public static class ScopeSwitcherFeature extends Feature {
        @Override
        public boolean enabled() {
            return Config.getConfig().ScopeSwitcher;
        }

        public void open(String option) {
            CodeClient.MC.gui.setScreen(new ScopeSwitcher(option));
        }
    }

    @Override
    protected void init() {
        footer = Component.translatable("codeclient.switcher.scope.select", Component.translatable("codeclient.switcher.footer.brackets", "Click").withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.WHITE);

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
        return List.of(new Option(Component.literal(Scope.unsaved.longName).setStyle(Style.EMPTY.withItalic(false).withColor(Scope.unsaved.color)), Items.ENDER_CHEST.getDefaultInstance(), () -> run("unsaved")),
                new Option(Component.literal(Scope.saved.longName).setStyle(Style.EMPTY.withItalic(false).withColor(Scope.saved.color)), Items.CHEST.getDefaultInstance(), () -> run("saved")),
                new Option(Component.literal(Scope.local.longName).setStyle(Style.EMPTY.withItalic(false).withColor(Scope.local.color)), Items.EMERALD_BLOCK.getDefaultInstance(), () -> run("local")),
                new Option(Component.literal(Scope.line.longName).setStyle(Style.EMPTY.withItalic(false).withColor(Scope.line.color)), Items.LAPIS_BLOCK.getDefaultInstance(), () -> run("line"))
        );
    }

    private void run(String name) {
        ItemStack stack = CodeClient.MC.player.getItemInHand(InteractionHand.MAIN_HAND);

        DFItem item = DFItem.of(stack);

        if (!item.hasHypercubeKey("varitem")) return;
        PublicBukkitValues pbv = item.getPublicBukkitValues();
        if (pbv == null) return;
        Optional<String> varItem = pbv.getHypercubeStringValue("varitem");
        if (varItem.isEmpty()) return;
        JsonObject var = JsonParser.parseString(varItem.get()).getAsJsonObject();
        if (!var.get("id").getAsString().equals("var")) return;
        JsonObject data = var.get("data").getAsJsonObject();
        data.addProperty("scope", name);
        pbv.setHypercubeStringValue("varitem", var.toString());
        item.getItemData().setPublicBukkitValues(pbv);
        item.setLore(List.of(getSelected().text()));
        Utility.sendHandItem(item.getItemStack());
        CodeClient.MC.gameRenderer.itemInHandRenderer.itemUsed(InteractionHand.MAIN_HAND);
    }

    @Override
    protected boolean checkFinished() {
        if (hasClicked) {
            Option selected = getSelected();
            if (selected != null) selected.run();
            this.minecraft.gui.setScreen(null);
            return true;
        }
        return false;
    }
}
