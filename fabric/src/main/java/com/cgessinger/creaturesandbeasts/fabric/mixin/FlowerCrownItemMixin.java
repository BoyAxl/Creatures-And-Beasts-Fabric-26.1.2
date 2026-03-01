package com.cgessinger.creaturesandbeasts.fabric.mixin;

import com.cgessinger.creaturesandbeasts.client.armor.render.FlowerCrownRenderer;
import com.cgessinger.creaturesandbeasts.fabric.client.FabricArmorRenderer;
import com.cgessinger.creaturesandbeasts.items.FlowerCrownItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import org.spongepowered.asm.mixin.Mixin;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(FlowerCrownItem.class)
public abstract class FlowerCrownItemMixin extends ArmorItem implements GeoItem {
    public FlowerCrownItemMixin(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    @Override
    public Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new FabricArmorRenderer(FlowerCrownRenderer::new));
    }
}
