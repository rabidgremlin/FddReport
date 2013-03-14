package com.rabidgremlin.fddreport;

public class WeekCount implements Comparable<WeekCount>
{
  int weekNumber;
  int actualCount;
  int plannedCount;

  @Override
  public int compareTo(WeekCount o)
  {
    Integer w1 = new Integer(weekNumber);

    return w1.compareTo(o.weekNumber);
  }
}
