package com.sleepstream.checkkeeper.modules;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.sleepstream.checkkeeper.MainActivity;
import com.sleepstream.checkkeeper.Navigation;
import com.sleepstream.checkkeeper.R;
import com.sleepstream.checkkeeper.helper.SimpleItemTouchHelperCallback;
import com.sleepstream.checkkeeper.invoiceObjects.InvoiceData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;

import static android.view.View.GONE;
import static com.sleepstream.checkkeeper.MainActivity.*;

public class PurchasesPageFragment extends Fragment implements PurchasesListAdapter.OnStartDragListener{

    private static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    private static final String LOG_TAG = "PageFragment";
    public static int page_title;
    private Navigation navigation;

    private Integer pageNumber;
    private int backColor;
    public static GoogleFotoListAdapter googleFotoListAdapter;

    private RecyclerView recyclerViewPurList;

    private ItemTouchHelper mItemTouchHelperPurList;
    private TextView currentName;
    private TextView currentNumber;
    private TextView currentDate ;
    private TextView store_name;
    private TextView strore_adress ;
    public static ImageView placeImage;
    private RelativeLayout settingsMenu;

    public static PurchasesListAdapter purchasesListAdapter;
    private final int PLACE_PICKER_REQUEST = 3000;
    public static String subTitle="";

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

    public void PurchasesPageFragmentSet(Navigation navigation)
    {
        this.navigation = navigation;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
        }
        catch (Exception ex)
        {
            pageNumber = null;
        }

        Random rnd = new Random();
        backColor = Color.argb(40, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

    }
    private void setPageData()
    {
        currentNumber.setText("0");
        if(currentInvoice!=null) {
            currentDate.setText(currentInvoice.getDateInvoice(null));
            currentName.setText(String.valueOf((float) Math.round(currentInvoice.getFullPrice() * 100) / 100 ));

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
                //PhotoTask photoTask = new PhotoTask(500, 500);
                //photoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentInvoice.store.place_id, currentInvoice.store.latitude.toString(), currentInvoice.store.longitude.toString());
            }
            currentNumber.setText(String.valueOf(currentInvoice.quantity == null? "0" : currentInvoice.quantity));
        }
        else {
            currentName.setText("");
            placeImage.setImageBitmap(null);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        purchasesList.reloadPurchasesList();
        purchasesListAdapter.notifyDataSetChanged();

        MainActivity.currentNumber.setText(String.valueOf(purchasesList.purchasesListData.size()));
        strore_adress.setText("");
        store_name.setText("");
        placeImage.setImageBitmap(null);
        View map=  getView().findViewById(R.id.map);
        setPageData();


        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                if(currentInvoice.store != null) {
                    if (currentInvoice.store.latitude > 0 && currentInvoice.store.longitude > 0)
                        builder.setLatLngBounds(new LatLngBounds(new LatLng( currentInvoice.store.latitude,  currentInvoice.store.longitude),
                                new LatLng(currentInvoice.store.latitude, currentInvoice.store.longitude)));


                }

                //Context context = getApplicationContext();
                try {
                    getActivity().startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    Log.d(LOG_TAG, e.getMessage() + "Error");
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
        map.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                blurPlotter.setVisibility(View.VISIBLE);

                View v=LayoutInflater.from(getContext()).inflate(R.layout.edit_store_information_layout, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setView(v);
                final EditText Name = v.findViewById(R.id.name);
                final EditText storeType = v.findViewById(R.id.storeType);

                Name.setText(MainActivity.currentInvoice.store.name);
                storeType.setText(MainActivity.currentInvoice.store.store_type);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        blurPlotter.setVisibility(GONE);
                        MainActivity.currentInvoice.store.name = Name.getText().toString();
                        MainActivity.currentInvoice.store.update = true;
                        invoice.updateInvoice(MainActivity.currentInvoice);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        blurPlotter.setVisibility(GONE);
                        dialog.cancel();
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        blurPlotter.setVisibility(GONE);
                    }
                });
                builder.show();

                return true;
            }
        });
        if(currentInvoice != null) {
            try {
                if(currentInvoice.store.name == null || (currentInvoice.store.name != null && currentInvoice.store.name == "")) {
                    if (currentInvoice.store.name_from_fns != null && currentInvoice.store.name_from_fns != "")
                        store_name.setText(currentInvoice.store.name_from_fns);
                }
                else if(currentInvoice.store.name != null && currentInvoice.store.name != "") {
                    store_name.setText(currentInvoice.store.name);
                }
                else
                    store_name.setText("");


                if(currentInvoice.store.adress == null || (currentInvoice.store.adress != null && currentInvoice.store.adress == ""))
                {
                    if(currentInvoice.store.address_from_fns != null && currentInvoice.store.address_from_fns != "")
                        strore_adress.setText(currentInvoice.store.address_from_fns);
                }
                else if(currentInvoice.store.adress != null && currentInvoice.store.adress != "")
                {
                    strore_adress.setText(currentInvoice.store.adress);
                }
                else
                {
                    strore_adress.setText("");
                }
            }
            catch (Exception e)
            {
                store_name.setText("");
                strore_adress.setText("");

                Log.d(LOG_TAG, e.getMessage() + " Error no store name/adress");
            }
        }

        //change name of store
        //if(MainActivity.pageNow == "purchasesList")
         //   fab.hide();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fab.hide();
        View view = inflater.inflate(R.layout.content_purchases_page, null);
        final Context context = view.getContext();
        currentDate = view.findViewById(R.id.currentDate);
        currentName = view.findViewById(R.id.currentName);
        currentNumber =  view.findViewById(R.id.currentNumber);
        store_name =  view.findViewById(R.id.strore_name);
        strore_adress = view.findViewById(R.id.strore_adress);
        placeImage = view.findViewById(R.id.placeImage);
        settingsMenu = view.findViewById(R.id.settingsMenu);
        //slidingUpPanelLayout = (SlidingUpPanelLayout) view.findViewById(R.id.sliding_layout);
        setPageData();

        //slidingUpPanelLayout.setTouchEnabled(false);
/*
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {
            }

            @Override
            public void onPanelStateChanged(View view, SlidingUpPanelLayout.PanelState panelState, SlidingUpPanelLayout.PanelState panelState1) {
                if (panelState.equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                    Toast.makeText(context, panelState + " page1 " + currentName.getText().toString(), Toast.LENGTH_LONG).show();
                    currentName.setText(context.getString(R.string.purchasesListTitle));
                    currentNumber.setText(purchasesList.purchasesListData.size() + "");
                    //MainActivity.pageNow = "purchasesList";
                    purchasesListAdapter.notifyDataSetChanged();
                }

                if (panelState.equals(SlidingUpPanelLayout.PanelState.COLLAPSED) && currentName.getText().equals(context.getString(R.string.purchasesListTitle))) {

                    if (!purchasesList.checkFilter()) {
                        purchasesList.clearFilter();
                        purchasesList.reloadPurchasesList();
                    }
                    //MainActivity.pageNow = "onePurchaseLists";
                    currentName.setText(R.string.purchasListTitle);
                    //currentNumber.setText(purchase.invoices.size() + "");

                    purchasesListAdapter.notifyDataSetChanged();
                }

            }
        });
*/


        settingsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenuAccountingList(view);
            }
        });
        recyclerViewPurList =  view.findViewById(R.id.cardList);
        assert recyclerViewPurList != null;
        recyclerViewPurList.setHasFixedSize(true);
        final LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewPurList.setLayoutManager(llm);


        purchasesListAdapter = new PurchasesListAdapter(context, this, currentNumber, purchasesList, view);
        recyclerViewPurList.setAdapter(purchasesListAdapter);
        ItemTouchHelper.Callback callbackPurList = new SimpleItemTouchHelperCallback(purchasesListAdapter, context);
        mItemTouchHelperPurList = new ItemTouchHelper(callbackPurList);
        mItemTouchHelperPurList.attachToRecyclerView(recyclerViewPurList);
        currentName.setText(purchasesList.title);
        purchasesListAdapter.notifyDataSetChanged();

        //if(MainActivity.pageNow == "purchasesList")
        //fab.hide();


        assert recyclerViewFotoList != null;
        recyclerViewFotoList.setHasFixedSize(true);
        final LinearLayoutManager llmFoto = new LinearLayoutManager(context);
        llmFoto.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewFotoList.setLayoutManager(llmFoto);
        googleFotoListAdapter = new GoogleFotoListAdapter(context);
        recyclerViewFotoList.setAdapter(googleFotoListAdapter);



        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
            mItemTouchHelperPurList.startDrag(viewHolder);
    }




    class PhotoTask extends AsyncTask<String, Void, PurchasesPageFragment.PhotoTask.AttributedPhoto> {

        private int mHeight;

        private int mWidth;

        public PhotoTask(int width, int height) {
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
       protected PurchasesPageFragment.PhotoTask.AttributedPhoto doInBackground(String... params) {

           Log.d(LOG_TAG, "trying to get place_id image ");
           if (params.length != 3) {
               return null;
           }
           final String placeId = params[0];
           PurchasesPageFragment.PhotoTask.AttributedPhoto attributedPhoto = null;
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
               attributedPhoto = new PurchasesPageFragment.PhotoTask.AttributedPhoto(image, mapImage);
           } catch (Exception ex) {
               attributedPhoto = new PurchasesPageFragment.PhotoTask.AttributedPhoto(image, mapImage);
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
                        if(item.getItemId() == R.id.setPhoto)
                        {
                            Intent chooseImageIntent = ImagePicker.getPickImageIntent(getContext());
                            getActivity().startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
                        }
                        return false;
                    }
                });
        popupMenu.show();

        Runnable runnableUndo = new Runnable() {

            @Override
            public void run() {
                popupMenu.dismiss();
            }
        };


        Handler handlerUndo=new Handler();
        handlerUndo.postDelayed(runnableUndo,2500);

    }
}
