package Windows.Components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import net.miginfocom.swing.MigLayout;

public class HexInputPanel extends JPanel {
    public static final Color BACKGROUND_COLOR = Color.BLACK;
    public static final Color BUTTON_BG_COLOR = Color.BLACK;
    public static final Color BORDER_AND_TEXT = Color.WHITE;
    public static final int PANEL_INSET = 10;
    public static final int BUTTON_HEIGHT = 30;
    public static final int BUTTON_PADDING_X = 10;
    
    public static final String PROP_HEX = "hexValue";
    
    private final JTextField hexField;
    private final JButton copyButton;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    /**
     * Constructs a new HexInputPanel initialized with the given hex string.
     *
     * @param initialHex the initial hexadecimal string to display in the text
     * field
     */
    public HexInputPanel(String initialHex) {
        setLayout(new MigLayout("insets " + PANEL_INSET + ", gap 10", "[grow][pref]"));
        setBackground(BACKGROUND_COLOR);

        hexField = new JTextField(initialHex);
        hexField.putClientProperty(PROP_HEX, initialHex);
        addTextFieldListeners();

        Action copyAllAction = new AbstractAction("Copy All") {
            @Override
            public void actionPerformed(ActionEvent e) {
                pcs.firePropertyChange("copyAll", null, null);
            }
        };

        copyButton = createButton(copyAllAction);
        add(copyButton, "cell 1 0");
    }
    
    private void addTextFieldListeners() {
        ActionListener commit = e -> {
            String oldHex = (String) hexField.getClientProperty(PROP_HEX);
            String newHex = hexField.getText().trim();
            
            if (!newHex.equals(oldHex)) {
                hexField.putClientProperty(PROP_HEX, newHex);
                pcs.firePropertyChange(PROP_HEX, oldHex, newHex);
            }
        };
        
        hexField.addActionListener(commit);
        hexField.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { commit.actionPerformed(null); }
        });
        
        add(hexField, "growx, pushx");
    }
    
    private JButton createButton(Action action) {
        JButton button = new JButton(action);
        button.setBackground(BUTTON_BG_COLOR);
        button.setForeground(BORDER_AND_TEXT);
        button.setBorder(BorderFactory.createLineBorder(BORDER_AND_TEXT));
        button.setFocusPainted(false);
        
        Dimension pref = button.getPreferredSize();
        button.setPreferredSize(new Dimension(
            pref.width + BUTTON_PADDING_X,
            BUTTON_HEIGHT
        ));
        
        return button;
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    @Override
    public void addPropertyChangeListener(String prop, PropertyChangeListener l) {
        pcs.addPropertyChangeListener(prop, l);
    }
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    @Override
    public void removePropertyChangeListener(String prop, PropertyChangeListener l) {
        pcs.removePropertyChangeListener(prop, l);
    }
    
    /**
     * Updates the displayed hex string in the text field without firing a
     * change event.
     *
     * @param hex the new hexadecimal string to display
     */
    public void setHex(String hex) {
        hexField.setText(hex);
        hexField.putClientProperty(PROP_HEX, hex);
    }
    
    /**
     * Returns the “Copy All” button so that external code (e.g.
     * PaletteChangerWindow) can attach its own listeners.
     *
     * @return the JButton configured to fire the copy-all event
     */
    public JButton getCopyButton() {
        return copyButton;
    }
}