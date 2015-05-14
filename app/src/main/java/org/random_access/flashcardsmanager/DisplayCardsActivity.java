package org.random_access.flashcardsmanager;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import org.random_access.flashcardsmanager.adapter.FlashCardCursorAdapter;
import org.random_access.flashcardsmanager.provider.contracts.DbJoins;
import org.random_access.flashcardsmanager.provider.contracts.FlashCardContract;
import org.random_access.flashcardsmanager.provider.contracts.LFRelationContract;

/**
 * Project: FlashCards Manager for Android
 * Date: 11.05.15
 * Author: Monika Schrenk
 * E-Mail: software@random-access.org
 */
public class DisplayCardsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = DisplayCardsActivity.class.getSimpleName();
    private static final String TAG_ADD_CARD = "add-card";

    private ListView mCardListView;
    private FlashCardCursorAdapter mCardAdapter;

    private long mCurrentProject;
    private long mCurrentLabel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "in oncreate");
        Bundle extras = getIntent().getExtras();
        mCurrentProject = extras.getLong(DisplayProjectsActivity.TAG_PROJECT_ID);
        mCurrentLabel = extras.getLong(DisplayLabelsActivity.TAG_LABEL_ID);
        setTitle("Cards");
        setContentView(R.layout.activity_display_cards);
        mCardListView = (ListView) findViewById(R.id.list_cards);
        mCardListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        mCardAdapter = new FlashCardCursorAdapter(this, null);
        mCardListView.setAdapter(mCardAdapter);
        setListActions();
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_display_cards, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case R.id.action_settings:
                return true;
            case R.id.action_add_card:
                // TODO
                AddCardFragment addCardFragment = AddCardFragment.newInstance(mCurrentProject, mCurrentLabel);
                addCardFragment.show(getFragmentManager(), TAG_ADD_CARD);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Starts a new or restarts an existing Loader
        getLoaderManager().restartLoader(0, null, this);;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] C_LIST_PROJECTION = {FlashCardContract.FlashCardEntry._ID,
                FlashCardContract.FlashCardEntry.COLUMN_NAME_QUESTION};
        return new CursorLoader(this, DbJoins.CONTENT_URI_FLASHCARDS_JOIN_LFRELS, C_LIST_PROJECTION,
                LFRelationContract.LFRelEntry.COLUMN_NAME_FK_L_ID + " = ? ", new String[]{mCurrentLabel + ""}, null); // TODO check if possible...
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "in onLoadFinished");
        mCardAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCardAdapter.swapCursor(null);
    }



    private void deleteSelectedCards() {
        long[] currentSelections = mCardListView.getCheckedItemIds();
        OnDeleteCardsDialogListener dialogClickListener = new OnDeleteCardsDialogListener(currentSelections);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setNeutralButton(getResources().getString(R.string.no), dialogClickListener)
                .setPositiveButton(getResources().getString(R.string.yes), dialogClickListener)
                .setTitle(getResources().getString(R.string.delete))
                .setMessage(getResources().getQuantityString(R.plurals.really_delete_card, currentSelections.length, currentSelections.length))
                .setCancelable(false);
        builder.show();
    }

    class OnDeleteCardsDialogListener implements DialogInterface.OnClickListener {

        long[] currentSelection;

        OnDeleteCardsDialogListener(long[] currentSelection) {
            this.currentSelection = currentSelection;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    int selCount = currentSelection.length;
                    for (long l : currentSelection) {
                        getContentResolver().delete(LFRelationContract.CONTENT_URI,
                                LFRelationContract.LFRelEntry.COLUMN_NAME_FK_F_ID + "=?", new String[]{ l + ""});
                        getContentResolver().delete(FlashCardContract.CONTENT_URI,
                                FlashCardContract.FlashCardEntry._ID + "=?", new String[]{l + ""});
                    }
                    Toast.makeText(DisplayCardsActivity.this, getResources().
                            getQuantityString(R.plurals.deleted_card, selCount, selCount), Toast.LENGTH_SHORT).show();
                    // set count for deleting multiple projects
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    // user cancelled
                    break;
            }
        }
    };


    private void setListActions() {

        mCardListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mCardListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.menu_card_context, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.action_edit_card:
                        Toast.makeText(DisplayCardsActivity.this, "Edit selected cards", Toast.LENGTH_SHORT).show();
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    case R.id.action_delete_card:
                        deleteSelectedCards();
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
    }
}