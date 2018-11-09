package testable;

import internal.os.StrictOS;
import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.assertEquals;

public class ChangeToRedWhenClickedButtonTest {

    @Test
    public void clickingOnButton_whenPermissionIsGranted_changesToRed() {
        ChangeToRedWhenClickedButton button = new ChangeToRedWhenClickedButton(new GrantedClickPermissionRequester());
        button.setColor(Color.GREEN);

        button.onClick();
        assertEquals(button.getColor(), Color.RED);
    }

    private class GrantedClickPermissionRequester implements ClickPermissionRequester {

        @Override
        public void request(StrictOS.PermissionCallback permissionCallback) {
            permissionCallback.onGranted();
        }
    }
}
