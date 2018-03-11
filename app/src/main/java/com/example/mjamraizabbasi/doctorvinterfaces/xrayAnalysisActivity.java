package com.example.mjamraizabbasi.doctorvinterfaces;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassResult;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifiedImages;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class xrayAnalysisActivity extends AppCompatActivity {
    static final int CAM_REQUEST = 1;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    Button analyze_button;
    ImageButton input_button;
    ImageView image_view;
    TextView result_view;
    ClassResult best_match = null;
    Uri image_uri;
    boolean cam_flag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //For handling file exposure when taken from camera
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xray_analysis);
        input_button = (ImageButton) findViewById(R.id.uploadImageBtn);
        image_view = (ImageView) findViewById(R.id.view2);
        result_view = (TextView) findViewById(R.id.resultView);
        analyze_button = (Button) findViewById(R.id.analyzeXrayBtn);
        analyze_button.setEnabled(false);
        analyze_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                result_view.setText("Processing...");
                                analyze_button.setEnabled(false);
                                input_button.setEnabled(false);
                            }
                        });
                        VisualRecognition service = new VisualRecognition(
                                VisualRecognition.VERSION_DATE_2016_05_20
                        );
                        service.setApiKey("f12f32b71a26d7b0258eba7f1cad8cab808076af");
                        try {
                            InputStream imageStream;
                            if (cam_flag)
                                imageStream = new FileInputStream(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");
                            else
                                imageStream = getContentResolver().openInputStream(image_uri);

                            ClassifyOptions classifyOptions = new ClassifyOptions.Builder()
                                    .imagesFile(imageStream)
                                    .imagesFilename("image.jpg")
                                    .parameters("{\"classifier_ids\": [\"X_RAY_279256972\"],"
                                            + "\"owners\": [\"IBM\", \"me\"],\"Threshold\": " + 0.0 + "}")
                                    .build();
                            final ClassifiedImages result = service.classify(classifyOptions).execute();
                            List<ClassResult> classifications = result.getImages().get(0).getClassifiers().get(0).getClasses();
                            int total_classifiers = classifications.toArray().length;
                            if (total_classifiers > 0) {
                                for (int i = 0; i < total_classifiers; i++) {
                                    if (i == 0)
                                        best_match = classifications.get(0);
                                    else if (classifications.get(i).getScore() > best_match.getScore())
                                        best_match = classifications.get(i);
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (best_match.getScore() > 0.5)
                                            result_view.setText(best_match.getClassName());
                                        else
                                            result_view.setText("Try Again!");
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        result_view.setText("Try Again!");
                                    }
                                });
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    input_button.setEnabled(true);
                                }
                            });

                        } catch (Exception e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "FILE NOT FOUND...", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(image_view.getDrawable() != null)
                                    analyze_button.setEnabled(true);
                            }
                        });
                    }
                });
                thread.start();
            }
        });

        //Input Button
        input_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage("Where would you like to input image from?")
                        .setCancelable(true)
                        .setPositiveButton("Camera", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                File file = getFile();
                                camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                                startActivityForResult(camera_intent,CAM_REQUEST);
                            }
                        })
                        .setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                                File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                                String pictureDirectoryPath = pictureDirectory.getPath();
                                Uri data = Uri.parse(pictureDirectoryPath);
                                photoPickerIntent.setDataAndType(data,"image/*");
                                startActivityForResult(photoPickerIntent,9);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        drawerLayout= (DrawerLayout)findViewById(R.id.drawerid);
        actionBarDrawerToggle= new ActionBarDrawerToggle(this,drawerLayout,R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Color the acion bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xff2b78b4));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            InputStream imagestream = null;
            if(requestCode == 9){
                cam_flag = false;
                image_uri = data.getData();
                try {
                    imagestream = getContentResolver().openInputStream(image_uri);
                } catch (FileNotFoundException e) {
                    Toast.makeText(this,"FILE NOT FOUND...", Toast.LENGTH_SHORT).show();
                }
            }
            else if(requestCode == CAM_REQUEST){
                cam_flag = true;
                try {
                    imagestream = new FileInputStream(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");
                } catch (FileNotFoundException e) {
                    Toast.makeText(this,"FILE NOT FOUND...", Toast.LENGTH_SHORT).show();
                }
            }
            try{
                Bitmap image = BitmapFactory.decodeStream(imagestream);
                image_view.setImageBitmap(image);
            }
            catch(Exception e){
                Toast.makeText(this,"Internal Error, Try Again",Toast.LENGTH_SHORT);
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(image_view.getDrawable() != null)
                    analyze_button.setEnabled(true);
            }
        });
    }

    private File getFile(){
        File folder = new File(Environment.getExternalStorageDirectory().getPath());
        if(!folder.exists()){
            folder.mkdir();
        }
        File file = new File(folder,"image.jpg");
        return file;
    }
}
