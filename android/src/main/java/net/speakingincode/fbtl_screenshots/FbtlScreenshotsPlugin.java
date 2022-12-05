package net.speakingincode.fbtl_screenshots;

import android.annotation.TargetApi;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

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
  private static final String TAG = "FbtlScreenshotsPlugin";
  private static final int UI_AUTOMATION_RETRIES = 3;
  private static final int UI_AUTOMATION_DELAY_MILLIS = 2000;

  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel mChannel;

  // UiAutomation, initialization protected by lock. This object is subtle because of a race
  // condition in Instrumentation.getUiAutomation(): the automation returned may or may not have
  // been successfully initialized. To work around the problem we initialize it early, and we also
  // check for a successful connection before trying to take a screenshot.
  private Object mLock = new Object();
  private UiAutomation mAutomation;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    Log.i(TAG, "Attaching to Flutter engine");
    mChannel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "fbtl_screenshots");
    mChannel.setMethodCallHandler(this);
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    Log.i(TAG, "Detaching from Flutter engine");
    mChannel.setMethodCallHandler(null);
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    Log.v(TAG, "Received method call " + call.method);
    if (call.method.equals("connect")) {
      connect();
      result.success(null);
    } else if (call.method.equals("takeScreenshot")) {
      takeScreenshot(result);
    } else {
      result.notImplemented();
    }
  }

  private void connect() {
    synchronized (mLock) {
      if (mAutomation == null) {
        // UiAutomation is slow to set up, so we create it early to give it time to warm up.
        try {
          Log.i(TAG, "First attempt to initialize UiAutomation");
          mAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
          Log.i(TAG, "Got UiAutomation on first try");
        } catch (RuntimeException e) {
          Log.w(TAG, "UiAutomation initialization failed, retrying", e);
          mAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
          Log.w(TAG, "Using (possibly unconnected) UiAutomation.");
        }
      }
    }
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  public void takeScreenshot(@NonNull Result result) {
    synchronized (mLock) {
      if (mAutomation == null) {
        result.error("FAILURE", "Call connect() before trying to use FBTLScreenshots.", null);
        return;
      }
    }
    try {
      UiAutomation automation = getConnectedAutomation();
      Log.v(TAG, "Capturing screenshot");
      Bitmap screenshot = automation.takeScreenshot();
      ByteArrayOutputStream pngStream = new ByteArrayOutputStream();
      screenshot.compress(Bitmap.CompressFormat.PNG, 100, pngStream);
      byte[] png = pngStream.toByteArray();
      Log.v(TAG, "Screenshot complete");
      result.success(png);
    } catch (IllegalStateException e) {
      Log.e(TAG, "Error connecting to UiAutomation", e);
      result.error("FAILURE", "android.app.UiAutomation not found. Potential solutions:\n" +
          "  1) Invoke this test with 'gradlew app:connectedAndroidTest', not 'flutter test' or 'flutter drive'.\n" +
          "  2) Follow steps in https://github.com/flutter/flutter/tree/main/packages/integration_test#android-device-testing to configure androidx AndroidJUnitRunner.\n",
          e.toString());
    }
  }

  private UiAutomation getConnectedAutomation() throws RuntimeException {
    synchronized (mLock) {
      RuntimeException lastEx = null;
      for (int i = 1; i <= UI_AUTOMATION_RETRIES; ++i) {
        Log.i(TAG, "Checking status for UiAutomation, attempt " + i);
        try {
          // Force a check to see if the connection really worked, this throws if the connection
          // isn't yet complete.
          mAutomation.getServiceInfo();
          Log.v(TAG, "Successful connection to UiAutomation: " + mAutomation);
          return mAutomation;
        } catch (RuntimeException e) {
          Log.w(TAG, "Error connecting to UiAutomation on attempt " + i, e);
          lastEx = e;
          if (i != UI_AUTOMATION_RETRIES) {
            try {
              Thread.sleep(UI_AUTOMATION_DELAY_MILLIS);
            } catch (InterruptedException interrupted) {
            }
          }
        }
      }
      throw lastEx;
    }
  }
}
