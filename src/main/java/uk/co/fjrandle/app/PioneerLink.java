package uk.co.fjrandle.app;

import org.deepsymmetry.beatlink.*;
import org.deepsymmetry.beatlink.data.*;

import java.util.Date;

class PioneerLink {
    PioneerLink() {
        try {
            VirtualCdj.getInstance().start();
        } catch (java.net.SocketException e) {
            System.err.println("Unable to start VirtualCdj: " + e);
        }

        try {
            TimeFinder.getInstance().start();
            MetadataFinder.getInstance().start();
            CrateDigger.getInstance().start();
        } catch (Exception e) {
            System.err.println("Unable to start finders:" + e);
        }

        VirtualCdj.getInstance().addMediaDetailsListener(new MediaDetailsListener() {
            @Override
            public void detailsAvailable(MediaDetails mediaDetails) {
                System.out.println(mediaDetails.toString());
            }
        });

        VirtualCdj.getInstance().addUpdateListener(new DeviceUpdateListener() {
            @Override
            public void received(DeviceUpdate deviceUpdate) {

                TrackMetadata metadata = MetadataFinder.getInstance().getLatestMetadataFor(deviceUpdate);
                System.out.println(metadata.getTitle());
                System.out.println("time:");
                TrackPositionUpdate time = TimeFinder.getInstance().getLatestPositionFor(deviceUpdate);
                System.out.println(time.milliseconds);
            }
        });

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

    }
}
