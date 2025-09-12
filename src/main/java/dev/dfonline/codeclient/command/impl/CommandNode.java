package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.command.Command;
import dev.dfonline.codeclient.config.Config;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class CommandNode extends Command {

    private static final Map<String, String> NODE_MAP = new HashMap<>();
    private static final Pattern PRIVATE_NODE_PATTERN = Pattern.compile("^p(\\d+)$");

    public void loadNodes() {
        NODE_MAP.clear();
        NODE_MAP.put("1", "node1");
        NODE_MAP.put("2", "node2");
        NODE_MAP.put("3", "node3");
        NODE_MAP.put("4", "node4");
        NODE_MAP.put("5", "node5");
        NODE_MAP.put("6", "node6");
        NODE_MAP.put("7", "node7");
        NODE_MAP.put("beta", "beta");
        NODE_MAP.put("b", "beta");
        NODE_MAP.put("event", "event");

        if (Config.getConfig().DevNodes) {
            NODE_MAP.put("dev", "dev");
            NODE_MAP.put("dev1", "dev");
            NODE_MAP.put("dev2", "dev2");
            NODE_MAP.put("dev3", "dev3");

            NODE_MAP.put("d", "dev");
            NODE_MAP.put("d1", "dev");
            NODE_MAP.put("d2", "dev2");
            NODE_MAP.put("d3", "dev3");
        }
    }

    @Override
    public String name() {
        return "node";
    }


    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandRegistryAccess registryAccess) {
        return cmd.then(argument("node", word())
                .suggests((context, builder) -> {
                    loadNodes(); // Refresh in case of config change.
                    NODE_MAP.keySet().forEach(builder::suggest);
                    return builder.buildFuture();
                })
                .executes(context -> {
                    String key = context.getArgument("node", String.class);
                    if (CodeClient.MC.getNetworkHandler() == null) return -1;

                    String serverId = NODE_MAP.getOrDefault(key, key);

                    // If the server ID is not found, as a last resort,
                    // try to process "pX" syntax as a private node shorthand.
                    if (serverId.equals(key)) {
                        Matcher privateNodeMatcher = PRIVATE_NODE_PATTERN.matcher(key.trim());
                        if (privateNodeMatcher.find()) {
                            serverId = "private" + privateNodeMatcher.group(1);
                        }
                    }

                    CodeClient.MC.getNetworkHandler().sendCommand("server " + serverId);
                    return 0;
                }));
    }
}
