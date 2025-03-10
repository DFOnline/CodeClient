package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.action.None;
import dev.dfonline.codeclient.action.impl.GoTo;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Backwarp {

    public static class BackwarpResult {
        public boolean success;
        public Text message;

        public BackwarpResult(boolean suc, Text msg) {
            success = suc;
            message = msg;
        }
    }


    public static BackwarpResult to(Dev dev, String backType, String query) {

        var results = dev.scanForSigns(Pattern.compile("^" + backType + "$"), Pattern.compile("^" + Pattern.quote(query) + "$", Pattern.CASE_INSENSITIVE));

        if (results == null || results.isEmpty()) {
            return new BackwarpResult(false, Text.translatable("codeclient.backwarp.no_results"));
        }

        if (results.keySet().size() == 1) {
            results.forEach((pos, text) -> {
                CodeClient.currentAction = new GoTo(pos.toCenterPos(), () -> CodeClient.currentAction = new None());
                CodeClient.currentAction.init();
            });
            return new BackwarpResult(false, null);
        }

        var message = Text.translatable("codeclient.backwarp.results");

        var sortedKeys = results.keySet();

        Stream<BlockPos> posStream = sortedKeys.stream().sorted(Comparator.comparingDouble((value -> value.toCenterPos().distanceTo(CodeClient.MC.player.getPos()))));

        posStream.forEach(pos -> {
            Vec3d plotPos = pos.toCenterPos().subtract(dev.getPos());

            String plotCoords = "%f %f %f".formatted(plotPos.x, plotPos.y, plotPos.z);

            var action = Text.empty().append(" [⏼]").setStyle(Style.EMPTY
                    .withColor(Formatting.GREEN)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/plot tp %s", plotCoords)))
                    // i have these as plot coords for now but they were originally world i believe
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("codeclient.search.hover.teleport", plotPos.getX(), plotPos.getY(), plotPos.getZ())))
            );

            int distance = (int) CodeClient.MC.player.getPos().distanceTo(pos.toCenterPos());

            var entry = Text.empty().append("\n • ").formatted(Formatting.GREEN)
                    .append(Text.empty().append(distance + " blocks away").formatted(Formatting.WHITE))
                    .append(action);
            message.append(entry);
        });

        return new BackwarpResult(true, message);
    }
}
