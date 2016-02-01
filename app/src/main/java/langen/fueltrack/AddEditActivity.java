package langen.fueltrack;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/*
 * AddEditActivity
 * The main activity used to add a new LogEntry or edit an existing LogEntry
 * Takes a serialized LogEntry from which to work, and returns a new updated
 * serialized log entry. The Id of the log entry is used to match up the
 * new & old LogEntries for editing.
 */
public class AddEditActivity extends AppCompatActivity {

    // The LogEntry which this activity is editing / adding
    private LogEntry entryToEdit;

    // Update the view with the data currently in the entry
    private void updateViewWithEntry() {
        // The fields to update
        EditText edit_date = (EditText)findViewById(R.id.date_input);
        EditText edit_station = (EditText)findViewById(R.id.station_input);
        EditText edit_odometer = (EditText)findViewById(R.id.odometer_input);
        EditText edit_grade = (EditText)findViewById(R.id.grade_input);
        EditText edit_rate = (EditText)findViewById(R.id.rate_input);
        EditText edit_amount = (EditText)findViewById(R.id.amount_input);
        TextView totalAmount = (TextView)findViewById(R.id.total_cost_output);

        // Update them
        edit_date.setText(entryToEdit.getDate());
        edit_station.setText(entryToEdit.getStation());
        edit_odometer.setText(entryToEdit.getOdometerReading());
        edit_grade.setText(entryToEdit.getFuelGrade());
        edit_rate.setText(entryToEdit.getFuelUnitCost());
        edit_amount.setText(entryToEdit.getFuelAmount());
        totalAmount.setText(entryToEdit.getFuelTotalCost());
    }

    // Attempt to update the entry with the data currently in the view
    private void updateEntryWithView() throws LogFormatException {
        // The fields to read from
        EditText edit_date = (EditText)findViewById(R.id.date_input);
        EditText edit_station = (EditText)findViewById(R.id.station_input);
        EditText edit_odometer = (EditText)findViewById(R.id.odometer_input);
        EditText edit_grade = (EditText)findViewById(R.id.grade_input);
        EditText edit_rate = (EditText)findViewById(R.id.rate_input);
        EditText edit_amount = (EditText)findViewById(R.id.amount_input);
        TextView totalAmount = (TextView)findViewById(R.id.total_cost_output);

        // Try to update entry
        try {
            // If setting any of the fields fails then an exception will be thrown
            // and we will proceed to the catch clause
            entryToEdit.setDate(edit_date.getText().toString());
            entryToEdit.setStation(edit_station.getText().toString());
            entryToEdit.setOdometerReading(edit_odometer.getText().toString());
            entryToEdit.setFuelGrade(edit_grade.getText().toString());
            entryToEdit.setFuelUnitCost(edit_rate.getText().toString());
            entryToEdit.setFuelAmount(edit_amount.getText().toString());
        } catch (LogFormatException e) {
            // Failed, let the user know how, and set the total amount to a placeholder
            // since it will not be reliable in this state.
            totalAmount.setText("??$");
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG);
            toast.show();
            throw e;
        }
    }

    // Commit the current changes if possible and return to the calling activity.
    private void done() {
        // Try to commit the results
        try {
            updateEntryWithView();
        } catch (LogFormatException e) {
            // Failed, can't be done yet
            return;
        }

        // Results commited, send updated entry back to view log activity
        try {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(ViewLogActivity.ENTRY_TO_EDIT, entryToEdit.serialize());
            setResult(ViewLogActivity.RES_COMMIT, resultIntent);
        } catch (IOException e) {
            Log.d("fuelapp", "Failed to serialize in AddEditActivity");
            setResult(ViewLogActivity.RES_CANCEL);
        }
        finish();
    }

    // Return to the calling activity without commiting the changes.
    private void cancel() {
        setResult(ViewLogActivity.RES_CANCEL);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        // Set up buttons
        Button done = (Button)findViewById(R.id.done_button);
        Button cancel = (Button)findViewById(R.id.cancel_button);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                done();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Handle the input from the calling activity
        Intent intent = getIntent();
        byte[] entrySerial = intent.getExtras().getByteArray(ViewLogActivity.ENTRY_TO_EDIT);
        try {
            // Deserialize the entry to edit
            ObjectInputStream inStream =
                    new ObjectInputStream(new ByteArrayInputStream(entrySerial));
            entryToEdit = (LogEntry)inStream.readObject();

            Log.d("fuelapp", "AddEdit got entry");
        } catch (IOException e) {
            Log.d("fuelapp", "Failed to deserialize result");
        } catch (ClassNotFoundException e) {
            Log.d("fuelapp", "Failed to deserialize (class)");
        }

        // Update the view with the entry data now that we have it
        updateViewWithEntry();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
