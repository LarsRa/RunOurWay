package de.hsf.mobcomgroup1.runourway.HelpfulClasses;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class Permission {

    public static boolean askForPermissions(Activity activity){
        boolean result = checkPermissions(activity,Manifest.permission.ACCESS_FINE_LOCATION
                , Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(!result){
           // Snackbar.make(view, "Ohne Rechte kann die App leider nicht richtig funktionieren.", 10000).show();
        }
        return result;
    }

    private static boolean checkPermissions(Activity activity, String... permissions){
        List<String> uncheckedPermissions = filterUncheckedPermissions(activity,permissions);
        if(uncheckedPermissions.size() > 0){
            ActivityCompat.requestPermissions(activity, ListConverter.listToArray(String.class,uncheckedPermissions),1);
            /**
             * zweite abfrage -> sollte keine ergebnisse liefern
             */
            uncheckedPermissions = filterUncheckedPermissions(activity,permissions);
        }
        return uncheckedPermissions.size() == 0;
    }

    private static ArrayList<String> filterUncheckedPermissions(Context context, String... permissions){
        ArrayList<String> result = new ArrayList<>();
        for(String permission : permissions)
        {
            if(!hasPermission(context,permission)){
                result.add(permission);
            }
        }
        return result;
    }

    private static boolean hasPermission(Context context, String permission){
        return ActivityCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_GRANTED;
    }


}
