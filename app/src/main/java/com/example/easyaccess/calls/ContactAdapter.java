package com.example.easyaccess.calls;

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

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    Context mContext;
    List<Contact> contactList;
    List<Contact> contactListFull;

    public ContactAdapter(Context mContext, List<Contact> contactList) {
        this.mContext = mContext;
        this.contactList = contactList;
        contactListFull = new ArrayList<>(contactList);
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.name_contact.setText(contact.getName());
        holder.phone_contact.setText(contact.getPhone());
        if (contact.getPhoto() != null) {
            if (contact.getPhoto().equals("Drawable")) {
                if (contact.getDir().equals("OUTGOING")) {
                    holder.img_contact.setImageResource(R.drawable.arrow_outgoing);
                } else if (contact.getDir().equals("INCOMING")) {
                    holder.img_contact.setImageResource(R.drawable.arrow_incoming);
                } else {
                    holder.img_contact.setImageResource(R.drawable.missed_call);
                }
            } else {
                Picasso.get().load(contact.getPhoto()).into(holder.img_contact);
            }
        } else {
            holder.img_contact.setImageResource(R.drawable.avatar);
        }

    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }


    public class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView name_contact, phone_contact;
        CircleImageView img_contact;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            name_contact = itemView.findViewById(R.id.name_contact);
            phone_contact = itemView.findViewById(R.id.phone_contact);
            img_contact = itemView.findViewById(R.id.img_contact);
        }
    }
}
