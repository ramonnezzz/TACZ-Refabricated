package com.tacz.guns.client.resource.serialize;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;

import java.lang.reflect.Type;

// ItemTransforms.Deserializer sumiu na 26.2 (virou record puro) - reimplementa a leitura das
// mesmas chaves de perspectiva que os JSONs de display deste mod já usavam. "fixedFromBottom"
// é um slot novo do record sem chave correspondente nos JSONs existentes - fica sempre
// NO_TRANSFORM, igual ao comportamento antigo pra chaves ausentes.
public class ItemTransformsSerializer implements JsonDeserializer<ItemTransforms> {
    @Override
    public ItemTransforms deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        return new ItemTransforms(
                getTransform(object, context, "thirdperson_lefthand"),
                getTransform(object, context, "thirdperson_righthand"),
                getTransform(object, context, "firstperson_lefthand"),
                getTransform(object, context, "firstperson_righthand"),
                getTransform(object, context, "head"),
                getTransform(object, context, "gui"),
                getTransform(object, context, "ground"),
                getTransform(object, context, "fixed"),
                ItemTransform.NO_TRANSFORM
        );
    }

    private static ItemTransform getTransform(JsonObject object, JsonDeserializationContext context, String key) {
        return object.has(key) ? context.deserialize(object.get(key), ItemTransform.class) : ItemTransform.NO_TRANSFORM;
    }
}
