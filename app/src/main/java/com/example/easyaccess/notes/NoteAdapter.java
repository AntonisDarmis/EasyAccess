package com.example.easyaccess.notes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyaccess.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    Context mContext;
    List<Note> notes = new ArrayList<>();

    public NoteAdapter(Context context, List<Note> notes) {
        this.mContext = context;
        this.notes = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.title.setText(note.getTitle());
        holder.description.setText(insertNewLineAtFiveWords(note.getDescription()));
        holder.id.setText(String.valueOf(note.getId()));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, id;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_view_title);
            description = itemView.findViewById(R.id.text_view_description);
            id = itemView.findViewById(R.id.text_view_priority);
        }
    }

    public String insertNewLineAtFiveWords(String description) {
        // Split the description into words
        String[] words = description.split("\\b");

        // Initialize a counter to keep track of the number of words processed
        int wordCount = 0;

        // Initialize a StringBuilder to construct the new string
        StringBuilder resultBuilder = new StringBuilder();

        // Iterate through the words
        for (String word : words) {
            // Skip empty strings
            if (word.trim().isEmpty()) {
                continue;
            }

            // Append the word to the result
            resultBuilder.append(word).append(" ");

            // Increment the word count
            wordCount++;

            // Insert a new line after every 5 words
            if (wordCount % 5 == 0) {
                resultBuilder.append("\n");
            }
        }

        // Convert the StringBuilder to a string and return
        return resultBuilder.toString().trim();
    }
}
