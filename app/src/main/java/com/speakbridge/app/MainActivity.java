
package com.speakbridge.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private final int BG = Color.rgb(16, 29, 43);
    private final int PANEL = Color.rgb(27, 45, 61);
    private final int TEAL = Color.rgb(72, 230, 192);
    private final int WHITE = Color.WHITE;
    private final int MUTED = Color.rgb(183, 202, 211);

    private LinearLayout root;
    private LinearLayout content;

    private String currentScreen = "Home";
    private String selectedLanguage = "Spanish";
    private String recognizedText = "";

    private int xp = 0;
    private int streak = 1;
    private int completedLessons = 0;

    private TextToSpeech speech;
    private SharedPreferences preferences;

    private static final int SPEECH_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(
                "SpeakBridgeProgress",
                MODE_PRIVATE
        );

        loadProgress();
        updateLearningStreak();

        speech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                speech.setLanguage(Locale.getDefault());
            }
        });

        showScreen("Home");
    }

    // ------------------------------
    // PERMANENT PROGRESS STORAGE
    // ------------------------------

    private void loadProgress() {

        xp = preferences.getInt("xp", 0);

        streak = preferences.getInt("streak", 1);

        completedLessons = preferences.getInt(
                "completed_lessons", 0
        );

        selectedLanguage = preferences.getString(
                "selected_language", "Spanish"
        );

        recognizedText = preferences.getString(
                "recognized_text", ""
        );
    }

    private void saveProgress() {

        preferences.edit()
                .putInt("xp", xp)
                .putInt("streak", streak)
                .putInt("completed_lessons", completedLessons)
                .putString("selected_language", selectedLanguage)
                .putString("recognized_text", recognizedText)
                .apply();
    }

    private String getTodayDate() {

        return new SimpleDateFormat(
                "yyyyMMdd", Locale.US
        ).format(new Date());
    }

    private String getYesterdayDate() {

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);

        return new SimpleDateFormat(
                "yyyyMMdd", Locale.US
        ).format(calendar.getTime());
    }

    private void updateLearningStreak() {

        String today = getTodayDate();

        String lastDate = preferences.getString(
                "last_active_date", ""
        );

        if (lastDate.equals(today)) {
            return;
        }

        if (lastDate.equals(getYesterdayDate())) {
            streak++;
        } else {
            streak = 1;
        }

        preferences.edit()
                .putInt("streak", streak)
                .putString("last_active_date", today)
                .apply();
    }

    private void addXP(int points) {

        xp += points;
        saveProgress();
    }

    // ------------------------------
    // SCREEN SYSTEM
    // ------------------------------

    private void showScreen(String screen) {

        currentScreen = screen;

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        TextView header = makeText(
                "SpeakBridge", 25, TEAL, true
        );

        header.setPadding(
                dp(20), dp(18), dp(20), dp(3)
        );

        root.addView(header);

        TextView tagline = makeText(
                "ONE WORLD. EVERY LANGUAGE.",
                10, MUTED, true
        );

        tagline.setPadding(
                dp(20), 0, dp(20), dp(15)
        );

        root.addView(tagline);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);

        content.setPadding(
                dp(18), dp(8), dp(18), dp(20)
        );

        scroll.addView(content);

        root.addView(scroll,
                new LinearLayout.LayoutParams(
                        -1, 0, 1
                ));

        showContent(screen);
        addNavigation();

        setContentView(root);
    }

    private void showContent(String screen) {

        if (screen.equals("Home")) {
            showHome();
        } else if (screen.equals("Learn")) {
            showLearn();
        } else if (screen.equals("Speak")) {
            showSpeak();
        } else if (screen.equals("Challenges")) {
            showChallenges();
        } else if (screen.equals("Profile")) {
            showProfile();
        } else if (screen.equals("Lesson")) {
            showLesson();
        } else if (screen.equals("Translate")) {
            showTranslate();
        }
    }

    // ------------------------------
    // HOME
    // ------------------------------

    private void showHome() {

        addText(
                "HELLO, LANGUAGE EXPLORER 👋",
                12, TEAL, true
        );

        addText(
                "Your next conversation starts here.",
                23, WHITE, true
        );

        addText(
                "Learn a language. Connect with the world.",
                14, MUTED, false
        );

        addSpace(18);

        LinearLayout course = card();

        addTextTo(course,
                "YOUR CURRENT LANGUAGE", 11, TEAL, true);

        addTextTo(course,
                selectedLanguage, 23, WHITE, true);

        addTextTo(course,
                "Beginner • First steps", 13, MUTED, false);

        Button continueButton = makeButton(
                "CONTINUE LEARNING"
        );

        course.addView(continueButton);

        continueButton.setOnClickListener(v ->
                showScreen("Lesson"));

        content.addView(course);

        addSpace(18);

        addText(
                "EXPLORE SPEAKBRIDGE",
                15, WHITE, true
        );

        addSpace(8);

        addFeature(
                "📚", "Learn languages",
                "Vocabulary, grammar and lessons",
                "Learn"
        );

        addFeature(
                "🎤", "Speaking practice",
                "Practise saying words aloud",
                "Speak"
        );

        addFeature(
                "🌍", "Translate & connect",
                "Explore useful conversation phrases",
                "Translate"
        );

        addFeature(
                "🏆", "Daily challenges",
                "Build your skills and earn XP",
                "Challenges"
        );

        addSpace(16);

        LinearLayout progress = card();

        addTextTo(progress,
                "YOUR DAILY PROGRESS", 12, TEAL, true);

        addTextTo(progress,
                xp + " XP earned", 22, WHITE, true);

        addTextTo(progress,
                "🔥 " + streak + " day streak",
                14, MUTED, false);

        addTextTo(progress,
                completedLessons + " lessons completed",
                13, MUTED, false);

        content.addView(progress);
    }

    // ------------------------------
    // LANGUAGE SELECTION
    // ------------------------------

    private void showLearn() {

        addText(
                "CHOOSE YOUR LANGUAGE",
                22, WHITE, true
        );

        addText(
                "Start with a language you want to learn.",
                14, MUTED, false
        );

        addSpace(15);

        String[] languages = {
                "English", "Spanish", "French", "German",
                "Portuguese", "Arabic", "Chinese (Mandarin)",
                "Japanese", "Korean", "Hindi", "Swahili",
                "isiZulu", "Sesotho"
        };

        for (String language : languages) {

            LinearLayout item = card();

            TextView name = makeText(
                    language, 17, WHITE, true
            );

            item.addView(name);

            TextView status = makeText(
                    language.equals(selectedLanguage)
                            ? "CURRENT LANGUAGE"
                            : "SELECT LANGUAGE",
                    10, TEAL, true
            );

            item.addView(status);

            item.setOnClickListener(v -> {

                selectedLanguage = language;
                saveProgress();

                Toast.makeText(
                        this,
                        language + " selected",
                        Toast.LENGTH_SHORT
                ).show();

                showScreen("Lesson");
            });

            content.addView(item);
        }

        addSpace(10);

        addText(
                "More languages and courses will be added as "
                        + "SpeakBridge grows.",
                12, MUTED, false
        );
    }

    // ------------------------------
    // LESSON
    // ------------------------------

    private void showLesson() {

        addText(
                "YOUR FIRST LESSON",
                12, TEAL, true
        );

        addText(
                selectedLanguage,
                25, WHITE, true
        );

        addText(
                "Useful everyday greetings",
                14, MUTED, false
        );

        addSpace(18);

        String phrase = "Hello";
        String meaning = "A friendly greeting.";

        if (selectedLanguage.equals("Spanish")) {
            phrase = "Hola";
            meaning = "Hello";
        } else if (selectedLanguage.equals("French")) {
            phrase = "Bonjour";
            meaning = "Good morning / Hello";
        } else if (selectedLanguage.equals("German")) {
            phrase = "Hallo";
            meaning = "Hello";
        } else if (selectedLanguage.equals("Portuguese")) {
            phrase = "Olá";
            meaning = "Hello";
        } else if (selectedLanguage.equals("Sesotho")) {
            phrase = "Lumela";
            meaning = "Hello";
        } else if (selectedLanguage.equals("isiZulu")) {
            phrase = "Sawubona";
            meaning = "Hello";
        } else if (selectedLanguage.equals("Swahili")) {
            phrase = "Habari";
            meaning = "Hello / How are you?";
        } else if (selectedLanguage.equals("Japanese")) {
            phrase = "こんにちは";
            meaning = "Konnichiwa — Hello";
        } else if (selectedLanguage.equals("Korean")) {
            phrase = "안녕하세요";
            meaning = "Annyeonghaseyo — Hello";
        } else if (selectedLanguage.equals("Arabic")) {
            phrase = "مرحبا";
            meaning = "Marhaba — Hello";
        } else if (selectedLanguage.equals("Chinese (Mandarin)")) {
            phrase = "你好";
            meaning = "Nǐ hǎo — Hello";
        } else if (selectedLanguage.equals("Hindi")) {
            phrase = "नमस्ते";
            meaning = "Namaste — Hello";
        }

        LinearLayout lesson = card();

        addTextTo(lesson,
                "SAY THIS WORD", 11, TEAL, true);

        addTextTo(lesson,
                phrase, 34, WHITE, true);

        addTextTo(lesson,
                meaning, 15, MUTED, false);

        Button listen = makeButton("🔊  LISTEN");
        lesson.addView(listen);

        String finalPhrase = phrase;

        listen.setOnClickListener(v ->
                speak(finalPhrase));

        content.addView(lesson);

        addSpace(16);

        addText("PRACTISE", 16, WHITE, true);

        addText(
                "Say the word aloud and practise your pronunciation.",
                13, MUTED, false
        );

        Button practice = makeButton(
                "🎤  TRY SPEAKING"
        );

        content.addView(practice);

        practice.setOnClickListener(v ->
                startSpeaking());

        Button complete = makeButton(
                "COMPLETE LESSON  +10 XP"
        );

        content.addView(complete);

        complete.setOnClickListener(v -> {

            addXP(10);
            completedLessons++;
            saveProgress();

            Toast.makeText(
                    this,
                    "Lesson completed! +10 XP",
                    Toast.LENGTH_SHORT
            ).show();

            showScreen("Home");
        });
    }

    // ------------------------------
    // SPEAKING PRACTICE
    // ------------------------------

    private void showSpeak() {

        addText(
                "SPEAKING PRACTICE",
                23, WHITE, true
        );

        addText(
                "Build confidence one conversation at a time.",
                14, MUTED, false
        );

        addSpace(20);

        LinearLayout panel = card();

        addTextTo(panel,
                "TODAY'S PRACTICE", 11, TEAL, true);

        addTextTo(panel,
                "Hello! How are you?", 24, WHITE, true);

        addTextTo(panel,
                "Try saying this phrase using your voice.",
                13, MUTED, false);

        Button listen = makeButton(
                "🔊  HEAR EXAMPLE"
        );

        panel.addView(listen);

        listen.setOnClickListener(v ->
                speak("Hello! How are you?"));

        Button record = makeButton(
                "🎤  START SPEAKING"
        );

        panel.addView(record);

        record.setOnClickListener(v ->
                startSpeaking());

        content.addView(panel);

        addSpace(15);

        addText("YOUR RESULT", 14, TEAL, true);

        addText(
                recognizedText.isEmpty()
                        ? "Your recognised words will appear here."
                        : recognizedText,
                16, WHITE, false
        );

        addText(
                "Speech recognition depends on a compatible "
                        + "speech service installed on your device.",
                12, MUTED, false
        );
    }

    // ------------------------------
    // TRANSLATE
    // ------------------------------

    private void showTranslate() {

        addText(
                "CONVERSATION BRIDGE",
                22, WHITE, true
        );

        addText(
                "Explore useful phrases between languages.",
                14, MUTED, false
        );

        addSpace(15);

        LinearLayout panel = card();

        addTextTo(panel,
                "SAMPLE PHRASE", 11, TEAL, true);

        addTextTo(panel,
                "Hello!", 27, WHITE, true);

        addTextTo(panel,
                "English → " + selectedLanguage,
                13, MUTED, false);

        String translated = "Hello";

        if (selectedLanguage.equals("Spanish")) {
            translated = "Hola";
        } else if (selectedLanguage.equals("French")) {
            translated = "Bonjour";
        } else if (selectedLanguage.equals("German")) {
            translated = "Hallo";
        } else if (selectedLanguage.equals("Sesotho")) {
            translated = "Lumela";
        } else if (selectedLanguage.equals("isiZulu")) {
            translated = "Sawubona";
        } else if (selectedLanguage.equals("Swahili")) {
            translated = "Habari";
        }

        addTextTo(panel,
                translated, 25, TEAL, true);

        Button listen = makeButton("🔊  LISTEN");
        panel.addView(listen);

        String finalTranslated = translated;

        listen.setOnClickListener(v ->
                speak(finalTranslated));

        content.addView(panel);

        addSpace(12);

        addText(
                "This is an introductory phrasebook sample, "
                        + "not live automatic translation yet.",
                12, MUTED, false
        );

        Button languages = makeButton(
                "CHANGE LANGUAGE"
        );

        content.addView(languages);

        languages.setOnClickListener(v ->
                showScreen("Learn"));
    }

    // ------------------------------
    // CHALLENGES
    // ------------------------------

    private void showChallenges() {

        addText(
                "DAILY CHALLENGES",
                23, WHITE, true
        );

        addText(
                "Small steps. Big progress.",
                14, MUTED, false
        );

        addSpace(15);

        addChallenge(
                "📖", "Learn 5 new words",
                "Build your vocabulary", 10
        );

        addChallenge(
                "🎤", "Speak one phrase",
                "Practise using your voice", 15
        );

        addChallenge(
                "🌍", "Explore another language",
                "Discover something new", 10
        );

        addSpace(15);

        addText(
                "Your current XP: " + xp,
                20, TEAL, true
        );
    }

    private void addChallenge(
            String icon,
            String title,
            String description,
            int points) {

        LinearLayout item = card();

        addTextTo(item,
                icon + "  " + title, 17, WHITE, true);

        addTextTo(item,
                description, 12, MUTED, false);

        Button done = makeButton(
                "COMPLETE  +" + points + " XP"
        );

        item.addView(done);

        done.setOnClickListener(v -> {

            addXP(points);

            Toast.makeText(
                    this,
                    "+" + points + " XP earned!",
                    Toast.LENGTH_SHORT
            ).show();

            showScreen("Challenges");
        });

        content.addView(item);
    }

    // ------------------------------
    // PROFILE
    // ------------------------------

    private void showProfile() {

        addText(
                "YOUR PROFILE",
                23, WHITE, true
        );

        addText(
                "Your learning journey.",
                14, MUTED, false
        );

        addSpace(15);

        LinearLayout panel = card();

        addTextTo(panel,
                "🌍  SpeakBridge Learner",
                20, WHITE, true);

        addTextTo(panel,
                "Learning: " + selectedLanguage,
                14, MUTED, false);

        addTextTo(panel,
                "Experience: " + xp + " XP",
                14, TEAL, true);

        addTextTo(panel,
                "🔥 Learning streak: " + streak + " day",
                14, MUTED, false);

        addTextTo(panel,
                "Completed lessons: " + completedLessons,
                14, MUTED, false);

        content.addView(panel);

        addSpace(15);

        Button change = makeButton(
                "CHANGE LANGUAGE"
        );

        content.addView(change);

        change.setOnClickListener(v ->
                showScreen("Learn"));

        Button home = makeButton(
                "BACK TO HOME"
        );

        content.addView(home);

        home.setOnClickListener(v ->
                showScreen("Home"));

        addSpace(12);

        addText(
                "Your progress is saved on this device. "
                        + "Cloud accounts and sync can be added later.",
                12, MUTED, false
        );
    }

    // ------------------------------
    // REUSABLE UI
    // ------------------------------

    private void addFeature(
            String emoji,
            String title,
            String description,
            String destination) {

        LinearLayout item = card();

        addTextTo(item,
                emoji + "  " + title, 17, WHITE, true);

        addTextTo(item,
                description, 12, MUTED, false);

        item.setOnClickListener(v ->
                showScreen(destination));

        content.addView(item);
    }

    private void addNavigation() {

        LinearLayout nav = new LinearLayout(this);

        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);

        nav.setPadding(
                dp(4), dp(8), dp(4), dp(8)
        );

        nav.setBackgroundColor(
                Color.rgb(12, 23, 35)
        );

        String[] tabs = {
                "Home", "Learn", "Speak",
                "Challenges", "Profile"
        };

        for (String tab : tabs) {

            TextView button = makeText(
                    tab,
                    10,
                    currentScreen.equals(tab) ? TEAL : MUTED,
                    true
            );

            button.setGravity(Gravity.CENTER);

            button.setPadding(
                    dp(2), dp(12), dp(2), dp(12)
            );

            nav.addView(button,
                    new LinearLayout.LayoutParams(
                            0, -2, 1
                    ));

            button.setOnClickListener(v ->
                    showScreen(tab));
        }

        root.addView(nav);
    }

    private LinearLayout card() {

        LinearLayout panel = new LinearLayout(this);

        panel.setOrientation(LinearLayout.VERTICAL);

        panel.setPadding(
                dp(16), dp(16), dp(16), dp(16)
        );

        GradientDrawable shape = new GradientDrawable();

        shape.setColor(PANEL);
        shape.setCornerRadius(dp(18));

        panel.setBackground(shape);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);

        params.bottomMargin = dp(12);

        panel.setLayoutParams(params);

        return panel;
    }

    private void addTextTo(
            LinearLayout parent,
            String value,
            int size,
            int color,
            boolean bold) {

        TextView view = makeText(
                value, size, color, bold
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);

        params.bottomMargin = dp(7);

        parent.addView(view, params);
    }

    private void addText(
            String value,
            int size,
            int color,
            boolean bold) {

        TextView view = makeText(
                value, size, color, bold
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);

        params.bottomMargin = dp(8);

        content.addView(view, params);
    }

    private TextView makeText(
            String value,
            int size,
            int color,
            boolean bold) {

        TextView view = new TextView(this);

        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);

        if (bold) {
            view.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );
        }

        return view;
    }

    private Button makeButton(String label) {

        Button button = new Button(this);

        button.setText(label);
        button.setTextColor(BG);
        button.setTextSize(12);
        button.setAllCaps(false);

        GradientDrawable shape = new GradientDrawable();

        shape.setColor(TEAL);
        shape.setCornerRadius(dp(13));

        button.setBackground(shape);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, dp(48));

        params.topMargin = dp(7);

        button.setLayoutParams(params);

        return button;
    }

    private void addSpace(int height) {

        View space = new View(this);

        content.addView(space,
                new LinearLayout.LayoutParams(
                        1, dp(height)
                ));
    }

    // ------------------------------
    // VOICE AND SPEECH
    // ------------------------------

    private void speak(String words) {

        if (speech != null) {

            speech.speak(
                    words,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "speakbridge_audio"
            );
        }
    }

    private void startSpeaking() {

        try {

            Intent intent = new Intent(
                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    Locale.getDefault()
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_PROMPT,
                    "Speak now"
            );

            startActivityForResult(
                    intent, SPEECH_REQUEST
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "No compatible speech recognition service found.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode, resultCode, data
        );

        if (requestCode == SPEECH_REQUEST
                && resultCode == RESULT_OK
                && data != null) {

            ArrayList<String> results =
                    data.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS
                    );

            if (results != null && !results.isEmpty()) {

                recognizedText = results.get(0);

                addXP(5);
                saveProgress();

                Toast.makeText(
                        this,
                        "Speaking practice completed! +5 XP",
                        Toast.LENGTH_SHORT
                ).show();

                showScreen("Speak");
            }
        }
    }

    // ------------------------------
    // ANDROID HELPERS
    // ------------------------------

    private int dp(int value) {

        return (int) (
                value *
                getResources().getDisplayMetrics().density
                + 0.5f
        );
    }

    @Override
    protected void onDestroy() {

        if (speech != null) {
            speech.stop();
            speech.shutdown();
        }

        super.onDestroy();
    }
}
