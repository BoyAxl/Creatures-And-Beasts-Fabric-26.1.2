package com.cgessinger.creaturesandbeasts.init;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;

public class CNBSoundEvents {
    public static DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(CreaturesAndBeasts.MOD_ID, Registries.SOUND_EVENT);

    public static final RegistrySupplier<SoundEvent> LITTLE_GREBE_AMBIENT = SOUND_EVENTS.register("entity.little_grebe.ambient", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.little_grebe.ambient")));
    public static final RegistrySupplier<SoundEvent> LITTLE_GREBE_HURT = SOUND_EVENTS.register("entity.little_grebe.hurt", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.little_grebe.hurt")));
    public static final RegistrySupplier<SoundEvent> LITTLE_GREBE_CHICK_AMBIENT = SOUND_EVENTS.register("entity.little_grebe_chick.ambient", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.little_grebe_chick.ambient")));

    public static final RegistrySupplier<SoundEvent> CINDERSHELL_AMBIENT = SOUND_EVENTS.register("entity.cindershell.ambient", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.cindershell.ambient")));
    public static final RegistrySupplier<SoundEvent> CINDERSHELL_HURT = SOUND_EVENTS.register("entity.cindershell.hurt", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.cindershell.hurt")));
    public static final RegistrySupplier<SoundEvent> CINDERSHELL_ADULT_EAT = SOUND_EVENTS.register("entity.cindershell_adult.eat", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.cindershell_adult.eat")));
    public static final RegistrySupplier<SoundEvent> CINDERSHELL_BABY_EAT = SOUND_EVENTS.register("entity.cindershell_baby.eat", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.cindershell_baby.eat")));

    public static final RegistrySupplier<SoundEvent> SPORELING_OVERWORLD_AMBIENT = SOUND_EVENTS.register("entity.sporeling_overworld.ambient", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.sporeling_overworld.ambient")));
    public static final RegistrySupplier<SoundEvent> SPORELING_OVERWORLD_HURT = SOUND_EVENTS.register("entity.sporeling_overworld.hurt", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.sporeling_overworld.hurt")));
    public static final RegistrySupplier<SoundEvent> SPORELING_NETHER_AMBIENT = SOUND_EVENTS.register("entity.sporeling_nether.ambient", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.sporeling_nether.ambient")));
    public static final RegistrySupplier<SoundEvent> SPORELING_NETHER_HURT = SOUND_EVENTS.register("entity.sporeling_nether.hurt", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.sporeling_nether.hurt")));
    public static final RegistrySupplier<SoundEvent> SPORELING_WARPED_AMBIENT = SOUND_EVENTS.register("entity.sporeling_warped.ambient", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.sporeling_warped.ambient")));
    public static final RegistrySupplier<SoundEvent> SPORELING_WARPED_HURT = SOUND_EVENTS.register("entity.sporeling_warped.hurt", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.sporeling_warped.hurt")));
    public static final RegistrySupplier<SoundEvent> SPORELING_BITE = SOUND_EVENTS.register("entity.sporeling.bite", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.sporeling.bite")));

    public static final RegistrySupplier<SoundEvent> LILYTAD_AMBIENT = SOUND_EVENTS.register("entity.lilytad.ambient", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.lilytad.ambient")));
    public static final RegistrySupplier<SoundEvent> LILYTAD_HURT = SOUND_EVENTS.register("entity.lilytad.hurt", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.lilytad.hurt")));
    public static final RegistrySupplier<SoundEvent> LILYTAD_DEATH = SOUND_EVENTS.register("entity.lilytad.death", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.lilytad.death")));

    public static final RegistrySupplier<SoundEvent> YETI_AMBIENT = SOUND_EVENTS.register("entity.yeti.ambient", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.yeti.ambient")));
    public static final RegistrySupplier<SoundEvent> YETI_HURT = SOUND_EVENTS.register("entity.yeti.hurt", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.yeti.hurt")));
    public static final RegistrySupplier<SoundEvent> YETI_STEP = SOUND_EVENTS.register("entity.yeti.step", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.yeti.step")));
    public static final RegistrySupplier<SoundEvent> YETI_HIT = SOUND_EVENTS.register("entity.yeti.hit", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.yeti.hit")));
    public static final RegistrySupplier<SoundEvent> YETI_ADULT_EAT = SOUND_EVENTS.register("entity.yeti_adult.eat", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.yeti_adult.eat")));
    public static final RegistrySupplier<SoundEvent> YETI_BABY_EAT = SOUND_EVENTS.register("entity.yeti_baby.eat", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.yeti_baby.eat")));

    public static final RegistrySupplier<SoundEvent> MINIPAD_HURT = SOUND_EVENTS.register("entity.minipad.hurt", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.minipad.hurt")));
    public static final RegistrySupplier<SoundEvent> MINIPAD_STEP = SOUND_EVENTS.register("entity.minipad.step", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.minipad.step")));
    public static final RegistrySupplier<SoundEvent> MINIPAD_SWIM = SOUND_EVENTS.register("entity.minipad.swim", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.minipad.swim")));

    public static final RegistrySupplier<SoundEvent> END_WHALE_AMBIENT = SOUND_EVENTS.register("entity.end_whale.ambient", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.end_whale.ambient")));

    public static final RegistrySupplier<SoundEvent> CACTEM_AMBIENT = SOUND_EVENTS.register("entity.cactem.ambient", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.cactem.ambient")));
    public static final RegistrySupplier<SoundEvent> CACTEM_HURT = SOUND_EVENTS.register("entity.cactem.hurt", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.cactem.hurt")));
    public static final RegistrySupplier<SoundEvent> CACTEM_HEAL = SOUND_EVENTS.register("entity.cactem.heal", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.cactem.heal")));

    public static final RegistrySupplier<SoundEvent> PLAYER_HEAL = SOUND_EVENTS.register("item.heal_spell_book.player_heal", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("item.heal_spell_book.player_heal")));

    public static final RegistrySupplier<SoundEvent> SPEAR_THROW = SOUND_EVENTS.register("item.cactem_spear.throw", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("item.cactem_spear.throw")));

    public static final RegistrySupplier<SoundEvent> LIZARD_EGG_HATCH = SOUND_EVENTS.register("entity.lizard.egg_hatch", () -> SoundEvent.createVariableRangeEvent(CreaturesAndBeasts.id("entity.lizard.egg_hatch")));
}
