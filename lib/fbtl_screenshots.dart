library fbtl_screenshots;

import 'dart:io' show File, Platform;

import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:path_provider/path_provider.dart';

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
    if (!Platform.isAndroid && !Platform.isIOS) {
      throw FBTLScreenshotsException(
          'Unsupported platform ${Platform.operatingSystem}');
    }
    if (Platform.isAndroid) {
      // See comments on the takeScreenshot method, need to do this or the
      // takeScreenshot method will hang indefinitely.
      await _binding.convertFlutterSurfaceToImage();
      await tester.pumpAndSettle();
    }
    final bytes = await _binding
        .takeScreenshot(name)
        .timeout(kTimeout, onTimeout: () => kTimeoutResult);
    if (bytes == kTimeoutResult) {
      throw FBTLScreenshotsException(
          'IntegrationTestWidgetsFlutterBinding.takeScreenshot took longer than $kTimeout');
    }
    if (Platform.isAndroid) {
      final extStorage = await getExternalStorageDirectory();
      if (extStorage == null) {
        throw FBTLScreenshotsException('No external storage directory');
      }
      final file = File('${extStorage.path}/screenshots/$name.png');
      file.parent.createSync(recursive: true);
      file.writeAsBytesSync(bytes, flush: true);
    }
  }
}

class FBTLScreenshotsException implements Exception {
  final String message;

  FBTLScreenshotsException(this.message);

  @override
  String toString() => 'FBTLScreenshotsException: $message';
}
