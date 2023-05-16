package com.example.easyaccess.sms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyaccess.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ConversationViewHolder> {

    Context mContext;
    List<SMSConversation> conversationList;

    List<SMSConversation> conversationListFull;

    public ConversationsAdapter(Context mContext, List<SMSConversation> conversationList) {
        this.mContext = mContext;
        this.conversationList = conversationList;
        conversationListFull = new ArrayList<>(conversationList);
    }


    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_contact, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        SMSConversation conversation = conversationList.get(position);
        holder.name_contact.setText(conversation.getName());
        holder.last_message.setText(conversation.getMessage());
        if (conversation.getPhoto() != null) {
            Picasso.get().load(conversation.getPhoto()).into(holder.img_contact);
        } else {
            holder.img_contact.setImageResource(R.drawable.avatar);
        }
    }


    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    public class ConversationViewHolder extends RecyclerView.ViewHolder {
        TextView name_contact, last_message;
        CircleImageView img_contact;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            name_contact = itemView.findViewById(R.id.name_contact);
            last_message = itemView.findViewById(R.id.phone_contact);
            img_contact = itemView.findViewById(R.id.img_contact);
        }
    }
}
