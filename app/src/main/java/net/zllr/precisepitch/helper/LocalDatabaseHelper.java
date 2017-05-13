package net.zllr.precisepitch.helper;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class will help to save your scores in game on local database (NoSQL)
 */

public class LocalDatabaseHelper {

    private Manager manager;
    private Database database;
    private String databaseName = "precise";

    public LocalDatabaseHelper(Context context) throws IOException, CouchbaseLiteException {
        manager = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
        database = manager.getDatabase(databaseName);
    }

    public void addScore(String scale, Deque<Double> scores){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(new Date());
        Map<String, Object> newEntry = new HashMap<String, Object>();
        Map<String, Object> scaleEntry = new HashMap<String, Object>();
        Document document = database.getDocument(date);
        if (document.getProperties()!=null){
            newEntry.putAll(document.getProperties());
            scaleEntry.putAll((Map<? extends String, ?>) document.getProperties().get(date));
        }
        //add new values
        scaleEntry.put(scale,scores.toArray());
        newEntry.put(date,scaleEntry);
        try {
            document.putProperties(newEntry);
            Log.i(getClass().getName(),"A new entry in local database for date "+date);
        } catch (CouchbaseLiteException e) {
            Log.e(getClass().getName(), "Error to save score in database : "+e.toString());
        }
    }

    public Map<String, Object> getHistoScoresFromDate(String date){
        return database.getDocument(date).getProperties();
    }

    public List<String> getAllDate(){
        List<String> listOfDocKeys = new ArrayList<>();
        Query query = this.database.createAllDocumentsQuery();
        query.setAllDocsMode(Query.AllDocsMode.ALL_DOCS);
        try {
            QueryEnumerator result = query.run();
            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                QueryRow doc = it.next();
                listOfDocKeys.add(doc.getDocument().getId());
            }
        } catch (CouchbaseLiteException e) {
            Log.e(getClass().getName(),"Error in query : "+e);
        }
        return listOfDocKeys;
    }
}
