package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class BucketVar extends NamedItem {
    /**
     * the Bucket this var is in. it's saved as `key` in the json.
     */
    private String key;
    private String namespace_type;
    private String namespace_alias;

    public BucketVar(JsonObject var) {
        super(var);
        this.key = data.get("key").getAsString();
        this.namespace_type = data.get("namespace_type").getAsString();
        this.namespace_alias = data.get("namespace_alias").getAsString();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
        data.addProperty("key", key);
    }

    @Override
    public String getId() {
        return "bucket_var";
    }

    @Override
    protected Item getIconItem() {
        return Items.POPPED_CHORUS_FRUIT;
    }
}
