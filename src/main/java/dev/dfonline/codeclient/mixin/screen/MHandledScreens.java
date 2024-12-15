package dev.dfonline.codeclient.mixin.screen;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.dev.menu.InsertOverlayFeature;
import dev.dfonline.codeclient.dev.InteractionManager;
import dev.dfonline.codeclient.dev.menu.customchest.CustomChestHandler;
import dev.dfonline.codeclient.dev.menu.customchest.CustomChestMenu;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

import static net.minecraft.client.gui.screen.ingame.HandledScreens.Provider;

@Mixin(HandledScreens.class)
public class MHandledScreens {
    @Shadow
    @Final
    private static Map<ScreenHandlerType<?>, HandledScreens.Provider<?, ?>> PROVIDERS;

    @Redirect(method = "open", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreens;getProvider(Lnet/minecraft/screen/ScreenHandlerType;)Lnet/minecraft/client/gui/screen/ingame/HandledScreens$Provider;"))
    private static <T extends ScreenHandler> Provider<?, ?> overrideProvider(ScreenHandlerType<T> type) {
        boolean open = InteractionManager.isOpeningCodeChest;
        InteractionManager.isOpeningCodeChest = false;
        if(open && type == ScreenHandlerType.GENERIC_9X3) {
            CodeClient.getFeature(InsertOverlayFeature.class).ifPresent(insertOverlayFeature -> {
                CodeClient.isCodeChest();
            });
            if (Config.getConfig().CustomCodeChest != Config.CustomChestMenuType.OFF) {
                //noinspection rawtypes
                return (Provider) (handler, playerInventory, title) -> new CustomChestMenu(new CustomChestHandler(handler.syncId), playerInventory, title);
            }
        }
        return PROVIDERS.get(type);
    }
}
