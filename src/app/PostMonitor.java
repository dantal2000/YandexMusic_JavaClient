package app;

public class PostMonitor {
    interface Action {
        void action();
    }

    private final Action action;
    private String threadName;
    private final Object monitor = new Object();

    public PostMonitor(Action action) {
        this.action = action;
    }

    public PostMonitor(Action action, String threadName) {

        this.action = action;
        this.threadName = threadName;
    }

    public void runMonitor() {
        Thread thread = new Thread(() -> {
            synchronized (monitor) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            action.action();
        });
        if (threadName != null) thread.setName(threadName);
        thread.setDaemon(true);
        thread.start();
    }

    public void notifyMonitor() {
        synchronized (monitor) {
            monitor.notify();
        }
    }
}
