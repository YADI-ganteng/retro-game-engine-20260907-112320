
package com.retroengine.app;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
    final String FOLDER_GAME = "games";

    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefresh;
    EditText searchBar;
    ProgressBar progressBar;
    GameAdapter adapter;
    List<GameItem> gameList = new ArrayList<>();
    List<GameItem> filteredList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        searchBar = findViewById(R.id.searchBar);
        progressBar = findViewById(R.id.progressBar);

        adapter = new GameAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadGames();
        swipeRefresh.setOnRefreshListener(() -> { loadGames(); swipeRefresh.setRefreshing(false); });
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadGames() {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                URL url = new URL("https://api.github.com/repos/" + GITHUB_USER + "/" + GITHUB_REPO + "/contents/" + FOLDER_GAME);
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

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    filteredList.clear();
                    filteredList.addAll(gameList);
                    adapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                runOnUiThread(() -> { progressBar.setVisibility(View.GONE); Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show(); });
            }
        }).start();
    }

    private void filter(String q) {
        filteredList.clear();
        if (q.isEmpty()) filteredList.addAll(gameList);
        else for (GameItem g : gameList) if (g.name.toLowerCase().contains(q.toLowerCase())) filteredList.add(g);
        adapter.notifyDataSetChanged();
    }

    private String getExt(String n) { int d = n.lastIndexOf("."); return d == -1 ? "" : n.substring(d+1).toLowerCase(); }

    private void play(String name) {
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), name);
        if (!f.exists()) { Toast.makeText(this, "Download dulu!", Toast.LENGTH_SHORT).show(); return; }
        Intent i = new Intent(this, GameActivity.class);
        i.putExtra("file_path", f.getAbsolutePath());
        i.putExtra("file_ext", getExt(name));
        startActivity(i);
    }

    private void download(String url, String name) {
        DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
        r.setTitle(name);
        r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
        r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (dm != null) { dm.enqueue(r); Toast.makeText(this, "Download dimulai!", Toast.LENGTH_SHORT).show(); }
    }

    private class GameItem { String name, url; long size; GameItem(String n, String u, long s) { name=n; url=u; size=s; } }

    private class GameAdapter extends RecyclerView.Adapter<GameAdapter.VH> {
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_game, p, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            GameItem g = filteredList.get(pos);
            h.name.setText(g.name);
            h.ext.setText(getExt(g.name).toUpperCase());
            h.dl.setOnClickListener(v -> download(g.url, g.name));
            h.play.setOnClickListener(v -> play(g.name));
        }
        @Override public int getItemCount() { return filteredList.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView name, ext; Button dl, play;
            VH(View v) { super(v); name = v.findViewById(R.id.gameName); ext = v.findViewById(R.id.fileType); dl = v.findViewById(R.id.downloadBtn); play = v.findViewById(R.id.playBtn); }
        }
    }
}
