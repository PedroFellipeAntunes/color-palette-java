package Windows;

import Data.ChannelRange;
import Data.ColorData;
import Data.Palette;

import Palette.PatternToImage;

import Windows.Components.ButtonPanel;
import Windows.Components.SliderPanel;
import Windows.Components.TopButtonPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;

import net.miginfocom.swing.MigLayout;

public class PaletteChangerWindow extends JDialog {
    private static final Color BG_COLOR = Color.BLACK;
    private static final Color FG_COLOR = Color.WHITE;
    
    private ColorData clipboardColor = null;
    
    private final ChannelRange[] ranges = new ChannelRange[3];
    private final Palette palette;
    
    private final BufferedImage image;
    private final String filePath;
    
    private TopButtonPanel topPanel;
    private ButtonPanel buttonPanel;
    private SliderPanel sliderPanel;
    
    private int currentIndex = 0;

    public PaletteChangerWindow(ColorData[] initialData, BufferedImage image, String filePath) {
        super((Frame) null, "Choose palette colors", true);
        this.image = image;
        this.filePath = filePath;

        ranges[0] = new ChannelRange(0.0f, 1.0f, 0.01f);
        ranges[1] = new ChannelRange(0.0f, 0.37f, 0.01f);
        ranges[2] = new ChannelRange(0.0f, 360.0f, 1.0f);

        this.palette = new Palette(initialData, ranges, 4);
        
        initFrame();
        initComponents();
        initLayout();
        initListeners();
        loadColorIntoControls(0);
        setupKeyBindings();
        setResizable(false);
        setVisible(true);
    }

    private void initFrame() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(750, 500);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new MigLayout("insets 0, gap 0, fill", "[grow]", "[grow 0][grow 1]"));
    }
    
    private void initComponents() {
        buttonPanel = new ButtonPanel(palette.toAwtColors());
        ColorData first = palette.getData()[0];
        
        float[] init = { first.getX(), first.getY(), first.getZ() };
        
        sliderPanel = new SliderPanel(init, ranges);
        sliderPanel.setLabels("L", "C", "H");
    }
    
    private void initLayout() {
        String[] btnNames = {"Next", "Generate", "Generate Lerp",
                "Random", "Random All", "Invert",
                "Reset", "Reset All", "Return"};
        
        topPanel = new TopButtonPanel(
                btnNames[0], btnNames[1], btnNames[2],
                btnNames[3], btnNames[4], btnNames[5],
                btnNames[6], btnNames[7], btnNames[8]
        );

        topPanel.addButtonListener(btnNames[0], e -> onNext());
        topPanel.addButtonListener(btnNames[1], e -> onGenerate());
        topPanel.addButtonListener(btnNames[2], e -> onGenerateInterpolated());
        topPanel.addButtonListener(btnNames[3], e -> onRandom());
        topPanel.addButtonListener(btnNames[4], e -> onFullRandom());
        topPanel.addButtonListener(btnNames[5], e -> onInvert());
        topPanel.addButtonListener(btnNames[6], e -> onReset());
        topPanel.addButtonListener(btnNames[7], e -> onResetAll());
        topPanel.addButtonListener(btnNames[8], e -> onReturn());

        add(topPanel, "cell 0 0, growx");

        JPanel control = new JPanel(new MigLayout("insets 0, gap 0, fill", "[grow 3][grow 1]", "[grow]"));
        control.add(buttonPanel, "cell 0 0, grow");
        control.add(sliderPanel, "cell 1 0, grow");
        add(control, "cell 0 1, grow");
    }
    
    private void initListeners() {
        buttonPanel.addPropertyChangeListener("selectedIndex", evt -> {
            currentIndex = (Integer) evt.getNewValue();
            
            loadColorIntoControls(currentIndex);
        });
        
        PropertyChangeListener sliderListener = evt -> {
            ColorData cd = palette.getData()[currentIndex];
            
            cd.setX(sliderPanel.getPanelX());
            cd.setY(sliderPanel.getPanelY());
            cd.setZ(sliderPanel.getPanelZ());
            
            updateButtonColor(currentIndex);
        };
        
        sliderPanel.addPropertyChangeListener(SliderPanel.PROP_X, sliderListener);
        sliderPanel.addPropertyChangeListener(SliderPanel.PROP_Y, sliderListener);
        sliderPanel.addPropertyChangeListener(SliderPanel.PROP_Z, sliderListener);
    }
    
    private void onNext() {
        long startTime = System.currentTimeMillis();
        System.out.println("Applying Palette to Image");
        
        PatternToImage pti = new PatternToImage();
        BufferedImage output = pti.applyPattern(image, palette.getOriginal(), palette.getData());
        
        long endTime = System.currentTimeMillis();
        System.out.println("TIME: " + (endTime - startTime) + "ms");
        
        ImageViewer iv = new ImageViewer(output, filePath);
    }
    
    private void onGenerate() {
        palette.generate();
        update();
    }
    
    private void onGenerateInterpolated() {
        palette.generateInterpolated();
        update();
    }
    
    private void onRandom() {
        palette.randomSingle(currentIndex);
        update();
    }
    
    private void onFullRandom() {
        palette.randomAll();
        update();
    }
    
    private void onReset() {
        palette.reset(currentIndex);
        updateButtonColor(currentIndex);
        loadColorIntoControls(currentIndex);
    }
    
    private void onResetAll() {
        palette.resetAll();
        update();
    }

    private void onInvert() {
        palette.invert();
        update();
    }
    
    private void onReturn() {
        dispose();
    }
    
    private void update() {
        buttonPanel.updateColors(palette.toAwtColors());
        loadColorIntoControls(currentIndex);
    }
    
    private void loadColorIntoControls(int idx) {
        ColorData cd = palette.getData()[idx];
        
        sliderPanel.setX(cd.getX());
        sliderPanel.setY(cd.getY());
        sliderPanel.setZ(cd.getZ());
    }

    private void updateButtonColor(int idx) {
        buttonPanel.updateColor(palette.toAwtColor(idx), idx);
    }
    
    private void setupKeyBindings() {
        JRootPane root = this.getRootPane();
        InputMap  im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        
        // Ctrl+C binding
        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK);
        im.put(copy, "copyColor");
        am.put("copyColor", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clipboardColor = new ColorData(
                        palette.getData()[currentIndex].getX(),
                        palette.getData()[currentIndex].getY(),
                        palette.getData()[currentIndex].getZ()
                );
            }
        });
        
        // Ctrl+V binding
        KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK);
        im.put(paste, "pasteColor");
        am.put("pasteColor", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (clipboardColor != null) {
                    ColorData target = palette.getData()[currentIndex];
                    
                    target.setX(clipboardColor.getX());
                    target.setY(clipboardColor.getY());
                    target.setZ(clipboardColor.getZ());
                    
                    update();
                }
            }
        });
        
        // Reset color binding
        KeyStroke reset = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        im.put(reset, "resetColor");
        am.put("resetColor", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onReset();
            }
        });
        
        // Random Single color binding
        KeyStroke random = KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK);
        im.put(random, "randomColor");
        am.put("randomColor", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onRandom();
            }
        });
        
        // ←
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "prevColor");
        am.put("prevColor", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonPanel.moveLeft();
            }
        });

        // →
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "nextColor");
        am.put("nextColor", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonPanel.moveRight();
            }
        });

        // ↑
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "upColor");
        am.put("upColor", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonPanel.moveUp();
            }
        });

        // ↓
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "downColor");
        am.put("downColor", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonPanel.moveDown();
            }
        });
    }
}