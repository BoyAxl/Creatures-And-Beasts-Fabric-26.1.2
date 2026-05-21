package com.cgessinger.creaturesandbeasts.client.gui.screens.inventory;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.containers.CinderFurnaceContainer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

@Environment(EnvType.CLIENT)
public class CinderFurnaceScreen extends AbstractContainerScreen<CinderFurnaceContainer> {
    private static final Identifier TEXTURE = CreaturesAndBeasts.id("textures/gui/container/cinder_furnace.png");

    public CinderFurnaceScreen(CinderFurnaceContainer cinderFurnaceContainer, Inventory inventory, Component component) {
        super(cinderFurnaceContainer, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int x = this.leftPos;
        int y = this.topPos;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 57, y + 37, 176.0F, 0.0F, 14, 14, 256, 256);

        int cookingProgress = this.menu.getCookingProgress();
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 79, y + 35, 176.0F, 14.0F, cookingProgress + 1, 16, 256, 256);
    }
}
