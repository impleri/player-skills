package net.impleri.playerskills.api;

enum TeamModeType {
    OFF,
    SHARED,
    SPLIT_EVENLY,
    PYRAMID,
    PROPORTIONAL,
    LIMITED,
}

public class TeamMode {
    public static TeamMode off() {
        return new TeamMode(TeamModeType.OFF);
    }

    public static TeamMode shared() {
        return new TeamMode(TeamModeType.SHARED);
    }

    public static TeamMode splitEvenly() {
        return new TeamMode(TeamModeType.SPLIT_EVENLY);
    }

    public static TeamMode pyramid() {
        return new TeamMode(TeamModeType.PYRAMID);
    }

    public static TeamMode proportional(Double percentage) {
        return new TeamMode(TeamModeType.PROPORTIONAL, percentage);
    }

    public static TeamMode limited(Double amount) {
        return new TeamMode(TeamModeType.LIMITED, amount);
    }

    private final TeamModeType type;
    public final Double rate;

    private TeamMode(TeamModeType type, Double rate) {
        this.type = type;
        this.rate = rate;
    }

    private TeamMode(TeamModeType type) {
        this(type, null);
    }

    public boolean isOff() {
        return type == TeamModeType.OFF;
    }

    public boolean isShared() {
        return type == TeamModeType.SHARED;
    }

    public boolean isSplitEvenly() {
        return type == TeamModeType.SPLIT_EVENLY;
    }

    public boolean isPyramid() {
        return type == TeamModeType.PYRAMID;
    }

    public boolean isProportional() {
        return type == TeamModeType.PROPORTIONAL;
    }

    public boolean isLimited() {
        return type == TeamModeType.LIMITED;
    }
}
