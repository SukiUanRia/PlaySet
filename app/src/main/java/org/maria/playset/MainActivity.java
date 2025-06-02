package org.maria.playset;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // 导入 Button
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.android.material.button.MaterialButton; // 仍然需要 MaterialButton 用于对话框中的按钮

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private TextView tvGooglePlayServicesVersion;
    private TextView tvGooglePlayStoreVersion;
    private TextView tvGoogleServicesFrameworkVersion;
    private TextView tvGoogleSystemUpdateVersion;
    private TextView tvDeviceModel;
    private TextView tvAndroidVersion;
    private Button btnOpenGoogleServicesSettings; // 类型修正为 Button
    private Button btnOpenFcmDiagnostics; // 类型修正为 Button

    private LinearLayout llDynamicButtonsContainer;
    private Button btnAddApp; // 类型修正为 Button

    private SharedPreferences sharedPreferences;
    private Gson gson;

    private static final int REQUEST_CODE_SETTINGS = 1;
    private static final String PREF_KEY_CUSTOM_APPS = "custom_apps";

    // 用于防抖动的变量
    private long lastClickTime = 0;
    private static final long MIN_CLICK_INTERVAL = 500; // 最小点击间隔，单位毫秒

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyAppTheme(); // 在调用 super.onCreate() 之前应用主题

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        gson = new Gson();

        // 初始化 UI 元素
        tvDeviceModel = findViewById(R.id.tv_device_model);
        tvAndroidVersion = findViewById(R.id.tv_android_version);
        tvGooglePlayServicesVersion = findViewById(R.id.tv_google_play_services_version);
        tvGooglePlayStoreVersion = findViewById(R.id.tv_google_play_store_version);
        tvGoogleServicesFrameworkVersion = findViewById(R.id.tv_google_services_framework_version);
        tvGoogleSystemUpdateVersion = findViewById(R.id.tv_google_system_update_version);

        btnOpenGoogleServicesSettings = findViewById(R.id.btn_open_google_services_settings); // 类型修正
        btnOpenFcmDiagnostics = findViewById(R.id.btn_open_fcm_diagnostics); // 类型修正
        llDynamicButtonsContainer = findViewById(R.id.ll_dynamic_buttons_container);

        // 获取 XML 中定义的“添加应用”按钮并设置其点击事件
        btnAddApp = findViewById(R.id.btn_add_app_xml); // 类型修正
        setDebouncedOnClickListener(btnAddApp, v -> showAddAppDialog()); // 使用防抖动封装

        // 添加长按监听器以清除所有自定义应用
        btnAddApp.setOnLongClickListener(v -> {
            showClearAllAppsDialog();
            return true; // 消费长按事件
        });

        // 加载并显示已保存的应用按钮
        loadSavedApps();

        // 获取并显示版本信息
        displaySystemInfo();
        displayPackageVersions();

        // 设置现有按钮点击事件
        setDebouncedOnClickListener(btnOpenGoogleServicesSettings, v -> openGooglePlayServicesSettings()); // 使用防抖动封装
        setDebouncedOnClickListener(btnOpenFcmDiagnostics, v -> openFcmDiagnostics()); // 使用防抖动封装
    }

    /**
     * 检查是否允许点击（防抖动）。
     * @return 如果允许点击，则返回 true；否则返回 false。
     */
    private boolean isClickAllowed() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < MIN_CLICK_INTERVAL) {
            return false;
        }
        lastClickTime = currentTime;
        return true;
    }

    /**
     * 为 View 设置一个带有防抖动功能的点击监听器。
     * @param view 要设置监听器的 View。
     * @param listener 实际的点击监听器。
     */
    private void setDebouncedOnClickListener(View view, View.OnClickListener listener) {
        view.setOnClickListener(v -> {
            if (isClickAllowed()) {
                listener.onClick(v);
            }
        });
    }

    /**
     * 将 dp 值转换为像素值。
     */
    private int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    /**
     * 根据 SharedPreferences 中的设置应用应用程序主题。
     */
    private void applyAppTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean monetEnabled = prefs.getBoolean("monet_enabled", true);
        String customThemeColor = prefs.getString("custom_theme_color", "default");

        if (monetEnabled) {
            setTheme(R.style.Theme_App_Monet); // 启用莫奈取色时，使用专门的莫奈主题
        } else {
            // 禁用莫奈取色时，根据选择的自定义颜色应用主题
            switch (customThemeColor) {
                case "red":
                    setTheme(R.style.Theme_App_CustomRed);
                    break;
                case "green":
                    setTheme(R.style.Theme_App_CustomGreen);
                    break;
                case "blue":
                    setTheme(R.style.Theme_App_CustomBlue);
                    break;
                case "yellow":
                    setTheme(R.style.Theme_App_CustomYellow);
                    break;
                case "orange":
                    setTheme(R.style.Theme_App_CustomOrange);
                    break;
                case "purple":
                    setTheme(R.style.Theme_App_CustomPurple);
                    break;
                case "cyan":
                    setTheme(R.style.Theme_App_CustomCyan);
                    break;
                default:
                    setTheme(R.style.Theme_App_StaticDefault); // 默认静态主题
                    break;
            }
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
            // 为菜单项点击直接应用防抖动检查
            if (isClickAllowed()) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SETTINGS);
            }
            return true; // 消耗事件
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
     * 显示添加应用信息的对话框。
     */
    private void showAddAppDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_app, null);
        builder.setView(dialogView);

        EditText etAppName = dialogView.findViewById(R.id.et_app_name);
        EditText etPackageName = dialogView.findViewById(R.id.et_package_name);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save);
        MaterialButton btnPastePackage = dialogView.findViewById(R.id.btn_paste_package);
        MaterialButton btnSaveAndContinue = dialogView.findViewById(R.id.btn_save_and_continue);

        AlertDialog dialog = builder.create();

        setDebouncedOnClickListener(btnCancel, v -> dialog.dismiss());
        setDebouncedOnClickListener(btnSave, v -> {
            String appName = etAppName.getText().toString().trim();
            String packageName = etPackageName.getText().toString().trim();

            if (appName.isEmpty() || packageName.isEmpty()) {
                Toast.makeText(this, "应用名称和包名不能为空", Toast.LENGTH_SHORT).show();
            } else {
                addAppButton(appName, packageName, true);
                dialog.dismiss(); // 保存并关闭对话框
            }
        });

        setDebouncedOnClickListener(btnSaveAndContinue, v -> {
            String appName = etAppName.getText().toString().trim();
            String packageName = etPackageName.getText().toString().trim();

            if (appName.isEmpty() || packageName.isEmpty()) {
                Toast.makeText(this, "应用名称和包名不能为空", Toast.LENGTH_SHORT).show();
            } else {
                addAppButton(appName, packageName, true);
                etAppName.setText(""); // 清空应用名称输入框
                etPackageName.setText(""); // 清空包名输入框
                etAppName.requestFocus(); // 焦点回到应用名称输入框
                Toast.makeText(this, "'" + appName + "' 已保存，请继续添加", Toast.LENGTH_SHORT).show();
            }
        });

        setDebouncedOnClickListener(btnPastePackage, v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                if (item != null && item.getText() != null) {
                    etPackageName.setText(item.getText().toString());
                    Toast.makeText(this, "已从剪贴板粘贴", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "剪贴板中没有文本", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "剪贴板为空或无法访问", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    /**
     * 动态添加应用按钮到布局。
     * @param appName 应用名称。
     * @param packageName 应用包名。
     * @param save 是否将此应用保存到 SharedPreferences。
     */
    private void addAppButton(String appName, String packageName, boolean save) {
        MaterialButton appButton = new MaterialButton(this); // 动态添加的按钮仍然使用 MaterialButton
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(this, 80), // 按钮宽度，大约四个中文字符的宽度 + padding
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dpToPx(this, 8), 0, 0); // 按钮之间的上边距
        appButton.setLayoutParams(params);
        appButton.setText(appName);
        appButton.setSingleLine(true);
        appButton.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        appButton.setMarqueeRepeatLimit(-1);
        appButton.setFocusable(true);
        appButton.setFocusableInTouchMode(true);
        appButton.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);

        setDebouncedOnClickListener(appButton, v -> openApp(packageName));
        appButton.setOnLongClickListener(v -> {
            showAppOptionsDialog(appName, packageName, appButton);
            return true;
        });

        llDynamicButtonsContainer.post(() -> llDynamicButtonsContainer.addView(appButton));

        if (save) {
            saveApp(appName, packageName);
        }
    }

    /**
     * 显示应用选项的对话框，包括删除和添加到主屏幕。
     */
    private void showAppOptionsDialog(String appName, String packageName, MaterialButton buttonToRemove) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择操作");
        String[] options = {"删除", "添加到主屏幕"};

        builder.setItems(options, (dialog, which) -> {
            // 这里不需要 isClickAllowed()，因为这是 AlertDialog 的回调
            switch (which) {
                case 0: // 删除
                    showDeleteAppDialog(appName, packageName, buttonToRemove);
                    break;
                case 1: // 添加到主屏幕
                    addAppToHomeScreen(appName, packageName);
                    break;
            }
        });
        builder.show();
    }

    /**
     * 将应用添加到主屏幕。
     */
    private void addAppToHomeScreen(String appName, String packageName) {
        PackageManager pm = getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(packageName);

        if (launchIntent != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
                if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported()) {
                    Drawable iconDrawable = null;
                    try {
                        iconDrawable = pm.getApplicationIcon(packageName);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    Bitmap iconBitmap = null;
                    if (iconDrawable != null) {
                        iconBitmap = Bitmap.createBitmap(iconDrawable.getIntrinsicWidth(),
                                iconDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(iconBitmap);
                        iconDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                        iconDrawable.draw(canvas);
                    }

                    ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(this, packageName)
                            .setIntent(launchIntent.setAction(Intent.ACTION_MAIN))
                            .setShortLabel(appName)
                            .setLongLabel(appName)
                            .setIcon(iconBitmap != null ? Icon.createWithBitmap(iconBitmap) : Icon.createWithResource(this, R.mipmap.ic_launcher))
                            .build();

                    shortcutManager.requestPinShortcut(shortcutInfo, null);
                    Toast.makeText(this, "'" + appName + "' 已请求添加到主屏幕", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "您的启动器不支持固定快捷方式。", Toast.LENGTH_SHORT).show();
                }
            } else {
                Intent shortcutIntent = new Intent();
                shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
                shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName);

                Drawable iconDrawable = null;
                try {
                    iconDrawable = pm.getApplicationIcon(packageName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                if (iconDrawable != null) {
                    Bitmap iconBitmap = Bitmap.createBitmap(iconDrawable.getIntrinsicWidth(),
                            iconDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(iconBitmap);
                    iconDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    iconDrawable.draw(canvas);
                    shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, iconBitmap);
                } else {
                    shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_launcher));
                }

                shortcutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                sendBroadcast(shortcutIntent);
                Toast.makeText(this, "'" + appName + "' 已添加到主屏幕", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "无法为 '" + appName + "' 创建快捷方式，未找到启动器活动。", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 显示删除应用的确认对话框。
     */
    private void showDeleteAppDialog(String appName, String packageName, MaterialButton buttonToRemove) {
        new AlertDialog.Builder(this)
                .setTitle("删除应用")
                .setMessage("确定要删除 '" + appName + "' 吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    removeApp(appName, packageName);
                    llDynamicButtonsContainer.removeView(buttonToRemove);
                    Toast.makeText(this, "'" + appName + "' 已删除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 显示清除所有自定义应用的确认对话框。
     */
    private void showClearAllAppsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("清除所有自定义应用")
                .setMessage("确定要清除所有自定义添加的应用吗？此操作不可撤销。")
                .setPositiveButton("清除", (dialog, which) -> {
                    clearAllSavedApps();
                    llDynamicButtonsContainer.removeAllViews(); // 清除所有动态添加的按钮
                    llDynamicButtonsContainer.addView(btnAddApp); // 重新添加“添加应用”按钮
                    Toast.makeText(this, "所有自定义应用已清除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 将应用信息保存到 SharedPreferences。
     */
    private void saveApp(String appName, String packageName) {
        Set<String> customAppsJson = sharedPreferences.getStringSet(PREF_KEY_CUSTOM_APPS, new HashSet<>());
        AppInfo newApp = new AppInfo(appName, packageName);
        customAppsJson.add(gson.toJson(newApp));
        sharedPreferences.edit().putStringSet(PREF_KEY_CUSTOM_APPS, customAppsJson).apply();
    }

    /**
     * 从 SharedPreferences 中移除应用信息。
     */
    private void removeApp(String appName, String packageName) {
        Set<String> customAppsJson = sharedPreferences.getStringSet(PREF_KEY_CUSTOM_APPS, new HashSet<>());
        AppInfo appToRemove = new AppInfo(appName, packageName);
        customAppsJson.remove(gson.toJson(appToRemove)); // 移除对应的 JSON 字符串
        sharedPreferences.edit().putStringSet(PREF_KEY_CUSTOM_APPS, customAppsJson).apply();
    }

    /**
     * 从 SharedPreferences 中清除所有自定义应用信息。
     */
    private void clearAllSavedApps() {
        sharedPreferences.edit().remove(PREF_KEY_CUSTOM_APPS).apply();
    }

    /**
     * 从 SharedPreferences 加载所有已保存的应用并添加到布局。
     */
    private void loadSavedApps() {
        llDynamicButtonsContainer.removeAllViews(); // 首先清除所有动态添加的按钮
        llDynamicButtonsContainer.addView(btnAddApp); // 重新添加“添加应用”按钮

        Set<String> customAppsJson = sharedPreferences.getStringSet(PREF_KEY_CUSTOM_APPS, new HashSet<>());
        Type type = new TypeToken<AppInfo>(){}.getType();
        for (String appJson : customAppsJson) {
            AppInfo app = gson.fromJson(appJson, type);
            if (app != null) {
                addAppButton(app.getName(), app.getPackageName(), false); // 不再重复保存
            }
        }
    }

    /**
     * 打开指定包名的应用程序。
     */
    private void openApp(String packageName) {
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        if (intent != null) {
            try {
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "无法打开应用: " + packageName, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "未找到应用: " + packageName, Toast.LENGTH_SHORT).show();
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
        String playServicesPackage = "com.google.android.gms";
        String playServicesVersion = getPackageVersion(pm, playServicesPackage);
        tvGooglePlayServicesVersion.setText("Google Play 服务版本: " + playServicesVersion);

        String playStorePackage = "com.android.vending";
        String playStoreVersion = getPackageVersion(pm, playStorePackage);
        tvGooglePlayStoreVersion.setText("Google Play 商店版本: " + playStoreVersion);

        String servicesFrameworkPackage = "com.google.android.gsf";
        String servicesFrameworkVersion = getPackageVersion(pm, servicesFrameworkPackage);
        tvGoogleServicesFrameworkVersion.setText("Google 服务框架版本: " + servicesFrameworkVersion);

        String googleSystemUpdatePackage = "com.google.android.modulemetadata";
        String systemUpdateVersion = getPackageVersion(pm, googleSystemUpdatePackage);
        tvGoogleSystemUpdateVersion.setText("Google 系统更新版本: " + systemUpdateVersion);
    }

    /**
     * 获取指定包名的应用程序版本信息。
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

    // 用于 SharedPreferences 存储的应用信息数据类
    private static class AppInfo {
        private String name;
        private String packageName;

        public AppInfo(String name, String packageName) {
            this.name = name;
            this.packageName = packageName;
        }

        public String getName() {
            return name;
        }

        public String getPackageName() {
            return packageName;
        }

        // 覆盖 equals 和 hashCode 以便 Set 能够正确识别对象
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AppInfo appInfo = (AppInfo) o;
            return name.equals(appInfo.name) && packageName.equals(appInfo.packageName);
        }

        @Override
        public int hashCode() {
            return name.hashCode() + packageName.hashCode();
        }
    }
}
