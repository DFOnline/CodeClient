package dev.dfonline.codeclient.hypercube.actiondump;

public class Tag {
    public String name;
    public TagOption[] options;
    public String defaultOption;
    public int slot;

    public static class TagOption {
        public String name;
        public Icon icon;
        public String[] aliases;
    }
}