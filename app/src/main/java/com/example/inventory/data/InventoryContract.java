package com.example.inventory.data;

import android.net.Uri;
import android.provider.BaseColumns;

public final class InventoryContract {
    private InventoryContract(){}

    public static final String CONTENT_AUTHORITY = "com.example.inventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH = "inventory";

    public static abstract class InventoryEntry implements BaseColumns{
        public static final String TABLE_NAME = "inventory";


        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH);

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_SUPPLIER = "supplier";
        public static final String COLUMN_IMAGE = "image";

        public static final int SUPPLIER_0 = 0;
        public static final int SUPPLIER_1 = 1;
        public static final int SUPPLIER_2 = 2;
        public static final int SUPPLIER_3 = 3;
        public static final int SUPPLIER_4 = 4;
    }

}
