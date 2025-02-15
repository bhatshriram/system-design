import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Leaky Bucket Rate Limiter Implementation
 * <p>
 * This class implements rate limiting using the **Leaky Bucket Algorithm**.
 * It ensures a **steady** and **controlled** flow of requests by processing them at
 * a fixed rate, preventing sudden traffic spikes.
 * <p>
 * Why Do We Need Leaky Bucket Limiting?
 * =====================================
 * - **Prevents Sudden Traffic Spikes**: Requests are processed at a constant rate.
 * - **Ensures System Stability**: Unlike token bucket, this method does not allow bursts.
 * - **Fair Usage**: Guarantees a predictable flow, ensuring no single user floods the system.
 * <p>
 * **How It Works:**
 * 1. A **bucket** (queue) holds incoming requests up to **maxCapacity**.
 * 2. Requests arrive and are added to the bucket **if it’s not full**.
 * 3. Requests are **processed (leaked)** at a **fixed leakRate per second**.
 * 4. If a request arrives when the bucket is full, it is **denied**.
 * 5. The bucket continuously **empties at a steady rate**, ensuring smooth processing.
 * <p>
 * **Example Usage:**
 * - A **maxCapacity = 5** and **leakRate = 1/sec** setup:
 * - Allows up to 5 requests to queue.
 * - Processes **1 request per second**.
 * - If more than 5 requests arrive quickly, excess requests are dropped.
 * <p>
 * **Thread Safety Considerations:**
 * - Uses a **synchronized block** to prevent race conditions while adding/removing requests.
 * - Uses a **TimerTask** to leak requests at a fixed interval.
 */


public class LeakyBucketLimiter {

    private final int maxCapacity;

    private final int leakRate;

    private final Queue<Long> bucket;

    public LeakyBucketLimiter(int maxCapacity, int leakRate) {
        this.maxCapacity = maxCapacity;
        this.leakRate = leakRate;
        bucket = new LinkedList<>();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new LeakRate(), 0, 1000 / this.leakRate);
    }

    public boolean allowRequest() {
        long currentTime = System.currentTimeMillis();

        synchronized (this) {
            if (bucket.size() < maxCapacity) {
                bucket.add(currentTime);
                return true;
            } else {
                return false;
            }
        }
    }

    class LeakRate extends TimerTask {

        @Override
        public void run() {
            synchronized (LeakyBucketLimiter.this) {
                if (!bucket.isEmpty()) {
                    bucket.poll();
                }
            }
        }
    }

    public static void main(String[] args) {
        LeakyBucketLimiter rateLimiter = new LeakyBucketLimiter(5, 1);

        // Simulating requests
        for (int i = 0; i < 10; i++) {
            if (rateLimiter.allowRequest()) {
                System.out.println("Request " + (i + 1) + " allowed.");
            } else {
                System.out.println("Request " + (i + 1) + " denied (bucket is full).");
            }

            // Simulate a 200ms delay between requests
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}