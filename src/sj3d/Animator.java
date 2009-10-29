package sj3d;

public class Animator implements Runnable {

	private final World world;
	private Thread thread;
	private boolean running;
	private int delay = 10;

	private float fps = 0.0f;
	private float avg_fps = 0.0f;
	private int fps_probes = 0;
	private final int fps_probe_step = 500; // milliseconds
	private long time = 0;
	private long timestamp = 0;
	private long total_frames;
	private long start_time;
	private long total_time;

	public Animator(World w) {
		world = w;
		thread = new Thread(this, "animation");
		running = false;
	}

	public void start() {
		if (!running) {
			running = true;
			thread.start();
			start_time = System.currentTimeMillis();
		}
	}

	public void stop() {
		running = false;
	}

	public void setDelay(int d) {
		delay = d;
	}

	public void run() {
		while (running) {
			benchmark();
			world.render();
			try {
				Thread.sleep(delay);
			} catch (Exception e) {

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
		time = System.currentTimeMillis();
		total_frames++;
		fps_probes += 1;
		total_time = time - start_time;
		if (System.currentTimeMillis() > timestamp + fps_probe_step) {
			fps = (float) fps_probes / ((float) (time - timestamp) / 1000);
			timestamp = time;
			fps_probes = 0;
			avg_fps = (float) total_frames / total_time * 1000;
		}
	}

}
