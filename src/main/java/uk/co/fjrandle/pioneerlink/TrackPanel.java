package uk.co.fjrandle.pioneerlink;

import javax.swing.*;
import java.awt.*;

public class TrackPanel extends Panel {
    GridBagConstraints constraints;

    String trackName;
    String artistName;
    Timecode offset;

    public TrackPanel() {
        this("", "", new Timecode(0));
    }

    public TrackPanel(String trackName, String artistName, Timecode offset) {
        this.trackName = trackName;
        this.artistName = artistName;
        this.offset = offset;

        this.setLayout(new GridLayout(1, 0));

        this.constraints = new GridBagConstraints();

        this.add(new JTextField(this.trackName));

        this.add(new JLabel(this.offset.toString()));

    }
}
