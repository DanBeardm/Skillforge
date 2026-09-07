package dev.olliesbrother.skill.events;

import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.skill.ExperienceManager;
import dev.olliesbrother.skill.Skill;
import dev.olliesbrother.skill.SkillRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Collection;

public final class FishingEvents {

    private static final Identifier FISHING_ID =
            Identifier.of(
                    "skillforge",
                    "fishing"
            );

    private FishingEvents() {
    }

    public static void onSuccessfulCatch(
            ServerPlayerEntity player,
            Collection<ItemStack> loot
    ) {

        if (player.isCreative()) {
            return;
        }

        if (!ConfigManager.get()
                .fishing
                .enabled) {

            return;
        }

        if (loot == null
                || loot.isEmpty()) {

            return;
        }

        CatchType catchType =
                classifyCatch(loot);

        double experience =
                getExperience(catchType);

        if (experience <= 0) {
            return;
        }

        Skill fishing =
                SkillRegistry.get(
                        FISHING_ID
                );

        if (fishing == null) {
            return;
        }

        ExperienceManager.addExperience(
                player,
                fishing,
                experience
        );
    }

    public static CatchType classifyCatch(
            Collection<ItemStack> loot
    ) {

        boolean containsFish =
                false;

        /*
         * We give the catch its most valuable category.
         *
         * Treasure > Fish > Junk
         *
         * Normally vanilla fishing produces one main
         * stack, but this also behaves sensibly if a
         * modded loot table produces several.
         */
        for (ItemStack stack : loot) {

            if (stack.isEmpty()) {
                continue;
            }

            if (isTreasure(stack)) {
                return CatchType.TREASURE;
            }

            if (stack.isIn(ItemTags.FISHES)) {
                containsFish = true;
            }
        }

        if (containsFish) {
            return CatchType.FISH;
        }

        return CatchType.JUNK;
    }

    private static boolean isTreasure(
            ItemStack stack
    ) {

        Identifier itemId =
                Registries.ITEM.getId(
                        stack.getItem()
                );

        return ConfigManager.get()
                .fishing
                .treasureItems
                .contains(
                        itemId.toString()
                );
    }

    private static double getExperience(
            CatchType catchType
    ) {

        var experience =
                ConfigManager.get()
                        .fishing
                        .experience;

        return switch (catchType) {

            case FISH ->
                    experience.fish;

            case JUNK ->
                    experience.junk;

            case TREASURE ->
                    experience.treasure;
        };
    }

    public enum CatchType {
        FISH,
        JUNK,
        TREASURE
    }
}