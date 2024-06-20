package com.cookandroid.userapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.AsyncTask;

import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cookandroid.apicall.GetLog;
import com.cookandroid.apicall.GetRoot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.io.File;


public class MainActivity extends AppCompatActivity {
    public String nano33_1 = "https://8cnagbvcke.execute-api.ap-northeast-2.amazonaws.com/prod/ArduinoNano33IoT_1";
    String nano33_2 = "https://t2xdlgj7ee.execute-api.ap-northeast-2.amazonaws.com/prod/ArduinoNano33IoT_2";
    String nano33_3 = "https://02420f7rlk.execute-api.ap-northeast-2.amazonaws.com/prod/ArduinoNano33IoT_3";
    String nano33_4 = "https://i0d01iw4r5.execute-api.ap-northeast-2.amazonaws.com/prod/ArduinoNano33IoT_4";

    String every_map = "https://sphizygxo4.execute-api.ap-northeast-2.amazonaws.com/prod/maps";

    private static final String BUCKET_NAME = "imagebucketfornano";

    private Button start;
    private TextView rootNameTextView;
    public List<datalist> combinedList = new ArrayList<>();
    private ListView routelog;
    public TextView message;
    public static TextView currentTextView;
    private Button drawallhold,mapcheck;

    private static FrameLayout drawinglayout;
    private ImageView routeimg;
    private int imageViewWidth;
    private int imageViewHeight;
    public int targetWidth;
    public List<mapdatalist> sortedmlist = new ArrayList<>();
    int state = 0;

    public int datacount = 0;
    private int firstTime = -1;
    String routename;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        routeimg = findViewById(R.id.routeimage);
        routelog = findViewById(R.id.routelog);
        message = findViewById(R.id.recsec);
        rootNameTextView = findViewById(R.id.routename);
        start  = findViewById(R.id.recordloading);
        drawinglayout = findViewById(R.id.drawinglayout);
        mapcheck = findViewById(R.id.mapcheck);
        drawallhold = findViewById(R.id.allhold);
        drawallhold.setEnabled(false);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start.setText("기록 검색중...");
                start.setEnabled(false);
                combinedList.clear();
                datacount = 0;
                firstTime = -1;
                new GetLog(MainActivity.this, nano33_1).execute();
                new GetLog(MainActivity.this, nano33_2).execute();
                new GetLog(MainActivity.this, nano33_3).execute();
                new GetLog(MainActivity.this, nano33_4).execute();
                if (combinedList != null) {
                    new GetRoot(MainActivity.this, every_map).execute();
                }
            }
        });

        routelog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                double x = combinedList.get(i).x;
                double y = combinedList.get(i).y;

                imageViewWidth = routeimg.getWidth();
                imageViewHeight = routeimg.getHeight();
                removeCurrentTextView();
                drawPointOnImage(x, y, Color.GRAY); // 예시 좌표
                addtextview(x,y,combinedList.get(i).holdnum,combinedList.get(i).positoncolor);
            }
        });

        drawallhold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                drawallhold.setEnabled(false);
                animatePointsSequentially(R.drawable.righthand, Color.RED);
                animatePointsSequentially(R.drawable.lefthand, Color.YELLOW);
                animatePointsSequentially(R.drawable.rightfoot, Color.CYAN);
                animatePointsSequentially(R.drawable.leftfoot, Color.GREEN);


            }
        });

        mapcheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state == 0) {
                    for (int i = 0; i < sortedmlist.size(); i++) {
                        setallhold(sortedmlist.get(i).x, sortedmlist.get(i).y);
                    }
                    showAllHoldTexts();
                    mapcheck.setText("홀드 번호 가리기");
                    state = 1;
                } else {
                    for (int i = 0; i < drawinglayout.getChildCount(); i++) {
                        View child = drawinglayout.getChildAt(i);
                        if (child instanceof basePointView || child instanceof TextView) {
                            drawinglayout.removeView(child);
                            i--;
                        }
                    }
                    mapcheck.setText("홀드 번호 보기");
                    state = 0;
                }
            }
        });


    }

    private void showAllHoldTexts() {
        for (int i = 0; i < sortedmlist.size(); i++) {
            TextView holdTextView = new TextView(MainActivity.this);
            holdTextView.setText(sortedmlist.get(i).holdnum);
            holdTextView.setTextSize(5);
            holdTextView.setPadding(10, 5, 10, 5);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            params.leftMargin = (int) sortedmlist.get(i).x;
            params.topMargin = (int) sortedmlist.get(i).y;
            drawinglayout.addView(holdTextView, params);
        }
    }

    private void setallhold(double x, double y) {
        basePointView pointView = new basePointView(this, (float) x, (float) y);
        drawinglayout.addView(pointView);

    }

    public class basePointView extends View {
        private float x;
        private float y;
        private Paint paint;

        public basePointView(Context context, float x, float y) {
            super(context);
            this.x = x;
            this.y = y;
            paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
        }


        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawCircle(x, y, 5, paint);
        }
    }

    private static void removeCurrentTextView() {
        if (currentTextView != null) {
            drawinglayout.removeView(currentTextView);
            currentTextView = null;
        }
    }

    private void addtextview(double x, double y, String holdnum, int position) {
        String body = "";
        currentTextView = new TextView(this);
        currentTextView.setTextSize(13);
        currentTextView.setPadding(10, 5, 10, 5);
        currentTextView.setBackgroundResource(R.drawable.recordborder);

        if(position == R.drawable.righthand){
            currentTextView.setText("오른손 : "+holdnum);
            currentTextView.setTextColor(Color.RED);
        }
        else if (position == R.drawable.lefthand){
            currentTextView.setText("왼손 : "+holdnum);
            currentTextView.setTextColor(Color.YELLOW);
        }
        else if (position == R.drawable.rightfoot){
            currentTextView.setText("오른발 : "+holdnum);
            currentTextView.setTextColor(Color.CYAN);
        }
        else if (position == R.drawable.leftfoot){
            currentTextView.setText("왼발 : "+holdnum);
            currentTextView.setTextColor(Color.GREEN);
        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = (int) x;
        params.topMargin = (int) y;
        drawinglayout.addView(currentTextView, params);
    }

    private class DownloadAndResizeImageTask extends AsyncTask<Void, Void, Bitmap> {
        private static final String BUCKET_NAME = "imagebucketfornano";
        private File downloadedImageFile;
        private int targetHeight;

        public DownloadAndResizeImageTask(int targetHeight) {
            this.targetHeight = targetHeight;
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(MainActivity.this, "다운로드 중...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            try {
                File externalFilesDir = getExternalFilesDir(null);
                File imageFile = new File(externalFilesDir, "downloaded_image.png");
                DownloadS3Image.downloadImage(BUCKET_NAME, "images/" + routename + ".png", imageFile);
                downloadedImageFile = imageFile;

                Bitmap originalBitmap = BitmapFactory.decodeFile(downloadedImageFile.getAbsolutePath());
                if (originalBitmap == null) {
                    throw new Exception("이미지 로드 실패");
                }

                int originalWidth = originalBitmap.getWidth();
                int originalHeight = originalBitmap.getHeight();
                System.out.println(originalWidth + ":"+originalHeight);
                targetWidth = (int) ((double) targetHeight / originalHeight * originalWidth);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true);

                updatedatalist(GetRoot.marrayList, rootNameTextView.getText().toString(),targetWidth,targetHeight);


                return resizedBitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                routeimg.setImageBitmap(result);
                Toast.makeText(MainActivity.this, "이미지 다운로드 성공", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "이미지 다운로드 실패", Toast.LENGTH_SHORT).show();
            }
            start.setText("기록불러오기");
            start.setEnabled(true);
            drawallhold.setEnabled(true);
        }
    }



    public synchronized void updateList(List<datalist> newList) {
        combinedList.addAll(newList);
        datacount++;

        if (datacount >= 4) {
            Collections.sort(combinedList, new Comparator<datalist>() {
                @Override
                public int compare(datalist t1, datalist t2) {
                    return Integer.compare(t1.rectime, t2.rectime);
                }
            });

            if (combinedList.size() > 0 && firstTime == -1) {
                firstTime = combinedList.get(0).rectime;
            }

            recordlistview radapter = new recordlistview(new ArrayList<>(combinedList), firstTime);
            routelog.setAdapter(radapter);
            routelog.setDividerHeight(10);

            int lastTime = combinedList.get(combinedList.size() - 1).rectime;
            int timeDifference = lastTime - firstTime;
            message.setText(timeDifference + "초");

        } else {
            start.setText("조회중... " + datacount + "/4");
        }
    }

    public synchronized void searchUidInResponse(String rootValue) {
        if (rootValue != null) {
            routename = rootValue;
            rootNameTextView.setText(rootValue);

            new DownloadAndResizeImageTask(480).execute();
        } else {
            rootNameTextView.setText("검색된 결과가 없습니다.");
        }
    }

    private void drawPointOnImage(double x, double y, int color) {

        PointView pointView = new PointView(this, (float) x, (float) y, color);
        PointView.clearAllPoints();
        drawinglayout.addView(pointView);
    }

    private void animatePointsSequentially(int position, int color) {
        if (combinedList.isEmpty()) return;
        int additionaldelay = 0;
        List<datalist> points = new ArrayList<>();
        for (int i = 0; i < combinedList.size(); i++) {
            if (combinedList.get(i).positoncolor == position){
                points.add(combinedList.get(i));
            }
        }

        if (position == R.drawable.righthand) {
            additionaldelay = 1000;
        }
        else if (position == R.drawable.lefthand) {
            additionaldelay = 2000;
        }
        else if (position == R.drawable.rightfoot) {
            additionaldelay = 7000;
        }
        else if (position == R.drawable.leftfoot) {
            additionaldelay = 8000;
        }


        final PointView pointView = new PointView(this, (float) points.get(0).x, (float) points.get(0).y, color);
        drawinglayout.addView(pointView);

        final int[] index = {0};
        final int size = points.size();

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                double startX = points.get(index[0]).x;
                double startY = points.get(index[0]).y;
                double endX = points.get(index[0] + 1).x;
                double endY = points.get(index[0] + 1).y;
                float newX = (float) (startX + (endX - startX) * fraction);
                float newY = (float) (startY + (endY - startY) * fraction);

                pointView.setCoordinates(newX, newY);
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                index[0]++;
                if (index[0] < size - 1) {
                    int starttime = points.get(index[0]).rectime;
                    int endtime = points.get(index[0] + 1).rectime;
                    int delay = (endtime - starttime) * 1000-1000;

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            animator.start();

                        }
                    }, delay);
                } else {
                    drawallhold.setEnabled(true);
                }
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                animator.start();
            }
        }, additionaldelay);

    }


    public static class PointView extends View {
        private float x;
        private float y;
        private Paint paint;

        public PointView(Context context, float x, float y, int color) {
            super(context);
            this.x = x;
            this.y = y;
            paint = new Paint();
            paint.setColor(color);
            paint.setStyle(Paint.Style.FILL);

            setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    clearAllPoints();
                    return true;
                }
            });
        }

        private static void clearAllPoints() {
            for (int i = 0; i < drawinglayout.getChildCount(); i++) {
                View child = drawinglayout.getChildAt(i);
                if (child instanceof PointView) {
                    removeCurrentTextView();
                    drawinglayout.removeView(child);
                    i--;
                }
            }
        }

        public void setCoordinates(float x, float y) {
            this.x = x;
            this.y = y;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawCircle(x, y, 10, paint);

        }
    }

    public void updatedatalist(ArrayList<mapdatalist> mlist, String route, int targetWidth, int targetheight) {
        double mimx = Double.POSITIVE_INFINITY;
        double mimy = Double.POSITIVE_INFINITY;
        double maxx = Double.NEGATIVE_INFINITY;
        double maxy = Double.NEGATIVE_INFINITY;
        double scale_factor = 1.37;

        for (int i = 0; i < mlist.size(); i++) {
            if (mlist.get(i).rootname.equals(route)){
                sortedmlist.add(mlist.get(i));
                System.out.println(mlist.get(i).x);
            }
        }

        for (int i = 0; i < sortedmlist.size(); i++) {
            if (mimx > sortedmlist.get(i).x){
                mimx = sortedmlist.get(i).x;
            }
            if (mimy > sortedmlist.get(i).y) {
                mimy = sortedmlist.get(i).y;
            }
            //가장작은 x,y 값
            if (maxx < sortedmlist.get(i).x){
                maxx = sortedmlist.get(i).x;
            }
            if (maxy < sortedmlist.get(i).y){
                maxy = sortedmlist.get(i).y;
            }
        }

        double originalwidth = maxx-mimx+400;
        double originalheight = maxy-mimx+400;

        double absmimx = Math.abs(mimx);
        double absmimy = Math.abs(mimy);

        for (int i = 0; i < combinedList.size() ; i++) {
            double curx = combinedList.get(i).x + absmimx;
            double cury = combinedList.get(i).y + absmimy;

            if (cury > 0) {
                combinedList.get(i).y = (targetheight/originalheight)*(cury*scale_factor+200)-50;
            }
            else {
                combinedList.get(i).y = (targetheight/originalheight)*200-50;
            }
            if (curx > 0) {
                combinedList.get(i).x = (targetWidth/originalwidth)*(curx*(scale_factor+0.5)+200)+targetWidth/2+290;
            }
            else {
                combinedList.get(i).x = (targetWidth/originalwidth)*200+targetWidth/2+290;
            }
        }

        for (int i = 0; i < sortedmlist.size(); i++) {
            double curx = sortedmlist.get(i).x + absmimx;
            double cury = sortedmlist.get(i).y + absmimy;

            if (cury > 0) {
                sortedmlist.get(i).y = (targetheight/originalheight)*(cury*scale_factor+200)-50;
            }
            else {
                sortedmlist.get(i).y = (targetheight/originalheight)*200-50;
            }
            if (curx > 0) {
                sortedmlist.get(i).x = (targetWidth/originalwidth)*(curx*(scale_factor+0.5)+200)+targetWidth/2+290;
            }
            else {
                sortedmlist.get(i).x = (targetWidth/originalwidth)*200+targetWidth/2+290;
            }
        }
    }

}
