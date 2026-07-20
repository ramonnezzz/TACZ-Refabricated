package com.tacz.guns.client.gui.components.smith;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Substitui o antigo construtor de 7-int (x,y,w,h,u,v,vDiffTex,texture,onPress) de ImageButton,
 * que virou WidgetSprites (arquivos de textura inteiros, não sub-regiões de uma folha) - esse
 * mod usa uma única folha com várias regiões, então blita a sub-região diretamente.
 */
public class TextureImageButton extends Button {
    private final Identifier texture;
    private final int u;
    private final int v;
    private final int vDiffTex;

    public TextureImageButton(int x, int y, int width, int height, int u, int v, int vDiffTex, Identifier texture, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.vDiffTex = vDiffTex;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int vOffset = this.isHoveredOrFocused() ? this.vDiffTex : 0;
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, this.getX(), this.getY(), u, v + vOffset, this.width, this.height, 256, 256);
    }
}
