package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.command.CommandSender;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.config.KeyBinds;
import dev.dfonline.codeclient.location.Creator;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.phys.Vec3;

public class BuildPhaser extends Feature {
    private boolean clipping = false;
    private boolean wasFlying = true;
    private Vec3 lastPos = new Vec3(0, 0, 0);
    private boolean allowPacket = false;
    private boolean waitForTP = false;
    private boolean updateVelocity = false;
    private boolean dontSpamBuildWarn = false;
    private boolean heldKeyCheck = false;
    private Vec3 velocity = null;

    @Override
    public void reset() {
        clipping = false;
        wasFlying = true;
        lastPos = new Vec3(0,0,0);
        allowPacket = false;
        waitForTP = false;
        dontSpamBuildWarn = false;
        heldKeyCheck = false;
        velocity = null;
    }

    public boolean isClipping() {
        return clipping;
    }

    private boolean isPhaseToggleEnabled() {
        return Config.getConfig().PhaseToggle;
    }

    public void tick() {
        if (CodeClient.location instanceof Creator plot) {
            if (plot.getX() == null) {
                if (KeyBinds.clipBind.consumeClick())
                    Utility.sendMessage(Component.translatable("codeclient.phaser.plot_origin"));
            }
            boolean currentKeyPressed = KeyBinds.clipBind.isDown();
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
                player.setPosRaw(
                        Math.min(Math.max(player.getX(), plot.getX() - size.codeWidth), plot.getX() + size.size + 1),
                        player.getY(),
                        Math.min(Math.max(player.getZ(), plot.getZ() - size.codeLength), plot.getZ() + size.size + 1)
                );
                allowPacket = true;
                CodeClient.MC.getConnection().send(new ServerboundMovePlayerPacket.Pos(lastPos.x, lastPos.y, lastPos.z, false, true));
                CodeClient.MC.player.getAbilities().flying = true;
            }
        } else if (clipping || waitForTP) {
            disableClipping();
        }

//        if (CodeClient.location instanceof Build) {
//            if (KeyBinds.clipBind.isPressed() && !dontSpamBuildWarn) {
//                dontSpamBuildWarn = true;
//                Utility.sendMessage(Text.translatable("codeclient.phaser.dev_mode1",
//                                Text.translatable("codeclient.phaser.dev_mode2")
//                                        .setStyle(Text.empty().getStyle()
//                                                .withClickEvent(new ClickEvent.OpenUrl(URI.create("https://github.com/DFOnline/CodeClient/wiki/phaser#internal"))
//                                                .withHoverEvent(new HoverEvent.ShowText(Text.translatable("chat.link.open")))
//                                        ).formatted(Formatting.AQUA, Formatting.UNDERLINE)),
//                        ChatType.FAIL);
//            }
//            if (dontSpamBuildWarn && !KeyBinds.clipBind.isPressed()) dontSpamBuildWarn = false;
//        }
    }

    public boolean onSendPacket(Packet<?> packet) {
        if (allowPacket) {
            allowPacket = false;
            return false;
        }
        if (packet instanceof ServerboundMovePlayerPacket.Pos && updateVelocity) {
            var player = CodeClient.MC.player;
            if (player != null && velocity != null) {
                player.setDeltaMovement(velocity);
            }
            updateVelocity = false;
            return false;
        }
        return clipping && (packet instanceof ServerboundMovePlayerPacket || packet instanceof ServerboundPlayerCommandPacket);
    }

    public boolean onReceivePacket(Packet<?> packet) {
        if (!waitForTP) return false;
        if (packet instanceof ClientboundPlayerPositionPacket move) {
            var net = CodeClient.MC.getConnection();

            net.send(new ServerboundAcceptTeleportationPacket(move.id()));
            var change = move.change();
            net.send(
                            new ServerboundMovePlayerPacket.PosRot(lastPos.add(change.deltaMovement()), change.yRot(), change.xRot(), false, false)
                    );

            updateVelocity = true;
            return true;
        }
        if (packet instanceof ClientboundAnimatePacket) return true;
        if (packet instanceof ClientboundSoundEntityPacket) {
            waitForTP = false;
            return true;
        }
        return false;
    }

    private void startClipping() {
        Abilities abilities = CodeClient.MC.player.getAbilities();
        lastPos = CodeClient.MC.player.position();
        wasFlying = abilities.flying;
        clipping = true;
        abilities.flying = true;
        abilities.mayfly = false;
    }

    private void finishClipping() {
        disableClipping();
        if (CodeClient.location instanceof Creator plot) {
            LocalPlayer player = CodeClient.MC.player;
            Abilities abilities = player.getAbilities();
            abilities.mayfly = true;
            abilities.flying = wasFlying;
            waitForTP = true;

            var size = plot.assumeSize();

            var x = Math.min(Math.max(player.getX(), plot.getX() - plot.assumeSize().codeLength), plot.getX() + size.size + 1);
            var y = player.getY();
            var z = Math.min(Math.max(player.getZ(), plot.getZ() - plot.assumeSize().codeWidth), plot.getZ() + size.size + 1);
            var pitch = player.getXRot();
            var yaw = player.getYRot();

            velocity = player.getDeltaMovement();

            CommandSender.queue(String.format("ptp %s %s %s %s %s", x, y, z, pitch, yaw));

            /*
            Vec3d pos = plot.getPos().relativize(player.getEntityPos());
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
            */
        }
    }

    public void disableClipping() {
        clipping = false;
        waitForTP = false;
    }
}
