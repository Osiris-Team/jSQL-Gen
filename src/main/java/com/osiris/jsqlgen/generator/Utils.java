package com.osiris.jsqlgen.generator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class Utils {

    public static List<String> mergeListContents(List<String>... lists) {
        List<String> unified = new ArrayList<>();
        for (List<String> list : lists) {
            for (String s : list) {
                if (!unified.contains(s))
                    unified.add(s);
            }
        }
        return unified;
    }

    public static LinkedHashSet<String> mergeListContents(LinkedHashSet<String>... lists) {
        LinkedHashSet<String> unified = new LinkedHashSet<>();
        for (LinkedHashSet<String> list : lists) {
            for (String s : list) {
                unified.add(s);
            }
        }
        return unified;
    }
}
