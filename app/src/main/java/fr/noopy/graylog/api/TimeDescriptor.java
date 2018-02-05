package fr.noopy.graylog.api;

/**
 * Created by cyrille on 05/02/18.
 */

public class TimeDescriptor {

    public int duration;
    public String label;


    public TimeDescriptor(int mDuration, String mLabel) {
        duration = mDuration;
        label = mLabel;
    }

    @Override
    public String toString() {
        return label;
    }
}
