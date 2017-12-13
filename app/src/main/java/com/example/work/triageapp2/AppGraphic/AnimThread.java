package com.example.work.triageapp2.AppGraphic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceHolder;

import com.example.work.triageapp2.R;
import com.example.work.triageapp2.SoldierParameter;

/**
 * Created by BoryS on 09.10.2017.
 */

public class AnimThread extends Thread {

    private int width, height;

    private SurfaceHolder holder;
    private Context context;
    private Paint stringPaint,linePaint, t1Paint, blackStringPaint;
    TimerThreadForHeartRate timerThreadForHeartRate;

    private boolean isTriageScreenVisible = true;
    private boolean isDrawingFinished = false;
    private boolean running = true;



    private boolean isHeartShouldBeDrawn;
    int i = 0;
    int backgroundColor,darkKhakiColor,t1Color;


    public AnimThread(Context context, SurfaceHolder holder, int width, int height){
        this.holder = holder;
        this.context = context;
        this.width = width;
        this.height = height;
        timerThreadForHeartRate = new TimerThreadForHeartRate(this);
        timerThreadForHeartRate.start();
        setupPaintAndColors();
        Log.i(this.getClass().getName(),"AnimThread constructor has been called");
    }

    private void setupPaintAndColors(){
        backgroundColor = ContextCompat.getColor(context, R.color.colorKhaki);
        darkKhakiColor = ContextCompat.getColor(context, R.color.darkKhakiColor);
        t1Color = ContextCompat.getColor(context,R.color.t1Color);

        stringPaint = new Paint();
        stringPaint.setColor(Color.WHITE);
        stringPaint.setTextSize(35);

        linePaint = new Paint();
        linePaint.setColor(darkKhakiColor);
        linePaint.setAntiAlias(true);

        t1Paint = new Paint();
        t1Paint.setColor(t1Color);
        t1Paint.setStyle(Paint.Style.FILL);

        blackStringPaint = new Paint();
        blackStringPaint.setColor(Color.BLACK);
        blackStringPaint.setTextSize(55);

    }

    @Override
    public void run() {

        while(running){
            Canvas canvas = null;
            try{
                canvas = holder.lockCanvas();
                if(canvas != null) {
                    synchronized (holder) {
                        if (isTriageScreenVisible == true) {
                            canvas.drawColor(backgroundColor);

                            canvas.drawRect(0, 0, width, 380, t1Paint);
                            canvas.drawText("T1 - Immediate Treatment", 50, 200, blackStringPaint);
                            //canvas.drawLine(0,400,width,401,linePaint);
                            canvas.drawLine(0, 550, width, 551, linePaint);
                            canvas.drawLine(0, 700, width, 701, linePaint);
                            canvas.drawText("Heart rate is: " + SoldierParameter.heartRate, 300, 480, stringPaint);
                            drawHeart(canvas);

                        } else {
                            canvas.drawColor(backgroundColor);
                        }

                    }
                }
            }
            finally {
                if(canvas != null){
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
        isDrawingFinished = true;
    }


    public void drawHeart(Canvas canvas){
        if(SoldierParameter.isHeartRateActive){
            if(isHeartShouldBeDrawn()) {
                Drawable d = context.getResources().getDrawable(R.drawable.green_heart2);
                setDrawableBounds(d, 50, 400, 150, 150);
                d.draw(canvas);
            }
        }else{
            Drawable d = context.getResources().getDrawable(R.mipmap.green_heart_rate_disable);
            setDrawableBounds(d, 50, 400, 150, 150);
            d.draw(canvas);
        }
    }

    public void setDrawableBounds(Drawable d, int posX, int posY, int width, int height){
        d.setBounds(posX,posY,posX + width,posY + height);
    }

    public boolean isHeartShouldBeDrawn() {
        return isHeartShouldBeDrawn;
    }

    public void setHeartShouldBeDrawn(boolean heartShouldBeDrawn) {
        isHeartShouldBeDrawn = heartShouldBeDrawn;
    }
    public void setTriageScreenVisible(boolean b) {  isTriageScreenVisible = b; }
    public boolean isTriageScreenVisible() {    return isTriageScreenVisible;  }
    public boolean isDrawingFinished() {    return isDrawingFinished;  }


    public void setRunning(boolean b){  running = b;    }
}
