package gg.bmstu.entity;

import java.util.UUID;


public class NewsEntity {
    private UUID id;
    private String header;
    private String body;
    private String URL;
    private String date;
    private String author;

    public NewsEntity(UUID id, String header, String body ,String URL, String date, String author){
        this.id = id;
        this.header = header;
        this.body = body;
        this.URL = URL;
        this.date = date;
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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
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


    @Override
    public String toString() {
        return "NewsEntity{" +
                "id=" + id +
                ", header='" + header + '\'' +
                ", body='" + body + '\'' +
                ", URL='" + URL + '\'' +
                ", date='" + date + '\'' +
                ", author='" + author + '\'' +
                '}';
    }
}
