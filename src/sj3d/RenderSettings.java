package sj3d;

import static sj3d.Material.SMOOTH;
import static sj3d.Material.TEXTURED;

/**
 *
 * A collection of settings for render modes and quality. The default settings
 * are as follows:<br>
 * <ul>
 * <li>Motion blur: off</li>
 * <li>Maximum shade mode: smooth & textured</li>
 * <li>Shadows: off</li>
 * <li>Antialiasing: 1x (none)</li>
 * </ul>
 *
 * @author Calvin Loncaric
 *
 */
public final class RenderSettings {

    // Settings

    /**
     * The amount of anti-aliasing
     */
    float aaFactor = 1;

    /**
     * The motion blur factor -- that is, the base alpha value for new frames.
     */
    int mblur = 0xFF000000;

    /**
     * The maximum shading mode for the renderer. No triangle will be rendered
     * with more complex settings than this.
     */
    int shadeMode = SMOOTH | TEXTURED;

    /**
     * Enable or disable shadows
     */
    private boolean shadows = false;

    // Methods

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
     * Identical to calling <code>setAA((float)factor)</code>
     *
     * @param factor
     */
    public void setAA(double factor) {
        setAA((float) factor);
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
     * Identical to calling <code>setMotionBlur((float)factor)</code>
     *
     * @param factor
     */
    public void setMotionBlur(double factor) {
        setMotionBlur((float) factor);
    }

    /**
     * Determine whether motion blur is enabled in these settings.
     *
     * @return true if motion blur is enabled, or false otherwise
     */
    public boolean hasMotionBlur() {
        return mblur >> 24 < 0xFF;
    }

    /**
     * Set whether a given shading mode will be allowed.
     *
     * @param mode
     *            the shading mode
     * @param flag
     *            true to enable the mode, false to disable i
     */
    public void setShading(int mode, boolean flag) {
        if (flag)
            shadeMode |= mode;
        else
            shadeMode ^= mode;
    }

    /**
     * Determine whether a given shading mode is enabled.
     *
     * @param mode
     *            the mode to check
     * @return true if <code>mode</code> is enabled, or false otherwise
     */
    public boolean hasShading(int mode) {
        return (shadeMode & mode) != 0;
    }

    /**
     * Set whether shadows will be rendered or not.
     *
     * @param flag
     *            true to enable shadows, false to disable them
     */
    public void setShadows(boolean flag) {
        shadows = flag;
    }

    /**
     * Determine whether shadows are currently enabled.
     *
     * @return true if shadows are enabled, or false otherwise
     */
    public boolean hasShadows() {
        return shadows;
    }

}
