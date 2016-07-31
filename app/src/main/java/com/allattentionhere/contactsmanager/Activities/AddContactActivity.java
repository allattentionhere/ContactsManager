package com.allattentionhere.contactsmanager.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.Toast;

import com.allattentionhere.contactsmanager.Helper.Datacallback;
import com.allattentionhere.contactsmanager.Helper.HttpRequestHelper;
import com.allattentionhere.contactsmanager.Helper.MyApplication;
import com.allattentionhere.contactsmanager.Model.Person;
import com.allattentionhere.contactsmanager.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class AddContactActivity extends AppCompatActivity implements View.OnClickListener, Datacallback {

    ImageView img_contact, img_back;
    EditText etxt_email, etxt_mobile, etxt_lastname, etxt_firstname;
    ProgressDialog pd;
    Button btn_save, btn_gallery, btn_camera;
    Person p;
    ConnectivityManager connMgr;
    NetworkInfo networkInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addcontact);

        init();
        setListener();

    }

    private void setListener() {
        img_back.setOnClickListener(this);
        btn_save.setOnClickListener(this);
        btn_gallery.setOnClickListener(this);
        btn_camera.setOnClickListener(this);
    }

    private void init() {

        pd = new ProgressDialog(this);
        etxt_email = (EditText) findViewById(R.id.etxt_email);
        etxt_mobile = (EditText) findViewById(R.id.etxt_mobile);
        img_contact = (ImageView) findViewById(R.id.img_contact);
        img_back = (ImageView) findViewById(R.id.img_back);
        etxt_lastname = (EditText) findViewById(R.id.etxt_lastname);
        etxt_firstname = (EditText) findViewById(R.id.etxt_firstname);
        btn_save = (Button) findViewById(R.id.btn_save);
        btn_camera = (Button) findViewById(R.id.btn_camera);
        btn_gallery = (Button) findViewById(R.id.btn_gallery);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.img_back:
                finish();
                break;

            case R.id.btn_save:
                if (isValid()) {
                    showDialog("Uploading Image...");
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference fileRef = storage.getReferenceFromUrl("gs://contactsmanager-a9edd.appspot.com").child(System.currentTimeMillis() + ".jpg");

                    img_contact.setDrawingCacheEnabled(true);
                    img_contact.buildDrawingCache();
                    Bitmap bitmap = img_contact.getDrawingCache();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] data = baos.toByteArray();
                    UploadTask uploadTask = fileRef.putBytes(data);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            hideDialog();
                            Log.d("exception", "e=" + exception.toString());
                            Toast.makeText(AddContactActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            hideDialog();
                            p = new Person(etxt_firstname.getText().toString(), etxt_mobile.getText().toString(), etxt_email.getText().toString(), etxt_lastname.getText().toString(), taskSnapshot.getDownloadUrl() + "");
                            makeNetworkCallForSave(p);
                        }
                    });

                }
                break;
            case R.id.btn_camera:
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePicture.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePicture, 0);
                } else {
                    Toast.makeText(this, "No camera app installed", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_gallery:
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (pickPhoto.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(pickPhoto, 1);
                } else {
                    Toast.makeText(this, "No gallery app installed", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    Bitmap photo = (Bitmap) imageReturnedIntent.getExtras().get("data");
                    img_contact.setImageBitmap(photo);
                }
                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    img_contact.setImageURI(selectedImage);
                }
                break;
        }
    }

    private boolean isValid() {
        String num = etxt_mobile.getText().toString().trim();
        connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connMgr.getActiveNetworkInfo();
        if (etxt_firstname.getText().toString().trim().length() < 3) {
            etxt_firstname.requestFocus();
            etxt_firstname.setError("First Name not valid");
            return false;
        } else if (num.length() < 10 || num.length() > 13 || num.contains(".")) {
            etxt_mobile.requestFocus();
            etxt_mobile.setError("Mobile Number not valid");
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(etxt_email.getText().toString()).matches()) {
            etxt_email.requestFocus();
            etxt_email.setError("Email not valid");
            return false;
        } else if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(this, "Not connected to Internet", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    @Override
    public void onSuccess(JSONObject success, String uri, String method) {
        hideDialog();
        if (uri.equalsIgnoreCase("/contacts.json")) {
            hideDialog();
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    public void onFailure(JSONObject failure, String uri, String method) {
        hideDialog();
        if (uri.equalsIgnoreCase("/contacts.json")) {
            try {
                JSONArray jsonArray = failure.getJSONArray("errors");
                Toast.makeText(this, jsonArray.get(0).toString(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {

            }
            showRetry();
        }
    }


    private void makeNetworkCallForSave(Person p) {
        connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //make network call
            showDialog("Saving contact...");
            new HttpRequestHelper().MakeJsonPostRequest("/contacts.json", new Gson().toJson(p), this, this);
        } else {
            hideDialog();
            showRetry();
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

    public void showRetry() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Network call failed, want to retry?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (isValid()) {
                            makeNetworkCallForSave(p);
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
