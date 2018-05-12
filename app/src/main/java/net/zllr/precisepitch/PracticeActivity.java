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
package net.zllr.precisepitch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.zllr.precisepitch.helper.LocalDatabaseHelper;
import net.zllr.precisepitch.model.DisplayNote;
import net.zllr.precisepitch.model.Note;
import net.zllr.precisepitch.model.NoteDocument;
import net.zllr.precisepitch.view.CenterOffsetView;
import net.zllr.precisepitch.view.CombineAnnotator;
import net.zllr.precisepitch.view.HighlightAnnotator;
import net.zllr.precisepitch.view.HistogramAnnotator;
import net.zllr.precisepitch.view.PraticeTimerView;
import net.zllr.precisepitch.view.StaffView;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class PracticeActivity extends Activity {
    private static final String BUNDLE_KEY_MODEL = "PracticeActivity.model";
    private static final int kCentThreshold = 20;
    private static final int kHighlightColors[] = {
            Color.argb(70, 0xff, 0, 0),     // red
            Color.argb(70, 0xff, 0x80, 0),  // orange
            Color.argb(70, 0xff, 0xff, 0),   // yellow
    };

    // All the activity state that we need to keep track of between teardown
    // restart.
    private static class ActivityState implements Serializable {
        public ActivityState() {
            noteModel = new NoteDocument();
        }
        final NoteDocument noteModel;
        int followPos = -1;
    };

    private ActivityState istate;
    private TuneChoiceControl tuneChoice;
    private StaffView staff;
    private CenterOffsetView ledview;
    private Button startbutton;
    private TextView instructions;
    private PraticeTimerView timerView;
    private Button newPractice;
    private Button canDoBetter;
    private NoteFollowRecorder noteFollower;
    private Deque<Double> practiceResult;
    private List<String> timePracticeResult;
    private LocalDatabaseHelper databaseHelper;
    private String baseNote;
    private String noteSelection;

    private enum State {
        EMPTY_SCALE,     // initial state or after 'clear'
        WAIT_FOR_START,  // when notes are visible. Start button visible.
                         // TODO: should be automatic when !isEmpty()
        PRACTICE,        // The game.
        FINISHED         // finished assignment.
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.practice);

        staff = (StaffView) findViewById(R.id.practiceStaff);
        staff.setNotesPerStaff(16);

        practiceResult = new ArrayDeque<Double>();
        timePracticeResult = new ArrayList<>();
        try {
            databaseHelper = new LocalDatabaseHelper(getApplicationContext());
        } catch (Exception e) {
            Log.e(getClass().getName(), "Error in creating local database "+e.toString());
        }
        // For now we have a couple of buttons to create some basics, but
        // these should be replaced by: (a) Spinner (for choosing scales and randomTune)
        // and (b) direct editing.

        ledview = (CenterOffsetView) findViewById(R.id.practiceLedDisplay);
        ledview.setQuantization(2.5f);
        ledview.setRange(Math.min(50, kCentThreshold + 10));
        ledview.setKeepScreenOn(true);
        ledview.setMarkAt(kCentThreshold);
        startbutton = (Button) findViewById(R.id.practiceStartButton);
        startbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doPractice();
            }
        });
        newPractice = (Button) findViewById(R.id.newPractice);
        newPractice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Walk our view to the start screen to choose tune, but also
                // offer start-button, as things are already chosen.
                setActivityState(State.FINISHED);
                setActivityState(State.EMPTY_SCALE);
                setActivityState(State.WAIT_FOR_START);  // We already have notes
            }
        });
        canDoBetter = (Button) findViewById(R.id.canDoBetterButton);
        canDoBetter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setActivityState(State.WAIT_FOR_START);
                doPractice();
            }
        });

        instructions = (TextView) findViewById(R.id.practiceInstructions);

        if (savedInstanceState != null) {
            istate = (ActivityState) savedInstanceState.getSerializable(BUNDLE_KEY_MODEL);
        }
        if (istate == null) {
            istate = new ActivityState();
        }
        staff.setNoteModel(istate.noteModel);

        staff.ensureNoteInView(0);

        tuneChoice = (TuneChoiceControl) findViewById(R.id.tuneChoice);
        tuneChoice.setNoteModel(staff.getNoteModel());
        tuneChoice.setOnChangeListener(new TuneChoiceControl.OnChangeListener() {
            @Override
            public void onChange() {
                staff.ensureNoteInView(0);
                setActivityState(staff.getNoteModel().isEmpty()
                                         ? State.EMPTY_SCALE
                                         : State.WAIT_FOR_START);
                staff.onModelChanged();
                baseNote = Note.getNoteString(getApplicationContext(),staff.getNoteModel().getNotes().get(0).note);
                noteSelection = tuneChoice.getScaleName();
            }
        });

        setActivityState(State.EMPTY_SCALE);  // need to walk this state first.
        if (!istate.noteModel.isEmpty()) {
            setActivityState(State.WAIT_FOR_START);
        }

        // Now configure Timer View
        timerView = (PraticeTimerView) findViewById(R.id.practiceTimer);
        timerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        databaseHelper.close();
        super.onBackPressed();
    }

    // TODO: remember practice result per model and persist.
    private void addPracticeResult(double centOff) {
        practiceResult.addFirst(centOff);
        while (practiceResult.size() > 3) {
            practiceResult.removeLast();
        }
    }

    // Callbacks from the NoteFollowRecorder. We use this to record statistics.
    private class FollowEventListener implements NoteFollowRecorder.EventListener {
        public void onStartModel(NoteDocument model) {
            startPracticeTime = -1;
            positionalResult = new float [model.size()];
            instructions.setText(getString(R.string.start_message));
        }

        public void onFinishedModel(NoteDocument model) {
            // Now, annotate the worst notes. We could sort the whole list, but for the handful
            // of values in there, it is cheaper to just go through it
            for (int hidx= 0; hidx < Math.min(kHighlightColors.length, positionalResult.length); hidx++) {
                // Find worst
                int worst = 0;
                float val = -1;
                for (int i = 0; i < positionalResult.length; ++i) {
                    if (positionalResult[i] > val) {
                        val = positionalResult[i];
                        worst = i;
                    }
                }
                positionalResult[worst] = -1;
                DisplayNote.Annotator oldAnnotator = model.get(worst).annotator;
                CombineAnnotator combined = new CombineAnnotator();
                combined.addAnnotator(oldAnnotator);
                combined.addAnnotator(new HighlightAnnotator(kHighlightColors[hidx]));
                model.get(worst).annotator = combined;
            }
            final double centOff = sumAbsoluteOffset / absoluteOffsetCount;
            String result = String.format(getString(R.string.average_msg)+" %.1f¢ "+getString(R.string.off_msg), centOff);
            if (practiceResult.size() > 0) {
                result += " ("+getString(R.string.last_msg)+" ";
                boolean isFirst = true;
                for (Double d : practiceResult) {
                    result += String.format("%s%.1f", isFirst ? "" : ", ", d);
                    isFirst = false;
                }
                result += ")";
            }
            result += ".";
            instructions.setText(result);
            setActivityState(State.FINISHED);
            addPracticeResult(centOff);
            //TODO : Add worst notes in function
            databaseHelper.addScore(noteSelection,practiceResult,timePracticeResult);
            //Test the result of the practice
            skillResultTest(Double.valueOf(centOff));
        }

        public void onStartNote(int modelPos, DisplayNote note) {
            currentHistogram = new Histogram(100);
            currentNoteOffsetCount = 0;
            sumCurrentNoteOffset = 0;
            currentModelPos = modelPos;
            currentNote = note;
        }

        public void onSilence() {
            ledview.setDataValid(false);
        }

        public void onNoteMiss(int diff) {
            ledview.setDataValid(true);
            ledview.setValue(diff * 100);  // displays too low/high arrows
        }

        public boolean isInTune(double cent, int ticksInTuneSoFar) {
            ledview.setDataValid(true);
            ledview.setValue(cent);
            // The following stat can probably go as we can determine a better
            // score out of the histogram data.
            sumCurrentNoteOffset += Math.abs(cent);
            currentNoteOffsetCount++;

            currentHistogram.increment((int)(cent + 50.0));

            // Give some instructions depending on ticks in tune.
            if (ticksInTuneSoFar == 0) {
                if (startPracticeTime > 0) {
                    instructions.setText(getString(R.string.find_note_hold_msg));
                }
            } else if (startPracticeTime < 0 && ticksInTuneSoFar > 5) {
                startPracticeTime = System.currentTimeMillis();
                instructions.setText(getString(R.string.we_are_now_msg));
            }

            return true; // accept everything. Accuracy recorded in histogram.
        }

        public void onFinishedNote() {
            sumAbsoluteOffset += sumCurrentNoteOffset;
            absoluteOffsetCount += currentNoteOffsetCount;
            currentHistogram.filter(20);
            currentNote.annotator = new HistogramAnnotator(staff.getNoteWidth(), currentHistogram);
            positionalResult[currentModelPos] = sumCurrentNoteOffset / currentNoteOffsetCount;
        }

        private long startPracticeTime;
        private float sumAbsoluteOffset;
        private long absoluteOffsetCount;

        private float sumCurrentNoteOffset;
        private long currentNoteOffsetCount;

        float positionalResult[];

        private int currentModelPos;
        private DisplayNote currentNote;
        private Histogram currentHistogram;
    }

    /**
     * This function test with the user's skill with result
     * @param aDouble
     */
    private void skillResultTest(Double aDouble) {
        String[] levelNameArray = getResources().getStringArray(R.array.intro_array_level);
        String[] levelValueArray = getResources().getStringArray(R.array.intro_array_level_values_ranges);
        int index = 0;
        for (String levelValue : levelValueArray){
            String[] valueParsed = levelValue.split("-");
            Double maxValue = Double.parseDouble(valueParsed[0]);
            Double minValue = Double.parseDouble(valueParsed[1]);
            if (aDouble < maxValue && aDouble > minValue){
                Log.i(getClass().getName(), "Your value is in range of "+levelNameArray[index]);
                // Now check if the new level is the same of the user's level
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String userSkillLevel = prefs.getString("USER_LEVEL", "NO_LEVEL");
                if (!userSkillLevel.equals("NO_LEVEL") && !userSkillLevel.equals(levelNameArray[index])){
                    String levelName = levelNameArray[index];
                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setMessage(getResources().getString(R.string.dialog_level_new)+levelName)
                            .setPositiveButton(getResources().getString(R.string.dialog_level_button_update), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString("USER_LEVEL", levelName).apply();
                                    editor.commit();
                                    Log.i(getClass().getName(),"Updating user's level (old : "+userSkillLevel+
                                    " | new : "+levelName);
                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.dialog_level_button_cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                    builder.show();
                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "You dont choose level check the preferences", Toast.LENGTH_LONG);
                }
            }
            index++;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (noteFollower != null)
            istate.followPos = noteFollower.getPosition();
        if (noteFollower != null) {
            // TODO: NoteFollower actually can handle being paused/resumed
            // now. Take this into account.
            noteFollower.pause();
            setActivityState(State.FINISHED);
            // Now prepare for a new game, with properly reset notes before
            // the state is serialized (the Paint in note-annotators doesn't serialize).
            setActivityState(State.EMPTY_SCALE);
            setActivityState(State.WAIT_FOR_START);  // We already have notes
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (noteFollower != null)
            noteFollower.resume(istate.followPos, getApplicationContext());
    }

    @Override
    protected void onSaveInstanceState(Bundle saveState) {
        saveState.putSerializable(BUNDLE_KEY_MODEL, istate);
    }

    private void doPractice() {
        if (istate.noteModel.isEmpty()) return;
        setActivityState(State.PRACTICE);
        noteFollower = new NoteFollowRecorder(staff, new FollowEventListener());
    }

    // Depending on the state we're in, enable/disable the
    // visibility of things.
    private void setActivityState(State state) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(staff.getContext());
        switch (state) {
            case EMPTY_SCALE:
                practiceResult.clear();
                startbutton.setVisibility(View.INVISIBLE);
                newPractice.setVisibility(View.INVISIBLE);
                canDoBetter.setVisibility(View.INVISIBLE);
                ledview.setVisibility(View.INVISIBLE);
                tuneChoice.setVisibility(View.VISIBLE);
                instructions.setText(getString(R.string.welcome_message));
                break;
            case WAIT_FOR_START:
                for (DisplayNote n : istate.noteModel.getNotes()) {
                    n.color = Color.BLACK;
                    n.annotator = null;
                }
                staff.ensureNoteInView(0);
                staff.onModelChanged();
                instructions.setText("");
                startbutton.setVisibility(View.VISIBLE);
                tuneChoice.setVisibility(View.VISIBLE);
                timerView.setVisibility(View.INVISIBLE);
                break;
            case PRACTICE:
                startbutton.setVisibility(View.INVISIBLE);
                newPractice.setVisibility(View.VISIBLE);
                canDoBetter.setVisibility(View.INVISIBLE);
                if (sharedPreferences.getBoolean("indicatormode", true)) {
                    ledview.setVisibility(View.VISIBLE);
                    ledview.setDataValid(false);
                }
                tuneChoice.setVisibility(View.INVISIBLE);
                timerView.startTimer();
                timerView.setVisibility(View.INVISIBLE);
                break;
            case FINISHED:
                if (noteFollower != null) {
                    noteFollower.pause();
                    noteFollower = null;
                }
                startbutton.setVisibility(View.INVISIBLE);
                newPractice.setVisibility(View.VISIBLE);
                canDoBetter.setVisibility(View.VISIBLE);
                ledview.setVisibility(View.INVISIBLE);
                ledview.setDataValid(false);
                tuneChoice.setVisibility(View.INVISIBLE);
                timerView.stopTimer();
                String timerTxt = getApplicationContext().getResources().getString(R.string.timer_msg);
                timerTxt = timerTxt+" "+timerView.getTimerInSeconds()+" second(s)";
                timerView.setText(timerTxt);
                timerView.setVisibility(View.VISIBLE);
                // for finish add time result in list
                timePracticeResult.add(timerTxt);
        }
    }
}
