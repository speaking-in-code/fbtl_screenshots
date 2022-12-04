import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'fbtl_screenshots_platform_interface.dart';

/// An implementation of [FBTLScreenshotsPlatform] that uses method channels.
class MethodChannelFBTLScreenshots extends FBTLScreenshotsPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('fbtl_screenshots');

  @override
  Future<List<int>?> takeScreenshot() async {
    final List<int>? png = await methodChannel.invokeMethod<List<int>?>('takeScreenshot');
    return png;
  }
}
