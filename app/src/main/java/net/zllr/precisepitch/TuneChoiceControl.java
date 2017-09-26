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

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;

import net.zllr.precisepitch.model.DisplayNote;
import net.zllr.precisepitch.model.Note;
import net.zllr.precisepitch.model.NoteDocument;

import java.util.List;

public class TuneChoiceControl extends LinearLayout implements AdapterView.OnItemSelectedListener,
        View.OnClickListener {

    private static final int kMajorScaleSequence[] = { 2, 2, 1, 2, 2, 2, 1 };
    private static final int kMinorScaleSequence[] = { 2, 1, 2, 2, 1, 2, 2 };

    private NoteDocument model;
    private OnChangeListener changeListener;
    private CheckBox randomTune;
    private Button changeOctave;
    private boolean wantsFlat;
    private int baseNote;
    private int scaleSpinnerPosition = 0; // 0 -> major, 1 -> minor

    private static enum State {
        BASE_OCTAVE,
        HIGH_OCTAVE,
        TWO_OCTAVE,
    };
    private State state = State.BASE_OCTAVE;

    public interface OnChangeListener {
        void onChange();
    }

    public TuneChoiceControl(Context context) {
        super(context);
    }

    public TuneChoiceControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.tune_choice_component, this);
    }

    public void setOnChangeListener(OnChangeListener listener) {
        changeListener = listener;
    }
    public void setNoteModel(NoteDocument newModel) {
        if (this.model == null)
            InitializeListeners();
        this.model = newModel;
    }

    private void InitializeListeners() {
        // Options for the tuner activity
        final FixedNoteSequenceListener seqCreator = new FixedNoteSequenceListener();
        randomTune = (CheckBox) findViewById(R.id.tcRandomSequence);
        randomTune.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                changeScale();
            }
        });
        Button seq = (Button) findViewById(R.id.tcNewSeq);
        seq.setOnClickListener(seqCreator);

        // Octave option
        changeOctave = (Button) findViewById(R.id.changeOctave);
        changeOctave.setOnClickListener(this);

        // First spinner to select the base note
        Spinner spinner = (Spinner) findViewById(R.id.baseNoteSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.baseNoteArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        // Second spinner to select the scale type (major/minor)
        Spinner spinnerScale = (Spinner) findViewById(R.id.scaleSelectSpinner);
        ArrayAdapter<CharSequence> adapterScale = ArrayAdapter.createFromResource(getContext(),
                R.array.scaleNoteArray, android.R.layout.simple_spinner_item);
        adapterScale.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerScale.setAdapter(adapterScale);
        spinnerScale.setOnItemSelectedListener(this);
    }

    private final class FixedNoteSequenceListener implements View.OnClickListener {
        int sequenceNumber = 0;
        public void onClick(View button) {
            if (model == null) {
                return;
            }
            final int[][] sequences = {
                    {19, 23, 21, 23, 17, 16, 14, 16, 19, 23, 21, 19, 16, 12, 14, 16},
                    {19, 23, 21, 23, 19, 16, 19, 16, 14, 16, 12, 16, 10, 16, 14, 16},
                    { 5,  9, 12,  9,  7,  9,  5,  9,  7, 10, 16, 12,  9,  5,  9,  5}};
            model.clear();
            model.setFlat(false);
            for (int note : sequences[sequenceNumber]) {
                model.add(new DisplayNote(note, 4));
            }
            sequenceNumber = (sequenceNumber + 1) % sequences.length;
            if (changeListener != null) {
                changeListener.onChange();
            }
        }
    }

    // Add a major scale to the model except the last note. Returns last note pitch.
    private int addMajorScale(int startNote, boolean ascending, NoteDocument model) {
        int note = startNote;
        model.add(new DisplayNote(note, 4));
        for (int i = 0; i < kMajorScaleSequence.length; ++i) {
            if (ascending) {
                note += kMajorScaleSequence[i];
            } else {
                note -= kMajorScaleSequence[kMajorScaleSequence.length - 1 - i ];
            }
            if (i == kMajorScaleSequence.length - 1)
                break;
            model.add(new DisplayNote(note, 4));
        }
        return note;
    }

    // Add a minor scale based on the last method
    private int addMinorScale(int startNote, boolean ascending, NoteDocument model) {
        int note = startNote;
        model.add(new DisplayNote(note, 4));
        for (int i = 0; i < kMinorScaleSequence.length; ++i) {
            if (ascending) {
                note += kMinorScaleSequence[i];
            } else {
                note -= kMinorScaleSequence[kMinorScaleSequence.length - 1 - i ];
            }
            if (i == kMinorScaleSequence.length - 1)
                break;
            model.add(new DisplayNote(note, 4));
        }
        return note;
    }

    // Add a random sequence in a particular Major scale to the model.
    private void addRandomMajorSequence(int baseNote,
                                        NoteDocument model,
                                        int count) {
       while (baseNote > Note.a)
            baseNote -= 12;
        int seq[] = new int[kMajorScaleSequence.length + 1];
        seq[0] = baseNote;
        for (int i = 0; i < kMajorScaleSequence.length; ++i) {
            seq[i+1] = seq[i] + kMajorScaleSequence[i];
        }
        seq[seq.length-1] = baseNote + 12;
        int previousIndex = -1;
        int randomIndex;
        for (int i = 0; i < count; ++i) {
            do {
                // Don't do the same note twice in a row.
                randomIndex = (int) Math.round((seq.length-1)* Math.random());
            } while (randomIndex == previousIndex);
            previousIndex = randomIndex;
            model.add(new DisplayNote(seq[randomIndex], 4));
        }
    }

    // Add a random sequence in a particular Major scale to the model.
    private void addRandomMinorSequence(int baseNote,
                                        NoteDocument model,
                                        int count) {
        while (baseNote > Note.a)
            baseNote -= 12;
        int seq[] = new int[kMinorScaleSequence.length + 1];
        seq[0] = baseNote;
        for (int i = 0; i < kMinorScaleSequence.length; ++i) {
            seq[i+1] = seq[i] + kMinorScaleSequence[i];
        }
        seq[seq.length-1] = baseNote + 12;
        int previousIndex = -1;
        int randomIndex;
        for (int i = 0; i < count; ++i) {
            do {
                // Don't do the same note twice in a row.
                randomIndex = (int) Math.round((seq.length-1)* Math.random());
            } while (randomIndex == previousIndex);
            previousIndex = randomIndex;
            model.add(new DisplayNote(seq[randomIndex], 4));
        }
    }

    private void addAscDescMajorScale(int startNote, int octaves, NoteDocument model) {
        for (int octave = 0; octave < octaves; ++octave) {
            startNote = addMajorScale(startNote, true, model);
        }
        model.add(new DisplayNote(startNote, 4));
        for (int octave = 0; octave < octaves; ++octave) {
            startNote = addMajorScale(startNote, false, model);
        }
        model.add(new DisplayNote(startNote, 4));
    }

    private void addAscDescMinorScale(int startNote, int octaves, NoteDocument model) {
        for (int octave = 0; octave < octaves; ++octave) {
            startNote = addMinorScale(startNote, true, model);
        }
        model.add(new DisplayNote(startNote, 4));
        for (int octave = 0; octave < octaves; ++octave) {
            startNote = addMinorScale(startNote, false, model);
        }
        model.add(new DisplayNote(startNote, 4));
    }

    @Override
    public void onClick(View view) {
        switch (state) {
            case BASE_OCTAVE:
                state = State.HIGH_OCTAVE;
                changeOctave.setText(getContext().getResources().getString(R.string.high_octave));
                break;
            case HIGH_OCTAVE:
                state = State.TWO_OCTAVE;
                changeOctave.setText(getContext().getResources().getString(R.string.tow_octave));
                break;
            case TWO_OCTAVE:
                state = State.BASE_OCTAVE;
                changeOctave.setText(getContext().getResources().getString(R.string.bass_octave));
                break;
        }
        changeScale();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Spinner spinner = (Spinner) adapterView;
        if (spinner.getId() == R.id.baseNoteSpinner){
            switch (i){
                case 0:
                    baseNote = Note.C;
                    wantsFlat = false;
                    break;
                case 1:
                    baseNote = Note.G;
                    wantsFlat = false;
                    break;
                case 2:
                    baseNote = Note.D;
                    wantsFlat = false;
                    break;
                case 3:
                    baseNote = Note.A;
                    wantsFlat = false;
                    break;
                case 4:
                    baseNote = Note.E;
                    wantsFlat = false;
                    break;
                case 5:
                    baseNote = Note.A_b;
                    wantsFlat = true;
                    break;
                case 6:
                    baseNote = Note.E_b;
                    wantsFlat = true;
                    break;
                case 7:
                    baseNote = Note.F;
                    wantsFlat = true;
                    break;
                case 8:
                    baseNote = Note.B_b;
                    wantsFlat = true;
                    break;
            }
        }
        if (spinner.getId() == R.id.scaleSelectSpinner){
            scaleSpinnerPosition = i;
        }
        changeScale();
    }

    private void changeScale(){
        if (model == null)
            return;
        model.setFlat(wantsFlat);
        model.clear();
        switch (scaleSpinnerPosition){
            case 0:
                if (randomTune.isChecked()) {
                    if (state == State.BASE_OCTAVE) {
                        addRandomMajorSequence(baseNote, model, 16);
                    } else {
                        addRandomMajorSequence(baseNote + 12, model, 16);
                    }
                } else {
                    switch (state) {
                        case BASE_OCTAVE:
                            addAscDescMajorScale(baseNote, 1, model);
                            break;
                        case HIGH_OCTAVE:
                            addAscDescMajorScale(baseNote + 12, 1, model);
                            break;
                        case TWO_OCTAVE:
                            addAscDescMajorScale(baseNote, 2, model);
                            break;
                    }
                }
            break;
            case 1:
                if (randomTune.isChecked()) {
                    if (state == State.BASE_OCTAVE) {
                        addRandomMinorSequence(baseNote, model, 16);
                    } else {
                        addRandomMinorSequence(baseNote + 12, model, 16);
                    }
                } else {
                    switch (state) {
                        case BASE_OCTAVE:
                            addAscDescMinorScale(baseNote, 1, model);
                            break;
                        case HIGH_OCTAVE:
                            addAscDescMinorScale(baseNote + 12, 1, model);
                            break;
                        case TWO_OCTAVE:
                            addAscDescMinorScale(baseNote, 2, model);
                            break;
                    }
                }
            break;
        }
        if (changeListener != null) {
            changeListener.onChange();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
