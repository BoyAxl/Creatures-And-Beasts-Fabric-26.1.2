package com.cgessinger.creaturesandbeasts.forge.mixin;

import com.cgessinger.creaturesandbeasts.client.armor.render.FlowerCrownRenderer;
import com.cgessinger.creaturesandbeasts.forge.client.ForgeArmorRenderer;
import com.cgessinger.creaturesandbeasts.items.FlowerCrownItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.function.Consumer;

@Mixin(FlowerCrownItem.class)
public abstract class FlowerCrownItemMixin extends ArmorItem implements GeoItem {
    public FlowerCrownItemMixin(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new ForgeArmorRenderer(FlowerCrownRenderer::new));
    }
}
