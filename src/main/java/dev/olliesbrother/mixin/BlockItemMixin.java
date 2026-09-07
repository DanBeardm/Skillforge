package dev.olliesbrother.mixin;

import dev.olliesbrother.persistence.SkillforgeState;
import dev.olliesbrother.experience.BlockExperienceSourceRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {

    @Inject(
            method = "place(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z",
            at = @At("RETURN")
    )
    private void skillforge$trackPlayerPlacedBlock(
            ItemPlacementContext context,
            BlockState state,
            CallbackInfoReturnable<Boolean> cir
    ) {

        // Placement failed.
        if (!cir.getReturnValue()) {
            return;
        }

        if (!(context.getWorld()
                instanceof ServerWorld serverWorld)) {
            return;
        }

        PlayerEntity player =
                context.getPlayer();

        // Dispensers etc. are not player placement.
        if (player == null) {
            return;
        }

        if (!BlockExperienceSourceRegistry
                .awardsExperience(state)) {

            return;
        }

        BlockPos pos =
                context.getBlockPos();

        SkillforgeState
                .get(serverWorld.getServer())
                .markPlayerPlaced(
                        serverWorld,
                        pos
                );
    }
}