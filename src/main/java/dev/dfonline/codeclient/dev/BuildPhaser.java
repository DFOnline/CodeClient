package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.config.KeyBinds;
import dev.dfonline.codeclient.hypercube.item.Location;
import dev.dfonline.codeclient.location.Build;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class BuildPhaser extends Feature {
    private boolean clipping = false;
    private boolean wasFlying = true;
    private Vec3d lastPos = new Vec3d(0, 0, 0);
    private boolean allowPacket = false;
    private boolean waitForTP = false;
    private boolean dontSpamBuildWarn = false;
    private boolean heldKeyCheck = false;

    @Override
    public void reset() {
        clipping = false;
        wasFlying = true;
        lastPos = new Vec3d(0,0,0);
        allowPacket = false;
        waitForTP = false;
        dontSpamBuildWarn = false;
        heldKeyCheck = false;
    }

    public boolean isClipping() {
        return clipping;
    }

    private boolean isPhaseToggleEnabled() {
        return Config.getConfig().PhaseToggle;
    }

    public void tick() {
        if (CodeClient.location instanceof Dev plot) {
            if (plot.getX() == null) {
                if (KeyBinds.clipBind.wasPressed())
                    Utility.sendMessage(Text.translatable("codeclient.phaser.plot_origin"));
            }
            boolean currentKeyPressed = KeyBinds.clipBind.isPressed();
            if (isPhaseToggleEnabled()) {
                if (currentKeyPressed && !heldKeyCheck) {
                    if (clipping) {
                        finishClipping();
                    } else if (!clipping && plot.getX() != null) {
                        startClipping();
                    }
                    heldKeyCheck = true;
                } else if (!currentKeyPressed && heldKeyCheck) {
                    heldKeyCheck = false;
                }
            } else {
                if (!clipping && currentKeyPressed && plot.getX() != null) startClipping();
                if (clipping && !currentKeyPressed) finishClipping();
            }
            if (clipping) {
                var player = CodeClient.MC.player;
                var size = plot.assumeSize();
                player.setPos(
                        Math.min(Math.max(player.getX(), plot.getX()), plot.getX() + plot.assumeSize().size + 1),
                        player.getY(),
                        Math.min(Math.max(player.getZ(), plot.getZ()), plot.getZ() + plot.assumeSize().size + 1)
                );
                allowPacket = true;
                CodeClient.MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(lastPos.x, lastPos.y, lastPos.z, false));
                CodeClient.MC.player.getAbilities().flying = true;
            }
        } else if (clipping || waitForTP) {
            disableClipping();
        }

        if (CodeClient.location instanceof Build) {
            if (KeyBinds.clipBind.isPressed() && !dontSpamBuildWarn) {
                dontSpamBuildWarn = true;
                Utility.sendMessage(Text.translatable("codeclient.phaser.dev_mode1",
                                Text.translatable("codeclient.phaser.dev_mode2")
                                        .setStyle(Text.empty().getStyle()
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/DFOnline/CodeClient/wiki/phaser#internal"))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.link.open")))
                                        ).formatted(Formatting.AQUA, Formatting.UNDERLINE)),
                        ChatType.FAIL);
            }
            if (dontSpamBuildWarn && !KeyBinds.clipBind.isPressed()) dontSpamBuildWarn = false;
        }
    }

    public boolean onSendPacket(Packet<?> packet) {
        if (allowPacket) {
            allowPacket = false;
            return false;
        }
        return clipping && (packet instanceof PlayerMoveC2SPacket || packet instanceof ClientCommandC2SPacket);
    }

    public boolean onReceivePacket(Packet<?> packet) {
        if (!waitForTP) return false;
        if (packet instanceof PlayerPositionLookS2CPacket move) {
            CodeClient.MC.getNetworkHandler().sendPacket(new TeleportConfirmC2SPacket(move.getTeleportId()));
            return true;
        }
        if (packet instanceof EntityAnimationS2CPacket) return true;
        if (packet instanceof PlaySoundFromEntityS2CPacket) {
            waitForTP = false;
            return true;
        }
        return false;
    }

    private void startClipping() {
        PlayerAbilities abilities = CodeClient.MC.player.getAbilities();
        lastPos = CodeClient.MC.player.getPos();
        wasFlying = abilities.flying;
        clipping = true;
        abilities.flying = true;
        abilities.allowFlying = false;
    }

    private void finishClipping() {
        disableClipping();
        if (CodeClient.location instanceof Dev plot) {
            ClientPlayerEntity player = CodeClient.MC.player;
            PlayerAbilities abilities = player.getAbilities();
            abilities.allowFlying = true;
            abilities.flying = wasFlying;
            waitForTP = true;

            Vec3d pos = plot.getPos().relativize(player.getPos());
            ItemStack location = new Location(pos.x, pos.y, pos.z, player.getPitch(), player.getYaw()).toStack();

            ItemStack lastItem = player.getStackInHand(Hand.MAIN_HAND);
            Utility.sendHandItem(location);

            boolean sneaky = !player.isSneaking();
            BlockPos lastBlockPos = new BlockPos((int) lastPos.x, (int) lastPos.y, (int) lastPos.z);
            ClientPlayNetworkHandler net = CodeClient.MC.getNetworkHandler();
            if (sneaky) net.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            net.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, lastBlockPos, Direction.UP));
            net.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, lastBlockPos, Direction.UP));
            if (sneaky)
                net.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));

            Utility.sendHandItem(lastItem);

            final int MAX_SPEED = 2;

            var velocity = CodeClient.MC.player.getVelocity();
            if(velocity.length() > MAX_SPEED) {
                CodeClient.MC.player.setVelocity(velocity.normalize().multiply(MAX_SPEED));
            }
        }
    }

    public void disableClipping() {
        clipping = false;
        waitForTP = false;
    }
}
