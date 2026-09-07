package dev.olliesbrother;

import dev.olliesbrother.damage.DamageModifierRegistry;
import dev.olliesbrother.experience.sources.*;
import dev.olliesbrother.persistence.PlayerPlacedBlockEvents;
import dev.olliesbrother.skill.SkillRegistry;
import dev.olliesbrother.skill.abilities.active.GigaDrillAbility;
import dev.olliesbrother.skill.abilities.active.SuperBreakerAbility;
import dev.olliesbrother.skill.abilities.active.TreeFellerAbility;
import dev.olliesbrother.skill.abilities.passive.*;
import dev.olliesbrother.skill.events.*;
import dev.olliesbrother.skill.skills.*;
import dev.olliesbrother.experience.*;
import dev.olliesbrother.commands.SkillsCommand;
import dev.olliesbrother.commands.SkillforgeCommand;
import dev.olliesbrother.config.ConfigManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Skillforge implements ModInitializer {

    public static final String MOD_ID = "skillforge";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {

        ConfigManager.load();

        SkillRegistry.register(new MiningSkill());
        SkillRegistry.register(new WoodcuttingSkill());
        SkillRegistry.register(new ExcavationSkill());
        SkillRegistry.register(new HerbalismSkill());
        SkillRegistry.register(new FishingSkill());
        SkillRegistry.register(new SwordsSkill());
        SkillRegistry.register(new AxesSkill());
        SkillRegistry.register(new ArcherySkill());
        SkillRegistry.register(new UnarmedSkill());
        SkillRegistry.register(new AcrobaticsSkill());

        BlockExperienceSourceRegistry.register(MiningExperienceSource.INSTANCE);
        BlockExperienceSourceRegistry.register(WoodcuttingExperienceSource.INSTANCE);
        BlockExperienceSourceRegistry.register(ExcavationExperienceSource.INSTANCE);
        BlockExperienceSourceRegistry.register(HerbalismExperienceSource.INSTANCE);

        DamageModifierRegistry.registerIncoming(CriticalStrikePassive::modifyDamage);
        DamageModifierRegistry.registerIncoming(IronFistsPassive::modifyDamage);
        DamageModifierRegistry.registerIncoming(RollPassive::modifyDamage);
        DamageModifierRegistry.registerArmor(PiercingShotPassive::modifyArmor);

        MiningEvents.register();
        WoodcuttingEvents.register();
        ExcavationEvents.register();
        HerbalismEvents.register();
        SwordsEvents.register();
        AxesEvents.register();
        ArcheryEvents.register();
        UnarmedEvents.register();
        AcrobaticsEvents.register();

        SuperBreakerAbility.register();
        TreeFellerAbility.register();
        GigaDrillAbility.register();
        BleedPassive.register();

        PlayerPlacedBlockEvents.register();

        SkillsCommand.register();
        SkillforgeCommand.register();

        LOGGER.info(
                "Registered {} incoming damage modifier(s) and {} armor modifier(s).",
                DamageModifierRegistry.getIncomingModifierCount(),
                DamageModifierRegistry.getArmorModifierCount()
        );
    }
}