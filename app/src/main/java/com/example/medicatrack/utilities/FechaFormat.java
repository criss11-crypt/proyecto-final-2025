package com.example.medicatrack.utilities;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public final class FechaFormat
{
    private FechaFormat(){};

    public static String formattedHora(ZonedDateTime date)
    {
        StringBuilder text = new StringBuilder();
        if(date.getHour() < 10) text.append("0");
        text.append(date.getHour()).append(":");
        if(date.getMinute() < 10) text.append("0");
        text.append(date.getMinute());
        return text.toString();
    }

    public static int greaterTime(ZonedDateTime date, ZonedDateTime date2)  //1 si date > date2, -1 si date < date2
    {
        if(date.getHour() == date2.getHour())
        {
            return Integer.compare(date.getMinute(), date2.getMinute());
        }
        else return Integer.compare(date.getHour(),date2.getHour());
    }

    public static List<DayOfWeek> toDiasSemana(String dias)
    {
        List<DayOfWeek> lista = new ArrayList<>();
        byte[] bytes = dias.getBytes();
        for (byte num : bytes)
        {
            switch (num)
            {
                case '1':
                    lista.add(DayOfWeek.MONDAY);
                    break;
                case '2':
                    lista.add(DayOfWeek.TUESDAY);
                    break;
                case '3':
                    lista.add(DayOfWeek.WEDNESDAY);
                    break;
                case '4':
                    lista.add(DayOfWeek.THURSDAY);
                    break;
                case '5':
                    lista.add(DayOfWeek.FRIDAY);
                    break;
                case '6':
                    lista.add(DayOfWeek.SATURDAY);
                    break;
                case '7':
                    lista.add(DayOfWeek.SUNDAY);
                    break;

            }
        }
        return lista;
    }

}
