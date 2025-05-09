package Windows.Components;

import Data.ChannelRange;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import net.miginfocom.swing.MigLayout;

public class SliderPanel extends JPanel {
    public static final String PROP_X = "x";
    public static final String PROP_Y = "y";
    public static final String PROP_Z = "z";

    private final ChannelPanel panelX;
    private final ChannelPanel panelY;
    private final ChannelPanel panelZ;

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public SliderPanel(float[] initValues, ChannelRange[] ranges) {
        super(new MigLayout("insets 10, fill", "[grow,fill]", ""));
        if (initValues.length != 3 || ranges.length != 3) {
            throw new IllegalArgumentException("Must be 3 values and 3 ranges");
        }

        panelX = new ChannelPanel("L", initValues[0], PROP_X, ranges[0]);
        panelY = new ChannelPanel("A", initValues[1], PROP_Y, ranges[1]);
        panelZ = new ChannelPanel("B", initValues[2], PROP_Z, ranges[2]);

        setBackground(Color.BLACK);
        add(panelX, "growx, wrap");
        add(panelY, "growx, wrap");
        add(panelZ, "growx");
    }

    public float getPanelX() { return panelX.getValue(); }
    public float getPanelY() { return panelY.getValue(); }
    public float getPanelZ() { return panelZ.getValue(); }

    public void setX(float v) { panelX.setValueSilently(v); }
    public void setY(float v) { panelY.setValueSilently(v); }
    public void setZ(float v) { panelZ.setValueSilently(v); }

    public void setLabels(String lx, String ly, String lz) {
        panelX.setLabel(lx);
        panelY.setLabel(ly);
        panelZ.setLabel(lz);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) { pcs.addPropertyChangeListener(l); }
    @Override
    public void addPropertyChangeListener(String prop, PropertyChangeListener l) { pcs.addPropertyChangeListener(prop, l); }
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) { pcs.removePropertyChangeListener(l); }
    @Override
    public void removePropertyChangeListener(String prop, PropertyChangeListener l) { pcs.removePropertyChangeListener(prop, l); }

    private final class ChannelPanel extends JPanel {
        private final JLabel label;
        private final JSlider slider;
        private final JSpinner spinner;
        private final String propertyName;
        private final ChannelRange range;
        private final int SLIDER_RES = 1_000_000;

        private float value;
        private boolean adjusting = false, silent = false;

        ChannelPanel(String lblText, float initial, String propName, ChannelRange range) {
            super(new MigLayout("insets 0, gap 5", "[][grow,fill][50!]"));
            this.propertyName = propName;
            this.range = range;
            this.value = clamp(initial);

            // Label
            label = new JLabel(lblText);
            label.setForeground(Color.WHITE);

            // Slider 0..SLIDER_RES
            slider = new JSlider(0, SLIDER_RES, realToSlider(value));
            slider.setBackground(Color.BLACK);
            
            // Spinner with min/max/step of range
            spinner = new JSpinner(new SpinnerNumberModel(value, range.getMin(), range.getMax(), range.getStep()));
            JSpinner.NumberEditor numEditor = new JSpinner.NumberEditor(spinner, "#0.00");
            
            spinner.setEditor(numEditor);
            JFormattedTextField tf = numEditor.getTextField();
            tf.setColumns(4);
            spinner.setPreferredSize(new Dimension(80, spinner.getPreferredSize().height));

            // Listeners
            slider.addChangeListener(e -> {
                if (adjusting || silent) return;
                
                adjusting = true;
                
                float old = value;
                
                value = sliderToReal(slider.getValue());
                spinner.setValue((double) value);
                pcs.firePropertyChange(propertyName, old, value);
                
                adjusting = false;
            });

            spinner.addChangeListener(e -> {
                if (adjusting || silent) return;
                
                adjusting = true;
                
                float old = value;
                
                value = clamp(((Number) spinner.getValue()).floatValue());
                slider.setValue(realToSlider(value));
                pcs.firePropertyChange(propertyName, old, value);
                
                adjusting = false;
            });
            
            setBackground(Color.BLACK);
            add(label);
            add(slider, "growx");
            add(spinner);
        }

        int realToSlider(float v) {
            float norm = (clamp(v) - range.getMin()) / (range.getMax() - range.getMin());
            
            return Math.round(norm * SLIDER_RES);
        }

        float sliderToReal(int s) {
            float norm = s / (float) SLIDER_RES;
            
            return range.getMin() + norm * (range.getMax() - range.getMin());
        }

        float clamp(float v) {
            return Math.max(range.getMin(), Math.min(range.getMax(), v));
        }

        float getValue() { return value; }

        void setValueSilently(float v) {
            silent = true;
            value = clamp(v);
            slider.setValue(realToSlider(value));
            spinner.setValue((double) value);
            silent = false;
        }

        void setLabel(String txt) { label.setText(txt); }
    }
}