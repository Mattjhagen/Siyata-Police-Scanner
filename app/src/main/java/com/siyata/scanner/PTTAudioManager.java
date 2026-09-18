package com.siyata.scanner;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.AudioManager;
import android.util.Log;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PTTAudioManager {
    private static final String TAG = "PTTAudioManager";
    private static final int SAMPLE_RATE = 48000;
    private static final int CHANNEL_CONFIG_IN = AudioFormat.CHANNEL_IN_MONO;
    private static final int CHANNEL_CONFIG_OUT = AudioFormat.CHANNEL_OUT_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int CHUNK_SIZE = 4096;

    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private BlockingQueue<byte[]> audioQueue;

    private PTTWebSocketClient webSocketClient;

    public PTTAudioManager(PTTWebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
        this.audioQueue = new LinkedBlockingQueue<>();
        initAudioTrack();
    }

    // Initialize AudioTrack for playback
    private void initAudioTrack() {
        int bufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG_OUT,
            AUDIO_FORMAT
        );

        audioTrack = new AudioTrack(
            AudioManager.STREAM_MUSIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG_OUT,
            AUDIO_FORMAT,
            bufferSize,
            AudioTrack.MODE_STREAM
        );
    }

    // Start recording and transmitting audio (PTT pressed)
    public void startTransmission() {
        if (isRecording) return;

        try {
            int bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG_IN,
                AUDIO_FORMAT
            );

            audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG_IN,
                AUDIO_FORMAT,
                bufferSize
            );

            audioRecord.startRecording();
            isRecording = true;

            // Notify server we're starting transmission
            webSocketClient.startTransmission();

            // Start recording thread
            new Thread(() -> {
                byte[] buffer = new byte[CHUNK_SIZE];

                while (isRecording) {
                    int bytesRead = audioRecord.read(buffer, 0, buffer.length);

                    if (bytesRead > 0) {
                        // Send audio data to WebSocket
                        byte[] chunk = new byte[bytesRead];
                        System.arraycopy(buffer, 0, chunk, 0, bytesRead);
                        webSocketClient.sendAudioData(chunk);
                    }
                }

                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;

                // Notify server we're done
                webSocketClient.endTransmission();

            }, "PTT-Recording-Thread").start();

            Log.d(TAG, "Started audio transmission");

        } catch (Exception e) {
            Log.e(TAG, "Error starting transmission", e);
            isRecording = false;
        }
    }

    // Stop recording (PTT released)
    public void stopTransmission() {
        isRecording = false;
        Log.d(TAG, "Stopped audio transmission");
    }

    // Start playing received audio
    public void startPlayback() {
        if (isPlaying) return;

        isPlaying = true;
        audioTrack.play();

        new Thread(() -> {
            while (isPlaying || !audioQueue.isEmpty()) {
                try {
                    byte[] audioData = audioQueue.poll();

                    if (audioData != null) {
                        audioTrack.write(audioData, 0, audioData.length);
                    } else {
                        Thread.sleep(10);
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }

            audioTrack.pause();
            audioTrack.flush();
            Log.d(TAG, "Playback stopped");

        }, "PTT-Playback-Thread").start();

        Log.d(TAG, "Started audio playback");
    }

    // Stop playing
    public void stopPlayback() {
        isPlaying = false;
        audioQueue.clear();
    }

    // Queue received audio data for playback
    public void queueAudioData(byte[] pcmData) {
        if (isPlaying) {
            audioQueue.offer(pcmData);
        }
    }

    // Cleanup
    public void release() {
        stopTransmission();
        stopPlayback();

        if (audioTrack != null) {
            audioTrack.release();
            audioTrack = null;
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}
