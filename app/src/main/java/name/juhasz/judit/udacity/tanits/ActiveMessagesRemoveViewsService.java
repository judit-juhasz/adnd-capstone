package name.juhasz.judit.udacity.tanits;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class ActiveMessagesRemoveViewsService extends RemoteViewsService {
    public static String EXTRA_MESSAGES = "EXTRA_MESSAGES";

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
