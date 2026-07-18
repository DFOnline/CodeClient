package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.KeyBinds;
import dev.dfonline.codeclient.dev.overlay.ChestPeeker;
import dev.dfonline.codeclient.hypercube.actiondump.Sound;
import dev.dfonline.codeclient.hypercube.item.VarItem;
import dev.dfonline.codeclient.hypercube.item.VarItems;
import dev.dfonline.codeclient.location.Dev;
import java.util.List;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public class PreviewSoundChest extends Feature {

    @SuppressWarnings("deprecation")
    private static final RandomSource threadSafeRandom = RandomSource.createThreadSafe();

    @Override
    public void tick() {
        if (KeyBinds.previewSounds.consumeClick() && CodeClient.MC.level != null && CodeClient.location instanceof Dev) {
            if (CodeClient.MC.player == null) return;
            ChestPeeker.pick(PreviewSoundChest::previewSounds);
        }
    }

    public static void previewSounds(List<ItemStack> items) {
        LocalPlayer player = CodeClient.MC.player;
        if (player == null || CodeClient.MC.level == null) return;

        for (ItemStack item : items) {
            VarItem varItem = VarItems.parse(item);
            if (!(varItem instanceof dev.dfonline.codeclient.hypercube.item.Sound sound)) continue;

            sound.getSoundId().ifPresent(adSound -> {
                Sound.Variant variant = adSound.getVariantFromName(sound.getVariant());
                Identifier id = Identifier.withDefaultNamespace(adSound.soundId);
                SoundEvent event = SoundEvent.createVariableRangeEvent(id);

                CodeClient.MC.level.playSeededSound(
                        player,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        event,
                        SoundSource.MASTER,
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
