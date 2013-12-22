package com.magicmod.mmgeoprovider;

import android.R.integer;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;


public class FilePhonenumDataLoader {

    public static final String TAG="FilePhonenumDataLoader";
    
    static SQLiteDatabase mDB;

    //use a cached date to save time
    private static String mCachedPhoneNum,mCachedDateString;
    
    private static final int TYPE_MOBILE = 0;
    private static final int TYPE_GUHUA = 1;
    private static final int TYPE_OTHER = 2;
    private static final int TYPE_GLOBAL_PHONE = 3;
    
    private static int mNumType;
    
    private static final String[][] PHONE_TYPE_INFO = {
        {"100","移动"},
        {"200","联通"},
        {"300","电信"},
        {"400","固话"},
        {"500","其他"},
    };
    
    /* 573 12345678 11
     * 573 1234567 10
     * 21 12345678  10
     * 4008888400 10 
     * 21 1234567  9
     * 13800138000 11
     * 
     */
    public String searchGeocode(String number){
        if (number.equals(mCachedPhoneNum)) {
            return mCachedDateString;
        }
        mCachedPhoneNum = number;

        String s = adjustNumPreFix(number);
        int length = s.length();

        // TODO: 需要正确判断字符串的长度来区分号码类型
        switch (length) {
            case 9: {// 固话
                s = s.substring(0, 2);
                mNumType = TYPE_GUHUA;
            }
                break;
            /*
             * 当字符串长度为10时 由于prefix以后，地区编号为2位的只有1或者2开头的，所以根据字符串开头进行分别判断
             * 同时，10个字符串特殊号码目前只知道400和800开头的号码， 但是固定电话的区号划分里面，并没有400或者800开头的号码，
             * 故这里可以单独考虑，也可以直接跳到下一步
             */
            case 10: {
                if (s.startsWith("1") || s.startsWith("2")) {
                    s = s.substring(0, 2);
                    mNumType = TYPE_GUHUA;
                } else if (!s.startsWith("400") && !s.startsWith("800")) {
                    s = s.substring(0, 3);
                    mNumType = TYPE_GUHUA;
                } else {
                    mNumType = TYPE_OTHER;
                }
            }
                break;
            /*
             * 11位长度的只有1开头的手机和非1开头的固话
             */
            case 11: {
                if (s.startsWith("1")) {
                    s = s.substring(0, 7);
                    mNumType = TYPE_MOBILE;
                } else {
                    s = s.substring(0, 3);
                    mNumType = TYPE_GUHUA;
                }
            }
                break;
            default:
                mNumType = TYPE_OTHER;
                break;
        }

        if (mNumType == TYPE_OTHER) {
            mCachedDateString = CommonNumData.getInfo(number);
        } else {
            mCachedDateString = getDateFromSQL(s);
        }
        return (mCachedDateString==null) ? null : mCachedDateString;
    }

    private String getDateFromSQL(String preFixedNumber) {

        mDB = SQLiteDatabase.openDatabase(MMPhonenumGeoProvider.CN_DB_FILE, null, SQLiteDatabase.OPEN_READONLY);
        if (!mDB.isOpen()){
            Log.e(TAG, "==== open DB error");
            mDB = null;
            return null;
        }
        
        String date[] = null;
        String s = null;
        String cmd = String.format("select DataIndex from tablenumindex where ID=%s", preFixedNumber);
        Cursor c = mDB.rawQuery(cmd, null);
        if (c.moveToFirst()) {
            s = c.getString(0);
            c.close();
            if (!s.isEmpty() && (s != null)) {
                date = s.split("-");
            } else {
                mDB.close();
                return null;
            }
        } else {
            if (!c.isClosed())
                c.close();
            if (mDB.isOpen())
                mDB.close();
            return null;
        }
        cmd = String.format("select DataValue from tabledataindex where ID=%s", date[0]);
        c = mDB.rawQuery(cmd, null);
        if (c.moveToFirst()){
            s = c.getString(0);
            c.close();
            if (s.isEmpty() || s == null || s.equals("")) {
                mDB.close();
                return null;
            }
        }
        mDB.close();

        /*
         * 移动 100
         * 联通 200
         * 电信 300
         * 固话 400
         * 其他 500
         */
        for (int i = 0; i < PHONE_TYPE_INFO.length; i++) {
            if ( PHONE_TYPE_INFO[i][0].equals(date[1])){
                date[1] = PHONE_TYPE_INFO[i][1];
                break;
            }
        }
        return s + date[1];
    }

    private static FilePhonenumDataLoader filePhonenumDataLoader;

    public static FilePhonenumDataLoader getInstance(Context context){

        if (filePhonenumDataLoader == null) {
            filePhonenumDataLoader = new FilePhonenumDataLoader();
            
        }
        return filePhonenumDataLoader;
    }

    /* 
     * 0573 12345678 12
     * 0573 1234567 11
     * 021 85908183  11
     * 013800138000 12
     * +86 xxx
     * 
     */    
    private static String adjustNumPreFix(String num) {
        String s = num;
        /*
         * 去除空格和多余字符
         */
        s = s.replaceAll(" ", "");
        s = s.replaceAll("-", "");
        /*
         * 检测国际代码，如果不是+86开头的则直接返回原字符串
         */
        if (s.startsWith("+86")) {
            s = s.substring(3, s.length());
            /*
             * 去处字符串开头的0
             */
            for (;;) {
                if (s.startsWith("0")) {
                    s = s.substring(1, s.length());
                } else {
                    break;
                }
            }
        }
        return s;
    }
}
