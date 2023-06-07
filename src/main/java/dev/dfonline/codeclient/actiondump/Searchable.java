package dev.dfonline.codeclient.actiondump;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public interface Searchable {
    List<String> getTerms();
    ItemStack getItem();

    class StaticSearchable implements Searchable {
        private final List<String> terms;
        private final ItemStack item;

        public StaticSearchable(ItemStack item) {
            this.terms = new ArrayList<>();
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