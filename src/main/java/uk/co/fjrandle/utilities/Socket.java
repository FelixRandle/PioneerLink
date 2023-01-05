package uk.co.fjrandle.utilities;

import java.io.IOException;
import java.net.*;

public class Socket {

    DatagramSocket socket;

    public Socket() throws SocketException {
        this.socket = new DatagramSocket(7000);
    }

    public void sendPacket(DatagramPacket _packet) throws IOException {
        this.socket.send(_packet);
    }
}
