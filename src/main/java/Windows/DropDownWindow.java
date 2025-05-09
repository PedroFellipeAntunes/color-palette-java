package Windows;

import Palette.Operations;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class DropDownWindow {
    private Operations op = new Operations();
    
    private JFrame frame;
    private JLabel dropLabel;
    private JSlider slider;
    private JTextField valueField;
    private JButton dynamicButton;
    
    boolean loading = false;
    boolean rangeQ = false;
    private int colorLevels = 8;
    private final int minLevels = 2, maxLevels = 256;
    
    private Font defaultFont = UIManager.getDefaults().getFont("Label.font");
    
    public DropDownWindow() {
        frame = new JFrame("Image Color Palette");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setLayout(new BorderLayout());
        
        dropLabel = new JLabel("Drop IMAGE files here", SwingConstants.CENTER);
        dropLabel.setPreferredSize(new Dimension(300, 200));
        dropLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        dropLabel.setForeground(Color.WHITE);
        dropLabel.setOpaque(true);
        dropLabel.setBackground(Color.BLACK);
        dropLabel.setTransferHandler(new TransferHandler() {
            public boolean canImport(TransferHandler.TransferSupport support) {
                if (!loading && support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    return true;
                }
                
                return false;
            }
            
            public boolean importData(TransferHandler.TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                
                try {
                    Transferable transferable = support.getTransferable();
                    List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    
                    for (File file : files) {
                        if (!file.getName().toLowerCase().endsWith(".png")
                                && !file.getName().toLowerCase().endsWith(".jpg")
                                && !file.getName().toLowerCase().endsWith(".jpeg")) {
                            JOptionPane.showMessageDialog(frame, "Incorrect image format, use: png, jpg or jpeg", "Error", JOptionPane.ERROR_MESSAGE);
                            
                            return false;
                        }
                    }
                    
                    dropLabel.setText("LOADING (1/" + files.size() + ")");
                    loading = true;
                    slider.setEnabled(false);
                    valueField.setEnabled(false);
                    ableOrDisableButton(dynamicButton);
                    
                    frame.repaint();
                    
                    new Thread(() -> {
                        int filesProcessed = 1;
                        for (File file : files) {
                            op.processFile(file.getPath(), colorLevels, rangeQ);
                            filesProcessed++;
                            
                            final int finalFilesProcessed = filesProcessed;
                            SwingUtilities.invokeLater(() -> {
                                dropLabel.setText("LOADING (" + finalFilesProcessed + "/" + files.size() + ")");
                            });
                        }
                        
                        SwingUtilities.invokeLater(() -> {
                            dropLabel.setText("Images Colors Changed");
                            
                            Timer resetTimer = new Timer(1000, e2 -> {
                                dropLabel.setText("Drop IMAGE files here");
                                loading = false;
                                slider.setEnabled(true);
                                valueField.setEnabled(true);
                                ableOrDisableButton(dynamicButton);
                            });
                            
                            resetTimer.setRepeats(false);
                            resetTimer.start();
                        });
                    }).start();
                    
                    return true;
                } catch (UnsupportedFlavorException | IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        });
        
        //Slider
        slider = new JSlider(JSlider.HORIZONTAL, minLevels, maxLevels, colorLevels);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(maxLevels / 10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBackground(Color.BLACK);
        slider.setForeground(Color.WHITE);
        slider.setValue(colorLevels);
        
        //Value of slider
        valueField = new JTextField();
        valueField.setForeground(Color.WHITE);
        valueField.setBackground(Color.BLACK);
        valueField.setFont(defaultFont);
        valueField.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        valueField.setText(String.valueOf(slider.getValue()));
        valueField.setPreferredSize(new Dimension(50, 20));
        
        valueField.addActionListener(e -> {
            if (!loading) {
                String text = valueField.getText();
                
                if (!text.isEmpty()) {
                    text = text.substring(0, Math.min(text.length(), 3));
                    
                    int value = Integer.parseInt(text);
                    
                    value = Math.max(minLevels, Math.min(maxLevels, value));
                    
                    slider.setValue(value);
                    valueField.setText(String.valueOf(value));
                } else {
                    valueField.setText(String.valueOf(slider.getValue()));
                }
                
                valueField.transferFocus();
            }
        });
        
        slider.addChangeListener(e -> {
            if (!loading) {
                colorLevels = slider.getValue();
                valueField.setText(String.valueOf(slider.getValue()));
            }
        });
        
        //Panel for colorLevel and slider
        JPanel colorLevelPanel = new JPanel(new BorderLayout());
        colorLevelPanel.add(slider, BorderLayout.WEST);
        colorLevelPanel.add(valueField, BorderLayout.EAST);
        
        // Color Level
        JLabel colorLevelLabel = new JLabel("Color Levels:");
        colorLevelLabel.setHorizontalAlignment(SwingConstants.LEFT);
        colorLevelLabel.setForeground(Color.WHITE);
        colorLevelLabel.setBackground(Color.BLACK);
        colorLevelLabel.setOpaque(true);

        JPanel colorLevelSingle = new JPanel(new BorderLayout());
        colorLevelSingle.setBackground(Color.BLACK);
        colorLevelSingle.add(colorLevelLabel, BorderLayout.NORTH);
        colorLevelSingle.add(slider, BorderLayout.CENTER);

        colorLevelPanel.add(colorLevelSingle, BorderLayout.WEST);
        
        // Dynamic button
        dynamicButton = new JButton("Dynamic Range");
        setButtonsVisuals(dynamicButton);
        dynamicButton.addActionListener(e -> {
            if (!loading) {
                rangeQ = !rangeQ;
                if (rangeQ) {
                    dynamicButton.setBackground(Color.WHITE);
                    dynamicButton.setForeground(Color.BLACK);
                } else {
                    resetButton(dynamicButton);
                }
            }
        });
        
        //Bottom panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        controlPanel.setBackground(Color.BLACK);
        controlPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        controlPanel.add(colorLevelPanel);
        controlPanel.add(dynamicButton);
        
        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.add(dropLabel, BorderLayout.CENTER);
        
        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int xPos = (screenSize.width - frame.getWidth()) / 2;
        int yPos = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(xPos, yPos);
        
        frame.setVisible(true);
    }
    
    private void setButtonsVisuals(JButton button) {
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(130, 40));
    }
    
    void resetButton(JButton button) {
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
    }
    
    private void ableOrDisableButton(JButton button) {
        button.setEnabled(!loading);
    }
}