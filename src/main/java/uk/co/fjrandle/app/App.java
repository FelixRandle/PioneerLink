package uk.co.fjrandle.app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

public class App {
    public static void main(String[] args) throws Exception {
//        PioneerLink link = new PioneerLink();
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
            midi.openDevice((String) jComboBox.getSelectedItem());
        });

        constraints.gridx = 1;
        pane.add(b);//adding button in JFrame

        frame.pack();
        frame.setVisible(true);
    }
}


