package sj3d;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An Animator is a wrapper around a background thread that calls
 * {@link World#render()} periodically.  There is a 10 millisecond
 * delay between calls, so there can be no more than 100 frames per
 * second.  If rendering is slow then there may be far fewer.
 *
 * <p>Thread safety note: the {@link World} class is not thread-safe.
 * The <code>Animator</code> acquires a lock on the world instance while
 * rendering.  Any concurrent operations that modify the world or any
 * object it contains should also acquire a lock on the world instance.
 *
 * @see #start()
 * @see #stop()
 */
public class Animator implements Runnable {

    private final World world;
    private final Thread thread;
    private final AtomicBoolean started;
    private volatile boolean running;
    private volatile int delay = 10;

    private volatile float fps = 0.0f;
    private volatile float avg_fps = 0.0f;
    private int fps_probes = 0;
    private final int fps_probe_step = 500; // milliseconds
    private long timestamp = 0;
    private long total_frames;
    private long start_time;

    public Animator(World w) {
        world = w;
        thread = new Thread(this, "animation");
        thread.setDaemon(true);
        started = new AtomicBoolean(false);
        running = false;
    }

    /**
     * Start a background thread that periodically renders the world.  A given
     * <code>Animator</code> can only be started once.  Even if it is explicitly
     * stopped via {@link #stop()}, it cannot be started again.
     *
     * <p>This method may be called from any thread.
     *
     * @throws IllegalArgumentException if this instance has already been started
     */
    public void start() {
        if (started.compareAndSet(false, true)) {
            running = true;
            start_time = System.currentTimeMillis();
            thread.start();
        } else {
            throw new IllegalStateException("An Animator cannot be started more than once");
        }
    }

    /**
     * Stop and {@link Thread#join(long)} the background thread.  This method may
     * be called from any thread.  It is idempotent and may be called multiple times.
     *
     * <p>Performance note: if the background thread is busy with a long-running
     * operation, this method may block for up to 0.5 seconds or longer (depending on
     * the system clock and thread scheduler).
     *
     * @throws InterruptedException if the current thread is interrupted while waiting
     *   for the background thread to stop
     * @throws TimeoutException if the background thread does not stop after 0.5 seconds
     */
    public void stop() throws InterruptedException, TimeoutException {
        if (started.get()) {
            running = false;
            thread.join(500);
            if (thread.isAlive()) {
                throw new TimeoutException("Animator thread did not stop in 0.5 seconds");
            }
        } else {
            throw new IllegalStateException("An Animator was stopped before it was started");
        }
    }

    public void setDelay(int d) {
        delay = d;
    }

    public void run() {
        while (running) {
            benchmark();
            synchronized (world) {
                world.render();
            }
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {

            }
        }
    }

    public float getFPS() {
        return fps;
    }

    public float getAverageFPS() {
        return avg_fps;
    }

    private void benchmark() {
        final long now = System.currentTimeMillis();
        total_frames++;
        fps_probes += 1;
        if (now > timestamp + fps_probe_step) {
            fps = (float) fps_probes / ((float) (now - timestamp) / 1000);
            timestamp = now;
            fps_probes = 0;
            final long total_time = now - start_time;
            avg_fps = (float) total_frames / total_time * 1000;
        }
    }

}
