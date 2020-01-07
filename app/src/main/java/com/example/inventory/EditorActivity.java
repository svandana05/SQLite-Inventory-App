package com.example.inventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventory.data.InventoryContract.InventoryEntry;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private Spinner mSupplierSpinner;
    private int mSupplier;
    Bitmap bitmap;
    byte[] bytesArray;
    ImageView imageViewLoad;
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    Uri selectedImage;

    private Uri currentProductUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        imageViewLoad = findViewById(R.id.product_image);
        Button buttonChooseImage = findViewById(R.id.choose_image);
        mNameEditText =  findViewById(R.id.product_name);
        mPriceEditText =  findViewById(R.id.product_price);
        mQuantityEditText =  findViewById(R.id.product_quantity);
        mSupplierSpinner =  findViewById(R.id.supplier_list);



        setUpSpinner();
        buttonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        Intent intent = getIntent();
        currentProductUri = intent.getData();

        if (currentProductUri == null){
            setTitle("Add a product");
            invalidateOptionsMenu();
        }else {
            setTitle("Edit product");
            getLoaderManager().initLoader(1, null, this);
        }


    }


    private void setUpSpinner(){
        ArrayAdapter supplierSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.supplier_array, android.R.layout.simple_spinner_item);
        supplierSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSupplierSpinner.setAdapter(supplierSpinnerAdapter);
        mSupplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (!TextUtils.isEmpty(selectedItem)) {
                    if (selectedItem.equals("Meridian")) {
                        mSupplier = InventoryEntry.SUPPLIER_1; // 1
                    } else if (selectedItem.equals("Olip Itali Spa")) {
                        mSupplier = InventoryEntry.SUPPLIER_2; // 2
                    } else if (selectedItem.equals("Lucky Zone")){
                        mSupplier = InventoryEntry.SUPPLIER_3; // 3
                    } else if (selectedItem.equals("Unique Treasure")){
                        mSupplier = InventoryEntry.SUPPLIER_4;
                    } else {
                        mSupplier = InventoryEntry.SUPPLIER_0;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSupplier = 0;
            }
        });
    }

    private void selectImage(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {

            if (requestCode == 1 && resultCode == RESULT_OK  && null != data) {
                selectedImage = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 1, stream );
                    bytesArray = stream.toByteArray();
                    Toast.makeText(getApplicationContext(), "Image bitmap selected is: " +bitmap, Toast.LENGTH_SHORT ).show();
                    imageViewLoad.setImageBitmap(bitmap);
                    //Toast.makeText(getApplicationContext(), "Image byteArray: " +bytesArray, Toast.LENGTH_LONG ).show();
                } catch (IOException e) {
                    Log.i("TAG", "Some exception " + e);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProduct(){
        String productName = mNameEditText.getText().toString().trim();
        String stringPrice = mPriceEditText.getText().toString().trim();
        String  stringQuantity = mQuantityEditText.getText().toString().trim();

        int productPrice = 0;
        int productQuantity = 0;
        if (!TextUtils.isEmpty(stringPrice)) {
            productPrice = Integer.parseInt(stringPrice);
        }
        if (!TextUtils.isEmpty(stringQuantity)) {
            productQuantity = Integer.parseInt(stringQuantity);
        }

        if (TextUtils.isEmpty(productName) || TextUtils.isEmpty(stringPrice) || TextUtils.isEmpty(stringQuantity)) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
        }

        else if(mSupplier == InventoryEntry.SUPPLIER_0){
            Toast.makeText(this, "Select a supplier.", Toast.LENGTH_SHORT).show();
        }

        else {
            ContentValues values = new ContentValues();
            values.put(InventoryEntry.COLUMN_NAME, productName);
            values.put(InventoryEntry.COLUMN_PRICE, productPrice);
            values.put(InventoryEntry.COLUMN_QUANTITY, productQuantity);
            values.put(InventoryEntry.COLUMN_SUPPLIER, mSupplier);
            values.put(InventoryEntry.COLUMN_IMAGE, bytesArray);

            if (currentProductUri == null) {
                Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
                if (newUri == null) {
                    Toast.makeText(this, "Product is not inserted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "New product inserted with uri " + newUri, Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                int rowUpdated = getContentResolver().update(currentProductUri, values, null, null);
                if (rowUpdated == 0) {
                    Toast.makeText(this, "Product is not updated.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Number of row updated is: " + rowUpdated, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }

    }

    private void delete(){
        if (currentProductUri != null){
            int rowDelete = getContentResolver().delete(currentProductUri, null, null);
            Toast.makeText(this, "Number of deleted row:  " + rowDelete, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editor_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (currentProductUri == null){
            menu.findItem(R.id.action_delete).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                saveProduct();
                return true;
            case R.id.action_delete:
                delete();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_NAME,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_SUPPLIER,
                InventoryEntry.COLUMN_IMAGE};
        return new CursorLoader(this, currentProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {

            if (cursor.moveToFirst()){
                String name = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_NAME));
                int price = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE));
                int quantity = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY));
                int supplier = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER));
                byte[] imageByte = cursor.getBlob(cursor.getColumnIndex(InventoryEntry.COLUMN_IMAGE));

                mNameEditText.setText(name);
                mPriceEditText.setText(Integer.toString(price));
                mQuantityEditText.setText(Integer.toString(quantity));

                if (imageByte != null){
                    imageViewLoad.setImageBitmap(BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length));
                }

                switch (supplier){
                    case InventoryEntry.SUPPLIER_1:
                        mSupplierSpinner.setSelection(InventoryEntry.SUPPLIER_1);
                        break;
                    case InventoryEntry.SUPPLIER_2:
                        mSupplierSpinner.setSelection(InventoryEntry.SUPPLIER_2);
                        break;
                    case InventoryEntry.SUPPLIER_3:
                        mSupplierSpinner.setSelection(InventoryEntry.SUPPLIER_3);
                        break;
                    case InventoryEntry.SUPPLIER_4:
                        mSupplierSpinner.setSelection(InventoryEntry.SUPPLIER_4);
                        break;
                        default:
                            mSupplierSpinner.setSelection(InventoryEntry.SUPPLIER_0);
                }
            }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierSpinner.setSelection(InventoryEntry.SUPPLIER_0);
        imageViewLoad.setImageResource(R.drawable.ic_photo);
    }
}
