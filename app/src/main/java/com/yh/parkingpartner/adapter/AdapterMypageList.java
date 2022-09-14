package com.yh.parkingpartner.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yh.parkingpartner.R;
import com.yh.parkingpartner.model.Review;
import com.yh.parkingpartner.ui.MypageActivity;
import com.yh.parkingpartner.ui.ReviewEditActivity;
import com.yh.parkingpartner.ui.ReviewAddActivity;

import java.util.List;

public class AdapterMypageList extends RecyclerView.Adapter<AdapterMypageList.ViewHolder> {

    Context context;
    List<Review> reviewList;
    String[] time;

    public AdapterMypageList(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public AdapterMypageList.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mypagelist_row, parent, false);
        return new AdapterMypageList.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviewList.get(position);

        holder.txtPrkNm.setText(review.getPrk_plce_nm());
        holder.txtPrkAdres.setText(review.getPrk_plce_adres());
        holder.txtStart.setText("입차시간 : " + review.getStart_prk_at().replace("T", " ").substring(0, 16));
        holder.txtEnd.setText("출차시간 : " + review.getEnd_prk().replace("T", " ").substring(0, 16));

        if (review.getPrk_area() != null) {
            holder.txtPrkArea.setText("구역 : " + review.getPrk_area());
        } else {

            holder.txtPrkArea.setText("구역 : 정보 없음");
        }
        holder.txtPay.setText("요금 ( " + review.getParking_chrge_bs_time() + "분 / " + review.getParking_chrge_bs_chrg() + "원 )");
        time = review.getUse_prk_at().split(":");
        if (time[0].equals("0") ) {
            holder.txtUsePay.setText(":  "+time[1] + "분"+" / " + review.getEnd_pay() + "원");
        } else {
            if (review.getUse_prk_at().contains("day")) {
                holder.txtUsePay.setText(":  "+time[0] + "시간 " + time[1] + "분"+" / " + review.getEnd_pay() + "원");
            } else {
                holder.txtUsePay.setText(":  "+time[0] + "시간 " + time[1] + "분"+" / " + review.getEnd_pay() + "원");
            }
        }

        Glide.with(context).load(review.getImg_prk()).placeholder(R.drawable.ic_baseline_photo_camera_back_24).into(holder.imgPrk);

        if (review.getId() != 0) {
            holder.btnReview.setText("리뷰 수정");
            holder.ratingBar.setRating(review.getRating());
        } else {
            holder.btnReview.setText("리뷰 작성");
            holder.ratingBar.setRating(0);
        }
    }


    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtPrkNm;
        TextView txtPrkAdres;
        TextView txtStart;
        TextView txtEnd;
        TextView txtPrkArea;
        TextView txtPay;
        TextView txtUsePay;
        ImageView imgPrk;
        Button btnReview;
        RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtPrkNm = itemView.findViewById(R.id.txtPrkNm);
            txtPrkAdres = itemView.findViewById(R.id.txtPrkAdres);
            txtStart = itemView.findViewById(R.id.txtStart);
            txtEnd = itemView.findViewById(R.id.txtEnd);
            txtPrkArea = itemView.findViewById(R.id.txtPrkArea);
            txtPay = itemView.findViewById(R.id.txtPay);
            txtUsePay = itemView.findViewById(R.id.txtUsePay);
            imgPrk = itemView.findViewById(R.id.imgPrk);
            btnReview = itemView.findViewById(R.id.btnReview);
            ratingBar = itemView.findViewById(R.id.ratingBar);

            btnReview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (btnReview.getText().toString().equals("리뷰 수정")) {
                        int index = getAdapterPosition();
                        Review review = reviewList.get(index);

                        Intent intent = new Intent(context, ReviewEditActivity.class);
                        intent.putExtra("review", review);
                        ((MypageActivity)context).finish();
                        context.startActivity(intent);
                    } else if(btnReview.getText().toString().equals("리뷰 작성")) {
                        int index = getAdapterPosition();
                        Review review = reviewList.get(index);

                        Intent intent = new Intent(context, ReviewAddActivity.class);
                        intent.putExtra("prkId", review.getPrk_id());
                        intent.putExtra("prkNm", review.getPrk_plce_nm());
                        intent.putExtra("prkEnd", review.getEnd_prk());
                        ((MypageActivity)context).finish();
                        context.startActivity(intent);
                    }

                }
            });
        }
    }
}
