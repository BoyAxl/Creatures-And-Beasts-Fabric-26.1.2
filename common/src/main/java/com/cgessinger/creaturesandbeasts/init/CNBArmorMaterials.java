package com.cgessinger.creaturesandbeasts.init;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.Map;

public final class CNBArmorMaterials {
    public static final ArmorMaterial FLOWER_CROWN = new ArmorMaterial(
            2,
            Map.of(ArmorType.HELMET, 1, ArmorType.CHESTPLATE, 2, ArmorType.LEGGINGS, 3, ArmorType.BOOTS, 1),
            5,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            0.0F,
            0.0F,
            CNBTags.Items.MINIPAD_FLOWERS,
            EquipmentAssets.LEATHER
    );
    public static final ArmorMaterial SPORELING_BACKPACK = new ArmorMaterial(
            3,
            Map.of(ArmorType.HELMET, 0, ArmorType.CHESTPLATE, 1, ArmorType.LEGGINGS, 0, ArmorType.BOOTS, 0),
            2,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            0.0F,
            0.0F,
            ItemTags.REPAIRS_LEATHER_ARMOR,
            EquipmentAssets.LEATHER
    );

    private CNBArmorMaterials() {
    }
}
