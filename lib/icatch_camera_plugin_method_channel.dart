import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'icatch_camera_plugin_platform_interface.dart';

/// An implementation of [IcatchCameraPluginPlatform] that uses method channels.
class MethodChannelIcatchCameraPlugin extends IcatchCameraPluginPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('icatch_camera_plugin');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<bool> initializeCamera() async {
    final result = await methodChannel.invokeMethod<bool>('initializeCamera');
    return result ?? false;
  }

  @override
  Future<bool> connectCamera() async {
    final result = await methodChannel.invokeMethod<bool>('connectCamera');
    return result ?? false;
  }

  @override
  Future<bool> disconnectCamera() async {
    final result = await methodChannel.invokeMethod<bool>('disconnectCamera');
    return result ?? false;
  }

  @override
  Future<bool> startPreview() async {
    final result = await methodChannel.invokeMethod<bool>('startPreview');
    return result ?? false;
  }

  @override
  Future<bool> stopPreview() async {
    final result = await methodChannel.invokeMethod<bool>('stopPreview');
    return result ?? false;
  }

  @override
  Future<String> getCameraList() async {
    final result = await methodChannel.invokeMethod<String>('getCameraList');
    return result ?? 'No cameras found';
  }
}
