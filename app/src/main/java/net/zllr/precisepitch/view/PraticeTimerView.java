package net.zllr.precisepitch.view;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import net.zllr.precisepitch.R;

import java.util.Date;

/**
 * This class describe the Timer in practice activity
 */

public class PraticeTimerView extends android.support.v7.widget.AppCompatTextView {

    private long startTime;
    private Context appContext;

    public PraticeTimerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setTextSize(30);
        appContext = context;
    }

    public void startTimer() {
        startTime = 0;
        startTime = System.currentTimeMillis();
        Log.i(getClass().getName(),"Start timer for practice");
    }

    public void stopTimer() {
        Log.i(getClass().getName(),"Stop timer for practice : "+getTimerInSeconds());
        setText(String.valueOf(getTimerInSeconds()));
    }

    public long getTimerInSeconds() {
        return ((new Date()).getTime() - startTime) / 1000;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
