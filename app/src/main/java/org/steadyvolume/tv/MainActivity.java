package org.steadyvolume.tv;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import org.steadyvolume.tv.Leveler.Preset;

import java.util.EnumMap;
import java.util.Map;

/** One screen, built for the D-pad: two check boxes, four preset tiles, one slider. */
public class MainActivity extends Activity {
    private static final long STATUS_DELAY_MS = 400;

    private Settings settings;
    private TextView status;
    private TextView tileHint;
    private TextView boostLabel;
    private final Map<Preset, View> tiles = new EnumMap<>(Preset.class);
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

        bindTile(R.id.tile_off, Preset.OFF);
        bindTile(R.id.tile_light, Preset.LIGHT);
        bindTile(R.id.tile_auto, Preset.AUTO);
        bindTile(R.id.tile_night, Preset.NIGHT);
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

        tiles.get(settings.preset()).requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sync();
    }

    private void bindTile(int viewId, Preset preset) {
        View tile = findViewById(viewId);
        tiles.put(preset, tile);
        tile.setOnClickListener(v -> {
            settings.setPreset(preset);
            showTiles();
            sync();
        });
    }

    private void showTiles() {
        Preset p = settings.preset();
        for (Map.Entry<Preset, View> e : tiles.entrySet()) {
            e.getValue().setSelected(e.getKey() == p);
        }
        tileHint.setText(p.hintRes);
    }

    private void sync() {
        LevelerService.sync(this);
        handler.removeCallbacks(refreshStatus);
        handler.postDelayed(refreshStatus, STATUS_DELAY_MS);
    }

    private void showStatus() {
        if (settings.preset().isOff()) {
            status.setText(R.string.status_off);
        } else if (LevelerService.isRunning()) {
            status.setText(R.string.status_running);
        } else if (LevelerService.lastError() != null) {
            status.setText(getString(R.string.status_error_detail, LevelerService.lastError()));
        } else {
            status.setText(R.string.status_off);
        }
    }

    private void showBoost(int db) {
        boostLabel.setText(getString(R.string.boost_value, db));
    }
}
