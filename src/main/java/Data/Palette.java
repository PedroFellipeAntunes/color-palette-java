package Data;

import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;

public class Palette {
    private final ColorData[] data;
    private final ColorData[] original;
    
    private final ChannelRange[] ranges;
    
    private final int modeQuantity;
    private final float maxOffset;
    
    /**
     * Create a new Palette from initial colors, channel ranges, and a maximum
     * hue mode count.
     *
     * @param initial starting array of OKLCh colors
     * @param ranges array of three ChannelRange intervals for L, C, and H
     * @param modeQuantity maximum number of hue segments (must be ≥ 1)
     * @throws IllegalArgumentException if initial is empty, ranges length ≠ 3,
     * or modeQuantity less than 1
     */
    public Palette(ColorData[] initial, ChannelRange[] ranges, int modeQuantity) {
        if (initial.length == 0 || ranges.length != 3 || modeQuantity < 1) {
            throw new IllegalArgumentException("Palette requires at least 1 color, exactly 3 rangers and modeQuantity needs to be at least 1");
        }
        
        this.data = new ColorData[initial.length];
        this.original = new ColorData[initial.length];
        
        for (int i = 0; i < initial.length; i++) {
            this.data[i] = new ColorData(initial[i].getX(), initial[i].getY(), initial[i].getZ());
            this.original[i] = new ColorData(initial[i].getX(), initial[i].getY(), initial[i].getZ());
        }
        
        this.ranges = ranges;
        this.modeQuantity = modeQuantity;
        this.maxOffset = computeMaxOffset(data.length);
        System.out.println("MAXOFFSET:"+this.maxOffset);
    }
    
    private float computeMaxOffset(int n) {
        float step = 1f / (n - 1f);
        
        return Math.min(0.33f, step * 0.5f);
    }
    
    /**
     * Generate a new palette by selecting random hue segments and a uniform
     * chroma, then assigning each block of colors to one of the segments.
     */
    public void generate() {
        resetAll();
        
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        int n = data.length;
        
        int maxModes = Math.min(modeQuantity, n);
        int modes = rnd.nextInt(1, maxModes + 1);
        
        // Pick a single random offset for L, to avoid colors which are fully
        // black (L=0) or fully white (L=1)
//        float lOffset = rnd.nextFloat(0f, maxOffset);
//        float lRange = 1f - 2f * lOffset;
        float lMin = rnd.nextFloat(0f, 1f);
        float lMax = rnd.nextFloat(0f, 1f);
        
        if (lMin > lMax) {
            float tmp = lMin;
            
            lMin = lMax;
            lMax = tmp;
        }
        
        if (lMax - lMin < 0.25f) {
            if (lMax + 0.25f <= 1f) {
                lMax += 0.25f;
            } else {
                lMin = Math.max(0f, lMin - 0.25f);
            }
        }
        
        float lRange = lMax - lMin;
        
        float hueStart = rnd.nextFloat(0f, 360f);
        
        // Prepare an array of evenly spaced hue values around the circle
        float[] hues = new float[modes];
        
        boolean clockwise = rnd.nextBoolean();
        
        float step = 360f / modes;
        
        for (int m = 0; m < modes; m++) {
            if (clockwise) {
                hues[m] = (hueStart + m * step) % 360f;
            } else {
                hues[m] = (hueStart - m * step) % 360f;
                
                if (hues[m] < 0f) {
                    hues[m] += 360f;
                }
            }
        }
        
        // Determine how many colors each mode will get
        int baseSize = n / modes;
        int remainder = n % modes;
        
        float chroma = rnd.nextFloat(ranges[1].getMin(), ranges[1].getMax());
        int index = 0;
        
        // Fill in each ColorData with Z=Hue, Y=Chroma and X=L interpolated
        // across the whole array
        for (int m = 0; m < modes; m++) {
            // Distribute the “extra” one-per-mode until remainder is exhausted
            int blockSize = baseSize + (m < remainder ? 1 : 0);
            
            for (int j = 0; j < blockSize; j++, index++) {
                ColorData cd = data[index];
                cd.setZ(hues[m]);
                cd.setY(chroma);
                
                float t = (float) index / (n - 1);
//                cd.setX(lOffset + t * lRange);
                cd.setX(lMin + t * lRange);
            }
        }
    }
    
    /**
     * Generate a smoothly interpolated palette by choosing random key hues and
     * chroma, then linearly interpolating OKLab lightness and hue between keys.
     */
    public void generateInterpolated() {
        resetAll();

        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        int n = data.length;
        
        if (n < 1) {
            return;
        }
        
        int maxModes = Math.min(n, modeQuantity);
        int modes = rnd.nextInt(1, maxModes + 1);
        
//        float dynamicMaxOffset = computeMaxOffset(n);
//        float lOffset = rnd.nextFloat(0f, dynamicMaxOffset);
//        float lRange = 1f - 2f * lOffset;
        float lMin = rnd.nextFloat(0f, 1f);
        float lMax = rnd.nextFloat(0f, 1f);
        
        if (lMin > lMax) {
            float tmp = lMin;
            
            lMin = lMax;
            lMax = tmp;
        }
        
        if (lMax - lMin < 0.25f) {
            if (lMax + 0.25f <= 1f) {
                lMax += 0.25f;
            } else {
                lMin = Math.max(0f, lMin - 0.25f);
            }
        }
        
        float lRange = lMax - lMin;
        
        float chroma = rnd.nextFloat(ranges[1].getMin(), ranges[1].getMax());
        
        float hueStart = rnd.nextFloat(0f, 360f);
        boolean cw = rnd.nextBoolean();
        
        float[] keyHues = new float[modes];
        float step = 360f / modes;
        
        for (int i = 0; i < modes; i++) {
            float raw = cw ? hueStart + i * step : hueStart - i * step;
            
            keyHues[i] = ((raw % 360f) + 360f) % 360f;
        }
        
        // If there’s only one hue mode, interpolate L across the whole array
        // but keep hue/chroma fixed
        if (modes == 1) {
            for (int i = 0; i < n; i++) {
                data[i].setY(chroma);
                data[i].setZ(keyHues[0]);
                
                float t = (float) i / (n - 1);
//                data[i].setX(lOffset + t * lRange);
                data[i].setX(lMin + t * lRange);
            }
            
            return;
        }
        
        // Compute integer positions of each key hue in the array
        int[] keyPos = new int[modes];
        
        for (int i = 0; i < modes; i++) {
            float p = i * (n - 1f) / (modes - 1f);
            keyPos[i] = Math.round(p);
            
            // Also assign the L value at each key position
//            float keyL = lOffset + ((float) i / (modes - 1)) * lRange;
            float keyL = lMin + ((float) i / (modes - 1)) * lRange;
            
            data[keyPos[i]].setY(chroma);
            data[keyPos[i]].setZ(keyHues[i]);
            data[keyPos[i]].setX(keyL);
        }
        
        // Interpolate between each pair of key points in OKLab space,
        // then convert back to OKLCh for storing in data[]
        for (int seg = 0; seg < modes - 1; seg++) {
            int start = keyPos[seg], end = keyPos[seg + 1], span = end - start;
            
            ColorData labStart = new ColorData(
                    data[start].getX(), data[start].getY(), data[start].getZ()
            ).oklchToOklab();
            
            ColorData labEnd = new ColorData(
                    data[end].getX(), data[end].getY(), data[end].getZ()
            ).oklchToOklab();
            
            // Interpolate each channel L, a, b in OKLab
            for (int j = 1; j < span; j++) {
                float t = (float) j / span;
                
                float L = labStart.getX() + t * (labEnd.getX() - labStart.getX());
                float a = labStart.getY() + t * (labEnd.getY() - labStart.getY());
                float b = labStart.getZ() + t * (labEnd.getZ() - labStart.getZ());
                
                ColorData interp = new ColorData(L, a, b).oklabToOklch();
                
                int idx = start + j;
                
                data[idx].setX(interp.getX());
                data[idx].setY(interp.getY());
                data[idx].setZ(interp.getZ());
            }
        }
    }
    
    /**
     * Randomize all three OKLCh channels of the specified color independently
     * within their configured ranges.
     *
     * @param index index of the color to randomize
     */
    public void randomSingle(int index) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        ColorData color = data[index];
        
        float newL = randomInRange(rnd, ranges[0].getMin(), ranges[0].getMax());
        float newA = randomInRange(rnd, ranges[1].getMin(), ranges[1].getMax());
        float newB = randomInRange(rnd, ranges[2].getMin(), ranges[2].getMax());
        
        color.setX(newL);
        color.setY(newA);
        color.setZ(newB);
    }
    
    /**
     * Randomize all three OKLCh channels of every color in the palette
     * independently within their configured ranges.
     */
    public void randomAll() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        
        for (ColorData color : data) {
            float newL = randomInRange(rnd, ranges[0].getMin(), ranges[0].getMax());
            float newA = randomInRange(rnd, ranges[1].getMin(), ranges[1].getMax());
            float newB = randomInRange(rnd, ranges[2].getMin(), ranges[2].getMax());

            color.setX(newL);
            color.setY(newA);
            color.setZ(newB);
        }
    }
    
    private float randomInRange(ThreadLocalRandom rnd, float min, float max) {
        return min + rnd.nextFloat() * (max - min);
    }
    
    /**
     * Invert the hue of every color in the palette by 180°, wrapping around
     * 360°.
     */
    public void invert() {
        for (ColorData color : data) {
            float hue = color.getZ();
            color.setZ((hue + 180f) % 360f);
        }
    }
    
    /**
     * Reset the specified color back to its original value provided at
     * construction.
     *
     * @param index index of the color to reset
     */
    public void reset(int index) {
        data[index].setX(original[index].getX());
        data[index].setY(original[index].getY());
        data[index].setZ(original[index].getZ());
    }
    
    /**
     * Reset the entire palette back to the original colors provided at
     * construction.
     */
    public void resetAll() {
        for (int i = 0; i < data.length; i++) {
            data[i].setX(original[i].getX());
            data[i].setY(original[i].getY());
            data[i].setZ(original[i].getZ());
        }
    }
    
    /**
     * Convert the current OKLCh palette to an array of AWT Color instances.
     * Performs OKLCh → OKLab → sRGB conversion and 8-bit quantization.
     *
     * @return array of java.awt.Color matching this palette
     */
    public Color[] toAwtColors() {
        Color[] cols = new Color[data.length];
        
        for (int i = 0; i < data.length; i++) {
            int[] rgb = data[i].oklchToRgb().toRgb255();
            cols[i] = new Color(rgb[0], rgb[1], rgb[2]);
        }

        return cols;
    }
    
    /**
     * Convert the specified OKLCh color to an AWT Color instance. Performs
     * OKLCh → OKLab → sRGB conversion and 8-bit quantization.
     *
     * @param index index of the color to convert
     * @return java.awt.Color corresponding to the palette color
     */
    public Color toAwtColor(int index) {
        int[] rgb = data[index].oklchToRgb().toRgb255();

        return new Color(rgb[0], rgb[1], rgb[2]);
    }
    
    /**
     * Converts the color at the specified index from OKLCH to an RGB hex
     * string.
     *
     * @param index the index of the color in the palette array
     * @return the color as a hexadecimal string in the format "#RRGGBB"
     */
    public String rgbToHex(int index) {
        int[] rgb = data[index].oklchToRgb().toRgb255();
        
        return String.format("#%02X%02X%02X", rgb[0], rgb[1], rgb[2]);
    }
    
    /**
     * Builds a bracketed list containing the hexadecimal representation of
     * every color in the palette.
     *
     * @return a string in the format "[#RRGGBB,#RRGGBB,…]" representing the
     * full palette
     */
    public String paletteToHex() {
        String hexPalette = "[";
        
        for (int i = 0; i < data.length; i++) {
            int[] rgb = data[i].oklchToRgb().toRgb255();
            
            hexPalette += String.format("#%02X%02X%02X", rgb[0], rgb[1], rgb[2]);
            
            if (i < data.length - 1) {
                hexPalette += ",";
            }
        }
        
        hexPalette += "]";
        
        return hexPalette;
    }
    
    /**
     * Parses the given hexadecimal string and updates the color at the
     * specified index in the palette, converting from RGB into OKLCH.
     *
     * @param hex the hexadecimal color string (with or without leading '#')
     * @param index the index in the palette array to update
     * @throws IllegalArgumentException if the hex string is not exactly six
     * hexadecimal digits
     */
    public void hexToRgb(String hex, int index) {
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        
        if (h.length() != 6) {
            throw new IllegalArgumentException("Hex must be 6 digits: " + hex);
        }
        
        int r = Integer.parseInt(h.substring(0, 2), 16);
        int g = Integer.parseInt(h.substring(2, 4), 16);
        int b = Integer.parseInt(h.substring(4, 6), 16);
        
        data[index] = new ColorData(r, g, b).rgbToOklab().oklabToOklch();
    }
    
    public ColorData[] getData() {
        return data;
    }
    
    public ColorData[] getOriginal() {
        return original;
    }
}