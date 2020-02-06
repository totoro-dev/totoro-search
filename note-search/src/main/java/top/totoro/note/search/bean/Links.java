package top.totoro.note.search.bean;

import java.io.Serializable;

public class Links implements Serializable {

    public String[] titles;
    public String[] notes;
    public String[] urls;

    public Links(String[] titles, String[] notes, String[] urls) {
        this.titles = titles;
        this.notes = notes;
        this.urls = urls;
    }

    public String[] getTitles() {
        return titles;
    }

    public void setTitles(String[] titles) {
        this.titles = titles;
    }

    public String[] getNotes() {
        return notes;
    }

    public void setNotes(String[] notes) {
        this.notes = notes;
    }

    public String[] getUrls() {
        return urls;
    }

    public void setUrls(String[] urls) {
        this.urls = urls;
    }
}
