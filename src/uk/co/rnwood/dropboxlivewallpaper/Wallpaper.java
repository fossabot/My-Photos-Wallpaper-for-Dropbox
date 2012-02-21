/*
 * Copyright (c) 2012. Robert Wood <rob@rnwood.co.uk>
 * All rights reserved.
 */

package uk.co.rnwood.dropboxlivewallpaper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.*;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

import static android.view.GestureDetector.SimpleOnGestureListener;

public class Wallpaper extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new WallpaperEngine(this);
    }

    public static final String DROPBOX_APPKEY = "r79kug7kqybcg7l";
    public static final String DROPBOX_APPSECRET = "w9dcryx8is9mcc5";


    public class WallpaperEngine extends WallpaperService.Engine implements SharedPreferences.OnSharedPreferenceChangeListener {

        private WindowManager windowManager;
        private NotificationManager notificationManager;
        private ConnectivityManager connectivityManager;
        private BroadcastReceiver broadcastReceiver;
        private Notification authRequiredNotification;
        private final int AUTHREQUIRED_NOTIFICATIONID = 1;
        private Handler uiThreadHandler;
        private Thread downloadThread;
        private GestureDetector gestureDetector;

        public WallpaperEngine(Wallpaper service) {
            context = service;
            imageFetcher = new ImageFetcher();
            uiThreadHandler = new Handler();
            imageQueue = new ImageQueue(new File(getFilesDir().getPath() + "/images"), new ImageQueue.OnImageAddedHandler() {
                @Override
                public void OnImageAdded() {

                    Log.v(TAG, "Image added to queue, notifying UI thread");

                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            render();
                        }
                    });
                }
            }, new ImageQueue.OnImageRemovedHandler() {
                @Override
                public void OnImageRemoved() {
                    Log.v(TAG, "Image removed from queue, notifying download thread");
                    notifyDownloadThread();
                }
            }
            );

            preferences = new Preferences(service);
            notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            broadcastReceiver = new BroadcastReceiver();

            authRequiredNotification = new Notification(R.drawable.icon, context.getResources().getString(R.string.notification_authrequired), 0);
            PendingIntent authIntent = PendingIntent.getActivity(context, 0, new Intent("uk.co.rnwood.dropboxlivewallpaper.AUTHORIZEDROPBOXACCOUNTNOTIFICATION"), 0);
            authRequiredNotification.setLatestEventInfo(context, context.getString(R.string.app_name), context.getString(R.string.notification_authrequired), authIntent);

            downloadThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    downloadThreadWork();
                }
            });
            downloadThread.start();

            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(broadcastReceiver, filter);

            preferences.registerOnSharedPreferenceChangeListener(this);

            gestureDetector = new GestureDetector(new SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.v(TAG, "Double tap - moving to next image if possible");
                    preferences.SetLastImageChange(0);
                    render();
                    return true;
                }
            });
            setTouchEventsEnabled(true);
        }

        private Context context;

        @Override
        public void onTouchEvent(MotionEvent event) {
            gestureDetector.onTouchEvent(event);
        }

        class BroadcastReceiver extends android.content.BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

                    if (activeNetwork != null && activeNetwork.getState() == NetworkInfo.State.CONNECTED) {
                        Log.v(TAG, "Network connected - notifying download thread");
                        notifyDownloadThread();
                    }
                }
            }
        }


        private final String TAG = WallpaperEngine.class.getName();

        private void downloadThreadWork() {

            Log.v(TAG, "Download thread running");

            while (true) {
                while (imageQueue.size() < 5) {
                    Log.v(TAG, imageQueue.size() + " images in queue. Downloading next image.");

                    if (!tryGetNextImage()) {
                        //Give up
                        Log.v(TAG, "Error downloading images - giving up");
                        break;
                    }
                }

                Log.v(TAG, "Stopping");

                try {
                    synchronized (Thread.currentThread()) {
                        Log.v(TAG, "Sleeping until there's something to do");
                        Thread.currentThread().wait();
                        Log.v(TAG, "Woken up");
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        private boolean tryGetNextImage() {
            Log.v(TAG, "Attempting to get next image");

            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if (activeNetwork == null || !activeNetwork.isConnected()) {
                Log.v(TAG, "No active network connection");
                return false;
            }

            if (preferences.GetOnlyDownloadOnWifi()) {
                if (activeNetwork.getType() != ConnectivityManager.TYPE_WIFI) {
                    Log.v(TAG, "No active WiFi connection (WiFi only setting is enabled)");
                    return false;
                }
            }

            AndroidAuthSession authSession = new AndroidAuthSession(new AppKeyPair(Wallpaper.DROPBOX_APPKEY, Wallpaper.DROPBOX_APPSECRET), Session.AccessType.DROPBOX);

            String authKey = preferences.GetAuthKey();
            String authSecret = preferences.GetAuthSecret();

            if (authKey == null || authSecret == null) {
                Log.v(TAG, "Don't have Dropbox credentials. Giving up and notifying user");
                notificationManager.notify(AUTHREQUIRED_NOTIFICATIONID, authRequiredNotification);
                return false;

            } else {
                authSession.setAccessTokenPair(new AccessTokenPair(authKey, authSecret));
            }


            try {
                Display display = windowManager.getDefaultDisplay();
                int width = display.getWidth();
                int height = display.getHeight();

                if (preferences.GetLowQualityImages()) {
                    width = width / 2;
                    height = height / 2;
                }

                Bitmap image = imageFetcher.fetchNextImage(authSession, preferences.GetFolders(), width, height);

                if (image == null) {
                    return false;
                }

                imageQueue.push(image);
                image.recycle();

                return true;
            } catch (DropboxUnlinkedException ex) {
                Log.e(TAG, "Got DropBoxUnlinked exception - notifying user", ex);
                notificationManager.notify(AUTHREQUIRED_NOTIFICATIONID, authRequiredNotification);
                return false;
            } catch (DropboxException ex) {
                Log.e(TAG, "Got DropBox exception", ex);
                return false;
            } catch (IOException ex) {
                Log.e(TAG, "Got IO exception", ex);
                return false;
            }
        }


        private void notifyDownloadThread() {
            synchronized (downloadThread) {
                Log.v(TAG, "Trying to wake up download thread");
                downloadThread.notify();
            }
        }


        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

            if (s.equals(Preferences.KEY_DROPBOXAUTHKEY)) {
                Log.v(TAG, "Dropbox credentials changed. Notifying download thread.");
                notificationManager.cancel(AUTHREQUIRED_NOTIFICATIONID);
                notifyDownloadThread();
            }
        }


        @Override
        public void onDestroy() {
            Log.v(TAG, "Being destroyed");

            super.onDestroy();

            Log.v(TAG, "Unregistering listeners");
            context.unregisterReceiver(broadcastReceiver);
            preferences.unregisterOnSharedPreferenceChangeListener(this);

            Log.v(TAG, "Stopping download thread");
            downloadThread.interrupt();
            try {
                downloadThread.join();
            } catch (InterruptedException ex) {

            }
            Log.v(TAG, "Download thread exited");
        }


        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                Log.v(TAG, "Visible");
            } else {
                Log.v(TAG, "Not Visible");
            }
            render();
        }

        private ImageFetcher imageFetcher;
        private Preferences preferences;
        private ImageQueue imageQueue;

        private Bitmap currentImage;

        private void render() {
            Log.v(TAG, "Beginning render");
            if (!isVisible()) {
                Log.v(TAG, "Not visible - not rendering");
                return;
            }

            if (isPreview()) {
                Log.v(TAG, "Rendering preview image");
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.preview);
                render(bitmap);
                return;
            }

            long nextImageDate = preferences.GetLastImageChange() + (preferences.GetFrequency() * 1000);
            long now = new Date().getTime();

            Log.v(TAG, "Checking if image needs to be changed:  now: " + now + " nextimage:" + nextImageDate);
            if (currentImage == null || now >= nextImageDate) {
                Log.v(TAG, "Image needs changing");

                try {
                    Bitmap nextImage = imageQueue.pop();
                    if (nextImage != null) {
                        Log.v(TAG, "Got next image - changing current image");
                        Bitmap lastImage = currentImage;
                        currentImage = nextImage;
                        preferences.SetLastImageChange(now);
                        if (lastImage != null) {
                            lastImage.recycle();
                        }

                    } else {
                        Log.v(TAG, "Next image not available - keeping current image for now");
                    }
                } catch (IOException ex) {

                }
            } else {
                Log.v(TAG, "Image change not needed");
            }

            render(currentImage);
        }

        private void render(Bitmap image) {

            SurfaceHolder surfaceHolder = getSurfaceHolder();

            Canvas canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(preferences.GetBackgroundColour());

            if (image != null) {

                int screenWidth = canvas.getWidth();
                int screenHeight = canvas.getHeight();

                if (preferences.GetScaleMode() == Preferences.ScaleMode.Scale) {

                    float scaleFactor = Math.min(screenWidth / ((float) image.getWidth()), screenHeight / ((float) image.getHeight()));
                    Matrix scale = new Matrix();
                    scale.postScale(scaleFactor, scaleFactor);
                    image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), scale, false);
                } else {

                    float scaleFactor = Math.max(screenWidth / ((float) image.getWidth()), screenHeight / ((float) image.getHeight()));
                    Matrix scale = new Matrix();
                    scale.postScale(scaleFactor, scaleFactor);
                    image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), scale, false);
                }

                Paint imagePaint = new Paint();
                imagePaint.setAntiAlias(true);

                image = applyImageEffects(canvas, image, imagePaint);

                int renderX = (screenWidth / 2) - (image.getWidth() / 2);
                int renderY = (screenHeight / 2) - (image.getHeight() / 2);

                canvas.drawBitmap(image, renderX, renderY, imagePaint);
            }

            surfaceHolder.unlockCanvasAndPost(canvas);
        }

        private Random random = new Random();

        private Bitmap applyImageEffects(Canvas canvas, Bitmap image, Paint imagePaint) {

            if (preferences.GetEffectGrayscale()
                    || preferences.GetEffectSepia()
                    || preferences.GetEffectTransparent()
                    ) {
                ColorMatrix cm = new ColorMatrix();

                if (preferences.GetEffectGrayscale() || preferences.GetEffectSepia()) {
                    cm.setSaturation(0);
                }

                if (preferences.GetEffectSepia() || preferences.GetEffectTransparent()) {
                    final ColorMatrix matrixB = new ColorMatrix();


                    float[] matrix = new float[]{
                            1, 0, 0, 0, 0,
                            0, 1, 0, 0, 0,
                            0, 0, 1, 0, 0,
                            0, 0, 0, 1, 0
                    };


                    if (preferences.GetEffectSepia()) {
                        int index = 0;
                        for (float replacement :
                                new float[]{0.3930000066757202f, 0.7689999938011169f, 0.1889999955892563f, 0, 0, 0.3490000069141388f, 0.6859999895095825f, 0.1679999977350235f, 0, 0, 0.2720000147819519f, 0.5339999794960022f, 0.1309999972581863f, 0, 0}) {
                            matrix[index] = replacement;
                            index++;
                        }

                    }

                    if (preferences.GetEffectTransparent()) {
                        matrix[18] = 0.3f;
                    }

                    matrixB.set(matrix);
                    cm.setConcat(matrixB, cm);
                }


                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);
                imagePaint.setColorFilter(filter);
            }

            if (preferences.GetEffectBlurEdges()) {
                imagePaint.setMaskFilter(new BlurMaskFilter(30, BlurMaskFilter.Blur.NORMAL));
            }

            if (preferences.GetEffectInstantPhoto()) {
                float scaleFactor = 0.8F;
                Matrix scale = new Matrix();
                scale.postScale(scaleFactor, scaleFactor);
                image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), scale, false);

                int renderX = (canvas.getWidth() / 2) - (image.getWidth() / 2);
                int renderY = (canvas.getHeight() / 2) - (image.getHeight() / 2);

                canvas.rotate(random.nextInt(20) - 10, canvas.getWidth() / 2, canvas.getHeight() / 2);

                Paint rectPaint = new Paint();
                rectPaint.setAntiAlias(true);
                rectPaint.setStyle(Paint.Style.FILL);
                rectPaint.setColor(Color.argb(255, 245, 245, 245));
                rectPaint.setShadowLayer(15f, 0f, 0f, Color.GRAY);

                canvas.drawRect(new RectF(renderX - 20, renderY - 20, renderX + image.getWidth() + 20, renderY + image.getHeight() + 50), rectPaint);

            }

            return image;
        }


        @Override
        public void onDesiredSizeChanged(int desiredWidth, int desiredHeight) {
            super.onDesiredSizeChanged(desiredWidth, desiredHeight);

            Log.v(TAG, "Size changed");
            render();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);    //To change body of overridden methods use File | Settings | File Templates.

            Log.v(TAG, "Surface changed");
            render();
        }
    }


}
