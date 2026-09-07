package com.retroengine.app;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {
    
    static {
        System.loadLibrary("retrofrontend");
    }
    
    private native int retroLoadRom(String romPath, String coreName);
    private native int retroRun();
    private native void retroStop();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String filePath = getIntent().getStringExtra("file_path");
        String fileExt = getIntent().getStringExtra("file_ext");
        
        String coreName = getCoreForExt(fileExt);
        
        if (coreName == null) {
            Toast.makeText(this, "Format tidak didukung", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        Toast.makeText(this, "Loading: " + coreName, Toast.LENGTH_SHORT).show();
        
        int result = retroLoadRom(filePath, coreName);
        
        if (result == 0) {
            retroRun();
        } else {
            Toast.makeText(this, "Gagal load ROM", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private String getCoreForExt(String ext) {
        switch (ext) {
            case "nes": return "nestopia";
            case "gba": return "mgba";
            case "gb": case "gbc": return "gambatte";
            case "snes": case "smc": return "snes9x";
            case "md": case "gen": return "genesis_plus_gx";
            case "psx": return "pcsx_rearmed";
            default: return null;
        }
    }
    
    @Override
    protected void onDestroy() {
        retroStop();
        super.onDestroy();
    }
}