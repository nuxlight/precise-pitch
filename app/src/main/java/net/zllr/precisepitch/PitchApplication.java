package net.zllr.precisepitch;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

/**
 * Application class to add ARAS reporting
 */

@ReportsCrashes(
        formUri = "http://www.backendofyourchoice.com/reportpath"
)
public class PitchApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}
