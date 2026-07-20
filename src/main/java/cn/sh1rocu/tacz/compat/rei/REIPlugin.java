package cn.sh1rocu.tacz.compat.rei;

import com.tacz.guns.api.item.gun.GunItemManager;
import com.tacz.guns.init.ModItems;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;

// registerItemComparators virou default method de REICommonPlugin (não mais de REIPlugin<P>
// genérico) na 26.2 - a interface base REIPlugin<P> agora só tem hooks de ciclo de vida
public class REIPlugin implements REICommonPlugin {
    @Override
    public void registerItemComparators(ItemComparatorRegistry registry) {
        registry.register(REISubtype.getAmmoSubtype(), ModItems.AMMO);
        registry.register(REISubtype.getAttachmentSubtype(), ModItems.ATTACHMENT);
        registry.register(REISubtype.getAmmoBoxSubtype(), ModItems.AMMO_BOX);
        GunItemManager.getAllGunItems().forEach(item ->
                registry.register(REISubtype.getGunSubtype(), item));
    }


    @Override
    public Class<REICommonPlugin> getPluginProviderClass() {
        return REICommonPlugin.class;
    }
}
