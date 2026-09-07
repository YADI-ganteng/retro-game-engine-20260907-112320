package com.retroengine.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;

public class GameActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String filePath = getIntent().getStringExtra("file_path");
        String fileExt = getIntent().getStringExtra("file_ext");
        
        File romFile = new File(filePath);
        
        if (!romFile.exists()) {
            Toast.makeText(this, "File tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        if (fileExt != null && fileExt.equals("html")) {
            openHtml(romFile);
            return;
        }
        
        if (fileExt != null && fileExt.equals("apk")) {
            installApk(romFile);
            return;
        }
        
        // Untuk ROM - buka di RetroArch atau emulator lain
        openRom(romFile);
    }
    
    private void openHtml(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "text/html");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Browser tidak tersedia", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void installApk(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Gagal install", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
    
    private void openRom(File romFile) {
        // Cek RetroArch
        if (isAppInstalled("com.retroarch")) {
            launchApp("com.retroarch", romFile);
            return;
        }
        
        // Cek emulator lain
        String[] emulators = {
            "com.retroarch",           // RetroArch
            "com.nesbox.retroarch",    // RetroArch alternative
            "com.emulator.nes",        // NES Emulator
            "com.gba.emulator",        // GBA Emulator
            "com.snes.emulator",       // SNES Emulator
        };
        
        for (String pkg : emulators) {
            if (isAppInstalled(pkg)) {
                launchApp(pkg, romFile);
                return;
            }
        }
        
        // Tidak ada emulator - buka Play Store RetroArch
        Toast.makeText(this, "Install RetroArch dari Play Store", Toast.LENGTH_LONG).show();
        openPlayStore("com.retroarch");
    }
    
    private boolean isAppInstalled(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private void launchApp(String packageName, File romFile) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setPackage(packageName);
            intent.setDataAndType(Uri.fromFile(romFile), "*/*");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Gagal buka emulator", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
    
    private void openPlayStore(String packageName) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + packageName));
            startActivity(intent);
        } catch (Exception e) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
            startActivity(intent);
        }
        finish();
    }
}