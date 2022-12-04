library fbtl_screenshots;

import 'dart:io' show File, Platform;

import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:path_provider/path_provider.dart';

import 'fbtl_screenshots_platform_interface.dart';

/// Takes screenshots in a firebase test lab environment and makes them
/// available in the firebase test lab output.
class FBTLScreenshots {
  static const kTimeout = Duration(seconds: 10);
  static const kTimeoutResult = <int>[];
  final _binding = IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  /// Takes a screenshot with the given name.
  /// On iOS, the resulting screenshot is attached to the xctest results.
  /// On Android, the screenshot is written to the external storage directory.
  Future<void> takeScreenshot(WidgetTester tester, String name) async {
    if (Platform.isAndroid) {
      return _takeAndroidScreenshot(tester, name);
    } else if (Platform.isIOS) {
      return _takeIOSScreenshot(tester, name);
    } else {
      throw FBTLScreenshotsException(
          'Unsupported platform ${Platform.operatingSystem}');
    }
  }

  Future<void> _takeIOSScreenshot(WidgetTester tester, String name) async {
    final bytes = await _binding
        .takeScreenshot(name)
        .timeout(kTimeout, onTimeout: () => kTimeoutResult);
    if (bytes == kTimeoutResult) {
      throw FBTLScreenshotsException(
          'IntegrationTestWidgetsFlutterBinding.takeScreenshot took longer than $kTimeout');
    }
  }

  Future<void> _takeAndroidScreenshot(WidgetTester tester, String name) async {
    final extStorage = await getExternalStorageDirectory();
    if (extStorage == null) {
      throw FBTLScreenshotsException('No external storage directory');
    }
    final bytes = await FBTLScreenshotsPlatform.instance.takeScreenshot()
        .timeout(kTimeout, onTimeout: () => kTimeoutResult);
    if (bytes == null) {
      throw FBTLScreenshotsException('Unexpected fbtl_screenshots failure with no error message');
    }
    if (bytes == kTimeoutResult) {
      throw FBTLScreenshotsException(
          'FBTLScreenshots use of UIAutomator took longer than $kTimeout');
    }
    final dest = File('${extStorage.path}/screenshots/$name.png');
    dest.parent.createSync(recursive: true);
    dest.writeAsBytesSync(bytes, flush: true);
  }
}

class FBTLScreenshotsException implements Exception {
  final String message;

  FBTLScreenshotsException(this.message);

  @override
  String toString() => 'FBTLScreenshotsException: $message';
}
