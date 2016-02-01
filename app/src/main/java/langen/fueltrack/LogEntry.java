package langen.fueltrack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Log entry class
 * Handles storing the data for a single fuel tracking
 * logModel entry.
 */
public class LogEntry implements Serializable {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");

    // Data describing the entry
    private int id;
    private Date date = Calendar.getInstance().getTime();
    private String station = "";
    private float odometerReading = 0;
    private String fuelGrade = "";
    private float fuelAmount = 0;
    private float fuelUnitCost = 0;

    // Constructor
    public LogEntry(int id) {
        this.id = id;
    }

    // Getters
    public int getId() {
        return id;
    }
    public String getDate() {
        return sdf.format(date);
    }
    public String getStation() {
        return station;
    }
    public String getOdometerReading() {
        return String.format("%.1f", odometerReading);
    }
    public String getFuelGrade() {
        return fuelGrade;
    }
    public String getFuelAmount() {
        return String.format("%.3f", fuelAmount);
    }
    public String getFuelUnitCost() {
        return String.format("%.1f", fuelUnitCost);
    }
    public float getFuelTotalCostNumeric() {
        return fuelUnitCost * fuelAmount / 100;
    }
    public String getFuelTotalCost() {
        return String.format("%.2f", getFuelTotalCostNumeric());
    }

    // Setters
    public void setDate(String s) throws LogFormatException {
        try {
            date = sdf.parse(s);
        } catch (ParseException e) {
            throw new LogFormatException("Date must be formatted as: yyyy-mm-dd");
        }
    }
    public void setStation(String s) throws LogFormatException {
        if (s.equals("")) {
            throw new LogFormatException("Station must be set");
        } else {
            station = s;
        }
    }
    public void setOdometerReading(String s) throws LogFormatException {
        try {
            odometerReading = Float.parseFloat(s);
        } catch (NumberFormatException e) {
            throw new LogFormatException("Odometer reading must be a number");
        }
    }
    public void setFuelGrade(String s) throws LogFormatException {
        if (s.equals("")) {
            throw new LogFormatException("Fuel Grade must be set");
        } else {
            fuelGrade = s;
        }
    }
    public void setFuelAmount(String s) throws LogFormatException {
        try {
            fuelAmount = Float.parseFloat(s);
        } catch (NumberFormatException e) {
            throw new LogFormatException("Amount of fuel must be a number");
        }
    }
    public void setFuelUnitCost(String s) throws LogFormatException {
        try {
            fuelUnitCost = Float.parseFloat(s);
        } catch (NumberFormatException e) {
            throw new LogFormatException("Price of fuel must be a number");
        }
    }

    // Serialize or deserialize a LogEntry
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        ObjectOutputStream outStream = new ObjectOutputStream(outBytes);
        outStream.writeObject(this);
        return outBytes.toByteArray();
    }
    public static LogEntry deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ObjectInputStream inStream =
                new ObjectInputStream(new ByteArrayInputStream(data));
        return (LogEntry)inStream.readObject();
    }
}