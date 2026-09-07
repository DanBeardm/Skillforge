package dev.olliesbrother.skill;

import net.minecraft.util.Identifier;

public abstract class Skill {

    private final Identifier id;
    private final String displayName;

    protected Skill(Identifier id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public Identifier getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }
}

