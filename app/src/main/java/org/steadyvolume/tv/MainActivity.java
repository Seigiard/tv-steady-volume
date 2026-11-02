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
import org.steadyvolume.tv.LevelerService.Status;

import java.util.EnumMap;
import java.util.Map;

/** One screen, built for the D-pad: two check boxes, four preset tiles, one slider. */
public class MainActivity extends Activity {
    /** D-pad steps on the slider arrive in bursts; the leveller is updated once the burst ends. */
    private static final long SLIDER_SETTLE_MS = 300;

    private Settings settings;
    private TextView status;
    private TextView tileHint;
    private TextView boostLabel;
    private CheckBox speech;
    private SeekBar boost;
    private final Map<Preset, View> tiles = new EnumMap<>(Preset.class);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean syncPending;
    private boolean refreshing;
    private final Runnable settledSync = () -> {
        syncPending = false;
        LevelerService.sync(this);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = new Settings(this);

        status = findViewById(R.id.status);
        tileHint = findViewById(R.id.tile_hint);
        boostLabel = findViewById(R.id.boost_label);
        CheckBox boot = findViewById(R.id.boot);
        speech = findViewById(R.id.speech);
        boost = findViewById(R.id.boost);

        boot.setChecked(settings.startOnBoot());
        boot.setOnCheckedChangeListener((v, on) -> settings.setStartOnBoot(on));

        speech.setChecked(settings.speech());
        speech.setOnCheckedChangeListener((v, on) -> {
            if (refreshing) {
                return;
            }
            settings.setSpeech(on);
            LevelerService.sync(this);
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
                if (fromUser && !refreshing) {
                    settings.setGainDb(value);
                    syncPending = true;
                    handler.removeCallbacks(settledSync);
                    handler.postDelayed(settledSync, SLIDER_SETTLE_MS);
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
        refreshing = true;
        showTiles();
        int gainDb = settings.gainDb();
        boost.setProgress(gainDb);
        showBoost(gainDb);
        speech.setChecked(settings.speech());
        refreshing = false;
        LevelerService.setStatusListener(this::showStatus);
        LevelerService.sync(this);
    }

    @Override
    protected void onPause() {
        LevelerService.setStatusListener(null);
        if (syncPending) {
            handler.removeCallbacks(settledSync);
            settledSync.run();
        }
        super.onPause();
    }

    private void bindTile(int viewId, Preset preset) {
        View tile = findViewById(viewId);
        tiles.put(preset, tile);
        tile.setOnClickListener(v -> {
            settings.setPreset(preset);
            showTiles();
            LevelerService.sync(this);
        });
    }

    private void showTiles() {
        Preset p = settings.preset();
        for (Map.Entry<Preset, View> e : tiles.entrySet()) {
            e.getValue().setSelected(e.getKey() == p);
        }
        tileHint.setText(p.hintRes);
    }

    private void showStatus(Status s) {
        switch (s.kind) {
            case RUNNING:
                status.setText(R.string.status_running);
                break;
            case FAILED:
                status.setText(getString(R.string.status_error_detail, s.reason));
                break;
            default:
                status.setText(R.string.status_off);
        }
    }

    private void showBoost(int db) {
        boostLabel.setText(getString(R.string.boost_value, db));
    }
}
