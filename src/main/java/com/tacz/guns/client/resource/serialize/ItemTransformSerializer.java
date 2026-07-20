package com.tacz.guns.client.resource.serialize;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.lang.reflect.Type;

// ItemTransform.Deserializer sumiu na 26.2 (ItemTransform virou record puro, sem parsing Gson
// próprio) - reimplementa a mesma leitura de "rotation"/"translation"/"scale" (arrays [x,y,z])
// que os JSONs de display deste mod já usavam (formato herdado do modelo de item da vanilla)
public class ItemTransformSerializer implements JsonDeserializer<ItemTransform> {
    private static final Vector3fc IDENTITY = new Vector3f(0, 0, 0);
    private static final Vector3fc UNIT_SCALE = new Vector3f(1, 1, 1);

    @Override
    public ItemTransform deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        Vector3fc rotation = object.has("rotation") ? context.deserialize(object.get("rotation"), Vector3f.class) : IDENTITY;
        Vector3fc translation = object.has("translation") ? context.deserialize(object.get("translation"), Vector3f.class) : IDENTITY;
        Vector3fc scale = object.has("scale") ? context.deserialize(object.get("scale"), Vector3f.class) : UNIT_SCALE;
        return new ItemTransform(rotation, translation, scale);
    }
}
