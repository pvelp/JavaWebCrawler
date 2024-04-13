package gg.bmstu.entity;

import java.util.List;
import java.util.UUID;


public class NewsEntity {
    private UUID id;
    private String header;
    private String text;
    private String summary;
    private String URL;
    private String date;
    private String time;
    private String place;
    private List<String> themes;
    private List<String> persons;
    private String author;

    public NewsEntity(String header, String text, String summary, String URL, String date, String time, String place, List<String> themes, List<String> persons, String author) {
        this.id = UUID.randomUUID();
        this.header = header;
        this.text = text;
        this.summary = summary;
        this.URL = URL;
        this.date = date;
        this.time = time;
        this.place = place;
        this.themes = themes;
        this.persons = persons;
        this.author = author;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public List<String> getThemes() {
        return themes;
    }

    public void setThemes(List<String> themes) {
        this.themes = themes;
    }

    public List<String> getPersons() {
        return persons;
    }

    public void setPersons(List<String> persons) {
        this.persons = persons;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Override
    public String toString() {
        return "NewsEntity{" +
                "id=" + id +
                ", header='" + header + '\'' +
                ", text='" + text + '\'' +
                ", summary='" + summary + '\'' +
                ", URL='" + URL + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", place='" + place + '\'' +
                ", themes=" + themes +
                ", persons=" + persons +
                ", author='" + author + '\'' +
                '}';
    }
}
