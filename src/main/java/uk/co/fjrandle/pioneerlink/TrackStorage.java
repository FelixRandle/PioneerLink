package uk.co.fjrandle.pioneerlink;

import java.io.*;
import java.util.HashMap;

public class TrackStorage {
    public static HashMap<String, Timecode> trackList = new HashMap<>() {
        {
            put("Tokyo", new Timecode(3600000));
            put("Freaks (Radio Et)", new Timecode(3600000 * 3));
            put("Hello World", new Timecode(3600000 * 5));
        }
    };

    static void saveToFile(String fileName) throws IOException {
        try {
            FileOutputStream fileOutStream
                    = new FileOutputStream(fileName);
            ObjectOutputStream trackStream
                    = new ObjectOutputStream(fileOutStream);

            trackStream.writeObject(trackList);

            trackStream.close();
            fileOutStream.close();
        } catch (IOException e) {
            throw e; //TODO: Custom Exception
        }
    }

    static void loadFromFile(String fileName) throws IOException, ClassNotFoundException {
        HashMap<String, Timecode> tracks = null;

        try{
            FileInputStream fileInputStream
                    = new FileInputStream(fileName);
            ObjectInputStream trackInputStream
                    = new ObjectInputStream(fileInputStream);

            try {
                tracks = (HashMap<String, Timecode>) trackInputStream.readObject();
            } catch (ClassNotFoundException e) {
                throw e; //TODO: Custom Exception
            }

            trackInputStream.close();
            fileInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            throw e; //TODO: Custom Exception
        }

        trackList = tracks;
    }
}
