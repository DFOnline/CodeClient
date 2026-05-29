package dev.dfonline.codeclient.command.argument;

import com.google.common.collect.Collections2;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;

import java.util.concurrent.CompletableFuture;

public class PlayerArgumentType implements ArgumentType<String> {

    private PlayerArgumentType() {}

    public static PlayerArgumentType player() {
        return new PlayerArgumentType();
    }

    @Override
    public String parse(StringReader stringReader) throws CommandSyntaxException {
        return stringReader.readUnquotedString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (context.getSource() instanceof FabricClientCommandSource clientCommandSource)
            return CommandSource.suggestMatching(Collections2.transform(
                    clientCommandSource.getPlayer().networkHandler.getPlayerList(),
                    playerEntry -> playerEntry.getProfile().name()
            ), builder);

        return builder.buildFuture();
    }
}
