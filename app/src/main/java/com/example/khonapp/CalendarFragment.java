package com.example.khonapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;

import java.util.ArrayList;
import java.util.Calendar;

public class CalendarFragment extends Fragment {
    private static final String TAG = "FC";

    ArrayList<EventDay> events = new ArrayList<EventDay>();
    ArrayList<String> title = new ArrayList<>();
    ArrayList<String> description = new ArrayList<>();
    ArrayList<String> years = new ArrayList<>();
    ArrayList<String> months = new ArrayList<>();
    ArrayList<String> days = new ArrayList<>();

    TextView text_title, text_description;
    CalendarView calendarView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.calendarfra_layout, container, false);

        MainActivity activity = (MainActivity) getActivity();
        activity.settoolbarTitle("Even list");

        calendarView = view.findViewById(R.id.calendarView);
        text_title = view.findViewById(R.id.event_title);
        text_description = view.findViewById(R.id.event_description);

        String[] arr = {"2019-11-16", "2019-11-23", "2019-11-28", "2019-11-31", "2020-0-1", "2020-0-2"};
        String[] des = {"description 1", "description 2", "description 3", "description 4", "description 5", "description 6"};
        String[] tit = {"Title 1", "Title 2", "Title 3", "Title 4", "Title 5", "Title 6"};

        if (savedInstanceState == null) {
            boolean match = false;
            int date = calendarView.getFirstSelectedDate().get(Calendar.DATE);
            for (int i = 0; i < days.size(); i++) {
                if (String.valueOf(date).equals(days.get(i))) {
                    if (text_description.getVisibility() != View.VISIBLE) {
                        text_description.setVisibility(View.VISIBLE);
                    }
                    text_title.setText(tit[i]);
                    text_description.setText(description.get(i));
                    match = true;
                    Log.d(TAG, "onDayClick: Event Day position : " + i);
                    Log.d(TAG, "onDayClick: Event Description  : " + description.get(i));
                    break;
                } else {
                    match = false;
                }
            }
            if (!match) {
                text_title.setText("No Event");
                text_description.setVisibility(View.GONE);
            }
        }

        for (int i = 0; i < arr.length; i++) {
            String[] item = arr[i].split("-");
            years.add(item[0]);
            months.add(item[1]);
            days.add(item[2]);
            description.add(des[i]);
        }

        for (int j = 0; j < years.size(); j++) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Integer.valueOf(years.get(j)), Integer.valueOf(months.get(j)), Integer.valueOf(days.get(j)));
            events.add(new MyEventDay(calendar, R.drawable.calendar_dot, tit[j]));
        }
        /*Calendar calendar1 = Calendar.getInstance();
        calendar1.set(2019,11,4);
        events.add(new MyEventDay(calendar1, R.drawable.circle_btn_after, "I am Event 1"));

        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(2019,11,11);
        events.add(new MyEventDay(calendar2, R.drawable.circle_btn_after, "I am Event 2"));*/

        calendarView.setEvents(events);

        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                //Log.d(TAG, "onDayClick: " + eventDay.getCalendar().get(Calendar.DATE));
                int i;
                boolean match = false;
                for (i = 0; i < days.size(); i++) {
                    if (String.valueOf(eventDay.getCalendar().get(Calendar.DATE)).equals(days.get(i))) {
                        if (text_description.getVisibility() != View.VISIBLE) {
                            text_description.setVisibility(View.VISIBLE);
                        }
                        text_title.setText(((MyEventDay) eventDay).getNote());
                        text_description.setText(description.get(i));
                        match = true;
                        Log.d(TAG, "onDayClick: Event Day position : " + i);
                        Log.d(TAG, "onDayClick: Event Description  : " + ((MyEventDay) eventDay).getNote());
                        break;
                    } else {
                        match = false;
                    }
                }
                if (!match) {
                    text_title.setText("No Event");
                    text_description.setVisibility(View.GONE);
                }
            }
        });
        return view;
    }

    public void initData() {

    }
}
