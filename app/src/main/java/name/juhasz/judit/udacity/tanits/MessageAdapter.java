package name.juhasz.judit.udacity.tanits;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Message[] mMessages = null;

    private OnClickListener mListener;
    private Context mContext = null;

    public interface OnClickListener {
        void onItemClick(Message message);
    }

    public MessageAdapter(final Context context, final OnClickListener listener) {
        mContext = context;
        mListener = listener;
    }

    public void setMessages(final Message[] messages) {
        this.mMessages = messages;
        notifyDataSetChanged();
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();

        int layoutIdForListItem = R.layout.item_messages;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        MessageViewHolder viewHolder = new MessageViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
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

        public TextView subjectTextView;
        public TextView dateTextView;

        public MessageViewHolder(View itemView) {
            super(itemView);

            subjectTextView = (TextView) itemView.findViewById(R.id.tv_subject);
            dateTextView = (TextView) itemView.findViewById(R.id.tv_date);
            itemView.setOnClickListener(this);
        }

        void bind(int position) {
            final Message message = mMessages[position];
            String subjectOfMessage = message.getSubject();
            subjectTextView.setText(subjectOfMessage);
            String dateOfMessage = message.getDate();
            dateTextView.setText(dateOfMessage);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Message message = mMessages[adapterPosition];
            mListener.onItemClick(message);
        }
    }
}
