package dev.dfonline.codeclient.command.impl.polytope;

import java.util.HashMap;
import java.util.Map;

public class TextReplacer {

    public static final Map<Character, Character> smallCaps = new HashMap<>();

    private static void pcap(char from, char to) {
        smallCaps.put(from, to);
    }

    public static final Map<Character, Character> bubbles = new HashMap<>();

    private static void pbub(char from, char to) {
        bubbles.put(from, to);
    }

    static {

        pcap('a', 'ᴀ');
        pcap('b', 'ʙ');
        pcap('c', 'ᴄ');
        pcap('d', 'ᴅ');
        pcap('e', 'ᴇ');
        pcap('f', 'ꜰ');
        pcap('g', 'ɢ');
        pcap('h', 'ʜ');
        pcap('i', 'ɪ');
        pcap('j', 'ᴊ');
        pcap('k', 'ᴋ');
        pcap('l', 'ʟ');
        pcap('m', 'ᴍ');
        pcap('n', 'ɴ');
        pcap('o', 'ᴏ');
        pcap('p', 'ᴘ');
        pcap('q', '\uA7AF');
        pcap('r', 'ʀ');
        pcap('s', 'ꜱ');
        pcap('t', 'ᴛ');
        pcap('u', 'ᴜ');
        pcap('v', 'ᴠ');
        pcap('w', 'ᴡ');
        pcap('x', 'x');
        pcap('y', 'ʏ');
        pcap('z', 'ᴢ');

        pbub('A', 'Ⓐ');
        pbub('B', 'Ⓑ');
        pbub('C', 'Ⓒ');
        pbub('D', 'Ⓓ');
        pbub('E', 'Ⓔ');
        pbub('F', 'Ⓕ');
        pbub('G', 'Ⓖ');
        pbub('H', 'Ⓗ');
        pbub('I', 'Ⓘ');
        pbub('J', 'Ⓙ');
        pbub('K', 'Ⓚ');
        pbub('L', 'Ⓛ');
        pbub('M', 'Ⓜ');
        pbub('N', 'Ⓝ');
        pbub('O', 'Ⓞ');
        pbub('P', 'Ⓟ');
        pbub('Q', 'Ⓠ');
        pbub('R', 'Ⓡ');
        pbub('S', 'Ⓢ');
        pbub('T', 'Ⓣ');
        pbub('U', 'Ⓤ');
        pbub('V', 'Ⓥ');
        pbub('W', 'Ⓦ');
        pbub('X', 'Ⓧ');
        pbub('Y', 'Ⓨ');
        pbub('Z', 'Ⓩ');

        pbub('a', 'ⓐ');
        pbub('b', 'ⓑ');
        pbub('c', 'ⓒ');
        pbub('d', 'ⓓ');
        pbub('e', 'ⓔ');
        pbub('f', 'ⓕ');
        pbub('g', 'ⓖ');
        pbub('h', 'ⓗ');
        pbub('i', 'ⓘ');
        pbub('j', 'ⓙ');
        pbub('k', 'ⓚ');
        pbub('l', 'ⓛ');
        pbub('m', 'ⓜ');
        pbub('n', 'ⓝ');
        pbub('o', 'ⓞ');
        pbub('p', 'ⓟ');
        pbub('q', 'ⓠ');
        pbub('r', 'ⓡ');
        pbub('s', 'ⓢ');
        pbub('t', 'ⓣ');
        pbub('u', 'ⓤ');
        pbub('v', 'ⓥ');
        pbub('w', 'ⓦ');
        pbub('x', 'ⓧ');
        pbub('y', 'ⓨ');
        pbub('z', 'ⓩ');

        pbub('0', '⓪');
        pbub('1', '①');
        pbub('2', '②');
        pbub('3', '③');
        pbub('4', '④');
        pbub('5', '⑤');
        pbub('6', '⑥');
        pbub('7', '⑦');
        pbub('8', '⑧');
        pbub('9', '⑨');

    }

    public static final String[] rainbowColors = new String[]{
            "&c", "&6", "&e", "&a", "&b", "&5", "&d"
    };

    public static String replaceFromMapping(String sourceString, Map<Character, Character> mapping) {
        char[] replacedBuffer = new char[sourceString.length()];

        for (int i = 0; i < replacedBuffer.length; i++) {
            char srcChar = sourceString.charAt(i);
            Character replacementChar = mapping.get(srcChar);
            replacedBuffer[i] = ((replacementChar == null) ? srcChar : replacementChar);
        }

        return new String(replacedBuffer);
    }

}
