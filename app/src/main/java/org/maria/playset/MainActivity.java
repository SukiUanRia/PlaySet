package org.maria.playset;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {

    private TextView tvGooglePlayServicesVersion;
    private TextView tvGooglePlayStoreVersion;
    private TextView tvGoogleServicesFrameworkVersion;
    private TextView tvGoogleSystemUpdateVersion;
    private TextView tvDeviceModel; // 新增：设备型号 TextView
    private TextView tvAndroidVersion; // 新增：Android 版本 TextView
    private Button btnOpenGoogleServicesSettings;
    private Button btnOpenFcmDiagnostics;

    private static final int REQUEST_CODE_SETTINGS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyAppTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化 UI 元素
        tvDeviceModel = findViewById(R.id.tv_device_model); // 初始化设备型号 TextView
        tvAndroidVersion = findViewById(R.id.tv_android_version); // 初始化 Android 版本 TextView
        tvGooglePlayServicesVersion = findViewById(R.id.tv_google_play_services_version);
        tvGooglePlayStoreVersion = findViewById(R.id.tv_google_play_store_version);
        tvGoogleServicesFrameworkVersion = findViewById(R.id.tv_google_services_framework_version);
        tvGoogleSystemUpdateVersion = findViewById(R.id.tv_google_system_update_version);
        btnOpenGoogleServicesSettings = findViewById(R.id.btn_open_google_services_settings);
        btnOpenFcmDiagnostics = findViewById(R.id.btn_open_fcm_diagnostics);

        // 获取并显示版本信息
        displaySystemInfo(); // 新增：显示系统信息
        displayPackageVersions();

        // 设置按钮点击事件
        btnOpenGoogleServicesSettings.setOnClickListener(v -> openGooglePlayServicesSettings());
        btnOpenFcmDiagnostics.setOnClickListener(v -> openFcmDiagnostics());
    }

    /**
     * 根据 SharedPreferences 中的设置应用应用程序主题。
     */
    private void applyAppTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean monetEnabled = prefs.getBoolean("monet_enabled", true);
        String customThemeColor = prefs.getString("custom_theme_color", "default");

        if (!monetEnabled) {
            switch (customThemeColor) {
                case "red":
                    setTheme(R.style.AppTheme_CustomRed);
                    break;
                case "green":
                    setTheme(R.style.AppTheme_CustomGreen);
                    break;
                case "blue":
                    setTheme(R.style.AppTheme_CustomBlue);
                    break;
                case "yellow":
                    setTheme(R.style.AppTheme_CustomYellow);
                    break;
                case "orange":
                    setTheme(R.style.AppTheme_CustomOrange);
                    break;
                case "purple":
                    setTheme(R.style.AppTheme_CustomPurple);
                    break;
                case "cyan":
                    setTheme(R.style.AppTheme_CustomCyan);
                    break;
                default:
                    setTheme(R.style.AppTheme_Transparent);
                    break;
            }
        } else {
            setTheme(R.style.AppTheme_Transparent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SETTINGS);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SETTINGS) {
            recreate();
        }
    }

    /**
     * 获取并显示设备型号和 Android 版本信息。
     */
    private void displaySystemInfo() {
        String deviceModel = Build.MODEL;
        String androidVersion = Build.VERSION.RELEASE;
        tvDeviceModel.setText("设备型号: " + deviceModel);
        tvAndroidVersion.setText("Android 版本: " + androidVersion);
    }

    /**
     * 获取并显示 Google 相关服务的版本信息。
     */
    private void displayPackageVersions() {
        PackageManager pm = getPackageManager();

        // Google Play 服务
        String playServicesPackage = "com.google.android.gms";
        String playServicesVersion = getPackageVersion(pm, playServicesPackage);
        tvGooglePlayServicesVersion.setText("Google Play 服务版本: " + playServicesVersion);

        // Google Play 商店
        String playStorePackage = "com.android.vending";
        String playStoreVersion = getPackageVersion(pm, playStorePackage);
        tvGooglePlayStoreVersion.setText("Google Play 商店版本: " + playStoreVersion);

        // Google 服务框架 (GMS Core)
        String servicesFrameworkPackage = "com.google.android.gsf";
        String servicesFrameworkVersion = getPackageVersion(pm, servicesFrameworkPackage);
        tvGoogleServicesFrameworkVersion.setText("Google 服务框架版本: " + servicesFrameworkVersion);

        // Google 系统更新版本 (尝试获取 com.google.android.modulemetadata 的版本)
        String googleSystemUpdatePackage = "com.google.android.modulemetadata";
        String systemUpdateVersion = getPackageVersion(pm, googleSystemUpdatePackage);
        tvGoogleSystemUpdateVersion.setText("Google 系统更新版本: " + systemUpdateVersion);
    }

    /**
     * 获取指定包名的应用程序版本信息。
     * @param pm PackageManager 实例。
     * @param packageName 要查询的包名。
     * @return 应用程序版本字符串，如果未找到则返回 "未安装 / 无法获取"。
     */
    private String getPackageVersion(PackageManager pm, String packageName) {
        try {
            PackageInfo pInfo = pm.getPackageInfo(packageName, 0);
            return pInfo.versionName + " (" + pInfo.versionCode + ")";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "未安装 / 无法获取";
        }
    }

    /**
     * 打开 Google Play 服务的特定设置活动。
     */
    private void openGooglePlayServicesSettings() {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(
                    "com.google.android.gms",
                    "com.google.android.gms.app.settings.GoogleSettingsLink"
            ));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "无法打开 Google Play 服务设置。", Toast.LENGTH_SHORT).show();
            try {
                Intent fallbackIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", "com.google.android.gms", null);
                fallbackIntent.setData(uri);
                startActivity(fallbackIntent);
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(this, "备用方案也失败了，无法打开设置。", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 尝试打开 FCM Diagnostics 界面。
     */
    private void openFcmDiagnostics() {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(
                    "com.google.android.gms",
                    "com.google.android.gms.gcm.GcmDiagnostics"
            ));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "无法打开 FCM Diagnostics 界面。", Toast.LENGTH_SHORT).show();
        }
    }
}
