package Data;

public class ColorData {
    private float x;
    private float y;
    private float z;
    
    private final int maxFallbackIterations = 20;

    // ────────────────────────────────────────────────────────────────────────────
    // Constructors
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Create a ColorData with normalized channels in [0..1].
     *
     * @param x channel X or red
     * @param y channel Y or green
     * @param z channel Z or blue
     */
    public ColorData(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Create a ColorData from RGB components.
     * Values are normalized to [0..1] and clamped.
     *
     * @param r red 0–255
     * @param g green 0–255
     * @param b blue 0–255
     */
    public ColorData(int r, int g, int b) {
        this.x = clamp01(r / 255f);
        this.y = clamp01(g / 255f);
        this.z = clamp01(b / 255f);
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Accessors
    // ────────────────────────────────────────────────────────────────────────────

    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }

    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setZ(float z) { this.z = z; }

    // ────────────────────────────────────────────────────────────────────────────
    // Conversion Methods
    // ────────────────────────────────────────────────────────────────────────────
    
    /**
     * Convert this normalized channels to non-normalized.
     * Considers that this object is normalized RGB.
     *
     * @return int[3] {r, g, b} each in [0..255]
     */
    public int[] toRgb255() {
        int r = Math.round(clamp01(x) * 255f);
        int g = Math.round(clamp01(y) * 255f);
        int b = Math.round(clamp01(z) * 255f);
        
        return new int[]{r, g, b};
    }
    
    /**
     * Convert this sRGB [0..1] to OKLab using Ottosson’s matrices.
     *
     * @return new ColorData(L, a, b)
     */
    public ColorData rgbToOklab() {
        float r = invCompand(x);
        float g = invCompand(y);
        float b = invCompand(z);

        float Lm =  0.4122214708f * r + 0.5363325363f * g + 0.0514459929f * b;
        float Mm =  0.2119034982f * r + 0.6806995451f * g + 0.1073969566f * b;
        float Sm =  0.0883024619f * r + 0.2817188376f * g + 0.6299787005f * b;

        float l_ = (float) Math.cbrt(Lm);
        float m_ = (float) Math.cbrt(Mm);
        float s_ = (float) Math.cbrt(Sm);

        float L =  0.2104542553f * l_ + 0.7936177850f * m_ - 0.0040720468f * s_;
        float a =  1.9779984951f * l_ - 2.4285922050f * m_ + 0.4505937099f * s_;
        float b_ = 0.0259040371f * l_ + 0.7827717662f * m_ - 0.8086757660f * s_;

        return new ColorData(L, a, b_);
    }

    /**
     * Convert this OKLab (L, a, b) to OKLCh (L, C, H°).
     *
     * @return new ColorData(L, C, H in degrees)
     */
    public ColorData oklabToOklch() {
        float C = Math.min((float) Math.hypot(y, z), 0.37f);
        float hRad = (float) Math.atan2(z, y);
        float H = (hRad >= 0f
            ? (float) Math.toDegrees(hRad)
            : (float) Math.toDegrees(hRad) + 360f);
        
        return new ColorData(x, C, H);
    }

    /**
     * Convert this OKLCh (L, C, H°) to OKLab (L, a, b).
     *
     * @return new ColorData(L, a, b)
     */
    public ColorData oklchToOklab() {
        float hRad = (float) Math.toRadians(z);
        float a = y * (float) Math.cos(hRad);
        float b = y * (float) Math.sin(hRad);
        
        return new ColorData(x, a, b);
    }

    /**
     * Convert this OKLCh to linear sRGB with closest-fallback by chroma.
     *
     * @return new ColorData(rLin, gLin, bLin) in [0..1] linear sRGB
     */
    public ColorData oklchToRgb() {
        float originalC = y;
        float low = 0f, high = originalC, mid;
        ColorData candidate;

        for (int i = 0; i < maxFallbackIterations; i++) {
            mid = (low + high) * 0.5f;
            
            candidate = new ColorData(x, mid, z).oklchToOklab().oklabToRgb(true);
            
            if (inGamut(candidate)) {
                low = mid;
            } else {
                high = mid;
            }
        }

        return new ColorData(x, low, z).oklchToOklab().oklabToRgb(false);
    }
    
    /**
     * Convert this OKLab color to sRGB.
     * 
     * If linear is true, returns straight linear-RGB channels;
     * otherwise applies the sRGB companding curve and clamps to [0..1].
     *
     * @param linear whether to output linear sRGB (true) or companded sRGB
     * (false)
     * @return a new ColorData containing R, G, B channels
     */
    public ColorData oklabToRgb(boolean linear) {
        float l_ = x + 0.3963377774f * y + 0.2158037573f * z;
        float m_ = x - 0.1055613458f * y - 0.0638541728f * z;
        float s_ = x - 0.0894841775f * y - 1.2914855480f * z;

        float Lm = l_ * l_ * l_;
        float Mm = m_ * m_ * m_;
        float Sm = s_ * s_ * s_;

        float rLin = 4.0767416621f * Lm - 3.3077115901f * Mm + 0.2309699292f * Sm;
        float gLin = -1.2684380046f * Lm + 2.6097574011f * Mm - 0.3413193965f * Sm;
        float bLin = -0.0041960863f * Lm - 0.7034186147f * Mm + 1.7076147010f * Sm;

        if (linear) {
            return new ColorData(rLin, gLin, bLin);
        }

        return new ColorData(clamp01(compand(rLin)), clamp01(compand(gLin)), clamp01(compand(bLin)));
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Private Helpers
    // ────────────────────────────────────────────────────────────────────────────

    private boolean inGamut(ColorData c) {
        return c.x >= 0f && c.x <= 1f
            && c.y >= 0f && c.y <= 1f
            && c.z >= 0f && c.z <= 1f;
    }

    private static float invCompand(float c) {
        if (Math.abs(c) <= 0.04045f) return c / 12.92f;
        
        return Math.signum(c) * (float) Math.pow((Math.abs(c) + 0.055f) / 1.055f, 2.4f);
    }

    private static float compand(float c) {
        if (c <= 0f) return 0f;
        
        if (c < 0.0031308f) return 12.92f * c;
        
        return 1.055f * (float) Math.pow(c, 1.0 / 2.4) - 0.055f;
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}