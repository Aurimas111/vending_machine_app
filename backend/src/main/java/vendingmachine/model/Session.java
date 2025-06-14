package vendingmachine.model;

import java.util.concurrent.atomic.AtomicBoolean;

public class Session {
    private final AtomicBoolean stopFlag = new AtomicBoolean(false);

    public AtomicBoolean getStopFlag() {
        return stopFlag;
    }

    public void stop() {
        stopFlag.set(true);
    }

    public boolean isStopped() {
        return stopFlag.get();
    }
}
