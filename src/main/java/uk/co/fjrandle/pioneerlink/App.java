package uk.co.fjrandle.pioneerlink;

import com.alee.laf.WebLookAndFeel;
import org.deepsymmetry.beatlink.data.MetadataFinder;
import org.deepsymmetry.beatlink.data.TimeFinder;
import org.deepsymmetry.beatlink.data.TrackMetadata;
import org.deepsymmetry.beatlink.data.TrackPositionUpdate;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.HashMap;

public class App {
    private static boolean debug = false;
    private static String currentTrack = "";

    private static HashMap<String, Timecode> trackList = new HashMap<String, Timecode>() {
        {
            put("Tokyo", new Timecode(3600000));
            put("Turn On The Lights again.. (feat. Future)", new Timecode(7200000));
        }
    };

    public static void main(String[] args) {
        PioneerLink link = new PioneerLink();
        MIDIOutput midi = new MIDIOutput();

        System.out.println(midi.getAvailableDevices());
        JFrame frame = new JFrame("MIDI thing");//creating instance of JFrame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            UIManager.setLookAndFeel(new WebLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] components = new String[]{"Label.font", "Button.font", "ComboBox.font", "TabbedPane"};

        for (String component :
                components) {
            UIManager.put(component, new FontUIResource(new Font("Dialog", Font.PLAIN, 15)));
        }

        Container pane = frame.getContentPane();
        pane.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        JComboBox<Object> deviceSelector = new JComboBox<>(midi.getAvailableDevices().toArray());
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(20, 20, 0, 20);
        pane.add(deviceSelector, constraints);


        JButton deviceOpener = new JButton("Open Device");
        deviceOpener.addActionListener(e -> {
            try {
                midi.openDevice((String) deviceSelector.getSelectedItem());
            } catch (MidiUnavailableException ex) {
                throw new RuntimeException(ex);
            }
        });

        constraints.gridx = 1;
        constraints.gridy = 0;

        pane.add(deviceOpener, constraints);//adding button in JFrame

        // INTERNAL TIMER PANEL

        JPanel internalTimerPanel = new JPanel();

        internalTimerPanel.setLayout(new GridBagLayout());
        GridBagConstraints internalTimerConstraints = new GridBagConstraints();

        internalTimerConstraints.gridx = 0;
        internalTimerConstraints.gridy = 0;
        internalTimerConstraints.gridwidth = 3;
        internalTimerConstraints.insets = new Insets(40, 0, 0, 30);

        JLabel localTime = new JLabel("00:00:00:00");
        internalTimerPanel.add(localTime, internalTimerConstraints);

        localTime.setFont(new Font("Dialog", Font.PLAIN, 40));

        internalTimerConstraints.gridwidth = 1;
        internalTimerConstraints.gridy = 1;

        JButton play = new JButton("PLAY");
        internalTimerPanel.add(play, internalTimerConstraints);

        JButton pause = new JButton("PAUSE");
        internalTimerConstraints.gridx = 1;
        internalTimerPanel.add(pause, internalTimerConstraints);

        JButton reset = new JButton("RESET");
        internalTimerConstraints.gridx = 2;
        internalTimerPanel.add(reset, internalTimerConstraints);

        // LINK PANEL

        JPanel linkPanel = new JPanel();
        JLabel linkLabel = new JLabel("LINK");
        linkPanel.add(linkLabel);

        JLabel currentTrackLabel = new JLabel();

        linkPanel.add(currentTrackLabel);

        // TABBING PANELS

        JTabbedPane tabs = new JTabbedPane();
        java.net.URL clockIconUrl = App.class.getResource("/icons/clock.png");
        ImageIcon clockIcon = new ImageIcon(new ImageIcon(clockIconUrl).getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
        tabs.addTab("Internal Timer", clockIcon, internalTimerPanel);

        java.net.URL cdjIconUrl = App.class.getResource("/icons/cdj.png");
        ImageIcon cdjIcon = new ImageIcon(new ImageIcon(cdjIconUrl).getImage().getScaledInstance(40, 30, Image.SCALE_SMOOTH));
        tabs.addTab("Link", cdjIcon, linkPanel);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.ipadx = 200;
        constraints.insets = new Insets(20, 0, 0, 0);

        pane.add(tabs, constraints);

        tabs.addChangeListener(e -> {
            System.out.println(e.toString());
            System.out.println(tabs.getSelectedIndex());
        });


        frame.pack();
        frame.setVisible(true);

        // Link listeners.

        if (!debug) {
            link.addTrackListener(deviceUpdate -> {


                TrackMetadata metadata = MetadataFinder.getInstance().getLatestMetadataFor(deviceUpdate);
                if (!metadata.getTitle().equals(App.currentTrack)) {
                    App.currentTrack = metadata.getTitle();
                    System.out.println("Song changed to: " + App.currentTrack);
                    currentTrackLabel.setText(App.currentTrack);
                    metadata.getArtworkId();
                }
                TrackPositionUpdate time = TimeFinder.getInstance().getLatestPositionFor(deviceUpdate);

                Timecode t = new Timecode(time.milliseconds);

                if (App.trackList.containsKey(App.currentTrack)) {
                    t.add(App.trackList.get(App.currentTrack));
                }

                midi.sendTimecode(t, true); //TODO: Force update when DJ is doing weird shit
            });
        } else {
            System.out.println("adding debug");
            link.addDebugListener(millis -> {
                Timecode t = new Timecode(millis);

                midi.sendTimecode(t, false);
            });
        }
    }
}

