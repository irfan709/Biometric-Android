package com.example.biometric;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MyPrefs";
    private static final String BIOMETRIC_LOCK_ENABLED = "biometric_lock_enabled";

    private Switch biometricSwitch;
    private boolean biometricLockEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        biometricSwitch = findViewById(R.id.biometric_switch);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        biometricLockEnabled = preferences.getBoolean(BIOMETRIC_LOCK_ENABLED, false);
        biometricSwitch.setChecked(biometricLockEnabled);

        biometricSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Enable biometric lock
                showBiometricPrompt();
            } else {
                // Disable biometric lock
                biometricLockEnabled = false;
                saveBiometricLockPreference(false);
            }
        });

        // Check if biometric authentication is enabled
        if (biometricLockEnabled) {
            showBiometricPrompt();
        }
    }

    private void showBiometricPrompt() {
        // Check if biometric authentication is available
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS) {
            // Biometric authentication is available
            Executor executor = ContextCompat.getMainExecutor(this);
            BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                    new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                            // Biometric authentication successful, perform actions here
                            Toast.makeText(MainActivity.this, "Biometric authentication succeeded!", Toast.LENGTH_SHORT).show();
                            // Save the biometric lock preference when authentication succeeds
                            biometricLockEnabled = true;
                            saveBiometricLockPreference(true);
                        }

                        @Override
                        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            // Handle authentication error, if any
                            Toast.makeText(MainActivity.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            // Handle authentication failure
                            Toast.makeText(MainActivity.this, "Authentication failed!", Toast.LENGTH_SHORT).show();
                        }
                    });

            // Build the biometric prompt
            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric Authentication")
                    .setSubtitle("Verify your fingerprint")
                    .setNegativeButtonText("Cancel")
                    .build();

            // Show the biometric prompt
            biometricPrompt.authenticate(promptInfo);
        } else {
            // Biometric authentication not available
            Toast.makeText(this, "Biometric authentication is not available.", Toast.LENGTH_SHORT).show();
            biometricSwitch.setChecked(false); // Turn off the switch if biometric is not available
        }
    }

    private void saveBiometricLockPreference(boolean isEnabled) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(BIOMETRIC_LOCK_ENABLED, isEnabled);
        editor.apply();
    }
}
