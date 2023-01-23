package uk.co.fjrandle.pioneerlink;

import java.io.*;
import java.util.HashMap;

@SuppressWarnings("unused")
public class TrackStorage {
    private static HashMap<String, Timecode> trackList = new HashMap<>();

    static HashMap<String, Timecode> asMap() {
        return trackList;
    }

    static void put(String trackName, Timecode timecode) {
        trackList.put(trackName, timecode);
    }

    static void remove(String trackName) {
        trackList.remove(trackName);
    }

    static void clear() {
        trackList.clear();
    }

    static void get(String trackName) {
        trackList.get(trackName);
    }

    static void saveToFile(String fileName) throws SaveTrackFileException {
        try {
            FileOutputStream fileOutStream
                    = new FileOutputStream(fileName);
            ObjectOutputStream trackStream
                    = new ObjectOutputStream(fileOutStream);

            trackStream.writeObject(trackList);

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

        trackList = tracks;
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
