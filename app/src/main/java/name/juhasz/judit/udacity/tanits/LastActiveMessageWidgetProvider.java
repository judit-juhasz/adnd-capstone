package name.juhasz.judit.udacity.tanits;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;

import org.joda.time.LocalDate;

import java.util.List;

import name.juhasz.judit.udacity.tanits.util.FirebaseUtils;
import name.juhasz.judit.udacity.tanits.util.NetworkUtils;

public class LastActiveMessageWidgetProvider extends AppWidgetProvider {

    private static String JOB_SCHEDULER_ID = "LastActiveMessageWidgetProviderJobScheduler";

    public static void updateAllWidgets(@NonNull final Context context) {
        final Class<LastActiveMessageWidgetProvider> widgetProviderClass =
                LastActiveMessageWidgetProvider.class;
        final Intent updateWidgetsIntent = new Intent(context, widgetProviderClass);
        updateWidgetsIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        final int[] appWidgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, widgetProviderClass));
        updateWidgetsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        context.sendBroadcast(updateWidgetsIntent);
    }

    private static final String LOG_TAG = LastActiveMessageWidgetProvider.class.getSimpleName();

    static void showMessageList(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, @NonNull final List<Message> messages) {
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_last_active_message);

        showMessageView(views);

        final int lastMessageIndex = messages.size()-1;
        final Message lastMessage = messages.get(lastMessageIndex);

        views.setTextViewText(R.id.widget_last_active_message_date, lastMessage.getDate());
        final Intent remoteViewServiceIntent =
                new Intent(context, ActiveMessagesRemoveViewsService.class);
        remoteViewServiceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        final Message[] messagesArray = messages.toArray(new Message[messages.size()]);
        // https://stackoverflow.com/q/13363046
        final Bundle bundle = new Bundle();
        bundle.putParcelableArray(ActiveMessagesRemoveViewsService.EXTRA_MESSAGES, messagesArray);
        remoteViewServiceIntent.putExtra(ActiveMessagesRemoveViewsService.EXTRA_MESSAGES, bundle);
        remoteViewServiceIntent.setData(Uri.parse(remoteViewServiceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.widget_active_messages_list, remoteViewServiceIntent);

        final Intent mainActivityIntent = new Intent(context, MainActivity.class);
        final PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 0, mainActivityIntent, 0);
        views.setOnClickPendingIntent(R.id.widget_last_active_message, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    static void showAppWidgetNotification(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, final String notificationText) {
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_last_active_message);
        showNotificationView(views);
        views.setTextViewText(R.id.widget_notification, notificationText);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager,
                         final int[] appWidgetIds) {
        FirebaseUtils.initialize(context);
        if (!NetworkUtils.isNetworkAvailable(context)) {
            for (int appWidgetId : appWidgetIds) {
                showAppWidgetNotification(context, appWidgetManager, appWidgetId,
                        context.getString(R.string.internet_required));
            }
            return;
        }
        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (null == currentFirebaseUser) {
            for (int appWidgetId : appWidgetIds) {
                showAppWidgetNotification(context, appWidgetManager, appWidgetId,
                        context.getString(R.string.widget_login_notification));
            }
            return;
        }
        FirebaseUtils.queryUserProfile(new FirebaseUtils.UserProfileListener() {
            @Override
            public void onReceive(UserProfile userProfile) {
                if (null == userProfile || null == userProfile.getChildBirthdate()) {
                    for (int appWidgetId : appWidgetIds) {
                        showAppWidgetNotification(context, appWidgetManager, appWidgetId,
                                context.getString(R.string.widget_login_notification));
                    }
                    return;
                }
                final LocalDate childBirthdate = new LocalDate(userProfile.getChildBirthdate());
                FirebaseUtils.queryMessages(childBirthdate, FirebaseUtils.MESSAGE_STATUS_FILTER_ACTIVE,
                        new FirebaseUtils.MessageListListener() {
                            @Override
                            public void onReceive(List<Message> messageList) {
                                if (messageList.isEmpty()) {
                                    for (int appWidgetId : appWidgetIds) {
                                        showAppWidgetNotification(context, appWidgetManager, appWidgetId,
                                                context.getString(R.string.all_message_done));
                                    }
                                    return;
                                }
                                for (int appWidgetId : appWidgetIds) {
                                    showMessageList(context, appWidgetManager, appWidgetId, messageList);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                for (int appWidgetId : appWidgetIds) {
                                    showAppWidgetNotification(context, appWidgetManager, appWidgetId,
                                            context.getString(R.string.error_unknown));
                                }
                                Log.w(LOG_TAG, context.getString(R.string.log_message_query_canceled),
                                        databaseError.toException());
                            }
                        }, false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                for (int appWidgetId : appWidgetIds) {
                    showAppWidgetNotification(context, appWidgetManager, appWidgetId,
                            context.getString(R.string.error_unknown));
                }
                Log.w(LOG_TAG, context.getString(R.string.log_user_profile_query_canceled),
                        databaseError.toException());
            }
        }, false);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        final FirebaseJobDispatcher dispatcher =
                new FirebaseJobDispatcher(new GooglePlayDriver(context));
        final int hourInSeconds =  60 * 60;
        final int halfHourInSeconds = hourInSeconds / 2;
        final Job widgetUpdateJob = dispatcher.newJobBuilder()
                .setService(WidgetUpdateJobService.class)
                .setTag(JOB_SCHEDULER_ID)
                .setLifetime(Lifetime.FOREVER)
                .setReplaceCurrent(true)
                .setRecurring(true)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setTrigger(Trigger.executionWindow(hourInSeconds
                        , hourInSeconds + halfHourInSeconds))
                .build();

        dispatcher.mustSchedule(widgetUpdateJob);
    }

    @Override
    public void onDisabled(Context context) {
        final FirebaseJobDispatcher dispatcher =
                new FirebaseJobDispatcher(new GooglePlayDriver(context));
        dispatcher.cancel(JOB_SCHEDULER_ID);
    }

    private static void showNotificationView(final RemoteViews views) {
        views.setViewVisibility(R.id.widget_last_active_message_date, View.GONE);
        views.setViewVisibility(R.id.widget_last_active_message_author, View.GONE);
        views.setViewVisibility(R.id.widget_active_messages_list, View.GONE);
        views.setViewVisibility(R.id.widget_notification, View.VISIBLE);
    }

    private static void showMessageView(final RemoteViews views) {
        views.setViewVisibility(R.id.widget_last_active_message_date, View.VISIBLE);
        views.setViewVisibility(R.id.widget_last_active_message_author, View.VISIBLE);
        views.setViewVisibility(R.id.widget_active_messages_list, View.VISIBLE);
        views.setViewVisibility(R.id.widget_notification, View.GONE);
    }
}

