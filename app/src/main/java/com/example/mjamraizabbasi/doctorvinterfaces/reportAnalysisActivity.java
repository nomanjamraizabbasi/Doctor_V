package com.example.mjamraizabbasi.doctorvinterfaces;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Block;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.Page;
import com.google.api.services.vision.v1.model.Paragraph;
import com.google.api.services.vision.v1.model.Symbol;
import com.google.api.services.vision.v1.model.TextAnnotation;
import com.google.api.services.vision.v1.model.Vertex;
import com.google.api.services.vision.v1.model.Word;

import org.apache.commons.io.IOUtils;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class reportAnalysisActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    static{ System.loadLibrary("opencv_java3"); }

    static final int CAM_REQUEST = 1;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    String username;
    NavigationView navigationView;
    Button analyze_button;
    ImageButton input_button;
    ImageView image_view;
    Uri image_uri;
    boolean cam_flag; //turns ON when image is taken with camera and helps in fetching it later
    //OCR VARIABLES
    TextAnnotation text;
    ArrayList<Vertex> testnameBlocks_coords; //Contains block/s that have test names
    ArrayList<Integer> testvaluesBlocks_coords; //COntains block/s that have values
    ArrayList<String> testnames;
    ArrayList<Float> testValues;
    ArrayList<Block> allBlocks;
    AlertDialog alertDialog1;
    CharSequence[] values = {" Male "," Female "};
    Vision vision;
    String gender="";
    Boolean done=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //For handling file exposure when taken from camera
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_analysis);
        input_button = (ImageButton) findViewById(R.id.uploadImageBtn);
        image_view = (ImageView) findViewById(R.id.view2);
        analyze_button = (Button) findViewById(R.id.analyzeReportBtn);

        //ANALYSIS PART
        analyze_button.setEnabled(false);
        analyze_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    analyze_button.setEnabled(false);
                                    input_button.setEnabled(false);
                                }
                            });
                            try {
                                InputStream inputStream;
                                //this will automatically handle image from camera/gallery based on "cam_flag"
                                if (cam_flag) //usage of cam_flag
                                    inputStream = new FileInputStream(Environment.getExternalStorageDirectory().getPath() + "/image.jpg");
                                else
                                    inputStream = getContentResolver().openInputStream(image_uri); //the image you have to play with now is inputStream

                                //OCR WORK
                                Vision.Builder visionBuilder = new Vision.Builder(
                                        new NetHttpTransport(),
                                        new AndroidJsonFactory(),
                                        null);


                                visionBuilder.setVisionRequestInitializer(new VisionRequestInitializer("AIzaSyDnTlB2Lt4YDMAY06rcyNyRJuYcxM7GfKA"));
                                vision = visionBuilder.build();

                                //Call to analysis part
                                analyze_Report(inputStream, view);

                            } catch (Exception e) {

                            }
                        }
                    });
                    thread.run();

                }

        });

        //INPUT IMAGE PART
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

        //nav bar open and close
        actionBarDrawerToggle= new ActionBarDrawerToggle(this,drawerLayout,R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Color the acion bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xff2b78b4));

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);




    }

    public void analyze_Report(final InputStream inputStream, final View view){
      //  CreateAlertDialogWithRadioButtonGroup() ;
        testnameBlocks_coords = new ArrayList<Vertex>(); //Contains block/s that have test names
        testvaluesBlocks_coords = new ArrayList<Integer>(); //COntains block/s that have values
        testnames = new ArrayList<String>();
        testValues = new ArrayList<Float>();
        allBlocks= new ArrayList<Block>();

        // Create new thread
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Convert photo to byte array
                try {
                    byte[] photoData = IOUtils.toByteArray(inputStream);
                    inputStream.close();
                    //Mat src = Imgcodecs.imdecode(new MatOfByte(photoData), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
//                    photoData = new byte[(int) (src.total() *  src.channels())];
//                    src.get(0, 0, photoData);



//                    //OCR PREPROCESSING FOR FAREHA'S MODULE
//                    try{
////                                Mat src = Utils.loadResource(reportAnalysisActivity.this, R.drawable.bloodf, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
//                        //boolean ans = src.isContinuous();
//                        //1. Resizing
//                        double ratio = (double)src.width()/src.height();
//                        if(src.width()>768){
//                            int newHeight = (int)(768/ratio);
//                            Imgproc.resize(src,src,new Size(768,newHeight ));
//                        }
//                        else if(src.height()>1024){
//                            int newWidth = (int)(1024*ratio);
//                            Imgproc.resize(src,src,new Size(newWidth,1024));
//                        }
//
//                        //2. denoising
//                        Photo.fastNlMeansDenoising(src,src,10,7,21);
//                    }
//                    catch(Exception e){}
//
//                    photoData = new byte[(int) (src.total() *  src.channels())];
//                    src.get(0, 0, photoData);
//
                    Image inputImage = new Image();
                    inputImage.encodeContent(photoData);

                    Feature desiredFeature = new Feature();
                    desiredFeature.setType("TEXT_DETECTION");

                    BatchAnnotateImagesRequest batchRequest =
                            new BatchAnnotateImagesRequest();
                    final AnnotateImageRequest request = new AnnotateImageRequest();
                    request.setImage(inputImage);
                    request.setFeatures(Arrays.asList(desiredFeature));
                    batchRequest.setRequests(Arrays.asList(request));
                    BatchAnnotateImagesResponse batchResponse = vision.images().annotate(batchRequest).execute();
                    text = batchResponse.getResponses().get(0).getFullTextAnnotation();

                    int block_number = -1;
                    int current_block = 0;
                    ArrayList<Vertex> block_coords = new ArrayList<Vertex>();

                    for (Page page : text.getPages()) {
                        for (Block block : page.getBlocks()) {

                            block_number++;
                            //Save vertices of all the blocks
                            block_coords.add(block.getBoundingBox().getVertices().get(0));
                            allBlocks.add(block);

                            for (Paragraph paragraph : block.getParagraphs()) {
                                for (Word word : paragraph.getWords()) {
                                    String c_word = "";
                                    for (Symbol symbol : word.getSymbols()) {
                                        c_word += symbol.getText();
                                    }
                                    if (c_word.equals("WBC") || c_word.equals("RBC") ||
                                            c_word.equals("HB") || c_word.equals("Hb") || c_word.contains("Hemoglobin") || c_word.equals("Haemoglobin")
                                            || c_word.equals("Hematocrit") || c_word.equals("HCT") || c_word.equals("MCV") || c_word.equals("MCH")
                                            || c_word.equals("MCHC") || c_word.contains("Platelet") || c_word.equals("PLT") || c_word.equals("ESR")
                                            || c_word.equals("LYM") || c_word.equals("LYM#") || c_word.equals("LYM%") || c_word.contains("Lym")
                                            || c_word.equals("NEUT#") || c_word.contains("NUET%") || c_word.equals("NEUT") || c_word.contains("Neut")
                                            || c_word.contains("Monocytes") || c_word.contains("Eosinophils")
                                            || c_word.equals("Mixed Cells") ||c_word.equals("Basophils") ||c_word.equals("Bands") ||
                                            c_word.contains("Bilirubin") || c_word.equals("ALT") || c_word.equals("SGPT") || c_word.equals("ALK-Phos") || c_word.contains("Alk")
                                            || c_word.equals("ALK")) {

                                        //Store the y coords of blocks containing testnames in array if not already saved
                                        if (testnameBlocks_coords.isEmpty() || !testnameBlocks_coords.contains(block_coords.get(block_number)))
                                            testnameBlocks_coords.add(block_coords.get(block_number));
                                    }
                                }
                            }
                        }

                    }

                    //Sort the array containing the blocks that contain the test names in an order of largest y coordinates
                    Collections.sort(testnameBlocks_coords, new Comparator<Vertex>() {
                        @Override
                        public int compare(Vertex x1, Vertex x2) {
                            int result= Integer.compare(x1.getY(), x2.getY());
                            if(result==0){
                                //both ys are equal so we compare the x
                                result=Integer.compare(x1.getX(), x2.getX());
                            }
                            return result;
                        }
                    });

                    //Save the names of the testnames in order in test_name array
                    int blocknum = 0;
                    for (int j = 0; j < testnameBlocks_coords.size(); j++) {
                        for (int i = 0; i < allBlocks.size(); i++) {
                            if (allBlocks.get(i).getBoundingBox().getVertices().get(0) == testnameBlocks_coords.get(j)) {
                                blocknum = i;     //The index at which the block coordinate is present in the block_cooords array
                                break;
                            }
                        }

                        for (Paragraph paragraph : allBlocks.get(blocknum).getParagraphs()) {      //Loop on its paragraphs
                            for (Word word : paragraph.getWords()) {
                                String name = "";
                                for (Symbol symbol : word.getSymbols()) {
                                    name += symbol.getText();
                                    //save the testnames in testnames array
                                }
                                if (name.equals("%") || name.equals("#") || name.equals("Count") || name.equals(",")
                                        || name.equals("Level")) {
                                    StringBuilder stringBuilder = new StringBuilder(testnames.get(testnames.size() - 1));
                                    stringBuilder.append(name);
                                    testnames.add(testnames.size() - 1, stringBuilder.toString());
                                    testnames.remove(testnames.size() - 1);
                                }
                                else if (name.equals("WBC") || name.equals("RBC") ||
                                        name.equals("HB") || name.equals("Hb") || name.contains("Hemoglobin") || name.equals("Haemoglobin")
                                        || name.equals("Hematocrit") || name.equals("HCT") || name.equals("MCV") || name.equals("MCH")
                                        || name.equals("MCHC") || name.contains("Platelet") || name.equals("PLT") || name.equals("ESR")
                                        || name.equals("LYM") || name.equals("LYM#") || name.equals("LYM%") || name.contains("Lym")
                                        || name.equals("NEUT#") || name.contains("NUET%") || name.equals("NEUT") || name.contains("Neut")
                                        || name.contains("Monocytes") || name.contains("Eosinophils")
                                        || name.equals("Mixed Cells") ||name.equals("Basophils") ||name.equals("Bands") ||
                                        name.contains("Bilirubin") || name.equals("ALT") || name.equals("SGPT") || name.equals("ALK-Phos") || name.contains("Alk.")
                                        || name.equals("ALK"))
                                    testnames.add(name);

                            }
                        }

                    }

                    //Below is the procedure to find the values of testvalues
                    int result_block_index = 0;
                    int num_of_values=0;
                    int diff=0;
                    ArrayList<Integer> ignoreIndex=new ArrayList<Integer>();
                    Boolean isFloat=true;
                    float value=0;

                    while(num_of_values<testnames.size()){
                        //next block of values
                        if(!testvaluesBlocks_coords.isEmpty()){
                            int previous_result_block_index = result_block_index; //Index at which the value block lies
                            diff = Integer.MAX_VALUE;

                            for(int i=0; i<block_coords.size(); i++){
                                if(Math.abs(block_coords.get(previous_result_block_index).getX()-block_coords.get(i).getX())<diff && previous_result_block_index!=i){
                                    if(!ignoreIndex.contains(i)){
                                        diff= Math.abs(block_coords.get(previous_result_block_index).getX()-block_coords.get(i).getX());
                                        result_block_index=i;
                                    }

                                }
                            }
                        }
                        //first block of values
                        else{
                            //Getting values from te first block
                            diff=Integer.MAX_VALUE;

                            for(int i=0; i<block_coords.size(); i++){
                                if(Math.abs(testnameBlocks_coords.get(0).getY()-block_coords.get(i).getY())<diff && block_coords.indexOf(testnameBlocks_coords.get(0))!=i){
                                    if(!ignoreIndex.contains(i)){
                                        diff= testnameBlocks_coords.get(0).getY()-block_coords.get(i).getY();
                                        result_block_index=i;
                                    }

                                }
                            }
                        }
                        isFloat=false;
                        for(Paragraph paragraph: allBlocks.get(result_block_index).getParagraphs()){
                            for(Word word: paragraph.getWords()){
                                String value_string="";
                                for(Symbol symbol: word.getSymbols()){
                                    value_string+=symbol.getText();

                                }
                                while(value_string.contains(",")){
                                    int z = value_string.indexOf(",");
                                    value_string = value_string.substring(0, z) + value_string.substring(z + 1);
                                }
                                if(value_string.contains("-")){
                                    isFloat=false;
                                    //ignore indices that have been selected no matter if they had the values or not
                                    if(!ignoreIndex.contains(result_block_index))
                                        ignoreIndex.add(result_block_index);
                                    break;
                                }
                                try{
                                    value= Float.parseFloat(value_string);
                                    num_of_values++;
                                    //ignore indices that have been selected no matter if they had the values or not
                                    if(!ignoreIndex.contains(result_block_index))
                                        ignoreIndex.add(result_block_index);
                                    isFloat=true;

                                }
                                catch (NumberFormatException e){
                                    //ignore indices that have been selected no matter if they had the values or not
                                    if(!ignoreIndex.contains(result_block_index))
                                        ignoreIndex.add(result_block_index);
                                }




                            }
                        }

                        if(isFloat){
                            //Save y coordinates of value block
                            testvaluesBlocks_coords.add(block_coords.get(result_block_index).getY());
                        }
                    }
                    //sort test values coordinates array
                    Collections.sort(testvaluesBlocks_coords);

                    //save the values in an array
                    for(int i=0; i<testvaluesBlocks_coords.size();i++){
                        for(int j=0; j<allBlocks.size();j++){
                            if(allBlocks.get(j).getBoundingBox().getVertices().get(0).getY()==testvaluesBlocks_coords.get(i)){
                                blocknum=j;     //The index at which the block coordinate is present in the block_cooords array
                                break;
                            }
                        }

                        //save values in array
                        for (Paragraph paragraph: allBlocks.get(blocknum).getParagraphs()) {      //Loop on its paragraphs
                            for(Word word: paragraph.getWords()){
                                String value_string="";
                                for(Symbol symbol: word.getSymbols()){
                                    value_string+=symbol.getText();
                                }

                                while(value_string.contains(",")){
                                    int z = value_string.indexOf(",");
                                    value_string = value_string.substring(0, z)  + value_string.substring(z + 1);
                                }
                                try{
                                    value= Float.parseFloat(value_string);
                                    //save the testnames in testnames array
                                    testValues.add(Float.parseFloat(value_string));
                                }
                                catch(NumberFormatException n){

                                }
                            }

                        }
                    }

                    for (int a = 0; a < testnames.size(); a++) {
                        Log.e(testnames.get(a), Float.toString(testValues.get(a)));
                    }

                    //Convert the testname and testvalues array to string so they can be passed on
                    StringBuilder sb = new StringBuilder();
                    StringBuilder sb2 = new StringBuilder();
                    for (int i = 0; i < testnames.size(); i++) {
                        sb.append(testnames.get(i)).append(",");
                        sb2.append(testValues.get(i)).append(",");

                    }

                    //Save the values and testnames so they can be passed on to next activity
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(reportAnalysisActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("testnames", sb.toString());
                    editor.putString("testvalues", sb2.toString());
                    editor.putString("gender", gender);
                    editor.apply();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent reportresultScreen = new Intent(view.getContext(), reportResult_Activity.class);
                            startActivity(reportresultScreen);
                        }
                    });
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                done=true;
            }

        });

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
                CreateAlertDialogWithRadioButtonGroup() ;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.HomeItem:
                Intent homescreen = new Intent(this,homepageActivity.class);
                startActivity(homescreen);
                break;
            case R.id.ProfileSettingItem:
                Intent profilescreen = new Intent(this,setProfileActivity.class);
                startActivity(profilescreen);
                break;
            case R.id.ChangePasswordItem:
                //INTERNET CONNECTION CHECK COMES HERE
                Intent changepasswordscreen = new Intent(this,changePasswordActivity.class);
                startActivity(changepasswordscreen);
                break;
            case R.id.HistorylogItem:
                Intent historyscreen = new Intent(this,historylogActivity.class);
                startActivity(historyscreen);
                break;
            case R.id.LogoutItem:
                //LOGOUT LOGIC WILL COME HERE
                break;
        }
        //close navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void CreateAlertDialogWithRadioButtonGroup(){

        AlertDialog.Builder builder = new AlertDialog.Builder(reportAnalysisActivity.this);

        builder.setTitle("This Report Belongs To A: ");

        builder.setSingleChoiceItems(values, -1, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {

                switch(item)
                {
                    case 0:
                        gender= "male";
                        //Toast.makeText(reportAnalysisActivity.this, "Male", Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        gender="female";
                        //Toast.makeText(reportAnalysisActivity.this, "Female", Toast.LENGTH_LONG).show();
                        break;
                }
                alertDialog1.dismiss();
            }
        });
        alertDialog1 = builder.create();
        alertDialog1.show();
    }
}

