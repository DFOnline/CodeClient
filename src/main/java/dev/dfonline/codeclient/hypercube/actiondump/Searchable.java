package dev.dfonline.codeclient.hypercube.actiondump;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public interface Searchable {
    List<String> getTerms();

    ItemStack getItem();

    class StaticSearchable implements Searchable {
        private final List<String> terms;
        private final ItemStack item;

        public StaticSearchable(ItemStack item) {
            this.terms = List.of(item.getHoverName().getString());
            this.item = item;
        }

        @Override
        public List<String> getTerms() {
            return terms;
        }

        @Override
        public ItemStack getItem() {
            return item;
        }
    }
}