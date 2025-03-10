package dev.dfonline.codeclient.dev;

import net.minecraft.block.entity.SignBlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CodeSearcher {

    private static String searchStr = "";
    private static int searchLineIdx;

    private static final List<String> exclusions = new ArrayList<>();

    public static void setSearchString(String str, int searchIdx) {
        searchStr = str;
        searchLineIdx = searchIdx;

        if (searchStr.isEmpty()) {
            exclusions.clear();
        }
    }

    public static void addExclusion(String excl) {
        exclusions.add(excl.toLowerCase());
    }


    public static boolean shouldHighlight(SignBlockEntity sign) {
        String relevantLine = sign.getFrontText().getMessage(searchLineIdx, false).getString().toLowerCase(Locale.ROOT);
        if (!searchStr.isEmpty() && relevantLine.contains(searchStr.toLowerCase(Locale.ROOT))) {
            for (String excl : exclusions) {
                if (relevantLine.contains(excl)) return false;
            }
            return true;
        }
        return false;

    }

    public static void tryCache(SignBlockEntity sign) {

    }

}
