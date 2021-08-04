package com.nimasystems.lightcast.threading;

import androidx.annotation.NonNull;

import com.nimasystems.lightcast.logging.LcLogger;
import com.nimasystems.lightcast.network.OperationResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackgroundJobPool {

    //private Logger mLogger;
    private LcLogger mLogger;

    private ExecutorService executor;

    public interface BackgroundTasksExecutorListener {
        void OnExecutionComplete(boolean success, @NonNull List<OperationResponse> jobResponses);
    }

    public interface BackgroundTaskExecutionCallback {
        void signalCompleted(OperationResponse taskResponse);
    }

    public interface BackgroundJob {
        void run(final Executor executor, final BackgroundTaskExecutionCallback callback);
    }

    public BackgroundJobPool() {
        //mLogger = LoggerFactory.getLogger(this.getClass());
        init();
    }

    public BackgroundJobPool(ExecutorService executor) {
        this.executor = executor;
        init();
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    protected void init() {
        //mLogger = LoggerFactory.getLogger(this.getClass());

        if (this.executor == null) {
            this.executor = Executors
                    .newCachedThreadPool();
        }
    }

    public void execute(final List<BackgroundJob> jobs, final BackgroundTasksExecutorListener listener) {

        this.executor.submit(() -> {
            final CountDownLatch latch = new CountDownLatch(jobs.size());

            final List<OperationResponse> responses = new ArrayList<>();

            final BackgroundTaskExecutionCallback execCallback = taskResponse -> {

                if (taskResponse != null) {
                    responses.add(taskResponse);
                }

                latch.countDown();
            };

            for (final BackgroundJob job : jobs) {
                executor.submit((Callable<Void>) () -> {
                    try {
                        job.run(executor, execCallback);
                    } catch (Exception e) {
                        if (mLogger != null) {
                            mLogger.err("Error while waiting for thread completion");
                        }
                    }
                    return null;
                });
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                if (mLogger != null) {
                    mLogger.err("Error while waiting for thread completion");
                }
            }

            if (listener != null) {
                listener.OnExecutionComplete(true, responses);
            }
        });
    }

}