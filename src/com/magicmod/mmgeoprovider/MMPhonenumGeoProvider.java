package com.magicmod.mmgeoprovider;

import android.R.integer;
import android.R.string;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class MMPhonenumGeoProvider extends ContentProvider {

    private static final String TAG = "PhonenumGeoProvider";

    private static final String[] COLUMN_NAMES = new String[]{"GEOCODE"};
    
    private static final UriMatcher MATCHER = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        MATCHER.addURI("com.magicmod.mmgeoprovider", "CN", 1);
        MATCHER.addURI("com.magicmod.mmgeoprovider", "CN/#", 2);
    }

    public static final String DB_PATH = Environment.getExternalStorageDirectory() + "/MagicMod/GeoDB";
    public static final String CN_DB_PATH = DB_PATH + "/CN";
    public static final String CN_DB_FILE = CN_DB_PATH + "/LocDB";
    
    public static boolean mDbCopyFlag = false;
    
    private Context mContext;
    
    @Override
    public boolean onCreate() {

        mContext = this.getContext();
        
        //Copy the datebase to sdcard
        File dir = new File(CN_DB_PATH);
        if (!dir.exists()){
            dir.mkdirs();
        }
        
        File file = new File(CN_DB_FILE);
        if (!file.exists()){
            mDbCopyFlag = true;
            DbCopyTask task = new DbCopyTask();
            task.execute("");
        }
        
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        List<String> paths = uri.getPathSegments();
        if(paths !=null && paths.size() >= 2){
            String countryIso = paths.get(0);
            String number = paths.get(1);
            Cursor cursor = wrapGeoCodeToCursor(searchGeoCode(countryIso, number));
            return cursor;
        }else {
            return null;
        }
    }

    private Cursor wrapGeoCodeToCursor(String geocode){

        if( TextUtils.isEmpty(geocode) ){
            return null;
        }

        MatrixCursor cursor = new MatrixCursor(COLUMN_NAMES);
        cursor.addRow(new String[]{geocode});
        return cursor;
    }

    private String searchGeoCode(String countryIso,String number){

        if (!mDbCopyFlag) {
            if ("CN".equals(countryIso)) { // only support CN users atm
                return FilePhonenumDataLoader.getInstance(getContext()).searchGeocode(number);
            }
        }
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
    
    private class DbCopyTask extends AsyncTask<String, Integer, String> {


        @Override
        protected String doInBackground(String... params) {
            
            Log.i(TAG, "==== Start copy files ===");
            
            AssetManager am = mContext.getAssets();
            File file = new File(CN_DB_FILE);
            try {
                InputStream ins = am.open("LocDB");
                FileOutputStream fos = new FileOutputStream(file);
                int date = ins.read();
                while (date != -1) {
                    fos.write(date);
                    date = ins.read();
                }
                fos.close();
                ins.close();
                am.close();
            } catch (Exception e) {
                // TODO: handle exception
            }
            if (file.exists()) {
                mDbCopyFlag = false;
            }
            return null;
        }
    }
}
