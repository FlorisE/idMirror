package tsukuba.emp.mirrorgl.util;

public class Constants {
    /**
     * The amount of cells rendered per column and per row, e.g. BUFFER_NN = 40 implies a 40X40 grid
     */
    public static final int BUFFER_NN = 40;

    /**
     * The time it takes for the effect to fade in and fade out
     */
    public static final int FADE_TIME = 5000;

    /**
     * After how much time a picture will be taken
     */
    public static final int PICTURE_TIME = 2000;

    /**
     * The duration of the interaction, excluding fade in/fade out time. Set to -1 for infinite
     */
    public static final int INTERACTION_TIME = 10000;

    /**
     * Only draw cells which are located in an ellipse area, used when covering part of the screen with the
     * idMirror shell
     */
    public static final boolean DRAW_ELLIPSE = true;
}
