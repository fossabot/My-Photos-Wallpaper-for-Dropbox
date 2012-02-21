package uk.co.rnwood.dropboxlivewallpaper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EventListener;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ImageQueue {

    public interface OnImageAddedHandler
    {
        void OnImageAdded();
    }

    public interface OnImageRemovedHandler
    {
        void OnImageRemoved();
    }

    public ImageQueue(File directory, OnImageAddedHandler imageAddedHandler, OnImageRemovedHandler imageRemovedHandler) {
        this.directory = directory;
        this.imageAddedHandler = imageAddedHandler;
        this.imageRemovedHandler = imageRemovedHandler;
        
        if (!directory.exists())
        {
            directory.mkdir();
        }                     else {
            for(File file : directory.listFiles())
            {
                files.offer(file);
            }
        }
    }

    private File directory;
    private ConcurrentLinkedQueue<File> files = new ConcurrentLinkedQueue<File>();
    private OnImageAddedHandler imageAddedHandler;
    private OnImageRemovedHandler imageRemovedHandler;

    
    public void push(Bitmap bitmap) throws IOException
    {
        File file = new File(directory.getPath() + "/" + UUID.randomUUID().toString() + ".jpg");
        
        FileOutputStream outputStream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        outputStream.close();

        files.offer(file);

        if (imageAddedHandler != null)
        {
            imageAddedHandler.OnImageAdded();
        }
    }

    
    public Bitmap pop() throws IOException
    {
        File file = files.poll();
        Bitmap result=null;

        if (file != null)
        {
            result = BitmapFactory.decodeFile(file.getPath());
            file.delete();

            if (imageRemovedHandler != null)
            {
                imageRemovedHandler.OnImageRemoved();
            }
        }

        return result;
    }

    public int size() {
        return files.size();
    }
}
