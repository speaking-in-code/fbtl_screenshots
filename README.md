A flutter package for taking screenshots using [Firebase Test Lab](https://firebase.google.com/docs/test-lab).

Firebase Test Lab runs integration tests on emulators and real devices. This package helps you take
screenshots during tests and adds the resulting screenshots to the firebase test lab output files.

Note this package is not affiliated with Firebase or Google.

## Getting started

First, use the [flutter integration_test package](https://docs.flutter.dev/testing/integration-tests#testing-on-firebase-test-lab)
to run your tests on Firebase Test Lab.

Once your tests are running on Firebase Test Lab, add screenshot support. Start by editing your
pubspec.yaml file to add a dev dependency on fbtl_screenshots:

```
dev_dependencies:
  fbtl_screenshots: ^0.0.1
```

Then edit your integration_test/example_test.dart files to add calls to
FBTLScreenshots.takeScreenshot:

```
import 'package:fbtl_screenshots/fbtl_screenshots.dart';
...
void main() {
  final _screenshots = FBTLScreenshots();
  
  testWidgets('screenshot', (WidgetTester tester) async {
    // Build the app.
    app.main();
    // Trigger a frame.
    await tester.pumpAndSettle();
    // Take the screenshot
    await _screenshots.takeScreenshot('screenshot-1');
  });
}
```

## Examining the Screenshots

Firebase Test Lab writes screenshot output to different locations depending on the platform.

For iOS, download the xctest files from Firebase Test Lab, and open them in XCode. Screenshots
are included in the xctest output. Packages like [XCParse](https://github.com/ChargePoint/xcparse)
can be used to parse the xctest output in continuous integration environments.

For Android, screenshots are written to the external storage directories. Download the screenshots
by adding the '--directories-to-pull /sdcard' flag to your `gcloud firebase test android run`
command.
