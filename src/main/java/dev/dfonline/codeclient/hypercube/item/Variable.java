package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;

public class Variable extends NamedItem {
    private Scope scope;

    public Variable(Item material, JsonObject var) {
        super(material, var);
        this.scope = Scope.valueOf(data.get("scope").getAsString());
    }

    public void setScope(Scope scope) {
        this.scope = scope;
        data.addProperty("scope",scope.name());
    }

    public Scope getScope() {
        return scope;
    }
}
