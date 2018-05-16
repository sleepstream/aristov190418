package com.sleepstream.checkkeeper.modules;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.*;
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
import java.io.IOException;
import java.util.Random;

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
    private RelativeLayout placeImageLayout;
    private Context context;
    private Double map_offset= 0.02;
    public static PhotoTask photoTask;

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

        Random rnd = new Random();
        backColor = Color.argb(40, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

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
            purchasesList.reloadPurchasesList();
            purchasesListAdapter.notifyDataSetChanged();
            if ((currentInvoice.store != null && currentInvoice.store._status == 0) || (currentInvoice.kktRegId != null && currentInvoice.kktRegId._status == 0)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getString(R.string.finde_store_on_map));
                builder.setNegativeButton(context.getString(R.string.btnCancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton(context.getString(R.string.btnOk), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PlacePicker.IntentBuilder builder = placeBuilder();
                        //Context context = getApplicationContext();
                        try {
                            getActivity().startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
                        } catch (Exception e) {
                            Log.d(LOG_TAG, e.getMessage() + "Error");
                            e.printStackTrace();
                        }
                    }
                });
                builder.show();

            }

            MainActivity.currentNumber.setText(String.valueOf(purchasesList.purchasesListData.size()));
            strore_adress.setText("");
            store_name.setText("");
            placeImage.setImageBitmap(null);
            View map = getView().findViewById(R.id.map);
            setPageData();


            map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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
                }
            });
            map.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
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
                            MainActivity.currentInvoice.store.name = Name.getText().toString();
                            MainActivity.currentInvoice.store.update = true;
                            invoice.updateInvoice(MainActivity.currentInvoice);
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

                    return true;
                }
            });
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
            else  if(currentInvoice.latitudeAdd >0 && currentInvoice.longitudeAdd >0)
            {builder.setLatLngBounds(new LatLngBounds(new LatLng( currentInvoice.latitudeAdd-map_offset,  currentInvoice.longitudeAdd-map_offset),
                    new LatLng(currentInvoice.latitudeAdd+map_offset, currentInvoice.longitudeAdd+map_offset)));}
            else
            {
                //find best location
                Double[] latLng = invoice.findBestLocation(currentInvoice.store);
                if(latLng.length==2 && latLng[0] != null && latLng[1] != null)
                {
                    builder.setLatLngBounds(new LatLngBounds(new LatLng(latLng[0]-map_offset, latLng[1]-map_offset), new LatLng(latLng[0]+map_offset, latLng[1]+map_offset)));
                }
                else if(currentInvoice.latitudeAdd != null && currentInvoice.longitudeAdd != null)
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
        currentNumber =  view.findViewById(R.id.currentNumber);
        store_name =  view.findViewById(R.id.strore_name);
        strore_adress = view.findViewById(R.id.strore_adress);
        placeImage = view.findViewById(R.id.placeImage);
        placeImageLayout = view.findViewById(R.id.placeImageLayout);
        placeImageLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showPopupMenuAccountingList(view);
                return true;
            }
        });

        setPageData();


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
        //currentName.setText(purchasesList.title);
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
        if(photoTask!= null)
            photoTask.cancel(true);
        googleFotoListAdapter = null;
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
            mItemTouchHelperPurList.startDrag(viewHolder);
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
                            case R.id.setPhoto:
                                photoTask = new PhotoTask(500, 500);
                                photoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentInvoice.store.place_id, currentInvoice.store.latitude.toString(), currentInvoice.store.longitude.toString());
                                break;
                            case R.id.removePhoto:
                                String filepath = Environment.getExternalStorageDirectory()+"/PriceKeeper/storeImage/";
                                File file = new File(filepath, "IMG_"+currentInvoice.store.place_id + ".png");
                                if(file.exists())
                                    file.delete();
                                else
                                {
                                    file = new File(filepath, "MAP_"+currentInvoice.store.place_id + ".png");
                                    if(file.exists())
                                        file.delete();
                                }
                                onResume();
                                break;
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

        Runnable runnableUndo = new Runnable() {

            @Override
            public void run() {
                popupMenu.dismiss();
            }
        };


        Handler handlerUndo=new Handler();
        handlerUndo.postDelayed(runnableUndo,2500);

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
            GooglePlace googlePlace = new GooglePlace(placeId, getContext());
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
