package com.a.shon.scoutszone2;

import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

public abstract class PermissionUtil {

    /**
     * Check that all given permissions have been granted by verifying that each entry in the
     * given array is of the value {@link PackageManager#PERMISSION_GRANTED}.
     *
     * @see Activity#onRequestPermissionsResult(int, String[], int[])
     */
    public static boolean verifyAllPermissions(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean verifyPermissionsDetailed(Context context, String[] permissions, int[] grantResults) {

        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }

        boolean retVal = true;
        // Verify that each required permission has been granted, otherwise return false.
        for (int i = 0; i < grantResults.length; i++)
        {
            int result = grantResults[i];
            if (result != PackageManager.PERMISSION_GRANTED) {
                retVal = false;
                Toast.makeText(context, "Permission denied: " + permissions[i], Toast.LENGTH_LONG).show();
            }
        }
        return retVal;
    }
}