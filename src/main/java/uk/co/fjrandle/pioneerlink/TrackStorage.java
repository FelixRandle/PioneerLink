package uk.co.fjrandle.pioneerlink;

import java.io.*;
import java.util.*;

public class TrackStorage {
    private static List<TrackPanel> trackPanels = new ArrayList<TrackPanel>();

    public static HashMap<String, Timecode> asMap() {
        HashMap<String, Timecode> trackList = new HashMap<>();

        for (TrackPanel panel:
             TrackStorage.trackPanels) {
            trackList.put(panel.getTrackName(), panel.getOffset());
        }
        return trackList;
    }

    static List<TrackPanel> getTrackPanels() { return trackPanels; }

    static void put(String trackName, Timecode offset) {
        trackPanels.add(new TrackPanel(trackName, offset));
    }

    static void remove(String trackName) {
        Optional<TrackPanel> trackPanel = trackPanels.stream().filter(panel -> panel.getTrackName().equals(trackName)).findFirst();
        trackPanel.ifPresent(panel -> trackPanels.remove(panel));
    }

    static void clear() {
        trackPanels.clear();
    }

    static Optional<TrackPanel> get(String trackName) {
        return trackPanels.stream().filter(panel -> panel.getTrackName().equals(trackName)).findFirst();
    }

    static void saveToFile(String fileName) throws SaveTrackFileException {
        try {
            FileOutputStream fileOutStream
                    = new FileOutputStream(fileName);
            ObjectOutputStream trackStream
                    = new ObjectOutputStream(fileOutStream);

            trackStream.writeObject(asMap());

            trackStream.close();
            fileOutStream.close();
        } catch (IOException e) {
            throw new SaveTrackFileException("Failed to save track file");
        }
    }

    @SuppressWarnings("unchecked")
    static void loadFromFile(String fileName) throws LoadTrackFileException {
        HashMap<String, Timecode> tracks;

        try{
            FileInputStream fileInputStream
                    = new FileInputStream(fileName);
            ObjectInputStream trackInputStream
                    = new ObjectInputStream(fileInputStream);


            tracks = (HashMap<String, Timecode>) trackInputStream.readObject();

            trackInputStream.close();
            fileInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new LoadTrackFileException(e.getMessage());
        }

        for (Map.Entry<String, Timecode> track : tracks.entrySet()) {
            put(track.getKey(), track.getValue());
        }
    }

    static class SaveTrackFileException extends Exception {
        SaveTrackFileException(String message) {
            super(message);
        }
    }

    static class LoadTrackFileException extends Exception {
        LoadTrackFileException(String message) {
            super(message);
        }
    }
}
