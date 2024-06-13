package com.example.firstnote;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public List<String> getLabelsList() {
        List<String> labelList = new ArrayList<>();
        String[] labelArray = labels.split("/");
        Collections.addAll(labelList, labelArray);
        return labelList;
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

    public void addLabel(String label) {
        if (labels.isEmpty()) {
            labels = label;
        } else {
            labels += "/" + label;
        }
    }

    public Note() {
    }

    public Note(String title, String first_line, String labels) {
        this.title = title;
        this.first_line = first_line;
        setLabels(labels);
    }

    public Note(String title, String first_line, String labels, boolean select) {
        this.title = title;
        this.first_line = first_line;
        setLabels(labels);
        this.select = select;
    }
}
