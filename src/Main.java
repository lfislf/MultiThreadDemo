import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

    private volatile static Integer sResult;

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.call1();
        main.call2();
        main.call3();
        main.call4();
        main.call5();
        main.call6();
        main.call7();
        main.call8();
        main.call9();
        main.call10();
        main.call11();
    }

    public void call1() throws Exception {
        FutureTask<Integer> futureTask = new FutureTask(this::sum);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(futureTask);
        Integer integer = futureTask.get();
        System.out.println("call1 result: " + integer);
    }

    public void call2() throws Exception {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Integer integer = executorService.submit(this::sum).get();
        System.out.println("call2 result: " + integer);
    }

    public void call3() throws Exception {
        sResult = null;
        Thread thread = new Thread(() -> sResult = sum());
        thread.start();
        thread.join();
        System.out.println("call3 result: " + sResult);
    }

    public void call4() throws Exception {
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(this::sum);
        Integer integer = completableFuture.get();
        System.out.println("call4 result: " + integer);
    }

    public void call5() throws Exception {
        sResult = null;
        final CountDownLatch latch = new CountDownLatch(1);
        Thread thread = new Thread(() -> {
            sResult = sum();
            latch.countDown();
        });
        thread.start();
        latch.await();
        System.out.println("call5 result: " + sResult);
    }

    public void call6() throws Exception {
        sResult = null;
        final Object lock = new Object();
        Thread thread = new Thread(() -> {
            synchronized (lock) {
                sResult = sum();
                lock.notifyAll();
            }
        });
        thread.start();
        synchronized (lock) {
            while (sResult == null) {
                lock.wait();
            }
        }
        System.out.println("call6 result: " + sResult);
    }

    public void call7() throws Exception {
        sResult = null;
        final Lock lock = new ReentrantLock();
        final Condition condition = lock.newCondition();
        Thread thread = new Thread(() -> {
            lock.lock();
            try {
                sResult = sum();
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        });
        thread.start();
        lock.lock();
        try {
            while (sResult == null) {
                condition.await();
            }
        } finally {
            lock.unlock();
        }
        System.out.println("call7 result: " + sResult);
    }

    public void call8() throws Exception {
        sResult = null;
        final Semaphore semaphore = new Semaphore(1);
        Thread thread = new Thread(() -> {
            sResult = sum();
            semaphore.release();
        });
        semaphore.acquire();
        thread.start();
        semaphore.acquire();
        System.out.println("call8 result: " + sResult);
        semaphore.release();
    }

    public void call9() throws Exception {
        final BlockingQueue<Integer> blockingQueue = new SynchronousQueue<>();
        Thread thread = new Thread(() -> {
            try {
                blockingQueue.put(sum());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        Integer integer = blockingQueue.take();
        System.out.println("call9 result: " + integer);
    }

    public void call10() throws Exception {
        sResult = null;
        Thread thread = new Thread(() -> sResult = sum());
        thread.start();
        while (sResult == null) {
            Thread.sleep(10);
        }
        System.out.println("call10 result: " + sResult);
    }

    public void call11() throws Exception {
        sResult = null;
        final AtomicBoolean done = new AtomicBoolean(false);
        Thread thread = new Thread(() -> {
            sResult = sum();
            done.set(true);
        });
        thread.start();
        while (!done.get()) {
            Thread.sleep(10);
        }
        System.out.println("call11 result: " + sResult);
    }


    public Integer sum() {
        int sum = 0;
        for (int i = 1; i <= 100000; i++) {
            sum += i;
        }
        return sum;
    }
}
