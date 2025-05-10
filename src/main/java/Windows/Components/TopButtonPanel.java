package Windows.Components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;

import net.miginfocom.swing.MigLayout;

public class TopButtonPanel extends JPanel {
    public Color bg_color = Color.BLACK;
    
    private final Map<String, JButton> buttons = new LinkedHashMap<>();

    public TopButtonPanel(String... buttonLabels) {
        super(new MigLayout("insets 10, gapx 10, align center, fill"));
        setBackground(bg_color);

        for (String label : buttonLabels) {
            JButton btn = createButton(label);
            buttons.put(label, btn);
            add(btn);
        }
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        btn.setForeground(Color.WHITE);
        btn.setBackground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 10, 30));
        
        return btn;
    }

    public void addButtonListener(String label, ActionListener listener) {
        JButton btn = buttons.get(label);
        
        if (btn != null) {
            btn.addActionListener(listener);
        }
    }

    public JButton getButton(String label) {
        return buttons.get(label);
    }
}