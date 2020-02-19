package de.hsf.mobcomgroup1.runourway.HelpfulClasses;

import java.lang.reflect.Array;
import java.util.List;

public class ListConverter {


    public static <T> T[] listToArray(Class<T> type, List<T> list){
        T[] result = (T[]) Array.newInstance(type,list.size());

        for(int i = 0;i<list.size();i++){
            result[i] = list.get(i);
        }

        return result;
    }

}
