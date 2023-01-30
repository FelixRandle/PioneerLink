package uk.co.fjrandle.pioneerlink;

import javax.swing.*;
import java.awt.*;

public class TrackPanel extends Panel {
    GridBagConstraints constraints;

    JTextField trackNameInput = new JTextField("");

    String trackName = trackNameInput.getText();
    String artistName;
    Timecode offset;

    public TrackPanel() {
        this("", "", new Timecode(0));
    }

    public TrackPanel(String trackName, String artistName, Timecode offset) {
        this.artistName = artistName;
        this.offset = offset;

        this.setLayout(new GridLayout(1, 0));

        this.constraints = new GridBagConstraints();

        this.add(trackNameInput);

        this.add(new JLabel(this.offset.toString()));

    }

    public String getTrack() {
        return this.trackName;
    }
}
