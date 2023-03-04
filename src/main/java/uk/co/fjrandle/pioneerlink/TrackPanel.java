package uk.co.fjrandle.pioneerlink;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class TrackPanel extends Panel {
    private GridBagConstraints constraints;

    private JTextField trackNameInput = new JTextField("");

    private String trackName;
    private Timecode offset;
    private TimecodeInput timecodeInput;

    public TrackPanel() {
        this("", new Timecode(0));
    }

    public TrackPanel(String trackName, Timecode offset) {
        this.trackName = trackName;
        this.offset = offset;

        System.out.println(this.offset);

        this.setLayout(new GridLayout(1, 0));

        this.constraints = new GridBagConstraints();
        trackNameInput.setText(trackName);
        this.add(trackNameInput);

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

        this.timecodeInput = new TimecodeInput(offset);

        this.add(timecodeInput);

        this.timecodeInput.addInputListener(this::setOffset);

    }

    public String getTrackName() {
        return this.trackName;
    }

    public void setTrackName(String trackName) { this.trackName = trackName; }

    public Timecode getOffset() { return this.offset; }

    public void setOffset(Timecode offset) {
        System.out.println(offset) ;this.offset = offset; }
}

class TimecodeInput extends JPanel {
    private TimecodeInputListener mListener = null;

    public void addInputListener(TimecodeInputListener listener) {
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


        ChangeListener listener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                emitTimecodeChange(new Timecode((int) hourInput.getValue(), (int) minuteInput.getValue(), (int) secondInput.getValue()));
            }
        };

        hourInput.addChangeListener(listener);
        minuteInput.addChangeListener(listener);
        secondInput.addChangeListener(listener);
    }

    private void emitTimecodeChange(Timecode t) {
        this.mListener.onTimecodeChange(t);
    }

    public interface TimecodeInputListener {
        public void onTimecodeChange(Timecode t);
    }
}
