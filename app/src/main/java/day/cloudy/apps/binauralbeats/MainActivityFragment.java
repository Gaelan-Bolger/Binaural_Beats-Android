package day.cloudy.apps.binauralbeats;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.primitives.Shorts;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivityFragment extends Fragment {

    private static final ImmutableMap<Double, String> tones = ImmutableMap.<Double, String>builder()
            .put(55.00, "A1").put(61.74, "B1").put(65.41, "C2").put(73.41, "D2").put(82.41, "E2").put(87.31, "F2").put(98.00, "G2")
            .put(110.00, "A2").put(123.47, "B2").put(130.81, "C3").put(146.83, "D3").put(164.81, "E3").put(174.61, "F3").put(196.00, "G3")
            .put(220.00, "A3").put(246.94, "B3").put(261.63, "C4").put(293.66, "D4").put(329.63, "E4").put(349.23, "F4").put(392.00, "G4")
            .put(440.00, "A4").put(493.88, "B4").put(523.25, "C5").put(587.33, "D5").put(659.25, "E5").put(698.46, "F5").put(783.99, "G5")
            .put(880.00, "A5")
            .build();
    private static final int SAMPLE_RATE = 8000;
    private static final double DEFAULT_FREQUENCY = 440.00; // A4
    private static final int MAX_AMPLITUDE = 32767;
    private static final float DEFAULT_AMPLITUDE_FACTOR = 0.95f;

    @Bind(R.id.left_freq_text)
    TextView mLeftFreqText;
    @Bind(R.id.left_freq_bar)
    SeekBar mLeftFreqBar;
    @Bind(R.id.left_amp_text)
    TextView mLeftAmpText;
    @Bind(R.id.left_amp_bar)
    SeekBar mLeftAmpBar;

    @Bind(R.id.right_freq_text)
    TextView mRightText;
    @Bind(R.id.right_freq_bar)
    SeekBar mRightBar;
    @Bind(R.id.right_amp_text)
    TextView mRightAmpText;
    @Bind(R.id.right_amp_bar)
    SeekBar mRightAmpBar;

    @Bind(R.id.toggle)
    ToggleButton mToggle;

    private boolean isPlaying = false;
    private int minFreqProgress = 5500;
    private int maxFreqProgress = 88000 - minFreqProgress;

    private short sampleL[] = new short[SAMPLE_RATE];
    private short sampleR[] = new short[SAMPLE_RATE];
    private double twoPi = 8.0 * Math.atan(1.0);
    private double phL = 0.0;
    private double phR = 0.0;
    private double freqL = DEFAULT_FREQUENCY;
    private float ampL = DEFAULT_AMPLITUDE_FACTOR;
    private double freqR = DEFAULT_FREQUENCY;
    private float ampR = DEFAULT_AMPLITUDE_FACTOR;
    private AudioTrack audioTrack;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                SAMPLE_RATE, AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, SAMPLE_RATE,
                AudioTrack.MODE_STREAM);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);

        mLeftFreqBar.setMax(maxFreqProgress);
        mLeftFreqBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                freqL = getNearestWholeNoteFrequency((progress + minFreqProgress) / 100.0);
                seekBar.setProgress((int) (freqL * 100.0) - minFreqProgress);
                updateFrequencyText(mLeftFreqText, freqL);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mLeftFreqBar.setProgress((int) (freqL * 100.0) - minFreqProgress);

        mLeftAmpBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ampL = progress / 100.0f;
                updateAmplitudeText(mLeftAmpText, mLeftAmpBar);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mLeftAmpBar.setProgress((int) (ampL * 100));

        mRightBar.setMax(maxFreqProgress);
        mRightBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                freqR = getNearestWholeNoteFrequency((progress + minFreqProgress) / 100.0);
                seekBar.setProgress((int) (freqR * 100.0) - minFreqProgress);
                updateFrequencyText(mRightText, freqR);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mRightBar.setProgress((int) (freqR * 100.0) - minFreqProgress);

        mRightAmpBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ampR = progress / 100.0f;
                updateAmplitudeText(mRightAmpText, mRightAmpBar);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mRightAmpBar.setProgress((int) (ampR * 100));

        mToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startPlaying();
                } else {
                    stopPlaying();
                }
            }
        });
        return view;
    }

    @Override
    public void onPause() {
        stopPlaying();
        audioTrack.release();
        audioTrack = null;

        super.onPause();
    }

    private void updateFrequencyText(TextView textView, double freq) {
        textView.setText(String.format(Locale.getDefault(), "%s (%.2f Hz)", getNoteLabelForFrequency(freq), freq));
    }

    private void updateAmplitudeText(TextView textView, SeekBar seekBar) {
        textView.setText(String.valueOf(seekBar.getProgress()));
    }

    void startPlaying() {
        audioTrack.play();
        isPlaying = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isPlaying) {
                    final short[] tone = generateTone(freqL, freqR);
                    audioTrack.write(tone, 0, tone.length);
                }
            }
        }).start();
    }

    void stopPlaying() {
        isPlaying = false;

        audioTrack.stop();
        audioTrack.flush();
    }

    /*
    * https://audioprograming.wordpress.com/2012/10/18/a-simple-synth-in-android-step-by-step-guide-using-the-java-sdk/
    * http://dsp.stackexchange.com/a/973
    */
    short[] generateTone(double freqL, double freqR) {
        for (int i = 0; i < SAMPLE_RATE; ++i) {
            sampleL[i] = (short) ((MAX_AMPLITUDE * ampL) * Math.sin(phL));
            phL += twoPi * freqL / SAMPLE_RATE;
            sampleR[i] = (short) ((MAX_AMPLITUDE * ampR) * Math.sin(phR));
            phR += twoPi * freqR / SAMPLE_RATE;
        }

        List<Short> sampleList = Lists.newArrayListWithCapacity(SAMPLE_RATE * 2);
        for (int i = 0; i < sampleL.length; i++) {
            sampleList.add(sampleL[i]);
            sampleList.add(sampleR[i]);
        }
        return Shorts.toArray(sampleList);
    }

    double getNearestWholeNoteFrequency(double progress) {
        double freq = 0;
        List<Map.Entry<Double, String>> tonesList = Lists.newArrayList(tones.entrySet());
        for (int j = 1; j < tonesList.size(); j++) {
            if (progress < tonesList.get(j).getKey()) {
                freq = tonesList.get(j - 1).getKey();
                break;
            }
        }
        if (freq == 0) {
            freq = tonesList.get(tonesList.size() - 1).getKey();
        }
        return freq;
    }

    String getNoteLabelForFrequency(double freq) {
        ImmutableSet<Map.Entry<Double, String>> entrySet = tones.entrySet();
        for (Map.Entry<Double, String> entry : entrySet) {
            if (entry.getKey() == freq)
                return entry.getValue();
        }
        return null;
    }
}
