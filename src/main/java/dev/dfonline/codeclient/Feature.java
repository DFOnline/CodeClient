package dev.dfonline.codeclient;

import dev.dfonline.codeclient.location.Location;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.Packet;

public abstract class Feature {
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
    public void render(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, double cameraX, double cameraY, double cameraZ) {}
    public void onModeChange(Location location) {}
}
