package com.example.easyaccess.reminders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyaccess.R;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
    Context mContext;
    List<ReminderModel> reminders;

    public ReminderAdapter(Context mContext, List<ReminderModel> reminders) {
        this.mContext = mContext;
        this.reminders = reminders;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        ReminderModel reminder = reminders.get(position);
        //get and set logic for holder
        holder.reminderCategory.setText(reminder.getCategory() + " (ID:" + reminder.getId() + ")");
        if(reminder.getDescription().length() == 0) {
            holder.reminderDescription.setText("No description given.");
        }
        else{
            holder.reminderDescription.setText(reminder.getDescription());
        }
        if (reminder.getDate().length() == 10 || reminder.getDate().length() == 5) {
            holder.reminderDate.setText(reminder.getDate() + " " + reminder.getTime());
        } else {
            holder.reminderDate.setText(reminder.getTime());
        }
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    public class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView reminderCategory, reminderDescription, reminderDate, reminderFrequency;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            reminderCategory = itemView.findViewById(R.id.reminderCategory);
            reminderDescription = itemView.findViewById(R.id.reminderDescription);
            reminderDate = itemView.findViewById(R.id.reminderDate);
        }
    }


}
