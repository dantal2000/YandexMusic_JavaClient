package utils;

public class WaitingThread {
    private Action opening;
    private Action closing;
    private Action working;

    public WaitingThread() {
    }

    public void fire() {
        Thread thread = new Thread(() -> {
            if (opening != null) opening.action();
            if (working != null) working.action();
            if (closing != null) closing.action();
        });
        thread.setDaemon(true);
        thread.start();
    }

    public Action getOpening() {
        return opening;
    }

    public void setOpening(Action opening) {
        this.opening = opening;
    }

    public Action getClosing() {
        return closing;
    }

    public void setClosing(Action closing) {
        this.closing = closing;
    }

    public Action getWorking() {
        return working;
    }

    public void setWorking(Action working) {
        this.working = working;
    }

    public interface Action {
        void action();
    }
}
