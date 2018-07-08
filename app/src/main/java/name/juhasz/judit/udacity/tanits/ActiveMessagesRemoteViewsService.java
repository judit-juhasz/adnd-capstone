package name.juhasz.judit.udacity.tanits;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;

import org.joda.time.LocalDate;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import name.juhasz.judit.udacity.tanits.util.FirebaseUtils;
import name.juhasz.judit.udacity.tanits.util.NetworkUtils;

public class ActiveMessagesRemoteViewsService extends RemoteViewsService {
    public static final String EXTRA_MESSAGES = "EXTRA_MESSAGES";
    private static final String LOG_TAG = ActiveMessagesRemoteViewsService.class.getSimpleName();
    private static final int ON_DATA_SET_CHANGED_TIMEOUT_IN_SECONDS = 15;

    private class ActiveMessagesRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private Message[] mMessages = null;

        public ActiveMessagesRemoteViewsFactory(final Message[] messages) {
            this.mMessages = messages;
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            FirebaseUtils.initialize(ActiveMessagesRemoteViewsService.this);
            if (!NetworkUtils.isNetworkAvailable(ActiveMessagesRemoteViewsService.this)) {
                return;
            }
            final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (null == currentFirebaseUser) {
                mMessages = null;
                return;
            }
            // Need to wait for the result, because the other functions will be called after this
            // onDataSetChanged() call. Unfortunately, Firebase Realtime Database doesn't provide
            // synchronous calls by default. How to access Firebase realtime DB synchronously:
            // https://stackoverflow.com/a/31702957
            final Semaphore queryResultWaiter = new Semaphore(0);
            FirebaseUtils.queryUserProfile(new FirebaseUtils.UserProfileListener() {
                @Override
                public void onReceive(UserProfile userProfile) {
                    if (null == userProfile || null == userProfile.getChildBirthdate()) {
                        queryResultWaiter.release();
                        return;
                    }
                    final LocalDate childBirthdate = new LocalDate(userProfile.getChildBirthdate());
                    FirebaseUtils.queryMessages(childBirthdate, FirebaseUtils.MESSAGE_STATUS_FILTER_ACTIVE,
                            new FirebaseUtils.MessageListListener() {
                                @Override
                                public void onReceive(List<Message> messageList) {
                                    if (null == messageList || messageList.isEmpty()) {
                                        mMessages = null;
                                    } else {
                                        mMessages = messageList.toArray(new Message[messageList.size()]);
                                    }
                                    queryResultWaiter.release();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.w(LOG_TAG, getString(R.string.log_message_query_canceled),
                                            databaseError.toException());
                                    queryResultWaiter.release();
                                }
                            }, false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w(LOG_TAG, getString(R.string.log_user_profile_query_canceled),
                            databaseError.toException());
                    queryResultWaiter.release();
                }
            }, false);
            try {
                queryResultWaiter.tryAcquire(ON_DATA_SET_CHANGED_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                Log.i(LOG_TAG, "Messages query was interrupted ", e);
            }
        }

        @Override
        public void onDestroy() {
        }

        @Override
        public int getCount() {
            return null == mMessages ? 0 : mMessages.length;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (AdapterView.INVALID_POSITION == position || null == mMessages) {
                return null;
            }

            final RemoteViews views =
                    new RemoteViews(getPackageName(), R.layout.widget_list_elem_message);

            views.setTextViewText(R.id.widget_list_elem_message_date, mMessages[position].getDate());
            views.setTextViewText(R.id.widget_list_elem_message_summary, mMessages[position].getSummary());

            // https://stackoverflow.com/a/14811595
            final Intent fillInIntent = new Intent();
            views.setOnClickFillInIntent(R.id.widget_list_elem_message, fillInIntent);

            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(final Intent intent) {
        Message[] messages = null;
        final Bundle messagesBundleExtra = (null != intent && intent.hasExtra(EXTRA_MESSAGES)) ?
                intent.getBundleExtra(EXTRA_MESSAGES) : null;
        if (null != messagesBundleExtra) {
            // https://stackoverflow.com/q/8745893
            final Parcelable[] parcelableMessages = messagesBundleExtra.getParcelableArray(EXTRA_MESSAGES);
            messages = new Message[parcelableMessages.length];
            System.arraycopy(parcelableMessages, 0, messages, 0, parcelableMessages.length);
        }
        return new ActiveMessagesRemoteViewsFactory(messages);
    }
}
