package name.juhasz.judit.udacity.tanits;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final Message[] sDummyData = {
            new Message("Welcoming a new baby into your family", "2018-05-14T22:02:54+00:00"),
            new Message("Why art and creativity are important", "2018-05-15T22:02:54+00:00"),
            new Message("The role of parents in infant/toddler development", "2018-05-16T20:02:54+00:00"),
            new Message("Why parents sing to babies", "2018-05-17T13:02:54+00:00"),
            new Message("What role do parents play in a baby's brain development?", "2018-05-18T13:02:54+00:00"),
            new Message("How to support your child’s communication skills", "2018-05-19T18:02:54+00:00"),
            new Message("Baby sleep basics: birth to three months", "2018-05-20T18:02:54+00:00"),
            new Message("Why parents sing to babies 2", "2018-05-21T13:02:54+00:00"),
            new Message("How to support your child’s communication skills 2", "2018-05-22T18:02:54+00:00")
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
