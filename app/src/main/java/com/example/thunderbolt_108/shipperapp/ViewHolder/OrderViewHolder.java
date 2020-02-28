package com.example.thunderbolt_108.shipperapp.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.thunderbolt_108.shipperapp.R;

public class OrderViewHolder extends RecyclerView.ViewHolder{

    public TextView txtOrderId,txtOrderStatus,txtOrderPhone,txtOrderAddress,txtOrderDate,txtName,txtPaymentMode,txtPaymentState,txtTotal;
    public Button btnShipping;


    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);
        txtOrderAddress=(TextView)itemView.findViewById(R.id.order_address);
        txtOrderId=(TextView)itemView.findViewById(R.id.order_id);
        txtOrderStatus=(TextView)itemView.findViewById(R.id.order_status);
        txtOrderPhone=(TextView)itemView.findViewById(R.id.order_phone);
        txtOrderDate=(TextView)itemView.findViewById(R.id.order_date);
        txtName=(TextView)itemView.findViewById(R.id.userName);
        txtPaymentMode=(TextView)itemView.findViewById(R.id.paymentMethod);
        txtPaymentState=(TextView)itemView.findViewById(R.id.paymentState);
        txtTotal=(TextView)itemView.findViewById(R.id.totalAmount);

        btnShipping=(Button)itemView.findViewById(R.id.btnShipping);


    }

}
