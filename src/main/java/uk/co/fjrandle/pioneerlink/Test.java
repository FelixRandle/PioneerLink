package uk.co.fjrandle.pioneerlink;

import java.util.Date;
import org.deepsymmetry.beatlink.*;

public class Test {

    public static void main(String[] args) {
        try {
            VirtualCdj.getInstance().start();
        } catch (java.net.SocketException e) {
            System.err.println("Unable to start VirtualCdj: " + e);
        }

        VirtualCdj.getInstance().addMasterListener(new MasterListener() {
            @Override
            public void masterChanged(DeviceUpdate update) {
                System.out.println("Master changed at " + new Date() + ": " + update);
            }

            @Override
            public void tempoChanged(double tempo) {
                System.out.println("Tempo changed at " + new Date() + ": " + tempo);
            }

            @Override
            public void newBeat(Beat beat) {
                System.out.println("Master player beat at " + new Date() + ": " + beat);
            }
        });

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            System.out.println("Interrupted, exiting.");
        }
    }
}