package com.sleepstream.checkkeeper.modules;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.sleepstream.checkkeeper.IOnBackPressed;
import com.sleepstream.checkkeeper.MainActivity;
import com.sleepstream.checkkeeper.Navigation;
import com.sleepstream.checkkeeper.R;
import com.sleepstream.checkkeeper.accountinglistObject.AccountingListData;
import com.sleepstream.checkkeeper.helper.SimpleItemTouchHelperCallback;
import com.sleepstream.checkkeeper.invoiceObjects.InvoiceData;
import com.sleepstream.checkkeeper.purchasesObjects.PurchasesListData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.sleepstream.checkkeeper.MainActivity.*;

public class PurchasesPageFragment extends Fragment implements PurchasesListAdapter.OnStartDragListener, IOnBackPressed {

    private static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    private static final String LOG_TAG = "PageFragment";
    public static int page_title;
    private Navigation navigation;

    private Integer pageNumber;
    public static GoogleFotoListAdapter googleFotoListAdapter;

    public static RecyclerView recyclerViewPurList;
    private RecyclerView products_category_list;
    private RecyclerView category_chooser_list;

    private ItemTouchHelper mItemTouchHelperPurList;
    private ItemTouchHelper mItemTouchHelperAccList;
    private ItemTouchHelper mItemTouchHelperCatList;

    private TextView currentName;
    private TextView currentDate ;
    private TextView store_name;
    private TextView strore_adress ;
    private TextView invoice_date_text;
    private TextView invoice_count_text;
    private TextView invoice_sum_text;
    private TextView account_name;

    private Product_category_chooser_adapter category_chooser_list_adapter;




    public static ImageView placeImage;
    private ImageView account_logo_image;

    private RelativeLayout storeData;
    public static RelativeLayout mainView;
    public static RelativeLayout button_select_category;
    public static RelativeLayout categories_choose;

    public static PurchasesListAdapter purchasesListAdapter;
    public Product_category_viewer_adapter product_category_viewer_adapter;
    private final int PLACE_PICKER_REQUEST = 3000;
    public static String subTitle="";
    private RelativeLayout placeImageLayout;
    private Context context;
    private Double map_offset= 0.02;
    public static PhotoTask photoTask;
    private RelativeLayout cardList_view;
    private RelativeLayout categories_choose_button;
    private GridLayoutManager products_category_adapterLLM;


    public PurchasesPageFragment(){}

    public static PurchasesPageFragment newInstance(int page) {
        PurchasesPageFragment purchasesPageFragment = new PurchasesPageFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        page_title =  R.string.purchasesPageTitle;
        arguments.putInt("currentName", R.string.purchasesPageTitle);
        purchasesPageFragment.setArguments(arguments);
        //MainActivity.fab.hide();

        return purchasesPageFragment;
    }

    public void PurchasesPageFragmentSet(Navigation navigation, Context context)
    {
        this.navigation = navigation;
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(settings.settings.containsKey("map_offset")) {
            map_offset = Double.valueOf(settings.settings.get("map_offset"));
        }
        try {
            pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
        }
        catch (Exception ex)
        {
            pageNumber = null;
        }

        if ((currentInvoice.store != null && currentInvoice.store._status != 1) || currentInvoice.store == null){ //|| (currentInvoice.kktRegId != null && currentInvoice.kktRegId._status == 0)) {
            final AlertDialog builder = new AlertDialog.Builder(context).create();
            builder.setTitle(context.getString(R.string.finde_store_on_map));
            builder.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.btnCancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    builder.cancel();
                }
            });
            builder.setButton(AlertDialog.BUTTON_POSITIVE,context.getString(R.string.btnOk), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    PlacePicker.IntentBuilder builderMap = placeBuilder();
                    //Context context = getApplicationContext();
                    try {
                        getActivity().startActivityForResult(builderMap.build(getActivity()), PLACE_PICKER_REQUEST);
                    } catch (Exception e) {
                        log.info(LOG_TAG + "\n"+ e.getMessage() + "\nError starting map");
                        e.printStackTrace();
                    }
                }
            });
            builder.setCancelable(false);
            builder.setCanceledOnTouchOutside(false);
            builder.show();

        }

    }
    private void setPageData()
    {
        if(currentInvoice!=null) {
            //currentDate.setText(currentInvoice.getDateInvoice(null));
            //currentName.setText(String.valueOf((float) Math.round(currentInvoice.getFullPrice() * 100) / 100 ));

            if(currentInvoice.store != null && currentInvoice.store.place_id != null) {
                String filepath = Environment.getExternalStorageDirectory() + "/PriceKeeper/storeImage/IMG_" + currentInvoice.store.place_id + ".png";
                File imgFile = new File(filepath);

                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    placeImage.setImageBitmap(myBitmap);
                } else {
                    filepath = Environment.getExternalStorageDirectory() + "/PriceKeeper/storeImage/MAP_" + currentInvoice.store.place_id + ".png";
                    imgFile = new File(filepath);

                    if (imgFile.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        placeImage.setImageBitmap(myBitmap);
                    }
                }
                //PhotoTaskSaver photoTask = new PhotoTaskSaver(500, 500);
                //photoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentInvoice.store.place_id, currentInvoice.store.latitude.toString(), currentInvoice.store.longitude.toString());
            }
            String[] date = currentInvoice.getDateInvoice(null).split(" ");
            invoice_date_text.setText(date[0]+"\n"+date[1]);
            invoice_count_text.setText(currentInvoice.quantity.toString());
            invoice_sum_text.setText(currentInvoice.getFullPrice().toString());


            if(accountingList!= null) {
                AccountingListData data = accountingList.getAccByFk(currentInvoice.getFk_invoice_accountinglist());
                if (data != null)
                    account_name.setText(data.getName());
                else
                {
                    account_name.setText(context.getString(R.string.accountingList_default));

                }
            }



            //currentNumber.setText(String.valueOf(currentInvoice.quantity == null? "0" : currentInvoice.quantity));
        }
        else {
            //currentName.setText("");
            placeImage.setImageBitmap(null);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        try {


            MainActivity.currentNumber.setText(String.valueOf(purchasesList.purchasesListData.size()));
            strore_adress.setText("");
            store_name.setText("");
            placeImage.setImageBitmap(null);
            setPageData();

            if (currentInvoice != null) {
                try {
                    if (currentInvoice.store.name == null || (currentInvoice.store.name != null && currentInvoice.store.name == "")) {
                        if (currentInvoice.store.name_from_fns != null && currentInvoice.store.name_from_fns != "")
                            store_name.setText(currentInvoice.store.name_from_fns);
                    } else if (currentInvoice.store.name != null && currentInvoice.store.name != "") {
                        store_name.setText(currentInvoice.store.name);
                    } else
                        store_name.setText("");


                    if (currentInvoice.store.address == null || (currentInvoice.store.address != null && currentInvoice.store.address == "")) {
                        if (currentInvoice.store.address_from_fns != null && currentInvoice.store.address_from_fns != "")
                            strore_adress.setText(currentInvoice.store.address_from_fns);
                    } else if (currentInvoice.store.address != null && currentInvoice.store.address != "") {
                        strore_adress.setText(currentInvoice.store.address);
                    } else {
                        strore_adress.setText("");
                    }
                } catch (Exception e) {
                    store_name.setText("");
                    strore_adress.setText("");

                    Log.d(LOG_TAG, e.getMessage() + " Error no store name/address");
                }
            }

            //change name of store
            //if(MainActivity.pageNow == "purchasesList")
            //   fab.hide();
        }
        catch (Exception ex)
        {
            log.info(LOG_TAG + "\n"+ ex.getMessage() + "\nError on resume");
            ex.printStackTrace();
        }
    }

    private PlacePicker.IntentBuilder placeBuilder()
    {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        if(currentInvoice.store != null) {
            if (currentInvoice.store.latitude > 0 && currentInvoice.store.longitude > 0)
                builder.setLatLngBounds(new LatLngBounds(new LatLng( currentInvoice.store.latitude-map_offset,  currentInvoice.store.longitude-map_offset),
                        new LatLng(currentInvoice.store.latitude+map_offset, currentInvoice.store.longitude+map_offset)));
            else
            {
                //find best location
                Double[] latLng = invoice.findBestLocation(currentInvoice.store);
                if(latLng.length==2 && latLng[0] != null && latLng[1] != null)
                {
                    builder.setLatLngBounds(new LatLngBounds(new LatLng(latLng[0]-map_offset, latLng[1]-map_offset), new LatLng(latLng[0]+map_offset, latLng[1]+map_offset)));
                }
                else if(currentInvoice.latitudeAdd != null && currentInvoice.latitudeAdd >0 && currentInvoice.longitudeAdd != null && currentInvoice.longitudeAdd > 0)
                {
                    builder.setLatLngBounds(new LatLngBounds(new LatLng(currentInvoice.latitudeAdd-map_offset,currentInvoice.longitudeAdd-map_offset),new LatLng(currentInvoice.latitudeAdd+map_offset,currentInvoice.longitudeAdd+map_offset)));
                }
            }


        }
        return builder;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fab.hide();
        View view = inflater.inflate(R.layout.content_purchases_page, null);
        final Context context = view.getContext();
        currentDate = view.findViewById(R.id.currentDate);
        //currentName = view.findViewById(R.id.currentName);
        //currentNumber =  view.findViewById(R.id.currentNumber);
        store_name =  view.findViewById(R.id.strore_name);
        strore_adress = view.findViewById(R.id.strore_adress);
        placeImage = view.findViewById(R.id.placeImage);
        storeData = view.findViewById(R.id.storeData);
        storeData.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showPopupMenuAccountingList(view);
                return true;
            }
        });

        account_name = view.findViewById(R.id.account_name);
        account_logo_image = view.findViewById(R.id.account_logo_image);
        invoice_date_text = view.findViewById(R.id.invoice_date_text);
        invoice_count_text = view.findViewById(R.id.invoice_count_text);
        invoice_sum_text = view.findViewById(R.id.invoice_sum_text);
        mainView = view.findViewById(R.id.mainView);
        button_select_category = view.findViewById(R.id.button_select_category);
        categories_choose = view.findViewById(R.id.categories_choose);


        button_select_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                category_chooser_list_adapter.notifyDataSetChanged();
                Animation animation = AnimationUtils.loadAnimation(context,  R.anim.translate);
                categories_choose.setVisibility(View.VISIBLE);
                categories_choose.startAnimation(animation);
                animation.setAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        categories_choose.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                        categories_choose.setVisibility(View.VISIBLE);
                        cardList_view.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        categories_choose.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        categories_choose_button = view.findViewById(R.id.categories_choose_button);

        setPageData();


        recyclerViewPurList =  view.findViewById(R.id.cardList);
        cardList_view = view.findViewById(R.id.cardList_view);;
        products_category_list = view.findViewById(R.id.products_category_list);
        category_chooser_list = view.findViewById(R.id.category_chooser_list);


        category_chooser_list.setHasFixedSize(true);
        category_chooser_list_adapter = new Product_category_chooser_adapter(context, purchasesList, currentInvoice, view);
        final GridLayoutManager category_chooser_list_adapterGLM = new GridLayoutManager(context,4);
        category_chooser_list_adapterGLM.setOrientation(LinearLayoutManager.VERTICAL);
        category_chooser_list.setLayoutManager(category_chooser_list_adapterGLM);
        category_chooser_list.setAdapter(category_chooser_list_adapter);
        ItemTouchHelper.Callback callbackCatList = new SimpleItemTouchHelperCallback(purchasesListAdapter, context);
        mItemTouchHelperCatList = new ItemTouchHelper(callbackCatList);
        mItemTouchHelperCatList.attachToRecyclerView(category_chooser_list);


        recyclerViewPurList.setHasFixedSize(true);
        final LinearLayoutManager purchasesListLLM = new LinearLayoutManager(context);
        purchasesListLLM.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewPurList.setLayoutManager(purchasesListLLM);

        purchasesListAdapter = new PurchasesListAdapter(context, this, currentNumber, purchasesList, view);
        recyclerViewPurList.setAdapter(purchasesListAdapter);
        ItemTouchHelper.Callback callbackPurList = new SimpleItemTouchHelperCallback(purchasesListAdapter, context);
        mItemTouchHelperPurList = new ItemTouchHelper(callbackPurList);
        mItemTouchHelperPurList.attachToRecyclerView(recyclerViewPurList);
        //currentName.setText(purchasesList.title);
        purchasesListAdapter.notifyDataSetChanged();



        products_category_list.setHasFixedSize(true);
        product_category_viewer_adapter = new Product_category_viewer_adapter(context, purchasesList, currentInvoice, view, this);
        //final GridLayoutManager products_category_adapterLLM;

        products_category_adapterLLM = new GridLayoutManager(context,getColumnsCount(product_category_viewer_adapter.categoriesSelected.size()));

        products_category_adapterLLM.setOrientation(LinearLayoutManager.VERTICAL);
        products_category_list.setLayoutManager(products_category_adapterLLM);
        products_category_list.setAdapter(product_category_viewer_adapter);
        final ItemTouchHelper.Callback callbackAccList = new SimpleItemTouchHelperCallback(purchasesListAdapter, context);
        mItemTouchHelperAccList = new ItemTouchHelper(callbackAccList);
        mItemTouchHelperAccList.attachToRecyclerView(products_category_list);


        assert recyclerViewFotoList != null;
        recyclerViewFotoList.setHasFixedSize(true);
        final LinearLayoutManager llmFoto = new LinearLayoutManager(context);
        llmFoto.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewFotoList.setLayoutManager(llmFoto);
        googleFotoListAdapter = new GoogleFotoListAdapter(context);
        recyclerViewFotoList.setAdapter(googleFotoListAdapter);


        categories_choose_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(category_chooser_list_adapter!= null)
                {
                    if(category_chooser_list_adapter.categoriesAll.size()>0 && category_chooser_list_adapter.category_selected != null)
                    {
                        for(Integer position : purchasesListAdapter.selectedItems)
                        {
                            PurchasesListData purchasesListData = purchasesList.purchasesListData.get(position);
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("fk_product_category_data", category_chooser_list_adapter.category_selected);
                            contentValues.put("fk_product_category_products", purchasesListData.product.id);
                            Integer count = dbHelper.update("product_category", contentValues, "fk_product_category_products=?", new String[]{purchasesListData.product.id.toString()});
                            if(count < 1)
                            {
                                dbHelper.insert("product_category", null, contentValues);
                            }
                        }
                        purchasesList.reloadPurchasesList(null);
                        product_category_viewer_adapter.reloadCategoryProduct();
                        product_category_viewer_adapter.notifyDataSetChanged();
                        purchasesListAdapter.selectedItems.clear();
                        purchasesList.reloadPurchasesList(purchasesList.lastShowedCategory);

                        //number of products in current category show in number field
                        currentNumber.setText(String.valueOf(purchasesList.purchasesListData.size()));
                        purchasesListAdapter.notifyDataSetChanged();
                        onBackPressed();
                    }
                }
            }
        });

        return view;
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

    @Override
    public void onDestroyView() {

        super.onDestroyView();
        if(photoTask!= null)
            photoTask.cancel(true);
        googleFotoListAdapter = null;
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
            mItemTouchHelperPurList.startDrag(viewHolder);
    }

    @Override
    public boolean onBackPressed() {
        if(mainView.getVisibility() == View.VISIBLE && categories_choose.getVisibility() != View.VISIBLE) {
            purchasesListAdapter.selectedItems.clear();
            mainView.setVisibility(View.GONE);
            products_category_adapterLLM.setSpanCount(getColumnsCount(product_category_viewer_adapter.categoriesSelected.size()));
            return true;
        }
        else if(categories_choose.getVisibility() == View.VISIBLE)
        {
            category_chooser_list_adapter.category_selected = null;
            Animation animation = AnimationUtils.loadAnimation(context,  R.anim.translate);
            categories_choose.startAnimation(animation);
            animation.setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    cardList_view.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    categories_choose.setVisibility(View.GONE);
                    cardList_view.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    categories_choose.setVisibility(View.VISIBLE);
                }
            });
            return true;
        }
        return false;
    }


    class PhotoTaskSaver extends AsyncTask<String, Void, PhotoTaskSaver.AttributedPhoto> {

        private int mHeight;

        private int mWidth;

        public PhotoTaskSaver(int width, int height) {
            mHeight = height;
            mWidth = width;
        }

       @Override
       protected void onPostExecute(AttributedPhoto attributedPhoto) {
            if(attributedPhoto.image != null)
                placeImage.setImageBitmap(attributedPhoto.image);
            else if(attributedPhoto.map != null)
                placeImage.setImageBitmap(attributedPhoto.map);
           super.onPostExecute(attributedPhoto);
       }
       @Override
       protected PhotoTaskSaver.AttributedPhoto doInBackground(String... params) {

           Log.d(LOG_TAG, "trying to get place_id image ");
           if (params.length != 3) {
               return null;
           }
           final String placeId = params[0];
           PhotoTaskSaver.AttributedPhoto attributedPhoto = null;
           String filepath = Environment.getExternalStorageDirectory()+"/PriceKeeper/storeImage/";
           File file = new File(filepath, "IMG_"+placeId + ".png");
           Bitmap image = null;
           Bitmap mapImage = null;
           if(!file.exists()) {

               PlacePhotoMetadataResult result = Places.GeoDataApi
                       .getPlacePhotos(mGoogleApiClient, placeId).await();


               if (result.getStatus().isSuccess()) {
                   PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
                   if (photoMetadataBuffer.getCount() > 0 && !isCancelled()) {
                       // Get the first bitmap and its attributions.
                       PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
                       // Load a scaled bitmap for this photo.
                       image = photo.getScaledPhoto(mGoogleApiClient, mWidth, mHeight).await()
                               .getBitmap();

                   }
                   photoMetadataBuffer.release();
               }
           }
           file = new File(filepath, "MAP_"+placeId + ".png");
           if(!file.exists()) {
               String URL = "http://maps.google.com/maps/api/staticmap?center=" + params[1] + "," + params[2] + "&zoom=17&size=500x500&markers=" + params[1] + "," + params[2] + "&sensor=false";

               OkHttpClient httpclient = new OkHttpClient();
               okhttp3.Request request = new Request.Builder()
                       .url(URL)
                       .build();

               Response in = null;
               try {
                   in = httpclient.newCall(request).execute();
                   mapImage = BitmapFactory.decodeStream(in.body().byteStream());
                   in.close();
               } catch (Exception ex) {
                   Log.d(LOG_TAG, "ERROR to save place_id MAP image folder \n" + ex.getMessage());
                   ex.printStackTrace();
               }
           }
           try {
               Log.d(LOG_TAG, "trying to save place_id image folder " + filepath);
               File dir = new File(filepath);

               if (!dir.exists()) dir.mkdirs();

               if (image != null) {
                   file = new File(filepath, "IMG_" + placeId + ".png");
                   FileOutputStream fOut = new FileOutputStream(file);

                   image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                   fOut.flush();
                   fOut.close();
               }
               if (mapImage != null) {
                   file = new File(filepath, "MAP_" + placeId + ".png");
                   FileOutputStream fOut = new FileOutputStream(file);

                   mapImage.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                   fOut.flush();
                   fOut.close();
               }
               attributedPhoto = new PhotoTaskSaver.AttributedPhoto(image, mapImage);
           } catch (Exception ex) {
               attributedPhoto = new PhotoTaskSaver.AttributedPhoto(image, mapImage);
               Log.d(LOG_TAG, "ERROR to save place_id image folder \n" + ex.getMessage());
               ex.printStackTrace();
           }
           // Release the PlacePhotoMetadataBuffer.
           return attributedPhoto;
       }

        /**
         * Holder for an image and its attribution.
         */
        class AttributedPhoto {


            public final Bitmap image;
            public final Bitmap map;

            public AttributedPhoto(Bitmap image, Bitmap map) {
                this.image = image;
                this.map = map;
            }
        }
    }

    private void showPopupMenuAccountingList(final View view) {
        final PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.inflate(R.menu.storesettingsmenu);

        popupMenu
                .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(final MenuItem item) {
                        switch(item.getItemId())
                        {
                            case R.id.setPhoto: {
                                photoTask = new PhotoTask(500, 500);
                                photoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentInvoice.store.place_id, currentInvoice.store.latitude.toString(), currentInvoice.store.longitude.toString());
                                break;
                            }
                            case R.id.removePhoto: {
                                String filepath = Environment.getExternalStorageDirectory() + "/PriceKeeper/storeImage/";
                                File file = new File(filepath, "IMG_" + currentInvoice.store.place_id + ".png");
                                if (file.exists())
                                    file.delete();
                                else {
                                    file = new File(filepath, "MAP_" + currentInvoice.store.place_id + ".png");
                                    if (file.exists())
                                        file.delete();
                                }
                                onResume();
                                break;
                            }
                            case R.id.setName: {
                                blurPlotter.setVisibility(View.VISIBLE);

                                View v = LayoutInflater.from(getActivity()).inflate(R.layout.edit_store_information_layout, null);
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setView(v);
                                final EditText Name = v.findViewById(R.id.name);
                                //final EditText storeType = v.findViewById(R.id.storeType);

                                Name.setText(MainActivity.currentInvoice.store.name);
                                //storeType.setText(MainActivity.currentInvoice.store.store_type);

                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        blurPlotter.setVisibility(View.GONE);
                                        MainActivity.currentInvoice.store.name = Name.getText().toString().trim();
                                        MainActivity.currentInvoice.store.update = true;
                                        invoice.setStoreData(MainActivity.currentInvoice);
                                        onResume();
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        blurPlotter.setVisibility(View.GONE);
                                        dialog.cancel();
                                    }
                                });
                                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialogInterface) {
                                        blurPlotter.setVisibility(View.GONE);
                                    }
                                });
                                builder.show();
                                break;
                            }
                            case R.id.setOnMap: {
                                PlacePicker.IntentBuilder builder = placeBuilder();
                                //Context context = getApplicationContext();
                                try {
                                    getActivity().startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
                                } catch (GooglePlayServicesRepairableException e) {
                                    Log.d(LOG_TAG, e.getMessage() + "Error");
                                    e.printStackTrace();
                                } catch (GooglePlayServicesNotAvailableException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                        }

                        /*
                        if(item.getItemId() == R.id.setPhoto)
                        {
                            Intent chooseImageIntent = ImagePicker.getPickImageIntent(getActivity());
                            getActivity().startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
                        }
                        */
                        return false;
                    }
                });
        popupMenu.show();


        /*Runnable runnableUndo = new Runnable() {

            @Override
            public void run() {
                popupMenu.dismiss();
            }
        };


        Handler handlerUndo=new Handler();
        handlerUndo.postDelayed(runnableUndo,2500);
        */

    }
    public class PhotoTask extends AsyncTask<String, String, Void> {

        private int mHeight;
        private int mWidth;
        private String filepath;
        private File file;
        private InvoiceData invoiceData;

        public PhotoTask(int width, int height) {
            mHeight = height;
            mWidth = width;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            onResume();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            filepath = Environment.getExternalStorageDirectory()+"/PriceKeeper/storeImage/";
            file = new File(filepath, "IMG_"+currentInvoice.store.place_id + ".png");
            try {
                this.invoiceData = (InvoiceData) currentInvoice.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            if(googleFotoListAdapter != null) {
                addMyPhotoContainer.setVisibility(View.VISIBLE);
                blurPlotter.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                //recyclerViewFotoList.bringToFront();
            }
            //googleFotoListAdapter.placePhotoMetadataList.add(getUrlToResource(context, R.drawable.loading_icon));
            //googleFotoListAdapter.notifyDataSetChanged();
        }
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if(googleFotoListAdapter != null) {
                progressBar.setVisibility(View.GONE);
                googleFotoListAdapter.placePhotoMetadataList.add(values[1]);
                googleFotoListAdapter.photoData.put(values[1], values[0]);
                googleFotoListAdapter.notifyDataSetChanged();
            }
        }

        /**
         * Loads the first photo for a place id from the Geo Data API.
         * The place id must be the first (and only) parameter.
         */
        @Override
        protected Void doInBackground(String... params) {
            if (params.length != 3) {
                return null;
            }

            final String placeId = params[0];
            GooglePlace googlePlace = new GooglePlace(placeId, context);
                Bitmap image = null;
                if (googlePlace.photos != null) {
                    for (int count = 0; count < googlePlace.photos.size(); count++) {
                        GooglePlace.Photo photo = googlePlace.photos.get(count);
                        File file = new File(cacheDir, "IMG_" + placeId + "_" + count + ".png");
                        if (!file.exists()) {
                            saveImages(googlePlace.loadImage(photo.photo_reference), placeId, count);
                            file = new File(cacheDir, "IMG_" + placeId + "_" + count + ".png");
                        }
                        publishProgress(file.getPath(), photo.photo_reference);//cacheDir + "IMG_" + placeId + "_" + count + ".png");
                    }

                }
            return null;
        }

    }

    private void saveImages(Bitmap image, String place_id, int count)
    {
        FileOutputStream fOut= null;

        try {
            if(image!= null) {
                //log.info(LOG_TAG+"\n"+ "trying to save place_id image folder " + cacheDir +"IMG_"+place_id +"_"+count +".png");
                File file = new File(cacheDir, "IMG_"+place_id + "_"+count +".png");
                fOut = new FileOutputStream(file);
                image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            }
        }
        catch (Exception ex)
        {
            //log.info(LOG_TAG+"\n"+ "ERROR to save place_id image folder \n"+ex.getMessage());
            ex.printStackTrace();
        }
        finally
        {
            try {
                if (fOut != null) {
                    fOut.flush();
                    fOut.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
