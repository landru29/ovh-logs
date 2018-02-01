package fr.noopy.graylog.task;

/**
 * Created by cmeichel on 01/02/18.
 */

public class TaskReport<TObject>{

    public void onComplete() {

    }

    public void onFailure(String reason) {
        onComplete();
    }

    public void onSuccess(TObject data) {
        onComplete();
    }
}
