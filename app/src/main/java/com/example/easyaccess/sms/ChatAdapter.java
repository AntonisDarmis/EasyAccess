package com.example.easyaccess.sms;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyaccess.R;
import com.squareup.picasso.Picasso;

import java.time.Year;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private List<Message> messages;
    private int highlightedItem = -1;


    public ChatAdapter(Context mContext, List<Message> messages) {
        this.mContext = mContext;
        this.messages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_other, parent, false);
            return new ReceivedMessageHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_me, parent, false);
            return new SentMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder.getItemViewType() == 1) {
            // Log.d("RECEIVED  MESSAGE TIME: ",message.getTime());
            ((ReceivedMessageHolder) holder).message.setText(message.getMessage());
            ((ReceivedMessageHolder) holder).name.setText(message.getName());
            if (highlightedItem == position) {
                // Apply highlighting to the message
                ((ReceivedMessageHolder) holder).message.setBackgroundColor(mContext.getResources().getColor(R.color.purple_200));
            } else {
                // Reset the background color of the message
                ((ReceivedMessageHolder) holder).message.setBackgroundColor(Color.TRANSPARENT);
            }
            if (message.getTime().length() != 5) {
                //  ((ReceivedMessageHolder) holder).date.setText(message.getTime().substring(0, 10));
                ((ReceivedMessageHolder) holder).time.setText(message.getTime().substring(11, 16));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (Year.now().getValue() != Integer.parseInt(message.getTime().substring(30))) {
                        String withYear = message.getTime().substring(0, 10) + " " + message.getTime().substring(30);
                        ((ReceivedMessageHolder) holder).date.setText(withYear);
                    } else {
                        ((ReceivedMessageHolder) holder).date.setText(message.getTime().substring(0, 10));
                    }
                }
            } else {
                ((ReceivedMessageHolder) holder).date.setText("");
                ((ReceivedMessageHolder) holder).time.setText(message.getTime());
            }
            if (message.getProfileUrl() != null) {
                Picasso.get().load(message.getProfileUrl()).into(((ReceivedMessageHolder) holder).profileImage);
            }

        } else {
            ((SentMessageHolder) holder).message.setText(message.getMessage());
            if (highlightedItem == position) {
                // Apply highlighting to the message
                ((SentMessageHolder) holder).message.setBackgroundColor(mContext.getResources().getColor(R.color.purple_200));
            } else {
                // Reset the background color of the message
                ((SentMessageHolder) holder).message.setBackgroundColor(Color.TRANSPARENT);
            }
            if (!(message.getTime().length() == 5)) {
                ((SentMessageHolder) holder).time.setText(message.getTime().substring(10, 16));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (Year.now().getValue() != Integer.parseInt(message.getTime().substring(30))) {
                        String withYear = message.getTime().substring(0, 10) + " " + message.getTime().substring(30);
                        ((SentMessageHolder) holder).date.setText(withYear);
                    } else {
                        ((SentMessageHolder) holder).date.setText(message.getTime().substring(0, 10));
                    }
                }
            } else {
                ((SentMessageHolder) holder).date.setText(" ");
                ((SentMessageHolder) holder).time.setText(message.getTime());
            }
        }
//        if (highlightedItem == position) {
//            // Apply highlighting to the item
//            holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.purple_200));
//        } else {
//            // Reset the background color of the item
//            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
//        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = (Message) messages.get(position);
        if (message.getType() == 1) {
            return 1;
        } else {
            return 2;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView message, time, name, date;
        ImageView profileImage;


        public ReceivedMessageHolder(@NonNull View itemView) {
            super(itemView);
            message = (TextView) itemView.findViewById(R.id.text_gchat_message_other);
            date = (TextView) itemView.findViewById(R.id.text_gchat_date_other);
            name = (TextView) itemView.findViewById(R.id.text_gchat_user_other);
            time = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_other);
            profileImage = (ImageView) itemView.findViewById(R.id.image_gchat_profile_other);
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView message, date, time;

        public SentMessageHolder(@NonNull View itemView) {
            super(itemView);
            message = (TextView) itemView.findViewById(R.id.text_gchat_message_me);
            date = (TextView) itemView.findViewById(R.id.text_gchat_date_me);
            time = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_me);
        }
    }



    public void setHighlightedItem(int position) {
        int previousHighlightedItem = highlightedItem;
        highlightedItem = position;
        if (previousHighlightedItem != -1) {
            notifyItemChanged(previousHighlightedItem);
        }
        notifyItemChanged(position);
    }

    public void clearHighlightedItem() {
        int previousHighlightedItem = highlightedItem;
        highlightedItem = -1;
        if (previousHighlightedItem != -1) {
            notifyItemChanged(previousHighlightedItem);
        }
    }

}
