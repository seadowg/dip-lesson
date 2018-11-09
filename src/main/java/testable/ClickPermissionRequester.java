package testable;

import internal.os.StrictOS;

public interface ClickPermissionRequester {
    void request(StrictOS.PermissionCallback permissionCallback);
}
