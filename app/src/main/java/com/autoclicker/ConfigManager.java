package com.autoclicker;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置管理器 - 保存和加载点击配置
 */
public class ConfigManager {

    private static final String PREFS_NAME = "auto_clicker_config";
    private static final String KEY_CONFIGS = "saved_configs";
    private static final String KEY_LAST_CONFIG = "last_config";

    private SharedPreferences prefs;

    public ConfigManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 保存配置
     */
    public void saveConfig(String name, List<ClickPoint> clickPoints) {
        try {
            JSONObject config = new JSONObject();
            config.put("name", name);
            config.put("timestamp", System.currentTimeMillis());

            JSONArray pointsArray = new JSONArray();
            for (ClickPoint point : clickPoints) {
                JSONObject pointObj = new JSONObject();
                pointObj.put("x", point.getX());
                pointObj.put("y", point.getY());
                pointObj.put("waitTime", point.getWaitTime());
                pointObj.put("repeatCount", point.getRepeatCount());
                pointObj.put("clickInterval", point.getClickInterval());
                pointObj.put("roundInterval", point.getRoundInterval());
                pointsArray.put(pointObj);
            }
            config.put("points", pointsArray);

            // 获取现有配置列表
            List<JSONObject> configs = getAllConfigs();

            // 检查是否已存在同名配置，更新它
            boolean found = false;
            for (int i = 0; i < configs.size(); i++) {
                if (configs.get(i).getString("name").equals(name)) {
                    configs.set(i, config);
                    found = true;
                    break;
                }
            }

            if (!found) {
                configs.add(config);
            }

            // 保存所有配置
            saveAllConfigs(configs);

            // 同时保存为最后使用的配置
            prefs.edit().putString(KEY_LAST_CONFIG, config.toString()).apply();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载配置
     */
    public List<ClickPoint> loadConfig(String name) {
        try {
            List<JSONObject> configs = getAllConfigs();
            for (JSONObject config : configs) {
                if (config.getString("name").equals(name)) {
                    return parseConfig(config);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 加载最后使用的配置
     */
    public List<ClickPoint> loadLastConfig() {
        try {
            String lastConfig = prefs.getString(KEY_LAST_CONFIG, null);
            if (lastConfig != null) {
                JSONObject config = new JSONObject(lastConfig);
                return parseConfig(config);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取所有配置名称
     */
    public List<String> getConfigNames() {
        List<String> names = new ArrayList<>();
        try {
            List<JSONObject> configs = getAllConfigs();
            for (JSONObject config : configs) {
                names.add(config.getString("name"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return names;
    }

    /**
     * 删除配置
     */
    public boolean deleteConfig(String name) {
        try {
            List<JSONObject> configs = getAllConfigs();
            for (int i = 0; i < configs.size(); i++) {
                if (configs.get(i).getString("name").equals(name)) {
                    configs.remove(i);
                    saveAllConfigs(configs);
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 解析配置为 ClickPoint 列表
     */
    private List<ClickPoint> parseConfig(JSONObject config) throws JSONException {
        List<ClickPoint> points = new ArrayList<>();
        JSONArray pointsArray = config.getJSONArray("points");

        for (int i = 0; i < pointsArray.length(); i++) {
            JSONObject pointObj = pointsArray.getJSONObject(i);
            points.add(new ClickPoint(
                    pointObj.getInt("x"),
                    pointObj.getInt("y"),
                    pointObj.getInt("waitTime"),
                    pointObj.getInt("repeatCount"),
                    pointObj.getInt("clickInterval"),
                    pointObj.getInt("roundInterval")
            ));
        }
        return points;
    }

    /**
     * 获取所有保存的配置
     */
    private List<JSONObject> getAllConfigs() {
        List<JSONObject> configs = new ArrayList<>();
        try {
            String json = prefs.getString(KEY_CONFIGS, "[]");
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                configs.add(array.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return configs;
    }

    /**
     * 保存所有配置
     */
    private void saveAllConfigs(List<JSONObject> configs) {
        JSONArray array = new JSONArray();
        for (JSONObject config : configs) {
            array.put(config);
        }
        prefs.edit().putString(KEY_CONFIGS, array.toString()).apply();
    }
}
