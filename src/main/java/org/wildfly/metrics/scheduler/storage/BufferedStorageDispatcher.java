package org.wildfly.metrics.scheduler.storage;

import org.wildfly.metrics.scheduler.diagnose.Diagnostics;
import org.wildfly.metrics.scheduler.polling.Scheduler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Heiko Braun
 * @since 13/10/14
 */
public class BufferedStorageDispatcher implements Scheduler.CompletionHandler {

    private static final int MAX_BATCH_SIZE = 24;
    private static final int BUFFER_SIZE = 100;
    private final StorageAdapter storageAdapter;
    private final Diagnostics diagnostics;
    private final BlockingQueue<DataPoint> queue;
    private final Worker worker;

    public BufferedStorageDispatcher(StorageAdapter storageAdapter, Diagnostics diagnostics) {
        this.storageAdapter = storageAdapter;
        this.diagnostics = diagnostics;
        this.queue = new ArrayBlockingQueue<DataPoint>(BUFFER_SIZE);
        this.worker = new Worker(queue);
    }

    public void start() {
        worker.start();
    }

    public void shutdown() {
        worker.setKeepRunnig(false);
    }

    @Override
    public void onCompleted(DataPoint sample) {
        if(queue.remainingCapacity()>0) {
            //System.out.println(sample.getTask().getAttribute()+" > "+sample.getValue());
            diagnostics.getStorageBufferSize().inc();
            queue.add(sample);
        }
        else {
            throw new RuntimeException("buffer capacity exceeded");
        }
    }

    @Override
    public void onFailed(Throwable e) {
        e.printStackTrace();
    }

    public class Worker extends Thread {
        private final BlockingQueue<DataPoint> queue;
        private boolean keepRunning = true;

        public Worker(BlockingQueue<DataPoint> queue) {
            this.queue = queue;
        }

        public void run() {
            try {
                while ( keepRunning ) {

                    // batch processing
                    DataPoint sample = queue.take();
                    Set<DataPoint> samples = new HashSet<>();
                    queue.drainTo(samples, MAX_BATCH_SIZE);
                    samples.add(sample);

                    diagnostics.getStorageBufferSize().dec(samples.size());

                    // dispatch
                    storageAdapter.store(samples);
                }
            }
            catch ( InterruptedException ie ) {
                // just terminate
            }
        }

        public void setKeepRunnig(boolean keepRunning) {
            this.keepRunning = keepRunning;
        }
    }
}

