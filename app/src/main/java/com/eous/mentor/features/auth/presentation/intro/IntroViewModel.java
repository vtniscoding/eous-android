package com.eous.mentor.features.auth.presentation.intro;

import android.os.Handler;
import android.os.Looper;
import androidx.compose.runtime.MutableState;
import androidx.compose.runtime.SnapshotStateKt;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.TileMode;
import androidx.lifecycle.ViewModel;
import com.eous.mentor.core.theme.ColorKt;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class IntroViewModel extends ViewModel {
    private final MutableState<IntroState> state;
    private Timer autoAdvanceTimer;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public IntroViewModel() {
        List<Slide> initialSlides = Arrays.asList(
            new Slide(
                "Your Personal AI Mentor",
                "Works 24/7 with customized, step-by-step explanations tailored just for you.",
                Brush.Companion.horizontalGradient(
                    Arrays.asList(ColorKt.getEousPurple(), ColorKt.getEousIndigo()),
                    0.0f,
                    Float.POSITIVE_INFINITY,
                    TileMode.Companion.getClamp()
                )
            ),
            new Slide(
                "Tailored to Your Level",
                "Whether you are in Middle School, High School, or University, Eous adapts to you.",
                Brush.Companion.horizontalGradient(
                    Arrays.asList(ColorKt.getEousBlue(), ColorKt.getEousIndigo()),
                    0.0f,
                    Float.POSITIVE_INFINITY,
                    TileMode.Companion.getClamp()
                )
            ),
            new Slide(
                "Active Recall Quizzes",
                "Test your skills with interactive flashcards and gamified subject tracking.",
                Brush.Companion.horizontalGradient(
                    Arrays.asList(ColorKt.getEousPink(), ColorKt.getEousRed()),
                    0.0f,
                    Float.POSITIVE_INFINITY,
                    TileMode.Companion.getClamp()
                )
            )
        );

        this.state = SnapshotStateKt.mutableStateOf(
            new IntroState(0, initialSlides),
            SnapshotStateKt.structuralEqualityPolicy()
        );
        startAutoAdvance();
    }

    public MutableState<IntroState> getState() {
        return state;
    }

    private void startAutoAdvance() {
        autoAdvanceTimer = new Timer();
        autoAdvanceTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mainHandler.post(() -> {
                    IntroState current = state.getValue();
                    List<Slide> slides = current.getSlides();
                    if (!slides.isEmpty()) {
                        int nextIndex = (current.getActiveSlideIndex() + 1) % slides.size();
                        state.setValue(new IntroState(nextIndex, slides));
                    }
                });
            }
        }, 4000, 4000);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (autoAdvanceTimer != null) {
            autoAdvanceTimer.cancel();
        }
    }
}
