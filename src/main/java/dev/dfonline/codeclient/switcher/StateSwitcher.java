package dev.dfonline.codeclient.switcher;

import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class StateSwitcher extends GenericSwitcher {
    public StateSwitcher() {
        super(Text.literal("Mode Switcher"));
    }

    @Override
    protected void init() {
        Callback callback = () -> {};
        this.options.add(new Option(Text.literal("Test"), Items.STONE.getDefaultStack(), callback));
        this.options.add(new Option(Text.literal("Test"), Items.STONE.getDefaultStack(), callback));
        this.options.add(new Option(Text.literal("Test"), Items.STONE.getDefaultStack(), callback));
        this.options.add(new Option(Text.literal("Test"), Items.STONE.getDefaultStack(), callback));
        super.init();
    }
}
