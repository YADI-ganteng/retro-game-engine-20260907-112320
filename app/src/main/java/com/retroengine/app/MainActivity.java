package com.retroengine.app;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    final String GITHUB_USER = "YADI-ganteng";
    final String GITHUB_REPO = "retro-game-engine-20260907-112320";
    final String FOLDER = "games";

    RecyclerView recyclerView;
    GameAdapter adapter;
    List<GameItem> gameList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        adapter = new GameAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadGames();
    }

    private void loadGames() {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.github.com/repos/" + GITHUB_USER + "/" + GITHUB_REPO + "/contents/" + FOLDER);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "RetroEngine");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                
                int responseCode = conn.getResponseCode();
                
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();
                    conn.disconnect();

                    JSONArray arr = new JSONArray(sb.toString());
                    gameList.clear();
                    
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        String name = obj.getString("name");
                        String dl = obj.optString("download_url", "");
                        long size = obj.optLong("size", 0);
                        
                        if (!dl.isEmpty() && !name.equals("README.md")) {
                            gameList.add(new GameItem(name, dl, size));
                        }
                    }

                    runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                        if (gameList.isEmpty()) {
                            Toast.makeText(this, "Tidak ada game", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, gameList.size() + " game ditemukan", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "HTTP " + responseCode, Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private String getExt(String name) {
        int dot = name.lastIndexOf(".");
        return dot == -1 ? "" : name.substring(dot + 1).toLowerCase();
    }

    private File getDownloadedFile(String name) {
        // Cek external storage
        File extFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), name);
        if (extFile.exists()) return extFile;
        
        // Cek internal storage
        File intFile = new File(getExternalFilesDir(null), name);
        if (intFile.exists()) return intFile;
        
        return null;
    }

    private void downloadFile(String url, String name) {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle("Downloading " + name);
            request.setDescription("Retro Engine");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(true);
            
            DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            
            if (dm != null) {
                long downloadId = dm.enqueue(request);
                Toast.makeText(this, "Download dimulai! ID: " + downloadId, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Gagal download: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void playGame(String name) {
        File file = getDownloadedFile(name);
        
        if (file == null) {
            Toast.makeText(this, "Download dulu: " + name, Toast.LENGTH_LONG).show();
            return;
        }
        
        String ext = getExt(name);
        
        switch (ext) {
            case "html":
            case "htm":
                openHtml(file);
                break;
            case "apk":
                installApk(file);
                break;
            case "jar":
                openJ2ME(file);
                break;
            case "nes":
            case "gba":
            case "gb":
            case "gbc":
            case "snes":
            case "smc":
            case "md":
            case "gen":
                openRetroArch(file);
                break;
            default:
                Toast.makeText(this, "Format " + ext + " tidak didukung", Toast.LENGTH_SHORT).show();
        }
    }

    private void openHtml(File file) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            intent.setDataAndType(uri, "text/html");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Browser tidak tersedia", Toast.LENGTH_SHORT).show();
        }
    }

    private void installApk(File file) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            }
            
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Gagal install: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openJ2ME(File file) {
        String[] j2mePackages = {
            "ru.playsoftware.j2meloader",
            "com.j2meloader",
        };
        
        for (String pkg : j2mePackages) {
            try {
                getPackageManager().getPackageInfo(pkg, 0);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setPackage(pkg);
                intent.setDataAndType(Uri.fromFile(file), "*/*");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return;
            } catch (Exception e) {
                // Package tidak ada
            }
        }
        
        Toast.makeText(this, "Install J2ME Loader dulu!", Toast.LENGTH_LONG).show();
        openPlayStore("ru.playsoftware.j2meloader");
    }

    private void openRetroArch(File file) {
        String[] retroPackages = {
            "com.retroarch",
            "com.retroarch.aarch64",
        };
        
        for (String pkg : retroPackages) {
            try {
                getPackageManager().getPackageInfo(pkg, 0);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setPackage(pkg);
                intent.setDataAndType(Uri.fromFile(file), "*/*");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return;
            } catch (Exception e) {
                // Package tidak ada
            }
        }
        
        Toast.makeText(this, "Install RetroArch dulu!", Toast.LENGTH_LONG).show();
        openPlayStore("com.retroarch");
    }

    private void openPlayStore(String packageName) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + packageName));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
                startActivity(intent);
            } catch (Exception ex) {
                Toast.makeText(this, "Play Store tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class GameItem {
        String name, url;
        long size;
        GameItem(String n, String u, long s) { name = n; url = u; size = s; }
    }

    private class GameAdapter extends RecyclerView.Adapter<GameAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_game, p, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            GameItem g = gameList.get(pos);
            h.name.setText(g.name);
            h.size.setText(formatSize(g.size));
            h.downloadBtn.setOnClickListener(v -> downloadFile(g.url, g.name));
            h.playBtn.setOnClickListener(v -> playGame(g.name));
        }

        @Override
        public int getItemCount() { return gameList.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView name, size;
            Button downloadBtn, playBtn;
            VH(View v) {
                super(v);
                name = v.findViewById(R.id.gameName);
                size = v.findViewById(R.id.fileSize);
                downloadBtn = v.findViewById(R.id.downloadBtn);
                playBtn = v.findViewById(R.id.playBtn);
            }
        }
    }

    private String formatSize(long bytes) {
        if (bytes > 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        if (bytes > 1024) return String.format("%.0f KB", bytes / 1024.0);
        return bytes + " B";
    }
}