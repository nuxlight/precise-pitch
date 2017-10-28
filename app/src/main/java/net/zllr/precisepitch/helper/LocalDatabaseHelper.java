package net.zllr.precisepitch.helper;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.github.sundeepk.compactcalendarview.domain.Event;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.objects.ObjectRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.List;

/**
 * This class will help to save your scores in game on local database (NoSQL)
 */

public class LocalDatabaseHelper {

    private String databaseName = "preciseDB.db";
    private ObjectRepository<DataHisto> repository;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private Nitrite db;

    public LocalDatabaseHelper(Context context) {
        // android initialization
        db = Nitrite.builder()
                .compressed()
                .filePath(context.getFilesDir().getPath() + databaseName)
                .openOrCreate();

        // Create an Object Repository
        repository = db.getRepository(DataHisto.class);
    }

    public void addScore(String scale, Deque<Double> scores){
        boolean newEntry = true;
        if (repository != null){
            // Check for update
            for (DataHisto dataHisto : repository.find()){
                if ((sdf.format(Calendar.getInstance().getTime()))
                        .equals(sdf.format(dataHisto.getDate().getTime())) &&
                        (dataHisto.getScaleName().equals(scale))){
                    // this is an update
                    Deque<Double> tempDeq = dataHisto.getScores();
                    tempDeq.add(scores.getLast());
                    dataHisto.setScores(tempDeq);
                    repository.update(dataHisto);
                    Log.i(getClass().getName(), "Data updated for scale "+scale);
                    newEntry = false;
                }
            }
            if (newEntry) {
                DataHisto newDataHisto = new DataHisto();
                newDataHisto.setHistoId(System.currentTimeMillis());
                newDataHisto.setDate(Calendar.getInstance());
                newDataHisto.setScaleName(scale);
                newDataHisto.setScores(scores);
                repository.insert(newDataHisto);
                Log.i(getClass().getName(), "New data in database");
            }
        }
        else {
            Log.e(getClass().getName(), "Error, database not initialized ...");
        }
    }

    public List<DataHisto> getHistoScoresFromDate(String date) {
        List<DataHisto> dataHistos = new ArrayList<>();
        if (repository != null){
            for (DataHisto dataHisto : repository.find()){
                if (sdf.format(dataHisto.getDate().getTime()).equals(date)){
                    dataHistos.add(dataHisto);
                }
            }
        }
        return dataHistos;
    }

    public List<Event> getAllDate() {
        List<Event> dateList = new ArrayList<>();
        if (repository != null){
            for (DataHisto dataHisto : repository.find()){
                if (!dateList.contains(sdf.format(dataHisto.getDate().getTimeInMillis()))){
                    Calendar tempCal = dataHisto.getDate();
                    Event event = new Event(Color.YELLOW,tempCal.getTimeInMillis());
                    dateList.add(event);
                }
            }
        }
        return dateList;
    }

    public void close() {
        if (!db.isClosed()){
            db.close();
        }
    }
}
