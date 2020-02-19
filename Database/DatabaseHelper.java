package de.hsf.mobcomgroup1.runourway.Database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

public interface DatabaseHelper {
    /*
   ------------------------------------------------------------------------------------------------
                   Methoden für das Einfügen von Daten in die Datenbank
   ------------------------------------------------------------------------------------------------
    */

    /*
     * Statistiken für die einzelnen gesamten Läufe werden in die Tabelle eingefügt.
     * Übergabeparameter sind die Zeit in Millisekunden(int), die Distanz in km (float)
     * und ein Bild als Byte-Array.
     * Gibt true zurück, wenn erfolgreich eingefügt wurde.
     */
    boolean insertRun(int time, float distance, byte[] image);

    /*
     *Methode nur zum füllen mit daten in der Vergangenheit um Satistiken manipulieren zu
     *können. Statistiken werden zu presentationszwecken manipuliert. Wird im Betrieb nicht benötigt!
     */
    boolean insertRunWithDate(SQLiteDatabase db, int time, float distance, String date);

     /*
    ------------------------------------------------------------------------------------------------
                           Methoden für die Ausgabe/Abfrage von Daten
    ------------------------------------------------------------------------------------------------
     */

    /*
     * Gibt alle in der Tabelle Runs gespeicherten Daten zurück.
     * Der Rückgabeparameter ist ein Cursor. Für die Darstellung bietet sich ein ListView an.
     */
    Cursor getAllDataRuns();

    /*
     * Es werden das Datum und die gewünschten Werte der letzten übergebenen Anzahl an Tagen
     * aus der Datenbank ausgelesen. Anschließend werden die Werte in einer Hashmap gespeichert
     * und zurückgegeben.
     * Übergabeparamter:
     * -days: Anzahl der Tage, die ab dem Zeitpunkt der Abfrage zurückgeblickt werden
     * -mode: welche Werte zurückgegeben werden sollen. Möglich sind "time","distance" und "pace".
     * Rückgabeparameter:
     * -Hashmap mit dem Datum als Schlüssel(Long) und die geforderten Daten als zugeordnetem
     *  Wert(Float).
     */
    Map<Long,Float> getBarChartaData(int days, String mode);

    /*
     * Es wird eine Map mit den Daten als Schlüssel(Long) und der dazugehörigen id als Wert(int)
     * für den übergebenen Zeitraum(Tage als Integerwert) zurückgegeben.
     */
    Map<Long,Integer> getDateWithId(int days);

    /*
     * Es wird eine id(int) für einen Lauf übergeben und ein Cursor mit allen Daten zu diesem Lauf
     * zurückgegeben.
     */
    Cursor getRunData(int id);

    // Maximal gelaufene Strecke wird aus der Datenbank ausgelesen und als String zurückgegeben
    String getMaxDistance();

    // Die Gesamtstrecke der Läufe, die in der Datenbank gespeichert sind, wird zurückgegeben.
    String getTotalDistance();

    // Die Gesamtzeit aller gespeicherten Läufe wird zurückgegeben
    String getTotalTime();

    // Die Anzahl der in der Datenbank gelisteten Läufe wird zurückgegeben.
    String getCountOfRuns();
}
