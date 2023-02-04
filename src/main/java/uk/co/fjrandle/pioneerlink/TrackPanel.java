package uk.co.fjrandle.pioneerlink;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class TrackPanel extends Panel {
    private GridBagConstraints constraints;

    private JTextField trackNameInput = new JTextField("");

    private String trackName;
    private Timecode offset;

    public TrackPanel() {
        this("", new Timecode(0));
    }

    public TrackPanel(String trackName, Timecode offset) {
        this.trackName = trackName;
        this.offset = offset;

        this.setLayout(new GridLayout(1, 0));

        this.constraints = new GridBagConstraints();
        trackNameInput.setText(trackName);
        this.add(trackNameInput);

        this.add(new TimecodeInput(offset));

        this.trackNameInput.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent focusEvent) {

            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
                JTextField src = (JTextField)focusEvent.getSource();
                setTrackName(src.getText());
            }
        });

    }

    public String getTrackName() {
        return this.trackName;
    }

    public void setTrackName(String trackName) { this.trackName = trackName; }

    public Timecode getOffset() { return this.offset; }

    public void setOffset(Timecode offset) { this.offset = offset; }
}

class TimecodeInput extends JPanel {
    private TimecodeInputListener mListener = null;

    public void setMyClassListener(TimecodeInputListener listener) {
        this.mListener = listener;
    }
    public TimecodeInput(Timecode t) {
        setLayout(new GridLayout(1, 4));
        JSpinner hourInput = new JSpinner(new SpinnerNumberModel(t.getHours(), 0, 23, 1));

        add(hourInput);

        JSpinner minuteInput = new JSpinner(new SpinnerNumberModel(t.getMinutes(), 0, 59, 1));

        add(minuteInput);

        JSpinner secondInput = new JSpinner(new SpinnerNumberModel(t.getSeconds(), 0, 59, 1));

        add(secondInput);
    }

    public interface TimecodeInputListener {
        public void onTimecodeChange(Timecode t);
    }
}
