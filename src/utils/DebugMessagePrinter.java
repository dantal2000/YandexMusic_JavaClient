package utils;

import java.io.PrintStream;

public class DebugMessagePrinter {
    private final String methodName;
    private StackVoid debugMessage;
    private PrintStream printStream;
    private int count = -1;

    public DebugMessagePrinter(PrintStream printStream) {
        this.printStream = printStream;
        final boolean debugOn = printStream != null;
        methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        debugMessage = new StackVoid(() -> {
            if (debugOn) printStream.println("Отладка " + methodName + " включена и запущена");
        });
        if (!debugOn) debugMessage.deprecate();
    }

    public void println(String message) {
        count++;
        final int countCopy = count;
        debugMessage.addAction(() -> printStream.println(countCopy + "# " + message));
    }

    public void fire() {
        println("Отладка " + methodName + " закончена\n");
        debugMessage.fire();
    }

    static class StackVoid {
        private Runnable action;
        private boolean deprecated = false;

        public StackVoid(Runnable action) {
            this.action = action;
        }

        public void addAction(Runnable action) {
            if (!deprecated) {
                final Runnable cacheAction = this.action;
                this.action = () -> {
                    cacheAction.run();
                    action.run();
                };
            }
        }

        public void fire() {
            if (!deprecated)
                action.run();
        }

        public void deprecate() {
            if (!deprecated) deprecated = true;
            action = null;
        }
    }
}
