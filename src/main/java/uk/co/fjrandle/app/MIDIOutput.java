package uk.co.fjrandle.app;

import ovh.stranck.javaTimecode.*;
import ovh.stranck.javaTimecode.mtc.LTCGenerator;

import javax.sound.midi.*;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.sound.midi.ShortMessage.MIDI_TIME_CODE;

class MIDIOutput {
    //ArrayList of MidiDevices
    private List<MidiDevice.Info> midiDevices = new ArrayList<>();
    private MidiDevice.Info selectedDevice;
    private MidiDevice device = null;

    private Receiver receiver;

    private long lastFullFrameTime = System.currentTimeMillis();
    private long lastQuarterFrameTime = System.currentTimeMillis();
    private int quarterFramePieceNumber = 0;

    public static void main(String[] args) throws InvalidMidiDataException, LineUnavailableException, MidiUnavailableException {
        MidiDevice.Info[] midiDevices = MidiSystem.getMidiDeviceInfo();

        MidiDevice device = null;
        for (MidiDevice.Info info:
             midiDevices) {

            if (info.getDescription().equals("External MIDI Port") && info.getName().equals("USB MIDI Interface")) {
                try {
                    device = MidiSystem.getMidiDevice(info);
                } catch (MidiUnavailableException e) {
                    System.err.println("MIDI Device was unavailable: " + e);
                }
            }
        }

        if (device != null) {
            device.open();
            Receiver receiver = device.getReceiver();

            Mixer m = Utils.getMixer("Speaker/HP");
            LTCGenerator ltc = new LTCGenerator(m, 48000);
            TimecodePlayer tp = ltc.getTimecodePlayer();

            ltc.start();

            tp.setSpeed(1);

            long start = System.currentTimeMillis();


//            do {
//                System.out.println("------");
//                System.out.println(tp.getTimecode().toString());
//                SysexMessage msg = new SysexMessage();
//                msg.setMessage(MidiTimecode.timecodeToFullFrame(tp.getTimecode()), 10);
//                receiver.send(msg, -1);
//                System.out.println("------");
//            } while (System.currentTimeMillis() - start < 2000);

            tp.pause();
            ltc.stop();

            ShortMessage myMsg = new ShortMessage();
            myMsg.setMessage(ShortMessage.NOTE_ON, 0, 60, 93);
            long timeStamp = -1;

            receiver.send(myMsg, timeStamp);

            device.close();
        } else {
            System.out.println("No device found");
        }
    }

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
    }

    List<String> getAvailableDevices() {
        return midiDevices.stream().map(MidiDevice.Info::getName).collect(Collectors.toList());
    }

    void openDevice(String deviceName) throws MidiUnavailableException {
        for (MidiDevice.Info device:
             this.midiDevices) {
            if (device.getName().equals(deviceName)) {
                // Close any previously open device
                if (this.device != null) {
                    this.receiver.close();
                    this.device.close();
                }
                this.selectedDevice = device;
                try {
                    this.device = MidiSystem.getMidiDevice(selectedDevice);
                } catch (MidiUnavailableException e) {
                    System.err.println("Selected MIDI Device was unavailable: " + e);
                }
                System.out.println(this.device.toString());

                this.device.open();

                this.receiver = this.device.getReceiver();

                return;
            }
        }
        System.err.println("Unable to open selected device");
    }

    public void sendTimecode(Timecode t) {
        // Update full frame every second
        if (System.currentTimeMillis() - this.lastFullFrameTime > 5000) {
            System.out.println("Sent Full Frame");
            SysexMessage msg = new SysexMessage();
            try {
                msg.setMessage(MidiTimecode.timecodeToFullFrame(t), 10);
            } catch(InvalidMidiDataException e) {
                System.err.println(e);
            }
            this.receiver.send(msg, -1);
            this.lastFullFrameTime = System.currentTimeMillis();
        } else {
            // Send Quarter Frame
            if(System.currentTimeMillis() - this.lastQuarterFrameTime > (30 / 4)) {
                System.out.println("Sent Quarter Frame # " + this.quarterFramePieceNumber);

                byte messageData = MidiTimecode.timecodeToQuarterFrame(t, this.quarterFramePieceNumber);
                ShortMessage msg = new ShortMessage();
                try {
                    msg.setMessage(MIDI_TIME_CODE, messageData, 0);
                } catch (Exception e) {
                    System.err.println(e);
                    System.exit(1);
                }
                this.receiver.send(msg, -1);
                this.quarterFramePieceNumber ++;
                if (this.quarterFramePieceNumber > 7) {
                    this.quarterFramePieceNumber = 0;
                }
            }
        }
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
        int mask = 0b0_11_00000; // Bit mask for 30 fps
        bytes[5] = (byte) (t.getHours() | mask); // HR
        bytes[6] = (byte) t.getMinutes(); // MIN
        bytes[7] = (byte) t.getSeconds(); // SEC
        bytes[8] = (byte) t.getFrames(); // FRAME

        // STATIC BYTE
        bytes[9] = (byte) 0xF7; //EOX

        return bytes;
    }

    static byte timecodeToQuarterFrame(Timecode t, int pieceNumber) {
        // Piece number should be requested in order (0-7) unless a full frame has been sent.
        byte data;
        switch (pieceNumber) {
            // FRAME lsbits
            case 0:
                data = (byte) (((byte) t.getFrames() & 0x0F) | 0b0000_1111);
                break;
            // FRAME msbit
            case 1:
                data = (byte) ((t.getFrames() >> 4) | 0b0001_0001);
                break;
            // SECONDS lsbits
            case 2:
                data = (byte) ((t.getSeconds() & 0x0F) | 0b0010_1111);
                break;
            // SECONDS msbits
            case 3:
                data = (byte) ((t.getSeconds() >> 4) | 0b0011_0011);
                break;
            // MINUTES lsbits
            case 4:
                data = (byte) ((t.getMinutes() & 0x0F) | 0b0100_1111);
                break;
            // MINUTES msbits
            case 5:
                data = (byte) ((t.getMinutes() >> 4) | 0b0101_0011);
                break;
            // HOURS lsbits
            case 6:
                data = (byte) ((t.getHours() & 0x0F) | 0b0110_1111);
                break;
            // HOURS msbits & Framerate
            case 7:
                data = (byte) ((t.getHours() >> 4) | 0b0111_0111);
                break;
            default:
                data = 0;
        }
        return data;
    }
}
