package org.ros.android.main_app;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ismail almahdi on 12/12/2016.
 */

public class voiceRecognition implements RecognitionListener, TextToSpeech.OnInitListener
{

    private TextToSpeech textReader;
    private Map<String, String> messagesMap;
    private static final String TAG = "TestingVoice";
    private ImageButton ControlSpeakButton;
    voiceRecognition(TextToSpeech tts, ImageButton CSB){
        textReader = tts;
        ControlSpeakButton = CSB;
        messagesMap = new HashMap<String, String>();
        messagesMap.put("go home","Okay I am going home!");
        messagesMap.put("stop","I am stopping");
        messagesMap.put("resume","going todo what I was doing !");
        messagesMap.put("work","Going to work !");
        messagesMap.put("what to say","You can say: go home , stop, resume and work");
    }


    public void onReadyForSpeech(Bundle params)
    {
        Log.d(TAG, "onReadyForSpeech");

    }
    public void onBeginningOfSpeech()
    {
        Log.d(TAG, "onBeginningOfSpeech");
    }
    public void onRmsChanged(float rmsdB)
    {
        Log.d(TAG, "onRmsChanged");
    }
    public void onBufferReceived(byte[] buffer)
    {
        Log.d(TAG, "onBufferReceived");
    }

    public void onEndOfSpeech()
    {
        Log.d(TAG, "onEndofSpeech");
        ControlSpeakButton.setImageResource(R.drawable.mic);

    }

    public void onError(int error)
    {
        Log.d(TAG,  "error " +  error);
    }

    public void onResults(Bundle results)
    {
        boolean command_match = false;
        Log.d(TAG, "onResults " + results);
        ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        for (int i = 0; i < data.size(); i++)
        {
            String voiceMessage  = (String) data.get(i);
            for ( String received : messagesMap.keySet() ) {
                if(voiceMessage.contains(received)){// checking the results
                    command_match = true;
                    Log.d(TAG, messagesMap.get(received));
                    textReader.speak(messagesMap.get(received),TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        }
        if(!command_match){
            String whatTosay = "Sorry you can only say: go home , stop, resume and work for the time being.";
            textReader.speak(whatTosay ,TextToSpeech.QUEUE_FLUSH, null);
        }
    }
    public void onPartialResults(Bundle partialResults)
    {
        Log.d(TAG, "onPartialResults");
    }
    public void onEvent(int eventType, Bundle params)
    {
        Log.d(TAG, "onEvent " + eventType);
    }

    @Override
    public void onInit(int i) {

    }
}

