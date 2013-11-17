package com.collinguarino.nowmanager;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Main extends FragmentActivity implements ActionBar.OnNavigationListener {

    final Context context = this;
    public LinearLayout mContainerView;
    ActionBar actionBar;

    public static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Set up the action bar to show a dropdown list.
        actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(
                // Specify a SpinnerAdapter to populate the dropdown list.
                new ArrayAdapter<String>(
                        getActionBarThemedContextCompat(),
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        new String[]{
                                getString(R.string.title_section1),
                                getString(R.string.title_section2),
                        }),
                this);


        app_launched(this);

        // restore the previously serialized current dropdown position.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    /**
     * Backward-compatible version of {@link ActionBar#getThemedContext()} that
     * simply returns the {@link android.app.Activity} if
     * <code>getThemedContext</code> is unavailable.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private Context getActionBarThemedContextCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return getActionBar().getThemedContext();
        } else {
            return this;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Serialize the current dropdown position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getActionBar().getSelectedNavigationIndex());

    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newTimeFragment:

                inflateTimeCard();

                return true;

            case R.id.goTop:

                // go to top of scrollview
                ScrollView scrollView = (ScrollView) findViewById(R.id.mainView);
                scrollView.setSmoothScrollingEnabled(true);
                scrollView.fullScroll(ScrollView.FOCUS_UP);

                return true;

            case R.id.settings:

                Intent intent = new Intent(getApplicationContext(), Settings.class);
                startActivity(intent);

                return true;

            case R.id.goBot:

                // go to bottom of scrollview
                scrollView = (ScrollView) findViewById(R.id.mainView);
                scrollView.setSmoothScrollingEnabled(true);
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);

                return true;

            case R.id.deleteAll:

                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                builder1.setTitle("Delete All Logs?");
                builder1.setMessage("This action cannot be undone.");
                builder1.setCancelable(true);

                // delete
                builder1.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                hideKeyboard();
                                mContainerView.removeAllViews();
                                Toast.makeText(getApplicationContext(), "All Events Deleted", Toast.LENGTH_SHORT).show();

                            }
                        });

                // don't proceed
                builder1.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();

                            }
                        });

                AlertDialog alert = builder1.create();
                alert.show();

                return true;

        }
        return true;
    }

    private void inflateTimeCard() {
        // handling the inflation of a new timestamped card

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.time_card, null);

        final TextView dateText = (TextView) rowView.findViewById(R.id.dateText);
        final TextView timeText = (TextView) rowView.findViewById(R.id.timeText);

        Time time = new Time();
        time.setToNow();

        String ampm = "";

        Calendar datetime = Calendar.getInstance();

        if (datetime.get(Calendar.AM_PM) == Calendar.AM)
            ampm = "AM";
        else if (datetime.get(Calendar.AM_PM) == Calendar.PM)
            ampm = "PM";

        int hourString = Calendar.getInstance().get(Calendar.HOUR);
        if (hourString == 0) {
            hourString = 12;
        }

        int minuteString = Calendar.getInstance().get(Calendar.MINUTE);

        int secondString = Calendar.getInstance().get(Calendar.SECOND);

        if (!DateFormat.is24HourFormat(this)) {
            if (secondString < 10) {
                timeText.setText(hourString + ":" + Calendar.getInstance().get(Calendar.MINUTE) + ":" + "0" + secondString); // 12 hour version: add if statement on 24hr version
            } else if (minuteString < 10) {
                timeText.setText(hourString + ":0" + Calendar.getInstance().get(Calendar.MINUTE) + ":" + secondString); // 12 hour version: add if statement on 24hr version
            } else if (secondString < 10 && minuteString < 10) {
                timeText.setText(hourString + ":0" + Calendar.getInstance().get(Calendar.MINUTE) + ":" + "0" + secondString); // 12 hour version: add if statement on 24hr version
            } else {
                timeText.setText(hourString + ":" + Calendar.getInstance().get(Calendar.MINUTE) + ":" + secondString); // 12 hour version: add if statement on 24hr version
            }
        } else if (DateFormat.is24HourFormat(this)) {

            if (minuteString < 10) {
                timeText.setText(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":0" + Calendar.getInstance().get(Calendar.MINUTE) + ":" + secondString); // 12 hour version: add if statement on 24hr version
            }
            if (secondString < 10) {
                timeText.setText(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" + Calendar.getInstance().get(Calendar.MINUTE) + ":0" + secondString); // 12 hour version: add if statement on 24hr version
            }
            if (secondString < 10 && minuteString < 10) {
                timeText.setText(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":0" + Calendar.getInstance().get(Calendar.MINUTE) + ":0" + secondString); // 12 hour version: add if statement on 24hr version
            } else {
                timeText.setText(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" + Calendar.getInstance().get(Calendar.MINUTE) + ":" + secondString); // 12 hour version: add if statement on 24hr version
            }
        }

        dateText.setText(new SimpleDateFormat("MM-dd").format(new Date()) + " " + ampm);

        final CommonSwipeTouchListener onSwipeTouchListener = new CommonSwipeTouchListener(rowView);
        final RelativeLayout timeCardFragmentLayout = (RelativeLayout) rowView.findViewById(R.id.timeCardFragmentLayout);
        timeCardFragmentLayout.setOnTouchListener(onSwipeTouchListener);

        final RelativeLayout cardBack = (RelativeLayout) rowView.findViewById(R.id.cardBack);
        cardBack.setOnTouchListener(onSwipeTouchListener);

        // animation for popping in new card
        AnimationSet set = new AnimationSet(true);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        set.addAnimation(animation);

        animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
        );
        animation.setDuration(500);
        set.addAnimation(animation);
        rowView.setAnimation(animation);

        mContainerView = (LinearLayout) findViewById(R.id.parentView);

        // If tally counter is selected from the actionbar dropdown then inflate numbers
        if (actionBar.getSelectedNavigationIndex() == 1) {
            final EditText eventNameInput = (EditText) rowView.findViewById(R.id.eventNameInput);
            eventNameInput.setText(String.valueOf(mContainerView.getChildCount() + 1)); // gets index then adds one
        }

        mContainerView.addView(rowView, 0); //mContainerView.getChildCount() -1 for descending

        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                final ScrollView scrollView1 = (ScrollView) findViewById(R.id.mainView);
                scrollView1.setSmoothScrollingEnabled(true);
                scrollView1.fullScroll(View.FOCUS_UP);
            }
        };

        handler.postDelayed(r, 300);
    }

    /**
     * Common Swipe Touch Listener implementation
     */
    class CommonSwipeTouchListener extends OnSwipeTouchListener {

        final View rowView;

        public CommonSwipeTouchListener(final View rowView) {
            this.rowView = rowView;
        }

        public void onSwipeTop() {
        }

        public void onSwipeRight() {
            onSwipe();
        }

        public void onSwipeLeft() {
            onSwipe();
        }

        /**
         * Common swipe action.
         */
        private void onSwipe() {
            Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_SHORT).show();
            // Deletes the fragment
            hideKeyboard();
            mContainerView.removeViewAt(mContainerView.indexOfChild(rowView));
        }

        public void onSwipeBottom() {
        }

        public void onLongPressed() {
        }
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {

        return true;
    }


    // configures gesture actions for time card
    public class OnSwipeTouchListener implements View.OnTouchListener {

        public final GestureDetector gestureDetector = new GestureDetector(new GestureListener());

        public boolean onTouch(final View view, final MotionEvent motionEvent) {
            return gestureDetector.onTouchEvent(motionEvent);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener implements GestureDetector.OnGestureListener {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {

                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {


            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {


                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

                onLongPressed();

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                        }
                    } else {
                        if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffY > 0) {
                                onSwipeBottom();
                            } else {
                                onSwipeTop();
                            }
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }
        }

        public void onSwipeRight() {
        }

        public void onSwipeLeft() {
        }

        public void onSwipeTop() {
        }

        public void onSwipeBottom() {
        }

        public void onLongPressed() {

        }
    }


    // displays a dialog after x days or x app open intents asking the user to rate on the Google Play Store
    private final static String APP_TITLE = "Now Manager";
    private final static String APP_PNAME = "com.collinguarino.nowmanager";

    private final static int DAYS_UNTIL_PROMPT = 3;
    private final static int LAUNCHES_UNTIL_PROMPT = 6;

    public void app_launched(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
        if (prefs.getBoolean("dontshowagain", false)) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(mContext, editor);
            }
        }

        editor.commit();

        // used for testing on first start up
            /*SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
            SharedPreferences.Editor editor = prefs.edit();
            showRateDialog(mContext, editor);*/
    }

    // called by app_rater
    public void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.ratedialog);

        Button b1 = (Button) dialog.findViewById(R.id.button1);
        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                dialog.dismiss();
            }
        });

        Button b2 = (Button) dialog.findViewById(R.id.button2);
        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button b3 = (Button) dialog.findViewById(R.id.button3);
        b3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (editor != null) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
