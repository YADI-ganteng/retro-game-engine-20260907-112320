package com.retroengine.app;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONArray arr = new JSONArray(sb.toString());
                gameList.clear();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    String name = obj.getString("name");
                    String dl = obj.optString("download_url", "");
                    long size = obj.optLong("size", 0);
                    if (!dl.isEmpty()) gameList.add(new GameItem(name, dl, size));
                }

                runOnUiThread(() -> adapter.notifyDataSetChanged());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private String getExt(String n) {
        int d = n.lastIndexOf(".");
        return d == -1 ? "" : n.substring(d + 1).toLowerCase();
    }

    private void openGame(String name) {
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), name);
        
        String ext = getExt(name);
        
        if (ext.equals("html")) {
            // HTML5 - browser
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(f), "text/html");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try { startActivity(intent); } catch (Exception e) {}
            return;
        }
        
        if (ext.equals("apk")) {
            // APK - install
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(f), "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try { startActivity(intent); } catch (Exception e) {}
            return;
        }
        
        if (ext.equals("jar")) {
            // J2ME - buka di J2ME Loader
            if (isInstalled("ru.playsoftware.j2meloader")) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setPackage("ru.playsoftware.j2meloader");
                intent.setDataAndType(Uri.fromFile(f), "*/*");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try { startActivity(intent); } catch (Exception e) {}
            } else {
                Toast.makeText(this, "Install J2ME Loader dulu!", Toast.LENGTH_LONG).show();
                downloadJ2ME();
            }
            return;
        }
        
        // ROM - RetroArch
        if (isInstalled("com.retroarch")) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setPackage("com.retroarch");
            intent.setDataAndType(Uri.fromFile(f), "*/*");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try { startActivity(intent); } catch (Exception e) {}
        } else {
            Toast.makeText(this, "Install RetroArch dulu!", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isInstalled(String pkg) {
        try { getPackageManager().getPackageInfo(pkg, 0); return true; } catch (Exception e) { return false; }
    }

    private void downloadJ2ME() {
        String url = "https://github.com/nikita36078/J2ME-Loader/releases/latest/download/J2ME-Loader.apk";
        DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
        r.setTitle("J2ME Loader");
        r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "J2ME-Loader.apk");
        DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (dm != null) dm.enqueue(r);
    }

    private void download(String url, String name) {
        DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
        r.setTitle(name);
        r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
        r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (dm != null) {
            dm.enqueue(r);
            Toast.makeText(this, "Download: " + name, Toast.LENGTH_SHORT).show();
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
            h.downloadBtn.setOnClickListener(v -> download(g.url, g.name));
            h.openBtn.setOnClickListener(v -> openGame(g.name));
        }

        @Override
        public int getItemCount() { return gameList.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView name, size;
            Button downloadBtn, openBtn;
            VH(View v) {
                super(v);
                name = v.findViewById(R.id.gameName);
                size = v.findViewById(R.id.fileSize);
                downloadBtn = v.findViewById(R.id.downloadBtn);
                openBtn = v.findViewById(R.id.openBtn);
            }
        }
    }

    private String formatSize(long bytes) {
        if (bytes > 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        if (bytes > 1024) return String.format("%.0f KB", bytes / 1024.0);
        return bytes + " B";
    }
}