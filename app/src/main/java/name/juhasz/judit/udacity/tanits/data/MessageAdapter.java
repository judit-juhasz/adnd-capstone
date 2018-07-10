package name.juhasz.judit.udacity.tanits.data;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import name.juhasz.judit.udacity.tanits.R;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private static final String LOG_TAG = MessageAdapter.class.getSimpleName();

    private static final String COLOR_ACTIVE = "#81D4FA";
    private static final String COLOR_DONE = "#A5D6A7";
    private static final String COLOR_REJECTED = "#B0BEC5";

    private Message[] mMessages = null;

    private OnClickListener mListener;
    private Context mContext = null;

    public interface OnClickListener {
        void onItemClick(final Message message);
    }

    public MessageAdapter(final Context context, final OnClickListener listener) {
        mContext = context;
        mListener = listener;
    }

    public void setMessages(final Message[] messages) {
        this.mMessages = messages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final Context context = parent.getContext();

        final int layoutIdForListItem = R.layout.item_messages;
        final LayoutInflater inflater = LayoutInflater.from(context);
        final boolean shouldAttachToParentImmediately = false;

        final View view =
                inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        final MessageViewHolder viewHolder = new MessageViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        if (null == mMessages) {
            return 0;
        } else {
            return mMessages.length;
        }
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView mDateTextView;
        public TextView mSummaryTextView;

        public MessageViewHolder(View itemView) {
            super(itemView);

            mDateTextView = (TextView) itemView.findViewById(R.id.tv_date);
            mSummaryTextView = (TextView) itemView.findViewById(R.id.tv_summary);
            itemView.setOnClickListener(this);
        }

        void bind(int position) {
            final Message message = mMessages[position];
            final String dateOfMessage = message.getDate();
            mDateTextView.setText(dateOfMessage);
            final String summaryOfMessage = message.getSummary();
            mSummaryTextView.setText(summaryOfMessage);
            ImageView statusImageView = itemView.findViewById(R.id.iv_status_image);
            ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
            switch (message.getStatus()) {
                case Message.STATUS_ACTIVE:
                    drawable.getPaint().setColor(Color.parseColor(COLOR_ACTIVE));
                    break;
                case Message.STATUS_DONE:
                    drawable.getPaint().setColor(Color.parseColor(COLOR_DONE));
                    break;
                case Message.STATUS_REJECTED:
                    drawable.getPaint().setColor(Color.parseColor(COLOR_REJECTED));
                    break;
                default:
                    Log.w(LOG_TAG, mContext.getString(R.string.log_unknown_message_status, message.getStatus()));
                    drawable.getPaint().setColor(Color.parseColor(COLOR_ACTIVE));
            }
            statusImageView.setBackground(drawable);
        }

        @Override
        public void onClick(View v) {
            final int adapterPosition = getAdapterPosition();
            final Message message = mMessages[adapterPosition];
            mListener.onItemClick(message);
        }
    }
}
