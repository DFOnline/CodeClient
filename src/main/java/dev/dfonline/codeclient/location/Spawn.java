package dev.dfonline.codeclient.location;

public class Spawn extends Location {
    private boolean hasJustJoined = false;

    public Spawn() {
        hasJustJoined = true;
    }

    /**
     * If the player has gone to spawn, returns true once if they have just gone to spawn.
     */
    public boolean consumeHasJustJoined() {
        boolean justJoined = hasJustJoined;
        hasJustJoined = false;
        return justJoined;
    }

    @Override
    public String name() {
        return "spawn";
    }

    enum Node { // everything in /server from beta moment
        NODE1,
        NODE2,
        NODE3,
        NODE4,
        NODE5,
        NODE6,
        NODE7,
        BETA,
        EVENT,
        DEV,
        DEV2,
        DEV3,
        DEV_EVENT,
        BUILD,
    }
}
