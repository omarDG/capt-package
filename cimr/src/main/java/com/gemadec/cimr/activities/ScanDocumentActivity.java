package com.gemadec.cimr.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.gemadec.cimr.R;
import com.gemadec.cimr.Utils.ImageUtil;
import com.gemadec.cimr.classes.CinDocInfo;
import com.regula.documentreader.api.DocumentReader;
import com.regula.documentreader.api.completions.IDocumentReaderCompletion;
import com.regula.documentreader.api.completions.IDocumentReaderInitCompletion;
import com.regula.documentreader.api.completions.IDocumentReaderPrepareCompletion;
import com.regula.documentreader.api.enums.DocReaderAction;
import com.regula.documentreader.api.enums.Scenario;
import com.regula.documentreader.api.enums.eGraphicFieldType;
import com.regula.documentreader.api.enums.eVisualFieldType;
import com.regula.documentreader.api.errors.DocumentReaderException;
import com.regula.documentreader.api.params.DocReaderConfig;
import com.regula.documentreader.api.results.DocumentReaderResults;
import com.regula.documentreader.api.results.DocumentReaderScenario;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class ScanDocumentActivity extends AppCompatActivity {
    private AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_document);

        /* final AlertDialog initDialog = showDialog(getString(R.string.initializing));*/

        //Reading the license from raw resource file
        try {
            InputStream licInput = getResources().openRawResource(R.raw.regula);
            int available = licInput.available();
            byte[] license = new byte[available];
            //noinspection ResultOfMethodCallIgnored
            licInput.read(license);
/*            DocumentReader.Instance().prepareDatabase(getApplicationContext(), "Full", new
                    DocumentReader.DocumentReaderPrepareCompletion() {
                        @Override
                        public void onPrepareProgressChanged(int progress) {
                            //get progress update
                        }

                        @Override
                        public void onPrepareCompleted(boolean status, String error) {
                            //database downloaded
                        }
                    });*/
           /* DocumentReader.Instance().prepareDatabase(ScanDocumentActivity.this, "Full", new IDocumentReaderPrepareCompletion() {
                @Override
                public void onPrepareProgressChanged(int progress) {
                    // getting progress
                    Log.i("ScanDocumentActivity", "" +progress);
                }

                @Override
                public void onPrepareCompleted(boolean status, @Nullable DocumentReaderException error) {

                }


            });*/
            //Initializing the reader

            DocumentReader.Instance().initializeReader(ScanDocumentActivity.this, new DocReaderConfig(license), new IDocumentReaderInitCompletion() {
                @Override
                public void onInitCompleted(boolean success, @Nullable DocumentReaderException error) {
                    DocumentReader.Instance().customization().edit().setShowStatusMessages(true).apply();


                    //initialization successful
                    if (success) {

                        DocumentReader.Instance().processParams().dateFormat =
                                DocumentReader.Instance().processParams().dateFormat = "dd/MM/yyyy";
                        DocumentReader.Instance().processParams().scenario = Scenario.SCENARIO_MRZ;
                        DocumentReader.Instance().showScanner(ScanDocumentActivity.this, completion);
                    } else {
                        Log.i("ScanDocumentActivity", "" + error.getMessage());
                    }
                }


            });

           /* DocumentReader.Instance().initializeReader(ScanDocumentActivity.this, license, new DocumentReader.DocumentReaderInitCompletion() {
                @Override
                public void onInitCompleted(boolean success, String error) {


                    DocumentReader.Instance().customization.showStatusMessages = true;


                    //initialization successful
                    if (success) {
                        DocumentReader.Instance().showScanner(completion);
                        DocumentReader.Instance().processParams.dateFormat = "dd/MM/yyyy";


                        DocumentReader.Instance().processParams.scenario = "Mrz";

                    }
                    //Initialization was not successful
                    else {
                        Log.i("ScanDocumentActivity", "" + R.string.initializing_failed);

                    }
                }
            });*/

            licInput.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    private AlertDialog showDialog(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(ScanDocumentActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.simple_dialog, null);
        dialog.setTitle(msg);
        dialog.setView(dialogView);
        dialog.setCancelable(false);
        return dialog.show();
    }


    @Override
    protected void onPause() {
        super.onPause();

        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    private IDocumentReaderCompletion completion = new IDocumentReaderCompletion() {

        @Override
        public void onCompleted(int action, DocumentReaderResults results, DocumentReaderException error) {
            if (action == DocReaderAction.COMPLETE) {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
                CinDocInfo infos = new CinDocInfo();

                infos.setFirstName(results.getTextFieldValueByType(com.regula.documentreader.api.enums.eVisualFieldType.FT_GIVEN_NAMES));
                infos.setLastName(results.getTextFieldValueByType(com.regula.documentreader.api.enums.eVisualFieldType.FT_SURNAME));
                infos.setIdentifier(results.getTextFieldValueByType(com.regula.documentreader.api.enums.eVisualFieldType.FT_PERSONAL_NUMBER));
                infos.setBirthdate(results.getTextFieldValueByType(com.regula.documentreader.api.enums.eVisualFieldType.FT_DATE_OF_BIRTH));
                infos.setExpirydate(results.getTextFieldValueByType(com.regula.documentreader.api.enums.eVisualFieldType.FT_DATE_OF_EXPIRY));
                infos.setPassnumber(results.getTextFieldValueByType(com.regula.documentreader.api.enums.eVisualFieldType.FT_DOCUMENT_NUMBER));
                infos.setNationality(results.getTextFieldValueByType(com.regula.documentreader.api.enums.eVisualFieldType.FT_NATIONALITY));
                infos.setSexe(results.getTextFieldValueByType(com.regula.documentreader.api.enums.eVisualFieldType.FT_SEX));
                infos.setAddress(results.getTextFieldValueByType(com.regula.documentreader.api.enums.eVisualFieldType.FT_ADDRESS));
                Bitmap documentImage = results.getGraphicFieldImageByType(eGraphicFieldType.GF_DOCUMENT_IMAGE);

                Intent intent = new Intent();
                Bundle bundle = new Bundle();

                bundle.putParcelable("infos", infos);
                File f3 = new File(Environment.getExternalStorageDirectory() + "/tmp/");
                if (!f3.exists())
                    f3.mkdirs();
                OutputStream outStream = null;
                String pathName = Environment.getExternalStorageDirectory() + "/tmp/" + "photo.png";
                File file = new File(pathName);
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    outStream = new FileOutputStream(file);
                    documentImage.compress(Bitmap.CompressFormat.PNG, 85, outStream);
                    outStream.close();
                    // Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                bundle.putString("photoPath", pathName);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Intent intent = new Intent();
                //something happened before all results were ready
                if (action == DocReaderAction.CANCEL) {
                    setResult(RESULT_CANCELED, intent);
                    finish();
                    // Toast.makeText(ScanDocumentActivity.this, R.string.scan_cancelled,Toast.LENGTH_LONG).show();

                } else if (action == DocReaderAction.ERROR) {
                    setResult(RESULT_CANCELED, intent);
                    finish();
                    // Toast.makeText(ScanDocumentActivity.this, R.string.doc_reading_error, Toast.LENGTH_LONG).show();
                }
            }
        }
    };
    /*private DocumentReader.DocumentReaderCompletion completion = new DocumentReader.DocumentReaderCompletion() {
        @Override
        public void onCompleted(int action, DocumentReaderResults results, String error) {
            if (action == DocReaderAction.COMPLETE) {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
                CinDocInfo infos = new CinDocInfo();

                infos.setFirstName(results.getTextFieldValueByType(com.regula.documentreader.api.enums.eVisualFieldType.FT_GIVEN_NAMES));
                infos.setLastName(results.getTextFieldValueByType(com.regula.documentreader.api.enums.eVisualFieldType.FT_SURNAME));
                infos.setIdentifier(results.getTextFieldValueByType(com.regula.documentreader.api.enums.eVisualFieldType.FT_PERSONAL_NUMBER));
                infos.setBirthdate(results.getTextFieldValueByType(com.regula.documentreader.api.enums.eVisualFieldType.FT_DATE_OF_BIRTH));
                infos.setExpirydate(results.getTextFieldValueByType(com.regula.documentreader.api.enums.eVisualFieldType.FT_DATE_OF_EXPIRY));
                infos.setPassnumber(results.getTextFieldValueByType(com.regula.documentreader.api.enums.eVisualFieldType.FT_DOCUMENT_NUMBER));
                infos.setNationality(results.getTextFieldValueByType(com.regula.documentreader.api.enums.eVisualFieldType.FT_NATIONALITY));
                infos.setSexe(results.getTextFieldValueByType(com.regula.documentreader.api.enums.eVisualFieldType.FT_SEX));
                infos.setAddress(results.getTextFieldValueByType(com.regula.documentreader.api.enums.eVisualFieldType.FT_ADDRESS));
                Bitmap documentImage = results.getGraphicFieldImageByType(eGraphicFieldType.GT_DOCUMENT_FRONT);

                Intent intent = new Intent();
                Bundle bundle = new Bundle();

                bundle.putParcelable("infos", infos);
                File f3 = new File(Environment.getExternalStorageDirectory() + "/tmp/");
                if (!f3.exists())
                    f3.mkdirs();
                OutputStream outStream = null;
                String pathName = Environment.getExternalStorageDirectory() + "/tmp/" + "photo.png";
                File file = new File(pathName);
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    outStream = new FileOutputStream(file);
                    documentImage.compress(Bitmap.CompressFormat.PNG, 85, outStream);
                    outStream.close();
                    // Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                bundle.putString("photoPath", pathName);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Intent intent = new Intent();
                //something happened before all results were ready
                if (action == DocReaderAction.CANCEL) {
                    setResult(RESULT_CANCELED, intent);
                    finish();
                    // Toast.makeText(ScanDocumentActivity.this, R.string.scan_cancelled,Toast.LENGTH_LONG).show();

                } else if (action == DocReaderAction.ERROR) {
                    setResult(RESULT_CANCELED, intent);
                    finish();
                    // Toast.makeText(ScanDocumentActivity.this, R.string.doc_reading_error, Toast.LENGTH_LONG).show();
                }
            }
        }
    };*/
}
