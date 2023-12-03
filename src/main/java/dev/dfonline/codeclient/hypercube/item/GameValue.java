package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.hypercube.Target;
import net.minecraft.item.Item;

public class GameValue extends VarItem {
    private String type;
    private Target target;

    public GameValue(Item material, JsonObject var) {
        super(material, var);
        this.type = data.get("type").getAsString();
        this.target = Target.valueOf(data.get("target").getAsString());
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        this.data.addProperty("type",type);
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
        this.data.addProperty("target",target.name());
    }
}
