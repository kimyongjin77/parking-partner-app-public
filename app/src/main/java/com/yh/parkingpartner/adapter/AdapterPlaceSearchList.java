package com.yh.parkingpartner.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.yh.parkingpartner.model.Place;
import com.yh.parkingpartner.R;
import com.yh.parkingpartner.ui.SearchActivity;

import java.util.List;

public class AdapterPlaceSearchList extends RecyclerView.Adapter<AdapterPlaceSearchList.ViewHolder> {

    //멤버변수와 생성자를 만든다.
    Context context;    //소속 액티비티
    List<Place> dataList;  //표시할 데이터

    int oldPosition=-1;
    int nowPosition=-1;

    public AdapterPlaceSearchList(Context context, List<Place> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.placesearchlist_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Place data = dataList.get(position);
        holder.txtName.setText(data.getName());
        holder.txtAddr.setText(data.getFormatted_address());

        if (nowPosition == position){
            //holder.cardView.setCardBackgroundColor(Color.YELLOW);
            holder.cardView.setCardBackgroundColor(Color.parseColor("#C1F9C3"));
        } else{
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public  class  ViewHolder extends  RecyclerView.ViewHolder {

        TextView txtName;
        TextView txtAddr;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName=itemView.findViewById(R.id.txtName);
            txtAddr=itemView.findViewById(R.id.txtAddr);
            cardView=itemView.findViewById(R.id.cardView);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int index=getAdapterPosition();
//                    setNotifyItemSelected(index);
                    //전달할 데이터를 담는다.
                    Intent intent=new Intent();
                    intent.putExtra("destination", dataList.get(index));
                    ((SearchActivity)context).setResult(Activity.RESULT_OK, intent);
                    //액티비티는 종료한다.
                    ((SearchActivity)context).finish( );
                }
            });
        }

        void  setNotifyItemSelected(int index){
            oldPosition=nowPosition;
            nowPosition=index;
            if(oldPosition > -1){
                notifyItemChanged(oldPosition);
            }
            if(nowPosition > -1){
                notifyItemChanged(nowPosition);
            }
        }
    }
}
