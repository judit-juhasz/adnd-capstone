package name.juhasz.judit.udacity.tanits;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final Message[] sDummyData = {
            new Message("Me before you", "2018.05.15"),
            new Message("Avatar", "2018.05.15"),
            new Message("A beautiful mind", "2018.05.15"),
            new Message("A beautiful mind", "2018.05.15"),
            new Message("Pearl Harbour", "2018.05.15"),
            new Message("Saving private Ryan", "2018.05.15"),
            new Message("Son of Saul", "2018.05.15")
    };


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
        return sDummyData.length;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView subjectTextView;
        public TextView dateTextView;

        public MessageViewHolder(View itemView) {
            super(itemView);

            subjectTextView = (TextView) itemView.findViewById(R.id.tv_subject);
            dateTextView = (TextView) itemView.findViewById(R.id.tv_date);
        }

        void bind(int position) {
            final Message message = sDummyData[position];
            String subjectOfMessage = message.getSubject();
            subjectTextView.setText(subjectOfMessage);
            String dateOfMessage = message.getDate();
            dateTextView.setText(dateOfMessage);
        }
    }
}
