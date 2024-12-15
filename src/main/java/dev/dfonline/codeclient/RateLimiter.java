package dev.dfonline.codeclient;

/**
 * A rate limiter that can be used to limit the rate of execution of a certain operation.
 */
public class RateLimiter {
    private final int incrementStep;
    private final int threshold;
    private int count;

    /**
     * Creates a new rate limiter.
     * @param incrementStep The amount to increment the count by each time the operation is executed.
     * @param threshold The maximum count before the operation is rate limited.
     */
    public RateLimiter(int incrementStep, int threshold) {
        this.incrementStep = incrementStep;
        this.threshold = threshold;
    }

    /**
     * Registers an operation, incrementing the count.
     */
    public void increment() {
        count += incrementStep;
    }

    /**
     * Decrements the count, should be called once per tick.
     */
    public void tick() {
        if (count > 0) {
            --count;
        }
    }

    /**
     * Checks if the operation is rate limited.
     * @return True if the operation is rate limited, false otherwise.
     */
    public boolean isRateLimited() {
        return count >= threshold;
    }
}