package net.speakingincode.fbtl_screenshots;

import android.annotation.TargetApi;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;

import java.io.ByteArrayOutputStream;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * FbtlScreenshotsPlugin
 */
public class FbtlScreenshotsPlugin implements FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "fbtl_screenshots");
        channel.setMethodCallHandler(this);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("takeScreenshot")) {
            takeScreenshot(result);
        } else {
            result.notImplemented();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void takeScreenshot(@NonNull Result result) {
        try {
            Instrumentation inst = InstrumentationRegistry.getInstrumentation();
            UiAutomation automation = inst.getUiAutomation();
            Bitmap screenshot = automation.takeScreenshot();
            ByteArrayOutputStream pngStream = new ByteArrayOutputStream();
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, pngStream);
            byte[] png = pngStream.toByteArray();
            result.success(png);
        } catch (IllegalStateException e) {
            result.error("FAILURE", "android.app.Instrumentation not found. Potential solutions:\n" +
                         "  1) Invoke this test with 'gradlew app:connectedAndroidTest', not 'flutter test' or 'flutter drive'.\n" +
                         "  2) Follow steps in https://github.com/flutter/flutter/tree/main/packages/integration_test#android-device-testing to configure androidx AndroidJUnitRunner.\n",
                    e.toString());
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }
}
