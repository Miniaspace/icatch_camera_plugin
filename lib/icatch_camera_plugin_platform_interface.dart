import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'icatch_camera_plugin_method_channel.dart';

abstract class IcatchCameraPluginPlatform extends PlatformInterface {
  /// Constructs a IcatchCameraPluginPlatform.
  IcatchCameraPluginPlatform() : super(token: _token);

  static final Object _token = Object();

  static IcatchCameraPluginPlatform _instance = MethodChannelIcatchCameraPlugin();

  /// The default instance of [IcatchCameraPluginPlatform] to use.
  ///
  /// Defaults to [MethodChannelIcatchCameraPlugin].
  static IcatchCameraPluginPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [IcatchCameraPluginPlatform] when
  /// they register themselves.
  static set instance(IcatchCameraPluginPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<bool> initializeCamera() {
    throw UnimplementedError('initializeCamera() has not been implemented.');
  }

  Future<bool> connectCamera() {
    throw UnimplementedError('connectCamera() has not been implemented.');
  }

  Future<bool> disconnectCamera() {
    throw UnimplementedError('disconnectCamera() has not been implemented.');
  }

  Future<bool> startPreview() {
    throw UnimplementedError('startPreview() has not been implemented.');
  }

  Future<bool> stopPreview() {
    throw UnimplementedError('stopPreview() has not been implemented.');
  }

  Future<String> getCameraList() {
    throw UnimplementedError('getCameraList() has not been implemented.');
  }
}
