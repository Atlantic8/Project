package com.example.atlantic8.projectspartan;

/**
 * Created by Atlantic8 on 2016/3/4 0004.
 */
public class ServiceContentItem {
    public String content, tag;
    public int start_time, end_time;
    public String create_time;


    public ServiceContentItem(String content, String tag, int start_time, int end_time, String create_time) {
        this.content = content;
        this.tag = tag;
        this.start_time = start_time;
        this.end_time = end_time;
        this.create_time = create_time;
    }
}
