package com.example.mjamraizabbasi.doctorvinterfaces;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import org.apache.commons.io.IOUtils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class xrayAnalysisActivity extends AppCompatActivity {
    static{ System.loadLibrary("opencv_java3"); }
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
    public Mat src;
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
        analyze_button = (Button) findViewById(R.id.doneBtn);
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
                            final InputStream imageStream;
                            src = new Mat();
                            if (cam_flag){
                                imageStream = new FileInputStream(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");
                                byte[] b_array = IOUtils.toByteArray(new FileInputStream(Environment.getExternalStorageDirectory().getPath()+"/image.jpg"));
                                src = Imgcodecs.imdecode(new MatOfByte(b_array), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
                            }
                            else{
                                imageStream = getContentResolver().openInputStream(image_uri);
                                byte[] b_array = IOUtils.toByteArray(getContentResolver().openInputStream(image_uri));
                                src = Imgcodecs.imdecode(new MatOfByte(b_array), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
                            }

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
                                        if (best_match.getScore() > 0.5) {
                                            //ANALYSIS FOR FRACTURE DETECTION

                                            if(!src.empty()){
                                                Runnable thread = new MyRunnable(src,xrayAnalysisActivity.this,best_match.getClassName());
                                                new Thread(thread).start();
                                            }

                                        }
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

                        } catch (Exception e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "FILE NOT FOUND...", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
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

class MyRunnable implements Runnable {
    Mat src;
    ArrayList<Mat> clusters;
    Context context;
    String name;

    public MyRunnable(Mat src, Context context,String name) {
        this.src = src;
        this.context = context;
        this.name = name;
    }

    public void run() {
        //XRAY PREPROCESSING
        //1. Resizing
        double ratio = (double) src.width() / src.height();
        if (src.width() > 768) {
            int newWidth = (int) (768 / ratio);
            Imgproc.resize(src, src, new Size(768, newWidth));
        } else if (src.height() > 1024) {
            int newHeight = (int) (1024 * ratio);
            Imgproc.resize(src, src, new Size(newHeight, 1024));
        }

        //2. Noise removal
        Imgproc.medianBlur(src, src, 9);

        //3. Edge detection

        Mat dst = new Mat();
        double high_thresh = Imgproc.threshold(src, dst, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        Imgproc.Canny(src, src, high_thresh / 2, high_thresh, 3, false);

//                                 Mat kernel = new Mat(9,9, CvType.CV_32F){
//                                     {
//                                         put(0,0,-1);
//                                         put(0,1,0);
//                                         put(0,2,1);
//
//                                         put(1,0,-2);
//                                         put(1,1,0);
//                                         put(1,2,2);
//
//                                         put(2,0,-1);
//                                         put(2,1,0);
//                                         put(2,2,1);
//
//                                    }
//                                };
//
//                                Imgproc.filter2D(src,src,-1,kernel);

        //4. Image Segmentation using K-means
        Mat samples = src.reshape(1, src.cols() * src.rows());
        final int chanels = src.channels();
        Mat samples32f = new Mat();
        samples.convertTo(samples32f, CvType.CV_32F, 1.0 / 255.0);
        Mat labels = new Mat();
        TermCriteria criteria = new TermCriteria(TermCriteria.COUNT, 100, 1);
        Mat centers = new Mat();
        Core.kmeans(samples32f, 2, labels, criteria, 3, Core.KMEANS_PP_CENTERS, centers);
        centers.convertTo(centers, CvType.CV_8UC1, 255.0);
        centers.reshape(1);
        clusters = new ArrayList<Mat>();
        for (int i = 0; i < centers.rows(); i++) {
            clusters.add(Mat.zeros(src.size(), src.type()));
        }
        Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
        for (int i = 0; i < centers.rows(); i++) counts.put(i, 0);
        int rows = 0;
        for (int y = 0; y < src.rows(); y++) {
            for (int x = 0; x < src.cols(); x++) {
                int label = (int) labels.get(rows, 0)[0];
                int g = (int) centers.get(label, 0)[0];
                clusters.get(label).put(y, x, g);
                rows++;
            }
        }
        src = clusters.get(1);

        //5. Feature Extraction using GLCM
        GLCMFeatureExtraction glcmfe = new GLCMFeatureExtraction(context, src, 15);
        glcmfe.extract();
//        MainActivity.output += Double.toString(glcmfe.getContrast()) + "," + Double.toString(glcmfe.getHomogenity()) + "," + Double.toString(glcmfe.getEntropy())
//                + "," + Double.toString(glcmfe.getEnergy()) + "," + Double.toString(glcmfe.getDissimilarity()) + ",NOT_BROKEN\n";
        Log.d("Ouput",Double.toString(glcmfe.getContrast())+","+Double.toString(glcmfe.getHomogenity())+","+Double.toString(glcmfe.getEntropy())
                +","+Double.toString(glcmfe.getEnergy())+","+Double.toString(glcmfe.getDissimilarity()));

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("xray_name", name);
        boolean status = glcmfe.getHomogenity()<=0.97401;
        editor.putBoolean("fractured",status);
        editor.apply();
        Intent intent = new Intent(context,xrayAnalysisResult.class);
        context.startActivity(intent);
//        Log.d("Output", MainActivity.output);


        //1. Binarization (conversion to black and white)
        //Imgproc.threshold(src,src, 120  , 255, Imgproc.THRESH_BINARY);


//                                //deskewing
//                                Size size = src.size();
//                                //Core.bitwise_not(src, src);
//                                Mat lines = new Mat();
//                                Imgproc.HoughLinesP(src, lines, 1, Math.PI / 180, 100, size.width / 2.f, 20);
//                                double angle = 0.;
//                                for(int i = 0; i<lines.height(); i++){
//                                    for(int j = 0; j<lines.width();j++){
//                                        angle += Math.atan2(lines.get(i, j)[3] - lines.get(i, j)[1], lines.get(i, j)[2] - lines.get(i, j)[0]);
//                                    }
//                                }
//                                angle /= lines.size().area();
//                                angle = angle * 180 / Math.PI;
//                                final double a = angle;
//                                Point center = new Point(src.width()/2, src.height()/2);
//                                Mat rotImage = Imgproc.getRotationMatrix2D(center, angle, 1.0);
//                                //1.0 means 100 % scale
//                                Size size1 = new Size(src.width(), src.height());
//                                Imgproc.warpAffine(src, src, rotImage, size1, Imgproc.INTER_LINEAR + Imgproc.CV_WARP_FILL_OUTLIERS);


        //1.Median denoising
        //src = Imgproc.medianBlur(src,src,5);


        //Photo.fastNlMeansDenoising(src,src,10,7,21);

        //Photo.fastNlMeansDenoising(src,src,10,7,21);
        //Imgproc.threshold(src,src, 120  , 255, Imgproc.THRESH_BINARY_INV);
        //Mat dst = new Mat();
        //double high_thresh = Imgproc.threshold(src, dst, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        //Imgproc.Canny(src,src,high_thresh/2,high_thresh,3,false);

//                                Mat lines = new Mat(); // will hold the results of the detection
//                                Imgproc.HoughLines(src, lines, 1, Math.PI/180, 150);
//
//                                for (int x = 0; x < lines.rows(); x++) {
//                                    double rho = lines.get(x, 0)[0],
//                                            theta = lines.get(x, 0)[1];
//                                    double a = Math.cos(theta), b = Math.sin(theta);
//                                    double x0 = a*rho, y0 = b*rho;
//                                    Point pt1 = new Point(Math.round(x0 + 1000*(-b)), Math.round(y0 + 1000*(a)));
//                                    Point pt2 = new Point(Math.round(x0 - 1000*(-b)), Math.round(y0 - 1000*(a)));
//                                    Imgproc.line(src, pt1, pt2, new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
//                                }
//
//        final Bitmap bm = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
//        //MediaStore.Images.Media.insertImage(getContentResolver(), bm, "image" , "");
//        Utils.matToBitmap(src, bm);
    }
}
