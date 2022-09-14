package com.yh.parkingpartner.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.yh.parkingpartner.R;
import com.yh.parkingpartner.model.Data;
import com.yh.parkingpartner.model.DataListRes;
import com.yh.parkingpartner.ui.MainActivity;
import com.yh.parkingpartner.ui.ParkListActivity;
import com.yh.parkingpartner.ui.SearchActivity;

import org.w3c.dom.Text;

import java.util.List;

public class AdapterParkList extends RecyclerView.Adapter<AdapterParkList.ViewHolder>{

    Context context;
    List<Data> parkList;

    public AdapterParkList(Context context, List<Data> parkList) {

    this.context = context;
    this.parkList = parkList;

    }


    @NonNull
    @Override
    public AdapterParkList.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.parklist_row, parent, false);
        return new AdapterParkList.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Data data = parkList.get(position);

        holder.txtTitle.setText(data.getPrk_plce_nm());
        holder.txtDistance.setText((int) data.getDistance()+"m" );
        holder.txtCharge.setText(data.getParking_chrge_bs_time()+"분당" + data.getParking_chrge_bs_chrg()+"원");
        holder.txtCount.setText("총 구획수 : " + data.getPrk_cmprt_co());


    }

    @Override
    public int getItemCount() {

        return parkList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitle;
        TextView txtDistance;
        TextView txtCharge;
        TextView txtCount;

        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDistance = itemView.findViewById(R.id.txtDistance);
            txtCharge = itemView.findViewById(R.id.txtCharge);
            txtCount = itemView.findViewById(R.id.txtCount);
            cardView = itemView.findViewById(R.id.cardView);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int index=getAdapterPosition();
                    Data data = parkList.get(index);
//                    setNotifyItemSelected(index);
                    //전달할 데이터를 담는다.
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("parklist",data);
                    ((ParkListActivity)context).setResult(Activity.RESULT_OK, intent);
                    //액티비티는 종료한다.
                    ((ParkListActivity)context).finish( );
                }
            });
        }
    }
}
