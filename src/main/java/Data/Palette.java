package Data;

import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;

public class Palette {
    private final ColorData[] data;
    private final ColorData[] original;
    
    private final ChannelRange[] ranges;
    
    private final int modeQuantity;
    
    /**
     * Constructs a new Palette with the given initial colors, channel ranges,
     * and maximum hue mode quantity.
     *
     * @param initial An array of ColorData representing the starting palette
     * colors.
     * @param ranges An array of three ChannelRange objects defining valid
     * intervals for L, C, and H channels.
     * @param modeQuantity The maximum number of hue segments (1 to
     * modeQuantity) for gradient generation.
     * @throws IllegalArgumentException if initial is empty, ranges length is
     * not 3 or modeQuantity is lower than 1.
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
     * Generates a new palette gradient by:
     * 1) Resetting to the original colors,
     * 2) Randomly choosing 1–modeQuantity hue segments (up to palette size),
     * 3) Picking a random start hue and direction (clockwise/CCW),
     * 4) Dividing the palette into segments and interpolating hue within each,
     * 5) Setting chroma to a single random value across all colors.
     */
    public void generateComplex() {
        resetAll();
        
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        int n = this.data.length;
        
        int maxModes = Math.min(modeQuantity, n);
        int modes = rnd.nextInt(1, maxModes + 1);
        
        float hueStart = rnd.nextFloat(0f, 360f);
        
        boolean clockwise = rnd.nextBoolean();
        
        int colorsPerSegment = n / modes;
        float[][] hueRanges = new float[modes][2];
        
        for (int m = 0; m < modes; m++) {
            float start = (hueStart + m * (360f / modes));
            float end = (hueStart + (m + 1) * (360f / modes));
            
            hueRanges[m][0] = (start % 360f + 360f) % 360f;
            hueRanges[m][1] = (end % 360f + 360f) % 360f;
        }
        
        float chroma = rnd.nextFloat(ranges[1].getMin(), ranges[1].getMax());
        
        for (int i = 0; i < n; i++) {
            ColorData cd = this.data[i];
            cd.setY(chroma);
            
            int segment = Math.min(i / colorsPerSegment, modes - 1);
            float h0 = hueRanges[segment][0];
            float h1 = hueRanges[segment][1];
            
            float span;
            
            if (clockwise) {
                span = (h1 >= h0) ? (h1 - h0) : (h1 + 360f - h0);
            } else { // Counter Clock Wise
                span = (h0 >= h1) ? (h0 - h1) : (h0 + 360f - h1);
            }
            
            int idxInSegment = i - segment * colorsPerSegment;
            int countInSegment = (segment == modes - 1)
                    ? (n - segment * colorsPerSegment)
                    : colorsPerSegment;

            float hue;
            
            if (countInSegment <= 1) {
                hue = h0;
            } else {
                float stepHue = span / (countInSegment - 1);
                
                if (clockwise) {
                    hue = (h0 + stepHue * idxInSegment) % 360f;
                } else {
                    hue = (h0 - stepHue * idxInSegment) % 360f;
                    
                    if (hue < 0) {
                        hue += 360f;
                    }
                }
            }

            cd.setZ(hue);
        }
    }
    
    /**
     * Generates a new palette gradient by:
     * 1) Resetting to the original colors,
     * 2) Randomly choosing 1–modeQuantity hue segments (up to palette size),
     * 3) Picking a random start hue,
     * 4) Dividing the palette into segments,
     * 5) Setting chroma to a single random value across all colors.
     */
    public void generate() {
        resetAll();
        
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        int n = data.length;
        
        int maxModes = n;//Math.min(modeQuantity, n);
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
     * Randomizes every channel (X, Y, Z) of the index color in the palette
     * independently.Each channel value is chosen uniformly within its 
     * configured ChannelRange.
     * 
     * @param index Index for the color to be randomized.
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
     * Randomizes every channel (X, Y, Z) of each color in the palette
     * independently. Each channel value is chosen uniformly within its
     * configured ChannelRange.
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
     * Inverts the hue of every color in the palette by 180 degrees, wrapping
     * around the 0–360° color circle.
     */
    public void invert() {
        for (ColorData color : data) {
            float hue = color.getZ();
            color.setZ((hue + 180f) % 360f);
        }
    }
    
    /**
     * Resets the color back to its original provided at construction.
     * 
     * @param index Index for the color to be reset to original.
     */
    public void reset(int index) {
        data[index].setX(original[index].getX());
        data[index].setY(original[index].getY());
        data[index].setZ(original[index].getZ());
    }
    
    /**
     * Resets the palette back to its original colors provided at construction.
     */
    public void resetAll() {
        for (int i = 0; i < data.length; i++) {
            data[i].setX(original[i].getX());
            data[i].setY(original[i].getY());
            data[i].setZ(original[i].getZ());
        }
    }
    
    /**
     * Converts the current palette colors to an array of AWT Color objects.
     * Internally converts from OKLCh → OKLab → sRGB → 8-bit RGB.
     *
     * @return An array of java.awt.Color matching the palette.
     */
    public Color[] toAwtColors() {
        Color[] cols = new Color[data.length];
        
        for (int i = 0; i < data.length; i++) {
            int[] rgb = data[i].oklchToOklab().oklabToRgb().toRgb255();
            cols[i] = new Color(rgb[0], rgb[1], rgb[2]);
        }

        return cols;
    }
    
    public ColorData[] getData() {
        return data;
    }
    
    public ColorData[] getOriginal() {
        return original;
    }
}