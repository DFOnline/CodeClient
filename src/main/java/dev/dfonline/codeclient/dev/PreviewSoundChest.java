package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.KeyBinds;
import dev.dfonline.codeclient.dev.overlay.ChestPeeker;
import dev.dfonline.codeclient.hypercube.item.Sound;
import dev.dfonline.codeclient.hypercube.item.VarItem;
import dev.dfonline.codeclient.hypercube.item.VarItems;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.Optional;

public class PreviewSoundChest extends Feature {

    @Override
    public void tick() {
        if (KeyBinds.previewSounds.wasPressed() && CodeClient.MC.world != null && CodeClient.location instanceof Dev) {
            if (CodeClient.MC.player == null) return;
            ChestPeeker.pick(items -> {
                for (ItemStack item : items) {
                    VarItem varItem = VarItems.parse(item);
                    if (!(varItem instanceof Sound sound)) continue;
                    Optional<dev.dfonline.codeclient.hypercube.actiondump.Sound> adSound = sound.getSoundId();
                    if (adSound.isEmpty()) continue;
                    try {
                        CodeClient.MC.player.playSound(
                                (SoundEvent) SoundEvents.class.getDeclaredField(adSound.get().sound).get(null),
                                (float) sound.getVolume(),
                                (float) sound.getPitch()
                        );
                    } catch (Exception ignored) {

                    }
                }
            });
        }
    }
}
