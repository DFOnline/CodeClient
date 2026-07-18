package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.command.Command;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class CommandCalc extends Command {

    @Override
    public String name() {
        return "calc";
    }


    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandBuildContext registryAccess) {
        return cmd.then(argument("calc", greedyString())
                .executes(context -> {
                    String expr = context.getArgument("calc", String.class);
                    if (CodeClient.MC.getConnection() == null) return -1;

                    try {
                        String result = String.valueOf(calc(expr));
                        Component message = Component.translatable("codeclient.command.calc.success", expr, result)
                                        .withStyle(Style.EMPTY
                                        .withHoverEvent(new HoverEvent.ShowText(Component.translatable("codeclient.hover.click_to_copy")))
                                        .withClickEvent(new ClickEvent.CopyToClipboard(result)));

                        Utility.sendMessage(message, ChatType.SUCCESS);
                        
                    } catch (Exception e) {
                        Utility.sendMessage(Component.translatable("codeclient.command.calc.failure", expr), ChatType.FAIL);
                    }
                    return 0;
                }));
    }

    public static double calc(String exp) throws Exception {
        String result = exp.toLowerCase();

        if (result.contains("(")) {
            int open = -1;
            int close;
            int pos = 0;
            while (pos < result.length()) {
                if (result.charAt(pos) == '(') {
                    open = pos;
                } else if (result.charAt(pos) == ')') {
                    if (open != -1) {
                        close = pos;

                        String pre = result.substring(0, open);
                        String res = String.valueOf(
                                calcPart(result.substring(open + 1, close)));
                        String post = result.substring(close + 1);

                        return calc(pre + res + post);

                    } else {
                        throw new Exception("Invalid Brackets");
                    }
                }

                pos++;
            }
            throw new Exception("Invalid Brackets");
        }

        return calcPart(result);
    }

    public static double calcPart(String exp) {
        String texp = exp.replaceAll(" ", "");
        String old = null;

        while (!Objects.equals(old, texp)) {
            old = texp;

            Matcher powm = Pattern.compile(
                    "(?<n1>-?(\\.\\d+|\\d+\\.\\d+|\\d+))\\^(?<n2>-?(\\.\\d+|\\d+\\.\\d+|\\d+))"
            ).matcher(texp);
            Matcher mulm = Pattern.compile(
                    "(?<n1>-?(\\.\\d+|\\d+\\.\\d+|\\d+))(?<e>\\*|\\/|%)(?<n2>-?(\\.\\d+|\\d+\\.\\d+|\\d+))"
            ).matcher(texp);
            Matcher addm = Pattern.compile(
                    "(?<n1>-?(\\.\\d+|\\d+\\.\\d+|\\d+))(?<e>\\+|\\-)(?<n2>-?(\\.\\d+|\\d+\\.\\d+|\\d+))"
            ).matcher(texp);

            if (powm.find()) {
                double num1 = Double.parseDouble(powm.group("n1"));
                double num2 = Double.parseDouble(powm.group("n2"));
                texp = powm.replaceFirst(BigDecimal.valueOf(Math.pow(num1, num2)).toPlainString());
            } else if (mulm.find()) {
                double num1 = Double.parseDouble(mulm.group("n1"));
                double num2 = Double.parseDouble(mulm.group("n2"));
                if (Objects.equals(mulm.group("e"), "*")) {
                    texp = mulm.replaceFirst(new BigDecimal(num1 * num2).toPlainString());
                } else if (Objects.equals(mulm.group("e"), "%")) {
                    texp = mulm.replaceFirst(new BigDecimal(num1 % num2).toPlainString());
                } else {
                    texp = mulm.replaceFirst(new BigDecimal(num1 / num2).toPlainString());
                }


            } else if (addm.find()) {
                double num1 = Double.parseDouble(addm.group("n1"));
                double num2 = Double.parseDouble(addm.group("n2"));
                if (Objects.equals(addm.group("e"), "+")) {
                    texp = addm.replaceFirst(new BigDecimal(num1 + num2).toPlainString());
                } else {
                    texp = addm.replaceFirst(new BigDecimal(num1 - num2).toPlainString());
                }
            }
        }

        return Double.parseDouble(texp);
    }
}
