package com.cgessinger.creaturesandbeasts.forge;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.ModLoaderProxy;
import com.cgessinger.creaturesandbeasts.client.CNBClient;
import com.cgessinger.creaturesandbeasts.events.ClientEvents;
import com.cgessinger.creaturesandbeasts.util.BiomeSpawns;
import dev.architectury.platform.forge.EventBuses;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

@Mod(CreaturesAndBeasts.MOD_ID)
public class CreaturesAndBeastsForge implements ModLoaderProxy {
    private final CreaturesAndBeasts instance;

    public CreaturesAndBeastsForge(FMLJavaModLoadingContext ctx) {
        IEventBus modEventBus = ctx.getModEventBus();
        EventBuses.registerModEventBus(CreaturesAndBeasts.MOD_ID, modEventBus);
        instance = new CreaturesAndBeasts(this); // must be initialized here.

        MinecraftForge.EVENT_BUS.register(this);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ClientEvents.registerRenderers();
            ClientEvents.registerLayerDefinitions();
        });

        modEventBus.addListener((FMLCommonSetupEvent event) -> instance.commonSetup());
        modEventBus.addListener((FMLClientSetupEvent event) -> {
            CNBClient.init();
            event.enqueueWork(CNBClient::initClientDeferred);
        });
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    private final BiomeSpawns biomeSpawns = new BiomeSpawns();

    @SubscribeEvent
    public void onAddSpawns(LevelEvent.PotentialSpawns potentialSpawns) {
        if (biomeSpawns.getSpawns().isEmpty())
            instance.addSpawns(biomeSpawns);

        for (BiomeSpawns.SpawnData spawn : biomeSpawns.getSpawns()) {
            if (potentialSpawns.getMobCategory() == spawn.category() && spawn.selector().test(potentialSpawns.getLevel().getBiome(potentialSpawns.getPos()))) {
                potentialSpawns.addSpawnerData(spawn.spawnerData());

                // TODO: spawn costs
            }
        }
    }

    @SubscribeEvent
    public void onLootingCalculate(LootingLevelEvent event) {
        if (event.getDamageSource() != null)
            event.setLootingLevel(instance.getEvents().onLootingCalculate(event.getDamageSource()));
    }

    @SubscribeEvent
    public void onItemAttributeModifierCalculate(ItemAttributeModifierEvent event) {
        instance.getEvents().onItemAttributeModifierCalculate(event.getItemStack(), event.getSlotType(), event::addModifier);
    }

    @SubscribeEvent
    public void onAnvilChange(AnvilUpdateEvent event) {
        var result = instance.getEvents().onAnvilChange(event.getLeft(), event.getRight());
        if (result != null) {
            event.setCost(result.getLeft());
            event.setMaterialCost(result.getMiddle());
            event.setOutput(result.getRight());
        }
    }

    @Override
    public boolean callAnimalTameEvent(Animal animal, Player tamer) {
        return ModLoaderProxy.super.callAnimalTameEvent(animal, tamer) && ForgeEventFactory.onAnimalTame(animal, tamer);
    }

    @Override
    public void callPlayerSmeltedEvent(Player player, ItemStack stack) {
        ModLoaderProxy.super.callPlayerSmeltedEvent(player, stack);
        ForgeEventFactory.firePlayerSmeltedEvent(player, stack);
    }
}
