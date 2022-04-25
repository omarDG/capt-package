package com.gemadec.cimr.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gemadec.cimr.R;
import com.gemadec.cimr.Utils.DateUtil;
import com.gemadec.cimr.Utils.ImageUtil;

import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;

import org.jmrtd.BACKey;
import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.CardAccessFile;
import org.jmrtd.lds.PACEInfo;
import org.jmrtd.lds.SecurityInfo;
import org.jmrtd.lds.icao.DG1File;
import org.jmrtd.lds.icao.DG2File;
import org.jmrtd.lds.icao.MRZInfo;
import org.jmrtd.lds.iso19794.FaceImageInfo;
import org.jmrtd.lds.iso19794.FaceInfo;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TraitementActivity extends AppCompatActivity {
    private Tag mytag;

    ImageView imgaccess, imglecture, imgphoto, imglecturecancel, imgaccesscancel, imgphototcancel;
    ProgressBar progressaccess, progresslecture, progressphoto;
    TextView txtlecturedonnees, txttraitementphoto;
    boolean lu_infos = false;
    boolean traitement_photo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traitement);
        mytag = getIntent().getParcelableExtra("mytag");
        progressaccess = (ProgressBar) findViewById(R.id.progressaccess);
        progresslecture = (ProgressBar) findViewById(R.id.progresslecture);
        progressphoto = (ProgressBar) findViewById(R.id.progressphoto);
        imgaccess = (ImageView) findViewById(R.id.imgaccess);
        imglecturecancel = (ImageView) findViewById(R.id.imglecturecancel);
        imgaccesscancel = (ImageView) findViewById(R.id.imgaccesscancel);
        imgphototcancel = (ImageView) findViewById(R.id.imgphotocancel);
        imglecture = (ImageView) findViewById(R.id.imgLecture);
        imgphoto = (ImageView) findViewById(R.id.imgphoto);
        txtlecturedonnees = (TextView) findViewById(R.id.txtlectureinfos);
        txttraitementphoto = (TextView) findViewById(R.id.txttraitementphoto);
        new Getallinformation(this).execute();
    }

    private class Getallinformation extends AsyncTask<Void, String, Boolean> {
        private Context context;
        private Bitmap mybitmap;

        public Getallinformation(Context context) {
            this.context = context;
        }

        private final ProgressDialog dialog = new ProgressDialog(TraitementActivity.this);

        protected void onPreExecute() {

        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Boolean doInBackground(Void... params) {
            boolean statement = false;
            PassportService ps = null;
            try {
                IsoDep nfc = IsoDep.get(mytag);
                //Ajout d'un timeout
                //nfc.setTimeout(3000);
                CardService cs = CardService.getInstance(nfc);
                ps = new PassportService(cs,256, 224, false, true);
                ps.open();
                Log.i("SecondstepActivity", "PassportService open success : ");
                Intent intentpuce = getIntent();
                String passnum = intentpuce.getStringExtra("pass_num");
                Log.i("SecondstepActivity", "num_pass : " + passnum);
                BACKeySpec bacKey = new BACKey(intentpuce.getStringExtra("pass_num"), DateUtil.GetDateFormat(intentpuce.getStringExtra("birthdate"))
                        , DateUtil.GetDateFormat(intentpuce.getStringExtra("expirydate")));
                ps.doBAC(bacKey);
                /*if (!doPace(ps, bacKey)) {
                    Log.e("TraitementActi", "doPace false");
                    ps.sendSelectApplet(false);
                    ps.doBAC(bacKey);
                }
                else{
                    Log.e("TraitementActi", "doPace success");
                }*/
                /*
                 * Region for active authentication*/
                String digestAlgorithm = "SHA1";
                String signatureAlgorithm = "SHA1WithRSA/ISO9796-2";

                /* End region*/
                Log.i("SecondstepActivity", "doBAC success : ");

                InputStream is = null;
                InputStream is2 = null;

                try {
                    publishProgress("traitement");
                    // Basic data
                    is = ps.getInputStream(PassportService.EF_DG1);
                    is2 = ps.getInputStream(PassportService.EF_DG2);
                    // InputStream  is14= ps.getInputStream(PassportService.EF_DG14);

                    Log.i("SecondstepActivity", "getInputStream PassportService.EF_DG2 success : ");
                    DG1File dg1 = new DG1File(is);
                    MRZInfo mrzinfo = dg1.getMRZInfo();
                    Log.i("SecondstepActivity", "Document Number  : "+mrzinfo.getDocumentNumber());
                    Log.i("SecondstepActivity", "Nom : "+mrzinfo.getPrimaryIdentifier());
                    Log.i("SecondstepActivity", "Prenom : "+mrzinfo.getSecondaryIdentifier());
                    Log.i("SecondstepActivity", "naissance : "+mrzinfo.getDateOfBirth());
                    Log.i("SecondstepActivity", "naissance : "+mrzinfo.getDateOfExpiry());

                    //DG1File dg1 = (DG1File) LDSFileUtil.getLDSFile(PassportService.EF_DG1, is);
                    Log.i("SecondstepActivity", "DG1File success : ");
                    DG2File dg2 = new DG2File(is2);
                    // DG2File dg2 = (DG2File) LDSFileUtil.getLDSFile(PassportService.EF_DG2, is2);
                    Log.i("SecondstepActivity", "DG2File success : ");
                    int cpt = 0;
                    publishProgress("photo");
                    List<FaceInfo> faceInfos = dg2.getFaceInfos();
                    for (FaceInfo faceInfo : faceInfos) {
                        List<FaceImageInfo> faceImageInfos = faceInfo.getFaceImageInfos();
                        for (FaceImageInfo faceImageInfo : faceImageInfos) {
                            cpt++;
                            int imageLength = faceImageInfo.getImageLength();
                            DataInputStream dataInputStream = new DataInputStream(faceImageInfo.getImageInputStream());
                            Log.i("SecondstepActivity", "DataInputStream success : ");
                            byte[] buffer = new byte[imageLength];
                            dataInputStream.readFully(buffer, 0, imageLength);
                            Log.i("SecondstepActivity", "readFully success : ");
                            InputStream inputStream = new ByteArrayInputStream(buffer, 0, imageLength);
                            Log.i("SecondstepActivity", "InputStream success : ");
                            mybitmap = ImageUtil.decodeImage(
                                    TraitementActivity.this, faceImageInfo.getMimeType(), inputStream);
                            Log.i("SecondstepActivity", "Width image : " + mybitmap.getWidth());
                            Log.i("SecondstepActivity", "height image : " + mybitmap.getHeight());
                            publishProgress("photosuccess");

                            statement = true;
                        }
                    }
                } catch (Exception e) {
                    publishProgress("exception");
                    Log.i("SecondstepActivity", "Some exceptions : " + e.getMessage());
                    e.printStackTrace();

                }
            } catch (CardServiceException e) {

                Log.e("SecondstepActivity", "CardServiceException : " + e.getMessage());
                e.printStackTrace();
            } finally {
                return statement;
            }


        }

        @Override
        protected void onProgressUpdate(String... progress) {
            switch (progress[0]) {
                case "lecture":

                    break;
                case "traitement":

                    lu_infos = true;
                    progressaccess.setVisibility(View.INVISIBLE);
                    imgaccess.setVisibility(View.VISIBLE);
                    progresslecture.setVisibility(View.VISIBLE);
                    txtlecturedonnees.setEnabled(true);
                    break;
                case "photo":
                    traitement_photo = true;
                    progresslecture.setVisibility(View.INVISIBLE);
                    imglecture.setVisibility(View.VISIBLE);
                    progressphoto.setVisibility(View.VISIBLE);
                    txttraitementphoto.setEnabled(true);
                    break;
                case "photosuccess":
                    progressphoto.setVisibility(View.INVISIBLE);
                    imgphoto.setVisibility(View.VISIBLE);

                    break;
                case "exception":
                    if (traitement_photo) {
                        progressphoto.setVisibility(View.INVISIBLE);
                        imgphototcancel.setVisibility(View.VISIBLE);
                    } else if (lu_infos) {
                        progresslecture.setVisibility(View.INVISIBLE);
                        imglecturecancel.setVisibility(View.VISIBLE);
                    } else {
                        progressaccess.setVisibility(View.INVISIBLE);
                        imgaccesscancel.setVisibility(View.VISIBLE);
                    }
                    break;

            }


        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                Intent returnIntent = new Intent();
                try {

                    Bitmap finalBitmap = Bitmap.createScaledBitmap(mybitmap, 240, 320, false);
                    returnIntent.putExtra("mybitmap", finalBitmap);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                } catch (Exception e) {
                    Log.e("SecondStep", e.getMessage());
                    setResult(Activity.RESULT_CANCELED, returnIntent);
                    finish();
                }


            } else {
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
                //Toast.makeText(context, "Une erreur est survenue. veuillez réessayer.", Toast.LENGTH_LONG).show();
            }


        }

    }

    private boolean doPace(PassportService ps, BACKeySpec bacKey) {
        boolean isPaceSupported = false;
        try {
            CardAccessFile cardAccessFile = new CardAccessFile(ps.getInputStream(PassportService.EF_CARD_ACCESS));
            Collection<SecurityInfo> securityInfos = cardAccessFile.getSecurityInfos();
            SecurityInfo securityInfo = securityInfos.iterator().next();
            List<PACEInfo> paceInfos = getPACEInfos(securityInfos);
            if (paceInfos != null && paceInfos.size() > 0) {
                PACEInfo paceInfo = paceInfos.get(0);
                ps.doPACE(bacKey, paceInfo.getObjectIdentifier(), PACEInfo.toParameterSpec(paceInfo.getParameterId()),paceInfo.getParameterId());
                ps.sendSelectApplet(true);
                isPaceSupported = true;
            }
        } catch (IOException e) {
            Log.e("TraitementActi", "Error reading Pace IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (CardServiceException e) {
            Log.e("TraitementActi", "Error reading Pace: CardServiceException " + e.getMessage());
            e.printStackTrace();
        }
        return isPaceSupported;
    }

    public static List<PACEInfo> getPACEInfos(Collection<SecurityInfo> securityInfos) {
        List<PACEInfo> paceInfos = new ArrayList<PACEInfo>();

        if (securityInfos == null) {
            return paceInfos;
        }

        for (SecurityInfo securityInfo : securityInfos) {
            if (securityInfo instanceof PACEInfo) {
                paceInfos.add((PACEInfo) securityInfo);
            }
        }

        return paceInfos;
    }
}
