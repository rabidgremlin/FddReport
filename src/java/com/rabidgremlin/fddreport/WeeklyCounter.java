package com.rabidgremlin.fddreport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

public class WeeklyCounter
{
  private HashMap<Integer, WeekCount> counts = new HashMap<Integer, WeekCount>();

  public WeeklyCounter(XMLGregorianCalendar projectStart)
  {
    getCount(projectStart); 
  }
  
  public void incrementPlanned(XMLGregorianCalendar plannedDate)
  {
    WeekCount count = getCount(plannedDate);

    count.plannedCount += 1;
  }

  public void incrementActual(XMLGregorianCalendar actualDate)
  {
    WeekCount count = getCount(actualDate);

    count.actualCount += 1;
  }

  private WeekCount getCount(XMLGregorianCalendar date)
  {
    int weekNumber = date.getYear() * 52 + date.toGregorianCalendar().get(GregorianCalendar.WEEK_OF_YEAR);

    WeekCount count = counts.get(weekNumber);

    if (count == null)
    {
      count = new WeekCount();
      count.weekNumber = weekNumber;
      counts.put(weekNumber, count);
    }
    return count;
  }

  public List<WeekCount> getCounts()
  {
    List<WeekCount> tempCounts = new ArrayList<WeekCount>(counts.values());

    Collections.sort(tempCounts);

    return tempCounts;
  }
}
