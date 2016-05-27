package com.example.ahmedmoneeb.voicetesting;

import android.os.AsyncTask;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Locale;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.content.Intent;
import java.util.Locale;
import java.util.Scanner;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

public class MainActivity extends Activity implements OnInitListener{

    private static TextView txtSpeechInput;
    private Button btnSpeak;
    private Button downloadButton;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    //TTS object
    private TextToSpeech myTTS;
    //status check code
    private final int MY_DATA_CHECK_CODE = 0;
    private String textToSpeak;
    private static ArrayList<ArrayList<String>> responsesArray;
    //initialize root directory
    File rootDir = Environment.getExternalStorageDirectory();
    //defining file name and url
    public String fileName = "responses";
    public String fileURL = "http://lindabot.net23.net/srcs/responses";
    public String dirName = "/PikachuBot/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new DownloadFileAsync().execute(fileURL);

//
//
       txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (Button) findViewById(R.id.btnSpeak);
        downloadButton = (Button) findViewById(R.id.downloadButton);
//
//        // hide the action bar
//        // getActionBar().hide();
//
//
        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        //check for TTS data
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

        downloadButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DownloadFileAsync().execute(fileURL);
                responsesArray = createResponesArray();
            }
        });
    }
    class DownloadFileAsync extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... aurl) {
            checkAndCreateDirectory(dirName);
            checkAndCreateFile(fileName);

            try {
                //connecting to url
                URL u = new URL(fileURL);
                HttpURLConnection c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("GET");
                c.setDoOutput(true);
                c.connect();

                //this is where the file will be seen after the download
                FileOutputStream f = new FileOutputStream(new File(rootDir + dirName, fileName));
                //file input is from the url
                InputStream in = c.getInputStream();

                //here’s the download code
                byte[] buffer = new byte[1024];
                int len1 = 0;
                long total = 0;

                while ((len1 = in.read(buffer)) > 0) {
                    total += len1; //total = total + len1
                    f.write(buffer, 0, len1);
                }
                f.close();

            } catch (Exception e) {
                Log.d("Tes 1", "Test 2" + e.toString());
            }
            //printReady();
            return null;
        }
    }

public void printReady()
{
    responsesArray = createResponesArray();
    txtSpeechInput.setText("Ready");
}
    //function to verify if directory exists
    public void checkAndCreateDirectory(String dirName){
        File new_dir = new File( rootDir + dirName );
        if( !new_dir.exists() ){
            new_dir.mkdirs();
        }
    }

    public void checkAndCreateFile(String fileName){
        File new_fil = new File( rootDir + dirName + fileName );
        if( !new_fil.exists() ){
            try {
                new_fil.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));
                    textToSpeak = respond(result.get(0));
                    //textToSpeak = result.get(0);
                    speakWords(textToSpeak);
                }
                break;
            }
            case MY_DATA_CHECK_CODE: {
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    //the user has the necessary data - create the TTS
                    myTTS = new TextToSpeech(this, this);
                }
                break;

            }
        }
    }

    //setup TTS
    public void onInit(int initStatus) {

        //check for successful instantiation
        if (initStatus == TextToSpeech.SUCCESS) {
            if(myTTS.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
                myTTS.setLanguage(Locale.US);
        } else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }


    //initialize and create the array that has the responses
    private ArrayList<ArrayList<String>> createResponesArray()
    {
        String fileContent = "";

        try {
            InputStream inputStream = new FileInputStream(new File(rootDir + dirName + fileName));

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                fileContent = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            txtSpeechInput.setText("file not found");
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            txtSpeechInput.setText("can't read file");
            //Log.e("login activity", "Can not read file: " + e.toString());
        }
        txtSpeechInput.setText("Ready !!");
        Scanner input = null;
        String temp = "";
        String[] fileLines = fileContent.split(Pattern.quote("$"));
        ArrayList<ArrayList<String>> tempResponsesArray = new ArrayList();
        for (int i=0; i<fileLines.length; i++)
        {
            String[] tempRow = fileLines[i].split(Pattern.quote("#"));
            tempResponsesArray.add(new ArrayList<String>());
            tempResponsesArray.get(i).add(tempRow[0]);
            tempResponsesArray.get(i).add(tempRow[1]);
        }

        return tempResponsesArray;
        //return null;
    }

    //speak the user text
    private void speakWords(String speech) {

        //speak straight away
        myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }

    private static String respond(String speech)
    {
        String response = "Sorry what was that ?";
        for (int i=0;i<responsesArray.size();i++) {
            if (responsesArray.get(i).get(0).equalsIgnoreCase(speech)) {
                response = responsesArray.get(i).get(1);
            }
        }
        return response;
    }
}
