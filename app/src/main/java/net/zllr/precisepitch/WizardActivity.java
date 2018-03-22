package net.zllr.precisepitch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

public class WizardActivity extends IntroActivity {

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
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext())
                                .setTitle(view.getContext().getString(R.string.notes_title_string))
                                .setView(view.getRootView())
                                .setPositiveButton(view.getContext().getString(R.string.notes_btn_string),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        });
                        builder.create().show();
                    }
                })
                .background(R.color.slideTwoColor)
                .image(R.drawable.ic_supervisor_account)
                .build());
    }
}
