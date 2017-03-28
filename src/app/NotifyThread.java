package app;

public class NotifyThread {
    final private PostMonitor monitor;
    private PostMonitor.Action action;

    public NotifyThread(PostMonitor monitor, PostMonitor.Action action) {
        this.monitor = monitor;
        this.action = action;
    }

    public void runThread() {
        Thread thread = new Thread(() -> {
            action.action();
            monitor.notifyMonitor();
        });
        thread.setDaemon(true);
        thread.start();
    }
}
