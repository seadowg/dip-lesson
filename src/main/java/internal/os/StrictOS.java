package internal.os;

public class StrictOS {
    public static void requestClickPermission(PermissionCallback permissionCallback) {
        throw new IllegalStateException("UI not initialized!");
    }

    public interface PermissionCallback {
        void onGranted();
        void onDenied();
    }
}
