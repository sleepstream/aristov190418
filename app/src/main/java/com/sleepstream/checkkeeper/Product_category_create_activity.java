package com.sleepstream.checkkeeper;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import com.sleepstream.checkkeeper.R;
import com.sleepstream.checkkeeper.helper.SimpleItemTouchHelperCallback;
import com.sleepstream.checkkeeper.modules.Product_category_icons_viewer_adapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sleepstream.checkkeeper.MainActivity.*;
import static com.sleepstream.checkkeeper.invoiceObjects.Invoice.tableNameProduct_category_data;

public class Product_category_create_activity extends AppCompatActivity {
    private Context context = this;
    private List<Integer> icons = new ArrayList<>();
    private RecyclerView product_category_list;
    private GridLayoutManager products_category_adapterLLM;
    private ItemTouchHelper mItemTouchHelperCatList;
    private EditText category_name;
    private ImageView done_button;
    private ImageView cancel_action;
    private TextInputLayout category;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.product_category_icons);

        if(settings.settings.containsKey("theme"))
        {
            int theme = Integer.valueOf(settings.settings.get("theme"));
            setTheme(theme);
        }
        product_category_list = findViewById(R.id.category_icons_chooser_list);
        category_name = findViewById(R.id.category_name);

        category = findViewById(R.id.category);

        cancel_action = findViewById(R.id.cancel_action);
        cancel_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED, null);
                finish();
            }
        });
        done_button = findViewById(R.id.done_button);


        //category_name.clearFocus();


       /*category_name.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                return false;
            }
        });*/
        Field[] drawables = R.drawable.class.getFields();
        for (Field f : drawables)
        {
            try {
                if(f.getName().contains("product_category") && f.getName().contains("_48") && !f.getName().contains("default")) {
                    icons.add(f.getInt(null));
                    System.out.println("R.drawable." + f.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        category_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                {
                    if(category_name.getText().length()==0)
                    {
                        category.setError(getString(R.string.requested_field_error));
                        category.setErrorEnabled(true);
                    }
                    else if(category_name.getText().length()>20)
                    {
                        category.setError(getString(R.string.requested_field_length_error)+" 20 символов");
                        category.setErrorEnabled(true);
                    }
                    else if(!category_name.getText().toString().matches("(?i)[a-zа-я0-9  ]{0,20}"))
                    {
                        category.setError(getString(R.string.requested_field_data_error));
                        category.setErrorEnabled(true);
                    }
                }
                else
                {
                    category.setError(null);
                    category.setErrorEnabled(false);
                }
            }
        });


        final Product_category_icons_viewer_adapter product_category_icons_viewer_adapter = new Product_category_icons_viewer_adapter(context, icons);

        product_category_list.setHasFixedSize(true);
        products_category_adapterLLM = new GridLayoutManager(context,getColumnsCount(product_category_icons_viewer_adapter.categories.size()));


        products_category_adapterLLM.setOrientation(LinearLayoutManager.VERTICAL);
        product_category_list.setLayoutManager(products_category_adapterLLM);
        product_category_list.setAdapter(product_category_icons_viewer_adapter);
        final ItemTouchHelper.Callback callbackAccList = new SimpleItemTouchHelperCallback(product_category_icons_viewer_adapter, context);
        mItemTouchHelperCatList = new ItemTouchHelper(callbackAccList);
        mItemTouchHelperCatList.attachToRecyclerView(product_category_list);


        done_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!category.isErrorEnabled() && product_category_icons_viewer_adapter.iconName != null) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("category", category_name.getText().toString().trim());
                    contentValues.put("icon_name", product_category_icons_viewer_adapter.iconName);
                    dbHelper.insert(tableNameProduct_category_data, null, contentValues);
                    setResult(RESULT_OK, null);
                    finish();
                }
                else
                {
                    Toast.makeText(context, getString(R.string.error_fill_form), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private int getColumnsCount(int count)
    {
        if(count>3)
        {
            return 3;
        }
        else if(count>2)
        {
            return 3;
        }
        else if(count>1)
        {
            return 2;
        }
        else
        {
            return 1;
        }
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        //category_name.clearFocus();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }
}
