package dev.dfonline.codeclient.websocket.scope;

public enum AuthScope {
    DEFAULT(AuthScopeDangerLevel.DEFAULT, "default"),
    READ_PLOT(AuthScopeDangerLevel.CAUTION, "read_plot"),
    MOVEMENT(AuthScopeDangerLevel.CAUTION, "movement"),
    INVENTORY(AuthScopeDangerLevel.SAFE, "inventory"),
    WRITE_CODE(AuthScopeDangerLevel.RISKY, "write_code"),
    CLEAR_PLOT(AuthScopeDangerLevel.DANGEROUS, "clear_plot"),
    ;

    public final AuthScopeDangerLevel dangerLevel;
    public final String translationKey;
    AuthScope(AuthScopeDangerLevel dangerLevel, String translationKey) {
        this.dangerLevel = dangerLevel;
        this.translationKey = translationKey;
    }
}
