package dev.dfonline.codeclient.actiondump;

public class Action {
    public String name;
    public String codeblockName;
    public String[] aliases;
    public Item icon;
    public String[] subActionBlocks;

    public CodeBlock getCodeBlock() {
        try {
            for (CodeBlock codeBlock: ActionDump.getActionDump().codeblocks) {
                if(codeblockName.equals(codeBlock.name)) return codeBlock;
            }
        }
        catch (Exception ignored) {}
        return null;
    }
}
