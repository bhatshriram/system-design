import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiter Implementation using Token Bucket Algorithm
 * <p>
 * This class implements a rate-limiting mechanism that consists of:
 * 1. **Global Rate Limiter**: Controls the total number of requests across all users
 * to prevent system-wide overload.
 * 2. **User-Specific Rate Limiter**: Ensures individual users do not exceed
 * their allowed request quota, maintaining fairness.
 * <p>
 * Why Do We Need Both?
 * ====================
 * In large-scale systems, rate limiting is essential to:
 * - **Prevent Abuse**: A single user shouldn't be able to consume all system resources.
 * - **Ensure Fairness**: Different users should have equal opportunities to make requests.
 * - **Protect System Stability**: Unchecked high traffic can overwhelm servers, leading to downtime.
 * <p>
 * **Global Rate Limiter**:
 * - Controls the **total** request flow into the system.
 * - Protects against Distributed Denial of Service (DDoS) attacks.
 * - Ensures system resources are not exhausted by excessive traffic.
 * <p>
 * **User-Specific Rate Limiter**:
 * - Ensures no single user dominates the system.
 * - Helps enforce **per-user quotas** for APIs.
 * - Prevents unintended side effects where a single high-traffic user
 * blocks others from using the service.
 * <p>
 * **Example Scenario**:
 * - The system allows **25 global requests** per unit time with a refill rate of **15 tokens**.
 * - Each user can make **3 requests** per unit time with a refill rate of **1 token**.
 * - If a user exceeds their quota, their requests are blocked.
 * - If the global limit is hit, all further requests (even from valid users) are denied.
 * <p>
 * **How It Works**:
 * 1. The **global rate limiter** checks if the total allowed requests are within limit.
 * 2. If the global check passes, the **user-specific limiter** checks if the user can proceed.
 * 3. If both checks pass, the request is allowed.
 * 4. Otherwise, the request is denied.
 * <p>
 * This ensures both **fair access** and **system stability**.
 */

public class GlobalRateLimiter {

    private final int globalMaxTokens;
    private final int globalRefillTokens;

    private final int userMaxTokens;
    private final int userRefillTokens;

    private final ConcurrentHashMap<String, TokenBucketLimiter> userBucket;
    private final TokenBucketLimiter globalLimiter;


    GlobalRateLimiter(int globalMaxTokens, int globalRefillTokens, int userMaxTokens, int userRefillTokens) {
        this.globalMaxTokens = globalMaxTokens;
        this.globalRefillTokens = globalRefillTokens;
        this.userMaxTokens = userMaxTokens;
        this.userRefillTokens = userRefillTokens;
        userBucket = new ConcurrentHashMap<>();
        globalLimiter = new TokenBucketLimiter(globalMaxTokens, globalRefillTokens);
    }


    public boolean allowRequest(String userId) {

        if (!globalLimiter.allowRequest()) {
            return false;
        }

        if (!userBucket.containsKey(userId)) {
            userBucket.putIfAbsent(userId, new TokenBucketLimiter(userMaxTokens, userRefillTokens));
        }

        return userBucket.get(userId).allowRequest();
    }

    public static void main(String[] args) {

        GlobalRateLimiter rateLimiter = new GlobalRateLimiter(25, 15, 3, 1);

        String userId = "User1";
        for (int i = 0; i < 15; i++) {
            if (rateLimiter.allowRequest(userId)) {
                System.out.println("The request " + (i + 1) + " is allowed for user " + userId);
            } else {
                System.out.println("The request " + (i + 1) + " is denied for user " + userId);
            }
            //Thread.sleep(700);
        }
    }
}

