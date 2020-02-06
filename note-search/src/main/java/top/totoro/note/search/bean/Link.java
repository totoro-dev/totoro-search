package top.totoro.note.search.bean;

import java.io.Serializable;

public class Link implements Serializable{
    public String title;
    public String note;
    public String url;

    public Link(String title, String note, String url) {
        this.title = title;
        this.note = note;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
