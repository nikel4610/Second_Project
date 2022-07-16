package gd.hq.yolov5;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.RecognitionListener;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;

public class voice extends AppCompatActivity {
    Context cThis;
    Intent SttIntent;
    SpeechRecognizer mRecognizer;
    TextToSpeech tts;
    GestureDetector gestureDetector;
    View view;
    private TextView locationinfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        cThis = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SttIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getApplicationContext().getPackageName());
        SttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");
        mRecognizer=SpeechRecognizer.createSpeechRecognizer(cThis);
        mRecognizer.setRecognitionListener(listener);

        locationinfo = (TextView) findViewById(R.id.locationinfo);

        tts = new TextToSpeech(cThis, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != android.speech.tts.TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        view = findViewById(R.id.view);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent f) {
                if(ContextCompat.checkSelfPermission(cThis, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(voice.this,new String[]{Manifest.permission.RECORD_AUDIO},1);
                } else {
                    try {
                        mRecognizer.startListening(SttIntent);
                    } catch (SecurityException e){e.printStackTrace();}
                }

            }
        });

        }
    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {
            tts.speak("찾으시는 물건을 말씀해주세요.", TextToSpeech.QUEUE_FLUSH, null);

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {
            // 에러 발생시 알림음 + tts로 에러 메시지 출력
            tts.speak("오류가 발생했습니다.", TextToSpeech.QUEUE_FLUSH, null);

        }

        @Override
        public void onResults(Bundle results) {
            String key= "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult =results.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            locationinfo.setText(rs[0]+"\r\n"+locationinfo.getText());
            FuncVoiceOrderCheck(rs[0]);
            mRecognizer.startListening(SttIntent);

        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };


    }

}
