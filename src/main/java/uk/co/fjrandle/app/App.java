package uk.co.fjrandle.app;

import org.deepsymmetry.beatlink.DeviceUpdate;
import org.deepsymmetry.beatlink.DeviceUpdateListener;
import org.deepsymmetry.beatlink.data.MetadataFinder;
import org.deepsymmetry.beatlink.data.TimeFinder;
import org.deepsymmetry.beatlink.data.TrackMetadata;
import org.deepsymmetry.beatlink.data.TrackPositionUpdate;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

public class App {
    private static boolean debug = true;
    public static void main(String[] args) throws Exception {
        PioneerLink link = new PioneerLink(debug);
        MIDIOutput midi = new MIDIOutput();

        System.out.println(midi.getAvailableDevices());
        JFrame frame = new JFrame("MIDI thing");//creating instance of JFrame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container pane = frame.getContentPane();
        pane.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        JComboBox jComboBox = new JComboBox<>(midi.getAvailableDevices().toArray());
        constraints.gridx = 0;
        constraints.gridy = 0;
        pane.add(jComboBox, constraints);


        JButton b=new JButton("Open Device");//creating instance of JButton
        b.addActionListener(e -> {
            try {
                midi.openDevice((String) jComboBox.getSelectedItem());
            } catch (MidiUnavailableException ex) {
                throw new RuntimeException(ex);
            }
        });

        constraints.gridx = 1;
        pane.add(b);//adding button in JFrame

        frame.pack();
        frame.setVisible(true);

        if (!debug) {
            link.addTrackListener(new DeviceUpdateListener() {
                @Override
                public void received(DeviceUpdate deviceUpdate) {

                    TrackMetadata metadata = MetadataFinder.getInstance().getLatestMetadataFor(deviceUpdate);
//                System.out.println(metadata.getTitle());
//                System.out.println("time:");
                    TrackPositionUpdate time = TimeFinder.getInstance().getLatestPositionFor(deviceUpdate);
//                System.out.println(time.milliseconds);

                    Timecode t = new Timecode(time.milliseconds);


                    midi.sendTimecode(t);
                }
            });
        } else {
            System.out.println("adding debug");
            link.addDebugListener(new DebugListener() {
                @Override
                public void received(long millis) {
                    Timecode t = new Timecode(millis);

                    midi.sendTimecode(t);
                }
            });
        }


    }
}


