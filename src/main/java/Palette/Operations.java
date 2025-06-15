package Palette;

import Data.ColorData;
import Dither.RgbQuantization;

import FileManager.PngReader;
import FileManager.PngSaver;

import Windows.PaletteChangerWindow;
import java.awt.image.BufferedImage;

public class Operations {
    public static int colorLevels;
    
    public void processFile(String filePath, int colorLevels, boolean rangeQ) {
        Operations.colorLevels = colorLevels;
        
        PngReader pr = new PngReader();
        
        // Get 2D matrix of pixels
        BufferedImage image = pr.readPNG(filePath, true);
        
        long startTime = System.currentTimeMillis();
        
        // Quantize image
        System.out.println("Quantization Grayscale Image");
        RgbQuantization band = new RgbQuantization();
        band.applyQuantization(image, colorLevels, rangeQ);
        
        long endTime = System.currentTimeMillis();
        System.out.println("TIME: " + (endTime - startTime) + "ms");
        
        // Generating default grayscale palette
        System.out.println("Generating Default Grayscale Palette");
        ColorData[] palette = new ColorData[colorLevels];
        
        float delta = 255f / (colorLevels - 1);
        
        for (int i = 0; i < colorLevels; i++) {
            int c = Math.round(i * delta);
            palette[i] = new ColorData(c, c, c).rgbToOklab().oklabToOklch();
        }
        
        endTime = System.currentTimeMillis();
        System.out.println("TIME: " + (endTime - startTime) + "ms");
        
        // Move to palette window
        PaletteChangerWindow pcf = new PaletteChangerWindow(palette, image, filePath);
    }
    
    //Save files
    public static void saveImage(BufferedImage image, String filePath) {
        PngSaver listToImage = new PngSaver();
        
        listToImage.saveToFile("Palette[" + colorLevels + "color]", filePath, image);
    }
}