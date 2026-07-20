package com.tacz.guns.init;

// CCA sem build pra 26.2 - virou AttachmentType (ver DataHolderCapabilityProvider). Não precisa
// mais de registro condicional por entidade nem de invalidar manualmente no EntityRemoveEvent:
// attachments só existem sob demanda (getAttachedOrCreate) e desaparecem com a entidade sozinhos.
public class CapabilityRegistry {
    public static void init() {
    }
}