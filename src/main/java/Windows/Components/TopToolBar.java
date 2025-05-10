/*
======================== NOT USED==========================

package Windows.Components;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

public class TopToolBar extends JPanel {
    private final Map<String, JButton> buttons = new LinkedHashMap<>();

    // Define os grupos e seus botões
    private static final LinkedHashMap<String, List<String>> BUTTON_GROUPS = new LinkedHashMap<>() {{
        put("Navigation", List.of("Next", "Return"));
        put("Generation", List.of("Simple", "Interpolated"));
        put("Random", List.of("Single", "All"));
        put("Reset", List.of("Single", "All"));
        put("Extra", List.of("Invert"));
    }};

    public TopToolBar() {
        super(new MigLayout("insets 10, gapx 10, fillx", "[grow, push]"));
        setBackground(Color.BLACK);

        for (Map.Entry<String, List<String>> groupEntry : BUTTON_GROUPS.entrySet()) {
            String groupName = groupEntry.getKey();
            List<String> buttonNames = groupEntry.getValue();

            JPanel groupPanel = new JPanel(new MigLayout("insets 10, gapx 10, fillx", "[grow, push]"));
            groupPanel.setBackground(Color.BLACK);
            groupPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.GRAY),
                    groupName,
                    0, 0,
                    groupPanel.getFont().deriveFont(Font.BOLD),
                    Color.WHITE
            ));

            for (String label : buttonNames) {
                JButton btn = createButton(label);
                buttons.put(label, btn);
                groupPanel.add(btn, "grow, push");
            }
            
            add(groupPanel, "grow, push");
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
*/