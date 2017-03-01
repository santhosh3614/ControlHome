package com.nareshit.controlhome;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.preference.MultiSelectListPreference;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.nareshit.controlhome.adapter.AddDeviceAdapter;

public class AddDeviceActivity extends AppCompatActivity {

    private RecyclerView addDeviceRecyclerView;
    private AddDeviceAdapter addDeviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        addDeviceRecyclerView = (RecyclerView) findViewById(R.id.addDeviceRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        addDeviceRecyclerView.setLayoutManager(linearLayoutManager);
        addDeviceAdapter = new AddDeviceAdapter(this);
        addDeviceRecyclerView.setAdapter(addDeviceAdapter);
        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(addDeviceRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        Drawable horizontalDivider = ContextCompat.getDrawable(this, R.drawable.horizontal_divider);
        horizontalDecoration.setDrawable(horizontalDivider);
        addDeviceRecyclerView.addItemDecoration(horizontalDecoration);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addDeviceFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                final EditText editText = new EditText(AddDeviceActivity.this);
                new AlertDialog.Builder(AddDeviceActivity.this)
                        .setTitle(R.string.add_device)
                        .setView(editText).setPositiveButton(R.string.add_device, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addDeviceAdapter.addDevice(editText.getText().toString());
                        Snackbar.make(view, "New Device added", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }).show();
            }
        });
    }

}
