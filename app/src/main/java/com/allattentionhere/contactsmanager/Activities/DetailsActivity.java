package com.allattentionhere.contactsmanager.Activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.allattentionhere.contactsmanager.Helper.Datacallback;
import com.allattentionhere.contactsmanager.Helper.HttpRequestHelper;
import com.allattentionhere.contactsmanager.Helper.MyApplication;
import com.allattentionhere.contactsmanager.Model.Person;

import com.allattentionhere.contactsmanager.R;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class DetailsActivity extends AppCompatActivity implements View.OnClickListener, Datacallback {

    ImageView img_contact, img_back, img_favorite;
    Person p;
    TextView txt_name, txt_email, txt_phone, txt_message, txt_share;
    ProgressDialog pd;
    int id;
    Button btn_retry;
    LinearLayout ll_details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        id = getIntent().getIntExtra("id", -1);

        init();
        setListener();
        if (id > 0) {
            makeNetworkCallForData();
        } else {
            finish();
            Toast.makeText(this, "Contact not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void setListener() {

        txt_email.setOnClickListener(this);
        txt_phone.setOnClickListener(this);
        txt_message.setOnClickListener(this);
        txt_share.setOnClickListener(this);
        img_back.setOnClickListener(this);
        btn_retry.setOnClickListener(this);
        img_favorite.setOnClickListener(this);
    }

    private void init() {
        pd = new ProgressDialog(this);
        txt_message = (TextView) findViewById(R.id.txt_message);
        txt_share = (TextView) findViewById(R.id.txt_share);
        img_contact = (ImageView) findViewById(R.id.img_contact);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_favorite = (ImageView) findViewById(R.id.img_favorite);
        btn_retry = (Button) findViewById(R.id.btn_retry);
        ll_details = (LinearLayout) findViewById(R.id.ll_details);
        txt_name = (TextView) findViewById(R.id.txt_name);
        txt_email = (TextView) findViewById(R.id.txt_email);
        txt_phone = (TextView) findViewById(R.id.txt_phone);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.txt_email:
                if (p.getEmail() != null) {

                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + p.getEmail()));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Contact details of " + p.getFirst_name());
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "Name:" + p.getFirst_name() + " " + p.getLast_name() + "\nPhone:" + p.getPhone_number() + "\nID:" + p.getId() + "\nCreated At:" + p.getCreated_at());
                    if (emailIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(Intent.createChooser(emailIntent, "Select app to send email"));
                    } else {
                        Toast.makeText(this, "Email app not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Email address null", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.txt_phone:
                if (p.getPhone_number() != null) {
                    Intent phone = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", p.getPhone_number(), null));
                    startIntent(phone,"Dialer");
                } else {
                    Toast.makeText(this, "Phone number is null", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.txt_message:
                if (p.getPhone_number() != null) {
                    Uri uri = Uri.parse("smsto:" + p.getPhone_number());
                    Intent sms = new Intent(Intent.ACTION_SENDTO, uri);
                    sms.putExtra("sms_body", "Name:" + p.getFirst_name() + " " + p.getLast_name() + "\nPhone:" + p.getPhone_number() + "\nID:" + p.getId() + "\nCreated At:" + p.getCreated_at());
                    startIntent(sms,"SMS");
                } else {
                    Toast.makeText(this, "Phone number is null", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.txt_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Name:" + p.getFirst_name() + " " + p.getLast_name() + "\nPhone:" + p.getPhone_number() + "\nID:" + p.getId() + "\nCreated At:" + p.getCreated_at());
                sendIntent.setType("text/plain");
                startIntent(sendIntent,"Share");
                break;
            case R.id.img_back:
                finish();
                break;
            case R.id.img_favorite:
                if (p.isFavorite()) {
                    p.setFavorite(false);
                    makeNetworkCallForFavorite(p);
                } else {
                    p.setFavorite(true);
                    makeNetworkCallForFavorite(p);
                }
                break;
            case R.id.btn_retry:
                makeNetworkCallForData();
                break;
        }
    }

    private void startIntent(Intent i, String name) {
        if (i.resolveActivity(getPackageManager()) != null) {
            startActivity(i);
        } else {
            Toast.makeText(this, name + " app not found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSuccess(JSONObject success, String uri, String method) {
        hideDialog();
        if (uri.equalsIgnoreCase("/contacts/" + id + ".json")) {
            if (method.equalsIgnoreCase("get")) {
                //load data call success
                btn_retry.setVisibility(View.GONE);
                p = new Gson().fromJson(success.toString(), Person.class);
                initLayout();
            } else if (method.equalsIgnoreCase("put")) {
                //mark favorite call success
                //update db
                MyApplication.dbHandler.open();
                MyApplication.dbHandler.updateContact(p, p.getId());
                MyApplication.dbHandler.close();

                //update layout
                if (p.isFavorite()) {
                    img_favorite.setImageDrawable(getResources().getDrawable(R.drawable.star_filled));
                } else {
                    img_favorite.setImageDrawable(getResources().getDrawable(R.drawable.star_empty));
                }
                Toast.makeText(this, "Marked Favorite: " + p.isFavorite(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initLayout() {
        txt_name.setText(p.getFirst_name() + " " + p.getLast_name());
        txt_email.setText(p.getEmail() + "");
        txt_phone.setText(p.getPhone_number() + "");
        ll_details.setVisibility(View.VISIBLE);
        if (p.isFavorite()) {
            img_favorite.setImageDrawable(getResources().getDrawable(R.drawable.star_filled));
        } else {
            img_favorite.setImageDrawable(getResources().getDrawable(R.drawable.star_empty));
        }
        if (p.getProfile_pic() != null && p.getProfile_pic().startsWith("/")) {
            Glide.with(this).load("http://gojek-contacts-app.herokuapp.com" + p.getProfile_pic()).into(img_contact).onLoadFailed(new Exception(), getResources().getDrawable(R.drawable.person));
        } else if (p.getProfile_pic() != null) {
            Glide.with(this).load(p.getProfile_pic()).into(img_contact).onLoadFailed(new Exception(), getResources().getDrawable(R.drawable.person));
        }
    }

    @Override
    public void onFailure(JSONObject failure, String uri, String method) {
        hideDialog();
        if (uri.equalsIgnoreCase("/contacts/" + id + ".json")) {
            if (method.equalsIgnoreCase("get")) {
                //load data call fail
                //show retry
                btn_retry.setVisibility(View.VISIBLE);
                ll_details.setVisibility(View.GONE);
            } else if (method.equalsIgnoreCase("put")) {
                //mark favorite call fail
                if (p.isFavorite()) {
                    p.setFavorite(false);
                    img_favorite.setImageDrawable(getResources().getDrawable(R.drawable.star_empty));
                } else {
                    p.setFavorite(true);
                    img_favorite.setImageDrawable(getResources().getDrawable(R.drawable.star_filled));

                }
                Toast.makeText(this, "Could not mark Favorite. Try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void makeNetworkCallForData() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //make network call
            btn_retry.setVisibility(View.GONE);
            ll_details.setVisibility(View.GONE);
            showDialog("Loading...");
            new HttpRequestHelper().MakeJsonGetRequest("/contacts/" + id + ".json", null, this, this);
        } else {
            btn_retry.setVisibility(View.VISIBLE);
            ll_details.setVisibility(View.GONE);
            hideDialog();
            Toast.makeText(this, "Not connected to Internet", Toast.LENGTH_SHORT).show();
        }
    }

    private void makeNetworkCallForFavorite(Person p) {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //make network call
            showDialog("Marking Favorite...");
            new HttpRequestHelper().MakeJsonPutRequest("/contacts/" + id + ".json", new Gson().toJson(p), this, this);
        } else {
            hideDialog();
            Toast.makeText(this, "Not connected to Internet", Toast.LENGTH_SHORT).show();
        }
    }

    public void showDialog(String message) {
        if (pd == null) pd = new ProgressDialog(this);
        pd.setMessage(message);
        pd.setCancelable(false);
        pd.show();
    }

    public void hideDialog() {
        if (pd != null && pd.isShowing()) pd.dismiss();
    }

}
