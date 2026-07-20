package com.tacz.guns.client.gui.toast;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

// todo essa classe nunca teve o render() implementado de fato (já vinha inteiro comentado) -
// só implementa o mínimo pra compilar contra a interface Toast nova, sem recriar o desenho
@Environment(EnvType.CLIENT)
public class GunLevelUpToast implements Toast {
    private final Component title;
    private final Component subTitle;
    private final ItemStack icon;
    private Visibility visibility = Visibility.SHOW;

    public GunLevelUpToast(ItemStack icon, Component titleComponent, @Nullable Component subtitle) {
        this.icon = icon;
        this.title = titleComponent;
        this.subTitle = subtitle;
    }

    @NotNull
    @Override
    public Visibility getWantedVisibility() {
        return visibility;
    }

    @Override
    public void update(@NotNull ToastManager toastManager, long timeSinceLastVisible) {
        visibility = timeSinceLastVisible >= 5000L ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor guiGraphics, @NotNull Font font, long timeSinceLastVisible) {
    }
}
