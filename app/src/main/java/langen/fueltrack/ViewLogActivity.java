package langen.fueltrack;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringBufferInputStream;

/*
 * Main ViewController in the project
 * Handles the viewing of the list of LogEntries, and user control of the
 * app to enter editing / adding of an entry.
 */
public class ViewLogActivity extends AppCompatActivity implements LogView {
    // Where to save the data
    public static String DATAFILE_NAME = "fuellog_data.txt";

    // Codes for Intent request/response communication with AddEditActivity
    public static int REQ_ADD = 10001;
    public static int REQ_EDIT = 10002;
    public static int RES_CANCEL = 10003;
    public static int RES_COMMIT = 10004;

    // extra parameter to AddEditActivity intents
    public static String ENTRY_TO_EDIT = "EntryToEdit";

    // The fuel log model we are working with
    private FuelLog log;

    // The arrayadapter used to display the entries
    private LogEntryArrayAdapter entryArrayAdapter;

    // Serialize a log entry
    byte[] serializeLogEntry(LogEntry entry) {
        try {
            return entry.serialize();
        } catch (IOException e) {
            Log.d("fuelapp", "Failed to serialize entry out");
            return null;
        }
    }

    // Launch the add / edit activity with a given request code and entry
    public void launchAddEditActivity(int requestCode, LogEntry entry) {
        Intent intent = new Intent(this, AddEditActivity.class);
        intent.putExtra(ENTRY_TO_EDIT, serializeLogEntry(entry));
        startActivityForResult(intent, requestCode);
    }

    // Initiate adding an entry
    public void addEntry() {
        Log.d("fuelapp", "Add entry");
        launchAddEditActivity(REQ_ADD, log.newLogEntry());
    }

    // Initiate editing an entry
    public void editEntry(LogEntry entry) {
        Log.d("fuelapp", "Rem entry: " + entry.getId());
        launchAddEditActivity(REQ_EDIT, entry);
    }

    // Get results back from the add / edit activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RES_COMMIT) {
            // Commit the new or changed entry to the log
            byte[] entrySerial = data.getExtras().getByteArray(ENTRY_TO_EDIT);
            try {
                // Get the resulting entry
                LogEntry entry = LogEntry.deserialize(entrySerial);

                // Handle it
                if (requestCode == REQ_ADD) {
                    log.addLogEntry(entry);
                    log.saveToFile(this);
                } else if (requestCode == REQ_EDIT) {
                    log.updateLogEntry(entry);
                    log.saveToFile(this);
                }

            } catch (IOException e) {
                Log.d("fuelapp", "Failed to deserialize result");
            } catch (ClassNotFoundException e) {
                Log.d("fuelapp", "Failed to deserialize (class)");
            }
            Log.d("fuelapp", "Commited Result");
        } else {
            Log.d("fuelapp", "Canceled add/edit");
        }
    }

    // When the data set changes, notify the adapter
    @Override
    public void logEntriesChanged() {
        // Update the adapter -> list of entries
        entryArrayAdapter.notifyDataSetChanged();

        // Update the total cost
        TextView totalCost = (TextView)findViewById(R.id.totalcost_result);
        totalCost.setText("Total Cost: $" + log.getTotalFuelCost());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_log);

        // Create a new log and load the current data into it
        log = new FuelLog();
        log.loadFromFile(this);

        // Add this view as a listener
        log.addView(this);

        // Create an adapter and attach it to the list
        entryArrayAdapter = new LogEntryArrayAdapter(this, log.getLogEntries());
        ListView entryList = (ListView)findViewById(R.id.logentry_list);
        entryList.setAdapter(entryArrayAdapter);

        // Handle editing
        entryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                editEntry(entryArrayAdapter.getItem(position));
            }
        });

        // Set up the button
        Button addButton = (Button)findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEntry();
            }
        });

        // Initial update of view
        entryArrayAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        // Save the current data to the log
        log.saveToFile(this);

        // Propagate up
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_log, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Handle add from menu. Alternative entry point
        if (id == R.id.action_add) {
            addEntry();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
