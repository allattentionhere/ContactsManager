package com.allattentionhere.contactsmanager.Activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.allattentionhere.contactsmanager.Helper.Datacallback;
import com.allattentionhere.contactsmanager.Helper.HttpRequestHelper;
import com.allattentionhere.contactsmanager.Helper.MyApplication;
import com.allattentionhere.contactsmanager.Helper.MyCursorAdapter;
import com.allattentionhere.contactsmanager.Model.DBHandler;
import com.allattentionhere.contactsmanager.Model.Person;
import com.allattentionhere.contactsmanager.R;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Datacallback, View.OnClickListener {

    private ListView mListView;
    private Cursor mCursor;
    FloatingActionButton fab;
    Snackbar sb;
    private static final String TAG = "Main";
    ProgressBar pb;
    MyCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        setListener();

        makeNetworkCall();


    }

    private void makeNetworkCall() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //make network call
            showProgress();
            new HttpRequestHelper().MakeJsonArrayRequest("/contacts.json", null, this, this);
        } else {
            hideProgress();
            showSnackbar("Not able to connect to Server", "RETRY");
            checkDbEmpty();
        }
    }

    private void showSnackbar(String s, String action) {
        if (sb != null && sb.isShownOrQueued()) {
            sb.dismiss();
        }
        sb = Snackbar.make(findViewById(R.id.cl), s, Snackbar.LENGTH_LONG).setDuration(Snackbar.LENGTH_INDEFINITE);
        if (action != null) {
            sb.setAction(action, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    makeNetworkCall();
                }
            });
        }
        sb.show();
    }

    private void hideProgress() {
        pb.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
    }

    private void showProgress() {
        pb.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
    }

    private void setListener() {
        fab.setOnClickListener(this);
    }

    private void init() {
        pb = (ProgressBar) findViewById(R.id.pb);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        mListView = (ListView) findViewById(R.id.listView);

    }


    @Override
    public void onSuccess(JSONObject success, String uri,String method) {
        if (uri.equalsIgnoreCase("/contacts.json")) {
            hideProgress();
            try {
                JSONArray jArr = success.getJSONArray("/contacts.json");
                List<Person> list = new ArrayList<>();
                for (int i = 0; i < jArr.length(); i++) {
                    Log.d(TAG, jArr.get(i).toString());
                    Person p = new Gson().fromJson(jArr.get(i).toString(), Person.class);
                    list.add(p);
                }
                insertIntoDbAndDisplay(list);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    private void insertIntoDbAndDisplay(List<Person> list) {
        MyApplication.dbHandler.open();
        MyApplication.dbHandler.deletePersonTable();
        MyApplication.dbHandler.insertContacts(list);
//        MyApplication.dbHandler.insertRandomPerson();
        mCursor = MyApplication.dbHandler.selectNames();
        mListView.setFastScrollEnabled(true);
        mListView.setEmptyView(findViewById(R.id.txt_empty));
        mAdapter=new MyCursorAdapter(getApplicationContext(),mCursor,this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cur = (Cursor) mAdapter.getItem(position);
                cur.moveToPosition(position);
                Intent i = new Intent(MainActivity.this, DetailsActivity.class);
                i.putExtra("id",cur.getInt(cur.getColumnIndex("id")));
                startActivity(i);
            }
        });
        MyApplication.dbHandler.close();
    }

    private void checkDbEmpty() {
        MyApplication.dbHandler.open();
        mCursor = MyApplication.dbHandler.selectNames();
        if (mCursor.getCount() > 0) {
            //load data if not empty
            mListView.setFastScrollEnabled(true);
            mListView.setEmptyView(findViewById(R.id.txt_empty));
             mAdapter=new MyCursorAdapter(getApplicationContext(),mCursor,this);
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Cursor cur = (Cursor) mAdapter.getItem(position);
                    cur.moveToPosition(position);
                    Intent i = new Intent(MainActivity.this, DetailsActivity.class);
                    i.putExtra("id",cur.getInt(cur.getColumnIndex("id")));
                    startActivity(i);
                }
            });

        }
        MyApplication.dbHandler.close();
    }

    @Override
    public void onFailure(JSONObject failure, String uri,String method) {
        hideProgress();
        Log.d(TAG, failure.toString());
        if (uri.equalsIgnoreCase("/contacts.json")) {
            showSnackbar("Not able to connect to Server", "RETRY");
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                //add contact
                Intent i = new Intent(this,AddContactActivity.class);
                startActivityForResult(i,100);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter!=null){
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==100 && resultCode==RESULT_OK){
            makeNetworkCall();
        }

    }
}
