package com.tacz.guns.client.init;

import com.tacz.guns.GunMod;
import com.tacz.guns.api.client.other.ThirdPersonManager;
import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import com.tacz.guns.client.gui.overlay.HeatBarOverlay;
import com.tacz.guns.client.gui.overlay.InteractKeyTextOverlay;
import com.tacz.guns.client.gui.overlay.KillAmountOverlay;
import com.tacz.guns.client.input.*;
import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.client.tooltip.ClientAmmoBoxTooltip;
import com.tacz.guns.client.tooltip.ClientAttachmentItemTooltip;
import com.tacz.guns.client.tooltip.ClientBlockItemTooltip;
import com.tacz.guns.client.tooltip.ClientGunTooltip;
import com.tacz.guns.compat.ar.ARCompat;
import com.tacz.guns.compat.controllable.ControllableCompat;
import com.tacz.guns.compat.immediatelyfast.ImmediatelyFastCompat;
import com.tacz.guns.compat.playeranimator.PlayerAnimatorCompat;
import com.tacz.guns.compat.shouldersurfing.ShoulderSurfingCompat;
import com.tacz.guns.compat.zoomify.ZoomifyCompat;
import com.tacz.guns.inventory.tooltip.AmmoBoxTooltip;
import com.tacz.guns.inventory.tooltip.AttachmentItemTooltip;
import com.tacz.guns.inventory.tooltip.BlockItemTooltip;
import com.tacz.guns.inventory.tooltip.GunTooltip;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

@Environment(EnvType.CLIENT)
public class ClientSetupEvent {
    // KeyMapping agora exige uma Category registrada em vez de uma chave de tradução crua
    public static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "tacz"));

    public static void init() {
        registerKeyMappings();
        registerClientTooltips();
        registerGuiOverlays();
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> onClientSetup());
        onClientResourceReload();
    }

    public static void registerKeyMappings() {
        // 注册键位
        // ModernKeyBinding (committee.nova.mkb) ainda não tem build pra 26.2 (ver build.gradle):
        // fica só o registro base do Fabric, sem contexto de conflito/modificador customizado
        registerKeyBinding(InspectKey.INSPECT_KEY);
        registerKeyBinding(ReloadKey.RELOAD_KEY);
        registerKeyBinding(ShootKey.SHOOT_KEY);
        registerKeyBinding(InteractKey.INTERACT_KEY);
        registerKeyBinding(FireSelectKey.FIRE_SELECT_KEY);
        registerKeyBinding(AimKey.AIM_KEY);
        registerKeyBinding(CrawlKey.CRAWL_KEY);
        registerKeyBinding(RefitKey.REFIT_KEY);
        registerKeyBinding(ZoomKey.ZOOM_KEY);
        registerKeyBinding(MeleeKey.MELEE_KEY);
        registerKeyBinding(ConfigKey.OPEN_CONFIG_KEY);
    }

    private static void registerKeyBinding(KeyMapping keyMapping) {
        KeyMappingHelper.registerKeyMapping(keyMapping);
    }

    public static void registerClientTooltips() {
        // 注册文本提示
        ClientTooltipComponentCallback.EVENT.register(tooltip -> {
            if (tooltip instanceof GunTooltip gunTooltip) {
                return new ClientGunTooltip(gunTooltip);
            }
            if (tooltip instanceof AmmoBoxTooltip ammoBoxTooltip) {
                return new ClientAmmoBoxTooltip(ammoBoxTooltip);
            }
            if (tooltip instanceof AttachmentItemTooltip attachmentItemTooltip) {
                return new ClientAttachmentItemTooltip(attachmentItemTooltip);
            }
            if (tooltip instanceof BlockItemTooltip blockItemTooltip) {
                return new ClientBlockItemTooltip(blockItemTooltip);
            }
            return null;
        });
    }

    public static void registerGuiOverlays() {
        // 注册 HUD (HudRenderCallback foi substituído por HudElementRegistry)
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "gun_hud"), GunHudOverlay::extractRenderState);
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "heat_bar"), HeatBarOverlay::extractRenderState);
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "interact_key_text"), InteractKeyTextOverlay::extractRenderState);
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "kill_amount"), KillAmountOverlay::extractRenderState);
    }

    public static void onClientSetup() {
        // 注册自己的的硬编码第三人称动画
        ThirdPersonManager.registerDefault();

        // ColorProviderRegistry (tint de item) e ItemProperties (custom model property) sumiram
        // na 26.2 - viraram parte do mesmo sistema novo de item model orientado a JSON+registry
        // (ItemTintSource/SelectItemModelProperty etc, ver client/color/item e
        // client/renderer/item/properties na jar) que ainda não foi portado, junto com o gap do
        // SpecialModelRenderer (ver build.gradle). AmmoBoxItem.getColor/getStatue continuam
        // existindo prontos pra quando isso for implementado.

        // 初始化自己的枪包下载器
//       ClientGunPackDownloadManager.init();

//        // 与 player animator 的兼容
//       PlayerAnimatorCompat.init();

        // 与 Shoulder Surfing Reloaded 的兼容
        ShoulderSurfingCompat.init();

        // 与 Controllable 的兼容
        ControllableCompat.init();

        // 与 Accelerated Rendering 的兼容
        ARCompat.init();

        ZoomifyCompat.init();
        ImmediatelyFastCompat.init();
    }

    public static void onClientResourceReload() {
        PlayerAnimatorCompat.init();

        ClientAssetsManager.INSTANCE.reloadAndRegister(ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)::registerReloadListener);
        if (PlayerAnimatorCompat.isInstalled()) {
            PlayerAnimatorCompat.registerReloadListener(ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)::registerReloadListener);
        }
    }
}
