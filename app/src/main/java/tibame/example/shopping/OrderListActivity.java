package tibame.example.shopping;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OrderListActivity extends AppCompatActivity {

    Firebase firebase;
    OrderListAdapter orderListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        ListView listView = (ListView) findViewById(R.id.listView);
        orderListAdapter = new OrderListAdapter();
        listView.setAdapter(orderListAdapter);

        String currentUserUid = getIntent().getStringExtra("userUid");
        String mode = getIntent().getStringExtra("mode");

        firebase = new Firebase(Config.FIRE_BASE_URL);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ObjectMapper objectMapper = new ObjectMapper();

                List<Order> orders = new ArrayList<>();

                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {

                    Order order = objectMapper.convertValue(orderSnapshot.getValue(), Order.class);
                    order.setKey(orderSnapshot.getKey());
                    orders.add(order);
                }

                orderListAdapter.setOrders(orders);
                orderListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };


        Firebase orders = firebase.child("orders");
        if (mode.equals("buying")) {
            orders.orderByChild("buyerUserUid")
                    .equalTo(currentUserUid)
                    .addValueEventListener(listener);
        } else if (mode.equals("selling")) {
            orders.orderByChild("item/userUid")
                    .equalTo(currentUserUid)
                    .addValueEventListener(listener);
        }


    }

    class OrderListAdapter extends BaseAdapter {

        List<Order> orders = new ArrayList<>();

        public void setOrders(List<Order> orders) {
            this.orders = orders;
        }

        @Override
        public int getCount() {
            return orders.size();
        }

        @Override
        public Object getItem(int position) {
            return orders.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.listitem_order, null);
            }


            final Order order = (Order) getItem(position);
            Item item = order.getItem();


            TextView textViewItemName = (TextView) convertView.findViewById(R.id.textViewItemName);
            TextView textViewItemPrice = (TextView) convertView.findViewById(R.id.textViewItemPrice);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.imageViewItemPicture);

            textViewItemName.setText(item.getName());
            textViewItemPrice.setText(String.valueOf(item.getPrice()));

            Picasso.with(OrderListActivity.this).load(new File(getCacheDir(), item.getKey())).into(imageView);

            String currentUserUid = getIntent().getStringExtra("userUid");

            TextView textViewOrderStatus = (TextView) convertView.findViewById(R.id.textViewOrderStatus);
            if (order.getStatus() == Order.STATUS_PROCESSING) {
                textViewOrderStatus.setText("訂單處理中");
            }else if (order.getStatus() == Order.STATUS_CANCEL) {
                textViewOrderStatus.setText("取消");
            }else if (order.getStatus() == Order.STATUS_SHIPPING) {
                textViewOrderStatus.setText("已出貨");
            }


            Button buttonOrderCancel = (Button) convertView.findViewById(R.id.buttonOrderCancel);
            Button buttonOrderAction = (Button) convertView.findViewById(R.id.buttonOrderAction);

            View.OnClickListener cancelListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    firebase.child("orders")
                            .child(order.getKey())
                            .child("status")
                            .setValue(Order.STATUS_CANCEL);
                }
            };

            if (currentUserUid.equals(order.getBuyerUserUid())) {

                if (order.getStatus() == Order.STATUS_PROCESSING) {
                    buttonOrderCancel.setOnClickListener(cancelListener);
                } else {
                    buttonOrderCancel.setVisibility(View.GONE);
                }

                buttonOrderAction.setVisibility(View.GONE);

            } else {

                if (order.getStatus() == Order.STATUS_PROCESSING) {

                    buttonOrderCancel.setOnClickListener(cancelListener);

                    buttonOrderAction.setVisibility(View.VISIBLE);
                    buttonOrderAction.setText("出貨");
                    buttonOrderAction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            firebase.child("orders")
                                    .child(order.getKey())
                                    .child("status")
                                    .setValue(Order.STATUS_SHIPPING);
                        }
                    });

                } else {
                    buttonOrderCancel.setVisibility(View.GONE);
                    buttonOrderAction.setVisibility(View.GONE);
                }

            }


            return convertView;
        }
    }
}
