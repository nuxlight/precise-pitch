/*
 * Copyright 2013 Henner Zeller <h.zeller@acm.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.precise_team.cellocoach;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import net.precise_team.cellocoach.model.MeasuredPitch;
import net.precise_team.cellocoach.view.CenterOffsetView;

public class TunerActivity extends Activity {
    private static final int kCentThreshold = 10;  // TODO: make configurable
    private static final boolean kShowTechInfo = false;

    private TextView frequencyDisplay;
    private TextView noteDisplay;
    private TextView decibelView;
    private TextView prevNote;
    private TextView nextNote;
    private TextView instruction;
    private CenterOffsetView offsetCentView;

    private PitchSource pitchPoster;
    private ImageView earIcon;

    private enum KeyDisplay {
        DISPLAY_FLAT,
        DISPLAY_SHARP,
    }
    private KeyDisplay keyDisplay = KeyDisplay.DISPLAY_SHARP;
    private String noteNames[][];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //Get noteList
        noteNames = getNoteNames();

        earIcon = (ImageView) findViewById(R.id.earIcon);
        prevNote = (TextView) findViewById(R.id.nextLower);
        nextNote = (TextView) findViewById(R.id.nextHigher);
        noteDisplay = (TextView) findViewById(R.id.noteDisplay);
        noteDisplay.setKeepScreenOn(true);
        noteDisplay.setText("");
        instruction = (TextView) findViewById(R.id.tunerInsruction);
        instruction.setText("");

        offsetCentView = (CenterOffsetView) findViewById(R.id.centView);
        offsetCentView.setRange(25);
        offsetCentView.setQuantization(2.5f);
        offsetCentView.setMarkAt(kCentThreshold);

        int techVisibility = kShowTechInfo ? View.VISIBLE : View.INVISIBLE;
        frequencyDisplay = (TextView) findViewById(R.id.frequencyDisplay);
        frequencyDisplay.setVisibility(techVisibility);
        decibelView = (TextView) findViewById(R.id.decibelView);
        decibelView.setVisibility(techVisibility);

        addAccidentalListener();        
    }
    
    private void addAccidentalListener() {
        final RadioGroup accidentalGroup = (RadioGroup) findViewById(R.id.accidentalSelection);
        accidentalGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.flatRadio:
                        keyDisplay = KeyDisplay.DISPLAY_FLAT;
                        break;
                    case R.id.sharpRadio:
                        keyDisplay = KeyDisplay.DISPLAY_SHARP;
                        break;
                }
            }
        });
        ((RadioButton) findViewById(R.id.sharpRadio)).setChecked(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pitchPoster.stopSampling();
        pitchPoster = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        pitchPoster = new MicrophonePitchSource(getApplicationContext());
        pitchPoster.setHandler(new UIUpdateHandler());
        pitchPoster.startSampling();
    }

    // Whenever MicrophonePitchSource has a new note value available, it will
    // post it to the message queue, received here.
    private final class UIUpdateHandler extends Handler {
        private final static int kMaxWait = 32;

        // Old Android versions don't seem to have the 'setAlpha()' method.
        private void setAlphaOnText(TextView v, float alpha) {
            int alpha_bits = ((int) (0xFF * alpha)) << 24;
            v.setTextColor(v.getCurrentTextColor() & 0xFFFFFF | alpha_bits);
        }

        private void setFadeableComponentAlpha(float alpha) {
            setAlphaOnText(noteDisplay, alpha);;
            setAlphaOnText(frequencyDisplay, alpha);
            setAlphaOnText(prevNote, alpha);
            setAlphaOnText(nextNote, alpha);
            setAlphaOnText(instruction, alpha);
            offsetCentView.setFadeAlpha(alpha);
        }

        public void handleMessage(Message msg) {
            final MeasuredPitch data
                = (MeasuredPitch) msg.obj;

            if (data != null && data.decibel > -30) {
                frequencyDisplay.setText(String.format(data.frequency < 200 ? "%.1fHz" : "%.0fHz",
                                                       data.frequency));
                final String printNote = noteNames[keyDisplay.ordinal()][data.note % 12];
                noteDisplay.setText(printNote.substring(0, printNote.length()));
                final String accidental = printNote.length() > 1 ? printNote.substring(1) : "";
                nextNote.setText(noteNames[keyDisplay.ordinal()][(data.note + 1) % 12]);
                prevNote.setText(noteNames[keyDisplay.ordinal()][(data.note + 11) % 12]);
                final boolean inTune = Math.abs(data.cent) <= kCentThreshold;
                final int c = inTune ? Color.rgb(50, 255, 50) : Color.rgb(255,50, 50);
                noteDisplay.setTextColor(c);
                if (!inTune) {
                    instruction.setText(data.cent < 0 ? getString(R.string.too_high_string) : getString(R.string.too_low_string));
                } else {
                    instruction.setText("");
                }
                setFadeableComponentAlpha(1.0f);
                offsetCentView.setValue((int) data.cent);
                fadeCountdown = kMaxWait;
            } else {
                --fadeCountdown;
                if (fadeCountdown < 0) fadeCountdown = 0;
                setFadeableComponentAlpha(1.0f * fadeCountdown / kMaxWait);
            }
            earIcon.setVisibility(data != null && data.decibel > -30
                                  ? View.VISIBLE : View.INVISIBLE);
            if (data != null && data.decibel > -60) {
                decibelView.setText(String.format("%.0fdB", data.decibel));
            } else {
                decibelView.setText("");
            }
            lastPitch = data;
        }

        private MeasuredPitch lastPitch;
        private int fadeCountdown;
    }

    private String[][] getNoteNames(){
        String noteNames[][] = {
                {
                        getString(R.string.note_A), getString(R.string.note_B_b), getString(R.string.note_B),
                        getString(R.string.note_C), getString(R.string.note_D_b), getString(R.string.note_D),
                        getString(R.string.note_E_b), getString(R.string.note_E), getString(R.string.note_F),
                        getString(R.string.note_G_b), getString(R.string.note_G), getString(R.string.note_A_b)
                },
                {
                        getString(R.string.note_A), getString(R.string.note_A_s), getString(R.string.note_B),
                        getString(R.string.note_C), getString(R.string.note_C_s), getString(R.string.note_D),
                        getString(R.string.note_D_s), getString(R.string.note_E), getString(R.string.note_F),
                        getString(R.string.note_F_s), getString(R.string.note_G), getString(R.string.note_G_s)
                }
        };
        return noteNames;
    }
}
