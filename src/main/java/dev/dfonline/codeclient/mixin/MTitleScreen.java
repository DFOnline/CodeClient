package dev.dfonline.codeclient.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.dfonline.codeclient.CodeClient;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Consumer;

@Mixin(TitleScreen.class)
public class MTitleScreen extends Screen {
    protected MTitleScreen(Component component) {
        super(component);
    }

    @Shadow
    private Component getMultiplayerDisabledReason() {
        return null;
    }

    private void connect() {
        ServerData serverData = new ServerData("diamondfire", "mcdiamondfire.com", false);
        ConnectScreen.startConnecting((TitleScreen)(Object)this, CodeClient.MC, ServerAddress.parseString(serverData.ip), serverData);
    }

    // Gives an error but it does work
    @ModifyVariable(method = "init", at = @At("STORE"), ordinal = 3)
    private int changedHeight(int l) {
        return l - 48 - 12;
    }

    @Inject(method = "createNormalMenuOptions", at = @At("HEAD"), cancellable = true)
    private void createNormalMenuOptions(int y, int rowHeight, CallbackInfo ci) {
        final Component component = this.getMultiplayerDisabledReason();
        boolean bl = component == null;
        Button.OnTooltip onTooltip = component == null ? Button.NO_TOOLTIP : new Button.OnTooltip(){

            @Override
            public void onTooltip(Button button, PoseStack poseStack, int i, int j) {
                MTitleScreen.this.renderTooltip(poseStack, MTitleScreen.this.minecraft.font.split(component, Math.max(MTitleScreen.this.width / 2 - 43, 170)), i, j);
            }

            @Override
            public void narrateTooltip(Consumer<Component> contents) {
                contents.accept(component);
            }
        };
        this.addRenderableWidget(new Button(this.width / 2 - 100, y + rowHeight * 2 + 12, 200, 20, Component.literal("play"), button -> connect(), onTooltip));
        ci.cancel();
    }
}
