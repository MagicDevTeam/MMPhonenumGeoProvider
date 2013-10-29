package com.magicmod.mmgeoprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

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

    @Override
    public boolean onCreate() {
        //Log.i(TAG, "========== onCreat");
        
        /*
         * 2013-10-29 SunRain
         * TODO:we can save the data to other places like internal storage
         */
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

        if("CN".equals(countryIso)){ //only support CN users atm
            return FilePhonenumDataLoader.getInstance(getContext()).searchGeocode(number);
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
}
