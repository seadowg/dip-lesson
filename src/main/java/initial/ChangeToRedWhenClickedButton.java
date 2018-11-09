package initial;

import internal.os.StrictOS;
import internal.ui.UIButton;

import java.awt.*;

public class ChangeToRedWhenClickedButton extends UIButton {

    @Override
    public void onClick() {
        StrictOS.requestClickPermission(new StrictOS.PermissionCallback() {
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
