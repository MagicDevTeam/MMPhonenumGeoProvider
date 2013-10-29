package com.magicmod.mmgeoprovider;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;


public class FilePhonenumDataLoader {

    public static final String TAG="FilePhonenumDataLoader";
    
    private static final int BLOCK_SIZE = 8;
    private static final int COUNT = 44399;

    static class PhoneNumberStruct {
        int number;
        short range;
        short cityid;
    }

    private PhoneNumberStruct[] sts = new PhoneNumberStruct[COUNT];

    public void init(final InputStream is) throws IOException {
        try {
            for(int i = 0; i< COUNT; i++){
                byte[] buff = new byte[BLOCK_SIZE];
                is.read(buff);
                DataInputStream dais = new DataInputStream(new ByteArrayInputStream(buff));;
                sts[i] = new PhoneNumberStruct();
                // DONT CHANGE ORDER
                sts[i].number = dais.readInt();
                sts[i].range = dais.readShort();
                sts[i].cityid = dais.readShort();
            }
        }finally {
            is.close();
        }
    }


    private PhoneNumberStruct search(int number){
        Log.i(TAG, "number is "+number);

        PhoneNumberStruct phoneNumberStruct = new PhoneNumberStruct();
        phoneNumberStruct.number = number;

        int index = Arrays.binarySearch(sts, phoneNumberStruct, new Comparator<PhoneNumberStruct>() {
            @Override
            public int compare(PhoneNumberStruct lhs, PhoneNumberStruct rhs) {

                int diff = lhs.number - rhs.number;

                if((rhs.number <= (lhs.number + lhs.range)) && diff < 0){
                    return 0;
                }else
                if((lhs.number <= (rhs.number + rhs.range)) && diff > 0){
                    return 0;
                }else {
                    return diff;
                }
            }
        });

        if(index >= 0)
            return sts[index];
        else
            return null;
    }

    public String searchGeocode(String number){
        try {
            int _number = (int) (Long.parseLong(number) / 10000 - 1300000L);

            PhoneNumberStruct st = search(_number);

            if( st != null ){
                return CityNames.idToName( st.cityid);
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static FilePhonenumDataLoader filePhonenumDataLoader;

    public static FilePhonenumDataLoader getInstance(Context context){
        if (filePhonenumDataLoader == null) {
            filePhonenumDataLoader = new FilePhonenumDataLoader();
            try {
                /*
                 * 2013-10-29 SunRain
                 * TODO:we used the raw data atm, we should move the data file to anyother places later
                 */
                filePhonenumDataLoader.init(context.getResources().openRawResource(R.raw.phonenumber));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return filePhonenumDataLoader;
    }

    /*
    public static void main(String[] args) throws IOException {


        FilePhonenumDataLoader filePhonenumDataLoader = new FilePhonenumDataLoader();
        FileInputStream file = new FileInputStream(new File("/tmp/out.b"));
        filePhonenumDataLoader.init(file);

        System.out.println(filePhonenumDataLoader.searchGeocode("18910228396"));

//        PhoneNumberStruct st = filePhonenumDataLoader.sts[44];
//        st = filePhonenumDataLoader.search(171);
//        System.out.println(
//                        st.number + 1300000
//        );
//        System.out.println(
//                CityNames.idToName(
//        st.cityid)
//                );

    }*/
}
