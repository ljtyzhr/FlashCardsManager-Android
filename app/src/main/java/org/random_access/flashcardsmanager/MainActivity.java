package org.random_access.flashcardsmanager;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.random_access.flashcardsmanager.adapter.ProjectCursorAdapter;
import org.random_access.flashcardsmanager.storage.contracts.ProjectContract;

/**
 * Project: FlashCards Manager for Android
 * Date: 09.05.15
 * Author: Monika Schrenk
 * E-Mail: software@random-access.org
 */
public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String TAG_ADD_PROJECT = "add-project";

    private ListView mProjectListView;
    private ProjectCursorAdapter mProjectAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProjectListView = (ListView) findViewById(R.id.list_projects);
        mProjectListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        mProjectAdapter = new ProjectCursorAdapter(this, null);
        mProjectListView.setAdapter(mProjectAdapter);
        setListActions();
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_add_project:
                AddProjectFragment addProjectFragment = AddProjectFragment.newInstance();
                addProjectFragment.show(getFragmentManager(), TAG_ADD_PROJECT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Starts a new or restarts an existing Loader
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] P_LIST_PROJECTION = { ProjectContract.ProjectEntry._ID,
                ProjectContract.ProjectEntry.COLUMN_NAME_PROJECT_TITLE,
                ProjectContract.ProjectEntry.COLUMN_NAME_PROJECT_STACKS };
        return new CursorLoader(this, ProjectContract.CONTENT_URI, P_LIST_PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mProjectAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProjectAdapter.swapCursor(null);
    }

    private void deleteSelectedProjects() {
        long[] currentSelections = mProjectListView.getCheckedItemIds();
        OnDeleteProjectsDialogListener dialogClickListener = new OnDeleteProjectsDialogListener(currentSelections);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setNeutralButton(getResources().getString(R.string.no), dialogClickListener)
                .setPositiveButton(getResources().getString(R.string.yes), dialogClickListener)
                .setTitle(getResources().getString(R.string.delete))
                .setMessage(getResources().getQuantityString(R.plurals.really_delete_project, currentSelections.length, currentSelections.length))
                .setCancelable(false);
        builder.show();
    }

    class OnDeleteProjectsDialogListener implements DialogInterface.OnClickListener {

        long[] currentSelection;

        OnDeleteProjectsDialogListener(long[] currentSelection) {
            this.currentSelection = currentSelection;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    int selCount = currentSelection.length;
                    for (long l : currentSelection) {
                        getContentResolver().delete(ProjectContract.CONTENT_URI,
                                ProjectContract.ProjectEntry._ID + "=?", new String[]{l + ""});
                    }
                    Toast.makeText(MainActivity.this, getResources().
                            getQuantityString(R.plurals.deleted_project, selCount, selCount), Toast.LENGTH_SHORT).show();
                        // set count for deleting multiple projects
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    // user cancelled
                    break;
            }
        }
    };

    private void setListActions () {
        mProjectListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mProjectListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.menu_project_context, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.action_edit_project:
                            Toast.makeText(MainActivity.this, "Edit selected projects", Toast.LENGTH_SHORT).show();
                            mode.finish(); // Action picked, so close the CAB
                            return true;
                    case R.id.action_delete_project:
                        deleteSelectedProjects();
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
