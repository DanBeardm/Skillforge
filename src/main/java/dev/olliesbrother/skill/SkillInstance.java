package dev.olliesbrother.skill;

public class SkillInstance {

    private int level;
    private double experience;

    public SkillInstance() {
        this(1, 0);
    }

    public SkillInstance(int level, double experience) {
        this.level = level;
        this.experience = experience;
    }

    public int getLevel() {
        return level;
    }

    public double getExperience() {
        return experience;
    }

    public void addExperience(double amount) {
        experience += amount;
    }

    public void removeExperience(double amount) {
        experience -= amount;
    }

    public void levelUp() {
        level++;
    }
}