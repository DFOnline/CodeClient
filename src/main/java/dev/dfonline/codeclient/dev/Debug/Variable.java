package dev.dfonline.codeclient.dev.Debug;

import net.minecraft.text.TextColor;

import java.util.HashMap;
import java.util.Map;

import dev.dfonline.codeclient.hypercube.actiondump.Argument.Type;

public class Variable {
    public ValueType type;
    public String name;
    public String value = null;

    Variable(String name) {
        this.name = name;
    }

    public static enum ValueType {
        Dead("Dead", Type.NONE.color),
        Num("Number", Type.NUMBER.color),
        Txt("Text", Type.TEXT.color),
        Loc("Location", Type.LOCATION.color),
        Item("Item", Type.ITEM.color),
        List("List", Type.LIST.color),
        Pot("Potion Effect", Type.POTION.color),
        Snd("Sound", Type.SOUND.color),
        Pfx("Particle", Type.PARTICLE.color),
        Vec("Vector", Type.VECTOR.color),
        Dict("Dictionary", Type.DICT.color);

        public String name;
        public TextColor color;
        ValueType(String name, TextColor color) {
            this.name = name;
            this.color = color;
        }

        public static Map<String, ValueType> valueTypeMap;
        static {
            HashMap<String, ValueType> map = new HashMap<>();
            for (ValueType type: values()) {
                map.put(type.name, type);
            }
            valueTypeMap = Map.copyOf(map);
        }
    }
}
