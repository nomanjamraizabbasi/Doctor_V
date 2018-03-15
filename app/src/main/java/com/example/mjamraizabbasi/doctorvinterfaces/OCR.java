package com.example.mjamraizabbasi.doctorvinterfaces;

import android.graphics.Point;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.System.out;

public class OCR extends AppCompatActivity {

    TextView textView;
    TextAnnotation text;
    ArrayList<Vertex> testnameBlocks_coords = new ArrayList<Vertex>(); //Contains block/s that have test names
    ArrayList<Integer> testvaluesBlocks_coords = new ArrayList<Integer>(); //COntains block/s that have values
    ArrayList<String> testnames = new ArrayList<String>();
    ArrayList<Float> testValues = new ArrayList<Float>();
    ArrayList<Block> allBlocks= new ArrayList<Block>();
    Vision vision;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);
        textView=(TextView)findViewById(R.id.textview1);
        textView.setMovementMethod(new ScrollingMovementMethod());
        Vision.Builder visionBuilder = new Vision.Builder(
                new NetHttpTransport(),
                new AndroidJsonFactory(),
                null);
        //not
        visionBuilder.setVisionRequestInitializer(new VisionRequestInitializer("AIzaSyDnTlB2Lt4YDMAY06rcyNyRJuYcxM7GfKA"));
        vision = visionBuilder.build();

        analyze_Report();


    }

    public void analyze_CholestrolReport(){

        // Create new thread
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Convert photo to byte array
                InputStream inputStream = getResources().openRawResource(R.raw.blooda);
                try {
                    byte[] photoData = IOUtils.toByteArray(inputStream);
                    inputStream.close();
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
                                            || c_word.equals("Mixed Cells")) {

                                        //Store the y coords of blocks containing testnames in array if not already saved
                                        if (testnameBlocks_coords.isEmpty() || !testnameBlocks_coords.contains(block_coords.get(block_number)))
                                            testnameBlocks_coords.add(block_coords.get(block_number));
                                        current_block = block_number;
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
                                //boh ys are equal so we compare the x
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
                                else
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

                    while(num_of_values<=testnames.size()){
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
                        isFloat=true;
                        for(Paragraph paragraph: allBlocks.get(result_block_index).getParagraphs()){
                            for(Word word: paragraph.getWords()){
                                String value_string="";
                                for(Symbol symbol: word.getSymbols()){
                                    value_string+=symbol.getText();

                                }
                                if(value_string.contains(",")){
                                    int z = value_string.indexOf(",");
                                    value_string = value_string.substring(0, z) + "." + value_string.substring(z + 1);
                                }
                                try{
                                    value= Float.parseFloat(value_string);
                                    num_of_values++;
                                    //ignore indices that have been selected no matter if they had the values or not
                                    if(!ignoreIndex.contains(result_block_index))
                                        ignoreIndex.add(result_block_index);
                                }
                                catch (NumberFormatException e){
                                    isFloat=false;
                                    //ignore indices that have been selected no matter if they had the values or not
                                    if(!ignoreIndex.contains(result_block_index))
                                        ignoreIndex.add(result_block_index);
                                    break;
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

                                if(value_string.contains(",")){
                                    int z = value_string.indexOf(",");
                                    value_string = value_string.substring(0, z) + "." + value_string.substring(z + 1);
                                }

                                //save the testnames in testnames array
                                testValues.add(Float.parseFloat(value_string));
                            }

                        }
                    }

                    if(testnames.size()<testValues.size() || testnames.size()>testValues.size()){
                        Toast.makeText(OCR.this, "Error In Analyzing the file, Try again", Toast.LENGTH_LONG).show();
                    }
                    else {
                        for (int a = 0; a < testValues.size(); a++) {
                            Log.e(testnames.get(a), Float.toString(testValues.get(a)));
                        }
                    }

                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(text.getText());
                    }
                });
            }
        });

    }

    public void analyze_Report(){

        // Create new thread
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Convert photo to byte array
                InputStream inputStream = getResources().openRawResource(R.raw.blooda);
                try {
                    byte[] photoData = IOUtils.toByteArray(inputStream);
                    inputStream.close();
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
                                            || c_word.equals("Mixed Cells")) {

                                        //Store the y coords of blocks containing testnames in array if not already saved
                                        if (testnameBlocks_coords.isEmpty() || !testnameBlocks_coords.contains(block_coords.get(block_number)))
                                            testnameBlocks_coords.add(block_coords.get(block_number));
                                        current_block = block_number;
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
                                //boh ys are equal so we compare the x
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
                                else
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

                    while(num_of_values<=testnames.size()){
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
                        isFloat=true;
                        for(Paragraph paragraph: allBlocks.get(result_block_index).getParagraphs()){
                            for(Word word: paragraph.getWords()){
                                String value_string="";
                                for(Symbol symbol: word.getSymbols()){
                                    value_string+=symbol.getText();

                                }
                                if(value_string.contains(",")){
                                    int z = value_string.indexOf(",");
                                    value_string = value_string.substring(0, z) + "." + value_string.substring(z + 1);
                                }
                                try{
                                    value= Float.parseFloat(value_string);
                                    num_of_values++;
                                    //ignore indices that have been selected no matter if they had the values or not
                                    if(!ignoreIndex.contains(result_block_index))
                                        ignoreIndex.add(result_block_index);
                                }
                                catch (NumberFormatException e){
                                    isFloat=false;
                                    //ignore indices that have been selected no matter if they had the values or not
                                    if(!ignoreIndex.contains(result_block_index))
                                        ignoreIndex.add(result_block_index);
                                    break;
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

                                if(value_string.contains(",")){
                                    int z = value_string.indexOf(",");
                                    value_string = value_string.substring(0, z) + "." + value_string.substring(z + 1);
                                }

                                //save the testnames in testnames array
                                testValues.add(Float.parseFloat(value_string));
                            }

                        }
                    }

                    if(testnames.size()<testValues.size() || testnames.size()>testValues.size()){
                        Toast.makeText(OCR.this, "Error In Analyzing the file, Try again", Toast.LENGTH_LONG).show();
                    }
                    else {
                        for (int a = 0; a < testValues.size(); a++) {
                            Log.e(testnames.get(a), Float.toString(testValues.get(a)));
                        }
                    }

                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(text.getText());
                    }
                });
            }
        });

    }

}
