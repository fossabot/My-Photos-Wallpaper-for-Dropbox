/*
 * Copyright (c) 2012. Robert Wood <rob@rnwood.co.uk>
 * All rights reserved.
 */

package uk.co.rnwood.dropboxlivewallpaper;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxServerException;

import java.io.InputStream;
import java.util.*;

public class ImageFetcher {


    public ImageFetcher() {

        random = new Random();
    }

    private Random random;

    private Map<String, DropboxAPI.Entry> metaDataCache = new HashMap<String, DropboxAPI.Entry>();

    private DropboxAPI.Entry getDirectoryInfo(DropboxAPI<AndroidAuthSession> api, String path) throws DropboxException {
        DropboxAPI.Entry result = null;
        String cacheHash = null;

        if (metaDataCache.containsKey(path)) {
            result = metaDataCache.get(path);
            cacheHash = result.hash;
        }

        try {
            result = api.metadata(path, 0, cacheHash, true, null);

            metaDataCache.put(path, result);

            if (cacheHash != null) {
                metaDataCache.remove(cacheHash);
            }

        } catch (DropboxServerException ex) {
            if (ex.error != 304) {
                throw ex;
            }
        }

        return result;
    }

    public Bitmap fetchNextImage(AndroidAuthSession authSession, String[] searchFolders, int width, int height)
            throws DropboxException {
        DropboxAPI<AndroidAuthSession> dropboxAPI = new DropboxAPI<AndroidAuthSession>(authSession);

        Vector<String> subFolderNames = new Vector<String>();

        for (DropboxAPI.Entry subEntry : getDirectoryInfo(dropboxAPI, "/Photos").contents) {
            if (subEntry.isDir) {
                if ((searchFolders.length == 1 && searchFolders[0].equals("*")) || Arrays.asList(searchFolders).contains(subEntry.fileName())) {
                    subFolderNames.add(subEntry.fileName());
                }
            }
        }

        while (true) {

            if (subFolderNames.size() == 0)
                return null;

            DropboxAPI.Entry photosFolder = getDirectoryInfo(dropboxAPI, "/Photos/" + subFolderNames.get(subFolderNames.size() == 1 ? 0 : random.nextInt(subFolderNames.size())));

            List<DropboxAPI.Entry> photos = findPhotos(dropboxAPI, photosFolder);

            if (photos.size() == 0) {
                subFolderNames.remove(photosFolder.fileName());
            } else {

                DropboxAPI.Entry photo = photos.get(photos.size() == 1 ? 0 : random.nextInt(photos.size()));

                InputStream stream = dropboxAPI.getThumbnailStream(photo.path, getDropBoxThumbsize(width, height), DropboxAPI.ThumbFormat.JPEG);
                return BitmapFactory.decodeStream(stream);
            }
        }
    }

    private List<DropboxAPI.Entry> findPhotos(DropboxAPI<AndroidAuthSession> api, DropboxAPI.Entry photosFolder) throws DropboxException {
        List<DropboxAPI.Entry> photos = new Vector<DropboxAPI.Entry>();
        findPhotos(api, photosFolder, photos);
        return photos;
    }

    private void findPhotos(DropboxAPI<AndroidAuthSession> api, DropboxAPI.Entry photosFolder, List<DropboxAPI.Entry> photos) throws DropboxException {
        for (DropboxAPI.Entry file : photosFolder.contents) {
            if (!file.isDir && file.mimeType.equals("image/jpeg")) {
                photos.add(file);
            } else if (file.isDir) {
                DropboxAPI.Entry subDirEntry = getDirectoryInfo(api, file.path);
                findPhotos(api, subDirEntry, photos);
            }
        }
    }

    private DropboxAPI.ThumbSize getDropBoxThumbsize(int minWidth, int minHeight) {

        int minDimension = Math.min(minWidth, minHeight);

        if (minDimension <= 240) {
            return DropboxAPI.ThumbSize.BESTFIT_320x240;
        }

        if (minDimension <= 320) {
            return DropboxAPI.ThumbSize.BESTFIT_480x320;
        }

        if (minDimension <= 480) {
            return DropboxAPI.ThumbSize.BESTFIT_640x480;
        }

        if (minDimension <= 640) {
            return DropboxAPI.ThumbSize.BESTFIT_960x640;
        }

        return DropboxAPI.ThumbSize.BESTFIT_1024x768;
    }
}
