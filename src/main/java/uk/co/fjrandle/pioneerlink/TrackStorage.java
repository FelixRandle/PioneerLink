package uk.co.fjrandle.pioneerlink;

import java.io.*;
import java.util.HashMap;

public class TrackStorage {
    void writeTracks(String fileName, HashMap<String, Timecode> tracks) throws IOException {
        try {
            FileOutputStream fileOutStream
                    = new FileOutputStream(fileName);
            ObjectOutputStream trackStream
                    = new ObjectOutputStream(fileOutStream);

            trackStream.writeObject(tracks);

            trackStream.close();
            fileOutStream.close();
        } catch (IOException e) {
            throw e; //TODO: Custom Exception
        }
    }

    HashMap<String, Timecode> readTracks(String fileName) {
        HashMap<String, Timecode> tracks = null;

        try{
            FileInputStream fileInputStream = new FileInputStream(fileName);
            ObjectInputStream trackInputStream = new ObjectInputStream(fileInputStream);
            tracks = (HashMap<String, Timecode>) trackInputStream.readObject();
        }

        return tracks;

    }
}
