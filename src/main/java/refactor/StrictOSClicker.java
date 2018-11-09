package refactor;

import internal.os.StrictOS;

public class StrictOSClicker implements Clicker {
    @Override
    public void click(ClickCallback clickCallback) {
        StrictOS.requestClickPermission(new StrictOS.PermissionCallback() {
            @Override
            public void onGranted() {
                clickCallback.clicked();
            }

            @Override
            public void onDenied() {

            }
        });
    }
}
