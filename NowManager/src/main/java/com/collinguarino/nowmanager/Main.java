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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.collinguarino.nowmanager.provider.Contracts;
import com.collinguarino.nowmanager.provider.NowManagerProvider;

public class Main extends ListActivity implements ActionBar.OnNavigationListener, LoaderManager.LoaderCallbacks<Cursor> {

    private final static String TAG = Main.class.getSimpleName();
    final Context context = this;
    private ActionBar mActionBar;

    private TimeCardAdapter mAdapter;

    public static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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


        app_launched(this);

        // restore the previously serialized current dropdown position.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new TimeCardAdapter(this, null);
        setListAdapter(mAdapter);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Serialize the current dropdown position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar().getSelectedNavigationIndex());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newTimeCard:
                createNewTimeCard();
                return true;

            case R.id.goTop:
                // go to top of the list
                getListView().smoothScrollToPosition(0);
                return true;

            case R.id.settings:
                Intent intent = new Intent(getApplicationContext(), Settings.class);
                startActivity(intent);
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
    }

    private void createNewTimeCard() {
        final NowManagerProvider provider = new NowManagerProvider();
        final ContentValues values;
        // If tally counter is selected from the actionbar dropdown then inflate numbers
        if (mActionBar.getSelectedNavigationIndex() == 1) {
            //get the number of tally rows
            final int tallyCount = Contracts.TimeCards.getTallyTimeCardCount(this) + 1;
            //is tally
            values = Contracts.TimeCards.getInsertValues(String.valueOf(tallyCount), true);
        } else {
            //not a tally
            values = Contracts.TimeCards.getInsertValues(null, false);
        }
        getContentResolver().insert(Contracts.TimeCards.CONTENT_URI, values);
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
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }
}
