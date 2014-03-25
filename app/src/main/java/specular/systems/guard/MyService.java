package specular.systems.guard;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MyService extends Service {
    public MyService() {
    }
    int a = 0;
    Handler hndl = new Handler(){
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
            },1000);
            if(a>15){
                a=0;
                if(!added) {
                    windowManager.addView(ll, new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT));
                    added = true;
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
    LinearLayout ll;
    @Override
    public void onCreate() {
        if(windowManager!=null){
            windowManager.removeView(chatHead);
            windowManager.removeView(ll);
            stopSelf();
            return;
        }
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        ll = new LinearLayout(getBaseContext());
        ll.setBackgroundColor(Color.WHITE);
        ll.setAlpha(0.5f);
        chatHead = new ImageView(this);
        chatHead.setImageResource(R.drawable.lock);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
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
            private long start=0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        long end = System.currentTimeMillis();
                        if((end-start)<700){
                            windowManager.removeView(chatHead);
                            if(added)
                                windowManager.removeView(ll);
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
                        int x=params.x,y=params.y;
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(chatHead, params);
                        if(added&&x<params.x&&y<params.y){
                            added= false;
                            windowManager.removeView(ll);
                        }
                        return true;
                }
                return false;
            }
        });
        hndl.sendEmptyMessage(0);
    }
}
