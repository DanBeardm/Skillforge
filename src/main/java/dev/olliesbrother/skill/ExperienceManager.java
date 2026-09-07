package dev.olliesbrother.skill;

import dev.olliesbrother.player.PlayerDataManager;
import dev.olliesbrother.player.PlayerSkillData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class ExperienceManager {

    private ExperienceManager() {
    }

    public static void addExperience(
            ServerPlayerEntity player,
            Skill skill,
            double amount
    ) {

        if (amount <= 0) {
            return;
        }

        PlayerSkillData playerData =
                PlayerDataManager.get(player);

        SkillInstance instance =
                playerData.getSkill(skill);

        instance.addExperience(amount);

        player.sendMessage(
                Text.literal(
                        "+" + amount + " " + skill.getDisplayName() + " XP"
                ),
                true
        );

        checkForLevelUp(player, skill, instance);

        PlayerDataManager.markDirty(player);
    }

    private static void checkForLevelUp(
            ServerPlayerEntity player,
            Skill skill,
            SkillInstance instance
    ) {

        double requiredExperience =
                LevelCurve.getRequiredExperience(instance.getLevel());

        while (instance.getExperience() >= requiredExperience) {

            instance.removeExperience(requiredExperience);
            instance.levelUp();

            player.sendMessage(
                    Text.literal(
                            skill.getDisplayName()
                                    + " increased to level "
                                    + instance.getLevel()
                                    + "!"
                    ),
                    false
            );

            requiredExperience =
                    LevelCurve.getRequiredExperience(instance.getLevel());
        }
    }
}