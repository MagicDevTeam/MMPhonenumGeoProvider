MagicMod CN PhonenumGeoProvider

Usage
-----

    Cursor query = contentResolver.query(Uri.parse("content://com.android.i18n.phonenumbers.geocoding/CN/15110111111"), null, null, null, null);
    query.moveToFirst();
    
    String x = query.getString(0);
