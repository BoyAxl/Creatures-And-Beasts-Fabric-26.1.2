package com.cgessinger.creaturesandbeasts.client.gui.screens.inventory;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.containers.CinderFurnaceContainer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.AbstractFurnaceRecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.gui.screens.recipebook.SmeltingRecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

@Environment(EnvType.CLIENT)
public class CinderFurnaceScreen extends AbstractContainerScreen<CinderFurnaceContainer> implements RecipeUpdateListener {
    public final AbstractFurnaceRecipeBookComponent recipeBookComponent;
    private boolean widthTooNarrow;
    private final ResourceLocation texture = CreaturesAndBeasts.id("textures/gui/container/cinder_furnace.png");;

    public CinderFurnaceScreen(CinderFurnaceContainer cinderFurnaceContainer, Inventory inventory, Component component) {
        super(cinderFurnaceContainer, inventory, component);
        this.recipeBookComponent = new SmeltingRecipeBookComponent();
    }

    public void init() {
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    public void containerTick() {
        super.containerTick();
        this.recipeBookComponent.tick();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.renderBg(guiGraphics, partialTick, mouseX, mouseY);
            this.recipeBookComponent.render(guiGraphics, mouseX, mouseY, partialTick);
        } else {
            this.recipeBookComponent.render(guiGraphics, mouseX, mouseY, partialTick);
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            this.recipeBookComponent.renderGhostRecipe(guiGraphics, this.leftPos, this.topPos, true, partialTick);
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.recipeBookComponent.renderTooltip(guiGraphics, this.leftPos, this.topPos, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        guiGraphics.blit(this.texture, i, j, 0, 0, this.imageWidth, this.imageHeight);
        guiGraphics.blit(this.texture, i + 57, j + 37, 176, 0, 14, 14);
        int l = this.menu.getCookingProgress();
        guiGraphics.blit(this.texture, i + 79, j + 35, 176, 14, l + 1, 16);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    public boolean mouseClicked(double x, double y, int z) {
        if (this.recipeBookComponent.mouseClicked(x, y, z)) {
            return true;
        } else {
            return this.widthTooNarrow && this.recipeBookComponent.isVisible() || super.mouseClicked(x, y, z);
        }
    }

    @Override
    protected void slotClicked(Slot slot, int p_97849_, int p_97850_, ClickType clickType) {
        super.slotClicked(slot, p_97849_, p_97850_, clickType);
        this.recipeBookComponent.slotClicked(slot);
    }

    @Override
    public boolean keyPressed(int p_97844_, int p_97845_, int p_97846_) {
        return !this.recipeBookComponent.keyPressed(p_97844_, p_97845_, p_97846_) && super.keyPressed(p_97844_, p_97845_, p_97846_);
    }

    @Override
    protected boolean hasClickedOutside(double p_97838_, double p_97839_, int p_97840_, int p_97841_, int p_97842_) {
        boolean flag = p_97838_ < (double)p_97840_ || p_97839_ < (double)p_97841_ || p_97838_ >= (double)(p_97840_ + this.imageWidth) || p_97839_ >= (double)(p_97841_ + this.imageHeight);
        return this.recipeBookComponent.hasClickedOutside(p_97838_, p_97839_, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, p_97842_) && flag;
    }

    @Override
    public boolean charTyped(char p_97831_, int p_97832_) {
        return this.recipeBookComponent.charTyped(p_97831_, p_97832_) || super.charTyped(p_97831_, p_97832_);
    }

    @Override
    public void recipesUpdated() {
        this.recipeBookComponent.recipesUpdated();
    }

    @Override
    public RecipeBookComponent getRecipeBookComponent() {
        return this.recipeBookComponent;
    }

    @Override
    public void removed() {
//        this.recipeBookComponent.removed(); // TODO: ?
        super.removed();
    }
}
