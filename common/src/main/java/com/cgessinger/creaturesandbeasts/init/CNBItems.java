package com.cgessinger.creaturesandbeasts.init;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.items.*;
import dev.architectury.core.item.ArchitecturySpawnEggItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;

public class CNBItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(CreaturesAndBeasts.MOD_ID, Registries.ITEM);

    // Food
    public static final RegistrySupplier<Item> APPLE_SLICE = ITEMS.register("apple_slice", () -> new Item(new Item.Properties().arch$tab(CreaturesAndBeasts.TAB)
            .food(new FoodProperties.Builder().nutrition(1).saturationMod(0.3F).build())));
    public static final RegistrySupplier<WaterlilyBlockItem> PINK_WATERLILY = ITEMS.register("pink_waterlily", () -> new WaterlilyBlockItem(CNBBlocks.PINK_WATERLILY_BLOCK.get(), new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.5F).alwaysEat()
                    .effect(new MobEffectInstance(MobEffects.HEAL, 1), 1.0F).build())));
    public static final RegistrySupplier<WaterlilyBlockItem> LIGHT_PINK_WATERLILY = ITEMS.register("light_pink_waterlily", () -> new WaterlilyBlockItem(CNBBlocks.LIGHT_PINK_WATERLILY_BLOCK.get(), new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.5F).alwaysEat()
                    .effect(new MobEffectInstance(MobEffects.HEAL, 1), 1.0F).build())));
    public static final RegistrySupplier<WaterlilyBlockItem> YELLOW_WATERLILY = ITEMS.register("yellow_waterlily", () -> new WaterlilyBlockItem(CNBBlocks.YELLOW_WATERLILY_BLOCK.get(), new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.5F).alwaysEat()
                    .effect(new MobEffectInstance(MobEffects.HEAL, 1), 1.0F).build())));

    // Bucketed Mobs
    public static final RegistrySupplier<CNBEntityBucketItem> CINDERSHELL_BUCKET = ITEMS.register("cindershell_bucket", () -> new CNBEntityBucketItem(CNBEntityTypes.CINDERSHELL::get, Fluids.LAVA, () -> SoundEvents.BUCKET_EMPTY_LAVA, new Item.Properties().stacksTo(1).arch$tab(CreaturesAndBeasts.TAB)));

    // Misc. Items
    public static final RegistrySupplier<Item> ENTITY_NET = ITEMS.register("entity_net", () -> new Item(new Item.Properties().arch$tab(CreaturesAndBeasts.TAB).durability(64)));
    public static final RegistrySupplier<LizardEggItem> LIZARD_EGG = ITEMS.register("lizard_egg", () -> new LizardEggItem(CNBBlocks.LIZARD_EGGS.get()));
    public static final RegistrySupplier<CNBFuelItem> CINDERSHELL_SHELL_SHARD = ITEMS.register("cindershell_shell_shard", () -> new CNBFuelItem(6400));
    public static final RegistrySupplier<Item> YETI_ANTLER = ITEMS.register("yeti_antler", () -> new Item(new Item.Properties().arch$tab(CreaturesAndBeasts.TAB)));
    public static final RegistrySupplier<Item> YETI_HIDE = ITEMS.register("yeti_hide", () -> new Item(new Item.Properties().arch$tab(CreaturesAndBeasts.TAB)));

    public static final RegistrySupplier<Item> PINK_MINIPAD_FLOWER = ITEMS.register("pink_minipad_flower", () -> new Item(new Item.Properties().arch$tab(CreaturesAndBeasts.TAB)));
    public static final RegistrySupplier<Item> LIGHT_PINK_MINIPAD_FLOWER = ITEMS.register("light_pink_minipad_flower", () -> new Item(new Item.Properties().arch$tab(CreaturesAndBeasts.TAB)));
    public static final RegistrySupplier<Item> YELLOW_MINIPAD_FLOWER = ITEMS.register("yellow_minipad_flower", () -> new Item(new Item.Properties().arch$tab(CreaturesAndBeasts.TAB)));
    public static final RegistrySupplier<Item> PINK_MINIPAD_FLOWER_GLOW = ITEMS.register("pink_minipad_flower_glow", () -> new MinipadFlowerGlowItem(new Item.Properties().arch$tab(CreaturesAndBeasts.TAB)));
    public static final RegistrySupplier<Item> LIGHT_PINK_MINIPAD_FLOWER_GLOW = ITEMS.register("light_pink_minipad_flower_glow", () -> new MinipadFlowerGlowItem(new Item.Properties().arch$tab(CreaturesAndBeasts.TAB)));
    public static final RegistrySupplier<Item> YELLOW_MINIPAD_FLOWER_GLOW = ITEMS.register("yellow_minipad_flower_glow", () -> new MinipadFlowerGlowItem(new Item.Properties().arch$tab(CreaturesAndBeasts.TAB)));

    public static final RegistrySupplier<HealSpellBookItem> HEAL_SPELL_BOOK_1 = ITEMS.register("heal_spell_book_1", () -> new HealSpellBookItem(new Item.Properties().stacksTo(1).arch$tab(CreaturesAndBeasts.TAB)));
    public static final RegistrySupplier<HealSpellBookItem> HEAL_SPELL_BOOK_2 = ITEMS.register("heal_spell_book_2", () -> new HealSpellBookItem(new Item.Properties().stacksTo(1).arch$tab(CreaturesAndBeasts.TAB)));
    public static final RegistrySupplier<HealSpellBookItem> HEAL_SPELL_BOOK_3 = ITEMS.register("heal_spell_book_3", () -> new HealSpellBookItem(new Item.Properties().stacksTo(1).arch$tab(CreaturesAndBeasts.TAB)));

    // Armor
    public static final RegistrySupplier<FlowerCrownItem> FLOWER_CROWN = ITEMS.register("flower_crown", () -> new FlowerCrownItem(CNBArmorMaterials.FLOWER_CROWN, Ingredient.of(PINK_MINIPAD_FLOWER.get(), LIGHT_PINK_MINIPAD_FLOWER.get(), YELLOW_MINIPAD_FLOWER.get()), ArmorItem.Type.HELMET, new Item.Properties().arch$tab(CreaturesAndBeasts.TAB)));
    public static final RegistrySupplier<GlowingFlowerCrownItem> GLOWING_FLOWER_CROWN = ITEMS.register("glowing_flower_crown", () -> new GlowingFlowerCrownItem(CNBArmorMaterials.FLOWER_CROWN, Ingredient.of(PINK_MINIPAD_FLOWER_GLOW.get(), LIGHT_PINK_MINIPAD_FLOWER_GLOW.get(), YELLOW_MINIPAD_FLOWER_GLOW.get()), ArmorItem.Type.HELMET, new Item.Properties().arch$tab(CreaturesAndBeasts.TAB)));
    public static final RegistrySupplier<SporelingBackpackItem> SPORELING_BACKPACK = ITEMS.register("sporeling_backpack", () -> new SporelingBackpackItem(CNBArmorMaterials.SPORELING_BACKPACK, ArmorItem.Type.CHESTPLATE, new Item.Properties().arch$tab(CreaturesAndBeasts.TAB)));

    // Tools
    public static final RegistrySupplier<CinderSwordItem> CINDER_SWORD = ITEMS.register("cinder_sword", () -> new CinderSwordItem(CNBItemTiers.CINDER, 0, 3, -2.4F, new Item.Properties().arch$tab(CreaturesAndBeasts.TAB)));
    public static final RegistrySupplier<CinderSwordItem> CINDER_SWORD_1 = ITEMS.register("cinder_sword_1", () -> new CinderSwordItem(CNBItemTiers.CINDER, 1, 4, -2.4F, new Item.Properties()));
    public static final RegistrySupplier<CinderSwordItem> CINDER_SWORD_2 = ITEMS.register("cinder_sword_2", () -> new CinderSwordItem(CNBItemTiers.CINDER, 2, 5, -2.4F, new Item.Properties()));
    public static final RegistrySupplier<CinderSwordItem> CINDER_SWORD_3 = ITEMS.register("cinder_sword_3", () -> new CinderSwordItem(CNBItemTiers.CINDER, 3, 6, -2.4F, new Item.Properties()));
    public static final RegistrySupplier<CinderSwordItem> CINDER_SWORD_4 = ITEMS.register("cinder_sword_4", () -> new CinderSwordItem(CNBItemTiers.CINDER, 4, 7, -2.4F, new Item.Properties()));

    public static final RegistrySupplier<SpearItem> CACTEM_SPEAR = ITEMS.register("cactem_spear", () -> new SpearItem(new Item.Properties().durability(100).arch$tab(CreaturesAndBeasts.TAB)));

    // Spawn Eggs
    public static RegistrySupplier<ArchitecturySpawnEggItem> GREBE_SPAWN_EGG = ITEMS.register("little_grebe_spawn_egg", () -> new ArchitecturySpawnEggItem(CNBEntityTypes.LITTLE_GREBE, 0x00FFFFFF, 0x00FFFFFF, new Item.Properties().arch$tab(CreaturesAndBeasts.TAB)));
    public static RegistrySupplier<ArchitecturySpawnEggItem> CINDERSHELL_SPAWN_EGG = ITEMS.register("cindershell_spawn_egg", () -> new ArchitecturySpawnEggItem(CNBEntityTypes.CINDERSHELL, 0x0D0403, 0xC64500, new Item.Properties().arch$tab(CreaturesAndBeasts.TAB)));
    public static RegistrySupplier<ArchitecturySpawnEggItem> LILYTAD_SPAWN_EGG = ITEMS.register("lilytad_spawn_egg", () -> new ArchitecturySpawnEggItem(CNBEntityTypes.LILYTAD, 0x37702E, 0x102417, new Item.Properties().arch$tab(CreaturesAndBeasts.TAB)));
    public static RegistrySupplier<ArchitecturySpawnEggItem> YETI_SPAWN_EGG = ITEMS.register("yeti_spawn_egg", () -> new ArchitecturySpawnEggItem(CNBEntityTypes.YETI, 0xD7E1E7, 0x887E96, new Item.Properties().arch$tab(CreaturesAndBeasts.TAB)));
    public static RegistrySupplier<ArchitecturySpawnEggItem> MINIPAD_SPAWN_EGG = ITEMS.register("minipad_spawn_egg", () -> new ArchitecturySpawnEggItem(CNBEntityTypes.MINIPAD, 0x3EA62E, 0x194F28, new Item.Properties().arch$tab(CreaturesAndBeasts.TAB)));
    public static RegistrySupplier<ArchitecturySpawnEggItem> LIZARD_SPAWN_EGG = ITEMS.register("lizard_spawn_egg", () -> new ArchitecturySpawnEggItem(CNBEntityTypes.LIZARD, 0x00FFFFFF, 0x00FFFFFF, new Item.Properties().arch$tab(CreaturesAndBeasts.TAB)));
    public static RegistrySupplier<ArchitecturySpawnEggItem> END_WHALE_SPAWN_EGG = ITEMS.register("end_whale_spawn_egg", () -> new ArchitecturySpawnEggItem(CNBEntityTypes.END_WHALE, 0x5609AD, 0xD4AD5F, new Item.Properties().arch$tab(CreaturesAndBeasts.TAB)));
    public static RegistrySupplier<ArchitecturySpawnEggItem> CACTEM_SPAWN_EGG = ITEMS.register("cactem_spawn_egg", () -> new ArchitecturySpawnEggItem(CNBEntityTypes.CACTEM, 0x1A6E23, 0xDCEBAB, new Item.Properties().arch$tab(CreaturesAndBeasts.TAB)));
    public static RegistrySupplier<LizardItem> LIZARD_ITEM_DESERT = ITEMS.register("lizard_item_desert", () -> new LizardItem(CNBEntityTypes.LIZARD, 0x00FFFFFF, 0x00FFFFFF, new Item.Properties().arch$tab(CreaturesAndBeasts.TAB), CNBLizardTypes.DESERT));
    public static RegistrySupplier<LizardItem> LIZARD_ITEM_DESERT_2 = ITEMS.register("lizard_item_desert_2", () -> new LizardItem(CNBEntityTypes.LIZARD, 0x00FFFFFF, 0x00FFFFFF, new Item.Properties().arch$tab(CreaturesAndBeasts.TAB), CNBLizardTypes.DESERT_2));
    public static RegistrySupplier<LizardItem> LIZARD_ITEM_JUNGLE = ITEMS.register("lizard_item_jungle", () -> new LizardItem(CNBEntityTypes.LIZARD, 0x00FFFFFF, 0x00FFFFFF, new Item.Properties().arch$tab(CreaturesAndBeasts.TAB), CNBLizardTypes.JUNGLE));
    public static RegistrySupplier<LizardItem> LIZARD_ITEM_JUNGLE_2 = ITEMS.register("lizard_item_jungle_2", () -> new LizardItem(CNBEntityTypes.LIZARD, 0x00FFFFFF, 0x00FFFFFF, new Item.Properties().arch$tab(CreaturesAndBeasts.TAB), CNBLizardTypes.JUNGLE_2));
    public static RegistrySupplier<LizardItem> LIZARD_ITEM_MUSHROOM = ITEMS.register("lizard_item_mushroom", () -> new LizardItem(CNBEntityTypes.LIZARD, 0x00FFFFFF, 0x00FFFFFF, new Item.Properties().arch$tab(CreaturesAndBeasts.TAB), CNBLizardTypes.MUSHROOM));
    public static RegistrySupplier<SporelingSpawnEggItem> SPORELING_OVERWORLD_EGG = ITEMS.register("sporeling_overworld_egg", () -> new SporelingSpawnEggItem(CNBEntityTypes.SPORELING, 0xDE0942, 0xFFEBC4, new Item.Properties().arch$tab(CreaturesAndBeasts.TAB)));
    public static RegistrySupplier<SporelingSpawnEggItem> SPORELING_NETHER_EGG = ITEMS.register("sporeling_nether_egg", () -> new SporelingSpawnEggItem(CNBEntityTypes.SPORELING, 0xBF2828, 0xFF9245, new Item.Properties().arch$tab(CreaturesAndBeasts.TAB)));

    // Block Items
    public static RegistrySupplier<CinderFurnaceItem> CINDERSHELL_FURNACE = ITEMS.register("cinder_furnace", () -> new CinderFurnaceItem(CNBBlocks.CINDER_FURNACE.get(), new Item.Properties().arch$tab(CreaturesAndBeasts.TAB)));
}
