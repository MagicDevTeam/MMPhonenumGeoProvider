MagicMod CN PhonenumGeoProvider

Usage
-----

    Cursor query = contentResolver.query(Uri.parse("content://com.magicmod.mmgeoprovider/CN/15110111111"), null, null, null, null);
    query.moveToFirst();
    
    String x = query.getString(0);
    query.close();
