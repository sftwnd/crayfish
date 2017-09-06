package com.github.sftwnd.crayfish.utils;

/**
 * Created by ashindarev on 03.02.16.
 */
public interface BaseConfigUtility {

    String getValue(String step, String name);
    String getValue(String name);
    String getValue(String step, String name, int id);
    String getValue(String name, int id);

    String[] getValues(String step, String name);
    String[] getValues(String name);
    Long[]   getLongValues(String step, String name);
    Long[]   getLongValues(String name);

}
