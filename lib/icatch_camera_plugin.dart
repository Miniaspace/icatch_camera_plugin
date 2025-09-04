
import 'icatch_camera_plugin_platform_interface.dart';

class IcatchCameraPlugin {
  Future<String?> getPlatformVersion() {
    return IcatchCameraPluginPlatform.instance.getPlatformVersion();
  }

  /// Initialize the camera system
  Future<bool> initializeCamera() {
    return IcatchCameraPluginPlatform.instance.initializeCamera();
  }

  /// Connect to the camera
  Future<bool> connectCamera() {
    return IcatchCameraPluginPlatform.instance.connectCamera();
  }

  /// Disconnect from the camera
  Future<bool> disconnectCamera() {
    return IcatchCameraPluginPlatform.instance.disconnectCamera();
  }

  /// Start camera preview
  Future<bool> startPreview() {
    return IcatchCameraPluginPlatform.instance.startPreview();
  }

  /// Stop camera preview
  Future<bool> stopPreview() {
    return IcatchCameraPluginPlatform.instance.stopPreview();
  }

  /// Get list of available cameras
  Future<String> getCameraList() {
    return IcatchCameraPluginPlatform.instance.getCameraList();
  }
}
