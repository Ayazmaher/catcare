package com.app.catcare.ayaz.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.catcare.ayaz.activities.AddTaskActivity;
import com.app.catcare.ayaz.R;
import com.app.catcare.ayaz.fragments.CreateTaskBottomSheetFragment;
import com.app.catcare.ayaz.database.DatabaseClient;
import com.app.catcare.ayaz.models.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private AddTaskActivity context;
    private LayoutInflater inflater;
    private List<Task> taskList;
    public SimpleDateFormat dateFormat = new SimpleDateFormat("EE dd MMM yyyy", Locale.US);
    public SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd-M-yyyy", Locale.US);
    Date date = null;
    String outputDateString = null;
    CreateTaskBottomSheetFragment.setRefreshListener setRefreshListener;

    public TaskAdapter(AddTaskActivity context, List<Task> taskList, CreateTaskBottomSheetFragment.setRefreshListener setRefreshListener) {
        this.context = context;
        this.taskList = taskList;
        this.setRefreshListener = setRefreshListener;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = inflater.inflate(R.layout.item_task, viewGroup, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.title.setText(task.getTaskTitle());
        holder.description.setText(task.getTaskDescrption());
        holder.time.setText(task.getLastAlarm());
        holder.status.setText(task.isComplete() ? "COMPLETED" : "UPCOMING");
        holder.options.setOnClickListener(view -> showPopUpMenu(view, position));

        try {
            date = inputDateFormat.parse(task.getDate());
            outputDateString = dateFormat.format(date);

            String[] items1 = outputDateString.split(" ");
            String day = items1[0];
            String dd = items1[1];
            String month = items1[2];

            holder.day.setText(day);
            holder.date.setText(dd);
            holder.month.setText(month);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showPopUpMenu(View view, int position) {
        final Task task = taskList.get(position);
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId()==R.id.menuDelete)
            {
                callForDialog(R.string.sureToDelete,task,position,1);
            } else if (item.getItemId()==R.id.menuUpdate) {
                CreateTaskBottomSheetFragment createTaskBottomSheetFragment = new CreateTaskBottomSheetFragment();
                createTaskBottomSheetFragment.setTaskId(task.getTaskId(), true, context, context);
                createTaskBottomSheetFragment.show(context.getSupportFragmentManager(), createTaskBottomSheetFragment.getTag());
            } else if (item.getItemId()==R.id.menuComplete) {
                callForDialog(R.string.sureToMarkAsComplete,task,position,2);
            }
            return false;
        });
        popupMenu.show();
    }

    private void callForDialog(int sureToDelete, Task task, int position,int from) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.layout_confirmation);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        TextView textViewBody=dialog.findViewById(R.id.tvBodyDialog);
        textViewBody.setText(sureToDelete);

        dialog.findViewById(R.id.btnNoDialog).setOnClickListener(view1->{
            dialog.dismiss();
        });

        dialog.findViewById(R.id.btnYesDialog).setOnClickListener(view1->{
            if (from==1)
            {
                deleteTaskFromId(task.getTaskId(), position);
            }
            if (from==2)
            {
                showCompleteDialog(task.getTaskId(), position);
            }
            dialog.dismiss();
        });
        dialog.show();
    }



    public void showCompleteDialog(int taskId, int position) {
        Dialog dialog = new Dialog(context, R.style.Base_Theme_CatCare);
        dialog.setContentView(R.layout.dialog_completed_theme);
        Button close = dialog.findViewById(R.id.closeButton);
        close.setOnClickListener(view -> {
            deleteTaskFromId(taskId, position);
            dialog.dismiss();
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
    }


    private void deleteTaskFromId(int taskId, int position) {
        class GetSavedTasks extends AsyncTask<Void, Void, List<Task>> {
            @Override
            protected List<Task> doInBackground(Void... voids) {
                DatabaseClient.getInstance(context)
                        .getAppDatabase()
                        .dataBaseAction()
                        .deleteTaskFromId(taskId);

                return taskList;
            }

            @Override
            protected void onPostExecute(List<Task> tasks) {
                super.onPostExecute(tasks);
                removeAtPosition(position);
                setRefreshListener.refresh();
            }
        }
        GetSavedTasks savedTasks = new GetSavedTasks();
        savedTasks.execute();
    }

    private void removeAtPosition(int position) {
        taskList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, taskList.size());
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView day;
        TextView date;
        TextView month;
        TextView title;
        TextView description;
        TextView status;

        ImageView options;
        TextView time;




        TaskViewHolder(@NonNull View view) {
            super(view);
            this.day=view.findViewById(R.id.day);
            this.date=view.findViewById(R.id.date);
            this.month=view.findViewById(R.id.month);
            this.title=view.findViewById(R.id.title);
            this.description=view.findViewById(R.id.description);
            this.status=view.findViewById(R.id.status);
            this.options=view.findViewById(R.id.options);
            this.time=view.findViewById(R.id.time);

        }
    }

}
