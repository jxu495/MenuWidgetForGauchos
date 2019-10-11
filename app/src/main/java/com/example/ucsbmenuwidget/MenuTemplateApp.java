package com.example.ucsbmenuwidget;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class MenuTemplateApp extends FragmentActivity
        implements DatePickerDialog.OnDateSetListener {

    ExpandableListView expandMenu;
    ExpandableListAdapter expandMenuAdapter;
    List<String> menuTitles;
    HashMap<String, List<String>> menuDetails;
    TextView isClosedView;
    String closedText = "";
    Calendar menuDate = Calendar.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menutemplateapp);
        Intent intent = getIntent();
        int position = intent.getIntExtra("position", 0);

        // Here we turn your string.xml in an array
        String[] myKeys = getResources().getStringArray(R.array.dining_commons);
        TextView myTextView = findViewById(R.id.my_textview);
        myTextView.setText(myKeys[position]);
        menuTitles = new ArrayList<>();
        menuDetails = new HashMap<>();
        expandMenu = findViewById(R.id.expandmenu);
        isClosedView = findViewById(R.id.isclosedview);
        new MenuGetter().execute();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        menuDate.set(Calendar.YEAR, year);
        menuDate.set(Calendar.MONTH, month);
        menuDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        new MenuGetter().execute();

    }

    //change method to run in async task
    private class MenuGetter extends AsyncTask<Void, Void, Void> {
        private void getMenu() {
            try {
                //find selected diningcommon by checking string in my_textview
                String diningCommon = ((TextView) findViewById(R.id.my_textview)).getText().toString();
                String diningCommonID;
                switch (diningCommon) {
                    case "Carrillo":
                        diningCommonID = "carrillo-body";
                        break;
                    case "De La Guerra":
                        diningCommonID = "de-la-guerra-body";
                        break;
                    case "Ortega":
                        diningCommonID = "ortega-body";
                        break;
                    case "Portola":
                        diningCommonID = "portola-body";
                        break;
                    default:
                        diningCommonID = "Invalid";
                        break;
                }
                String commonsURL = diningCommonID.substring(0, diningCommonID.length() - 5);
                Document document = Jsoup.connect("https://appl.housing.ucsb.edu/menu/day/?dc="
                        + commonsURL + "&d=" + (menuDate.get(Calendar.YEAR)) + "-"
                        + (menuDate.get(Calendar.MONTH) + 1) + "-"
                        + (menuDate.get(Calendar.DAY_OF_MONTH)) +
                        "&m=breakfast&m=brunch&m=lunch&m=dinner&m=late-night&food=").get();
                //Document document = Jsoup.connect("https://appl.housing.ucsb.edu/menu/day/").get();
                /* this line finds the menu_widget for a dining common using the id tag (#), then
                * it goes through all its children, and looks through the children's children
                * for elements with class panel-body. This creates a list of menus based on meal time*/
                Elements meals = document.select("#" + diningCommonID + " > * > div.panel-body");
                Elements mealTitles = document.select("#" + diningCommonID + " > * > div.panel-heading");
                int idx = 0;
                //This for loop adds text to menuText in a format such that meal times, meal items are properly spaced
                menuTitles.clear();
                menuDetails.clear();
                for (Element title : mealTitles) {
                    menuTitles.add(title.text());
                    Elements formatMeals = meals.get(idx).select("> * > *");
                    List<String> mealList = new ArrayList<>();
                    for (Element meal : formatMeals) {
                        mealList.add(meal.text());
                    }
                    menuDetails.put(title.text(), mealList);
                    idx++;
                }
                //Note: closedText only shows in the onPostExecute method if there are no meals detected.
                closedText = diningCommon + " is closed.";
            } catch (IOException e) {
                e.printStackTrace();
                closedText = "Can't connect to UCSB Dining Website.";
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getMenu();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            expandMenuAdapter = new CustomExpandableListAdapter(getApplicationContext(), menuTitles, menuDetails);
            expandMenu.setAdapter(expandMenuAdapter);
            if(menuTitles.size() == 0) {
                isClosedView.setVisibility(View.VISIBLE);
                isClosedView.setText(closedText);
            } else {
                isClosedView.setVisibility(View.GONE);
            }
        }
    }

    public static class DatePickerFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle date = this.getArguments();
            Calendar c = (Calendar)date.getSerializable("menuDate");
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(getActivity(), (MenuTemplateApp)getActivity(), year, month, day);
            Calendar calendar = Calendar.getInstance();
            //set the minimum date to a week before the current date
            calendar.add(Calendar.DAY_OF_YEAR, -7);
            dialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
            //set max date to a week after current date
            calendar.add(Calendar.DAY_OF_YEAR, 14);
            dialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
            return dialog;
        }
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("menuDate", menuDate);
        newFragment.setArguments(bundle);
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }
}