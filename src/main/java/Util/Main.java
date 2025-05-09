package Util;

/*
Pedro Fellipe Cruz Antunes
Code that creates a window which receives an n amount of PNG files and changes
their color based on a user input palette. The output will be n PNG files.

User inputs:
    Drop files;
    Change level with slider;
    Choose which type of image editing;
    Save png file to the same folder as the original file;
*/

import Windows.DropDownWindow;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DropDownWindow dropDownWindow = new DropDownWindow();
        });
    }
}