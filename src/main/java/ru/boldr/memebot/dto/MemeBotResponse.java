package ru.boldr.memebot.dto;

import java.util.Date;

public class MemeBotResponse {
    private Date date;
    private String url;
    private boolean cool;

    public MemeBotResponse(String url, boolean cool) {
        this.date = new Date();
        this.url = url;
        this.cool = cool;
    }

    public Date getDate() {
        return date;
    }

    public String getUrl() {
        return url;
    }

    public boolean isCool() {
        return cool;
    }
}
