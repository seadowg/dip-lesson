package refactor;

public interface Clicker {
    void click(ClickCallback clickCallback);

    interface ClickCallback {
        void clicked();
    }
}
