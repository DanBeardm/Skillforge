package dev.olliesbrother.mixin;

import dev.olliesbrother.skill.abilities.passive.AnglersFortunePassive;
import dev.olliesbrother.skill.events.FishingEvents;
import net.minecraft.advancement.criterion.FishingRodHookedCriterion;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin {

    /*
     * Vanilla passes the actual generated fishing loot
     * into FishingRodHookedCriterion.trigger().
     *
     * Redirecting this invocation lets Skillforge inspect
     * exactly what was caught without changing the fishing
     * loot generation itself.
     */
    @Redirect(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/advancement/criterion/FishingRodHookedCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/projectile/FishingBobberEntity;Ljava/util/Collection;)V"
            )
    )
    private void skillforge$handleFishingLoot(
            FishingRodHookedCriterion criterion,
            ServerPlayerEntity player,
            ItemStack rod,
            FishingBobberEntity bobber,
            Collection<ItemStack> fishingLoot
    ) {

        /*
         * Preserve vanilla advancement behaviour using
         * the catch vanilla actually rolled.
         */
        criterion.trigger(
                player,
                rod,
                bobber,
                fishingLoot
        );

        /*
         * Skillforge may upgrade the catch before
         * FishingBobberEntity spawns the loot.
         */
        AnglersFortunePassive.tryUpgradeCatch(
                player,
                rod,
                bobber,
                fishingLoot
        );

        /*
         * Award XP based on the final catch.
         *
         * Therefore:
         *
         * Junk upgraded to Fish     -> Fish XP
         * Fish upgraded to Treasure -> Treasure XP
         */
        FishingEvents.onSuccessfulCatch(
                player,
                fishingLoot
        );
    }
}