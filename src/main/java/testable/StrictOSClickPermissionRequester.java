package testable;

import internal.os.StrictOS;

public class StrictOSClickPermissionRequester implements ClickPermissionRequester {
    @Override
    public void request(StrictOS.PermissionCallback permissionCallback) {
        StrictOS.requestClickPermission(permissionCallback);
    }
}
