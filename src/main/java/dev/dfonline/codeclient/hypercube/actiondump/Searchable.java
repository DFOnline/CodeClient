package dev.dfonline.codeclient.hypercube.actiondump;

import net.minecraft.item.ItemStack;

import java.util.List;

public interface Searchable {
    List<String> getTerms();

    ItemStack getItem();

    class StaticSearchable implements Searchable {
        private final List<String> terms;
        private final ItemStack item;

        public StaticSearchable(ItemStack item) {
            this.terms = List.of(item.getName().getString());
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