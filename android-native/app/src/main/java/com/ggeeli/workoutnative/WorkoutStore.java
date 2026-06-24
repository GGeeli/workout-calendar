package com.ggeeli.workoutnative;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class WorkoutStore {
    static final String[] TYPES = {"Warm up", "Chest", "Back", "Biceps", "Core", "Legs", "Stretching"};

    private static final String PREFS = "workout_native_store";
    private static final String WORKOUTS = "workouts";
    private static final String PRESETS = "presets";

    private WorkoutStore() {
    }

    static String todayKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    static String todayTitle() {
        return new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(new Date());
    }

    static Map<String, String> defaultPresets() {
        Map<String, String> presets = new LinkedHashMap<>();
        presets.put("Warm up", "Jumping jacks 30/30/30\nArm circles 20/20/20");
        presets.put("Chest", "Bench press 10/10/10\nIncline dumbbell press 12/12/12");
        presets.put("Back", "Pull-ups 10/10/10\nInverted rows 15/15/15");
        presets.put("Biceps", "Barbell curls 10/10/10\nHammer curls 12/12/12");
        presets.put("Core", "Plank 45/45/45\nLeg raises 15/15/15");
        presets.put("Legs", "Squats 10/10/10\nLunges 12/12/12");
        presets.put("Stretching", "Hamstring stretch 30/30/30\nChest stretch 30/30/30");
        return presets;
    }

    static Map<String, String> loadPresets(Context context) {
        Map<String, String> presets = defaultPresets();
        String raw = prefs(context).getString(PRESETS, null);
        if (raw == null) {
            return presets;
        }

        try {
            JSONObject json = new JSONObject(raw);
            for (String type : TYPES) {
                if (json.has(type)) {
                    presets.put(type, json.optString(type, presets.get(type)));
                }
            }
        } catch (JSONException ignored) {
            return presets;
        }

        return presets;
    }

    static void savePresets(Context context, Map<String, String> presets) {
        JSONObject json = new JSONObject();
        try {
            for (Map.Entry<String, String> entry : presets.entrySet()) {
                json.put(entry.getKey(), entry.getValue());
            }
        } catch (JSONException ignored) {
            return;
        }
        prefs(context).edit().putString(PRESETS, json.toString()).apply();
    }

    static List<WorkoutEntry> loadWorkouts(Context context) {
        List<WorkoutEntry> entries = new ArrayList<>();
        String raw = prefs(context).getString(WORKOUTS, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int index = 0; index < array.length(); index++) {
                JSONObject json = array.getJSONObject(index);
                JSONArray typeArray = json.optJSONArray("types");
                List<String> types = new ArrayList<>();
                if (typeArray != null) {
                    for (int typeIndex = 0; typeIndex < typeArray.length(); typeIndex++) {
                        types.add(typeArray.optString(typeIndex));
                    }
                }
                entries.add(new WorkoutEntry(
                    json.optString("date"),
                    types,
                    json.optInt("duration", 45),
                    json.optString("notes")
                ));
            }
        } catch (JSONException ignored) {
            return entries;
        }
        return entries;
    }

    static void addWorkout(Context context, WorkoutEntry entry) {
        List<WorkoutEntry> entries = loadWorkouts(context);
        entries.add(0, entry);
        saveWorkouts(context, entries);
    }

    static void deleteWorkout(Context context, int position) {
        List<WorkoutEntry> entries = loadWorkouts(context);
        if (position < 0 || position >= entries.size()) {
            return;
        }
        entries.remove(position);
        saveWorkouts(context, entries);
    }

    static int todayCount(Context context) {
        String today = todayKey();
        int count = 0;
        for (WorkoutEntry entry : loadWorkouts(context)) {
            if (today.equals(entry.date)) {
                count++;
            }
        }
        return count;
    }

    private static void saveWorkouts(Context context, List<WorkoutEntry> entries) {
        JSONArray array = new JSONArray();
        try {
            for (WorkoutEntry entry : entries) {
                JSONObject json = new JSONObject();
                json.put("date", entry.date);
                json.put("duration", entry.durationMinutes);
                json.put("notes", entry.notes);
                json.put("types", new JSONArray(entry.types));
                array.put(json);
            }
        } catch (JSONException ignored) {
            return;
        }
        prefs(context).edit().putString(WORKOUTS, array.toString()).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static final class WorkoutEntry {
        final String date;
        final List<String> types;
        final int durationMinutes;
        final String notes;

        WorkoutEntry(String date, List<String> types, int durationMinutes, String notes) {
            this.date = date;
            this.types = new ArrayList<>(types == null ? Collections.emptyList() : types);
            this.durationMinutes = durationMinutes;
            this.notes = notes == null ? "" : notes;
        }

        String typeLabel() {
            return types.isEmpty() ? "Workout" : String.join(", ", types);
        }
    }
}