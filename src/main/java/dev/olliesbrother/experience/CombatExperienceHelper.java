package dev.olliesbrother.experience;

import net.minecraft.entity.LivingEntity;

public final class CombatExperienceHelper {

    private CombatExperienceHelper() {
    }

    public static double calculateExperience(
            LivingEntity target,
            double experiencePerHealth,
            double minimumExperience,
            double maximumExperience
    ) {

        double experience =
                target.getMaxHealth()
                        * experiencePerHealth;

        experience =
                Math.max(
                        experience,
                        minimumExperience
                );

        experience =
                Math.min(
                        experience,
                        maximumExperience
                );

        return Math.max(
                0.0,
                experience
        );
    }
}