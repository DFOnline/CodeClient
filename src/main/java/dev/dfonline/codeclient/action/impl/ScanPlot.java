package dev.dfonline.codeclient.action.impl;

import dev.dfonline.codeclient.Callback;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.action.Action;
import dev.dfonline.codeclient.hypercube.template.Template;
import dev.dfonline.codeclient.hypercube.template.TemplateBlock;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ScanPlot extends Action {
    private List<BlockPos> blocks = null;
    private GoTo goTo = null;
    private int progress = 0;
    private static final Vec3d goToOffset = new Vec3d(0,1.5,0);
    private boolean waitForResponse = false;
    private final ArrayList<Template> scanList;

    public ScanPlot(Callback callback, ArrayList<Template> scanList) {
        super(callback);
        this.scanList = scanList;
        if (!(CodeClient.location instanceof Dev)) {
            throw new IllegalStateException("Player must be in dev mode.");
        }
    }

    @Override
    public void init() {
        if(CodeClient.location instanceof Dev plot) {
            blocks = plot.scanForSigns(Pattern.compile("(PLAYER|ENTITY) EVENT|FUNCTION|PROCESS"),Pattern.compile(".*")).keySet().stream().toList();
        }
    }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        var net = CodeClient.MC.getNetworkHandler();
        if(waitForResponse && net != null && packet instanceof ScreenHandlerSlotUpdateS2CPacket slot) {
            var data = Utility.templateDataItem(slot.getStack());
            var template = Template.parse64(data);
            if(template == null) return false;
            scanList.add(Template.parse64(data));
            waitForResponse = false;
            net.sendPacket(new CreativeInventoryActionC2SPacket(slot.getSlot(), ItemStack.EMPTY));
            return true;
        }
        return false;
    }

    @Override
    public void onTick() {
        var net = CodeClient.MC.getNetworkHandler();
        var player = CodeClient.MC.player;
        var inter = CodeClient.MC.interactionManager;
        if(CodeClient.location instanceof Dev && this.blocks != null && net != null && player != null && inter != null) {
            if(goTo == null || goTo.complete) {
                if(goTo != null && !waitForResponse) {
                    var current = blocks.get(progress);
                    boolean sneaky = !player.isSneaking();
                    if(sneaky) net.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                    inter.interactBlock(player, Hand.MAIN_HAND, new BlockHitResult(current.toCenterPos(), Direction.UP, current,false));
                    if(sneaky) net.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
                    waitForResponse = true;
                    progress += 1;
                    goTo = null;
                }
                if(progress == blocks.size()) {
                    if(!waitForResponse) {
                        callback();
                    }
                    return;
                }
                goTo = new GoTo(blocks.get(progress).toCenterPos().add(goToOffset), () -> {});
                goTo.init();
            }
            goTo.onTick();
        }
    }
}
