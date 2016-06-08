package com.example.atlantic8.projectspartan;

/**
 * Created by Atlantic8 on 2016/3/3 0003.
 */
public class ServiceItem {
    public int startTime;
    public int endTime;
    public String tag;

    public ServiceItem(int start, int end, String t) {
        this.startTime = start;
        this.endTime = end;
        this.tag = t;
    }
}
