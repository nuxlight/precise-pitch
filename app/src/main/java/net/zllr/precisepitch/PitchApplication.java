package net.zllr.precisepitch;

import android.app.Application;
import android.content.Context;
import org.acra.*;
import org.acra.annotation.*;


/**
 * Application class to add ARAS reporting
 */

@AcraCore(buildConfigClass = BuildConfig.class)
public class PitchApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}
