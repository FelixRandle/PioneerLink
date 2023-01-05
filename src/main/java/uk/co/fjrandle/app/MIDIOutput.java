package uk.co.fjrandle.app;

import ovh.stranck.javaTimecode.Timecode;
import ovh.stranck.javaTimecode.*;
import ovh.stranck.javaTimecode.mtc.LTCGenerator;

import javax.sound.midi.*;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class MIDIOutput {
    //ArrayList of MidiDevices
    private List<MidiDevice.Info> midiDevices = new ArrayList<>();
    private MidiDevice.Info selectedDevice;
    private MidiDevice device = null;

    MIDIOutput() throws LineUnavailableException {
        MidiDevice.Info[] allDevices = MidiSystem.getMidiDeviceInfo();

        for (MidiDevice.Info device:
             allDevices) {
            System.out.println(device.getName());
            System.out.println(device.getDescription());
            System.out.println("--------");
            if (device != null && device.getDescription().equals("External MIDI Port")) {
                midiDevices.add(device);
            }
        }

        selectedDevice = midiDevices.get(0);
//        for (MidiDevice.Info info:
//             midiDevices) {
//
//            if (info.getDescription().equals("External MIDI Port") && info.getName().equals("midiTHRU")) {
//                try {
//                    device = MidiSystem.getMidiDevice(info);
//                } catch (MidiUnavailableException e) {
//                    System.err.println("MIDI Device was unavailable: " + e);
//                }
//            }
//        }

        if (device != null) {
//            device.open();
//            Receiver receiver = device.getReceiver();
//
            Mixer m = Utils.getMixer("USB MIDI Interface");
            LTCGenerator ltc = new LTCGenerator(m, 48000);
            TimecodePlayer tp = ltc.getTimecodePlayer();

            ltc.start();

            tp.setSpeed(1);

            long start = System.currentTimeMillis();


            do {
                System.out.println("------");
                System.out.println(tp.getTimecode().toString());
                System.out.println("------");
            } while (System.currentTimeMillis() - start < 2000000);

//            tp.pause();
//            ltc.stop();

//            ShortMessage myMsg = new ShortMessage();
//            myMsg.setMessage(ShortMessage.NOTE_ON, 0, 60, 93);
//            long timeStamp = -1;

//            receiver.send(myMsg, timeStamp);

            device.close();
        } else {
            System.out.println("No device found");
        }
    }

    List<String> getAvailableDevices() {
        return midiDevices.stream().map(MidiDevice.Info::getName).collect(Collectors.toList());
    }

    void openDevice(String deviceName) {
        for (MidiDevice.Info device:
             this.midiDevices) {
            if (device.getName().equals(deviceName)) {
                // Close any previously open device
                if (this.device != null) this.device.close();
                this.selectedDevice = device;
                try {
                    this.device = MidiSystem.getMidiDevice(selectedDevice);
                } catch (MidiUnavailableException e) {
                    System.err.println("Selected MIDI Device was unavailable: " + e);
                }
                System.out.println(this.device.toString());

                return;
            }
        }
        System.err.println("Unable to open selected device");
    }
}

class MidiTimecode {
    static byte[] timecodeToFullFrame(Timecode t) {
        byte[] bytes = new byte[10];

        // STATIC BYTES
        bytes[0] = (byte) 0xF0; // UNIVERSAL SYSTEM
        bytes[1] = (byte) 0x7F; // EXCLUSIVE HEADER
        bytes[2] = (byte) 0x7F; // device ID (7F entire system)
        bytes[3] = (byte) 0x01; // System timecode
        bytes[4] = (byte) 0x01; // Full timecode message

        // DYNAMIC BYTES
        int mask = 0b0_11_11111; // Bit mask for 30 fps
        bytes[5] = (byte) (t.getHours() & mask); // HR
        bytes[6] = (byte) t.getMins(); // MIN
        bytes[7] = (byte) t.getSecs(); // SEC
        bytes[8] = (byte) t.getFrames(); // FRAME

        // STATIC BYTE
        bytes[9] = (byte) 0xF7; //EOX

        return bytes;
    }
}
