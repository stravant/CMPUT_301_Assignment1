package langen.fueltrack;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Adapter for a list of log entries
 */
public class LogEntryArrayAdapter extends ArrayAdapter<LogEntry> {
    public LogEntryArrayAdapter(Context context, ArrayList<LogEntry> objects) {
        super(context, 0, objects);
    }

    public View getView(int position, View v, ViewGroup parent) {
        // Generate entry view if necessary
        if (v == null) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.logentry, parent, false);
//            Button btn = (Button)v.findViewById(R.id.entry_button);
//            btn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    view.
//                }
//            });
        }

        // Fill in entry
        LogEntry i = getItem(position);
        if (i != null) {
            TextView tt = (TextView) v.findViewById(R.id.entry_text);
            tt.setText("Arrived at " + i.getStation() + " on " + i.getDate() + " with reading " +
                            i.getOdometerReading() + "km\n" +
                            "Bought " + i.getFuelAmount() + "L of fuel at " + i.getFuelUnitCost() +
                            "c/L for a total cost of:\n" +
                            "$" + i.getFuelTotalCost()
            );
        }

        return v;
    }
}