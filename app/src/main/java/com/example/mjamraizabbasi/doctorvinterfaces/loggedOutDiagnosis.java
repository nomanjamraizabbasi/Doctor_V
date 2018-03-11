package com.example.mjamraizabbasi.doctorvinterfaces;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import com.ibm.watson.developer_cloud.android.speech_to_text.v1.ISpeechDelegate;
import com.ibm.watson.developer_cloud.android.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.android.speech_to_text.v1.dto.SpeechConfiguration;
import com.ibm.watson.developer_cloud.conversation.v1.Conversation;
import com.ibm.watson.developer_cloud.conversation.v1.model.InputData;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageOptions;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import org.json.JSONObject;
import java.net.URI;
import java.util.HashMap;
import java.util.Locale;

public class loggedOutDiagnosis extends UnityPlayerActivity implements TextToSpeech.OnInitListener, ISpeechDelegate {

    FrameLayout fl_forUnity;
    ListView listView;
    EditText chat_text;
    Button send;
    boolean position= false;
    chatAdapter adapter;
    Context ctx=this;
    String output;
    boolean first = true;
    com.ibm.watson.developer_cloud.conversation.v1.model.Context context;
    TextToSpeech toSpeech;
    int result;
    boolean mProcessed;
    static ImageButton mic;
    HashMap<String, String> myHashRender;
    int i = 0;
    public static boolean recording = false;
    public static boolean canRecord = true;

    public static void donePlaying(){
        canRecord = true;
        mic.setEnabled(true);
        if(recording)
            SpeechToText.sharedInstance().recognize();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_diagnosis_acivity);

        mUnityPlayer = new UnityPlayer(this);
//        if (mUnityPlayer.getSettings().getBoolean("hide_status_bar", true)) {
//            setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        }
        setContentView(R.layout.activity_logged_out_diagnosis);
        this.fl_forUnity = (FrameLayout) findViewById(R.id.unityFrame);
        this.fl_forUnity.addView(mUnityPlayer.getView(), FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        mUnityPlayer.requestFocus();


        toSpeech = new TextToSpeech(this /* context */, this /* listener */);
        toSpeech.setOnUtteranceProgressListener(mProgressListener);


        //Speech to text
        try{
            SpeechToText.sharedInstance().initWithContext(new URI("wss://stream.watsonplatform.net/speech-to-text/api"), this.getApplicationContext(), new SpeechConfiguration());
            SpeechToText.sharedInstance().setCredentials(getString(R.string.speech_text_username),getString(R.string.speech_text_password));
            SpeechToText.sharedInstance().setDelegate(this);
            JSONObject models = SpeechToText.sharedInstance().getModels();
            JSONObject model = SpeechToText.sharedInstance().getModelInfo("en-US_BroadbandModel");
            SpeechToText.sharedInstance().setModel("en-US_BroadbandModel");
        }
        catch(Exception e){

        }

        mic = (ImageButton) findViewById(R.id.mic_button);
        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(recording){
                    recording = false;
                    SpeechToText.sharedInstance().stopRecording();
                    mic.setImageResource(R.drawable.microphonebigger);
                }
                else{
                    recording = true;
                    SpeechToText.sharedInstance().recognize();
                }
            }
        });

        listView=(ListView)findViewById(R.id.chat_list_view);
        chat_text= (EditText)findViewById(R.id.chattext);
        send= (Button)findViewById(R.id.send_button);
        adapter= new chatAdapter(ctx, R.layout.single_message_layout);
        listView.setAdapter(adapter);
        //set the listview scrollable
        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        //set the message to display at the end
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(listView.getCount()-1);
            }
        });

        //set on click listener on the send button
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                canRecord = false;
                mic.setEnabled(false);
                SpeechToText.sharedInstance().stopRecording();
                adapter.add(new DataProvider(position, chat_text.getText().toString()));
                position=!position;
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Conversation service = new Conversation(Conversation.VERSION_DATE_2017_05_26);
                        service.setUsernameAndPassword("1e93ea7a-d063-4805-bae5-cb8979e0529c","ggyxbj3hg5l6");
                        String workspaceId = "02e8a67a-750a-4f64-977f-bbc9688bd795";
                        InputData input = new InputData.Builder(chat_text.getText().toString()).build();
                        MessageOptions options;
                        if(first) {
                            options = new MessageOptions.Builder(workspaceId).input(input).build();
                            first = false;
                        }
                        else {
                            options = new MessageOptions.Builder(workspaceId).input(input).context(context).build();
                        }
                        MessageResponse response =  service.message(options).execute();
                        context = response.getContext();
                        output = response.getOutput().getText().toString();
                        output = output.substring(1,output.length()-1);
                        if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                            Toast.makeText(getApplicationContext(),"Feature not supported",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            String path = Environment.getExternalStorageDirectory().getPath()+"/clip.wav";

                            if(!mProcessed){
                                myHashRender = new HashMap();
                                myHashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "wpta");
                                toSpeech.synthesizeToFile(output,myHashRender, path);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // chat_text.setText("SYNTHESIZED");
                                        //mic.performClick();
                                    }
                                });
                            }

                            //toSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                UnityPlayer.UnitySendMessage("a","GetAudioPath", Environment.getExternalStorageDirectory().getPath());
                                adapter.add(new DataProvider(position, output));
                                position=!position;
                                //chat_text.setText("");
                            }
                        });
                    }
                });
                thread.start();
                //set it to opposite to display next message on the alterate side
            }
        });
    }

    @Override
    public void onOpen() {
        // the  connection to the STT service is successfully opened
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mic.setImageResource(R.drawable.speaking);
                // chat_text.setText("OPEN");
            }
        });
    }

    @Override
    public void onError(String error) {
        // error interacting with the STT service
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        // the connection with the STT service was just closed
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mic.setImageResource(R.drawable.microphonebigger);
            }
        });
    }

    @Override
    public void onMessage(final String message) {
        // a message comes from the STT service with recognition results
        if(canRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject json = new JSONObject(message);
                        boolean isFinal = json.getJSONArray("results").getJSONObject(0).getBoolean("final");
                        final String text_output = json.getJSONArray("results").getJSONObject(0).getJSONArray("alternatives").getJSONObject(0).getString("transcript");
                        chat_text.setText(text_output);
                        if (isFinal) {
                            canRecord = false;
                            mic.setEnabled(false);
                            SpeechToText.sharedInstance().stopRecording();
                            adapter.add(new DataProvider(position, text_output));
                            position = !position;
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Conversation service = new Conversation(Conversation.VERSION_DATE_2017_05_26);
                                    service.setUsernameAndPassword("1e93ea7a-d063-4805-bae5-cb8979e0529c", "ggyxbj3hg5l6");
                                    String workspaceId = "02e8a67a-750a-4f64-977f-bbc9688bd795";
                                    InputData input = new InputData.Builder(text_output.toString()).build();
                                    MessageOptions options;
                                    if (first) {
                                        options = new MessageOptions.Builder(workspaceId).input(input).build();
                                        first = false;
                                    } else {
                                        options = new MessageOptions.Builder(workspaceId).input(input).context(context).build();
                                    }
                                    MessageResponse response = service.message(options).execute();
                                    context = response.getContext();
                                    output = response.getOutput().getText().toString();
                                    output = output.substring(1, output.length() - 1);
                                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                        Toast.makeText(getApplicationContext(), "Feature not supported", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String path = Environment.getExternalStorageDirectory().getPath() + "/clip.wav";

                                        if (!mProcessed) {
                                            myHashRender = new HashMap();
                                            myHashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "wpta");
                                            toSpeech.synthesizeToFile(output, myHashRender, path);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    // chat_text.setText("SYNTHESIZED");
                                                    //mic.performClick();
                                                }
                                            });
                                        }

                                        //toSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null);
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
//                                UnityPlayer.UnitySendMessage("a","GetAudioPath", Environment.getExternalStorageDirectory().getPath());
                                            adapter.add(new DataProvider(position, output));
                                            position = !position;
                                            chat_text.setText("");
                                        }
                                    });
                                }
                            });
                            thread.start();
                        }
                    } catch (Exception e) {
                    }
                }
            });
        }
    }

    @Override
    public void onAmplitude(double amplitude, double volume) {
        // your code here
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(toSpeech!=null){
            toSpeech.stop();
            toSpeech.shutdown();
        }
    }



    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            toSpeech.setLanguage(Locale.ENGLISH);
        }
    }

    private UtteranceProgressListener mProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
        } // Do nothing

        @Override
        public void onError(String utteranceId) {
        } // Do nothing.

        @Override
        public void onDone(String utteranceId) {
            callSpeechRecognition();
        }
    };

    private void callSpeechRecognition() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UnityPlayer.UnitySendMessage("a","GetAudioPath", Environment.getExternalStorageDirectory().getPath());
            }
        });
    }

    private class MicrophoneRecognizeDelegate extends BaseRecognizeCallback {

        @Override
        public void onConnected(){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mic.setImageResource(R.drawable.speaking);
                }
            });
        }

        @Override
        public void onTranscription(final SpeechResults speechResults) {
            if (speechResults.getResults() != null && !speechResults.getResults().isEmpty()) {
                final String text = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(speechResults.getResults().get(0).isFinal()){
                            chat_text.setText(text);
                            adapter.add(new DataProvider(position, chat_text.getText().toString()));
                            position=!position;
                        }
                    }
                });
                if(speechResults.getResults().get(0).isFinal() && text.length() > 5) {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Conversation service = new Conversation(Conversation.VERSION_DATE_2017_05_26);
                            service.setUsernameAndPassword("1e93ea7a-d063-4805-bae5-cb8979e0529c", "ggyxbj3hg5l6");
                            String workspaceId = "02e8a67a-750a-4f64-977f-bbc9688bd795";
                            InputData input = new InputData.Builder(text).build();
                            MessageOptions options;
                            if (first) {
                                options = new MessageOptions.Builder(workspaceId).input(input).build();
                                first = false;
                            } else {
                                options = new MessageOptions.Builder(workspaceId).input(input).context(context).build();
                            }
                            MessageResponse response = service.message(options).execute();
                            context = response.getContext();
                            output = response.getOutput().getText().toString();
                            output = output.substring(1, output.length() - 1);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.add(new DataProvider(position, output));
                                    position = !position;
                                    //chat_text.setText("");
                                }
                            });
                        }
                    });
                    thread.start();
                }
            }
        }

        @Override
        public void onError(final Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(loggedOutDiagnosis.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            });
        }

        @Override
        public void onDisconnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mic.setImageResource(R.drawable.microphonebigger);
                    //chat_text.setText("");
                }
            });
        }
    }

}
