package Windows.Components;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Map;

import net.miginfocom.swing.MigLayout;

public class TopButtonPanel extends JPanel {
    public static final Color BACKGROUND_COLOR = Color.BLACK;
    public static final Color GROUP_BG_COLOR = Color.BLACK;
    public static final Color BUTTON_BG_COLOR = Color.BLACK;
    public static final Color BORDER_AND_TEXT = Color.WHITE;
    public static final int PANEL_INSET = 10;
    public static final int GROUP_INSET = 5;
    public static final int BUTTON_HEIGHT = 30;
    public static final int BUTTON_PADDING_X = 10;
    
    /**
     * Constructs a TopButtonPanel that organizes multiple action groups into
     * labeled, evenly spaced sub-panels. Each sub-panel is titled with its
     * group name and lays out its buttons horizontally, allowing them to grow
     * and share available space uniformly.
     *
     * @param actionGroups a map where each key is a group title and the value
     * is an array of Actions to create buttons for that group
     */
    public TopButtonPanel(Map<String, Action[]> actionGroups) {
        super(new MigLayout(
            "insets " + PANEL_INSET + ", gapx " + PANEL_INSET
            + ", align center center, fillx"
        ));
        
        setBackground(BACKGROUND_COLOR);
        
        for (Map.Entry<String, Action[]> entry : actionGroups.entrySet()) {
            String groupTitle = entry.getKey();
            Action[] actions  = entry.getValue();
            
            JPanel groupPanel = new JPanel(new MigLayout(
                "insets " + GROUP_INSET + ", gapx " + GROUP_INSET
                + ", align center center, fillx"
            ));
            
            groupPanel.setBackground(GROUP_BG_COLOR);
            
            groupPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_AND_TEXT),
                groupTitle,
                TitledBorder.LEADING,
                TitledBorder.TOP,
                groupPanel.getFont(),
                BORDER_AND_TEXT
            ));
            
            for (Action action : actions) {
                JButton button = createButton(action);
                groupPanel.add(button, "pushx, growx");
            }
            
            add(groupPanel, "pushx, growx");
        }
    }
    
    private JButton createButton(Action action) {
        JButton button = new JButton(action);
        button.setBackground(BUTTON_BG_COLOR);
        button.setForeground(BORDER_AND_TEXT);
        button.setBorder(BorderFactory.createLineBorder(BORDER_AND_TEXT));
        button.setFocusPainted(false);

        Dimension preferred = button.getPreferredSize();
        button.setPreferredSize(new Dimension(
            preferred.width + BUTTON_PADDING_X,
            BUTTON_HEIGHT
        ));
        
        return button;
    }
}