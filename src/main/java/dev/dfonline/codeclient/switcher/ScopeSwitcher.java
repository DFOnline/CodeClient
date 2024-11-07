package dev.dfonline.codeclient.switcher;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
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
        ArrayList list = new ArrayList<>();
        list.add(new Option(Text.literal(Scope.unsaved.longName).setStyle(Style.EMPTY.withColor(Scope.unsaved.color)), Items.ENDER_CHEST.getDefaultStack(), () -> run("unsaved")));
        list.add(new Option(Text.literal(Scope.saved.longName).setStyle(Style.EMPTY.withColor(Scope.saved.color)), Items.CHEST.getDefaultStack(), () -> run("saved")));
        list.add(new Option(Text.literal(Scope.local.longName).setStyle(Style.EMPTY.withColor(Scope.local.color)), Items.EMERALD_BLOCK.getDefaultStack(), () -> run("local")));
        list.add(new Option(Text.literal(Scope.line.longName).setStyle(Style.EMPTY.withColor(Scope.line.color)), Items.LAPIS_BLOCK.getDefaultStack(), () -> run("line")));
//        list.remove(3);
        return list;
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
