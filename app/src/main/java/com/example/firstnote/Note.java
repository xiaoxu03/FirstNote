package com.example.firstnote;

public class Note {
    String title = "";
    String first_line = "";
    String labels = "";
    boolean select = false;
    public boolean isSelect() {
        return select;
    }

    public String getTitle() {
        return title;
    }
    public String getFirst_line() {
        return first_line;
    }
    public String getLabels() {
        return labels;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setFirst_line(String first_line) {
        this.first_line = first_line;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public Note() {
    }

    public Note(String title, String first_line, String labels) {
        this.title = title;
        this.first_line = first_line;
        this.labels = labels;
    }

    public Note(String title, String first_line, String labels, boolean select) {
        this.title = title;
        this.first_line = first_line;
        this.labels = labels;
        this.select = select;
    }
}
