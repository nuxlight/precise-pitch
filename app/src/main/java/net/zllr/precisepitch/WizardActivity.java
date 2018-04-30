package net.zllr.precisepitch;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

public class WizardActivity extends IntroActivity {

    private EditText nameInput;
    private String nameUser;
    private String levelUser;
    private Context wizardContext = this;

    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setButtonBackVisible(false);
        setButtonNextVisible(false);
        addSlide(new SimpleSlide.Builder()
                .title(R.string.intro_slide_one_title)
                .description(R.string.intro_slide_one_description)
                .background(R.color.slideOneColor)
                .image(R.drawable.ic_launcher_material_web)
                .build());
        addSlide(new SimpleSlide.Builder()
                .title(R.string.intro_slide_two_title)
                .description(R.string.intro_slide_two_description)
                .buttonCtaLabel(R.string.intro_slide_two_btn)
                .buttonCtaClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nameInput = new EditText(view.getContext());
                        AlertDialog.Builder builder = new AlertDialog.Builder(wizardContext)
                                .setTitle(getApplicationContext().getString(R.string.intro_slide_two_dialog_user))
                                .setView(nameInput)
                                .setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                nameUser = nameInput.getText().toString();
                                                nextSlide();
                                            }
                                        });
                        builder.create().show();
                    }
                })
                .background(R.color.slideTwoColor)
                .image(R.drawable.ic_supervisor_account)
                .canGoBackward(false)
                .build());
        addSlide(new SimpleSlide.Builder()
                .title(R.string.intro_slide_three_title)
                .description(R.string.intro_slide_three_description)
                .buttonCtaLabel(R.string.intro_slide_three_btn)
                .buttonCtaClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(wizardContext);
                        builder.setTitle(getApplicationContext().getString(R.string.intro_slide_three_dialog_user))
                        .setItems(R.array.intro_array_level, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String[] levelList = getResources().getStringArray(R.array.intro_array_level);
                                levelUser = levelList[which];
                                nextSlide();
                            }
                        });
                        builder.create().show();
                    }
                })
                .background(R.color.slideThreeColor)
                .image(R.drawable.ic_level_icon)
                .canGoBackward(false)
                .build());
        addSlide(new SimpleSlide.Builder()
                .title(R.string.intro_slide_for_title)
                .description(R.string.intro_slide_for_description)
                .background(R.color.slideForColor)
                .image(R.drawable.ic_check_black)
                .build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveUserPreferences(nameUser, levelUser);
    }

    private void saveUserPreferences(String nameUser, String levelUser) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("FIRST_LAUNCH", true);
        editor.putString("USER_NAME", nameUser).apply();
        editor.putString("USER_LEVEL", levelUser).apply();
        editor.commit();
        Log.i(getClass().getName(), "User preferences set USER_NAME:"+nameUser+" | USER_LEVEL:"+levelUser);
    }

}
