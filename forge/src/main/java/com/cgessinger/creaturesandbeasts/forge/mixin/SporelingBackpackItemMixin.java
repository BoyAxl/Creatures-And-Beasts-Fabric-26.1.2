package com.cgessinger.creaturesandbeasts.forge.mixin;

import com.cgessinger.creaturesandbeasts.client.armor.render.SporelingBackpackRenderer;
import com.cgessinger.creaturesandbeasts.forge.client.ForgeArmorRenderer;
import com.cgessinger.creaturesandbeasts.items.SporelingBackpackItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.function.Consumer;

@Mixin(SporelingBackpackItem.class)
public abstract class SporelingBackpackItemMixin extends ArmorItem implements GeoItem {
    public SporelingBackpackItemMixin(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new ForgeArmorRenderer(SporelingBackpackRenderer::new));
    }
}
