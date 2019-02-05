package com.sleepstream.checkkeeper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.GeoApiContext;
import com.sleepstream.checkkeeper.MainActivity;
import com.sleepstream.checkkeeper.Navigation;
import com.sleepstream.checkkeeper.R;
import com.sleepstream.checkkeeper.invoiceObjects.InvoiceData;
import com.sleepstream.checkkeeper.modules.PlaceChooserAdapter;
import com.sleepstream.checkkeeper.modules.WrapContentLinearLayoutManager;
import com.squareup.timessquare.CalendarPickerView;

import java.util.*;

import static com.sleepstream.checkkeeper.MainActivity.*;

public class PlaceChooserActivity extends AppCompatActivity {

    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    private static final String LOG_TAG = "PlaceChooserActivity";
    public static String page_title;

    int pageNumber;
    int backColor;
    private CalendarPickerView calendar;
    public static RecyclerView recyclerViewInvList;
    public static LinearLayout mainView;
    public ImageView ivFilter;
    public FastScroller fastScroller;
    private ItemTouchHelper mItemTouchHelperInvList;
    private Context context;
    private boolean statusDateFilter = false;
    private List<InvoiceData.Store_on_map> stores_on_map = new ArrayList<>();
    //public String pageNow= "accountingLists";

    public static PlaceChooserAdapter placeChooserAdapter;
    public static WrapContentLinearLayoutManager llm;
    private Navigation navigation;

    private Double map_offset= 0.02;
    private Button button_find_on_map;


    public PlaceChooserActivity(){}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(settings.settings.containsKey("theme"))
        {
            int theme = Integer.valueOf(settings.settings.get("theme"));
            setTheme(theme);
        }
        super.onCreate(savedInstanceState);

        context = this;
        setContentView(R.layout.content_place_chooser_page);
        FirebaseFirestore.setLoggingEnabled(true);


        mainView = findViewById(R.id.mainView);
        button_find_on_map = findViewById(R.id.button_find_on_map);


        button_find_on_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builderMap = placeBuilder();
                try {
                    startActivityForResult(builderMap.build((Activity)context), PLACE_PICKER_REQUEST);

                } catch (Exception e) {
                    log.info(LOG_TAG + "\n"+ e.getMessage() + "\nError starting map");
                    e.printStackTrace();
                }
            }
        });


        recyclerViewInvList = findViewById(R.id.bestPlacesList);
        fastScroller = findViewById(R.id.fastscroll);

        assert recyclerViewInvList != null;
        recyclerViewInvList.setHasFixedSize(true);

        llm = new WrapContentLinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewInvList.setLayoutManager(llm);

        placeChooserAdapter = new PlaceChooserAdapter(context,  stores_on_map);
        recyclerViewInvList.setAdapter(placeChooserAdapter);
    }



    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, placeChooserAdapter.row_index+"");
        unHideFABMenu();
        /*if(invoice.lastIDCollection > 0) {
            llm.smoothScrollToPosition(recyclerViewInvList, null, invoice.lastIDCollection);
        }*/
        new ReloadNewAsyncTask().execute(currentInvoice);



    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(resultCode, data);
        finish();
    }

    public void PlaceChooserFragmentSet(Navigation navigation) {
        this.navigation = navigation;
    }


    private class ReloadNewAsyncTask extends AsyncTask<InvoiceData, Void, Void>
    {

        String distanceUrl ="https://maps.googleapis.com/maps/api/distancematrix/json?units=";
        String units = "metric";

        private  final String API_KEY = getString(R.string.google_maps_key);
        private  final GeoApiContext context = new GeoApiContext.Builder().apiKey(API_KEY).build();


        @Override
        protected void onPreExecute() {

            if(stores_on_map != null)
                stores_on_map.clear();
            placeChooserAdapter.notifyDataSetChanged();
            super.onPreExecute();
        }



        @Override
        protected void onPostExecute(Void aVoid) {
            placeChooserAdapter.stores_on_map_list = stores_on_map;
            placeChooserAdapter.notifyDataSetChanged();
            super.onPostExecute(aVoid);
        }


        @Override
        protected Void doInBackground(InvoiceData... invoiceData) {
            stores_on_map = invoice.findBestLocation(invoiceData[0]);
            for(InvoiceData.Store_on_map item : stores_on_map)
            {
                if(item.longitude != null && item.latitude != null)
                {
                    Double distance = null;
                    if(currentInvoice.store_on_map!= null && currentInvoice.store_on_map.latitude != null && currentInvoice.store_on_map.longitude!= null) {
                        distance= distance(currentInvoice.store_on_map.latitude, item.latitude, currentInvoice.store_on_map.longitude, item.longitude, 1, 1);
                    }
                    else if (currentInvoice.latitudeAdd!= null && currentInvoice.longitudeAdd!= null)
                    {
                        distance =  distance(currentInvoice.latitudeAdd, item.latitude, currentInvoice.longitudeAdd, item.longitude, 1, 1);
                    }
                    if(distance != null)
                        item.distance = distance.longValue();


                }
            }
            Map<Integer, Long> position = new TreeMap<>();
            List<InvoiceData.Store_on_map> sorted = new ArrayList<>();
            List<Long>tmpDis = new ArrayList<>();
            for(InvoiceData.Store_on_map item : stores_on_map)
            {
                position.put(stores_on_map.indexOf(item), item.distance);
                tmpDis.add(item.distance);
            }
            Collections.sort(tmpDis);
            for(Long item : tmpDis)
            {
                Integer key = null;
                for(Map.Entry<Integer, Long> entry : position.entrySet())
                {
                    if(entry.getValue().equals(item))
                    {
                        key = entry.getKey();
                        break;
                    }
                }
                if (key != null)
                    sorted.add(stores_on_map.get(key));
            }
            if(sorted.size() == stores_on_map.size())
                stores_on_map = sorted;


            return null;
        }


    }



    private PlacePicker.IntentBuilder placeBuilder()
    {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        if(currentInvoice.store_from_fns != null) {
            //find best location
            Double[] latLng = invoice.findBestLocation(currentInvoice.store_from_fns);
            if(latLng != null && latLng.length==2 && latLng[0] != null && latLng[1] != null)
            {
                builder.setLatLngBounds(new LatLngBounds(new LatLng(latLng[0]-map_offset, latLng[1]-map_offset), new LatLng(latLng[0]+map_offset, latLng[1]+map_offset)));
            }
            else if (currentInvoice.store_on_map!= null && currentInvoice.store_on_map.latitude > 0 && currentInvoice.store_on_map.longitude > 0)
                builder.setLatLngBounds(new LatLngBounds(new LatLng( currentInvoice.store_on_map.latitude-map_offset,  currentInvoice.store_on_map.longitude-map_offset),
                        new LatLng(currentInvoice.store_on_map.latitude+map_offset, currentInvoice.store_on_map.longitude+map_offset)));
            else if(currentInvoice.latitudeAdd != null && currentInvoice.latitudeAdd >0 && currentInvoice.longitudeAdd != null && currentInvoice.longitudeAdd > 0)
            {
                builder.setLatLngBounds(new LatLngBounds(new LatLng(currentInvoice.latitudeAdd-map_offset,currentInvoice.longitudeAdd-map_offset),new LatLng(currentInvoice.latitudeAdd+map_offset,currentInvoice.longitudeAdd+map_offset)));
            }
        }
        else if(currentInvoice.latitudeAdd != null && currentInvoice.latitudeAdd >0 && currentInvoice.longitudeAdd != null && currentInvoice.longitudeAdd > 0)
        {
            builder.setLatLngBounds(new LatLngBounds(new LatLng(currentInvoice.latitudeAdd-map_offset,currentInvoice.longitudeAdd-map_offset),new LatLng(currentInvoice.latitudeAdd+map_offset,currentInvoice.longitudeAdd+map_offset)));
        }
        return builder;
    }

    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }
}
