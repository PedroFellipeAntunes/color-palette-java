package Data;

import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;

public class Palette {
    private final ColorData[] data;
    private final ColorData[] original;
    
    private final ChannelRange[] ranges;
    
    private final int modeQuantity;
    
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
        
        float hueStart = rnd.nextFloat(0f, 360f);
        
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
        
        int baseSize = n / modes;
        int remainder = n % modes;
        
        float chroma = rnd.nextFloat(ranges[1].getMin(), ranges[1].getMax());
        int index = 0;
        
        for (int m = 0; m < modes; m++) {
            int blockSize = baseSize + (m < remainder ? 1 : 0);
            
            for (int j = 0; j < blockSize; j++) {
                ColorData cd = data[index++];
                cd.setZ(hues[m]);
                cd.setY(chroma);
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
        
        float chroma = rnd.nextFloat(ranges[1].getMin(), ranges[1].getMax());
        
        float hueStart = rnd.nextFloat(0f, 360f);
        boolean cw = rnd.nextBoolean();
        
        float[] keyHues = new float[modes];
        float step = 360f / modes;
        
        for (int i = 0; i < modes; i++) {
            float raw = cw ? hueStart + i * step : hueStart - i * step;
            
            keyHues[i] = ((raw % 360f) + 360f) % 360f;
        }
        
        if (modes == 1) {
            for (int i = 0; i < n; i++) {
                data[i].setY(chroma);
                data[i].setZ(keyHues[0]);
            }
            
            return;
        }
        
        int[] keyPos = new int[modes];
        
        for (int i = 0; i < modes; i++) {
            float p = i * (n - 1f) / (modes - 1f);
            
            keyPos[i] = Math.round(p);
        }
        
        for (int i = 0; i < modes; i++) {
            int pos = keyPos[i];
            
            data[pos].setY(chroma);
            data[pos].setZ(keyHues[i]);
        }
        
        for (int seg = 0; seg < modes - 1; seg++) {
            int startIdx = keyPos[seg];
            int endIdx = keyPos[seg + 1];
            int span = endIdx - startIdx;

            ColorData labStart = new ColorData(
                    data[startIdx].getX(),
                    data[startIdx].getY(),
                    data[startIdx].getZ()
            ).oklchToOklab();
            
            ColorData labEnd = new ColorData(
                    data[endIdx].getX(),
                    data[endIdx].getY(),
                    data[endIdx].getZ()
            ).oklchToOklab();

            for (int j = 1; j < span; j++) {
                float t = (float) j / span;
                
                float L = labStart.getX() + t * (labEnd.getX() - labStart.getX());
                float a = labStart.getY() + t * (labEnd.getY() - labStart.getY());
                float b = labStart.getZ() + t * (labEnd.getZ() - labStart.getZ());
                
                ColorData interpolated = new ColorData(L, a, b)
                        .oklabToOklch();
                
                int idx = startIdx + j;
                
                data[idx].setX(interpolated.getX());
                data[idx].setY(interpolated.getY());
                data[idx].setZ(interpolated.getZ());
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
    
    public ColorData[] getData() {
        return data;
    }
    
    public ColorData[] getOriginal() {
        return original;
    }
}