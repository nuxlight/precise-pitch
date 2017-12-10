package net.zllr.precisepitch.helper;

import org.dizitart.no2.objects.Id;

import java.util.Calendar;
import java.util.Deque;
import java.util.List;

/**
 * This class represent the data stored in database
 */

public class DataHisto {

    @Id
    private long histoId;
    private Calendar date;
    private String scaleName;
    private String notes;
    private List<String> timerList;
    private Deque<Double> scores;

    public String getScaleName() {
        return scaleName;
    }

    public void setScaleName(String scaleName) {
        this.scaleName = scaleName;
    }

    public Deque<Double> getScores() {
        return scores;
    }

    public void setScores(Deque<Double> scores) {
        this.scores = scores;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public long getHistoId() {
        return histoId;
    }

    public void setHistoId(long histoId) {
        this.histoId = histoId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<String> getTimerList() {
        return timerList;
    }

    public void setTimerList(List<String> timerList) {
        this.timerList = timerList;
    }
}
