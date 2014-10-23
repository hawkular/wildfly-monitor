package org.wildfly.metrics.scheduler.polling;

import org.wildfly.metrics.scheduler.storage.DataPoint;

public class DebugCompletionHandler implements Scheduler.CompletionHandler {
    @Override
    public void onCompleted(DataPoint sample) {
        System.out.println(sample.getTask().getAttribute() + " > "+ sample.getValue());
    }

    @Override
    public void onFailed(Throwable e) {
        System.out.println("TaskGroup failed: "+e.getMessage());
        e.printStackTrace();
    }
}