package com.sleepstream.checkkeeper;


import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.*;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.AnyRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.text.InputType;
import android.text.Spanned;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.sleepstream.checkkeeper.DB.DBHelper;
import com.sleepstream.checkkeeper.accountinglistObject.AccountingList;
import com.sleepstream.checkkeeper.accountinglistObject.AccountingListData;
import com.sleepstream.checkkeeper.crop.CropActivity;
import com.sleepstream.checkkeeper.invoiceObjects.Invoice;
import com.sleepstream.checkkeeper.invoiceObjects.InvoiceData;
import com.sleepstream.checkkeeper.linkedListObjects.LinkedListClass;
import com.sleepstream.checkkeeper.modules.*;
import com.sleepstream.checkkeeper.purchasesObjects.PurchasesList;
import com.sleepstream.checkkeeper.qrmanager.QRManager;
import com.sleepstream.checkkeeper.smsListener.SmsListener;
import com.sleepstream.checkkeeper.smsListener.SmsReceiver;
import com.sleepstream.checkkeeper.userModule.PersonalData;
import com.sleepstream.checkkeeper.userModule.UserDataActivity;
import com.takusemba.cropme.CropView;
import okhttp3.*;
import org.ghost4j.document.PDFDocument;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sleepstream.checkkeeper.modules.PurchasesPageFragment.googleFotoListAdapter;


public class MainActivity extends AppCompatActivity implements InvoiceListAdapter.OnStartDragListener, AccountingListAdapter.OnStartDragListener, GoogleApiClient.OnConnectionFailedListener, NavigationView.OnNavigationItemSelectedListener {

    static final int PAGE_COUNT = 5;
    private static final int PURCHASE_PAGE = 3;
    private static final float LOCATION_REFRESH_DISTANCE = (float) 100.0;
    private static final long LOCATION_REFRESH_TIME = 10000;
    public static Intent intentService;
    public static CustomViewPager pager;
    public static InvoiceData currentInvoice;
    private InvoiceData finalInvoiceData;
    //public static MyFragmentPagerAdapter pagerAdapter;
    public static TabLayout tabLayout;

    static final String LOG_TAG = "MainActivity";
    static Context context;
    // UI

    private QRManager QRitem;
    private GetFnsData getFnsData;
    public String android_id;

    public static GoogleApiClient mGoogleApiClient;
    private LocationManager locationManager;
    public static String cacheDir;
    public static String pageNow = "accountingLists";

    public static DBHelper dbHelper;
    private static final String cameraPerm = Manifest.permission.CAMERA;
    private static final String smsPerm = Manifest.permission.RECEIVE_SMS;
    private static final String sdPerm = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String MapPerm = Manifest.permission.ACCESS_FINE_LOCATION;
    String[] permisions = new String[]{smsPerm, cameraPerm, sdPerm, MapPerm};
    private String resultQR;
    public static PersonalData user;
    private boolean permChecked = false;

    private final int cameraRequest = 1000;
    private final int personalDataShow = 2001;
    private final int personalDataRequestFull = 2000;
    private final int PLACE_PICKER_REQUEST = 3000;
    public static final int CALENDAR_PICKER_REQUEST = 4000;
    public static final int PICK_IMAGE_ID = 234;
    public static final int SetImageFromGoogle_REQUEST = 5000;
    public static final int PICK_INVOICE_FROM_IMAGE = 6000;
    public static final int CAPTURE_FROM_PDF_QR = 7000;
    public static final int restoreFromBackUp = 8000;

    boolean hasCameraPermission = false;
    boolean hasSmsPermission = false;
    boolean hasSDPermission = false;
    private boolean hasGPSPermission = false;

    public static Invoice invoice;
    public static AccountingList accountingList;
    public static PurchasesList purchasesList;
    public static LinkedListClass linkedListClass;
    public AccountingListAdapter accountingListAdapter;

    private ItemTouchHelper mItemTouchHelperInvList;
    private ItemTouchHelper mItemTouchHelperAccList;
    public static RecyclerView recyclerViewFotoList;
    public static RelativeLayout blurPlotter;
    public static RelativeLayout progressBar;
    public static RelativeLayout addMyPhoto;
    public static RelativeLayout addMyPhotoContainer;

    public TextView toolbar_title;

    public static Logger log = Logger.getLogger(MainActivity.class.getName());

    private int nu = 0;

    public static FloatingActionButton fab;

    public static List<Integer> pageBack = new LinkedList<Integer>();
    private boolean isActive = false;

    private ImageView ivFilter;


    private android.app.FragmentTransaction fTrans;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private boolean statusDateFilter;
    public static ArrayList<? extends Date> filterDates;
    public static Map<String, String[]> filterParam = new LinkedHashMap<>();
    private Integer currentPageNumber = null;
    public static TextView currentNumber;
    public Navigation navigation;
    public TextView invoiceCount;
    public TextView accountingListCount;
    public TextView linkedListCount;
    public TextView invoicesLoadingPage;

    public Map<String, String[]> statusInvoices = new LinkedHashMap<>();
    public static CropView cropView;

    public static SettingsApp settings;
    private int backCount=0;
    private LocationManager mLocationManager;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        //check and create DB if nessesary
        dbHelper = new DBHelper(this);
        try {
            dbHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        try {
            dbHelper.openDataBase();
        } catch (SQLException sqle) {
            log.info(LOG_TAG + "\n" + "Error opening database, Exeption");
            //throw sqle;
        }

        if (!permChecked) {
            try {
                permChecked = getIntent().getExtras().getBoolean("permChecked");
            } catch (Exception ex) {
                permChecked = false;
            }
        }

        settings = new SettingsApp();
        if(settings.settings.containsKey("theme"))
        {
            int theme = Integer.valueOf(settings.settings.get("theme"));
            setTheme(theme);
        }

        super.onCreate(savedInstanceState);


        //status 0 - just loaded waiting for loading
        //status 3 - loading in progress
        //status -1 - error loading from FNS not exist
        //status 1 - loaded from fns
        //status 2 - confirmed by user
        //status -4 - Status Not Acceptable from Server
        //-3 - Status Not Found from Server
        statusInvoices.put("loading", new String[]{"0", "3", "-2", "-1", "-4", "-3"});
        statusInvoices.put("in_basket", new String[]{"1"});

        setContentView(R.layout.activity_main);
        context = this;
        toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                initializeCountDrawer();
                setNavMenuChecked();
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        progressBar = findViewById(R.id.progressBar);

        currentNumber = findViewById(R.id.currentNumber);
        ivFilter = findViewById(R.id.ivFilter);
        ivFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!statusDateFilter) {
                    Intent intent = new Intent(context, CalendarPickerActivity.class);
                    statusDateFilter = true;
                    startActivityForResult(intent, MainActivity.CALENDAR_PICKER_REQUEST);
                    ivFilter.setImageResource(R.drawable.ic_filter_remove_white_24dp);
                } else {
                    statusDateFilter = false;
                    navigation.clearFilter("date_day");
                    navigation.openCurrentPage(navigation.currentPage);
                    ivFilter.setImageResource(R.drawable.ic_filter_white_24dp);
                }
            }
        });
        toolbar_title = findViewById(R.id.action_bar_title);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        invoiceCount = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.invoicesPage));
        accountingListCount = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.accountingListPAge));
        linkedListCount = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.linkedListPage));
        invoicesLoadingPage = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.invoicesLoadingPage));

        navigation = new Navigation(context, toolbar_title);

        try {
            FileInputStream fis = new FileInputStream("resources.properties");
            LogManager.getLogManager().readConfiguration(fis);
        } catch (IOException e) {
            System.err.println("Could not setup logger configuration: " + e.toString());
        }
        String filepath = Environment.getExternalStorageDirectory() + "/PriceKeeper/log/";
        File tmp = new File(filepath);
        if (!tmp.exists())
            tmp.mkdirs();
        FileHandler fh = null;
        try {
            fh = new FileHandler(filepath + "application_log.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fh != null)
            log.addHandler(fh);
        log.info("Hello!");


        cacheDir = context.getCacheDir().getAbsolutePath() + "/";

        recyclerViewFotoList = findViewById(R.id.imagelist);
        blurPlotter = findViewById(R.id.blurPlotter);
        progressBar = findViewById(R.id.progressBar);
        addMyPhoto = findViewById(R.id.addMyPhoto);
        addMyPhotoContainer = findViewById(R.id.addMyPhotoContainer);

        blurPlotter.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                blurPlotter.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                addMyPhotoContainer.setVisibility(View.GONE);
                if (googleFotoListAdapter != null) {
                    googleFotoListAdapter.placePhotoMetadataList.clear();
                    googleFotoListAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });
        addMyPhoto.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Intent chooseImageIntent = ImagePicker.getPickImageIntent(context);
                startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
                return false;
            }
        });

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        intentService = new Intent(context, LoadingFromFNS.class);
        if (!isMyServiceRunning(LoadingFromFNS.class)) {
            startService(intentService);
        }


        android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);


        hasSmsPermission = RuntimePermissionUtil.checkPermissonGranted(this, smsPerm);
        hasCameraPermission = RuntimePermissionUtil.checkPermissonGranted(this, cameraPerm);
        hasSDPermission = RuntimePermissionUtil.checkPermissonGranted(this, sdPerm);
        hasGPSPermission = RuntimePermissionUtil.checkPermissonGranted(this, MapPerm);
        if (!hasSmsPermission || !hasCameraPermission || !hasSDPermission || !hasGPSPermission) {
            RuntimePermissionUtil.requestPermission(MainActivity.this, permisions, 200);
        } else
            permChecked = true;
/*

*/

        SmsReceiver.bindListener(new SmsListener() {
            @Override
            public void messageReceived(String messageText) {
                Pattern p = Pattern.compile("[0-9]{6,10}");
                Matcher m = p.matcher(messageText);
                if (m.matches()) {
                    user._status = 1;
                    user.generateAuth(messageText);
                    Toast.makeText(context, context.getString(R.string.personal_data_request_PasswordUpdated), Toast.LENGTH_LONG).show();

                }
            }
        });

        getFnsData = new GetFnsData(android_id);



        //initiolisation userData
        user = new PersonalData(context);
        //if new user
        if ((user.id == null && permChecked) || (user._status!= null && user._status == -1)) {
            AlertDialog.Builder adb = new AlertDialog.Builder(context);
            adb.setTitle(getString(R.string.titleGreetingMessage));
            adb.setMessage(R.string.firstTimeEnter);
            adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //send to registration page
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    intent.putExtra("settingsPage", "UsersDataPreferenceFragment");
                    startActivity(intent);
                }
            });
            adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            adb.show();

        }
        //если пользователь не новый, но нужна повторная авторизация
        else if (user._status != null && user._status == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(getString(R.string.getNewPswFNS))
                    .setCancelable(false)
                    .setNegativeButton(context.getString(R.string.btnManual), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(context);
                            alertDialog.setTitle(context.getString(R.string.Settings_PasswField));
                            alertDialog.setMessage(R.string.Settings_PasswField_message);
                            final EditText input = new EditText(context);
                            input.setGravity(Gravity.CENTER);
                            input.setInputType(InputType.TYPE_CLASS_TEXT);
                            alertDialog.setView(input);
                            alertDialog.setPositiveButton(context.getString(R.string.btnOk), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String passw = input.getText().toString();
                                    Pattern p = Pattern.compile("[0-9]{6,10}");
                                    Matcher m = p.matcher(passw);
                                    if (m.matches()) {
                                        user._status = 1;
                                        user.generateAuth(passw);
                                        Toast.makeText(context, context.getString(R.string.personal_data_request_PasswordSaved), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                            alertDialog.setNegativeButton(context.getString(R.string.btnCancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                            alertDialog.show();

                        }
                    })
                    .setPositiveButton(context.getString(R.string.btnReset), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            getFnsData.resetPassword(new Callback() {
                                @Override
                                public void onFailure(Call call, final IOException e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            log.info(LOG_TAG + "\n" + e.getMessage() + "error" + getFnsData.requestStr);
                                            //FNS_Data.setText(e.getMessage() + "error" + getFnsData.requestStr);///приходит сюда. понять почему
                                        }
                                    });
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    //do nothing
                                }
                            });
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

        }


        invoice = new Invoice(navigation);
        invoice.reLoadInvoice();
        accountingList = new AccountingList();
        purchasesList = new PurchasesList();
        linkedListClass = new LinkedListClass();

        initializeCountDrawer();


        fab = findViewById(R.id.fab);
        assert fab != null;
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                blurPlotter.setVisibility(View.VISIBLE);

                View v = LayoutInflater.from(context).inflate(R.layout.add_invoicemanual_layout, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(v);
                final EditText fP = v.findViewById(R.id.addFP);
                final EditText fN = v.findViewById(R.id.addFN);
                final EditText fD = v.findViewById(R.id.addFD);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        blurPlotter.setVisibility(View.GONE);
                        AsyncFirstAddInvoice asyncFirstAddInvoice = new AsyncFirstAddInvoice();
                        String qrResult = "t=T&s=&fn=" + fN.getText().toString() + "&i=" + fD.getText().toString() + "&fp=" + fP.getText().toString() + "&n=1";
                        asyncFirstAddInvoice.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, qrResult, invoice.checkFilter("fk_invoice_accountinglist", null) ? Integer.valueOf(invoice.getFilter("fk_invoice_accountinglist")[0]).toString() : null);
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
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log.info(LOG_TAG + "\n" + "page Now +" + pageNow);
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivityForResult(intent, cameraRequest);

/*                switch(pageNow)
                {
                    case "invoicesLists":
                        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                        startActivityForResult(intent, cameraRequest);
                        break;
                    case "accountingLists":

                        LayoutInflater inflater = getLayoutInflater();
                        View dialoglayout = inflater.inflate(R.layout.add_accountinglist_layout, null);
                        final EditText dialogHint = dialoglayout.findViewById(R.id.addList);
                        dialogHint.setHint(R.string.title_addAccointingList);
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        builder.setView(dialoglayout);
                        final AlertDialog dialog = builder.create();

                        TextView btnAdd = dialoglayout.findViewById(R.id.btnAdd1);
                        btnAdd.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.cancel();
                                AccountingListData tmp = new AccountingListData();
                                tmp.setName(dialogHint.getText().toString());
                                String res = accountingList.addAccountingList(null, tmp);
                                if(res.equals("exist"))
                                {
                                    Toast.makeText(context, "exist", Toast.LENGTH_LONG).show();
                                }
                                else if(res.equals(""))
                                    AccountingListPageFragment.accountingListAdapter.notifyDataSetChanged();
                                return;
                            }
                        });

                        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
                        wmlp.gravity = Gravity.TOP|Gravity.CENTER;
                        dialog.show();
                        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                                if (keyCode == KeyEvent.KEYCODE_BACK) {
                                    dialogInterface.cancel();
                                    return true;
                                }
                                return false;
                            }
                        });
                        break;
                }
*/
            }
        });

        navigation.openCurrentPage(new Page("", 0));
    }

    
    private void setNavMenuChecked() {
        uncheckNavMenu();
        switch (navigation.currentPage.position) {
            case 0:
                setNavMenuItemChecked(R.id.invoicesPage);
                break;
            case 2:
                setNavMenuItemChecked(R.id.accountingListPAge);
                break;
            case 3:
                setNavMenuItemChecked(R.id.linkedListPage);
                break;
            case 1:
                setNavMenuItemChecked(R.id.busketListPage);
                break;
            case 5:
                setNavMenuItemChecked(R.id.invoicesLoadingPage);
                break;
        }
    }

    private void setNavMenuItemChecked(Integer Id) {
        Menu navMenu = navigationView.getMenu();
        for (int i = 0; i < navMenu.size(); i++) {
            if (navMenu.getItem(i).getItemId() == Id)
                navMenu.getItem(i).setChecked(true);
        }
    }

    public void initializeCountDrawer() {
        Map<String, String[]> filter = new ArrayMap<>();
        //все чеки
        invoiceCount.setGravity(Gravity.CENTER_VERTICAL);
        invoiceCount.setTypeface(null, Typeface.BOLD);
        invoiceCount.setTextColor(getThemeColor(context, R.attr.colorNewAccentLink));
        filter.clear();
        filter.put("in_basket", new String[]{"0"});
        if (navigation.filterDates != null && navigation.filterDates.size() > 0) {
            filter.put("date_day", dateFilterToString());
        }
        invoiceCount.setText(String.valueOf(invoice.getCount(filter)));

        //список покупок
        accountingListCount.setGravity(Gravity.CENTER_VERTICAL);
        accountingListCount.setTypeface(null, Typeface.BOLD);
        accountingListCount.setTextColor(getThemeColor(context, R.attr.colorNewAccentLink));
        accountingListCount.setText(String.valueOf(accountingList.getCount()));

        //закрепленные
        linkedListCount.setGravity(Gravity.CENTER_VERTICAL);
        linkedListCount.setTypeface(null, Typeface.BOLD);
        linkedListCount.setTextColor(getThemeColor(context, R.attr.colorNewAccentLink));
        linkedListCount.setText(String.valueOf(linkedListClass.getCount()));
        //Загрузка
        invoicesLoadingPage.setGravity(Gravity.CENTER_VERTICAL);
        invoicesLoadingPage.setTypeface(null, Typeface.BOLD);
        invoicesLoadingPage.setTextColor(getThemeColor(context, R.attr.colorNewAccentLink));
        filter.clear();
        filter.put("_status", statusInvoices.get("loading"));
        filter.put("in_basket", new String[]{"0"});
        if (navigation.filterDates != null && navigation.filterDates.size() > 0) {
            filter.put("date_day", dateFilterToString());
        }
        invoicesLoadingPage.setText(String.valueOf(invoice.getCount(filter)));
    }


    private String[] dateFilterToString() {
        if (navigation.filterDates != null && navigation.filterDates.size() > 0) {
            List<String> selectionArgs = new ArrayList<>();
            for (Date date : navigation.filterDates) {
                selectionArgs.add(String.valueOf(date.getTime()));
            }
            return selectionArgs.toArray(new String[selectionArgs.size()]);
        }
        return null;
    }

    private void clearFilter(String date_day) {
        if (filterDates != null) {
            filterDates.clear();
            filterParam.remove("date_day");
        }
    }

    private void setFilter(String param, String value) {
        if (!filterParam.containsKey(param)) {
            filterParam.put(param, new String[]{value});
        }

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {


        uncheckNavMenu();
        switch (menuItem.getItemId()) {
            case R.id.invoicesPage:
                menuItem.setChecked(true);
                closeDrawer();
                navigation.openCurrentPage(new Page("", 0));
                return true;
            case R.id.accountingListPAge:
                menuItem.setChecked(true);
                closeDrawer();
                navigation.openCurrentPage(new Page("", 2));
                return true;
            case R.id.linkedListPage:
                menuItem.setChecked(true);
                closeDrawer();
                navigation.openCurrentPage(new Page("", 3));
                return true;
            case R.id.busketListPage:
                menuItem.setChecked(true);
                closeDrawer();
                navigation.openCurrentPage(new Page("", 1));
                return true;
            case R.id.invoicesLoadingPage:
                menuItem.setChecked(true);
                closeDrawer();
                navigation.openCurrentPage(new Page(context.getString(R.string.loadingInvoicesTitle), 5));
                return true;
            case R.id.makeBackup:
                closeDrawer();
                progressBar.setVisibility(View.VISIBLE);
                blurPlotter.setVisibility(View.VISIBLE);
                dbHelper.backUpDataBase(true);
                progressBar.setVisibility(View.GONE);
                blurPlotter.setVisibility(View.GONE);
                break;
            case R.id.settings:
                Intent intent = new Intent(context, SettingsActivity.class);
                startActivity(intent);
                closeDrawer();
                break;
            case R.id.restoreBackup:
                intent = new Intent()
                        .setType("file/db")
                        .setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent, "Select a file"), restoreFromBackUp);
                break;
            /*case R.id.changeTheme:
                Map<String, String> setTmp =new ArrayMap<>();

                Log.d("themetheme", settings.settings.get("theme") + "|"+String.valueOf(R.style.FirstTheme));
                if(!settings.settings.get("theme").equals(String.valueOf(R.style.FirstTheme)))
                    setTmp.put("theme", String.valueOf(R.style.FirstTheme));
                else
                    setTmp.put("theme", String.valueOf(R.style.SecondTheme));
                settings.setSettings(setTmp);
                restartActivity();
                break;
                */
        }

        return false;
    }

    private void closeDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    public void uncheckNavMenu() {
        int size = navigationView.getMenu().size();
        for (int i = 0; i < size; i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }
    }


    public static class Page implements Comparable<Page> {
        public Page(String pageName, Integer position) {
            this.pageName = pageName;
            this.position = position;
        }

        public Integer positionInList;
        private int id;
        public String pageName;
        public Integer position;
        public static int lastId = 0;

        public void setId() {
            lastId = lastId + 1;
            id = lastId;
        }

        public int getId() {
            return id;
        }


        @Override
        public int compareTo(Page page) {
            if (this.id == page.id)
                return 0;
            else if (this.id < page.id)
                return -1;
            else
                return 1;
        }
    }

    private boolean showMap(InvoiceData currentInvoice) {
        if (currentInvoice.kktRegId != null && currentInvoice.kktRegId._status == 0)
            return true;
        else if (currentInvoice.kktRegId == null && currentInvoice.store != null && currentInvoice.store._status == 0)
            return true;
        else if (currentInvoice.kktRegId == null && currentInvoice.store == null)
            return true;
        else
            return false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.reset_fns_psw: {

                Intent intent = new Intent(MainActivity.this, UserDataActivity.class);
                intent.putExtra("requestCode", personalDataShow);
                startActivityForResult(intent, personalDataShow);

                return true;
            }
            case R.id.show_acc_list: {

                return true;
            }
            case R.id.load_inv_from_img: {


                Intent intent = new Intent();
                intent.setType("*/*");
                String[] mimetypes = {"image/*", "application/pdf"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_INVOICE_FROM_IMAGE);

                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static String getRealPathFromURI_API19(Context context, Uri uri) {
        if (isExternalStorageDocument(uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];

            if ("primary".equalsIgnoreCase(type)) {
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            }

        }
        // DownloadsProvider
        else if (isDownloadsDocument(uri)) {

            final String id = DocumentsContract.getDocumentId(uri);
            final Uri contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

            return getDataColumn(context, contentUri, null, null);
        }
        // MediaProvider
        else if (isMediaDocument(uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];

            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if ("audio".equals(type)) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }

            final String selection = "_id=?";
            final String[] selectionArgs = new String[]{
                    split[1]
            };

            return getDataColumn(context, contentUri, selection, selectionArgs);
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        switch (requestCode) {
            case restoreFromBackUp:
                Uri uri = null;
                if (data != null) {
                    uri = data.getData();
                    String mimeType = getApplicationContext().getContentResolver().getType(uri);
                    Log.d(LOG_TAG, mimeType);

                }
            case CAPTURE_FROM_PDF_QR: {
                if (data == null) {
                    return;
                }
                if (resultCode == RESULT_OK) {
                    AsyncFirstAddInvoice asyncFirstAddInvoice = new AsyncFirstAddInvoice();
                    asyncFirstAddInvoice.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data.getStringExtra("qrCode"), invoice.checkFilter("fk_invoice_accountinglist", null) ? Integer.valueOf(invoice.getFilter("fk_invoice_accountinglist")[0]).toString() : null);
                }
            }
            break;
            case PICK_INVOICE_FROM_IMAGE: {
                if (data == null) {
                    return;
                }
                if (resultCode == RESULT_OK) {
                    Bitmap myBitmap = null;
                    ContentResolver cr = this.getContentResolver();
                    String mime = cr.getType(data.getData());
                    PDFDocument document = new PDFDocument();

                    if (mime.equals("application/pdf")) {

                        Intent intent = new Intent(MainActivity.this, PDFActivity.class);
                        intent.putExtra("pdfUrl", getRealPathFromURI_API19(context, data.getData()));
                        startActivityForResult(intent, CAPTURE_FROM_PDF_QR);
                    }
                    try {
                        InputStream inputStream = context.getContentResolver().openInputStream(data.getData());
                        myBitmap = BitmapFactory.decodeStream(inputStream);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    if (myBitmap != null) {
                        BarcodeDetector detector =
                                new BarcodeDetector.Builder(getApplicationContext())
                                        .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                                        .build();
                        if (!detector.isOperational()) {
                            return;
                        }

                        Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
                        SparseArray<Barcode> barcodes = detector.detect(frame);
                        if (barcodes.valueAt(0) != null) {
                            Barcode thisCode = barcodes.valueAt(0);
                            AsyncFirstAddInvoice asyncFirstAddInvoice = new AsyncFirstAddInvoice();
                            asyncFirstAddInvoice.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, thisCode.rawValue, invoice.checkFilter("fk_invoice_accountinglist", null) ? Integer.valueOf(invoice.getFilter("fk_invoice_accountinglist")[0]).toString() : null);
                        } else {
                            Toast.makeText(this, R.string.invoice_load_from_image_error, Toast.LENGTH_LONG);
                        }


                    }
                }
            }
            break;
            case CALENDAR_PICKER_REQUEST:
                if (data == null) {
                    return;
                }
                if (resultCode == RESULT_OK) {
                    invoice.filterDates = invoice.filterDates;
                    navigation.filterParam.put("date_day", null);
                    ArrayList<? extends Date> tmp = data.getParcelableArrayListExtra("dates");
                    if (navigation.filterDates == null)
                        navigation.filterDates = new ArrayList<>();
                    navigation.filterDates.addAll(tmp);
                    invoice.filterDates = invoice.filterDates;
                    navigation.openCurrentPage(navigation.currentPage);
                }
                break;
            case cameraRequest: {
                if (data == null) {
                    return;
                }
                if (resultCode == RESULT_OK) {
                    //status 0 - just loaded waiting for loading
                    //status 3 - loading in progress
                    //status -1 - error loading from FNS not exist
                    //status 1 - loaded from fns
                    //status 2 - confirmed by user
                    navigation.openCurrentPage(new Page("", 0));
                    AsyncFirstAddInvoice asyncFirstAddInvoice = new AsyncFirstAddInvoice();
                    asyncFirstAddInvoice.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data.getStringExtra("resultQR"), invoice.checkFilter("fk_invoice_accountinglist", null) ? Integer.valueOf(invoice.getFilter("fk_invoice_accountinglist")[0]).toString() : null);

                } else {
                    log.info(LOG_TAG + "\n" + "CameraActivity canceled");
                }
                break;
            }
            case personalDataShow: {
                if (data == null) {
                    return;
                }
                //user data is in object user!!!

                //String greetings =  data.getStringExtra("phone");
                //Toast.makeText(this.getApplicationContext(), "phone " + greetings, Toast.LENGTH_LONG).show();
                //getFnsData.setPhoneNumber(greetings);
                /*
                getFnsData.resetPassword(new Callback(){
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FNS_Data = findViewById(R.id.FNS_Data);
                                FNS_Data.setMovementMethod(new ScrollingMovementMethod());
                                FNS_Data.setText(e.getMessage() + "error" + getFnsData.requestStr);
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FNS_Data = findViewById(R.id.FNS_Data);
                                FNS_Data.setMovementMethod(new ScrollingMovementMethod());
                                try {
                                    FNS_Data.setText(getFnsData.requestStr+getFnsData.bodyRec.toString()+response.body().string().toString()+"\n\n\n");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                        });

                    }
                });
                */
                break;
            }
            case personalDataRequestFull: {
                if (data == null) {
                    return;
                }
                String result = "";
                try {
                    result = data.getExtras().getString("error");
                } catch (Exception ex) {
                    result = "";
                }
                if (result == null)
                    result = "";
                log.info(LOG_TAG + "\n" + "result from personalDataRequestFull\n" + result);
                switch (result) {
                    case "conflict": {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage(getString(R.string.errorUserAlreadyRegistered))
                                .setCancelable(false)
                                .setNegativeButton("Cansel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {


                                    }
                                })
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        getFnsData.resetPassword(new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                log.info(LOG_TAG + "\n" + e.getMessage() + "\nerror\n" + getFnsData.requestStr);
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                log.info(LOG_TAG + "\n" + response.message() + "\n" + getFnsData.requestStr);
                                            }
                                        });
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                        break;
                    }
                    case "connection": {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage(getString(R.string.connectionError))
                                .setCancelable(false)
                                .setNegativeButton(R.string.btnCancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                })
                                .setPositiveButton(R.string.btnOk, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent intent = new Intent(MainActivity.this, UserDataActivity.class);
                                        intent.putExtra("requestCode", personalDataRequestFull);
                                        startActivityForResult(intent, personalDataRequestFull);
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                        break;
                    }
                }


                //data recived just make Toas
                Toast.makeText(context, getString(R.string.UserDataEddedSuccesfuly) + " " + user.surname + " " + user.name, Toast.LENGTH_LONG).show();

                break;
            }
            //confitm place on google map
            case PLACE_PICKER_REQUEST: {
                if (data == null) {
                    return;
                }
                Place place = PlacePicker.getPlace(this, data);
                log.info(LOG_TAG + "\n" + "you finde store " + place.getName() + " address " + place.getAddress());
                InvoiceData invoiceData = currentInvoice;
                if (invoiceData.store == null) {
                    invoiceData.store = new InvoiceData.Store();
                }
                //if(place.getName().toString().contains(place.getLatLng().))

                if (!Pattern.matches(".*[-]?\\d{1,2}.\\d{1,2}.\\d{1,2}[.,]{1}\\d{1,2}.\\w.*", place.getName().toString()))
                    invoiceData.store.name = place.getName().toString();
                invoiceData.store.address = place.getAddress().toString();
                invoiceData.store.longitude = place.getLatLng().longitude;
                invoiceData.store.latitude = place.getLatLng().latitude;
                invoiceData.store.place_id = place.getId();
                invoiceData.store._status = 1;
                invoiceData.store.store_type = place.getPlaceTypes().toString();
                if (invoiceData.kktRegId != null)
                    invoiceData.kktRegId._status = 1;
                invoice.setStoreDataFull(invoiceData);
                invoice.updateInvoice(invoiceData);
                invoice.reLoadInvoice();
                InvoicesPageFragment.invoiceListAdapter.notifyDataSetChanged();
                if (invoiceData.store.place_id != null) {
                    PhotoTask photoTask = new PhotoTask(500, 500);
                    photoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, invoiceData.store.place_id, invoiceData.store.latitude.toString(), invoiceData.store.longitude.toString());
                }
                break;
            }
            //set oun image
            case PICK_IMAGE_ID: {
                Bitmap bitmap = ImagePicker.getImageFromResult(this, resultCode, data);
                saveImages(bitmap, null, null, currentInvoice.store.place_id);
                String filepath = Environment.getExternalStorageDirectory()+"/PriceKeeper/storeImage/IMG_"+currentInvoice.store.place_id + ".png";
                Intent intent = new Intent(context, CropActivity.class);
                intent.putExtra("photo_reference", filepath);
                intent.putExtra("place_id", currentInvoice.store.place_id);
                intent.putExtra("key", "1");
                intent.putExtra("store_id", currentInvoice.store.id);
                blurPlotter.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                addMyPhotoContainer.setVisibility(View.GONE);
                if (googleFotoListAdapter != null) {
                    googleFotoListAdapter.placePhotoMetadataList.clear();
                    googleFotoListAdapter.notifyDataSetChanged();
                }
                context.startActivity(intent);

                break;
            }
            //set chosen image from google
            case SetImageFromGoogle_REQUEST: {
                if (data != null) {
                    String imgUrl = data.getExtras().getString("url");
                    copyfile(imgUrl, Environment.getExternalStorageDirectory() + "/PriceKeeper/storeImage/" + "IMG_" + currentInvoice.store.place_id + ".png");
                }
                break;
            }

            default:
                break;
        }

    }


    public static void copyfile(String srFile, String dtFile) {
        try {
            File f1 = new File(srFile);
            File f2 = new File(dtFile);
            InputStream in = new FileInputStream(f1);

            //For Append the file.
            //OutputStream out = new FileOutputStream(f2,true);

            //For Overwrite the file.
            OutputStream out = new FileOutputStream(f2);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            System.out.println("File copied.");
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage() + " in the specified directory.");
            System.exit(0);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void fillData(Response response, InvoiceData finalInvoiceData) throws IOException {
        getFnsData.body = response.body().string();
        invoice.addJsonData(getFnsData.body, finalInvoiceData.getId());
        getFnsData.bodyJsonParse();

        //get GPS from address and full address


        if (getFnsData.dataFromReceipt.document.receipt.retailPlaceAddress == null &&
                (getFnsData.dataFromReceipt.document.receipt.user != null &&
                        (getFnsData.dataFromReceipt.document.receipt.user.toLowerCase().contains("г.") ||
                                getFnsData.dataFromReceipt.document.receipt.user.toLowerCase().contains("д.")))) {
            getFnsData.dataFromReceipt.document.receipt.retailPlaceAddress = getFnsData.dataFromReceipt.document.receipt.user;
        }
        if (getFnsData.dataFromReceipt.document.receipt.retailPlaceAddress != null) {
            Geocoder geocoder = new Geocoder(context);
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocationName(getFnsData.dataFromReceipt.document.receipt.retailPlaceAddress, 1);
                if (addresses.size() > 0) {
                    finalInvoiceData.store = new InvoiceData.Store();
                    finalInvoiceData.store.address = addresses.get(0).getAddressLine(0);
                    finalInvoiceData.store.latitude = addresses.get(0).getLatitude();
                    finalInvoiceData.store.longitude = addresses.get(0).getLongitude();

                    if (getFnsData.dataFromReceipt.document.receipt.retailPlaceAddress == getFnsData.dataFromReceipt.document.receipt.user)
                        getFnsData.dataFromReceipt.document.receipt.user = null;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        finalInvoiceData.set_status(2);
        //invoice.updateInvoice(finalInvoiceData);
        //check is address in
        final int count = invoice.fillReceiptData(getFnsData.dataFromReceipt.document.receipt, finalInvoiceData);

        //run activity with map to confirm address
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {

        if (grantResults.length > 0) {
            for (int i = 0; i < permissions.length; i++) {
                if (!RuntimePermissionUtil.checkPermissonGranted(MainActivity.this, permissions[i])) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Необходимо предоставить все разрешенния.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
            restartActivity();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Необходимо предоставить все разрешенния.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

/*
        if (requestCode == 100) {
            RuntimePermissionUtil.onRequestPermissionsResult(grantResults, new RPResultListener() {
                @Override
                public void onPermissionGranted() {
                    if ( RuntimePermissionUtil.checkPermissonGranted(MainActivity.this, cameraPerm)) {
                        restartActivity();
                    }
                }

                @Override
                public void onPermissionDenied() {
                }
            });
        }
        if (requestCode == 200) {
            RuntimePermissionUtil.onRequestPermissionsResult(grantResults, new RPResultListener() {
                @Override
                public void onPermissionGranted() {
                    if ( RuntimePermissionUtil.checkPermissonGranted(MainActivity.this, smsPerm)) {
                        restartActivity();

                    }
                }

                @Override
                public void onPermissionDenied() {
                }
            });
        }*/

    }

    public void restartActivity() {
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.putExtra("permChecked", true);
        isActive = true;
        startActivity(intent);

        finish();
    }

    public static void setPageBack(Integer back, Integer newPage) {
        if (newPage != back) {
            if (back != null && back != PURCHASE_PAGE) {
                if (pageBack.size() > 0 && pageBack.get(pageBack.size() - 1) != newPage && pageBack.get(pageBack.size() - 1) != back) {
                    pageBack.add(back);
                } else if (pageBack.size() == 0)
                    pageBack.add(back);
            }
        }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder.getClass().getName().contains("AccountingListAdapter"))
            mItemTouchHelperAccList.startDrag(viewHolder);
        if (viewHolder.getClass().getName().contains("InvoiceListAdapter"))
            mItemTouchHelperInvList.startDrag(viewHolder);
    }


    private void showPopupMenuAccountingList(View v) {
        final PopupMenu popupMenu = new PopupMenu(context, v);
        popupMenu.inflate(R.menu.popupmenu);

        MenuPopupHelper menuHelper = new MenuPopupHelper(context, (MenuBuilder) popupMenu.getMenu(), v);
        menuHelper.setForceShowIcon(true);

        //generate it from DB
        for (int i = 0; i < accountingList.accountingListData.size(); i++) {
            log.info(LOG_TAG + "\n" + "Add popUpMenu id =" + accountingList.accountingListData.get(i).getId());
            popupMenu.getMenu().add(R.id.menugroup1, accountingList.accountingListData.get(i).getId(), Menu.NONE, accountingList.accountingListData.get(i).getName());
        }

        // для версии Android 3.0 нужно использовать длинный вариант
        // popupMenu.getMenuInflater().inflate(R.menu.popupmenu,
        // popupMenu.getMenu());

        popupMenu
                .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.addButton) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle(R.string.title_addAccointingList);
                            final EditText input = new EditText(context);
                            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            builder.setView(input);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AccountingListData tmp = new AccountingListData();
                                    tmp.setName(input.getText().toString());
                                    accountingList.addAccountingList(null, tmp);

                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            builder.show();

                        } else if (item.getItemId() == R.id.allLists) {
                            invoice.setfilter("fk_invoice_accountinglist", new String[]{null});
                            invoice.reLoadInvoice();
                            Toast.makeText(getApplicationContext(), item.getItemId() + "", Toast.LENGTH_SHORT).show();
                        } else {

                            invoice.setfilter("fk_invoice_accountinglist", new String[]{item.getItemId() + ""});
                            invoice.reLoadInvoice();
                            Toast.makeText(getApplicationContext(), item.getItemId() + "", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                        // Toast.makeText(PopupMenuDemoActivity.this,
                        // item.toString(), Toast.LENGTH_LONG).show();
                        // return true;
                        /*switch (item.getItemId()) {

                            case R.id.menu1:
                                Toast.makeText(getApplicationContext(),
                                        "Вы выбрали PopupMenu 1",
                                        Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.menu2:
                                Toast.makeText(getApplicationContext(),
                                        "Вы выбрали PopupMenu 2",
                                        Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.menu3:
                                Toast.makeText(getApplicationContext(),
                                        "Вы выбрали PopupMenu 3",
                                        Toast.LENGTH_SHORT).show();
                                return true;
                            default:
                                return false;
                        }*/
                    }
                });
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {

            @Override
            public void onDismiss(PopupMenu menu) {
                //Toast.makeText(getApplicationContext(), "onDismiss", Toast.LENGTH_SHORT).show();
            }
        });
        menuHelper.show();
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isActive)
            dbHelper.close();

        isActive = false;

    }

    class AsyncFirstAddInvoice extends AsyncTask<String, Void, InvoiceData> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected InvoiceData doInBackground(String... resultQR) {
            InvoiceData currentInvoiceData =new InvoiceData();
            InvoiceData invoiceData = new InvoiceData();
            try {
                //set coordinates in background

                QRitem = new QRManager(resultQR[0]);//место редактирования полученного кода. стоит сразу создать объект
                //save QR data to DB
                Pattern p = Pattern.compile("t=[0-9]{6,}[Tt]{1}[0-9]{4,}&s=[0-9.]{1,}&fn=[0-9]{16}&i=[0-9]{4,}&fp=[0-9]{9,}");
                Matcher m = p.matcher(QRitem.resultQR);
                if(m.find()) {

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat dateFormat_day = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                    Long invoiceDate = null;
                    Long invoiceDate_day = null;
                    try {
                        if (QRitem.date.equals("") || QRitem.time.equals("")) {
                            invoiceDate = new Date().getTime();
                            invoiceDate_day = dateFormat_day.parse(dateFormat_day.format(new Date())).getTime();
                        } else {
                            invoiceDate = dateFormat.parse(QRitem.date + " " + QRitem.time).getTime();
                            invoiceDate_day = dateFormat_day.parse(QRitem.date).getTime();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    //Toast.makeText(context, QRitem.totalSum, Toast.LENGTH_LONG).show();
                    invoiceData.setAll(QRitem.FP, QRitem.FD, QRitem.FN, invoiceDate, QRitem.totalSum, null, null, resultQR[1] == null ? null : Integer.valueOf(resultQR[1]), null);

                    //just add data from QR - first time to save and check already exist
                    invoiceData.date_day = invoiceDate_day;
                    invoiceData.setDate_add(new Date().getTime());
                    String existInvoice = invoice.addInvoice(null, invoiceData);
                    currentInvoiceData = invoice.invoices.get(invoice.lastIDCollection);
                    if (existInvoice != "exist")
                        invoice.reLoadInvoice();

                }
            } catch (Exception e) {
                log.info(LOG_TAG+"\n"+ "ERROR\n");
                log.info(LOG_TAG+"\n"+ e.getMessage());
                e.printStackTrace();
            }
            return currentInvoiceData;
        }

        @Override
        protected void onPostExecute(InvoiceData result) {


            int position = -1;
            if(result.getId()!= null) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    final InvoiceData invoiceData = result;
                    SingleShotLocationProvider.requestSingleUpdate(context,
                            new SingleShotLocationProvider.LocationCallback() {
                                @Override public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {
                                    Log.d("Location", "my location is " + location.toString());
                                    if(location.latitude>0 && location.longitude>0)
                                    {
                                        invoiceData.longitudeAdd = location.longitude;
                                        invoiceData.latitudeAdd = location.latitude;
                                        invoice.updateInvoice(invoiceData);
                                    }
                                }
                            });
                }

                if (InvoicesPageFragment.invoiceListAdapter != null) {
                    position = InvoicesPageFragment.invoiceListAdapter.findPosition(result);
                    InvoicesPageFragment.invoiceListAdapter.row_index = position;
                    InvoicesPageFragment.invoiceListAdapter.notifyDataSetChanged();
                }
                //switch to page with invoices
                InvoicesPageFragment.llm.scrollToPositionWithOffset(position, 0);
                //InvoicesPageFragment.recyclerViewInvList.scrollToPositionWithOffset(position);
                log.info(LOG_TAG+"\n"+ "try to sart service ");
                if(!isMyServiceRunning(LoadingFromFNS.class))
                {
                    startService(intentService);
                }
            }
            else
            {
                Toast.makeText(context, context.getString(R.string.QRData_Error), Toast.LENGTH_LONG).show();
            }
        }
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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
            if(navigation.purchasesPageFragment!= null)
                navigation.purchasesPageFragment.onResume();

            progressBar.setVisibility(View.GONE);
            if(googleFotoListAdapter == null)
                blurPlotter.setVisibility(View.GONE);
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
           if(googleFotoListAdapter != null && !file.exists()) {
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
            log.info(LOG_TAG+"\n"+ "post progress ");
            if(values.length==0)
            {
                onResume();
            }
            else if(googleFotoListAdapter != null)
            {
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

            log.info(LOG_TAG+"\n"+ "trying to get place_id image ");
            if (params.length != 3) {
                return null;
            }
            final String placeId = params[0];

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            String URL = "http://maps.google.com/maps/api/staticmap?center=" +params[1]+","+params[2] + "&zoom=17&size="+width/2+"x"+(int)(height*0.1)+"&scale=2&markers="+params[1]+","+params[2] +"&key="+context.getString(R.string.google_maps_key);
            Bitmap mapImage = null;
            Bitmap iconImage = null;
            mapImage = getBitmapFromUrl(URL);
            GooglePlace googlePlace = new GooglePlace(placeId, getApplicationContext());
            if(googlePlace.icon != null)
            {
                iconImage = getBitmapFromUrl(googlePlace.icon);
                invoiceData.store.iconName = googlePlace.icon.substring(googlePlace.icon.lastIndexOf("/")+1);
                invoiceData.store.update = true;
                invoice.setStoreData(invoiceData);
                invoice.reLoadInvoice();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        InvoicesPageFragment.invoiceListAdapter.notifyDataSetChanged();
                    }
                });

            }
            saveImages(null, mapImage, iconImage,  placeId);
            publishProgress();

            if(!file.exists()) {
                Bitmap image = null;
                if (googlePlace.photos != null) {
                    log.info(LOG_TAG+"\n"+ "getting image from google count: " + googlePlace.photos.size());
                    for (int count = 0; count < googlePlace.photos.size(); count++) {
                        GooglePlace.Photo photo = googlePlace.photos.get(count);
                        log.info(LOG_TAG+"\n"+ "add image " + photo.photo_reference);
                        File file = new File(cacheDir, "IMG_" + placeId + "_" + count + ".png");
                        if (!file.exists()) {
                            saveImages(googlePlace.loadImage(photo.photo_reference), placeId, count);
                            file = new File(cacheDir, "IMG_" + placeId + "_" + count + ".png");
                        }
                        publishProgress(file.getPath(), photo.photo_reference);//cacheDir + "IMG_" + placeId + "_" + count + ".png");
                    }

                }
            }
            return null;
        }

    }
    private Bitmap getBitmapFromUrl(String url)
    {
        Bitmap image = null;
        OkHttpClient httpclient = new OkHttpClient();
        okhttp3.Request request = new Request.Builder()
                .url(url)
                .build();

        Response in = null;
        try {
            in = httpclient.newCall(request).execute();
            image = BitmapFactory.decodeStream(in.body().byteStream());
            in.close();
        } catch (Exception ex) {
            log.info(LOG_TAG+"\n"+ "ERROR to save place_id MAP image folder \n"+ex.getMessage());
            ex.printStackTrace();
        }
        return image;
    }

    private static void saveImages(Bitmap image, Bitmap mapImage, Bitmap iconImage, String placeId)
    {
        try {
            String filepath = Environment.getExternalStorageDirectory()+"/PriceKeeper/storeImage/";
            log.info(LOG_TAG+"\n"+ "trying to save place_id image folder " + filepath);
            File dir = new File(filepath);

            if (!dir.exists()) dir.mkdirs();

            if(image!= null) {
                File file = new File(filepath, "IMG_"+placeId + ".png");
                FileOutputStream fOut = new FileOutputStream(file);

                image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();
            }
            if(mapImage != null)
            {
                File file = new File(filepath, "MAP_"+placeId + ".png");
                FileOutputStream fOut = new FileOutputStream(file);

                mapImage.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();
            }
            if(iconImage != null && currentInvoice.store.iconName != "")
            {
                filepath = Environment.getExternalStorageDirectory()+"/PriceKeeper/icons/";
                dir = new File(filepath);
                if (!dir.exists()) dir.mkdirs();

                File file = new File(filepath, currentInvoice.store.iconName);
                FileOutputStream fOut = new FileOutputStream(file);

                iconImage.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();
            }
        }
        catch (Exception ex)
        {
            log.info(LOG_TAG+"\n"+ "ERROR to save place_id image folder \n"+ex.getMessage());
            ex.printStackTrace();
        }
    }
    private void saveImages(Bitmap image, String place_id, int count)
    {
        FileOutputStream fOut= null;

        try {
            if(image!= null) {
                log.info(LOG_TAG+"\n"+ "trying to save place_id image folder " + cacheDir +"IMG_"+place_id +"_"+count +".png");
                File file = new File(cacheDir, "IMG_"+place_id + "_"+count +".png");
                fOut = new FileOutputStream(file);
                image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            }
        }
        catch (Exception ex)
        {
            log.info(LOG_TAG+"\n"+ "ERROR to save place_id image folder \n"+ex.getMessage());
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

    public static final String getUrlToResource(@NonNull Context context,
                                             @AnyRes int resId)
            throws Resources.NotFoundException {
        /** Return a Resources instance for your application's package. */
        Resources res = context.getResources();
        /**
         * Creates a Uri which parses the given encoded URI string.
         * @param uriString an RFC 2396-compliant, encoded URI
         * @throws NullPointerException if uriString is null
         * @return Uri for this given uri string
         */
        String resUrl = res.getResourcePackageName(resId)
                + '/' + res.getResourceTypeName(resId)
                + '/' + res.getResourceEntryName(resId);
        /** return uri */
        return resUrl;
    }


    public static String setInvoiceNameByStatus(Integer status)
    {
        if(status != null) {
            switch (status) {
                case -3:
                    return context.getText(R.string.not_found_in_FNS).toString();
                case -4:
                    return context.getText(R.string.not_acceptable_in_FNS).toString();
                case -2:
                    return context.getText(R.string.acces_forbidden_to_FNS).toString();
                case -1:
                    return context.getText(R.string.errorGetFrom_FNS).toString();
                case 0:
                    return context.getText(R.string.waiting_for_loading_invoice_from_FNS).toString();
                case 3:
                    return context.getText(R.string.loading_invoice_from_FNS).toString();
                default:
                    return "";
            }
        }
        else return "";
    }

    @Override
    public void onBackPressed() {
        boolean mainBackPress = false;
        if(navigation.purchasesPageFragment != null)
            mainBackPress = navigation.purchasesPageFragment.onBackPressed();
        if (!mainBackPress) {
            if (blurPlotter.getVisibility() == View.VISIBLE) {
                blurPlotter.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            } else {
                if (navigation.pageBackList.size() > 0) {
                    navigation.backPress();

                    closeDrawer();
                } else if (backCount == 0) {
                    backCount += 1;
                    Toast.makeText(context, context.getString(R.string.press_back_to_exit), Toast.LENGTH_LONG).show();

                    //reset if no press for interval
                    Runnable runnableUndo = new Runnable() {

                        @Override
                        public void run() {
                            backCount = 0;
                        }
                    };
                    Handler handlerUndo = new Handler();
                    handlerUndo.postDelayed(runnableUndo, 2500);
                } else
                    finish();
            }
        }
    }

    public static boolean invoiceClickAble(@Nullable Integer status)
    {
        if(status != null) {
            switch (status) {
                case 1:
                    return true;
                case -1:
                    return true;
                case -3://Not Found from FNS
                    return true;
                case -4://Not Acceptable from FNS
                    return true;
                default:
                    return false;
            }
        }
        else return false;
    }
    public String stringSplitter(String string)
    {
        String[] chr = new String[]{};
        return string;
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

  /*  public static int getThemeColor (final Context context, final int colorAttr) {
        final TypedValue value = new TypedValue();
        boolean found = context.getTheme().resolveAttribute(colorAttr, value, true);
        if(found)
            return value.resourceId;
        else
            return R.color.background;

    }*/

    public static int getThemeColor(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        TypedArray ta = context.obtainStyledAttributes(typedValue.resourceId, new int[]{attr});
        int color = ta.getColor(0, 0);
        ta.recycle();
        return color;
    }

}
