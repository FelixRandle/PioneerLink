package uk.co.fjrandle.app;

import org.deepsymmetry.beatlink.*;
import org.deepsymmetry.beatlink.data.*;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class PioneerLink {
    private boolean debug;

    private Timer debugTimer;
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);
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

//            VirtualCdj.getInstance().addMediaDetailsListener(new MediaDetailsListener() {
//                @Override
//                public void detailsAvailable(MediaDetails mediaDetails) {
//                    System.out.println(mediaDetails.toString());
//                }
//            });


        VirtualCdj.getInstance().addMasterListener(new MasterListener() {
            @Override
            public void masterChanged(DeviceUpdate update) {
                System.out.println("Master changed at " + new Date() + ": " + update);
            }

            @Override
            public void tempoChanged(double tempo) {
//                System.out.println("Tempo changed at " + new Date() + ": " + tempo);
            }

            @Override
            public void newBeat(Beat beat) {
//                System.out.println("Master player beat at " + new Date() + ": " + beat);
            }
        });
    }

    public void addTrackListener(DeviceUpdateListener listener) {
        if(!this.debug) {
            VirtualCdj.getInstance().addUpdateListener(listener);
        } else {
            System.out.println("In debug mode, ignoring TrackListener");
        }
    }

    public void addDebugListener(DebugListener listener) {
        this.debugTimer.setCallback(listener);
    }
}

interface DebugListener {
    void received(long millis);
}

class Timer implements Runnable {
    private long timerStart = System.currentTimeMillis();
    private long timerPauseTime;
    private boolean playing;

    private DebugListener callback;

    Timer() {
        this.timerStart = System.currentTimeMillis();
        this.playing = false;
    }

    public void setCallback(DebugListener callback) {
        this.callback = callback;
    }

    public void play() {
        this.playing = true;
        this.timerStart = System.currentTimeMillis();
    }
    public void run() {
        try {
            if (this.callback != null) {
                this.callback.received(System.currentTimeMillis() - this.timerStart);
            }
        } catch ( Exception e) {
            System.err.println("TIMER Error: " + e);
            System.err.println(Arrays.toString(e.getStackTrace()));
        }
    }
}
