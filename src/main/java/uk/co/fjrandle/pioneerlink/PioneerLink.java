package uk.co.fjrandle.pioneerlink;

import org.deepsymmetry.beatlink.*;
import org.deepsymmetry.beatlink.data.*;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class PioneerLink {
    private final Timer debugTimer;
    private Set<DeviceAnnouncement> connectedDevices;

    PioneerLink() {
        try {
            DeviceFinder.getInstance().start();

            VirtualCdj.getInstance().start();
        } catch (java.net.SocketException e) {
            System.err.println("Unable to start VirtualCdj or DeviceFinder: " + e);
        }
        if (VirtualCdj.getInstance().isRunning()) {
            try {
                CrateDigger.getInstance().start();
                TimeFinder.getInstance().start();
                MetadataFinder.getInstance().start();
                AnalysisTagFinder.getInstance().start();
            } catch (Exception e) {
                System.err.println("Unable to start finders:" + e);
            }

            VirtualCdj.getInstance().addMediaDetailsListener(new MediaDetailsListener() {
                @Override
                public void detailsAvailable(MediaDetails mediaDetails) {
                    System.out.println(mediaDetails.toString());
                }
            });

            this.connectedDevices = DeviceFinder.getInstance().getCurrentDevices();

            for (DeviceAnnouncement device :
                    this.connectedDevices) {
                System.out.println(device);
            }

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

        this.debugTimer = new Timer();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this.debugTimer, 1, 5, TimeUnit.MILLISECONDS);

//        TimeFinder.getInstance().addTrackPositionListener(1, new TrackPositionListener() {
//            @Override
//            public void movementChanged(TrackPositionUpdate update) {
//                if (update != null) {
//                    System.out.println("TF: " + update.milliseconds);
//                }
//            }
//        });
    }

    public void addTrackListener(DeviceUpdateListener listener) {
        VirtualCdj.getInstance().addUpdateListener(listener);
    }

    public void addDebugListener(DebugListener listener) {
        this.debugTimer.setCallback(listener);
    }

    public void removeTrackListener(DeviceUpdateListener listener) {
        VirtualCdj.getInstance().removeUpdateListener(listener);
    }

    public void removeDebugListener() {
        this.debugTimer.removeCallback();
    }

    public void playInternalTimer() {
        this.debugTimer.play();
    }

    public void pauseInternalTimer() {
        this.debugTimer.pause();
    }

    public void resetInternalTimer() {
        this.debugTimer.reset();
    }
}

interface DebugListener {
    void received(long millis);
}

class Timer implements Runnable {
    private long timerStart;
    private long elapsedTime = 0;
    private boolean paused = true;

    private DebugListener callback;

    Timer() {
        this.timerStart = System.currentTimeMillis();
    }

    public void setCallback(DebugListener callback) {
        this.callback = callback;
    }

    public void removeCallback() {
        this.callback = null;
    }

    public void play() {
        if (this.paused) {
            this.timerStart = System.currentTimeMillis();
        }
        this.paused = false;
    }

    public void pause() {
        if (!this.paused) {
            this.elapsedTime = System.currentTimeMillis() - this.timerStart + this.elapsedTime;
        }
        this.paused = true;
    }

    public void reset() {
        this.timerStart = System.currentTimeMillis();
        this.elapsedTime = 0;
    }

    public void run() {
        if (this.callback != null) {
            long returnTime = this.paused ? this.elapsedTime : System.currentTimeMillis() - this.timerStart + this.elapsedTime;
            this.callback.received(returnTime);
        }
    }
}
