import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Token Bucket Rate Limiter Implementation
 * <p>
 * This class implements a rate-limiting mechanism using the **Token Bucket Algorithm**.
 * It controls the rate at which requests are allowed by maintaining a bucket of tokens
 * that refills at a fixed rate.
 * <p>
 * Why Do We Need Token Bucket Limiting?
 * =====================================
 * - **Prevents Overloading**: Ensures a steady flow of requests without overwhelming the system.
 * - **Allows Bursts**: Unlike a fixed rate limiter, this allows short bursts of requests
 * as long as tokens are available.
 * - **Fair Usage**: Ensures no user or process monopolizes resources.
 * <p>
 * **How It Works:**
 * 1. A bucket holds up to **maxTokens**.
 * 2. Tokens are added to the bucket at a rate of **refillRate per second**.
 * 3. Each request consumes one token.
 * 4. If a request arrives when no tokens are available, it is **denied**.
 * 5. The bucket refills periodically, ensuring smooth request flow over time.
 * <p>
 * **Example Usage:**
 * - A **maxTokens = 10** and **refillRate = 5/sec** setup:
 * - Allows up to 10 requests instantly.
 * - If tokens are exhausted, requests are blocked until more tokens are refilled.
 * - Every second, 5 new tokens are added.
 * <p>
 * **Thread Safety Considerations:**
 * - Uses `AtomicInteger` to ensure concurrent safety while updating available tokens.
 * - Uses `AtomicLong` to track the last refill time and prevent race conditions.
 */


public class TokenBucketLimiter {

    private final int maxTokens;
    private final int refillRate;
    private final AtomicInteger availableTokens;
    private final AtomicLong lastRefreshTime;

    TokenBucketLimiter(int maxTokens, int refillRate) {
        this.maxTokens = maxTokens;
        this.refillRate = refillRate;
        this.availableTokens = new AtomicInteger(maxTokens);
        this.lastRefreshTime = new AtomicLong(System.nanoTime());
    }

    public boolean allowRequest() {
        refillTokens();
        if (this.availableTokens.get() > 0) {
            this.availableTokens.decrementAndGet();
            return true;
        } else {
            return false;
        }
    }

    public void refillTokens() {
        long now = System.nanoTime();
        long elapsedTime = now - this.lastRefreshTime.get();
        int tokensToAdd = (int) (elapsedTime / 1000000000 * refillRate);

        if (tokensToAdd > 0) {
            this.availableTokens.set(Math.min(tokensToAdd + this.availableTokens.get(), maxTokens));
            this.lastRefreshTime.set(now);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        TokenBucketLimiter rateLimiter = new TokenBucketLimiter(10, 5);
        for (int i = 0; i < 15; i++) {
            if (rateLimiter.allowRequest()) {
                System.out.println("The request " + (i + 1) + " is allowed!");
            } else {
                System.out.println("The request " + (i + 1) + " is not allowed!");
            }
            //Thread.sleep(700);
        }
    }
}