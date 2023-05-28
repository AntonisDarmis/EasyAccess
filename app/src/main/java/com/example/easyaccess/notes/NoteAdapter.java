package com.example.easyaccess.notes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyaccess.DialogFormatter;
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
        holder.description.setText(DialogFormatter.insertNewLineAtWordsCount(note.getDescription(),5));
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


}
