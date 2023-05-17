package com.example.easyaccess.sms;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyaccess.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private List<Message> messages;
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    public ChatAdapter(Context mContext, List<Message> messages) {
        this.mContext = mContext;
        this.messages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == VIEW_TYPE_MESSAGE_SENT){
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_me,parent,false);
            return new SentMessageHolder(view);
        }
        else{
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_other,parent,false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if(holder.getItemViewType() == VIEW_TYPE_MESSAGE_SENT){

        }
    }

    @Override
    public int getItemViewType(int position){
        Message message = (Message) messages.get(position);
        if (message.getType() == 1){
            return VIEW_TYPE_MESSAGE_SENT;
        }
        else{
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder{
        TextView message,time,name;
        ImageView profileImage;


        public ReceivedMessageHolder(@NonNull View itemView) {
            super(itemView);
            message = (TextView) itemView.findViewById(R.id.text_gchat_message_other);
            time = (TextView) itemView.findViewById(R.id.text_gchat_date_other);
            name = (TextView) itemView.findViewById(R.id.text_gchat_user_other);
            profileImage = (ImageView) itemView.findViewById(R.id.image_gchat_profile_other);
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder{
        TextView message, date, time;

        public SentMessageHolder(@NonNull View itemView){
            super(itemView);
            message = (TextView) itemView.findViewById(R.id.text_gchat_message_me);
            date = (TextView) itemView.findViewById(R.id.text_gchat_date_me);
            time = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_me);
        }

        public void bind(){}
    }
}
