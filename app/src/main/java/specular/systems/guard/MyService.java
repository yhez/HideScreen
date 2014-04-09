package specular.systems.guard;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.OutputStream;

public class MyService extends Service {
    public MyService() {
    }

    int a = 0;
    Handler hndl = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            a++;
            chatHead.setVisibility(View.VISIBLE);
            chatHead.postDelayed(new Runnable() {
                @Override
                public void run() {
                    chatHead.setVisibility(View.INVISIBLE);
                    hndl.sendEmptyMessage(0);
                }
            }, 1000);
            if (a > 15) {
                a = 0;
                if (!added) {
                    try {
                        Process sh = Runtime.getRuntime().exec("su", null, null);
                        OutputStream os = sh.getOutputStream();
                        os.write(("/system/bin/screencap -p " + "/sdcard/img.png").getBytes("ASCII"));
                        os.flush();
                        os.close();
                        sh.waitFor();
                        Bitmap b = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/img.png");
                        b = b.copy(Bitmap.Config.ARGB_4444, true);
                        for (int a = 0; a < b.getHeight() / 10; a++) {
                            if (a % 2 == 0)
                                for (int y = 0; y < 10; y++) {
                                    for (int c = 0; c < b.getWidth() - 10; c++) {
                                        b.setPixel(c, a * 10 + y, b.getPixel(c + 10, a * 10 + y));
                                    }
                                }
                        }
                        ll.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        ll.setImageBitmap(b);
                        windowManager.addView(ll, param);
                        windowManager.removeViewImmediate(chatHead);
                        windowManager.addView(chatHead, params);
                        added = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("");
    }

    boolean added;
    WindowManager windowManager;
    ImageView chatHead;
    ImageView ll;
    WindowManager.LayoutParams param;
    WindowManager.LayoutParams params;

    @Override
    public void onCreate() {
        if (windowManager != null) {
            windowManager.removeViewImmediate(chatHead);
            windowManager.removeViewImmediate(ll);
            stopSelf();
            return;
        }
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        ll = new ImageView(getBaseContext());
        ll.setBackgroundColor(Color.WHITE);
        ll.setAlpha(0.5f);
        chatHead = new ImageView(this);
        chatHead.setImageResource(R.drawable.lock);
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        param = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;
        windowManager.addView(chatHead, params);
        chatHead.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private long start = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        long end = System.currentTimeMillis();
                        if ((end - start) < 700) {
                            windowManager.removeViewImmediate(chatHead);
                            if (added) {
                                windowManager.removeViewImmediate(ll);
                                added = false;
                            }
                            stopSelf();
                            return false;
                        }
                        start = end;
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int x = params.x, y = params.y;
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(chatHead, params);
                        if (added && x < params.x && y < params.y) {
                            windowManager.removeViewImmediate(ll);
                            added = false;
                        }
                        return true;
                }
                return false;
            }
        });
        hndl.sendEmptyMessage(0);
    }
}
