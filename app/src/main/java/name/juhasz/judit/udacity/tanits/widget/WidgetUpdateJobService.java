package name.juhasz.judit.udacity.tanits.widget;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import name.juhasz.judit.udacity.tanits.widget.ActiveMessagesWidgetProvider;

public class WidgetUpdateJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters job) {
        ActiveMessagesWidgetProvider.updateAllWidgets(this);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }
}
