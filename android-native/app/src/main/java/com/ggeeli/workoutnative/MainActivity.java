package com.ggeeli.workoutnative;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends android.app.Activity {
    private final Set<String> selectedTypes = new LinkedHashSet<>();
    private Map<String, String> presets;
    private String activePresetType;
    private boolean renderingNotes;

    private GridLayout typeGrid;
    private NumberPicker durationPicker;
    private EditText notesInput;
    private TextView presetLabel;
    private TextView todayTitle;
    private LinearLayout workoutList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        presets = WorkoutStore.loadPresets(this);
        typeGrid = findViewById(R.id.typeGrid);
        durationPicker = findViewById(R.id.durationPicker);
        notesInput = findViewById(R.id.notesInput);
        presetLabel = findViewById(R.id.presetLabel);
        todayTitle = findViewById(R.id.todayTitle);
        workoutList = findViewById(R.id.workoutList);

        setupDurationPicker();
        setupTypeButtons();
        setupNotesAutosave();
        findViewById(R.id.addWorkoutButton).setOnClickListener(view -> addWorkout());

        renderPresetEditor();
        renderWorkoutList();
    }

    private void setupDurationPicker() {
        String[] values = new String[34];
        for (int index = 0; index < values.length; index++) {
            values[index] = String.valueOf(15 + index * 5);
        }
        durationPicker.setMinValue(0);
        durationPicker.setMaxValue(values.length - 1);
        durationPicker.setDisplayedValues(values);
        durationPicker.setValue(6);
        durationPicker.setWrapSelectorWheel(false);
    }

    private void setupTypeButtons() {
        typeGrid.removeAllViews();
        for (String type : WorkoutStore.TYPES) {
            Button button = new Button(this);
            button.setText(type);
            button.setAllCaps(false);
            button.setTextColor(getColor(R.color.text));
            button.setBackgroundResource(R.drawable.type_button);
            button.setSelected(false);
            button.setOnClickListener(view -> toggleType(type, button));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = GridLayout.LayoutParams.WRAP_CONTENT;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.setMargins(0, 0, 8, 8);
            typeGrid.addView(button, params);
        }
    }

    private void setupNotesAutosave() {
        notesInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (renderingNotes || activePresetType == null) {
                    return;
                }
                presets.put(activePresetType, s.toString());
                WorkoutStore.savePresets(MainActivity.this, presets);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void toggleType(String type, Button button) {
        if (selectedTypes.contains(type)) {
            selectedTypes.remove(type);
            button.setSelected(false);
        } else {
            selectedTypes.add(type);
            activePresetType = type;
            button.setSelected(true);
        }

        if (!selectedTypes.contains(activePresetType)) {
            activePresetType = selectedTypes.isEmpty() ? null : new ArrayList<>(selectedTypes).get(selectedTypes.size() - 1);
        }

        renderPresetEditor();
    }

    private void renderPresetEditor() {
        presetLabel.setText(activePresetType == null ? "Editing preset: none selected" : "Editing preset: " + activePresetType);
        renderingNotes = true;
        notesInput.setText(buildSelectedNotes());
        notesInput.post(() -> renderingNotes = false);
    }

    private String buildSelectedNotes() {
        List<String> blocks = new ArrayList<>();
        for (String type : selectedTypes) {
            String notes = presets.get(type);
            if (notes != null && !notes.trim().isEmpty()) {
                blocks.add(notes);
            }
        }
        return String.join("\n\n", blocks);
    }

    private void addWorkout() {
        if (selectedTypes.isEmpty()) {
            presetLabel.setText("Select at least one type before saving.");
            return;
        }

        int duration = 15 + durationPicker.getValue() * 5;
        WorkoutStore.addWorkout(this, new WorkoutStore.WorkoutEntry(
            WorkoutStore.todayKey(),
            new ArrayList<>(selectedTypes),
            duration,
            notesInput.getText().toString().trim()
        ));

        durationPicker.setValue(6);
        renderWorkoutList();
        updateWidget();
    }

    private void renderWorkoutList() {
        todayTitle.setText(WorkoutStore.todayTitle());
        workoutList.removeAllViews();
        List<WorkoutStore.WorkoutEntry> entries = WorkoutStore.loadWorkouts(this);
        String today = WorkoutStore.todayKey();

        int renderedIndex = 0;
        for (int index = 0; index < entries.size(); index++) {
            WorkoutStore.WorkoutEntry entry = entries.get(index);
            if (!today.equals(entry.date)) {
                continue;
            }
            addWorkoutRow(entry, index);
            renderedIndex++;
        }

        if (renderedIndex == 0) {
            TextView empty = new TextView(this);
            empty.setText("No workouts yet today.");
            empty.setTextColor(getColor(R.color.muted));
            empty.setTextSize(14);
            workoutList.addView(empty);
        }
    }

    private void addWorkoutRow(WorkoutStore.WorkoutEntry entry, int position) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setBackgroundResource(R.drawable.entry_background);

        TextView title = new TextView(this);
        title.setText("Workout");
        title.setTextColor(getColor(R.color.text));
        title.setTextSize(17);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);

        TextView meta = new TextView(this);
        meta.setText(entry.typeLabel() + " • " + entry.durationMinutes + " min");
        meta.setTextColor(getColor(R.color.muted));
        meta.setTextSize(13);

        TextView notes = new TextView(this);
        notes.setText(entry.notes);
        notes.setTextColor(getColor(R.color.muted));
        notes.setTextSize(13);
        notes.setPadding(0, 8, 0, 0);

        Button delete = new Button(this);
        delete.setText("Delete");
        delete.setAllCaps(false);
        delete.setTextColor(getColor(R.color.danger));
        delete.setBackgroundResource(R.drawable.danger_button);
        delete.setOnClickListener(view -> {
            WorkoutStore.deleteWorkout(this, position);
            renderWorkoutList();
            updateWidget();
        });

        row.addView(title);
        row.addView(meta);
        if (!entry.notes.isEmpty()) {
            row.addView(notes);
        }
        row.addView(delete);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 10);
        workoutList.addView(row, params);
    }

    private void updateWidget() {
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        ComponentName componentName = new ComponentName(this, WorkoutWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(componentName);
        WorkoutWidgetProvider.updateAll(this, manager, ids);
    }
}