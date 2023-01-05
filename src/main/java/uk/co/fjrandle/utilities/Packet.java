package uk.co.fjrandle.utilities;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public class Packet {
    public int packetLength = 638;
    public InetAddress address;
    public int port = 5568;
    byte[] sACNPacketData = new byte[packetLength];
    byte[] preambleSize = {(byte)0x00, (byte)0x10};
    byte[] postambleSize = {(byte)0x00, (byte)0x00};
    byte[] packetIdentifier = {(byte)0x41, (byte)0x53, (byte)0x43, (byte)0x2d, (byte)0x45, (byte)0x31, (byte)0x2e, (byte)0x31, (byte)0x37, (byte)0x00, (byte)0x00, (byte)0x00};
    byte[] flagsLengthOne = {(byte)0x72, (byte)0x6e};
    byte[] vectorOne = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x04};
    byte[] CID; // Unique ID 16 bytes use UUID generator
    byte[] flagsLengthTwo = {(byte)0x72, (byte)0x58};
    byte[] vectorTwo = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x02};
    byte[] sourceName = new byte[64];
    byte priority = (byte)0x64;
    byte[] syncAddress = {(byte)0x00, (byte)0x00};
    byte sequenceNumber = (byte)0x00; //Increase by one after each packet
    byte options = (byte)0x00;
    byte[] universe = {(byte)0x00, (byte)0x05};
    byte[] flagsLengthThree = {(byte)0x72, (byte)0x0b};
    byte vectorThree = (byte)0x02;
    byte addressType = (byte)0xa1;
    byte[] firstPropertyAddress = {(byte)0x00, (byte)0x00};
    byte[] addressIncrement = {(byte)0x00, (byte)0x01};
    byte[] propertyValueCount = {(byte)0x02, (byte)0x01};
    byte startCode = (byte)0x00;
    byte[] DMXValues = new byte[512];

    public Packet(String _name) throws UnsupportedEncodingException {
        Arrays.fill(this.sourceName, (byte)0x00);
        byte[] nameBytes = _name.getBytes("UTF-8");
        System.arraycopy(nameBytes, 0, this.sourceName, 0, nameBytes.length);
        try {
            this.address = InetAddress.getByName("192.168.0.7");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.CID = asBytes(UUID.randomUUID());
    }

    public static byte[] asBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    public void setAddress(String _address){
        try {
            this.address = InetAddress.getByName(_address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void incrementNumber(){
        this.sequenceNumber++;
    }

    public void setPacket(){
        incrementNumber();
        System.arraycopy(this.preambleSize, 0, sACNPacketData, 0, 2);
        System.arraycopy(this.postambleSize, 0, sACNPacketData, 2, 2);
        System.arraycopy(this.packetIdentifier, 0, sACNPacketData, 4, 12);
        System.arraycopy(this.flagsLengthOne, 0, sACNPacketData, 16, 2);
        System.arraycopy(this.vectorOne, 0, sACNPacketData, 18, 4);
        System.arraycopy(this.CID, 0, sACNPacketData, 22, 16);
        System.arraycopy(this.flagsLengthTwo, 0, sACNPacketData, 38, 2);
        System.arraycopy(this.vectorTwo, 0, sACNPacketData, 40, 4);
        System.arraycopy(this.sourceName, 0, sACNPacketData, 44, 64);
        sACNPacketData[108] = this.priority;
        System.arraycopy(this.syncAddress, 0, sACNPacketData, 109, 2);
        sACNPacketData[111] = this.sequenceNumber;
        sACNPacketData[112] = this.options;
        System.arraycopy(this.universe, 0, sACNPacketData, 113, 2);
        System.arraycopy(this.flagsLengthThree, 0, sACNPacketData, 115, 2);
        sACNPacketData[117] = this.vectorThree;
        sACNPacketData[118] = this.addressType;
        System.arraycopy(this.firstPropertyAddress, 0, sACNPacketData, 119, 2);
        System.arraycopy(this.addressIncrement, 0, sACNPacketData, 121, 2);
        System.arraycopy(this.propertyValueCount, 0, sACNPacketData, 123, 2);
        sACNPacketData[125] = this.startCode;
        System.arraycopy(this.DMXValues, 0, sACNPacketData, 126, 512);
    }

    public byte[] getPacket(){
        return sACNPacketData;
    }

    public void setUniverse(int _value){
        Arrays.fill(DMXValues, (byte) _value);
    }
}
