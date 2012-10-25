/*
 * Copyright (C) 2009 The Android Open Source Project
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
package com.android.soundrecorder;

import java.io.File;
import java.io.IOException;
import android.content.Context;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * class extends com.android.soundrecorder.Recorder and overloads the
 * startRecording method to present extra configurability for tests
 */

public class ConfigurableRecorder extends Recorder {

    private static String RECORDING_PREFIX;
    private static String RECORDINGS_DIR;
    private static final String TAG = "ConfigurableRecorder";

    public void setStaticParameters(String recordingPrefix, String recordingDir) {
        RECORDING_PREFIX = recordingPrefix;
        RECORDINGS_DIR = recordingDir;
    }

    // method is almost the same as the original startRecording method in
    // the parrent Recorder class, but it is overloaded with more
    // configuration parameters to test the underlying recording options
    public void startRecording(int outputfileformat, int audioEncoder, int bitRate,
            int samplingRate, String extension, int audioChannels, Context context) {
        stop();

        // try to see if there is a problem with the sdcard
        try {
            mSampleFile = File.createTempFile(RECORDING_PREFIX, extension, new File(
                    RECORDINGS_DIR));
        } catch (IOException e) {
            mOnStateChangedListener.onError(SDCARD_ACCESS_ERROR);
        }

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(outputfileformat);
        mRecorder.setAudioEncoder(audioEncoder);
        mRecorder.setAudioEncodingBitRate(bitRate);
        mRecorder.setAudioSamplingRate(samplingRate);
        mRecorder.setAudioChannels(audioChannels);
        mRecorder.setOutputFile(mSampleFile.getAbsolutePath());

        // try to see if there is a problem with mediaplayer setup
        try {
            mRecorder.prepare();
        } catch (IOException exception) {
            mOnStateChangedListener.onError(INTERNAL_ERROR);
        }

        // there can be runtimeexceptions like an incoming call
        try {
            mRecorder.start();
        } catch (RuntimeException exception) {
            mOnStateChangedListener.onError(INTERNAL_ERROR);
        }
        mSampleStart = System.currentTimeMillis();
        setState(RECORDING_STATE);
    }

    // the following 3 methods are from the com.android.soundrecorder.Recorder
    // class. They are private in the parrent class so they need to be rewritten
    // to be used
    private void setState(int state) {
        Log.v(TAG, "Recorder State Changed to: " + state);
        mState = state;
    }

    // method identical to the one in com.android.soundrecorder.Recorder
    private void signalStateChanged(int state) {
        if (mOnStateChangedListener != null)
            mOnStateChangedListener.onStateChanged(state);
    }

    // method identical to the one in com.android.soundrecorder.Recorder
    private void setError(int error) {
        if (mOnStateChangedListener != null)
            mOnStateChangedListener.onError(error);
    }
}