package gd.hq.yolov5;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Barcode_activity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         scanBarcode();
    }

    private String key = "0782bb97eb8f4a2285d0";

    public void scanBarcode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CustomScannerActivity.class);
        integrator.initiateScan();
    }

    private TextToSpeech tts;
    Gson gson = new Gson();

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // Log.d("onActivityResult", "onActivityResult: .");
        if (resultCode == Activity.RESULT_OK) {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            String re = scanResult.getContents();
            // Log.d("onActivityResult", "onActivityResult: ." + re);
            // Toast.makeText(this, re, Toast.LENGTH_LONG).show();
            String url = "http://openapi.foodsafetykorea.go.kr/api/" + key + "/C005/json/1/500/BAR_CD=" + re;
            // Log.d("onActivityResult", "onActivityResult: ." + url);

            InputStream is = null;
            try {
                is = new URL(url).openStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                // TODO PRDLST_NM 만 뽑아내기

            } catch (IOException e) {
                e.printStackTrace();
            }

            finish();
        }
    }
}

// http://openapi.foodsafetykorea.go.kr/api/0782bb97eb8f4a2285d0/C005/json/1/500/BAR_CD=

