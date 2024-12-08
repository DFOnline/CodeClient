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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ScopeSwitcher extends GenericSwitcher {
    private final String option;

    public ScopeSwitcher(String option) {
        super(Text.translatable("codeclient.switcher.scope"), -1, GLFW.GLFW_KEY_SPACE);
        this.option = option;
    }

    public static class ScopeSwitcherFeature extends Feature {
        @Override
        public boolean enabled() {
            return Config.getConfig().ScopeSwitcher;
        }

        public void open(String option) {
            CodeClient.MC.setScreen(new ScopeSwitcher(option));
        }
    }

    @Override
    protected void init() {
        footer = Text.translatable("codeclient.switcher.scope.select", Text.translatable("codeclient.switcher.footer.brackets", "Click").formatted(Formatting.AQUA)).formatted(Formatting.WHITE);

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
        return List.of(new Option(Text.literal(Scope.unsaved.longName).setStyle(Style.EMPTY.withItalic(false).withColor(Scope.unsaved.color)), Items.ENDER_CHEST.getDefaultStack(), () -> run("unsaved")),
                new Option(Text.literal(Scope.saved.longName).setStyle(Style.EMPTY.withItalic(false).withColor(Scope.saved.color)), Items.CHEST.getDefaultStack(), () -> run("saved")),
                new Option(Text.literal(Scope.local.longName).setStyle(Style.EMPTY.withItalic(false).withColor(Scope.local.color)), Items.EMERALD_BLOCK.getDefaultStack(), () -> run("local")),
                new Option(Text.literal(Scope.line.longName).setStyle(Style.EMPTY.withItalic(false).withColor(Scope.line.color)), Items.LAPIS_BLOCK.getDefaultStack(), () -> run("line"))
        );
    }

    private void run(String name) {
        ItemStack stack = CodeClient.MC.player.getStackInHand(Hand.MAIN_HAND);

        DFItem item = DFItem.of(stack);
        PublicBukkitValues pbv = item.getPublicBukkitValues();
        String varItem = pbv.getHypercubeStringValue("varitem");
        if (varItem.isEmpty()) return;
        JsonObject var = JsonParser.parseString(varItem).getAsJsonObject();
        if (!var.get("id").getAsString().equals("var")) return;
        JsonObject data = var.get("data").getAsJsonObject();
        data.addProperty("scope", name);
        pbv.setHypercubeStringValue("varitem", var.toString());
        item.getItemData().setPublicBukkitValues(pbv);
        ArrayList<Text> lore = new ArrayList<>(item.getLore());
        lore.set(0, getSelected().text());
        item.setLore(lore);
        Utility.sendHandItem(item.getItemStack());
        CodeClient.MC.gameRenderer.firstPersonRenderer.resetEquipProgress(Hand.MAIN_HAND);
    }

    @Override
    protected boolean checkFinished() {
        if (hasClicked) {
            Option selected = getSelected();
            if (selected != null) selected.run();
            this.client.setScreen(null);
            return true;
        }
        return false;
    }
}
