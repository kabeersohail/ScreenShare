package com.example.accesstoken.utils;// Install the Java helper library from twilio.com/docs/java/install
import static com.example.accesstoken.utils.Constants.AUTH_TOKEN;
import static com.example.accesstoken.utils.Constants.TWILIO_ACCOUNT_SID;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.Twilio;
import com.twilio.rest.video.v1.room.RecordingRules;
import com.twilio.type.RecordingRule;
import com.twilio.type.RecordingRulesUpdate;

import java.util.ArrayList;
import java.util.Map;

public class UpdateRules{

    public String x(String sid){
        // Initialize the client
        Twilio.init(TWILIO_ACCOUNT_SID, AUTH_TOKEN);

        ArrayList<RecordingRule> rules = new ArrayList<>();
        rules.add(RecordingRule.builder()
                .withType(RecordingRule.Type.INCLUDE).withAll()
                .build());

        RecordingRules recordingRules =
                RecordingRules.updater(sid)
                        .setRules(new ObjectMapper().convertValue(new RecordingRulesUpdate(rules), Map.class))
                        .update();

        System.out.println(recordingRules.getRoomSid());
        Log.d("SOHAIL",recordingRules.getRoomSid());
        return recordingRules.getRoomSid();
    }
}
