import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:fbtl_screenshots/fbtl_screenshots_method_channel.dart';

void main() {
  MethodChannelFbtlScreenshots platform = MethodChannelFbtlScreenshots();
  const MethodChannel channel = MethodChannel('fbtl_screenshots');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
