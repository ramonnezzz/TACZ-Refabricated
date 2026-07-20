package com.tacz.guns.entity.sync.core;

import com.tacz.guns.GunMod;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

// Cardinal Components (CCA) sem build pra 26.2 - virou um AttachmentType do
// fabric-data-attachment-api-v1 direto em cima de Entity (que já implementa AttachmentTarget),
// sem precisar de wrapper de Component nem de registro condicional por entidade (o attachment
// é criado sob demanda via getAttachedOrCreate). Ver DataHolder#CODEC pro formato de persistência.
public class DataHolderCapabilityProvider {
    public static final AttachmentType<DataHolder> ATTACHMENT = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "synced_entity_data"),
            builder -> builder.initializer(DataHolder::new).persistent(DataHolder.CODEC)
    );
}
