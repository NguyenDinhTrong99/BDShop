package dnd.dongocduc.appdt3shop.screen.cart.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dnd.dongocduc.appdt3shop.R;
import dnd.dongocduc.appdt3shop.data.model.CartShop;
import dnd.dongocduc.appdt3shop.utils.GetSumPriceListener;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> implements GetSumPriceListener {
    private static final String TAG = CartAdapter.class.getSimpleName();
    private ArrayList<CartShop> mList = new ArrayList<>();
    private GetSumPriceListener mListener;
    private CartChildAdapter mAdapter;

    public CartAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_cart, parent, false), mList, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindView();
        setCartChildAdapter(holder.getCartChildAdapter());
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    @Override
    public void onGetSumPriceItemListener(String key, double price) {
        mListener.onGetSumPriceItemListener(key, price);
    }

    public void setListener(GetSumPriceListener listener) {
        this.mListener = listener;
    }

    private void setCartChildAdapter(CartChildAdapter adapter) {
        mAdapter = adapter;
    }

    public CartChildAdapter getAdapter() {
        return mAdapter;
    }

    public OnSelectedAllListener getOnSelectedAllListener() {
        return new OnSelectedAllListener() {
            @Override
            public void onSelectedAll() {
                if (mAdapter != null) {
                    mAdapter.setSelectAll(true);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onUnSelectAll() {
                if (mAdapter != null) {
                    mAdapter.setSelectAll(false);
                    notifyDataSetChanged();
                }
            }
        };
    }

    public void updateData(List<CartShop> data) {
        if (data != null) {
            mList.clear();
            mList.addAll(data);
            notifyDataSetChanged();
        }
    }

    public interface OnSelectedAllListener {
        void onSelectedAll();

        void onUnSelectAll();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ArrayList<CartShop> mList;
        private TextView textViewNameShop;
        private RecyclerView recyclerViewCartChild;
        private GetSumPriceListener mListener;
        private CartChildAdapter mAdapter;

        ViewHolder(@NonNull View itemView, ArrayList<CartShop> list, GetSumPriceListener listener) {
            super(itemView);
            mList = list;
            mListener = listener;
            textViewNameShop = itemView.findViewById(R.id.textViewNameShopCart);
            recyclerViewCartChild = itemView.findViewById(R.id.recyclerViewCartChild);
            RecyclerView.RecycledViewPool pool = new RecyclerView.RecycledViewPool();
            recyclerViewCartChild.setRecycledViewPool(pool);
            recyclerViewCartChild.setHasFixedSize(true);
        }

        void bindView() {
            mAdapter = new CartChildAdapter(mList.get(getAdapterPosition()).getCarts());
            textViewNameShop.setText(mList.get(getAdapterPosition()).getNameShop());
            recyclerViewCartChild.setAdapter(mAdapter);
            if (mListener != null) {
                mAdapter.setListener(mListener);
            }
        }

        CartChildAdapter getCartChildAdapter() {
            return mAdapter;
        }
    }
}

