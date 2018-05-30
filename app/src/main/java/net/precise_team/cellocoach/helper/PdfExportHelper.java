package net.precise_team.cellocoach.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.github.sundeepk.compactcalendarview.domain.Event;

import net.precise_team.cellocoach.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PdfExportHelper {

    private PdfDocument document;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private View view;

    public PdfExportHelper(View view) {
        this.view = view;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            document = new PdfDocument();
        }
        else {
            Snackbar.make(view,"Android version is not compatible", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void createDocument(Context context, final LocalDatabaseHelper databaseHelper){
        // User select a date to export with Dialog
        final CharSequence[] listOfDate = getDateFromEvent(databaseHelper.getAllDate());
        AlertDialog.Builder dateSelector = new AlertDialog.Builder(context);
        dateSelector.setTitle(R.string.pdf_date_title);
        dateSelector.setItems(listOfDate, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                List<DataHisto> dataToExport = databaseHelper.getHistoScoresFromDate(String.valueOf(listOfDate[i]));
                // create page in PDF document with data selected
                int pageIndex = 1;
                for (DataHisto dataHisto : dataToExport){
                    createPage(dataHisto, pageIndex);
                    pageIndex++;
                }
                // Save document
                try {
                    saveDocumentFile(String.valueOf(listOfDate[i]));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void saveDocumentFile(String fileName) throws IOException {
        if (document != null){
            File folder = new File(Environment.getExternalStorageDirectory(), "CelloCoachExport");
            if(!folder.exists()){
                folder.mkdirs();
            }
            File mPdfFile = new File(folder,fileName+".pdf");
            if(!mPdfFile.exists()){
                mPdfFile.createNewFile();
            }
            FileOutputStream os = new FileOutputStream(mPdfFile);
            document.writeTo(os);
            Snackbar.make(view,view.getContext().getString(R.string.pdf_msg_file_created)+
                    folder,Snackbar.LENGTH_LONG).show();
        }
        else {
            Snackbar.make(view,R.string.pdf_msg_error, Snackbar.LENGTH_SHORT).show();
            Log.e(getClass().getName(), "Error creating PDF file : " +
                    "document is null");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void createPage(DataHisto dataHisto, int pageNumber) {
        File extStore = Environment.getExternalStorageDirectory();
        if (document != null){
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo
                    .Builder(595, 842, pageNumber).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            //TODO : Improve result
            canvas.drawText("Date : "+sdf.format(dataHisto.getDate().getTime()),15,20, new Paint());
            canvas.drawText("Scale : "+dataHisto.getScaleName(),15,40, new Paint());
            int iterator = 20;
            for (Double value : dataHisto.getScores()){
                canvas.drawText(String.valueOf(round(value.floatValue(),2)), iterator, 60, new Paint());
                iterator = iterator + 40;
            }
            document.finishPage(page);
        }
        else {
            Snackbar.make(view,R.string.pdf_msg_error, Snackbar.LENGTH_SHORT).show();
            Log.e(getClass().getName(), "Error creating page in document PDF cause : " +
                    "document is null");
        }
    }

    private CharSequence[] getDateFromEvent(List<Event> allDate) {
        List<String> dates = new ArrayList<>();
        for (Event event : allDate){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(event.getTimeInMillis());
            if (!dates.contains(sdf.format(calendar.getTime())))
                dates.add(sdf.format(calendar.getTime()));
        }
        return dates.toArray(new CharSequence[dates.size()]);
    }

    /**
     * Thanks StackOverFlow :
     * http://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
     * @param value
     * @param places
     * @return
     */
    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
