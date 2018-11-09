package testable;

import internal.os.StrictOS;
import internal.ui.UIButton;

import java.awt.*;

public class ChangeToRedWhenClickedButton extends UIButton {

    private final ClickPermissionRequester clickPermissionRequester;

    public ChangeToRedWhenClickedButton(ClickPermissionRequester clickPermissionRequester) {
        this.clickPermissionRequester = clickPermissionRequester;
    }

    @Override
    public void onClick() {
        clickPermissionRequester.request(new StrictOS.PermissionCallback() {

            @Override
            public void onGranted() {
                setColor(Color.RED);
            }

            @Override
            public void onDenied() {
                // ignored
            }
        });
    }
}
