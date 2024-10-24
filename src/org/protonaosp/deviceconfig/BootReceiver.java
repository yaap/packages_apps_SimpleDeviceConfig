/*
 * Copyright (C) 2020 The Proton AOSP Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.protonaosp.deviceconfig;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.DeviceConfig;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "SimpleDeviceConfig";
    private static final boolean DEBUG = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        new Thread(() -> {
            Log.i(TAG, "Updating device config at boot");
            updateDefaultConfigs(context);
        }).start();
    }

    private void updateDefaultConfigs(Context context) {
        updateConfig(context, R.array.configs_base, false, "base");
        updateConfig(context, R.array.configs_base_soft, true, "soft");
        updateConfig(context, R.array.configs_device, false, "device");
    }

    private void updateConfig(Context context, int configArray, boolean isSoft, String name) {
        // Set current properties
        String[] rawProperties = context.getResources().getStringArray(configArray);
        Log.i(TAG, String.format("Setting %d '%s' configs", rawProperties.length, name));
        for (String property : rawProperties) {
            // Format: namespace/key=value
            String[] kv = property.split("=");
            String fullKey = kv[0];
            String[] nsKey = fullKey.split("/");

            String namespace = nsKey[0];
            String key = nsKey[1];
            String value = "";
            if (kv.length > 1) {
                value = kv[1];
            }

            // Skip soft configs that already have values
            String oldValue = DeviceConfig.getString(namespace, key, null);
            if (!isSoft || oldValue == null) {
                DeviceConfig.setProperty(namespace, key, value, false);
                if (DEBUG) {
                    Log.d(TAG, String.format("Setting: %s/%s=%s", namespace, key, value));
                }
            } else if (DEBUG) {
                Log.d(TAG, String.format("Skipped: %s/%s=%s", namespace, key, value));
            }
            if (DEBUG && oldValue != null) {
                Log.d(TAG, "old value: " + oldValue);
            }
        }
    }
}
