package langen.fueltrack;

import java.util.ArrayList;

/**
 * View for changes to a FuelLog
 * logEntriesChanged is called when an entry is added, removed, or updated
 */
public interface LogView {
    public void logEntriesChanged();
}
