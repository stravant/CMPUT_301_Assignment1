package langen.fueltrack;

import android.content.Context;
import android.provider.MediaStore;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * FuelLog
 * Main model class holding the data for the app, which consists of a list
 * of LogEntry structures, as well as information to notify any associated
 * views when entries are added, removed, or changed.
 */
public class FuelLog {
    // Constructor
    public FuelLog() {
        // Nothing to do
    }

    // Add / remove / update an entry from the FuelLog
    // Notify views after the change is made
    public void addLogEntry(LogEntry entry) {
        data.add(entry);
        notifyViews();
    }
    public void removeLogEntry(LogEntry entry) {
        data.remove(entry);
        notifyViews();
    }
    public void updateLogEntry(LogEntry newEntry) {
        data.remove(getEntryById(newEntry.getId()));
        data.add(newEntry);
        notifyViews();
    }

    // Get a list of log entries for the usage of an ArrayAdapter
    public ArrayList<LogEntry> getLogEntries() {
        return data;
    }

    // Get an entry by id, used when updating entries
    private LogEntry getEntryById(int id) {
        for (LogEntry e: data) {
            if (e.getId() == id) {
                return e;
            }
        }
        return null;
    }

    // Return the total fuel cost of all entries as a 2 decimal place string
    public String getTotalFuelCost() {
        float total = 0;
        for (LogEntry e: data) {
            total += e.getFuelTotalCostNumeric();
        }
        return String.format("%.2f", total);
    }

    // Loading data in from a file on the given context
    // Note: Does not appear to work for some reason. The FileInputStream does not
    //       have any content in it even though it successfully opens and the data
    //       does exist on the device.
    public void loadFromFile(Context ctx) {
        try {
            // Open the file to load from
            FileInputStream input =
                    ctx.openFileInput(ViewLogActivity.DATAFILE_NAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            //Log.d("fuelapp", "A" + (reader.ready() ? "T" : "F") + (input.available()));

            // Use GSON to load the entries from it
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<LogEntry>>() {}.getType();

//            String strb = "[{\"date\":\"Jan 1, 2016 12:21:00 AM\",\"station\":\"er\",\"fuelGrade\":\"\",\"fuelUnitCost\":0.0,\"id\":0,\"odometerReading\":0.0,\"fuelAmount\":0.0}]";
//            Log.d("fuelapp", "Thing: " + gson.fromJson(strb, listType));
//            try{
//                Log.d("fuelapp", "Thing2: " + reader.readLine());
//
//            } catch (IOException e) {
//                Log.d("fuelapp", "wat");
//            }

            ArrayList<LogEntry> entries = gson.fromJson(reader, listType);


            // Clear the old data and add the new data if we got it successfully
            if (entries == null) {
                Log.d("fuelapp", "Failed to load entries");
            } else {
                data.clear();
                data.addAll(entries);
            }

            // Update next id to be used when a new entry is created
            // (one greater than the greatest one)
            for (LogEntry e: data) {
                if (e.getId() > nextEntryId) {
                    nextEntryId = e.getId() + 1;
                }
            }

            // Notify views
            notifyViews();
        } catch (FileNotFoundException ex) {
            // Nothing to do. Recreate the file when saving next time
            // Will happen normally on the first run of the app
            Log.d("fueltrack", "Failed to open data");
        } catch (JsonSyntaxException ex) {
            Log.d("fueltrack", "Failed to parse data");
        }
    }

    // Save data out to a file on the given context
    public void saveToFile(Context ctx) {
        try {
            // Open the file to save to
            FileOutputStream output =
                    ctx.openFileOutput(ViewLogActivity.DATAFILE_NAME, Context.MODE_PRIVATE);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));

            // Use GSON to save the entries to it
            Gson gson = new Gson();
            //Log.d("fuelapp", gson.toJson(data));
            writer.write(gson.toJson(data));

            // Make sure the data is saved out
            output.flush();
            output.close();
            Log.d("fuelapp", "Saved successfully");
        } catch (IOException ex) {
            // Should never happen unless android file system is screwed up
            throw new Error("Android file system is borked");
        }
    }

    // View handling. Add/Remove, and ability to notify
    public void addView(LogView view) {
        views.add(view);
    }
    public void removeView(LogView view) {
        views.remove(view);
    }
    private void notifyViews() {
        for (LogView w: views) {
            w.logEntriesChanged();
        }
    }

    // Generate a new log entry
    public LogEntry newLogEntry() {
        return new LogEntry(nextEntryId++);
    }

    // Tracks what the next free unique ID for a log entry is
    private int nextEntryId = 0;

    // The list of log entries that we have
    private ArrayList<LogEntry> data = new ArrayList<>();

    // The views to notify on changes
    private ArrayList<LogView> views = new ArrayList<>();
}