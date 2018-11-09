package refactor;

import internal.ui.UIButton;

import java.awt.*;

public class ChangeToRedWhenClickedButton extends UIButton {

    private final Clicker clicker;

    public ChangeToRedWhenClickedButton(Clicker clicker) {
        this.clicker = clicker;
    }

    @Override
    public void onClick() {
        clicker.click(() -> setColor(Color.RED));
    }
}
