//package com.example.work.triageapp2.AppGraphic;
//
//import android.content.Context;
//import android.content.Intent;
//import android.util.AttributeSet;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//
///**
// * Created by BoryS on 09.10.2017.
// */
//
//public class MainActivityDrawingView extends SurfaceView implements SurfaceHolder.Callback {
//
//    private SurfaceHolder holder;
//    private AnimThread animThread;
//
//    public MainActivityDrawingView(Context context){
//        super(context);
//        holder = getHolder();
//        holder.addCallback(this);
//    }
//
//    public MainActivityDrawingView(Context context, AttributeSet attrs){
//        super(context,attrs);
//        holder = getHolder();
//        holder.addCallback(this);
//    }
//
//    public MainActivityDrawingView(Context context, AttributeSet attrs, int defStyle){
//        super(context, attrs, defStyle);
//        holder = getHolder();
//        holder.addCallback(this);
//    }
//
//
//    @Override
//    public void surfaceCreated(SurfaceHolder surfaceHolder) {
//        animThread = new AnimThread(getContext(), this,holder, this.getWidth(), this.getHeight());
//        animThread.setRunning(true);
//        animThread.start();
//
//        final Intent intent = new Intent("SURFACE_CREATED");
//        getContext().sendBroadcast(intent);
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
//       // Log.i(this.getClass().getName(),"surfaceChanged function has been called");
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
//        boolean retry = true;
//        animThread.setRunning(false);
//        while(retry){
//            try{
//                animThread.join();
//                retry = false;
//            }catch(InterruptedException e){
//
//            }
//        }
//
//        final Intent intent = new Intent("SURFACE_DESTROYED");
//        getContext().sendBroadcast(intent);
//
//       // Log.i(this.getClass().getName(),"surfaceView has been destroyed");
//    }
//
//    public AnimThread getAnimThread() {
//        return animThread;
//    }
//
//    public void setIsTriageScreenVisible(boolean b){   animThread.setTriageScreenVisibleAndSurfaceViewVisibility(b);    }
//    public boolean isTriageScreenVisible() {    return animThread.isTriageScreenVisible(); }
//}
//
