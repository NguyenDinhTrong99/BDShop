package dnd.dongocduc.appdt3shop.screen.cart.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dnd.dongocduc.appdt3shop.R;
import dnd.dongocduc.appdt3shop.data.model.Cart;
import dnd.dongocduc.appdt3shop.retrofit2.APIUtils;
import dnd.dongocduc.appdt3shop.retrofit2.DataClient;
import dnd.dongocduc.appdt3shop.utils.GetSumPriceListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static dnd.dongocduc.appdt3shop.utils.Common.formatPrice;
import static dnd.dongocduc.appdt3shop.utils.Common.hideKeybroad;
import static dnd.dongocduc.appdt3shop.utils.Common.loadImage;
import static dnd.dongocduc.appdt3shop.utils.Common.sUser;

public class CartChildAdapter extends RecyclerView.Adapter<CartChildAdapter.ViewHolder> {
    private static final String TAG = CartChildAdapter.class.getSimpleName();
    private static final String TEXT_NONE = "None";
    private List<Cart> mList;
    private GetSumPriceListener mListener;
    /**
     * static?
     * */
    private static boolean isSelectedAll = true;

    CartChildAdapter(List<Cart> list) {
        this.mList = handleData(list);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_child, parent, false), mList, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (!isSelectedAll){
            holder.checkBoxChoose.setChecked(false);
        }else {
            holder.checkBoxChoose.setChecked(true);
        }
        holder.bindView();
        holder.initListener();
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    void setListener(GetSumPriceListener listener) {
        this.mListener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, TextView.OnEditorActionListener {
        private static final String TEXT_NONE = "None";
        private static final int X = 2, Y = 10;
        private static ArrayMap<Integer, Integer> counts = new ArrayMap<>();
        private List<Cart> mList;
        private CheckBox checkBoxChoose;
        private ImageView imageViewAvatarProduct;
        private ImageButton imageButtonSub, imageButtonAdd;
        private EditText editTextCount;
        private TextView textViewName, textViewType, textViewPrice;
        private GetSumPriceListener mListener;
        private int count;
        private double price;

        ViewHolder(@NonNull View itemView, List<Cart> list, GetSumPriceListener listener) {
            super(itemView);
            mList = list;
            mListener = listener;
            checkBoxChoose = itemView.findViewById(R.id.checkBoxSelectProductItemCart);
            imageViewAvatarProduct = itemView.findViewById(R.id.imageViewProductItemCart);
            imageButtonSub = itemView.findViewById(R.id.imageViewSubItemCart);
            imageButtonAdd = itemView.findViewById(R.id.imageViewAddItemCart);
            editTextCount = itemView.findViewById(R.id.editTextCountItemCart);
            textViewName = itemView.findViewById(R.id.textViewNameProductItemCart);
            textViewType = itemView.findViewById(R.id.textViewTypeItemCart);
            textViewPrice = itemView.findViewById(R.id.textViewPriceItemCart);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            params.setMargins(X, Y, X, Y);
        }

        void bindView() {
            Cart cart = mList.get(getAdapterPosition());
            textViewName.setText(cart.getName());
            if(counts != null){
                for(){
                    editTextCount.setText(String.valueOf(cart.getCount()));
                    textViewPrice.setText(formatPrice(cart.getSumPrice()));
                }
            }
            setClassify(cart, textViewType);
            loadImage(itemView.getContext(), cart.getImageUrl(), imageViewAvatarProduct, true);
            count = mList.get(getAdapterPosition()).getCount();
            price = cart.getPrice() * count;
            setPrice(cart.getName(), price);
        }

        private void initListener() {
            imageButtonAdd.setOnClickListener(this);
            imageButtonSub.setOnClickListener(this);
            editTextCount.setOnEditorActionListener(this);
            checkBoxChoose.setOnClickListener(this);
        }

        @SuppressLint("SetTextI18n")
        private void setClassify(Cart cart, TextView textViewType) {
            if (!cart.getColor().equalsIgnoreCase(TEXT_NONE) && cart.getSize().equalsIgnoreCase(TEXT_NONE)) {
                textViewType.setText("Classify: " + cart.getColor());
            } else if (cart.getColor().equalsIgnoreCase(TEXT_NONE) && !cart.getSize().equalsIgnoreCase(TEXT_NONE)) {
                textViewType.setText("Classify: " + cart.getSize());
            } else if (!cart.getColor().equalsIgnoreCase(TEXT_NONE) && !cart.getSize().equalsIgnoreCase(TEXT_NONE)) {
                textViewType.setText("Classify: " + cart.getColor() + ", " + cart.getSize());
            } else {
                textViewType.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Cart cart = mList.get(adapterPosition);
            switch (v.getId()) {
                case R.id.imageViewSubItemCart:
                    count--;
                    if (count < 0) {
                        count = 0;
                    }
                    editTextCount.setText(String.valueOf(count));
                    price = count * cart.getPrice();
                    saveCount(getAdapterPosition(), count);
                    textViewPrice.setText(formatPrice(price));
                    sendPriceToContainer(cart.getName(), price);
                    break;
                case R.id.imageViewAddItemCart:
                    count++;
                    editTextCount.setText(String.valueOf(count));
                    price = count * cart.getPrice();
                    saveCount(getAdapterPosition(), count);
                    textViewPrice.setText(formatPrice(price));
                    sendPriceToContainer(cart.getName(), price);
                    break;
                case R.id.checkBoxSelectProductItemCart:
                    price = count * cart.getPrice();
                    setPrice(cart.getName(), price);
                    break;
            }
        }

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                try {
                    Cart cart = mList.get(getAdapterPosition());
                    String tmp = editTextCount.getText().toString();
                    int count = 0;
                    if (tmp.equals("")) {
                        price = this.count * cart.getPrice();
                        editTextCount.setText(String.valueOf(count));
                        sendPriceToContainer(cart.getName(), price);
                    } else {
                        this.count = Integer.parseInt(tmp);
                        saveCount(getAdapterPosition(), this.count);
                        editTextCount.setText(String.valueOf(this.count));
                        price = this.count * cart.getPrice();
                        textViewPrice.setText(formatPrice(price));
                        sendPriceToContainer(cart.getName(), price);
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(itemView.getContext(), itemView.getContext().getResources()
                            .getString(R.string.toast_out_size_count), Toast.LENGTH_LONG).show();
                }
            }
            hideKeybroad(itemView.getContext());
            return true;
        }

        private void setPrice(String name, double sum) {
            if (mListener != null) {
                if (checkBoxChoose.isChecked()) {
                    mListener.onGetSumPriceItemListener(name, sum);
                } else {
                    mListener.onGetSumPriceItemListener(name, -sum);
                }
            }
        }

        private void sendPriceToContainer(String name, double price) {
            if (checkBoxChoose.isChecked()) {
                setPrice(name, price);
            }
        }

        private void saveCount(int position, int count){
            counts.put(position, count);
        }

    }

    void selectAll() {
        isSelectedAll = true;
        notifyDataSetChanged();
    }

    void unSelectAll() {
        isSelectedAll = false;
        notifyDataSetChanged();
    }

    private List<Cart> handleData(List<Cart> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = 1; j < list.size(); j++) {
                if (list.get(i).getName().equals(list.get(j).getName())) {
                    int count = list.get(i).getCount() + list.get(j).getCount();
                    double sum = list.get(i).getPrice() * count;
                    String size = list.get(j).getSize();
                    if (!list.get(i).getSize().equals(TEXT_NONE)) {
                        size = size + ";" + list.get(i).getSize();
                    }
                    String color = list.get(j).getColor();
                    if (!list.get(i).getColor().equals(TEXT_NONE)) {
                        color = color + ";" + list.get(i).getColor();
                    }
                    Cart tmpCart = new Cart.CartBuilder()
                            .setIdUser(sUser.getIdUser())
                            .setIdProduct(list.get(j).getIdUser())
                            .setName(list.get(j).getName())
                            .setCount(count)
                            .setPrice(list.get(j).getPrice())
                            .setSize(size)
                            .setColor(color)
                            .setImageUrl(list.get(j).getImageUrl())
                            .setSumPrice(sum)
                            .build();
                    list.add(tmpCart);
                    addCart(tmpCart);
                    removeCart(list.get(i).getId());
                    removeCart(list.get(j).getId());
                    list.remove(list.get(j));
                    list.remove(list.get(i));
                }
            }
        }
        return list;
    }

    private void removeCart(int id_cart) {
        DataClient dataClient = APIUtils.getData();
        Call<String> callback = dataClient.removeCart(id_cart);
        callback.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@Nullable Call<String> call, @Nullable Response<String> response) {
                if (Objects.requireNonNull(response).isSuccessful()) {
                    if (Objects.equals(response.body(), "Success")) {
                        Log.d(TAG, "Remove cart success");
                    }
                }
            }

            @Override
            public void onFailure(@Nullable Call<String> call, @Nullable Throwable t) {
                Log.e(TAG, "Error remove cart", t);
            }
        });
    }

    private void addCart(Cart c) {
        DataClient dataClient = APIUtils.getData();
        Call<String> callback = dataClient.addProductToCart(c.getIdUser(), c.getIdProduct(), c.getName(),
                c.getColor(), c.getSize(), c.getPrice(), c.getSumPrice(), c.getImageUrl(), c.getCount());
        callback.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@Nullable Call<String> call, @Nullable Response<String> response) {
                if (Objects.requireNonNull(response).isSuccessful()) {
                    if (response.body() != null) {
                        if (response.body().equals("Success")) {
                            Log.d(TAG, "Add cart Success");
                        }
                    }
                }
            }

            @Override
            public void onFailure(@Nullable Call<String> call, @Nullable Throwable t) {
                Log.e(TAG, "EROROR:\t", t);
            }
        });
    }
}
