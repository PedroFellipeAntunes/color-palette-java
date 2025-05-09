package Data;

public class ChannelRange {
    private final float min;
    private final float max;
    private final float step;

    public ChannelRange(float min, float max, float step) {
        if (max <= min) {
            throw new IllegalArgumentException("Max must be greater than Min");
        }
        
        if (step <= 0) {
            throw new IllegalArgumentException("Step must be greater than 0");
        }
        
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public float getStep() {
        return step;
    }
}