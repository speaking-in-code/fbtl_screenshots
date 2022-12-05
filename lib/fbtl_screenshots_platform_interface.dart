import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'fbtl_screenshots_method_channel.dart';

abstract class FBTLScreenshotsPlatform extends PlatformInterface {
  /// Constructs a FBTLScreenshotsPlatform.
  FBTLScreenshotsPlatform() : super(token: _token);

  static final Object _token = Object();

  static FBTLScreenshotsPlatform _instance = MethodChannelFBTLScreenshots();

  /// The default instance of [FBTLScreenshotsPlatform] to use.
  ///
  /// Defaults to [MethodChannelFBTLScreenshots].
  static FBTLScreenshotsPlatform get instance => _instance;
  
  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FBTLScreenshotsPlatform] when
  /// they register themselves.
  static set instance(FBTLScreenshotsPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<void> connect() {
    throw UnimplementedError('connect() has not been implemented.');
  }

  Future<List<int>?> takeScreenshot() {
    throw UnimplementedError('takeScreenshot() has not been implemented.');
  }
}
