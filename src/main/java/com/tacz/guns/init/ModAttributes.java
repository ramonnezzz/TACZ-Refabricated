package com.tacz.guns.init;

import com.tacz.guns.GunMod;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class ModAttributes {
    public static void init() {

    }

    // AttributeSupplier.Builder#add e LivingEntity#getAttribute passam a exigir
    // Holder<Attribute>, não mais Attribute cru - Registry.registerForHolder devolve o Holder
    // já pronto em vez do valor registrado direto
    public static final Holder<Attribute> BULLET_RESISTANCE = register("tacz.bullet_resistance",
            new RangedAttribute("attribute.name.tacz.bullet_resistance", 0.0D, 0.0D, 1.0D).setSyncable(true));
//
//    public static final Attribute WEIGHT_CAPACITY = register("tacz.weight_capacity",
//             new RangedAttribute("attribute.name.tacz.weight_capacity", 0.0D, -1024D, 1024.0D).setSyncable(true));

    private static Holder<Attribute> register(String name, Attribute attribute) {
        return Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, Identifier.fromNamespaceAndPath(GunMod.MOD_ID, name), attribute);
    }
}
