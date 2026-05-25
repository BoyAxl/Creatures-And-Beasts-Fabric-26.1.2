package com.cgessinger.creaturesandbeasts.init;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.items.*;
import com.cgessinger.creaturesandbeasts.util.CNBDeferredRegister;
import com.cgessinger.creaturesandbeasts.util.CNBRegistrySupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.material.Fluids;

public class CNBItems {
    public static final CNBDeferredRegister<Item> ITEMS = CNBDeferredRegister.create(CreaturesAndBeasts.MOD_ID, BuiltInRegistries.ITEM);

    // Food
    public static final CNBRegistrySupplier<Item> APPLE_SLICE = ITEMS.register("apple_slice", () -> new Item(CreaturesAndBeasts.itemProperties("apple_slice")
            .food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.3F).build())));
    public static final CNBRegistrySupplier<FloatingFlowerBlockItem> PINK_WATERLILY = ITEMS.register("pink_waterlily", () -> new FloatingFlowerBlockItem(CNBBlocks.PINK_WATERLILY_BLOCK.get(), CreaturesAndBeasts.itemProperties("pink_waterlily")
            .food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.5F).alwaysEdible().build(), instantHealthFood())));
    public static final CNBRegistrySupplier<FloatingFlowerBlockItem> LIGHT_PINK_WATERLILY = ITEMS.register("light_pink_waterlily", () -> new FloatingFlowerBlockItem(CNBBlocks.LIGHT_PINK_WATERLILY_BLOCK.get(), CreaturesAndBeasts.itemProperties("light_pink_waterlily")
            .food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.5F).alwaysEdible().build(), instantHealthFood())));
    public static final CNBRegistrySupplier<FloatingFlowerBlockItem> YELLOW_WATERLILY = ITEMS.register("yellow_waterlily", () -> new FloatingFlowerBlockItem(CNBBlocks.YELLOW_WATERLILY_BLOCK.get(), CreaturesAndBeasts.itemProperties("yellow_waterlily")
            .food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.5F).alwaysEdible().build(), instantHealthFood())));

    // Bucketed Mobs
    public static final CNBRegistrySupplier<CNBEntityBucketItem> CINDERSHELL_BUCKET = ITEMS.register("cindershell_bucket", () -> new CNBEntityBucketItem(CNBEntityTypes.CINDERSHELL::get, Fluids.LAVA, () -> SoundEvents.BUCKET_EMPTY_LAVA, CreaturesAndBeasts.itemProperties("cindershell_bucket").stacksTo(1)));

    // Misc. Items
    public static final CNBRegistrySupplier<Item> ENTITY_NET = ITEMS.register("entity_net", () -> new Item(CreaturesAndBeasts.itemProperties("entity_net").durability(64)));
    public static final CNBRegistrySupplier<LizardEggItem> LIZARD_EGG = ITEMS.register("lizard_egg", () -> new LizardEggItem(CNBBlocks.LIZARD_EGGS.get(), CreaturesAndBeasts.itemProperties("lizard_egg").stacksTo(16)));
    public static final CNBRegistrySupplier<CNBFuelItem> CINDERSHELL_SHELL_SHARD = ITEMS.register("cindershell_shell_shard", () -> new CNBFuelItem(CreaturesAndBeasts.itemProperties("cindershell_shell_shard")));
    public static final CNBRegistrySupplier<Item> YETI_ANTLER = ITEMS.register("yeti_antler", () -> new Item(CreaturesAndBeasts.itemProperties("yeti_antler")));
    public static final CNBRegistrySupplier<Item> YETI_HIDE = ITEMS.register("yeti_hide", () -> new Item(CreaturesAndBeasts.itemProperties("yeti_hide")));

    public static final CNBRegistrySupplier<Item> PINK_MINIPAD_FLOWER = ITEMS.register("pink_minipad_flower", () -> new FloatingFlowerBlockItem(CNBBlocks.PINK_MINIPAD_FLOWER_BLOCK.get(), CreaturesAndBeasts.itemProperties("pink_minipad_flower")));
    public static final CNBRegistrySupplier<Item> LIGHT_PINK_MINIPAD_FLOWER = ITEMS.register("light_pink_minipad_flower", () -> new FloatingFlowerBlockItem(CNBBlocks.LIGHT_PINK_MINIPAD_FLOWER_BLOCK.get(), CreaturesAndBeasts.itemProperties("light_pink_minipad_flower")));
    public static final CNBRegistrySupplier<Item> YELLOW_MINIPAD_FLOWER = ITEMS.register("yellow_minipad_flower", () -> new FloatingFlowerBlockItem(CNBBlocks.YELLOW_MINIPAD_FLOWER_BLOCK.get(), CreaturesAndBeasts.itemProperties("yellow_minipad_flower")));
    public static final CNBRegistrySupplier<Item> PINK_MINIPAD_FLOWER_GLOW = ITEMS.register("pink_minipad_flower_glow", () -> new MinipadFlowerGlowItem(CNBBlocks.PINK_MINIPAD_FLOWER_GLOW_BLOCK.get(), CreaturesAndBeasts.itemProperties("pink_minipad_flower_glow")));
    public static final CNBRegistrySupplier<Item> LIGHT_PINK_MINIPAD_FLOWER_GLOW = ITEMS.register("light_pink_minipad_flower_glow", () -> new MinipadFlowerGlowItem(CNBBlocks.LIGHT_PINK_MINIPAD_FLOWER_GLOW_BLOCK.get(), CreaturesAndBeasts.itemProperties("light_pink_minipad_flower_glow")));
    public static final CNBRegistrySupplier<Item> YELLOW_MINIPAD_FLOWER_GLOW = ITEMS.register("yellow_minipad_flower_glow", () -> new MinipadFlowerGlowItem(CNBBlocks.YELLOW_MINIPAD_FLOWER_GLOW_BLOCK.get(), CreaturesAndBeasts.itemProperties("yellow_minipad_flower_glow")));

    public static final CNBRegistrySupplier<HealSpellBookItem> HEAL_SPELL_BOOK_1 = ITEMS.register("heal_spell_book_1", () -> new HealSpellBookItem(CreaturesAndBeasts.itemProperties("heal_spell_book_1").stacksTo(1)));
    public static final CNBRegistrySupplier<HealSpellBookItem> HEAL_SPELL_BOOK_2 = ITEMS.register("heal_spell_book_2", () -> new HealSpellBookItem(CreaturesAndBeasts.itemProperties("heal_spell_book_2").stacksTo(1)));
    public static final CNBRegistrySupplier<HealSpellBookItem> HEAL_SPELL_BOOK_3 = ITEMS.register("heal_spell_book_3", () -> new HealSpellBookItem(CreaturesAndBeasts.itemProperties("heal_spell_book_3").stacksTo(1)));

    // Armor
    public static final CNBRegistrySupplier<FlowerCrownItem> FLOWER_CROWN = ITEMS.register("flower_crown", () -> new FlowerCrownItem(armorProperties("flower_crown", CNBArmorMaterials.FLOWER_CROWN, ArmorType.HELMET).repairable(CNBTags.Items.MINIPAD_FLOWERS)));
    public static final CNBRegistrySupplier<GlowingFlowerCrownItem> GLOWING_FLOWER_CROWN = ITEMS.register("glowing_flower_crown", () -> new GlowingFlowerCrownItem(armorProperties("glowing_flower_crown", CNBArmorMaterials.FLOWER_CROWN, ArmorType.HELMET).repairable(CNBTags.Items.GLOWING_MINIPAD_FLOWERS)));
    public static final CNBRegistrySupplier<SporelingBackpackItem> SPORELING_BACKPACK = ITEMS.register("sporeling_backpack", () -> new SporelingBackpackItem(armorProperties("sporeling_backpack", CNBArmorMaterials.SPORELING_BACKPACK, ArmorType.CHESTPLATE)));

    // Tools
    public static final CNBRegistrySupplier<CinderSwordItem> CINDER_SWORD = ITEMS.register("cinder_sword", () -> new CinderSwordItem(0, CreaturesAndBeasts.itemProperties("cinder_sword").sword(CNBItemTiers.CINDER, 3, -2.4F)));
    public static final CNBRegistrySupplier<CinderSwordItem> CINDER_SWORD_1 = ITEMS.register("cinder_sword_1", () -> new CinderSwordItem(1, CreaturesAndBeasts.itemProperties("cinder_sword_1").sword(CNBItemTiers.CINDER, 4, -2.4F)));
    public static final CNBRegistrySupplier<CinderSwordItem> CINDER_SWORD_2 = ITEMS.register("cinder_sword_2", () -> new CinderSwordItem(2, CreaturesAndBeasts.itemProperties("cinder_sword_2").sword(CNBItemTiers.CINDER, 5, -2.4F)));
    public static final CNBRegistrySupplier<CinderSwordItem> CINDER_SWORD_3 = ITEMS.register("cinder_sword_3", () -> new CinderSwordItem(3, CreaturesAndBeasts.itemProperties("cinder_sword_3").sword(CNBItemTiers.CINDER, 6, -2.4F)));
    public static final CNBRegistrySupplier<CinderSwordItem> CINDER_SWORD_4 = ITEMS.register("cinder_sword_4", () -> new CinderSwordItem(4, CreaturesAndBeasts.itemProperties("cinder_sword_4").sword(CNBItemTiers.CINDER, 7, -2.4F)));

    public static final CNBRegistrySupplier<SpearItem> CACTEM_SPEAR = ITEMS.register("cactem_spear", () -> new SpearItem(CreaturesAndBeasts.itemProperties("cactem_spear").durability(100).enchantable(1).attributes(SpearItem.createAttributes())));

    // Spawn Eggs
    public static CNBRegistrySupplier<SpawnEggItem> GREBE_SPAWN_EGG = ITEMS.register("little_grebe_spawn_egg", () -> spawnEgg("little_grebe_spawn_egg", CNBEntityTypes.LITTLE_GREBE.get()));
    public static CNBRegistrySupplier<SpawnEggItem> CINDERSHELL_SPAWN_EGG = ITEMS.register("cindershell_spawn_egg", () -> spawnEgg("cindershell_spawn_egg", CNBEntityTypes.CINDERSHELL.get()));
    public static CNBRegistrySupplier<SpawnEggItem> LILYTAD_SPAWN_EGG = ITEMS.register("lilytad_spawn_egg", () -> spawnEgg("lilytad_spawn_egg", CNBEntityTypes.LILYTAD.get()));
    public static CNBRegistrySupplier<SpawnEggItem> YETI_SPAWN_EGG = ITEMS.register("yeti_spawn_egg", () -> spawnEgg("yeti_spawn_egg", CNBEntityTypes.YETI.get()));
    public static CNBRegistrySupplier<SpawnEggItem> MINIPAD_SPAWN_EGG = ITEMS.register("minipad_spawn_egg", () -> spawnEgg("minipad_spawn_egg", CNBEntityTypes.MINIPAD.get()));
    public static CNBRegistrySupplier<SpawnEggItem> LIZARD_SPAWN_EGG = ITEMS.register("lizard_spawn_egg", () -> spawnEgg("lizard_spawn_egg", CNBEntityTypes.LIZARD.get()));
    public static CNBRegistrySupplier<SpawnEggItem> END_WHALE_SPAWN_EGG = ITEMS.register("end_whale_spawn_egg", () -> spawnEgg("end_whale_spawn_egg", CNBEntityTypes.END_WHALE.get()));
    public static CNBRegistrySupplier<SpawnEggItem> CACTEM_SPAWN_EGG = ITEMS.register("cactem_spawn_egg", () -> spawnEgg("cactem_spawn_egg", CNBEntityTypes.CACTEM.get()));
    public static CNBRegistrySupplier<LizardItem> LIZARD_ITEM_DESERT = ITEMS.register("lizard_item_desert", () -> new LizardItem(CNBEntityTypes.LIZARD, CreaturesAndBeasts.itemProperties("lizard_item_desert"), CNBLizardTypes.DESERT));
    public static CNBRegistrySupplier<LizardItem> LIZARD_ITEM_DESERT_2 = ITEMS.register("lizard_item_desert_2", () -> new LizardItem(CNBEntityTypes.LIZARD, CreaturesAndBeasts.itemProperties("lizard_item_desert_2"), CNBLizardTypes.DESERT_2));
    public static CNBRegistrySupplier<LizardItem> LIZARD_ITEM_JUNGLE = ITEMS.register("lizard_item_jungle", () -> new LizardItem(CNBEntityTypes.LIZARD, CreaturesAndBeasts.itemProperties("lizard_item_jungle"), CNBLizardTypes.JUNGLE));
    public static CNBRegistrySupplier<LizardItem> LIZARD_ITEM_JUNGLE_2 = ITEMS.register("lizard_item_jungle_2", () -> new LizardItem(CNBEntityTypes.LIZARD, CreaturesAndBeasts.itemProperties("lizard_item_jungle_2"), CNBLizardTypes.JUNGLE_2));
    public static CNBRegistrySupplier<LizardItem> LIZARD_ITEM_MUSHROOM = ITEMS.register("lizard_item_mushroom", () -> new LizardItem(CNBEntityTypes.LIZARD, CreaturesAndBeasts.itemProperties("lizard_item_mushroom"), CNBLizardTypes.MUSHROOM));
    public static CNBRegistrySupplier<SporelingSpawnEggItem> SPORELING_OVERWORLD_EGG = ITEMS.register("sporeling_overworld_egg", () -> new SporelingSpawnEggItem(CNBEntityTypes.SPORELING, CreaturesAndBeasts.itemProperties("sporeling_overworld_egg")));
    public static CNBRegistrySupplier<SporelingSpawnEggItem> SPORELING_NETHER_EGG = ITEMS.register("sporeling_nether_egg", () -> new SporelingSpawnEggItem(CNBEntityTypes.SPORELING, CreaturesAndBeasts.itemProperties("sporeling_nether_egg")));

    // Block Items
    public static CNBRegistrySupplier<CinderFurnaceItem> CINDERSHELL_FURNACE = ITEMS.register("cinder_furnace", () -> new CinderFurnaceItem(CNBBlocks.CINDER_FURNACE.get(), CreaturesAndBeasts.itemProperties("cinder_furnace")));

    public static void addCreativeTabItems(CreativeModeTab.Output output) {
        output.accept(APPLE_SLICE.get());
        output.accept(PINK_WATERLILY.get());
        output.accept(LIGHT_PINK_WATERLILY.get());
        output.accept(YELLOW_WATERLILY.get());
        output.accept(CINDERSHELL_BUCKET.get());
        output.accept(ENTITY_NET.get());
        output.accept(YETI_ANTLER.get());
        output.accept(YETI_HIDE.get());
        output.accept(PINK_MINIPAD_FLOWER.get());
        output.accept(LIGHT_PINK_MINIPAD_FLOWER.get());
        output.accept(YELLOW_MINIPAD_FLOWER.get());
        output.accept(PINK_MINIPAD_FLOWER_GLOW.get());
        output.accept(LIGHT_PINK_MINIPAD_FLOWER_GLOW.get());
        output.accept(YELLOW_MINIPAD_FLOWER_GLOW.get());
        output.accept(HEAL_SPELL_BOOK_1.get());
        output.accept(HEAL_SPELL_BOOK_2.get());
        output.accept(HEAL_SPELL_BOOK_3.get());
        output.accept(FLOWER_CROWN.get());
        output.accept(GLOWING_FLOWER_CROWN.get());
        output.accept(SPORELING_BACKPACK.get());
        output.accept(CINDER_SWORD.get());
        output.accept(CACTEM_SPEAR.get());
        output.accept(GREBE_SPAWN_EGG.get());
        output.accept(CINDERSHELL_SPAWN_EGG.get());
        output.accept(LILYTAD_SPAWN_EGG.get());
        output.accept(YETI_SPAWN_EGG.get());
        output.accept(MINIPAD_SPAWN_EGG.get());
        output.accept(LIZARD_SPAWN_EGG.get());
        output.accept(END_WHALE_SPAWN_EGG.get());
        output.accept(CACTEM_SPAWN_EGG.get());
        output.accept(LIZARD_ITEM_DESERT.get());
        output.accept(LIZARD_ITEM_DESERT_2.get());
        output.accept(LIZARD_ITEM_JUNGLE.get());
        output.accept(LIZARD_ITEM_JUNGLE_2.get());
        output.accept(LIZARD_ITEM_MUSHROOM.get());
        output.accept(SPORELING_OVERWORLD_EGG.get());
        output.accept(SPORELING_NETHER_EGG.get());
        output.accept(CINDERSHELL_FURNACE.get());
    }

    private static Item.Properties armorProperties(String path, ArmorMaterial material, ArmorType type) {
        return CreaturesAndBeasts.itemProperties(path).humanoidArmor(material, type);
    }

    private static SpawnEggItem spawnEgg(String path, EntityType<?> entityType) {
        return new SpawnEggItem(CreaturesAndBeasts.itemProperties(path).spawnEgg(entityType));
    }

    private static Consumable instantHealthFood() {
        return Consumables.defaultFood()
                .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.INSTANT_HEALTH, 1), 1.0F))
                .build();
    }
}
