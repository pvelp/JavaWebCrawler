package gg.bmstu.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.List;
import java.util.UUID;


public class NewsEntity {
    private String id;
    private String header;
    private String text;
    private String summary;
    private String URL;
    private String date;
    private String time;
    private String place;
    private String hash;
//    private List<String> themes;
//    private List<String> persons;

    public NewsEntity(String header, String text, String summary, String URL, String date, String time, String place, String hash) {
        this.id = UUID.randomUUID().toString();
        this.header = header;
        this.text = text;
        this.summary = summary;
        this.URL = URL;
        this.date = date;
        this.time = time;
        this.place = place;
        this.hash = hash;
//        this.themes = themes;
//        this.persons = persons;
    }

    public NewsEntity(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

//    public List<String> getThemes() {
//        return themes;
//    }
//
//    public void setThemes(List<String> themes) {
//        this.themes = themes;
//    }
//
//    public List<String> getPersons() {
//        return persons;
//    }
//
//    public void setPersons(List<String> persons) {
//        this.persons = persons;
//    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void objectFromStrJson(String jsonData) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(jsonData);
        this.id = node.get("id").asText();
        this.header = node.get("header").asText();
        this.text = node.get("text").asText();
        this.summary = node.get("summary").asText();
        this.URL = node.get("url").asText();
        this.date = node.get("date").asText();
        this.time = node.get("time").asText();
        this.place = node.get("place").asText();
        this.hash = node.get("hash").asText();
    }

    public String toJsonString() throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(this);
    }

    @Override
    public String toString() {
        return "NewsEntity{" +
                "id='" + id + '\'' +
                ", header='" + header + '\'' +
                ", text='" + text + '\'' +
                ", summary='" + summary + '\'' +
                ", URL='" + URL + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", place='" + place + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
}
