package org.maria.playset;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast; // 导入 Toast 类
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {

    private Switch monetToggle;
    private LinearLayout colorOptionsLayout;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 在调用 super.onCreate() 之前应用主题
        applyAppTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 设置返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("主题设置");
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        monetToggle = findViewById(R.id.monet_toggle);
        colorOptionsLayout = findViewById(R.id.color_options_layout);

        // 初始化莫奈取色开关状态
        boolean monetEnabled = prefs.getBoolean("monet_enabled", true);
        monetToggle.setChecked(monetEnabled);
        colorOptionsLayout.setVisibility(monetEnabled ? View.GONE : View.VISIBLE);

        monetToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("monet_enabled", isChecked).apply();
            colorOptionsLayout.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            recreate(); // 重新创建 Activity 以应用新主题
            Toast.makeText(this, isChecked ? "已启用莫奈取色" : "已禁用莫奈取色", Toast.LENGTH_SHORT).show(); // 添加 Toast 提示
        });

        // 初始化颜色选择按钮
        setupColorButtons();
    }

    /**
     * 根据 SharedPreferences 中的设置应用应用程序主题。
     * 与 MainActivity 中的 applyAppTheme() 逻辑相同。
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

    /**
     * 设置自定义颜色选择按钮的点击事件。
     */
    private void setupColorButtons() {
        String[] colors = {"red", "green", "blue", "yellow", "orange", "purple", "cyan"};
        String[] colorNames = {"红色", "绿色", "蓝色", "黄色", "橙色", "紫色", "青色"}; // 添加颜色名称数组
        int[] buttonIds = {
                R.id.btn_color_red, R.id.btn_color_green, R.id.btn_color_blue,
                R.id.btn_color_yellow, R.id.btn_color_orange, R.id.btn_color_purple,
                R.id.btn_color_cyan
        };

        for (int i = 0; i < colors.length; i++) {
            Button button = findViewById(buttonIds[i]);
            String colorName = colors[i];
            String displayColorName = colorNames[i]; // 获取显示用的颜色名称
            button.setOnClickListener(v -> {
                prefs.edit().putString("custom_theme_color", colorName).apply();
                recreate(); // 重新创建 Activity 以应用新主题
                Toast.makeText(this, "已设置 " + displayColorName + " 主题", Toast.LENGTH_SHORT).show(); // 添加 Toast 提示
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        // 处理返回按钮点击事件
        finish();
        return true;
    }
}
