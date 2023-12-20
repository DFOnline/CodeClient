package dev.dfonline.codeclient.hypercube.template;

import com.google.gson.JsonObject;

public class Bracket extends TemplateBlock {
    public Bracket() {
        this(false,false);
    }

    public Bracket(boolean closing) {
        this(closing,false);
    }
    public Bracket(boolean closing, boolean sticky) {
        this.id = "bracket";
        this.block = null;
        this.action = null;
        this.direct = closing ? "close" : "open";
        this.type = sticky ? "repeat" : "norm";
    }

    @Override
    public JsonObject toJsonObject() {
        var obj = super.toJsonObject();
        obj.addProperty("direct",direct);
        obj.addProperty("type",type);
        return obj;
    }
}
