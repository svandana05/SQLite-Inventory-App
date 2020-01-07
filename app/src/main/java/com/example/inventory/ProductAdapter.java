package com.example.inventory;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.inventory.data.InventoryContract.InventoryEntry;

import java.io.InputStream;

public class ProductAdapter extends CursorAdapter {


    public ProductAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        TextView tvName = (TextView) view.findViewById(R.id.name);
        TextView tvPrice = (TextView) view.findViewById(R.id.price);
        TextView tvQuantity = (TextView) view.findViewById(R.id.quantity);

        //String image = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_IMAGE));
        String name = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_NAME));
        int price = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE));
        int quantity = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY));
        byte[] imageByte = cursor.getBlob(cursor.getColumnIndex(InventoryEntry.COLUMN_IMAGE));

        if (imageByte != null){
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length));
        }
        tvName.setText(name);
        tvPrice.setText(String.valueOf(price) + "$ for each");
        tvQuantity.setText("Quantity:" + String.valueOf(quantity));
    }


}
