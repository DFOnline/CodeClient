package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.action.None;
import dev.dfonline.codeclient.action.impl.GoTo;
import dev.dfonline.codeclient.command.Command;
import dev.dfonline.codeclient.location.Dev;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.entity.SignText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CommandJump extends Command {
    private static void jump(JumpType type, String name) {
        if (CodeClient.location instanceof Dev dev && CodeClient.currentAction instanceof None) {
            @Nullable HashMap<BlockPos, SignText> results;
            // functions/processes in diamondfire are case-sensitive, and you can have two functions with the same name if they have different cases.
            if (type == JumpType.FUNCTION || type == JumpType.PROCESS) {
                results = dev.scanForSigns(type.pattern, Pattern.compile("^" + Pattern.quote(name) + "$"));
            } else {
                // however, events do not have this problem and the case of what you type shouldn't matter.
                results = dev.scanForSigns(type.pattern, Pattern.compile("^" + Pattern.quote(name) + "$", Pattern.CASE_INSENSITIVE));
            }

            if (results == null) return;
            var first = results.keySet().stream().findFirst();

            // no exact match exists in the plot, so we can run a less restrictive search.
            if (first.isEmpty()) {
                results = dev.scanForSigns(type.pattern, Pattern.compile("^.*" + Pattern.quote(name) + ".*$", Pattern.CASE_INSENSITIVE));
                if (results == null) return;
                first = results.keySet().stream().findFirst();
                if (first.isEmpty()) return; // there is no partial match, so the player doesn't get sent
            }
            CodeClient.currentAction = new GoTo(first.get().toCenterPos(), () -> CodeClient.currentAction = new None());
            CodeClient.currentAction.init();
        }
    }

    public static CompletableFuture<Suggestions> suggestJump(JumpType type, CommandContext<FabricClientCommandSource> ignored, SuggestionsBuilder builder) {
        if (CodeClient.location instanceof Dev dev) {
            var possibilities = new ArrayList<String>();

            for (var lineStarter : dev.getLineStartCache().values()) {
                if (type.pattern.matcher(lineStarter.getMessage(0, false).getString()).matches())
                    possibilities.add(lineStarter.getMessage(1, false).getString());
            }

            for (String possibility : possibilities) {
                if (possibility.toLowerCase().contains(builder.getRemainingLowerCase()))
                    builder.suggest(possibility, Text.literal(type.name().toLowerCase()));
            }
        }
        return CompletableFuture.completedFuture(builder.build());
    }

    @Override
    public String name() {
        return "jump";
    }

    @Override
    public String[] aliases() {
        return new String[]{"goto"};
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd) {
        return cmd.then(literal("player").then(argument("name", greedyString()).suggests((context, builder) -> suggestJump(JumpType.PLAYER_EVENT, context, builder)).executes(context -> {
                    var name = context.getArgument("name", String.class);
                    jump(JumpType.PLAYER_EVENT, name);
                    return 0;
                })))
                .then(literal("entity").then(argument("name", greedyString()).suggests((context, builder) -> suggestJump(JumpType.ENTITY_EVENT, context, builder)).executes(context -> {
                    var name = context.getArgument("name", String.class);
                    jump(JumpType.ENTITY_EVENT, name);
                    return 0;
                })))
                .then(literal("func").then(argument("name", greedyString()).suggests((context, builder) -> suggestJump(JumpType.FUNCTION, context, builder)).executes(context -> {
                    var name = context.getArgument("name", String.class);
                    jump(JumpType.FUNCTION, name);
                    return 0;
                })))
                .then(literal("proc").then(argument("name", greedyString()).suggests((context, builder) -> suggestJump(JumpType.PROCESS, context, builder)).executes(context -> {
                    var name = context.getArgument("name", String.class);
                    jump(JumpType.PROCESS, name);
                    return 0;
                })));
    }

    public enum JumpType {
        PLAYER_EVENT("PLAYER EVENT"),
        ENTITY_EVENT("ENTITY EVENT"),
        FUNCTION("FUNCTION"),
        PROCESS("PROCESS"),
        ANY("(((PLAYER)|(ENTITY)) EVENT)|(FUNCTION)|(PROCESS)");

        public final Pattern pattern;

        JumpType(String scan) {
            pattern = Pattern.compile("^" + scan + "$");
        }
    }
}
