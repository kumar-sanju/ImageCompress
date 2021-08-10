package com.smartworld.imagecompressor;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import id.zelory.compressor.Compressor;

public class MainActivity extends AppCompatActivity {

    public static final int RESULT_IMAGE = 1;

    Button btnCompress, btnPick;
    SeekBar seekQuality;
    TextView textQuality, txtOriginal, txtCompress;
    EditText txtHeight, txtWeight;
    ImageView imgCompress, imgOriginal;
    File originalImage, compressImage;
    private static String filePath;
    File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/myCompressor");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCompress = findViewById(R.id.btnCompress);
        btnPick = findViewById(R.id.btnPick);
        seekQuality = findViewById(R.id.seekQuality);
        textQuality = findViewById(R.id.textQuality);
        txtHeight = findViewById(R.id.txtHeight);
        txtWeight = findViewById(R.id.txtWeight);
        imgCompress = findViewById(R.id.imgCompress);
        imgOriginal = findViewById(R.id.imgOriginal);
        txtOriginal = findViewById(R.id.txtOriginal);
        txtCompress = findViewById(R.id.txtCompress);
        
        askPermission();

        filePath = path.getAbsolutePath();

        if (!path.exists()){
            path.mkdirs();
        }

        seekQuality.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textQuality.setText("Quality: "+progress);
                seekBar.setMax(100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btnPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        btnCompress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtHeight.getText().toString().isEmpty() || txtWeight.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this,"Fields Can't be Empty.",Toast.LENGTH_SHORT).show();
                } else {
                    int quality = seekQuality.getProgress();
                    int width = Integer.parseInt(txtWeight.getText().toString());
                    int height = Integer.parseInt(txtHeight.getText().toString());

                    try {
                        compressImage = new Compressor(MainActivity.this)
                                .setMaxWidth(width)
                                .setMaxHeight(height)
                                .setQuality(quality)
                                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                                .setDestinationDirectoryPath(filePath)
                                .compressToFile(originalImage);
                        File finalFile = new File(filePath, originalImage.getName());
                        Bitmap finalBitmap = BitmapFactory.decodeFile(finalFile.getAbsolutePath());
                        imgCompress.setImageBitmap(finalBitmap);
                        txtCompress.setText("Size: "+ Formatter.formatShortFileSize(MainActivity.this,finalFile.length()));
                        Toast.makeText(MainActivity.this,"Image Compressed & Saved!", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this,"Error While Compressing!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            btnCompress.setVisibility(View.VISIBLE);
            final Uri imageUri = data.getData();
            try {
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectImage = BitmapFactory.decodeStream(imageStream);
                imgOriginal.setImageBitmap(selectImage);
                originalImage = new File(imageUri.getPath().replace("raw/",""));
                txtOriginal.setText("Size: "+ Formatter.formatShortFileSize(this,originalImage.length()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this,"Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this,"No Image Selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void askPermission() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {

            }
            @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }
}