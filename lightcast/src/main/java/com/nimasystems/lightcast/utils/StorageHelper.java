package com.nimasystems.lightcast.utils;

import android.os.Environment;

public class StorageHelper {
    // Storage states
    private boolean externalStorageAvailable, externalStorageWriteable;

    /**
     * Checks the external storage's state and saves it in member attributes.
     */
    private void checkStorage() {
        // Get the external storage's state
        String state = Environment.getExternalStorageState();
        //noinspection IfCanBeSwitch
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            // Storage is available and writeable
            externalStorageAvailable = externalStorageWriteable = true;
        } else if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            // Storage is only readable
            externalStorageAvailable = true;
            externalStorageWriteable = false;
        } else {
            // Storage is neither readable nor writeable
            externalStorageAvailable = externalStorageWriteable = false;
        }
    }

    /**
     * Checks the state of the external storage.
     *
     * @return True if the external storage is available, false otherwise.
     */
    public boolean isExternalStorageAvailable() {
        checkStorage();
        return externalStorageAvailable;
    }

    /**
     * Checks the state of the external storage.
     *
     * @return True if the external storage is writeable, false otherwise.
     */
    public boolean isExternalStorageWriteable() {
        checkStorage();
        return externalStorageWriteable;
    }

    /**
     * Checks the state of the external storage.
     *
     * @return True if the external storage is available and writeable, false
     * otherwise.
     */
    public boolean isExternalStorageAvailableAndWriteable() {
        checkStorage();
        //noinspection SimplifiableIfStatement
        if (!externalStorageAvailable) {
            return false;
        } else return externalStorageWriteable;
    }
}
