package com.example.atlantic8.projectspartan;

/**
 * Created by Atlantic8 on 2016/2/26 0026.
 */
public class PictureInfo {
    public String title;
    public String user_name;
    public String time;
    public String picture_path;
    public String value;
    public String extra;

    /**
     *
     * @param pname : name of picture
     * @param uname : username
     * @param times : unix timestamp
     * @param ppath : path in server
     * @param values
     * @param extras
     */
    public PictureInfo (String title, String username, String times,
                         String picture_path, String values, String extras) {
        this.title = title;
        this.picture_path = picture_path;
        this.value = values;
        this.time = this.TimeStamp2Date(times);
        this.user_name = username;
        this.extra = extras;
    }

    public PictureInfo (String title, String username, String times,
                        String picture_path, String values) {
        this.title = title;
        this.picture_path = picture_path;
        this.value = values;
        this.time = this.TimeStamp2Date(times);
        this.user_name = username;
    }

    /**Convert Unix timestamp to normal date style
     *
     * @param timestampString : timestamp in string format
     * @return normal date in "dd/MM/yyyy HH:mm:ss" format
     */
    private String TimeStamp2Date(String timestampString){
        Long timestamp = Long.parseLong(timestampString)*1000;
        String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date(timestamp));
        return date;
    }

}
