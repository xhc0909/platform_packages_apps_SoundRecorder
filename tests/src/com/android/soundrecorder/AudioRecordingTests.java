/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.soundrecorder.tests;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.KeyEvent;
import android.util.Log;
import android.content.Context;
import android.os.Environment;
import android.content.IntentFilter;
import java.io.*;
import java.io.File;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.content.Context;
import android.net.Uri;
import android.media.MediaRecorder;
import com.android.soundrecorder.Recorder;
import java.util.Properties;
import com.android.soundrecorder.SoundRecorder;
import com.android.soundrecorder.ConfigurableRecorder;

/**
 * Junit / Instrumentation test case for soundrecorder tests
 * 
 * Running the test suite:
 * 
 * adb shell am instrument \ -e class
 * com.android.soundrecorder.tests.AudioRecordingTests \ -w
 * com.android.soundrecorder.tests/android.test.InstrumentationTestRunner
 * 
 */

public class AudioRecordingTests extends ActivityInstrumentationTestCase2<SoundRecorder>
        implements Recorder.OnStateChangedListener {
    private static final String TAG = "AudioRecordingTests";
    private static final String RECORDINGS_DIR = Environment
            .getExternalStorageDirectory().getAbsolutePath();
    private static final String RECORDING_PREFIX = "testrecording";
    private static final String CONFIG_DIRECTORY = Environment
            .getExternalStorageDirectory().getAbsolutePath();
    private AudioManager mAudioManager;
    private Context mContext;
    private TestConfigurationManager mTestConfigurationManager;

    private ConfigurableRecorder mRecorder;

    public AudioRecordingTests() {
        super(SoundRecorder.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Instrumentation inst = getInstrumentation();
        mContext = inst.getTargetContext();
        // initialize AudioManager
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mTestConfigurationManager = new TestConfigurationManager(getInstrumentation()
                .getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

    }

    private void checkSavedSample() {
        Log.v(TAG, "Sample length = " + mRecorder.sampleLength());
        Log.v(TAG, "Sample file = " + mRecorder.sampleFile());
        assertTrue("sample Length is 0 ", (mRecorder.sampleLength() != 0));

    }

    public void onStateChanged(int state) {
        Log.v(TAG, "Recorder State Changed to: " + state);
    }

    public void onError(int error) {
        Log.v(TAG, "There was an error in the Recorder: " + error);
        assertTrue("Recorder error nr " + error, false);
    }

    public void audioRecording(Properties cProps) throws InterruptedException,
            IOException {
        Log.v(TAG, "Instrumentation test start");
        Log.v(TAG, "This test will verify that audio recording works as expected");

        String numberOFRecordings = cProps.getProperty("NumberOFRecordings");
        String recordingTime = cProps.getProperty("RecordingTime");
        String playbackTime = cProps.getProperty("PlaybackTime");
        String outputFormat = cProps.getProperty("OutputFormat");
        String audioEncoder = cProps.getProperty("AudioEncoder");
        String bitRate = cProps.getProperty("bitRate");
        String samplingRate = cProps.getProperty("samplingRate");
        String fileExtension = cProps.getProperty("Fileextension");
        String audioChannels = cProps.getProperty("AudioChannels");

        for (int i = 1; i <= Integer.parseInt(numberOFRecordings); i++) {
            Log.v(TAG, "iteration i= " + i);
            mRecorder = new ConfigurableRecorder();
            mRecorder.setOnStateChangedListener(this);
            mRecorder.setStaticParameters(RECORDING_PREFIX, RECORDINGS_DIR);
            Log.v(TAG, "Start recording");

            mRecorder.startRecording(Integer.parseInt(outputFormat),
                    Integer.parseInt(audioEncoder), Integer.parseInt(bitRate),
                    Integer.parseInt(samplingRate), fileExtension,
                    Integer.parseInt(audioChannels), mContext);

            // set recording time
            Thread.sleep(Long.parseLong(recordingTime));

            // stop recording
            mRecorder.stopRecording();
            checkSavedSample();

            // verify that PlaybackTime is smaller than songDuration before
            // playing
            assertTrue("PlaybackTime is not smaller than songDuration ",
                    (mRecorder.sampleLength() * 1000) > Long.parseLong(playbackTime));

            // play file to see if it is working
            mRecorder.startPlayback();

            // stop playback
            mRecorder.stopPlayback();

            // delete the recorded sample to prevent running out of space
            mRecorder.delete();
        }

        Log.v(TAG, "Instrumentation test stop");
    }

    public void testAudioRecording() throws InterruptedException, IOException {
        // pass a properties file to audioRecording method
        Properties audioProps = mTestConfigurationManager.getProperties(CONFIG_DIRECTORY);
        assertNotNull(TAG + " could not load config properties", audioProps);
        audioRecording(audioProps);
    }
}
