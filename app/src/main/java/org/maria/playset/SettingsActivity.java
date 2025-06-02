package org.maria.playset; // 请根据您的项目包名修改

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
// import android.content.pm.PackageManager.PermissionInfo; // 移除此行
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private Switch monetToggle;
    private LinearLayout colorOptionsLayout;
    private SharedPreferences prefs;
    private TextView tvVersionInfo;
    private TextView tvContactInfo;
    private TextView tvDisclaimer; // 新增：免责声明 TextView
    private TextView tvPrivacyStatement; // 新增：隐私声明 TextView
    private TextView tvPermissions; // 新增：权限列表 TextView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyAppTheme(); // 在 super.onCreate() 之前应用主题
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 设置 Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("主题设置"); // 或您设置页面的合适标题
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // 初始化主题设置相关的 UI 元素
        monetToggle = findViewById(R.id.monet_toggle);
        colorOptionsLayout = findViewById(R.id.color_options_layout);

        // 初始化关于信息相关的 UI 元素
        tvVersionInfo = findViewById(R.id.tv_version_info);
        tvContactInfo = findViewById(R.id.tv_contact_info);
        tvDisclaimer = findViewById(R.id.tv_disclaimer); // 关联免责声明 TextView
        tvPrivacyStatement = findViewById(R.id.tv_privacy_statement); // 关联隐私声明 TextView
        tvPermissions = findViewById(R.id.tv_permissions); // 关联权限列表 TextView

        // 设置关于内容
        tvDisclaimer.setText(getString(R.string.disclaimer_content)); // 设置免责声明文本
        tvPrivacyStatement.setText(getString(R.string.privacy_statement_content)); // 设置隐私声明文本

        // 初始化莫奈取色开关状态
        boolean monetEnabled = prefs.getBoolean("monet_enabled", true);
        monetToggle.setChecked(monetEnabled);
        colorOptionsLayout.setVisibility(monetEnabled ? View.GONE : View.VISIBLE);

        monetToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("monet_enabled", isChecked).apply();
            colorOptionsLayout.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            recreate(); // 重新创建 Activity 以应用新主题
            Toast.makeText(this, isChecked ? "已启用莫奈取色" : "已禁用莫奈取色", Toast.LENGTH_SHORT).show();
        });

        // 初始化颜色选择按钮
        setupColorButtons();

        // 获取并显示应用程序所请求的权限
        displayPermissions(tvPermissions);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // 处理返回按钮点击
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    /**
     * 设置自定义颜色选择按钮的点击事件。
     */
    private void setupColorButtons() {
        String[] colors = {"red", "green", "blue", "yellow", "orange", "purple", "cyan"};
        String[] colorNames = {"红色", "绿色", "蓝色", "黄色", "橙色", "紫色", "青色"};
        int[] buttonIds = {
                R.id.btn_color_red, R.id.btn_color_green,
                R.id.btn_color_blue, R.id.btn_color_yellow,
                R.id.btn_color_orange, R.id.btn_color_purple,
                R.id.btn_color_cyan
        };

        for (int i = 0; i < colors.length; i++) {
            MaterialButton button = findViewById(buttonIds[i]);
            String colorName = colors[i];
            String displayColorName = colorNames[i];
            button.setOnClickListener(v -> {
                prefs.edit().putString("custom_theme_color", colorName).apply();
                recreate();
                Toast.makeText(this, "已设置 " + displayColorName + " 主题", Toast.LENGTH_SHORT).show();
            });
        }
    }

    /**
     * 获取并显示应用程序所请求的所有权限。
     * @param permissionsTextView 用于显示权限的 TextView。
     */
    private void displayPermissions(TextView permissionsTextView) {
        try {
            String packageName = getPackageName();
            PackageManager pm = getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            String[] requestedPermissions = packageInfo.requestedPermissions;

            if (requestedPermissions != null && requestedPermissions.length > 0) {
                StringBuilder permissionsBuilder = new StringBuilder();
                for (String permission : requestedPermissions) {
                    // 尝试为常见的 Android 权限获取用户友好的标签
                    try {
                        // 直接使用 android.content.pm.PermissionInfo 的完整限定名
                        android.content.pm.PermissionInfo pInfo = pm.getPermissionInfo(permission, 0);
                        CharSequence label = pInfo.loadLabel(pm);
                        if (label != null && !label.toString().isEmpty() && !label.equals(permission)) {
                            // 为了清晰起见，追加友好标签和原始权限名称
                            permissionsBuilder.append("• ").append(label).append(" (").append(permission).append(")\n");
                        } else {
                            // 如果没有友好标签，则只使用原始权限名称
                            permissionsBuilder.append("• ").append(permission).append("\n");
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        permissionsBuilder.append("• ").append(permission).append("\n"); // 如果信息未找到，则回退
                    }
                }
                permissionsTextView.setText(permissionsBuilder.toString().trim());
            } else {
                permissionsTextView.setText("本程序未申请任何特殊权限。");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            permissionsTextView.setText("无法获取权限信息。");
        }
    }
}
