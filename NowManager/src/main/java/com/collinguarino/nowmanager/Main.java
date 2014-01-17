package com.collinguarino.nowmanager;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.collinguarino.nowmanager.model.TimeCard;
import com.collinguarino.nowmanager.provider.Contracts;
import com.collinguarino.nowmanager.provider.NowManagerProvider;

import java.text.SimpleDateFormat;

public class Main extends ListActivity implements ActionBar.OnNavigationListener, LoaderManager.LoaderCallbacks<Cursor> {

    private final static String TAG = Main.class.getSimpleName();
    final Context context = this;
    private ActionBar mActionBar;
    public int countWarning, spinnerIndex;
    private TimeCardAdapter mAdapter;
    String defaultEventName;

    // UI
    Button newLogButton;

    // action bar
    ActionMode mActionMode;

    // For time elapsed label
    public static final SimpleDateFormat TIME_FORMAT_MILITARY = new SimpleDateFormat("kk:mm:ss");
    public static final SimpleDateFormat TIME_FORMAT_STANDARD = new SimpleDateFormat("hh:mm:ss");

    // Preferences
    boolean volumeKeys, vibrateOn, audioResponse;
    //int countInterval;

    // Sound recording vars
    private SoundMeter mSensor;
    private int mThreshold = 6; // sensitivity 0 - 8 (8 being hard to trigger)
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        spinnerIndex = preferences.getInt("spinnerIndex", 0);

        volumeKeys = preferences.getBoolean("volumeKeys", false);

        vibrateOn = preferences.getBoolean("vibrateOn", false);

        audioResponse = preferences.getBoolean("audioResponse", false);

        mThreshold = Integer.parseInt(preferences.getString("audio_sensitivity", "6"));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.main);

        // Set up the action bar to show a dropdown list.
        mActionBar = getActionBar();
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);


        // Set up the dropdown list navigation in the action bar.
        mActionBar.setListNavigationCallbacks(
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


        // runs
        app_launched(this);

        // restores index state of action bar spinner
        getActionBar().setSelectedNavigationItem(spinnerIndex);

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new TimeCardAdapter(this, null);
        setListAdapter(mAdapter);
        final ListView listView = getListView();
        listView.setLongClickable(true);
        listView.setItemsCanFocus(true);
        // Make the list dismissable by swipe.
        SwipeDismissListViewTouchListener swipeDismissListViewTouchListener = new SwipeDismissListViewTouchListener(listView, listDismissCallbacks);
        listView.setOnTouchListener(swipeDismissListViewTouchListener);
        listView.setOnScrollListener(swipeDismissListViewTouchListener.makeScrollListener());

        registerForContextMenu(listView);

        /*listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (mActionMode != null) {
                    return false;
                }

                // Start the CAB using the ActionMode.Callback defined above
                mActionMode = Main.this.startActionMode(mActionModeCallback);
                view.setSelected(true);
                //view.setBackgroundColor(Color.RED); // for touch radius testing purposes
                return true;

            }
        });*/

        newLogButton = (Button) findViewById(R.id.newLogButton);
        newLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewTimeCard();
            }
        });

        // Prepare the loader:  either re-connect with an existing one or start a new one.
        getLoaderManager().initLoader(0, null, this);

        /**
         * Define runnable thread to detect noise
         */
        if (audioResponse) {

            mSensor = new SoundMeter();

            try {
                mSensor.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }

            handler = new Handler();

            final Runnable r = new Runnable() {

                public void run() {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            double amp = mSensor.getAmplitude();

                            if (amp > mThreshold) {
                                createNewTimeCard();

                                // Will only play if ringer volume is set to ring
                                try {
                                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                                    r.play();
                                } catch (Exception e) {}
                            }

                            handler.postDelayed(this, 100); // amount of delay between every cycle of volume level detection + sending the data  out

                        }
                    });
                }
            };
            // initialize the handler (required for handler to work)
            handler.postDelayed(r, 100);

        }

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (volumeKeys) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                createNewTimeCard();
                return true;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * Contextual action bar
     * TODO: Share events, change background color, set reminder, delete log
     */
    final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.contextual, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // Actions on contextual action bar once inflated
            switch (item.getItemId()) {
                case R.id.share:

                    // DOES NOT WORK : how do I return the value of the selected TimeCard event name and date information?
                    String totalOutput = ("This feature doesn't work yet, check in later :)");

                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    String shareBody = totalOutput;
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                    startActivity(Intent.createChooser(sharingIntent, "Share Log With:"));

                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.reminder:

                    // set reminder - open dialog to choose time from event

                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        spinnerIndex = preferences.getInt("spinnerIndex", 0);

        volumeKeys = preferences.getBoolean("volumeKeys", false);

        vibrateOn = preferences.getBoolean("vibrateOn", false);

        audioResponse = preferences.getBoolean("audioResponse", false);

        mThreshold = Integer.parseInt(preferences.getString("audio_sensitivity", "6"));

        if (audioResponse) {
            stopService(new Intent(this, Main.class)); // required re-draw
            mSensor.start();
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getActionBar().setSelectedNavigationItem(spinnerIndex);
    }

    /**
     * Callbacks for when list items are dismissed (by swipe).
     */
    private SwipeDismissListViewTouchListener.DismissCallbacks listDismissCallbacks = new SwipeDismissListViewTouchListener.DismissCallbacks() {
        @Override
        public boolean canDismiss(int position) {
            //Return false here if the item at the position should not be dissmissable
            return true;
        }

        @Override
        public void onDismiss(ListView listView, int[] reverseSortedPositions) {
            // TODO Should flag items for deletion instead of a strict delete.
            // TODO only then will an Undo feature be possible.
            for (int position : reverseSortedPositions) {
                final TimeCard timeCard = ((TimeCardAdapter)mAdapter).getTimeCard(position);
                if(timeCard != null) {
                    getContentResolver().delete(Contracts.TimeCards.CONTENT_URI, Contracts.TimeCards._ID + " = " + timeCard.getId(), null);

                    Toast toast = Toast.makeText(getApplicationContext(), "Log Deleted", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM,0,280);
                    toast.show();
                }
            }
        }
    };

    /**
     * Backward-compatible version of {@link ActionBar#getThemedContext()} that
     * simply returns the {@link android.app.Activity} if
     * <code>getThemedContext</code> is unavailable.
     */
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        // The delete all menu option shouldn't be selectable if there are no items to delete.
        final int itemCount = getItemCount();
        final boolean shouldEnableButton = itemCount != 0;
        menu.findItem(R.id.deleteAll).setEnabled(shouldEnableButton);
        menu.findItem(R.id.goTop).setEnabled(shouldEnableButton);
        menu.findItem(R.id.goBot).setEnabled(shouldEnableButton);

        // The jump to top/bottom of the list buttons should only show when necessary.
        // Hide the buttons if there aren't many items in the list, show the buttons if there are.
        final ListView listView = getListView();
        if(listView != null) {
            final int visibleItemsInList = listView.getLastVisiblePosition() - listView.getFirstVisiblePosition() + 1;
            final boolean showJumpToOptions =  itemCount > visibleItemsInList;
            menu.findItem(R.id.goTop).setVisible(showJumpToOptions);
            menu.findItem(R.id.goBot).setVisible(showJumpToOptions);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(getApplicationContext(), Settings.class);
                startActivity(intent);
                return true;
            case R.id.goTop:
                // go to top of the list
                getListView().smoothScrollToPosition(0);
                return true;
            case R.id.goBot:
                // go to bottom of the list
                getListView().smoothScrollToPosition(getItemCount());
                return true;

            case R.id.deleteAll:
                showDeleteAllConfirmation();
                return true;
        }
        return true;
    }

    /**
     * Helper method to show an ok/cancel alert to the user for deleting all items.
     */
    private void showDeleteAllConfirmation() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setTitle("Delete All Logs?");
        builder1.setMessage("This action cannot be undone.");
        builder1.setCancelable(true);

        // delete
        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        NowManagerProvider provider = new NowManagerProvider();
                        getContentResolver().delete(Contracts.TimeCards.CONTENT_URI, null, null);
                        hideKeyboard();

                        Toast toast = Toast.makeText(getApplicationContext(), "All Logs Deleted", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.BOTTOM,0,280);
                        toast.show();

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
    }

    public void createNewTimeCard() {
        final NowManagerProvider provider = new NowManagerProvider();
        final ContentValues values;

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        countWarning = Integer.parseInt(preferences.getString("countWarning", "0"));

        defaultEventName = String.valueOf(preferences.getString("defaultEventName", ""));

        // cannot use countInterval because getTallyTimeCardCount() doesn't return the previous count
        //countInterval = Integer.parseInt(preferences.getString("countInterval", "1"));

        // If tally counter is selected from the actionbar dropdown then inflate numbers
        if (mActionBar.getSelectedNavigationIndex() == 1) {
            //get the number of tally rows
            final int tallyCount = Contracts.TimeCards.getTallyTimeCardCount(this) + 1; // + countInterval
            //is tally
            values = Contracts.TimeCards.getInsertValues(String.valueOf(tallyCount), true);

            // Tally limit has been reached
            if (countWarning == Integer.valueOf(tallyCount) && countWarning != 0) {
                showCountWarningDialog();
            }

        // If there is a defined default event name, use it
        } else if (defaultEventName != "") {
            //not a tally
            values = Contracts.TimeCards.getInsertValues(defaultEventName, false);
        // No default event name, proceed as normal without preferences
        } else {
            values = Contracts.TimeCards.getInsertValues(null, false);
        }
        getContentResolver().insert(Contracts.TimeCards.CONTENT_URI, values);
        if (vibrateOn) {
            ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(300);
        }
    }

    /**
     * Get the number of items that currently exist.
     *
     * @return The number of items that currently exist.
     */
    private int getItemCount() {
        if (mAdapter == null) {
            return 0;
        }
        return mAdapter.getCount();
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("spinnerIndex", mActionBar.getSelectedNavigationIndex());
        editor.commit();

        return true;
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
    //TODO Convert this dialog to a DialogFragment.
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

/*    private void showCountWarning() {
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
                this);

        alertDialog2.setTitle("Count Limit Reached");
        alertDialog2.setMessage("Your defined limit of " + countWarning + " has been reached.");

        // lets the user continue, keeps the count limit for next time
        alertDialog2.setPositiveButton("Continue",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();

                    }
                });

        // removes the limit set in preferences and allows the user to continue
        alertDialog2.setNegativeButton("Remove Limit",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        SharedPreferences preferences = PreferenceManager
                                .getDefaultSharedPreferences(context);

                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("countWarning", "0");
                        editor.commit();

                        Toast toast = Toast.makeText(getApplicationContext(), "Count Limit Reset", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.BOTTOM,0,280);
                        toast.show();

                    }
                });
        alertDialog2.show();
    }*/

    /**
     * Helper method to show the tally count warning after a user specified integer
     */
    public void showCountWarningDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.count_warning_dialog);

        TextView textView = (TextView) dialog.findViewById(R.id.textView);
        textView.setText("Your defined limit of " + String.valueOf(countWarning) + " has been reached.");

        Button b1 = (Button) dialog.findViewById(R.id.button1);
        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // remove limit
                SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(context);

                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("countWarning", "0");
                editor.commit();

                Toast toast = Toast.makeText(getApplicationContext(), "Count Limit Reset", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM,0,280);
                toast.show();

                // dismiss dialog to carry on using app
                dialog.dismiss();
            }
        });

        Button b2 = (Button) dialog.findViewById(R.id.button2);
        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // set new limit option

                // dismiss current dialog
                dialog.dismiss();

                // show new dialog to set new limit
                showNewInputCountLimitDialog();

            }
        });

        Button b3 = (Button) dialog.findViewById(R.id.button3);
        b3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Continue, quit dialog
                dialog.cancel();
            }
        });

        dialog.show();
    }


    /**
     * Helper method for showCountWarningDialog that allows user input of new count limit specification
     */
    public void showNewInputCountLimitDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.set_count_limit_dialog);

        final EditText countLimitInput = (EditText) dialog.findViewById(R.id.editText);
        //countLimitInput.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        Button b1 = (Button) dialog.findViewById(R.id.button1);
        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // grab new count limit integer from the edittext and set it in the sharedprefs editor

                SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(context);

                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("countWarning", String.valueOf(countLimitInput.getText().toString()));
                editor.commit();

                // hide the keyboard
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(countLimitInput.getWindowToken(), 0);

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void hideKeyboard() {
        final View currentFocusedView = this.getCurrentFocus();
        if (currentFocusedView == null) {
            return;
        }
        InputMethodManager inputManager = (InputMethodManager)
                this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        final Uri baseUri = Contracts.TimeCards.CONTENT_URI;

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(this, baseUri,
                Contracts.TimeCards.SELECT_ALL_PROJECTION, null, null,
                Contracts.TimeCards.C_TIMESTAMP + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no longer using it.
        mAdapter.swapCursor(null);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (audioResponse) {
            mSensor.stop();
        }

    }
}
