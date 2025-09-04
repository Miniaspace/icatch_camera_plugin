import 'package:flutter_test/flutter_test.dart';
import 'package:icatch_camera_plugin/icatch_camera_plugin.dart';
import 'package:icatch_camera_plugin/icatch_camera_plugin_platform_interface.dart';
import 'package:icatch_camera_plugin/icatch_camera_plugin_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockIcatchCameraPluginPlatform
    with MockPlatformInterfaceMixin
    implements IcatchCameraPluginPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final IcatchCameraPluginPlatform initialPlatform = IcatchCameraPluginPlatform.instance;

  test('$MethodChannelIcatchCameraPlugin is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelIcatchCameraPlugin>());
  });

  test('getPlatformVersion', () async {
    IcatchCameraPlugin icatchCameraPlugin = IcatchCameraPlugin();
    MockIcatchCameraPluginPlatform fakePlatform = MockIcatchCameraPluginPlatform();
    IcatchCameraPluginPlatform.instance = fakePlatform;

    expect(await icatchCameraPlugin.getPlatformVersion(), '42');
  });
}
