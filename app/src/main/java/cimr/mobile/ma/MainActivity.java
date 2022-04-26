package cimr.mobile.ma;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.gemadec.cimr.activities.PuceActivity;
import com.gemadec.cimr.activities.ScanDocumentActivity;
import com.gemadec.cimr.classes.CinDocInfo;
import com.gemadec.cimrtest2.R;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, ScanDocumentActivity.class);
        startActivityForResult(intent, 200);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK) {
            String photoPath = data.getExtras().getString("photoPath");
            CinDocInfo infos = data.getExtras().getParcelable("infos");

            Log.i("Mainactivity"," "+infos.getPassnumber());

            Intent intent = new Intent(this, PuceActivity.class);
            intent.putExtra("pass_num",infos.getPassnumber());
            intent.putExtra("birthdate",infos.getBirthdate());
            intent.putExtra("expirydate",infos.getExpirydate());
            startActivityForResult(intent, 2);
        }
        if (requestCode == 2 && resultCode == RESULT_OK) {
            Bitmap imgpassport = (Bitmap) data.getParcelableExtra("imgpassport");

            String code= data.getStringExtra("code");
            Log.i("Mainactivity","imgpassport "+(imgpassport!=null));
        }
        else if(resultCode == RESULT_CANCELED){
            String code= data.getStringExtra("code");
            Log.i("Mainactivity","code "+(code));
        }
    }
}
