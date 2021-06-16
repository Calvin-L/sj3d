package sj3d;

/**
 * A collection of settings for render modes and quality. The default settings
 * are as follows:<br>
 * <ul>
 * <li>Motion blur: off</li>
 * <li>Maximum shade mode: smooth & textured</li>
 * <li>Shadows: off</li>
 * <li>Antialiasing: 1x (none)</li>
 * </ul>
 */
public final class RenderSettings {

    // Settings

    /**
     * The background color
     */
    int bgcolor = 0;

    /**
     * The amount of anti-aliasing
     */
    float aaFactor = 1;

    /**
     * The motion blur factor -- that is, the base alpha value for new frames.
     */
    int mblur = 0xFF000000;

    // Methods

    public void setBackgroundColor(int color) {
        bgcolor = color & 0x00FFFFFF;
    }

    /**
     * Set the amount of anti-aliasing. Common values are 1 (no anti-aliasing)
     * and 2 (standard anti-aliasing). Intermediate values may be used for more
     * specific performance-to-speed ratios, larger values can be used for
     * higher quality, and even smaller values can be used for low-quality
     * rendering. (There is no point to this last one, however, as the overhead
     * and processing associated with scaling the result will effectively
     * eliminate any speed gains.)
     *
     * @param factor
     */
    public void setAA(float factor) {
        aaFactor = factor;
    }

    /**
     * Determine whether anti-aliasing is enabled.
     *
     * @return true if anti-aliasing is enabled, or false otherwise.
     */
    public boolean hasAA() {
        return aaFactor != 1;
    }

    /**
     * Set the amount of motion blur.
     *
     * @param factor
     *            a float value between 0 (for no motion blur) and 0.99 (for
     *            full motion blur)
     */
    public void setMotionBlur(float factor) {
        factor = Math.max(factor, 0);
        factor = Math.min(factor, 0.99f);
        mblur = Math.round((1.0f - factor) * 255.0f) << 24;
    }

    /**
     * Determine whether motion blur is enabled in these settings.
     *
     * @return true if motion blur is enabled, or false otherwise
     */
    public boolean hasMotionBlur() {
        return mblur >> 24 < 0xFF;
    }

}
