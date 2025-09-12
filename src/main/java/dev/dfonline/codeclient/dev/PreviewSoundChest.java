package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.KeyBinds;
import dev.dfonline.codeclient.dev.overlay.ChestPeeker;
import dev.dfonline.codeclient.hypercube.actiondump.Sound;
import dev.dfonline.codeclient.hypercube.item.VarItem;
import dev.dfonline.codeclient.hypercube.item.VarItems;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.util.List;

public class PreviewSoundChest extends Feature {

    @SuppressWarnings("deprecation")
    private static final Random threadSafeRandom = Random.createThreadSafe();

    @Override
    public void tick() {
        if (KeyBinds.previewSounds.wasPressed() && CodeClient.MC.world != null && CodeClient.location instanceof Dev) {
            if (CodeClient.MC.player == null) return;
            ChestPeeker.pick(PreviewSoundChest::previewSounds);
        }
    }

    public static void previewSounds(List<ItemStack> items) {
        ClientPlayerEntity player = CodeClient.MC.player;
        if (player == null || CodeClient.MC.world == null) return;

        for (ItemStack item : items) {
            VarItem varItem = VarItems.parse(item);
            if (!(varItem instanceof dev.dfonline.codeclient.hypercube.item.Sound sound)) continue;

            sound.getSoundId().ifPresent(adSound -> {
                Sound.Variant variant = adSound.getVariantFromName(sound.getVariant());
                Identifier id = Identifier.ofVanilla(adSound.soundId);
                SoundEvent event = SoundEvent.of(id);

                CodeClient.MC.world.playSound(
                        player,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        event,
                        SoundCategory.MASTER,
                        (float) sound.getVolume(),
                        (float) sound.getPitch(),
                        // I would copy-paste this entire method call for either variant or non-variant
                        // to preserve the original random, but this is way cleaner.
                        variant != null ? variant.seed() : threadSafeRandom.nextLong()
                );
            });
        }
    }

}
