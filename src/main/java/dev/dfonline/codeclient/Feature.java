package dev.dfonline.codeclient;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.dfonline.codeclient.location.Dev;
import dev.dfonline.codeclient.location.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.phys.BlockHitResult;

public abstract class Feature {
    private ChestFeature chest;

    public Feature() {}

    /**
     * Explicitly enabled in config by the user.
     * Not if the feature is turned on but inactive.
     */
    public boolean enabled() {
        return true;
    }
    public void reset() {}
    public void tick() {}
    public boolean onReceivePacket(Packet<?> packet) {
        return false;
    }
    public boolean onSendPacket(Packet<?> packet) {
        return false;
    }
    public void render(PoseStack matrices, MultiBufferSource.BufferSource vertexConsumers, double cameraX, double cameraY, double cameraZ) {}
    public void onModeChange(Location location) {}
    public void onClickChest(BlockHitResult hitResult) {}
    /**
     * When you break a block in the dev area.
     * @param dev The plot instance.
     * @param pos The blockPos of the broken block.
     * @param breakPos The origin of the given code block broken. May be null.
     */
    public void onBreakBlock(@NotNull Dev dev, @NotNull BlockPos pos, @Nullable BlockPos breakPos) {}

    /**
     * Load a chest feature.
     * @param screen The chest opened.
     * @return ChestFeature instance.
     */
    @Nullable
    protected ChestFeature makeChestFeature(AbstractContainerScreen<?> screen) {
        return null;
    }

    public final void openChest(AbstractContainerScreen<?> screen) {
        chest = makeChestFeature(screen);
    }

    public final void closeChest() {
        chest = null;
    }

    public final Optional<ChestFeature> getChest() {
        return Optional.ofNullable(chest);
    }
}
