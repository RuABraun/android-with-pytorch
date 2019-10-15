package ark.testapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public TextView tv;
    public static String modeldir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testapp/model/";
    private AssetManager assetManager;

    private String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_PERMISSIONS_CODE = 200;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        tv = findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        assetManager = getResources().getAssets();

        ArrayList<String> need_permissions = new ArrayList<>();
        for(String perm: permissions) {
            if (ActivityCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                need_permissions.add(perm);
            }
        }

        if (need_permissions.size() > 0) {
            ActivityCompat.requestPermissions(this,
                    need_permissions.toArray(new String[need_permissions.size()]),
                    REQUEST_PERMISSIONS_CODE);
        }
    }

    @Override
    protected  void onStart() {
        super.onStart();
        Log.i("APP", "Model dir: " + modeldir);
        File f = new File(modeldir);
        if (f.exists()) {
            Log.i("APP", "The dir exists!");
        } else {
            boolean created = f.mkdirs();
            if (created) {
                Log.i("APP", "now it exists");
            } else {
                Log.i("APP", "Still not");
            }
        }
        native_load(assetManager, modeldir);
        String fpath = modeldir + "/traced_model.pt";
        File fmodel = new File(fpath);
        if (fmodel.exists()) {
            tv.setText("Model exists!");
        }

    }

    public void onButtonPress(View view) {
        int retCode = runPytorch(modeldir);
        if (retCode == 0) {
            tv.setText("Model run successfully!");
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public native void native_load(AssetManager mgr, String modeldir);

    public native int runPytorch(String modeldir);
}
