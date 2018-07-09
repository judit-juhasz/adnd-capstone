package name.juhasz.judit.udacity.tanits.widget;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

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
