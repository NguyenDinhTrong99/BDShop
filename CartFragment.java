package dnd.dongocduc.appdt3shop.screen.cart;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dnd.dongocduc.appdt3shop.R;
import dnd.dongocduc.appdt3shop.data.model.Cart;
import dnd.dongocduc.appdt3shop.data.model.CartShop;
import dnd.dongocduc.appdt3shop.retrofit2.APIUtils;
import dnd.dongocduc.appdt3shop.retrofit2.DataClient;
import dnd.dongocduc.appdt3shop.screen.BaseFragment;
import dnd.dongocduc.appdt3shop.screen.cart.adapter.CartAdapter;
import dnd.dongocduc.appdt3shop.utils.GetSumPriceListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static dnd.dongocduc.appdt3shop.utils.Common.formatPrice;
import static dnd.dongocduc.appdt3shop.utils.Navigator.backToScreen;

public class CartFragment extends BaseFragment implements GetDataFromServerListener, View.OnClickListener, GetSumPriceListener {
    private static final String TAG = CartFragment.class.getSimpleName();
    private static final String KEY_EXTRA_ID_USER = "CART_FRAGMENT";
    private static final String KEY_EXTRA_ROOT_TAG = "ROOT_TAG_CART";
    private static String sRootTag;
    private LinearLayout linearLayoutProgressCart;
    private ConstraintLayout constraintLayoutBuy;
    private Toolbar mToolbar;
    private List<String> nameShop = new ArrayList<>();
    private List<String> tmpNameShop = new ArrayList<>();
    private RecyclerView mRecyclerViewCart;
    private GetDataFromServerListener mListener;
    private CartAdapter mAdapter;
    private ArrayList<CartShop> mShops = new ArrayList<>();
    private CheckBox checkBoxSelectAll;
    private TextView textViewSum;
    private Button buttonBuy;
    private double sum = 0;
    private Map<String, Double> sumPrice = new LinkedHashMap<>();

    public static CartFragment newInstance(int id_user, String rootTag) {
        CartFragment fragment = new CartFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_EXTRA_ID_USER, id_user);
        bundle.putString(KEY_EXTRA_ROOT_TAG, rootTag);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initData();
        initListener();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (menu.findItem(R.id.menuCart) != null)
            menu.findItem(R.id.menuCart).setVisible(false);
        if (menu.findItem(R.id.menuMessage) != null)
            menu.findItem(R.id.menuMessage).setVisible(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.checkboxSelectAllCart:
                CartAdapter.OnSelectedAllListener onSelectedListener = mAdapter.getOnSelectedAllListener();
                if (onSelectedListener != null) {
                    if (checkBoxSelectAll.isChecked()) {
                        onSelectedListener.onSelectedAll();
                    } else {
                        onSelectedListener.onUnSelectAll();
                    }
                }
                break;
            case R.id.buttonBuyCart:
                Toast.makeText(getContext(), "Buy", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void initView(View v) {
        sRootTag = Objects.requireNonNull(getArguments()).getString(KEY_EXTRA_ROOT_TAG);
        constraintLayoutBuy = v.findViewById(R.id.constraintLayoutBottomCart);
        linearLayoutProgressCart = v.findViewById(R.id.linearLayoutProgressCart);
        checkBoxSelectAll = v.findViewById(R.id.checkboxSelectAllCart);
        textViewSum = v.findViewById(R.id.textViewSumCart);
        buttonBuy = v.findViewById(R.id.buttonBuyCart);
        mRecyclerViewCart = v.findViewById(R.id.recyclerViewCart);
        mAdapter = new CartAdapter();
        mRecyclerViewCart.setHasFixedSize(true);
        mRecyclerViewCart.setAdapter(mAdapter);
        mToolbar = v.findViewById(R.id.toolbarCart);
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        }
        checkBoxSelectAll.setChecked(true);
        mListener = this;
        mAdapter.setListener(this);
    }

    @Override
    protected void initData() {
        int id_user = Objects.requireNonNull(getArguments()).getInt(KEY_EXTRA_ID_USER);
        if (id_user > 0) {
            getListCart(id_user);
        }
    }

    @Override
    protected void initListener() {
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sRootTag.equals("")) {
                    Fragment fragment = Objects.requireNonNull(getFragmentManager()).findFragmentByTag(sRootTag);
                    if (fragment != null) {
                        backToScreen(Objects.requireNonNull(getFragmentManager()), CartFragment.this, sRootTag);
                    } else {
                        getFragmentManager().beginTransaction().remove(CartFragment.this).commit();
                    }
                }

            }
        });
        buttonBuy.setOnClickListener(this);
        checkBoxSelectAll.setOnClickListener(this);
    }

    @Override
    public void getDataSucceed(List<Cart> data) {
        showHideProgressBar(false);
        handleDataCart(data);
    }

    @Override
    public void getDataFailed(Throwable e) {
        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        showHideProgressBar(true);
    }

    private void getListCart(int id_user) {
        DataClient dataClient = APIUtils.getData();
        Call<List<Cart>> callback = dataClient.getCart(id_user);
        callback.enqueue(new Callback<List<Cart>>() {
            @Override
            public void onResponse(@Nullable Call<List<Cart>> call, @Nullable Response<List<Cart>> response) {
                try {
                    if (Objects.requireNonNull(response).isSuccessful()) {
                        if (Objects.requireNonNull(response.body()).size() > 0) {
                            mListener.getDataSucceed(response.body());
                        }
                    }
                } catch (NullPointerException e) {
                    // Lỗi này người dùng back về khiến dữ liệu load xong thì mất đi Context
                    Log.e(TAG, "Error", e);
                }
            }

            @Override
            public void onFailure(@Nullable Call<List<Cart>> call, @Nullable Throwable t) {
                mListener.getDataFailed(t);
            }
        });
    }

    @Override
    public void onGetSumPriceItemListener(String key, double price) {
        double tmpSum = 0;
        sumPrice.put(key, price);
        if (price > 0) {
            for (Map.Entry<String, Double> entry : sumPrice.entrySet()) {
                tmpSum += entry.getValue();
            }
            sum = tmpSum;
        } else {
            sum += price;
            if (sum <= 0) {
                sum = 0;
                sumPrice.clear();
            }
        }
        textViewSum.setText(formatPrice(sum));
    }

    private void showHideProgressBar(boolean isShow) {
        if (isShow) {
            linearLayoutProgressCart.setVisibility(View.VISIBLE);
            mRecyclerViewCart.setVisibility(View.GONE);
            constraintLayoutBuy.setVisibility(View.GONE);
        } else {
            linearLayoutProgressCart.setVisibility(View.GONE);
            mRecyclerViewCart.setVisibility(View.VISIBLE);
            constraintLayoutBuy.setVisibility(View.VISIBLE);
        }
    }

    private void handleDataCart(List<Cart> data) {
        for (Cart c : data) {
            tmpNameShop.add(c.getNameShop());
        }
        for (String s : tmpNameShop) {
            if (!nameShop.contains(s)) {
                nameShop.add(s);
            }
        }
        Map<String, List<Cart>> maps = new HashMap<>();
        for (String s : nameShop) {
            List<Cart> tmp = new ArrayList<>();
            for (Cart c : data) {
                if (s.equals(c.getNameShop())) {
                    tmp.add(c);
                }
            }
            maps.put(s, tmp);
        }
        for (Map.Entry<String, List<Cart>> entry : maps.entrySet()) {
            CartShop cs = new CartShop.CartShopBuilder()
                    .setNameShop(entry.getKey())
                    .setCarts(entry.getValue()).build();
            mShops.add(cs);
            mAdapter.updateData(mShops);
        }
    }
}

interface GetDataFromServerListener {
    void getDataSucceed(List<Cart> data);

    void getDataFailed(Throwable e);
}
