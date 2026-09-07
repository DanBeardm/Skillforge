package dev.olliesbrother.skill.skills;

import dev.olliesbrother.skill.Skill;
import net.minecraft.util.Identifier;

public final class MiningSkill extends Skill {

    public MiningSkill() {
        super(
                Identifier.of("skillforge", "mining"),
                "Mining"
        );
    }
}