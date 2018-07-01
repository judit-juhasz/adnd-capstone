package name.juhasz.judit.udacity.tanits;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class WidgetUpdateJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters job) {
        LastActiveMessageWidgetProvider.updateAllWidgets(this);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }
}
