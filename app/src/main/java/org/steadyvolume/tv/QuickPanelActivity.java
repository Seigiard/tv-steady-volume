package org.steadyvolume.tv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import org.steadyvolume.tv.Leveler.Preset;

import java.util.EnumMap;
import java.util.Map;

public class QuickPanelActivity extends Activity {
    private static final long SLIDER_SETTLE_MS = 300;

    private Settings settings;
    private TextView tileHint;
    private TextView boostLabel;
    private final Map<Preset, View> tiles = new EnumMap<>(Preset.class);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean syncPending;
    private final Runnable settledSync = () -> {
        syncPending = false;
        LevelerService.sync(this);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_panel);
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        settings = new Settings(this);

        tileHint = findViewById(R.id.tile_hint);
        boostLabel = findViewById(R.id.boost_label);
        CheckBox speech = findViewById(R.id.speech);
        SeekBar boost = findViewById(R.id.boost);

        speech.setChecked(settings.speech());
        speech.setOnCheckedChangeListener((v, on) -> {
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
                if (fromUser) {
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
        LevelerService.sync(this);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(settledSync);
        if (syncPending) {
            settledSync.run();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isFinishing()) {
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        finish();
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

    private void showBoost(int db) {
        boostLabel.setText(getString(R.string.boost_value, db));
    }
}
