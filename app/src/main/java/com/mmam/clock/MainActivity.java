package com.mmam.clock;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private FrameLayout rootLayout;
    private LinearLayout clockContainer;
    private TextView textClockTime;
    private TextView textClockDay;
    private TextView textClockDate;
    private TextView textClockSeparator;

    private Typeface typefaceBold;
    private Typeface typefaceMedium;

    private SimpleDateFormat timeFormat24;
    private SimpleDateFormat timeFormat12;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat dayFormat;
    private final Date reusableDate = new Date();
    private String lastDayString = "";
    private String lastDateString = "";
    private int lastShiftMinute = -1;

    private ScaleGestureDetector scaleGestureDetector;
    private float currentScale = 0.75f;
    private SharedPreferences preferences;

    private int lastLayoutWidth = 0;
    private int lastLayoutHeight = 0;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            updateClock();
            long now = System.currentTimeMillis();
            long delay = 1000 - (now % 1000);
            handler.postDelayed(this, delay);
        }
    };

    private final BroadcastReceiver timeChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            initFormatters();
            if (lastLayoutWidth > 0 && lastLayoutHeight > 0) {
                updateResponsiveSizes(lastLayoutWidth, lastLayoutHeight);
            }
            updateClock();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences("clock_prefs", MODE_PRIVATE);
        currentScale = preferences.getFloat("scale", 0.75f);
        if (currentScale < 0.40f) {
            currentScale = 0.40f;
        } else if (currentScale > 1.0f) {
            currentScale = 1.0f;
        }

        rootLayout = findViewById(R.id.root_layout);
        clockContainer = findViewById(R.id.clock_container);
        textClockTime = findViewById(R.id.text_clock_time);
        textClockDay = findViewById(R.id.text_clock_day);
        textClockDate = findViewById(R.id.text_clock_date);
        textClockSeparator = findViewById(R.id.text_clock_separator);

        typefaceBold = Typeface.createFromAsset(getAssets(), "fonts/GoogleSans-Bold.ttf");
        typefaceMedium = Typeface.createFromAsset(getAssets(), "fonts/GoogleSans-Medium.ttf");

        textClockTime.setTypeface(typefaceBold);
        textClockDay.setTypeface(typefaceMedium);
        textClockDate.setTypeface(typefaceMedium);
        textClockSeparator.setTypeface(typefaceMedium);

        initFormatters();

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scaleGestureDetector.onTouchEvent(event);
                return true;
            }
        });

        rootLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int w = right - left;
                int h = bottom - top;
                if (w > 0 && h > 0 && (w != lastLayoutWidth || h != lastLayoutHeight)) {
                    lastLayoutWidth = w;
                    lastLayoutHeight = h;
                    updateResponsiveSizes(w, h);
                }
            }
        });
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            currentScale *= detector.getScaleFactor();
            if (currentScale < 0.40f) {
                currentScale = 0.40f;
            } else if (currentScale > 1.0f) {
                currentScale = 1.0f;
            }
            applyScale();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            preferences.edit().putFloat("scale", currentScale).apply();
        }
    }

    private void applyScale() {
        if (clockContainer != null) {
            clockContainer.setScaleX(currentScale);
            clockContainer.setScaleY(currentScale);
        }
    }

    private void updateResponsiveSizes(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        boolean is24 = DateFormat.is24HourFormat(this);
        String sampleTime = is24 ? "00:00:00" : "00:00:00 PM";
        String sampleDate = "Wednesday  •  28/12/2026";

        Paint timePaint = new Paint();
        timePaint.setTypeface(typefaceBold);
        timePaint.setTextSize(100f);
        float timeW = timePaint.measureText(sampleTime);
        Paint.FontMetrics tfm = timePaint.getFontMetrics();
        float timeH = tfm.descent - tfm.ascent;

        float dateRatio = 0.28f;
        Paint datePaint = new Paint();
        datePaint.setTypeface(typefaceMedium);
        datePaint.setTextSize(100f * dateRatio);
        float dateW = datePaint.measureText(sampleDate);
        Paint.FontMetrics dfm = datePaint.getFontMetrics();
        float dateH = dfm.descent - dfm.ascent;

        float density = getResources().getDisplayMetrics().density;
        float margin = 10f * density;
        float gap = 14f * density;

        float totalW = Math.max(timeW, dateW);
        float totalH = timeH + gap + dateH;

        float scaleW = (width - 2 * margin) / totalW;
        float scaleH = (height - 2 * margin) / totalH;
        float baseScale = Math.min(scaleW, scaleH);

        float timeSize = 100f * baseScale;
        float dateSize = timeSize * dateRatio;

        textClockTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, timeSize);
        textClockDay.setTextSize(TypedValue.COMPLEX_UNIT_PX, dateSize);
        textClockSeparator.setTextSize(TypedValue.COMPLEX_UNIT_PX, dateSize);
        textClockDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, dateSize);

        applyScale();
    }

    private void initFormatters() {
        Locale locale = Locale.getDefault();
        timeFormat24 = new SimpleDateFormat("HH:mm:ss", locale);
        timeFormat12 = new SimpleDateFormat("hh:mm:ss a", locale);
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", locale);
        dayFormat = new SimpleDateFormat("EEEE", locale);
        lastDayString = "";
        lastDateString = "";
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        registerReceiver(timeChangeReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemBars();
        handler.removeCallbacks(clockRunnable);
        handler.post(clockRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(clockRunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(timeChangeReceiver);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemBars();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    private void updateClock() {
        long nowMs = System.currentTimeMillis();
        reusableDate.setTime(nowMs);
        boolean is24Hour = DateFormat.is24HourFormat(this);
        SimpleDateFormat timeFmt = is24Hour ? timeFormat24 : timeFormat12;

        textClockTime.setText(timeFmt.format(reusableDate));

        String currentDay = dayFormat.format(reusableDate);
        if (!currentDay.equals(lastDayString)) {
            lastDayString = currentDay;
            textClockDay.setText(currentDay);
        }

        String currentDate = dateFormat.format(reusableDate);
        if (!currentDate.equals(lastDateString)) {
            lastDateString = currentDate;
            textClockDate.setText(currentDate);
        }

        int minute = (int) ((nowMs / 60000L) % 60L);
        if (minute != lastShiftMinute) {
            lastShiftMinute = minute;
            applyBurnIn(minute);
        }
    }

    private void applyBurnIn(int minute) {
        float density = getResources().getDisplayMetrics().density;
        float radiusPx = 8f * density;
        double angle = Math.toRadians((minute % 12) * 30.0);
        float shiftX = (float) (radiusPx * Math.cos(angle));
        float shiftY = (float) (radiusPx * Math.sin(angle));

        if (clockContainer != null) {
            clockContainer.animate()
                    .translationX(shiftX)
                    .translationY(shiftY)
                    .setDuration(400)
                    .start();
        }

        int[] colors = new int[]{0xFFFFFFFF, 0xFFEDEDED, 0xFFE2E2E2, 0xFFEDEDED};
        int color = colors[minute % colors.length];
        textClockTime.setTextColor(color);
        textClockDay.setTextColor(color);
        textClockSeparator.setTextColor(color);
        textClockDate.setTextColor(color);
    }

    private void hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }
}
