package itkach.aard2;

import android.net.Uri;
import android.util.Log;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class Util {

    static final String TAG = Util.class.getSimpleName();

    static int compare(long l1, long l2) {
        return l1 < l2 ? -1 : (l1 == l2 ? 0 : 1);
    }

    static <T extends Comparable<? super T>> void sort(List<T> list) {
        try {
            Collections.sort(list);
        }
        catch(Exception e) {
            Log.w(TAG, "Error while sorting:", e);
        }
    }

    static <T> void sort(List<T> list, Comparator<? super T> comparator) {
        try {
            Collections.sort(list, comparator);
        }
        catch(Exception e) {
            Log.w(TAG, "Error while sorting:", e);
        }
    }

    static boolean isBlank(String value) {
        return value == null || value.trim().equals("");
    }

    static String wikipediaToSlobUri(Uri uri) {
        String host = uri.getHost();
        if (isBlank(host)) {
            return null;
        }
        String normalizedHost = host;
        String[] parts = host.split("\\.");
        //if mobile host like en.m.wikipedia.opr get rid of m
        if (parts.length == 4) {
            normalizedHost = String.format("%s.%s.%s", parts[0], parts[2], parts[3]);
        }
        return "http://"+normalizedHost;
    }

}
