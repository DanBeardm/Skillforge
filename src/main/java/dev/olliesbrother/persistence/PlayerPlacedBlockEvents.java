package dev.olliesbrother.persistence;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.world.ServerWorld;

public final class PlayerPlacedBlockEvents {

    private PlayerPlacedBlockEvents() {
    }

    public static void register() {

        PlayerBlockBreakEvents.AFTER.register(
                (world, player, pos, state, blockEntity) -> {

                    if (!(world instanceof ServerWorld serverWorld)) {
                        return;
                    }

                    SkillforgeState
                            .get(serverWorld.getServer())
                            .removePlayerPlaced(
                                    serverWorld,
                                    pos
                            );
                }
        );
    }
}