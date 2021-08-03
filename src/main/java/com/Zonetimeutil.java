package com;

import java.util.Date;
import java.util.TimeZone;

public class Zonetimeutil {
    public static void main(String args[]) {
        // Creating an object of TimeZone class.
        TimeZone time_zone_default = TimeZone.getTimeZone("America/Costa_Rica");

        time_zone_default.setDefault(time_zone_default);

        // Displaying the default TimeZone
        System.out.println("Default TimeZone: " + time_zone_default);
        Date kk = new Date();
        System.out.print(kk);
    }
}