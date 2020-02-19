package de.hsf.mobcomgroup1.runourway.Database;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class BitmapUtils {

    /*
     * Konvertiert Bitmap in ein Array von Bytes
     * Wird benötigt um die Bitmap(Screenshot) in der Datenbank(Bytes) zu speichern.
     */
    public static byte[] getBytes(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,0,stream);
        return stream.toByteArray();
    }

    /*
     * Konvertiert ein Array von Bytes in eine Bitmap
     * Wird benötigt um die Bytes aus der Datenbank wieder als Bitmap in der View darstellen zu
     * lassen.
     */
    public static Bitmap getImage(byte[] data){
        return BitmapFactory.decodeByteArray(data,0,data.length);
    }
}

