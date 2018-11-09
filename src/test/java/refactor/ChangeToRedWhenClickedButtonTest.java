package refactor;

import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.assertEquals;

public class ChangeToRedWhenClickedButtonTest {

    @Test
    public void clickingOnButton_whenPermissionIsGranted_changesToRed() {
        ChangeToRedWhenClickedButton button = new ChangeToRedWhenClickedButton(new FakeClicker());
        button.setColor(Color.GREEN);

        button.onClick();
        assertEquals(button.getColor(), Color.RED);
    }

    private class FakeClicker implements Clicker {
        @Override
        public void click(ClickCallback clickCallback) {
            clickCallback.clicked();
        }
    }
}
