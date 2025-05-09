package Windows;

import Data.ChannelRange;
import Data.ColorData;
import Data.Palette;

import Palette.PatternToImage;

import Windows.Components.ButtonPanel;
import Windows.Components.SliderPanel;

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

    private ButtonPanel buttonPanel;
    private SliderPanel sliderPanel;
    private int currentIndex = 0;

    public PaletteChangerWindow(ColorData[] initialData, BufferedImage image, String filePath) {
        super((Frame) null, "Choose palette colors", true);
        this.image = image;
        this.filePath = filePath;

        ranges[0] = new ChannelRange(0.0f, 1.0f, 0.01f);
        ranges[1] = new ChannelRange(0.0f, 0.4f, 0.01f);
        ranges[2] = new ChannelRange(0.0f, 360.0f, 1.0f);

        this.palette = new Palette(initialData, ranges, 3);
        
        initFrame();
        initComponents();
        initLayout();
        initListeners();
        loadColorIntoControls(0);
        setupKeyBindings();
        setVisible(true);
    }

    private void initFrame() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(700, 500);
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
        JPanel top = createTopPanel();
        add(top, "cell 0 0, growx");

        JPanel control = new JPanel(new MigLayout("insets 0, gap 0, fill", "[grow 3][grow 1]", "[grow]"));
        control.add(buttonPanel, "cell 0 0, grow");
        control.add(sliderPanel, "cell 1 0, grow");
        add(control, "cell 0 1, grow");
    }
    
    private JPanel createTopPanel() {
        JPanel p = new JPanel(new MigLayout("insets 10, gapx 20, align center"));
        p.setBackground(BG_COLOR);
        
        JButton next = createButton("Next");
        JButton generate = createButton("Generate");
        JButton rand = createButton("Random");
        JButton trueRand = createButton("Random All");
        JButton invert = createButton("Invert");
        JButton reset = createButton("Reset");
        JButton resetAll = createButton("Reset All");
        JButton ret = createButton("Return");
        
        next.addActionListener(e -> onNext());
        generate.addActionListener(e -> onGenerate());
        rand.addActionListener(e -> onRandom());
        trueRand.addActionListener(e -> onFullRandom());
        invert.addActionListener(e -> onInvert());
        reset.addActionListener(e -> onReset());
        resetAll.addActionListener(e -> onResetAll());
        ret.addActionListener(e -> onReturn());
        
        p.add(next);
        p.add(generate);
        p.add(rand);
        p.add(trueRand);
        p.add(invert);
        p.add(reset);
        p.add(resetAll);
        p.add(ret);
        
        return p;
    }
    
    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        
        btn.setBorder(BorderFactory.createLineBorder(FG_COLOR));
        btn.setForeground(FG_COLOR);
        btn.setBackground(BG_COLOR);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 10, 30));
        
        return btn;
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
        PatternToImage pti = new PatternToImage();
        BufferedImage output = pti.applyPattern(image, palette.getOriginal(), palette.getData());
        
        ImageViewer iv = new ImageViewer(output, filePath);
    }
    
    private void onGenerate() {
        palette.generate();
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
        ColorData cd = palette.getData()[idx];
        
        int[] rgb = cd.oklchToOklab().oklabToRgb().toRgb255();
        buttonPanel.updateColor(new Color(rgb[0], rgb[1], rgb[2]), idx);
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