package Windows.Components;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import javax.swing.border.StrokeBorder;

import net.miginfocom.swing.MigLayout;

public class ButtonPanel extends JPanel {
    public Color bg_color = Color.BLACK;
    
    private final List<JButton> buttons = new ArrayList<>();
    
    private int selectedIndex = 0;
    private final int count;
    private final int cols;
    private final int rows;
    
    private final Color[] initialColors;
    
    private final int borderThickness = 3;
    private final float[] dashPattern = new float[] {5f, 5f};
    private final Border defaultBorder = BorderFactory.createEmptyBorder();
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    public ButtonPanel(Color[] bgColors) {
        this.count = bgColors.length;
        this.initialColors = bgColors.clone();
        
        setBackground(bg_color);
        
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        this.cols = (int) Math.ceil(Math.sqrt(count));
        this.rows = (int) Math.ceil((double) count / cols);
        
        setLayout(new MigLayout("wrap " + cols + ", insets 0, gap 0"));
        
        for (int i = 0; i < count; i++) {
            JButton btn = createButton(i);
            buttons.add(btn);
            add(btn, "grow, push");
        }
    }
    
    private JButton createButton(int idx) {
        JButton btn = new JButton();
        btn.setFocusPainted(false);
        btn.setBackground(initialColors[idx]);
        
        if (idx == selectedIndex) {
            Color bg = btn.getBackground();
            Color inverse = new Color(255 - bg.getRed(), 255 - bg.getGreen(), 255 - bg.getBlue());
            btn.setBorder(BorderFactory.createLineBorder(inverse, borderThickness));
        } else {
            btn.setBorder(defaultBorder);
        }
        
        btn.setPreferredSize(new Dimension(10, 10));
        configureListeners(btn, idx);
        
        return btn;
    }
    
    private void configureListeners(JButton btn, int idx) {
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                Color bg = btn.getBackground();
                
                Color inverse = new Color(255 - bg.getRed(), 255 - bg.getGreen(), 255 - bg.getBlue());
                BasicStroke bs = new BasicStroke(borderThickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, dashPattern, 0f);
                Border dashedInverse = new StrokeBorder(bs, inverse);
                
                btn.setBorder(dashedInverse);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (idx == selectedIndex) {
                    Color bg = btn.getBackground();
                    Color inverse = new Color(255 - bg.getRed(), 255 - bg.getGreen(), 255 - bg.getBlue());
                    btn.setBorder(BorderFactory.createLineBorder(inverse, borderThickness));
                } else {
                    btn.setBorder(defaultBorder);
                }
            }
        });

        btn.addActionListener(e -> {
            if (idx == selectedIndex) return;
            
            int old = selectedIndex;
            
            if (old >= 0) {
                JButton prev = buttons.get(old);
                prev.setBackground(initialColors[old]);
                prev.setBorder(defaultBorder);
            }
            
            selectedIndex = idx;
            Color bg = btn.getBackground();
            Color inverse = new Color(255 - bg.getRed(), 255 - bg.getGreen(), 255 - bg.getBlue());
            btn.setBorder(BorderFactory.createLineBorder(inverse, borderThickness));
            
            pcs.firePropertyChange("selectedIndex", old, idx);
        });
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) { pcs.addPropertyChangeListener(l); }
    @Override
    public void addPropertyChangeListener(String prop, PropertyChangeListener l) { pcs.addPropertyChangeListener(prop, l); }
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) { pcs.removePropertyChangeListener(l); }
    @Override
    public void removePropertyChangeListener(String prop, PropertyChangeListener l) { pcs.removePropertyChangeListener(prop, l); }
    
    public void setButtonBackground(int index, Color color) {
        if (index >= 0 && index < count) {
            buttons.get(index).setBackground(color);
            buttons.get(index).repaint();
        }
    }
    
    public void setAllButtonsBackground(Color color) {
        buttons.forEach(b -> b.setBackground(color));
        repaint();
    }
    
    public void resetAllButtons() {
        selectedIndex = -1;
        
        for (int i = 0; i < count; i++) {
            JButton btn = buttons.get(i);
            btn.setBackground(initialColors[i]);
            btn.setBorder(defaultBorder);
        }
        
        repaint();
    }
    
    public int getButtonCount() { return count; }
    
    public void updateColor(Color newColor, int idx) {
        initialColors[idx] = newColor;
        buttons.get(idx).setBackground(newColor);
    }
    
    public void updateColors(Color[] newColors) {
        if (newColors == null || newColors.length != count)
            throw new IllegalArgumentException("newColors must have length " + count);
        
        System.arraycopy(newColors, 0, initialColors, 0, count);
        
        for (int i = 0; i < count; i++)
            buttons.get(i).setBackground(initialColors[i]);
        
        revalidate(); repaint();
    }
    
    private void moveSelection(int next) {
        if (next == selectedIndex) {
            return;
        }

        int old = selectedIndex;
        buttons.get(old).setBorder(defaultBorder);

        selectedIndex = next;
        
        Color bg = buttons.get(selectedIndex).getBackground();
        Color inverse = new Color(255 - bg.getRed(), 255 - bg.getGreen(), 255 - bg.getBlue());
        buttons.get(selectedIndex).setBorder(BorderFactory.createLineBorder(inverse, borderThickness));
        
        pcs.firePropertyChange("selectedIndex", old, selectedIndex);
    }

    public void moveLeft() {
        int col = selectedIndex % cols;
        
        if (col > 0) {
            moveSelection(selectedIndex - 1);
        }
    }

    public void moveRight() {
        int col = selectedIndex % cols;
        int row = selectedIndex / cols;
        
        int maxColInRow = Math.min(cols - 1, (count - 1) % cols == col && row == rows - 1
                ? (count - 1) % cols
                : cols - 1);
        
        if (col < maxColInRow) {
            moveSelection(selectedIndex + 1);
        }
    }

    public void moveUp() {
        int row = selectedIndex / cols;
        
        if (row > 0) {
            int target = selectedIndex - cols;
            
            if (target + cols >= count) {
                target = (row - 1) * cols + ((count - 1) % cols);
            }
            
            moveSelection(target);
        }
    }

    public void moveDown() {
        int row = selectedIndex / cols;
        
        if (row < rows - 1) {
            int target = selectedIndex + cols;
            
            if (target >= count) {
                return;
            }
            
            moveSelection(target);
        }
    }
}