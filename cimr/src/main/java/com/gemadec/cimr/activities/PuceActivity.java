package com.gemadec.cimr.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gemadec.cimr.R;

import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

public class PuceActivity extends AppCompatActivity {
    private NfcAdapter mAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter intentFiltersArray[];
    private String techListsArray[][];
    private Tag mytag;
TextView txtreadfailed;
    //Button btnLire;
    boolean lu = false;
    static final int READING_TASK = 1;  // The request code

    static {
        Security.addProvider(new BouncyCastleProvider());
        //Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puce);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter techFilter = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter tagFilter = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        txtreadfailed = (TextView) findViewById(R.id.txtreadfailed);
        try {
            ndefFilter.addDataType("*/*");    /* Handles all MIME based dispatches.
                                       You should specify only the ones that you need. */
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        intentFiltersArray = new IntentFilter[]{tagFilter, ndefFilter};
        techListsArray = new String[][]{new String[]{NfcF.class.getName()}};
    }

    protected void onResume() {
        super.onResume();
        enableForegroundDispatch();

    }

    private void enableForegroundDispatch() {

        try {
            // if (mAdapter == null) {
            mAdapter = NfcAdapter.getDefaultAdapter(this);
            //}
            if (mAdapter == null) {
                Toast.makeText(this, R.string.nfc_unavailable, Toast.LENGTH_LONG).show();
            } else if (!mAdapter.isEnabled()) {
                Toast.makeText(this, R.string.nfc_deactivated, Toast.LENGTH_LONG).show();
            } else {
                mAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
            }
            //Toast.makeText(this, "ForeGroundDispatch Enabled", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    public void onPause() {
        super.onPause();
        //désactiver l'écoute NFC
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == READING_TASK) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Log.i("PuceActivity", "resultCode == RESULT_OK");
                Bitmap mybitmap = (Bitmap) data.getParcelableExtra("mybitmap");
                if (mybitmap != null) {
                    double ratio = 320.0 / mybitmap.getHeight();
                    int targetHeight = (int) (mybitmap.getHeight() * ratio);
                    int targetWidth = (int) (mybitmap.getWidth() * ratio);
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("imgpassport", Bitmap.createScaledBitmap(mybitmap, targetWidth, targetHeight, false));
                    returnIntent.putExtra("code", "read_chip_success");
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Log.i("SeconstepActivity", "resultCode == RESULT_CANCELED");

                //si la récupération des informations du Passeport a échoué
                txtreadfailed.setText(R.string.read_chip_failed);
                txtreadfailed.setVisibility(View.VISIBLE);
                txtreadfailed.setTextColor(Color.RED);
                /*Intent returnIntent = new Intent();
                returnIntent.putExtra("code", "read_chip_failed");
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();*/

            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onNewIntent(Intent intent) {
        // new Getallinformation(this).execute();
        //Toast.makeText(this, "onNewIntent ", Toast.LENGTH_LONG).show();
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Intent intentfrom =getIntent();
        Intent traitementintent = new Intent(PuceActivity.this, TraitementActivity.class);
        traitementintent.putExtra("mytag", tag);

        traitementintent.putExtra("pass_num",intentfrom.getStringExtra("pass_num"));
        traitementintent.putExtra("birthdate",intentfrom.getStringExtra("birthdate"));
        traitementintent.putExtra("expirydate",intentfrom.getStringExtra("expirydate"));
        startActivityForResult(traitementintent, READING_TASK);

    }
}

