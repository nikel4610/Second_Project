package gd.hq.yolov5;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.camera.core.UseCase;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Size;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.location.LocationManager;

import com.google.firebase.database.annotations.NotNull;
import com.gun0912.tedpermission.PermissionListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private ImageView resultImageView;
    private TextView thresholdTextview;
    private TextView tvInfo;
    private double threshold = 0.3, nms_threshold = 0.7;
    private TextureView viewFinder;
    private TextToSpeech tts;
    private TextView objectinfo;

    private AtomicBoolean detecting = new AtomicBoolean(false);
    private AtomicBoolean detectPhoto = new AtomicBoolean(false);

    private long startTime = 0;
    private long endTime = 0;
    private int width;
    private int height;
    private TextView locationinfo;
    private Permissions permission;
    View view;
    GestureDetector gestureDetector;
    Context cThis;
    SpeechRecognizer mRecognizer;
    Intent SttIntent;
    Intent i;
    private Button btnbarcode;
    private boolean _isBtnDown;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionCheck();

        cThis = this;

        objectinfo = (TextView) findViewById(R.id.objectinfo);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(listener);
        // mRecognizer.startListening(i);

        btnbarcode = (Button) findViewById(R.id.btnbarcode);
        btnbarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Barcode_activity.class);
                startActivity(intent);
            }
        });

        view = findViewById(R.id.view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(getApplicationContext(),"터치이벤트",Toast.LENGTH_SHORT).show();
                tts.setSpeechRate(1.5f);
                tts.speak(objectinfo.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Toast.makeText(getApplicationContext(),"롱터치이벤트",Toast.LENGTH_SHORT).show();
                Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vib.vibrate(500);

                if(ContextCompat.checkSelfPermission(cThis, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.RECORD_AUDIO},1);
                } else {
                    try {
                        mRecognizer.startListening(i);
                    } catch (SecurityException e){e.printStackTrace();}
                }

                return true;
            }
        });
        YOLOv5.init(getAssets());
        resultImageView = findViewById(R.id.imageView);
        thresholdTextview = findViewById(R.id.valTxtView);
        tvInfo = findViewById(R.id.tv_info);
        final String format = "Thresh: %.2f, NMS: %.2f";
        thresholdTextview.setText(String.format(Locale.ENGLISH, format, threshold, nms_threshold));

        resultImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectPhoto.set(false);
            }
        });

        viewFinder = findViewById(R.id.view_finder);
        viewFinder.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                updateTransform();
            }
        });

        viewFinder.post(new Runnable() {
            @Override
            public void run() {
                startCamera();
            }
        });
    }

    private void permissionCheck() {
        permission = new Permissions(this, this);

        if(!permission.checkPermission()) {
            permission.requestPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // 리턴이 false일 경우 다시 권한 요청
        if (!permission.permissionResult(requestCode, permissions, grantResults)){
            permission.requestPermission();
        }
    }

    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
//            Vibrator vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);
//            vib.vibrate(500);
            System.out.println("onReadyForSpeech.........................");
        }

        @Override
        public void onBeginningOfSpeech() {
            System.out.println("onBeginningOfSpeech.........................");
//            tts.setSpeechRate(1.5f);
//            tts.speak("찾으시는 물건을 말씀해주세요.", TextToSpeech.QUEUE_FLUSH, null);
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            System.out.println("onRmsChanged.........................");
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            System.out.println("onBufferReceived.........................");
        }

        @Override
        public void onEndOfSpeech() {
            System.out.println("onEndOfSpeech.........................");
        }

        @Override
        public void onError(int error) {
            // 에러 발생시 알림음 + tts로 에러 메시지 출력
//            tts.setSpeechRate(1.5f);
//            tts.speak("오류가 발생했습니다.", TextToSpeech.QUEUE_FLUSH, null);
            System.out.println("onError.........................");

            String message;

            if (error == SpeechRecognizer.ERROR_AUDIO) {
                message = "오디오 에러";
            } else if (error == SpeechRecognizer.ERROR_CLIENT) {
                message = "클라이언트 에러";
            } else if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                message = "퍼미션 없음";
            } else if (error == SpeechRecognizer.ERROR_NETWORK) {
                message = "네트워크 에러";
            } else if (error == SpeechRecognizer.ERROR_NETWORK_TIMEOUT) {
                message = "네트웍 타임아웃";
            } else if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                message = "찾을 수 없음";
                tts.setSpeechRate(1.5f);
                tts.speak("다시 말씀해주세요", TextToSpeech.QUEUE_FLUSH, null);
            } else if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                message = "RECOGNIZER가 바쁨";
            } else if (error == SpeechRecognizer.ERROR_SERVER) {
                message = "서버가 이상함";
            } else if (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                message = "말하는 시간초과";
            } else {
                message = "알 수 없는 오류임";
            }
            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. : " + message,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) {
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            FuncVoiceOrderCheck(rs[0]);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            System.out.println("onPartialResults.........................");
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            System.out.println("onEvent.........................");
        }
    };

    private void FuncVoiceOrderCheck(String VoiceMsg) {
        locationinfo = (TextView) findViewById(R.id.locationinfo);

        if(VoiceMsg.length()<1)return;

        VoiceMsg=VoiceMsg.replace(" ","");//공백제거
        locationinfo.setText(VoiceMsg); // 말한 문장을 텍스트뷰에 출력

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (locationinfo.length() > 0) {
                    objectinfo.getText().toString();
                    locationinfo.getText().toString();
                    // tts.setSpeechRate(1.5f);
                    // tts.speak("물건을 찾았습니다", TextToSpeech.QUEUE_FLUSH, null);
                    // locationinfo.setText("");
                }
            }
        };
        timer.schedule(timerTask, 0, 1000);
        // 일정 시간 지나면 종료 추가하기
    }

    private void updateTransform() {
        Matrix matrix = new Matrix();
        // Compute the center of the view finder
        float centerX = viewFinder.getWidth() / 2f;
        float centerY = viewFinder.getHeight() / 2f;

        float[] rotations = {0, 90, 180, 270};
        // Correct preview output to account for display rotation
        float rotationDegrees = rotations[viewFinder.getDisplay().getRotation()];

        matrix.postRotate(-rotationDegrees, centerX, centerY);

        // Finally, apply transformations to our TextureView
        viewFinder.setTransform(matrix);
    }

    private void startCamera() {
        CameraX.unbindAll();
        // 1. preview
        PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setLensFacing(CameraX.LensFacing.BACK)
//                .setTargetAspectRatio(Rational.NEGATIVE_INFINITY)
                .setTargetResolution(new Size(416, 416))
                .build();

        Preview preview = new Preview(previewConfig);
        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                ViewGroup parent = (ViewGroup) viewFinder.getParent();
                parent.removeView(viewFinder);
                parent.addView(viewFinder, 0);

                viewFinder.setSurfaceTexture(output.getSurfaceTexture());
                updateTransform();
            }
        });
        DetectAnalyzer detectAnalyzer = new DetectAnalyzer();
        CameraX.bindToLifecycle((LifecycleOwner) this, preview, gainAnalyzer(detectAnalyzer));

    }


    private UseCase gainAnalyzer(DetectAnalyzer detectAnalyzer) {
        ImageAnalysisConfig.Builder analysisConfigBuilder = new ImageAnalysisConfig.Builder();
        analysisConfigBuilder.setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE);
        analysisConfigBuilder.setTargetResolution(new Size(416, 416));
        ImageAnalysisConfig config = analysisConfigBuilder.build();
        ImageAnalysis analysis = new ImageAnalysis(config);
        analysis.setAnalyzer(detectAnalyzer);
        return analysis;
    }

    private class DetectAnalyzer implements ImageAnalysis.Analyzer {

        @Override
        public void analyze(ImageProxy image, final int rotationDegrees) {
            if (detecting.get() || detectPhoto.get()) {
                return;
            }
            detecting.set(true);
            startTime = System.currentTimeMillis();
            final Bitmap bitmapsrc = imageToBitmap(image);
            Thread detectThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(rotationDegrees);
                    width = bitmapsrc.getWidth();
                    height = bitmapsrc.getHeight();
                    Bitmap bitmap = Bitmap.createBitmap(bitmapsrc, 0, 0, width, height, matrix, false);

                    Box[] result = YOLOv5.detect(bitmap, threshold, nms_threshold);
                    final Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Canvas canvas = new Canvas(mutableBitmap);
                    final Paint boxPaint = new Paint();
                    boxPaint.setAlpha(200);
                    boxPaint.setStyle(Paint.Style.STROKE);
                    boxPaint.setStrokeWidth(4 * mutableBitmap.getWidth() / 800);
                    boxPaint.setTextSize(40 * mutableBitmap.getWidth() / 800);
                    for (Box box : result) {
                        boxPaint.setColor(box.getColor());
                        boxPaint.setStyle(Paint.Style.FILL);
                        canvas.drawText(box.getLabel(), box.x0 + 3, box.y0 + 40 * mutableBitmap.getWidth() / 1000, boxPaint);
                        boxPaint.setStyle(Paint.Style.STROKE);
                        canvas.drawRect(box.getRect(), boxPaint);
                        // locationinfo의 중심점 좌표 구하기
                        locationinfo = (TextView) findViewById(R.id.locationinfo);
                        if (locationinfo.length() > 0) {
                            if (box.getLabel().contains(locationinfo.getText().toString())) {
                                float x = (box.x0 + box.x1) / 2;
                                float y = (box.y0 + box.y1) / 2;
                                // x축이 view의 200dp 이하에 있으면 왼쪽에 있다고 말하기
                                if (x < 200) {
                                    tts.setSpeechRate(1.5f);
                                    tts.speak("전방에서 왼쪽부근에 있습니다", TextToSpeech.QUEUE_FLUSH, null);
                                    locationinfo.setText("");
                                }
                                // x축이 view의 200dp 이상 400dp 이하에 있으면 가운데에 있다고 말하기
                                else if (x >= 200) {
                                    tts.setSpeechRate(1.5f);
                                    tts.speak("전방에서 오른쪽부근에 있습니다", TextToSpeech.QUEUE_FLUSH, null);
                                    locationinfo.setText("");
                                }
                            }
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resultImageView.setImageBitmap(mutableBitmap);
                            detecting.set(false);
                            endTime = System.currentTimeMillis();
                            long dur = endTime - startTime;
                            float fps = (float) (1000.0 / dur);
                            tvInfo.setText(String.format(Locale.CHINESE,
                                    "Size: %dx%d\nTime: %.3f s\nFPS: %.3f",
                                    height, width, dur / 1000.0, fps));
                            // 인식된 물건 정보를 받아옴
                            if (result.length > 0) {
                                StringBuilder sb = new StringBuilder();
                                for (Box box : result) {
                                    // 중복이 되지 않도록 처리
                                    if (!sb.toString().contains(box.getLabel())) {
                                        // 같은 물건 정보가 여러개 있는 경우 개수 알려주기
                                        int count = 0;
                                        for (Box box2 : result) {
                                            if (box.getLabel().equals(box2.getLabel())) {
                                                count++;
                                            }
                                        }
                                        sb.append(box.getLabel()).append(" ");
                                        sb.append(count).append("개 ");
                                    }
                                }
                                objectinfo.setText(sb.toString());
                            }
                        }
                    });
                }
            }, "detect");
            detectThread.start();
        }

        private Bitmap imageToBitmap(ImageProxy image) {
            ImageProxy.PlaneProxy[] planes = image.getPlanes();
            ImageProxy.PlaneProxy y = planes[0];
            ImageProxy.PlaneProxy u = planes[1];
            ImageProxy.PlaneProxy v = planes[2];
            ByteBuffer yBuffer = y.getBuffer();
            ByteBuffer uBuffer = u.getBuffer();
            ByteBuffer vBuffer = v.getBuffer();
            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();
            byte[] nv21 = new byte[ySize + uSize + vSize];
            // U and V are swapped
            yBuffer.get(nv21, 0, ySize);
            vBuffer.get(nv21, ySize, vSize);
            uBuffer.get(nv21, ySize + vSize, uSize);

            YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 100, out);
            byte[] imageBytes = out.toByteArray();

            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        }

    }

    @Override
    protected void onDestroy() {
        CameraX.unbindAll();
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }

        if(mRecognizer != null){
            mRecognizer.destroy();
            mRecognizer.cancel();
            mRecognizer = null;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        detectPhoto.set(true);
        Bitmap image = getPicture(data.getData());
        Box[] result = YOLOv5.detect(image, threshold, nms_threshold);
        Bitmap mutableBitmap = image.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        final Paint boxPaint = new Paint();
        boxPaint.setAlpha(200);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(4 * image.getWidth() / 800);
        boxPaint.setTextSize(40 * image.getWidth() / 800);
        for (Box box : result) {
            boxPaint.setColor(box.getColor());
            boxPaint.setStyle(Paint.Style.FILL);
            canvas.drawText(box.getLabel(), box.x0 + 3, box.y0 + 17, boxPaint);
            boxPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(box.getRect(), boxPaint);
        }
        resultImageView.setImageBitmap(mutableBitmap);
    }

    public Bitmap getPicture(Uri selectedImage) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = this.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
        int rotate = readPictureDegree(picturePath);
        return rotateBitmapByDegree(bitmap, rotate);
    }

    public int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                    bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError ignored) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Toast.makeText(MainActivity.this, "권한이 허용됨", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(MainActivity.this, "권한이 거부됨", Toast.LENGTH_SHORT).show();
        }
    };

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;

    void checkRunTimePermission() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. 이미 퍼미션을 가지고 있음.
        }
    }

        class HomeFragment extends Fragment {

            private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
            private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
            private boolean isAccessFineLocation = false;
            private boolean isAccessCoarseLocation = false;
            private boolean isPermission = false;
            private TextView locationinfo;

            public HomeFragment() {

                // Required empty public constructor

            }

            @Override
            public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
                if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isAccessFineLocation = true;
                } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isAccessCoarseLocation = true;
                }

                if (isAccessFineLocation && isAccessCoarseLocation) {
                    isPermission = true;
                }

            }

        }
    }