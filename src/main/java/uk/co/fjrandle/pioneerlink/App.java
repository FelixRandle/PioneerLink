package uk.co.fjrandle.pioneerlink;

import com.alee.laf.WebLookAndFeel;
import com.alee.skin.dark.WebDarkSkin;
import org.deepsymmetry.beatlink.DeviceUpdateListener;
import org.deepsymmetry.beatlink.data.MetadataFinder;
import org.deepsymmetry.beatlink.data.TimeFinder;
import org.deepsymmetry.beatlink.data.TrackMetadata;
import org.deepsymmetry.beatlink.data.TrackPositionUpdate;

import javax.sound.midi.MidiUnavailableException;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class App {
    private static String currentTrack = "";
    private static JPanel tracks;

    public static void main(String[] args) {
        PioneerLink link = new PioneerLink();
        MIDIOutput midi = new MIDIOutput();

        JFrame frame = new JFrame("MIDI thing");//creating instance of JFrame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//        try {
//            WebLookAndFeel.install ( WebDarkSkin.class );
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

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
        play.addActionListener(e -> link.playInternalTimer());

        JButton pause = new JButton("PAUSE");
        internalTimerConstraints.gridx = 1;
        internalTimerPanel.add(pause, internalTimerConstraints);
        pause.addActionListener(e -> link.pauseInternalTimer());

        JButton reset = new JButton("RESET");
        internalTimerConstraints.gridx = 2;
        internalTimerPanel.add(reset, internalTimerConstraints);
        reset.addActionListener(e -> link.resetInternalTimer());

        // LINK PANEL

        JPanel linkPanel = new JPanel();
        linkPanel.setLayout(new GridLayout(0, 1));
        JLabel linkLabel = new JLabel("LINK");
        linkPanel.add(linkLabel);

        JLabel currentTrackLabel = new JLabel();

        linkPanel.add(currentTrackLabel);

        JButton addTrack = new JButton("Add Track");

        linkPanel.add(addTrack);

        tracks = new JPanel();
        tracks.setLayout(new GridLayout(0, 1));

        reloadTrackList();

        linkPanel.add(new JScrollPane(tracks));

        // TABBING PANELS

        JTabbedPane tabs = new JTabbedPane();
        java.net.URL clockIconUrl = App.class.getResource("/icons/clock.png");
        if (clockIconUrl != null) {
            ImageIcon clockIcon = new ImageIcon(new ImageIcon(clockIconUrl).getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
            tabs.addTab("Internal Timer", clockIcon, internalTimerPanel);
        } else {
            tabs.addTab("Internal Timer", internalTimerPanel);
        }

        java.net.URL cdjIconUrl = App.class.getResource("/icons/cdj.png");
        if (cdjIconUrl != null) {
            ImageIcon cdjIcon = new ImageIcon(new ImageIcon(cdjIconUrl).getImage().getScaledInstance(40, 30, Image.SCALE_SMOOTH));
            tabs.addTab("Link", cdjIcon, linkPanel);
        } else {
            tabs.addTab("Link", linkPanel);
        }

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.ipadx = 200;
        constraints.insets = new Insets(20, 0, 0, 0);

        pane.add(tabs, constraints);

        DeviceUpdateListener linkListener = deviceUpdate -> {
            TrackMetadata metadata = null;
            try {
                metadata = MetadataFinder.getInstance().getLatestMetadataFor(deviceUpdate);
            } catch(IllegalStateException e) {
                System.out.println(e);
            }
            if (metadata == null) {
                return;
            }
            //TODO:FIX HERE
            if (!metadata.getTitle().equals(App.currentTrack)) {
                App.currentTrack = metadata.getTitle();
                System.out.println("Song changed to: " + App.currentTrack);
                currentTrackLabel.setText(App.currentTrack);
            }
            TrackPositionUpdate time = TimeFinder.getInstance().getLatestPositionFor(deviceUpdate);

            System.out.println(time.milliseconds);

            Timecode t = new Timecode(time.milliseconds);

            if (TrackStorage.asMap().containsKey(App.currentTrack)) {
                t.add(TrackStorage.asMap().get(App.currentTrack));
            }

            // currently have to force full frame as partial frame isn't working?
            //midi.sendTimecode(t, true); //TODO: Force update when DJ is doing weird shit
        };

        DebugListener timerListener = millis -> {
            Timecode t = new Timecode(millis);

            localTime.setText(t.toString());
            midi.sendTimecode(t, false);
        };

        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1) {
                // Switch from internal timer to Pioneer Link
                link.removeDebugListener();
                link.addTrackListener(linkListener);
            } else {
                // Switch from Pioneer Link to Internal Timer
                link.removeTrackListener(linkListener);
                link.addDebugListener(timerListener);
            }
        });

        createMenuBar(frame);

        frame.pack();
        frame.setVisible(true);

        // Link listeners.

       link.addDebugListener(timerListener);
    }

    static void reloadTrackList() {
        tracks.removeAll();
        for (Map.Entry<String, Timecode> track:
                TrackStorage.asMap().entrySet()) {
            tracks.add(new TrackPanel(track.getKey(), "", track.getValue()));
        }
    }

    static void createMenuBar(JFrame frame) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        JMenuBar menuBar = new JMenuBar();

        JMenu trackListMenu = new JMenu("Track List");

        trackListMenu.setMnemonic(KeyEvent.VK_T);

        JMenuItem trackListSave = new JMenuItem("Save TrackList", KeyEvent.VK_S);
        trackListSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));

        trackListMenu.add(trackListSave);

        trackListSave.addActionListener(actionEvent -> {
            int result = fileChooser.showOpenDialog(frame);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    TrackStorage.saveToFile(selectedFile.getAbsolutePath());
                } catch (TrackStorage.SaveTrackFileException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        JMenuItem trackListLoad = new JMenuItem("Load TrackList", KeyEvent.VK_L);
        trackListLoad.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));

        trackListMenu.add(trackListLoad);

        trackListLoad.addActionListener(actionEvent -> {
            int result = fileChooser.showOpenDialog(frame);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    TrackStorage.loadFromFile(selectedFile.getAbsolutePath());
                    reloadTrackList();
                } catch (TrackStorage.LoadTrackFileException e) {
                    throw new RuntimeException(e);
                }
            }
        });



        menuBar.add(trackListMenu);

        frame.setJMenuBar(menuBar);
    }
}

