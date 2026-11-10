
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {

    private final int BG = Color.rgb(16, 29, 43);
    private final int PANEL = Color.rgb(27, 45, 61);
    private final int TEAL = Color.rgb(72, 230, 192);
    private final int WHITE = Color.WHITE;
    private final int MUTED = Color.rgb(183, 202, 211);
    private final int YELLOW = Color.rgb(255, 210, 90);

    private LinearLayout root;
    private LinearLayout content;

    private String currentScreen = "Home";
    private String selectedLanguage = "Spanish";
    private String recognizedText = "";

    private int xp = 0;
    private int streak = 1;
    private int completedLessons = 0;
    private int selectedLesson = 0;

    private boolean quizAnswered = false;

    private TextToSpeech speech;
    private SharedPreferences preferences;

    private static final int SPEECH_REQUEST = 101;

    private Set<String> completedLessonKeys = new HashSet<>();

    // Each lesson contains an English phrase and its translation.
    private final String[][] courseData = {
            {"Hello", "Thank you", "Goodbye"},
            {"Hola", "Gracias", "Adiós"},
            {"Bonjour", "Merci", "Au revoir"},
            {"Hallo", "Danke", "Auf Wiedersehen"},
            {"Olá", "Obrigado", "Adeus"},
            {"Lumela", "Kea leboha", "Sala hantle"},
            {"Sawubona", "Ngiyabonga", "Hamba kahle"},
            {"Habari", "Asante", "Kwaheri"},
            {"مرحبا", "شكرا", "مع السلامة"},
            {"こんにちは", "ありがとう", "さようなら"},
            {"안녕하세요", "감사합니다", "안녕히 가세요"},
            {"你好", "谢谢", "再见"},
            {"नमस्ते", "धन्यवाद", "अलविदा"}
    };

    private final String[] lessonTitles = {
            "Greetings",
            "Showing gratitude",
            "Saying goodbye"
    };

    private final String[] lessonDescriptions = {
            "Learn how to greet someone.",
            "Learn how to say thank you.",
            "Learn how to say goodbye."
    };

    private final String[] englishPhrases = {
            "Hello",
            "Thank you",
            "Goodbye"
    };

    private final String[] languageNames = {
            "English",
            "Spanish",
            "French",
            "German",
            "Portuguese",
            "Sesotho",
            "isiZulu",
            "Swahili",
            "Arabic",
            "Japanese",
            "Korean",
            "Chinese (Mandarin)",
            "Hindi"
    };

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

    // --------------------------------
    // PERMANENT PROGRESS
    // --------------------------------

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

        Set<String> saved = preferences.getStringSet(
                "completed_lesson_keys", new HashSet<>()
        );

        completedLessonKeys = new HashSet<>(saved);
    }

    private void saveProgress() {

        preferences.edit()
                .putInt("xp", xp)
                .putInt("streak", streak)
                .putInt("completed_lessons", completedLessons)
                .putString("selected_language", selectedLanguage)
                .putString("recognized_text", recognizedText)
                .putStringSet(
                        "completed_lesson_keys",
                        new HashSet<>(completedLessonKeys)
                )
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

    private String lessonKey(int index) {
        return selectedLanguage + "_" + index;
    }

    private boolean isLessonComplete(int index) {
        return completedLessonKeys.contains(lessonKey(index));
    }

    private void completeLesson(int index) {

        String key = lessonKey(index);

        if (!completedLessonKeys.contains(key)) {

            completedLessonKeys.add(key);
            completedLessons++;
            addXP(10);

            Toast.makeText(
                    this,
                    "Lesson completed! +10 XP",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            Toast.makeText(
                    this,
                    "You have already completed this lesson.",
                    Toast.LENGTH_SHORT
            ).show();
        }

        saveProgress();
        showScreen("Learn");
    }

    // --------------------------------
    // LANGUAGE COURSE DATA
    // --------------------------------

    private int getLanguageIndex() {

        for (int i = 0; i < languageNames.length; i++) {
            if (languageNames[i].equals(selectedLanguage)) {
                return i;
            }
        }

        return 1;
    }

    private String getTranslation(int lessonIndex) {

        int languageIndex = getLanguageIndex();

        return courseData[languageIndex][lessonIndex];
    }

    private String getLanguageNote() {

        if (selectedLanguage.equals("English")) {
            return "You are learning useful English phrases.";
        }

        return "Starter vocabulary for " + selectedLanguage + ".";
    }

    // --------------------------------
    // SCREEN SYSTEM
    // --------------------------------

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
        } else if (screen.equals("Quiz")) {
            showQuiz();
        } else if (screen.equals("Translate")) {
            showTranslate();
        }
    }

    // --------------------------------
    // HOME
    // --------------------------------

    private void showHome() {

        addText(
                "HELLO, LANGUAGE EXPLORER",
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
                "Beginner • Starter course", 13, MUTED, false);

        Button continueButton = makeButton(
                "CONTINUE LEARNING"
        );

        course.addView(continueButton);

        continueButton.setOnClickListener(v ->
                showScreen("Learn"));

        content.addView(course);

        addSpace(15);

        addText("YOUR LEARNING JOURNEY", 15, WHITE, true);

        LinearLayout stats = card();

        addTextTo(stats,
                xp + " XP earned", 21, TEAL, true);

        addTextTo(stats,
                "🔥 " + streak + " day streak",
                14, MUTED, false);

        addTextTo(stats,
                completedLessons + " lessons completed",
                14, MUTED, false);

        content.addView(stats);

        addSpace(12);

        addFeature(
                "📚", "Learn languages",
                "Structured lessons and vocabulary",
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
    }

    // --------------------------------
    // LEARN / COURSE LIST
    // --------------------------------

    private void showLearn() {

        addText(
                "YOUR LEARNING COURSE",
                22, WHITE, true
        );

        addText(
                selectedLanguage + " • Beginner level",
                14, TEAL, true
        );

        addText(
                getLanguageNote(),
                13, MUTED, false
        );

        addSpace(15);

        LinearLayout summary = card();

        addTextTo(summary,
                "COURSE PROGRESS", 11, TEAL, true);

        addTextTo(summary,
                getCompletedInCurrentCourse()
                        + " of 3 lessons completed",
                19, WHITE, true);

        addTextTo(summary,
                "Complete lessons and earn 10 XP each.",
                12, MUTED, false);

        content.addView(summary);

        addSpace(12);

        addText("LESSON PATH", 16, WHITE, true);

        for (int i = 0; i < lessonTitles.length; i++) {

            final int index = i;

            LinearLayout item = card();

            String status = isLessonComplete(i)
                    ? "✓ COMPLETED"
                    : "LESSON " + (i + 1);

            addTextTo(item,
                    status, 10,
                    isLessonComplete(i) ? TEAL : YELLOW,
                    true);

            addTextTo(item,
                    lessonTitles[i], 19, WHITE, true);

            addTextTo(item,
                    lessonDescriptions[i], 13, MUTED, false);

            Button open = makeButton(
                    isLessonComplete(i)
                            ? "REVIEW LESSON"
                            : "START LESSON"
            );

            item.addView(open);

            open.setOnClickListener(v -> {
                selectedLesson = index;
                showScreen("Lesson");
            });

            content.addView(item);
        }

        addSpace(12);

        Button change = makeButton("CHANGE LANGUAGE");
        content.addView(change);

        change.setOnClickListener(v ->
                showScreen("Languages"));
    }

    private int getCompletedInCurrentCourse() {

        int count = 0;

        for (int i = 0; i < 3; i++) {
            if (isLessonComplete(i)) {
                count++;
            }
        }

        return count;
    }

    // --------------------------------
    // LANGUAGE SELECTION
    // --------------------------------

    private void showLanguages() {

        addText(
                "CHOOSE YOUR LANGUAGE",
                22, WHITE, true
        );

        addText(
                "Choose the language you want to practise.",
                14, MUTED, false
        );

        addSpace(12);

        for (String language : languageNames) {

            LinearLayout item = card();

            addTextTo(item,
                    language, 18, WHITE, true);

            addTextTo(item,
                    language.equals(selectedLanguage)
                            ? "CURRENT LANGUAGE"
                            : "SELECT THIS LANGUAGE",
                    10, TEAL, true);

            item.setOnClickListener(v -> {

                selectedLanguage = language;
                saveProgress();

                Toast.makeText(
                        this,
                        language + " selected",
                        Toast.LENGTH_SHORT
                ).show();

                showScreen("Learn");
            });

            content.addView(item);
        }
    }

    // --------------------------------
    // LESSON DETAILS
    // --------------------------------

    private void showLesson() {

        String phrase = getTranslation(selectedLesson);

        addText(
                "LESSON " + (selectedLesson + 1)
                        + " OF 3",
                11, TEAL, true
        );

        addText(
                lessonTitles[selectedLesson],
                25, WHITE, true
        );

        addText(
                selectedLanguage + " • Beginner",
                14, MUTED, false
        );

        addSpace(15);

        LinearLayout panel = card();

        addTextTo(panel,
                "ENGLISH PHRASE", 11, TEAL, true);

        addTextTo(panel,
                englishPhrases[selectedLesson],
                24, WHITE, true);

        addTextTo(panel,
                "YOUR " + selectedLanguage + " PHRASE",
                11, TEAL, true);

        addTextTo(panel,
                phrase, 30, WHITE, true);

        addTextTo(panel,
                "Listen carefully and practise saying the phrase.",
                13, MUTED, false);

        Button listen = makeButton("🔊 LISTEN");
        panel.addView(listen);

        listen.setOnClickListener(v -> speak(phrase));

        content.addView(panel);

        addSpace(12);

        addText("LEARNING TIP", 15, TEAL, true);

        addText(
                lessonDescriptions[selectedLesson]
                        + " Repeat the phrase several times.",
                13, MUTED, false
        );

        addSpace(10);

        Button practise = makeButton("🎤 PRACTISE SPEAKING");
        content.addView(practise);

        practise.setOnClickListener(v ->
                startSpeaking());

        Button quiz = makeButton("TAKE LESSON QUIZ");
        content.addView(quiz);

        quiz.setOnClickListener(v -> {
            quizAnswered = false;
            showScreen("Quiz");
        });

        Button back = makeButton("BACK TO LESSON PATH");
        content.addView(back);

        back.setOnClickListener(v ->
                showScreen("Learn"));
    }

    // --------------------------------
    // LESSON QUIZ
    // --------------------------------

    private void showQuiz() {

        String correctAnswer = getTranslation(selectedLesson);

        addText(
                "QUICK KNOWLEDGE CHECK",
                12, TEAL, true
        );

        addText(
                "What is the " + selectedLanguage
                        + " phrase for:",
                16, MUTED, false
        );

        addText(
                englishPhrases[selectedLesson],
                26, WHITE, true
        );

        addSpace(15);

        addText(
                "CHOOSE THE CORRECT ANSWER",
                11, TEAL, true
        );

        for (int i = 0; i < 3; i++) {

            final int answerIndex = i;

            String option = getTranslation(i);

            Button answer = makeButton(option);

            content.addView(answer);

            answer.setOnClickListener(v -> {

                if (quizAnswered) {
                    return;
                }

                if (option.equals(correctAnswer)) {

                    quizAnswered = true;

                    completeLesson(selectedLesson);

                } else {

                    Toast.makeText(
                            this,
                            "Not quite. Try another answer.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }

        addSpace(12);

        addText(
                "A correct answer completes this lesson "
                        + "and awards 10 XP.",
                12, MUTED, false
        );

        Button back = makeButton("BACK TO LESSON");
        content.addView(back);

        back.setOnClickListener(v ->
                showScreen("Lesson"));
    }

    // --------------------------------
    // SPEAKING PRACTICE
    // --------------------------------

    private void showSpeak() {

        addText(
                "SPEAKING PRACTICE",
                23, WHITE, true
        );

        addText(
                "Build confidence one conversation at a time.",
                14, MUTED, false
        );

        addSpace(15);

        LinearLayout panel = card();

        addTextTo(panel,
                "TODAY'S PHRASE", 11, TEAL, true);

        addTextTo(panel,
                getTranslation(0), 27, WHITE, true);

        addTextTo(panel,
                "English: Hello",
                14, MUTED, false);

        Button listen = makeButton("🔊 HEAR EXAMPLE");
        panel.addView(listen);

        listen.setOnClickListener(v ->
                speak(getTranslation(0)));

        Button record = makeButton("🎤 START SPEAKING");
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

    // --------------------------------
    // TRANSLATE / PHRASEBOOK
    // --------------------------------

    private void showTranslate() {

        addText(
                "CONVERSATION BRIDGE",
                22, WHITE, true
        );

        addText(
                "Explore useful phrases between languages.",
                14, MUTED, false
        );

        addSpace(12);

        for (int i = 0; i < 3; i++) {

            final int index = i;

            LinearLayout panel = card();

            addTextTo(panel,
                    "ENGLISH", 10, TEAL, true);

            addTextTo(panel,
                    englishPhrases[i], 20, WHITE, true);

            addTextTo(panel,
                    selectedLanguage.toUpperCase(Locale.ROOT),
                    10, TEAL, true);

            addTextTo(panel,
                    getTranslation(i), 23, WHITE, true);

            Button listen = makeButton("🔊 LISTEN");
            panel.addView(listen);

            listen.setOnClickListener(v ->
                    speak(getTranslation(index)));

            content.addView(panel);
        }

        addText(
                "This is an introductory phrasebook. "
                        + "Live automatic translation is not enabled yet.",
                12, MUTED, false
        );

        Button change = makeButton("CHANGE LANGUAGE");
        content.addView(change);

        change.setOnClickListener(v ->
                showScreen("Languages"));
    }

    // --------------------------------
    // CHALLENGES
    // --------------------------------

    private void showChallenges() {

        addText(
                "DAILY CHALLENGES",
                23, WHITE, true
        );

        addText(
                "Small steps. Big progress.",
                14, MUTED, false
        );

        addSpace(12);

        addChallenge(
                "📖", "Review a lesson",
                "Return to your lesson path", 10
        );

        addChallenge(
                "🎤", "Speak one phrase",
                "Practise using your voice", 5
        );

        addChallenge(
                "🌍", "Explore your phrasebook",
                "Discover useful expressions", 5
        );

        addSpace(12);

        addText(
                "Your current XP: " + xp,
                20, TEAL, true
        );
    }

    private void addChallenge(
            String emoji,
            String title,
            String description,
            int points) {

        LinearLayout item = card();

        addTextTo(item,
                emoji + "  " + title, 17, WHITE, true);

        addTextTo(item,
                description, 12, MUTED, false);

        Button done = makeButton(
                "CLAIM  +" + points + " XP"
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

    // --------------------------------
    // PROFILE
    // --------------------------------

    private void showProfile() {

        addText(
                "YOUR PROFILE",
                23, WHITE, true
        );

        addText(
                "Your learning journey.",
                14, MUTED, false
        );

        addSpace(12);

        LinearLayout panel = card();

        addTextTo(panel,
                "🌍 SpeakBridge Learner",
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

        addSpace(12);

        Button change = makeButton("CHANGE LANGUAGE");
        content.addView(change);

        change.setOnClickListener(v ->
                showScreen("Languages"));

        Button home = makeButton("BACK TO HOME");
        content.addView(home);

        home.setOnClickListener(v ->
                showScreen("Home"));

        addText(
                "Your progress is saved on this device.",
                12, MUTED, false
        );
    }

    // --------------------------------
    // REUSABLE UI
    // --------------------------------

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

    // --------------------------------
    // VOICE AND SPEECH
    // --------------------------------

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

    // --------------------------------
    // ANDROID HELPERS
    // --------------------------------

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
