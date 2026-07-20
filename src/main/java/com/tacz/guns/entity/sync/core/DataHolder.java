package com.tacz.guns.entity.sync.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataHolder {
    // Cardinal Components (CCA) sem build pra 26.2 (ver build.gradle) - virou um
    // fabric-data-attachment-api-v1 AttachmentType (ver DataHolderCapabilityProvider), que
    // persiste via Codec em vez do antigo Component#readFromNbt/writeToNbt. Reaproveita o
    // mesmo formato de NBT de antes (lista de {ClassKey, DataKey, Value}), só embrulhado num
    // Codec no estilo Codec.PASSTHROUGH já usado em GunSmithTableSerializer.
    public static final Codec<DataHolder> CODEC = Codec.PASSTHROUGH.comapFlatMap(dynamic -> {
        Tag tag = dynamic.convert(NbtOps.INSTANCE).getValue();
        DataHolder holder = new DataHolder();
        if (tag instanceof ListTag listTag) {
            holder.deserializeNBT(listTag);
        }
        return DataResult.success(holder);
    }, holder -> new Dynamic<>(NbtOps.INSTANCE, holder.serializeNBT()));

    public Map<SyncedDataKey<?, ?>, DataEntry<?, ?>> dataMap = new HashMap<>();
    private boolean dirty = false;

    public ListTag serializeNBT() {
        ListTag list = new ListTag();
        this.dataMap.forEach((key, entry) -> {
            if (key.save()) {
                CompoundTag keyTag = new CompoundTag();
                keyTag.putString("ClassKey", key.classKey().id().toString());
                keyTag.putString("DataKey", key.id().toString());
                keyTag.put("Value", entry.writeValue());
                list.add(keyTag);
            }
        });
        return list;
    }

    public void deserializeNBT(ListTag listTag) {
        this.dataMap.clear();
        listTag.forEach(entryTag -> {
            CompoundTag keyTag = (CompoundTag) entryTag;
            Identifier classKey = Identifier.tryParse(keyTag.getString("ClassKey").orElse(""));
            Identifier dataKey = Identifier.tryParse(keyTag.getString("DataKey").orElse(""));
            Tag value = keyTag.get("Value");
            SyncedClassKey<?> syncedClassKey = SyncedEntityData.instance().getClassKey(classKey);
            if (syncedClassKey == null) {
                return;
            }
            SyncedDataKey<?, ?> syncedDataKey = SyncedEntityData.instance().getKey(syncedClassKey, dataKey);
            if (syncedDataKey == null || !syncedDataKey.save()) {
                return;
            }
            DataEntry<?, ?> entry = new DataEntry<>(syncedDataKey);
            entry.readValue(value);
            this.dataMap.put(syncedDataKey, entry);
        });
    }

    @SuppressWarnings("unchecked")
    public <E extends Entity, T> boolean set(E entity, SyncedDataKey<?, ?> key, T value) {
        DataEntry<E, T> entry = (DataEntry<E, T>) this.dataMap.computeIfAbsent(key, DataEntry::new);
        if (!entry.getValue().equals(value)) {
            boolean dirty = !entity.level().isClientSide() && entry.getKey().syncMode() != SyncedDataKey.SyncMode.NONE;
            entry.setValue(value, dirty);
            this.dirty = dirty;
            return true;
        }
        return false;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <E extends Entity, T> T get(SyncedDataKey<E, T> key) {
        return (T) this.dataMap.computeIfAbsent(key, DataEntry::new).getValue();
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public void clean() {
        this.dirty = false;
        this.dataMap.forEach((key, entry) -> entry.clean());
    }

    public List<DataEntry<?, ?>> gatherDirty() {
        return this.dataMap.values().stream().filter(DataEntry::isDirty).filter(entry -> entry.getKey().syncMode() != SyncedDataKey.SyncMode.NONE).collect(Collectors.toList());
    }

    public List<DataEntry<?, ?>> gatherAll() {
        return this.dataMap.values().stream().filter(entry -> entry.getKey().syncMode() != SyncedDataKey.SyncMode.NONE).collect(Collectors.toList());
    }
}
