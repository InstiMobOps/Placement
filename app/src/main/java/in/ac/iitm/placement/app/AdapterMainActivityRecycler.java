package in.ac.iitm.placement.app;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by arun on 17-Jul-15.
 */
public class AdapterMainActivityRecycler extends RecyclerView.Adapter<AdapterMainActivityRecycler.ViewHolder> {
    ArrayList<Event> mDataset;
    Context context;


    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterMainActivityRecycler(Context context, ArrayList<Event> myDataset) {
        this.mDataset = myDataset;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AdapterMainActivityRecycler.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                     int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_activity_list_single, parent, false);
        // set the view's size, margins, padding and layout parameters
        //...


        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
//        datePost = Float.toString(date.getTime()) ;
        holder.name.setText(mDataset.get(position).getName());
        holder.event.setText(mDataset.get(position).getEvent());
        holder.date.setText(mDataset.get(position).getDate());
        holder.venue.setText(mDataset.get(position).getVenue());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDialog(mDataset.get(position));
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    protected void showCustomDialog(final Event Data) {

        final Dialog dialog = new Dialog(context);
        //.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_event);
        dialog.setTitle(Data.getName());
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        TextView dateV, event, venue, description;
        Button cancel, calendar;
        //final EditText editText = (EditText)dialog.findViewById(R.id.editText1);
        // cancel = (Button)dialog.findViewById(R.id.cancel);
        calendar = (Button) dialog.findViewById(R.id.calendar);

        dateV = (TextView) dialog.findViewById(R.id.date);
        event = (TextView) dialog.findViewById(R.id.event);
        venue = (TextView) dialog.findViewById(R.id.venue);
        description = (TextView) dialog.findViewById(R.id.description);

       /* cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });   */
        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Calendar cal = null;
                cal=Calendar.getInstance();
                cal.setTime(Data.getFormatedDate());
                addEvent(Data.getName()+" - "+Data.getEvent(), Data.getVenue(),cal.getTimeInMillis(),cal.getTimeInMillis());
            }
        });
        dateV.setText(Data.getDate());
        venue.setText(Data.getVenue());
        event.setText(Data.getEvent());
        description.setText(Data.getDiscription());
        dialog.show();
        dialog.getWindow().setAttributes(lp);

    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView name, date, venue, event;
        public CardView cardView;

        public ViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.name);
            date = (TextView) v.findViewById(R.id.date);
            venue = (TextView) v.findViewById(R.id.venue);
            event = (TextView) v.findViewById(R.id.event);
            cardView = (CardView) v.findViewById(R.id.card_view);

        }
    }
    public void addEvent(String title, String location, long begin, long end) {
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, title)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

}