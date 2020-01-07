package com.example.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.inventory.data.InventoryContract.InventoryEntry;

public class InventoryProvider extends ContentProvider {
    private static final int INVENTORY = 1;
    private static final int INVENTORY_ID = 2;

    InventoryDbHelper dbHelper;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH, INVENTORY);
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH +"/#",INVENTORY_ID);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor;
        int match = uriMatcher.match(uri);
        switch (match){
            case INVENTORY:
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );
                break;
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );
                break;
                default:
                    throw new IllegalArgumentException("Can not query unknown uri"+ uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = uriMatcher.match(uri);
        switch (match){
            case INVENTORY:
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException("Can not insert.");
        }
    }

    private Uri insertProduct(Uri uri, ContentValues contentValues ){
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        long id;
        id = database.insert(InventoryEntry.TABLE_NAME, null, contentValues);
        if (id == -1) {
            Log.e("InventoryProvider", "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int rowDeleted;
        final int match = uriMatcher.match(uri);

        switch (match){
            case INVENTORY:
                rowDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                if (rowDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowDeleted;
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                if (rowDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowDeleted;
                default:
                    throw new IllegalArgumentException("Deletion is not done for uri: "+ uri);
        }

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int match = uriMatcher.match(uri);
        switch (match){
            case INVENTORY:
                return updateProduct(InventoryEntry.CONTENT_URI, values, selection, selectionArgs);
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(InventoryEntry.CONTENT_URI, values, selection, selectionArgs);
        }
        return 0;
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int rowUpdated = database.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowUpdated;
    }
}
