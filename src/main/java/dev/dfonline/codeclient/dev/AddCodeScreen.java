package dev.dfonline.codeclient.dev;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WSprite;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class AddCodeScreen extends LightweightGuiDescription {
    public AddCodeScreen() {
        WGridPanel root = new WGridPanel();
        setRootPanel(root);
        root.setSize(256, 240);
        root.setInsets(Insets.ROOT_PANEL);

        WSprite icon = new WSprite(new ResourceLocation("minecraft:textures/item/redstone.png"));
        root.add(icon, 0, 2, 1, 1);

        WButton button = new WButton(Component.translatable("gui.examplemod.examplebutton"));
        root.add(button, 0, 3, 4, 1);

        WLabel label = new WLabel(Component.literal("Test"), 0xFFFFFF);
        root.add(label, 0, 4, 2, 1);

        root.validate(this);
    }
}
