package Palette;

import Data.ColorData;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class PatternToImage {
    public BufferedImage applyPattern(BufferedImage image, ColorData[] originalPattern, ColorData[] newPattern) {
        BufferedImage out = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        
        ColorData[] og = new ColorData[originalPattern.length];
        int[][] nw = new int[originalPattern.length][];
        
        for (int i = 0; i < originalPattern.length; i++) {
            // Keep it normalized and use simple conversion
            // Simple clamping when outside gammut
            og[i] = originalPattern[i].oklchToOklab().oklabToRgb(false); // Non-linear
            
            // Conversion with gammut fallback
            nw[i] = newPattern[i].oklchToRgb().toRgb255();
        }
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color c = new Color(image.getRGB(x, y));
                ColorData cd = new ColorData(c.getRed(), c.getGreen(), c.getBlue());
                
                boolean matched = false;
                
                for (int i = 0; i < originalPattern.length; i++) {
                    if (compareColors(cd, og[i])) {
                        out.setRGB(x, y, new Color(nw[i][0], nw[i][1], nw[i][2]).getRGB());
                        
                        matched = true;
                        
                        break;
                    }
                }
                
                if (!matched) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "Pixel (%d,%d) with RGB=(%d,%d,%d) does not match any value in the default grayscale palette",
                                    x, y, c.getRed(), c.getGreen(), c.getBlue()
                            )
                    );
                }
            }
        }
        
        return out;
    }

    private boolean compareColors(ColorData imgLab, ColorData palLab) {
        float dL = Math.abs(imgLab.getX() - palLab.getX());
        float da = Math.abs(imgLab.getY() - palLab.getY());
        float db = Math.abs(imgLab.getZ() - palLab.getZ());
        
        final float THRESH = 0.01f;
        
        return dL <= THRESH && da <= THRESH && db <= THRESH;
    }
}