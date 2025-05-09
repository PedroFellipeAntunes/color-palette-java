package Data;

public class ColorData {
    private float x;
    private float y;
    private float z;
    
    /**
     * Constructs a new ColorData with explicit channel values.
     *
     * @param x Channel X value or R in RGB (normalized)
     * @param y Channel Y value or B in RGB (normalized)
     * @param z Channel Z value or G in RGB (normalized)
     */
    public ColorData(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * Constructs a new ColorData from 8-bit RGB components. Each component is
     * normalized to [0..1] and clamped.
     *
     * @param r Red component [0..255].
     * @param g Green component [0..255].
     * @param b Blue component [0..255].
     */
    public ColorData(int r, int g, int b) {
        this.x = clamp01(r / 255f);
        this.y = clamp01(g / 255f);
        this.z = clamp01(b / 255f);
    }
    
    /**
     * Converts this color’s internal [0..1] channels to 8-bit RGB.
     *
     * @return An int[3] array {r, g, b}, each in [0..255].
     */
    public int[] toRgb255() {
        int r = Math.round(clamp01(x) * 255f);
        int g = Math.round(clamp01(y) * 255f);
        int b = Math.round(clamp01(z) * 255f);
        
        return new int[]{r, g, b};
    }
    
    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }
    
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setZ(float z) { this.z = z; }
    
    @Override
    public String toString() {
        return String.format("(%.4f, %.4f, %.4f)", x, y, z);
    }
    
    /**
     * Converts from sRGB [0..1] to OKLab (L, a, b) using Ottosson’s matrices
     * and cubic transform.
     *
     * @return A new ColorData containing OKLab components.
     */
    public ColorData rgbToOklab() {
        float r = invCompand(x);
        float g = invCompand(y);
        float b = invCompand(z);
        
        float Lm =  0.4122214708f * r + 0.5363325363f * g + 0.0514459929f * b;
        float Mm =  0.2119034982f * r + 0.6806995451f * g + 0.1073969566f * b;
        float Sm =  0.0883024619f * r + 0.2817188376f * g + 0.6299787005f * b;
        
        float l_ = (float)Math.cbrt(Lm);
        float m_ = (float)Math.cbrt(Mm);
        float s_ = (float)Math.cbrt(Sm);
        
        float L =  0.2104542553f * l_ + 0.7936177850f * m_ - 0.0040720468f * s_;
        float a =  1.9779984951f * l_ - 2.4285922050f * m_ + 0.4505937099f * s_;
        float b_ = 0.0259040371f * l_ + 0.7827717662f * m_ - 0.8086757660f * s_;
        
        return new ColorData(L, a, b_);
    }
    
    /**
     * Converts from sRGB [0..1] to OKLab and expresses each component as a
     * percentage: L in [0..100], a and b as ±100% of their ±0.4 range.
     *
     * @return A new ColorData containing percentage OKLab values.
     */
    public ColorData rgbToOklabPercent() {
        ColorData ok = rgbToOklab();
        
        float L = ok.getX() * 100f;
        float A = (ok.getY() / 0.4f) * 100f;
        float B = (ok.getZ() / 0.4f) * 100f;
        
        return new ColorData(L, A, B);
    }
    
    /**
     * Converts from OKLab (L, a, b) to OKLCh (L, C, H°).
     *
     * @return A new ColorData containing L, chroma C, and hue H in degrees
     * [0..360).
     */
    public ColorData oklabToOklch() {
        float L = x;
        float a = y;
        float b = z;
        
        float C = (float)Math.hypot(a, b);
        float hRad = (float)Math.atan2(b, a);
        float H = (hRad >= 0f ? (float)Math.toDegrees(hRad) : (float)Math.toDegrees(hRad) + 360f);
        
        return new ColorData(L, C, H);
    }
    
    /**
     * Converts from OKLab to OKLCh and expresses each component as a
     * percentage: L% in [0..100], C% relative to max ~0.4, H% of full 360°.
     *
     * @return A new ColorData containing percentage OKLCh values.
     */
    public ColorData oklabToOklchPercent() {
        ColorData lch = oklabToOklch();
        
        float Lpct = lch.getX() * 100f;
        float Cpct = (lch.getY() / 0.4f) * 100f;
        float Hpct = (lch.getZ() / 360f) * 100f;
        
        return new ColorData(Lpct, Cpct, Hpct);
    }
    
    /**
     * Converts from OKLCh (L, C, H°) back to OKLab (L, a, b).
     *
     * @return A new ColorData containing OKLab Cartesian components.
     */
    public ColorData oklchToOklab() {
        float L = x;
        float C = y;
        float Hdeg = z;
        
        float hRad = (float)Math.toRadians(Hdeg);
        float a = C * (float)Math.cos(hRad);
        float b = C * (float)Math.sin(hRad);
        
        return new ColorData(L, a, b);
    }
    
    /**
     * Converts from OKLab (L, a, b) to linear sRGB [0..1], applying: 1)
     * OKLab→LMS inverse, 2) cubing, 3) LMS→RGB matrix, 4) sRGB companding, 5)
     * clamping.
     *
     * @return A new ColorData containing sRGB components.
     */
    public ColorData oklabToRgb() {
        float l_ = x + 0.3963377774f * y + 0.2158037573f * z;
        float m_ = x - 0.1055613458f * y - 0.0638541728f * z;
        float s_ = x - 0.0894841775f * y - 1.2914855480f * z;
        
        float Lm = l_ * l_ * l_;
        float Mm = m_ * m_ * m_;
        float Sm = s_ * s_ * s_;
        
        float rLin =  4.0767416621f * Lm - 3.3077115901f * Mm + 0.2309699292f * Sm;
        float gLin = -1.2684380046f * Lm + 2.6097574011f * Mm - 0.3413193965f * Sm;
        float bLin = -0.0041960863f * Lm - 0.7034186147f * Mm + 1.7076147010f * Sm;
        
        float r = compand(rLin);
        float g = compand(gLin);
        float b = compand(bLin);
        
        return new ColorData(clamp01(r), clamp01(g), clamp01(b));
    }

    private static float invCompand(float c) {
        return c <= 0.04045f ? c / 12.92f : (float)Math.pow((c + 0.055f) / 1.055f, 2.4f);
    }
    
    private static float compand(float c) {
        if (c <= 0f) return 0f;
        if (c < 0.0031308f) return 12.92f * c;
        
        return 1.055f * (float)Math.pow(c, 1.0/2.4) - 0.055f;
    }
    
    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}