package com.sleepstream.checkkeeper;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.squareup.timessquare.CalendarCellDecorator;
import com.squareup.timessquare.CalendarPickerView;
import com.squareup.timessquare.DefaultDayViewAdapter;

import java.util.*;

public class CalendarPickerActivity extends Activity{

    private static final String LOG_TAG = "CalendarPickerActivity";
    private CalendarPickerView calendar;
    private AlertDialog theDialog;
    private CalendarPickerView dialogView;
    private final Set<Button> modeButtons = new LinkedHashSet<Button>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(MainActivity.settings != null) {
            String themeId = MainActivity.settings.settings.get("theme");
            if (themeId.length() > 0)
                setTheme(Integer.valueOf(themeId));
        }
        super.onCreate(savedInstanceState);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_picker);

        final Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        final Calendar lastYear = Calendar.getInstance();
        lastYear.add(Calendar.YEAR, -1);

        calendar = (CalendarPickerView) findViewById(R.id.calendar_view);
        calendar.init(lastYear.getTime(), nextYear.getTime()) //
                .inMode(CalendarPickerView.SelectionMode.SINGLE) //
                .withSelectedDate(new Date());

        initButtonListeners(nextYear, lastYear);
    }

    private void initButtonListeners(final Calendar nextYear, final Calendar lastYear) {
        final Button single = (Button) findViewById(R.id.button_single);
        final Button multi = (Button) findViewById(R.id.button_multi);
        final Button range = (Button) findViewById(R.id.button_range);
        final Button done = (Button) findViewById(R.id.done_button);

        modeButtons.addAll(Arrays.asList(single, multi, range));

        single.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButtonsEnabled(single);

                calendar.setCustomDayView(new DefaultDayViewAdapter());
                calendar.setDecorators(Collections.<CalendarCellDecorator>emptyList());
                calendar.init(lastYear.getTime(), nextYear.getTime()) //
                        .inMode(CalendarPickerView.SelectionMode.SINGLE) //
                        .withSelectedDate(new Date());
            }
        });


        multi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButtonsEnabled(multi);

                calendar.setCustomDayView(new DefaultDayViewAdapter());
                /*Calendar today = Calendar.getInstance();
                ArrayList<Date> dates = new ArrayList<Date>();
                for (int i = 0; i < 5; i++) {
                    today.add(Calendar.DAY_OF_MONTH, 3);
                    dates.add(today.getTime());
                }*/
                calendar.setDecorators(Collections.<CalendarCellDecorator>emptyList());
                calendar.init(lastYear.getTime(), nextYear.getTime()) //
                        .inMode(CalendarPickerView.SelectionMode.MULTIPLE)
                        .withSelectedDate(new Date());
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ArrayList<Date> dates = (ArrayList<Date>) calendar.getSelectedDates();
                Intent intent = new Intent();
                intent.putExtra("dates", dates);
                setResult(RESULT_OK, intent);

                Log.d(LOG_TAG, "try to finish CalendarPickerActivity");
                finish();
            }
        });

        range.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButtonsEnabled(range);

                calendar.setCustomDayView(new DefaultDayViewAdapter());

                calendar.setDecorators(Collections.<CalendarCellDecorator>emptyList());
                calendar.init(lastYear.getTime(), nextYear.getTime()) //
                        .inMode(CalendarPickerView.SelectionMode.RANGE)
                        .withSelectedDate(new Date());
            }
        });
    }

    private void setButtonsEnabled(Button currentButton) {
        for (Button modeButton : modeButtons) {
            modeButton.setEnabled(modeButton != currentButton);
        }
    }





    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        boolean applyFixes = theDialog != null && theDialog.isShowing();
        if (applyFixes) {
            Log.d(LOG_TAG, "Config change: unfix the dimens so I'll get remeasured!");
            dialogView.unfixDialogDimens();
        }
        super.onConfigurationChanged(newConfig);
        if (applyFixes) {
            dialogView.post(new Runnable() {
                @Override public void run() {
                    Log.d(LOG_TAG, "Config change done: re-fix the dimens!");
                    dialogView.fixDialogDimens();
                }
            });
        }
    }
}
