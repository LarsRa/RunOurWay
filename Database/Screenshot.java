package de.hsf.mobcomgroup1.runourway.Database;


import android.graphics.Bitmap;
import android.view.View;

public class Screenshot {

    /*
     * Von dem übergebenen Viewelement wird ein Screenshot gemacht und als Bitmap zurückgegeben.
     */
    public static Bitmap takeScreenshot(View view){
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        Bitmap b = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return b;
    }
}
