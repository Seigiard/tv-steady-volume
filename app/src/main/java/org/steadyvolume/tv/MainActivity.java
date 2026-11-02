package org.steadyvolume.tv;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

/** One screen, built for the D-pad: two check boxes, three preset tiles, one slider. */
public class MainActivity extends Activity {
    private static final long STATUS_DELAY_MS = 400;

    private Settings settings;
    private TextView status;
    private TextView tileHint;
    private TextView boostLabel;
    private View tileSoft;
    private View tileAuto;
    private View tileNight;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refreshStatus = this::showStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = new Settings(this);

        status = findViewById(R.id.status);
        tileHint = findViewById(R.id.tile_hint);
        boostLabel = findViewById(R.id.boost_label);
        tileSoft = findViewById(R.id.tile_soft);
        tileAuto = findViewById(R.id.tile_auto);
        tileNight = findViewById(R.id.tile_night);
        CheckBox boot = findViewById(R.id.boot);
        CheckBox speech = findViewById(R.id.speech);
        SeekBar boost = findViewById(R.id.boost);

        boot.setChecked(settings.startOnBoot());
        boot.setOnCheckedChangeListener((v, on) -> settings.setStartOnBoot(on));

        speech.setChecked(settings.speech());
        speech.setOnCheckedChangeListener((v, on) -> {
            settings.setSpeech(on);
            sync();
        });

        bindTile(tileSoft, Settings.PRESET_SOFT);
        bindTile(tileAuto, Settings.PRESET_AUTO);
        bindTile(tileNight, Settings.PRESET_NIGHT);
        showTiles();

        boost.setMax(Settings.GAIN_MAX_DB);
        boost.setKeyProgressIncrement(1);
        boost.setProgress(settings.gainDb());
        showBoost(settings.gainDb());
        boost.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                showBoost(value);
                if (fromUser) {
                    settings.setGainDb(value);
                    sync();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar bar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar bar) {
            }
        });

        focusedTile().requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sync();
    }

    /** A tile selects its preset. The lit tile pressed again turns levelling off. */
    private void bindTile(View tile, String preset) {
        tile.setOnClickListener(v -> {
            settings.setPreset(preset.equals(settings.preset()) ? Settings.PRESET_OFF : preset);
            showTiles();
            sync();
        });
    }

    private void showTiles() {
        String p = settings.preset();
        tileSoft.setSelected(Settings.PRESET_SOFT.equals(p));
        tileAuto.setSelected(Settings.PRESET_AUTO.equals(p));
        tileNight.setSelected(Settings.PRESET_NIGHT.equals(p));
        switch (p) {
            case Settings.PRESET_SOFT:
                tileHint.setText(R.string.preset_soft_hint);
                break;
            case Settings.PRESET_AUTO:
                tileHint.setText(R.string.preset_auto_hint);
                break;
            case Settings.PRESET_NIGHT:
                tileHint.setText(R.string.preset_night_hint);
                break;
            default:
                tileHint.setText(R.string.preset_hint_off);
        }
    }

    private View focusedTile() {
        switch (settings.preset()) {
            case Settings.PRESET_SOFT:
                return tileSoft;
            case Settings.PRESET_NIGHT:
                return tileNight;
            default:
                return tileAuto;
        }
    }

    private void sync() {
        LevelerService.sync(this);
        handler.removeCallbacks(refreshStatus);
        handler.postDelayed(refreshStatus, STATUS_DELAY_MS);
    }

    private void showStatus() {
        if (!settings.enabled()) {
            status.setText(R.string.status_off);
        } else if (LevelerService.isRunning()) {
            status.setText(R.string.status_running);
        } else if (LevelerService.lastError() != null) {
            status.setText(getString(R.string.status_error) + "\n" + LevelerService.lastError());
        } else {
            status.setText(R.string.status_off);
        }
    }

    private void showBoost(int db) {
        boostLabel.setText(getString(R.string.boost) + ": +" + db + " dB");
    }
}
