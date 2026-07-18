package dev.dfonline.codeclient.mixin.screen;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.dev.menu.InsertOverlayFeature;
import dev.dfonline.codeclient.dev.InteractionManager;
import dev.dfonline.codeclient.dev.menu.customchest.CustomChestHandler;
import dev.dfonline.codeclient.dev.menu.customchest.CustomChestMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import static net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor;

@Mixin(MenuScreens.class)
public class MHandledScreens {
    @Shadow
    @Final
    private static Map<MenuType<?>, MenuScreens.ScreenConstructor<?, ?>> SCREENS;

    @Redirect(method = "create", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/MenuScreens;getConstructor(Lnet/minecraft/world/inventory/MenuType;)Lnet/minecraft/client/gui/screens/MenuScreens$ScreenConstructor;"))
    private static <T extends AbstractContainerMenu> ScreenConstructor<?, ?> overrideProvider(MenuType<T> type) {
        boolean open = InteractionManager.isOpeningCodeChest;
        InteractionManager.isOpeningCodeChest = false;
        if(open && type == MenuType.GENERIC_9x3) {
            CodeClient.getFeature(InsertOverlayFeature.class).ifPresent(insertOverlayFeature -> {
                CodeClient.isCodeChest();
            });
            if (Config.getConfig().CustomCodeChest != Config.CustomChestMenuType.OFF) {
                //noinspection rawtypes
                return (ScreenConstructor) (handler, playerInventory, title) -> new CustomChestMenu(new CustomChestHandler(handler.containerId), playerInventory, title);
            }
        }
        return SCREENS.get(type);
    }
}
