package com.turquoise.hotelbookrecomendation.Activities;

import static com.turquoise.hotelbookrecomendation.Activities.MainActivity.bookings;
import static com.turquoise.hotelbookrecomendation.Activities.MainActivity.cartList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.google.gson.Gson;
import com.paypal.checkout.PayPalCheckout;
import com.paypal.checkout.config.CheckoutConfig;
import com.paypal.checkout.config.Environment;
import com.paypal.checkout.config.SettingsConfig;
import com.paypal.checkout.createorder.CurrencyCode;
import com.paypal.checkout.createorder.OrderIntent;
import com.paypal.checkout.createorder.UserAction;
import com.paypal.checkout.order.Amount;
import com.paypal.checkout.order.AppContext;
import com.paypal.checkout.order.Order;
import com.paypal.checkout.order.PurchaseUnit;
import com.squareup.picasso.Picasso;
import com.turquoise.hotelbookrecomendation.Fragments.Recommendation;
import com.turquoise.hotelbookrecomendation.R;
import com.turquoise.hotelbookrecomendation.model.Hotel;
import com.turquoise.hotelbookrecomendation.model.HotelResult;
import com.turquoise.hotelbookrecomendation.model.UserHotel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HotelInfo extends AppCompatActivity {

    private static final String TAG = HotelInfo.class.getSimpleName();
    private Toolbar toolbar;
    private ImageView hotelImage;
    private TextView hotelDesc, views, drafts, completed, totalRooms, totalGuests;
    private Button book, draftBook, incrementRoomsBtn, decrementRoomsBtn,
            incrementGuestsBtn, decrementGuestsBtn, jacuzziBtn, swimmingBtn, mealsBtn;
    //    private RecommendationAdapter.HotelViewHolder hotelViewHolder;
    public Hotel hotel;
    private Boolean jacuzzi = false, swimming = false, meals = false;
    private int pos, totalRoomsCount = 1, totalGuestsCount = 1;
    private float bookingPrice = 0;
    private Boolean draftBooking = false;
    HotelResult hotelResult;
    UserHotel hotel1 = new UserHotel();

    /**
     * Merchant Sandbox Account Used:
     * Email - joey@paypaltest.com
     * Password - Test123!
     * <p>
     * Merchant Sandbox Account for User Payment:
     * Email - sb-xdw8h12108081@business.example.com
     * Password - kzi1V--L
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_info);

        toolbar = findViewById(R.id.toolbarInfo);
        hotelImage = findViewById(R.id.hotelImage);
        hotelDesc = findViewById(R.id.hotelDesc);
        book = findViewById(R.id.confirmBooking);
        draftBook = findViewById(R.id.draftBooking);
        views = findViewById(R.id.views);
        drafts = findViewById(R.id.draftText);
        completed = findViewById(R.id.completedText);
        totalRooms = findViewById(R.id.totalRoomsText);
        totalGuests = findViewById(R.id.totalGuestsText);
        incrementRoomsBtn = findViewById(R.id.incrementRoomsBtn);
        decrementRoomsBtn = findViewById(R.id.decrementRoomsBtn);
        incrementGuestsBtn = findViewById(R.id.incrementGuestsBtn);
        decrementGuestsBtn = findViewById(R.id.decrementGuestsBtn);
        jacuzziBtn = findViewById(R.id.jacuzziBtn);
        swimmingBtn = findViewById(R.id.swimmingBtn);
        mealsBtn = findViewById(R.id.mealsBtn);

        CheckoutConfig config = new CheckoutConfig(
                getApplication(),
                getString(R.string.client_id),
                Environment.SANDBOX,
                "com.turquoise.hotelbookrecomendation://paypalpay",
                CurrencyCode.USD,
                UserAction.PAY_NOW,
                new SettingsConfig(true, false)
        );
        PayPalCheckout.setConfig(config);
    }

    @Override
    protected void onResume() {
        super.onResume();

        hotel = (Hotel) getIntent().getExtras().getSerializable("data");

        hotelResult = new Gson().fromJson(getHotels(), HotelResult.class);
        pos = getIntent().getExtras().getInt("pos");

        toolbar.setTitle(hotel.getName());

        setSupportActionBar(toolbar);
        Picasso
                .get()
                .load(Uri.parse(hotel.getImageUrl()))
                .into(hotelImage);
        hotelDesc.setText(hotel.getDescription());

        Log.d(TAG, "onResume: bookings"+bookings.getUserHotels().size());
        for (Hotel cartHotel : cartList) {
            if (cartHotel.getName().equals(hotel.getName())) {
                for (UserHotel userHotel : bookings.getUserHotels()) {
                    if (userHotel.getHotel().getName().equals(hotel.getName())) {
                        hotel1 = userHotel;
                        draftBooking = true;
                        jacuzzi = userHotel.getOptions().get("jacuzzi");
                        swimming = userHotel.getOptions().get("swimming");
                        meals = userHotel.getOptions().get("meals");
                        totalGuestsCount = userHotel.getGuests();
                        totalRoomsCount = userHotel.getRooms();
                        break;
                    }
                }
            }
        }

        views.setText(String.format("%s views", hotelResult.getHotels().get(pos).getVisits()));
        drafts.setText(String.format("%s drafts", hotelResult.getHotels().get(pos).getDraftBookings()));
        completed.setText(String.format("%s booked", hotelResult.getHotels().get(pos).getCompletedBookings()));

        setHotelOptions(jacuzzi, jacuzziBtn);
        setHotelOptions(swimming, swimmingBtn);
        setHotelOptions(meals, mealsBtn);

        setTotalRooms(totalRoomsCount);
        setTotalGuests(totalGuestsCount);
        calculateTotalPrice();

        jacuzziBtn.setOnClickListener(view -> {
            jacuzzi = !jacuzzi;
            setHotelOptions(jacuzzi, jacuzziBtn);
            calculateTotalPrice();
        });

        swimmingBtn.setOnClickListener(view -> {
            swimming = !swimming;
            setHotelOptions(swimming, swimmingBtn);
            calculateTotalPrice();
        });

        mealsBtn.setOnClickListener(view -> {
            meals = !meals;
            setHotelOptions(meals, mealsBtn);
            calculateTotalPrice();
        });

        book.setOnClickListener(v -> setBooking(true));
        draftBook.setOnClickListener(v -> {
            setBooking(false);
            if (!draftBooking) {
                toast("Hotel Added to Cart");
                cartList.add(hotel);
                MainActivity.updateCartCount(cartList.size());
            } else {
                toast("Hotel updated in Cart");
            }
            finish();
        });

        incrementRoomsBtn.setOnClickListener(view -> {
            totalRoomsCount++;
            setTotalRooms(totalRoomsCount);
            if (totalRoomsCount == 2) {
                decrementRoomsBtn.setEnabled(true);
            }
            calculateTotalPrice();
        });

        decrementRoomsBtn.setOnClickListener(view -> {
            if (totalRoomsCount > 1) {
                totalRoomsCount--;
                setTotalRooms(totalRoomsCount);
                if (totalRoomsCount == 1) {
                    view.setEnabled(false);
                }
            }
            calculateTotalPrice();
        });

        incrementGuestsBtn.setOnClickListener(view -> {
            totalGuestsCount++;
            setTotalGuests(totalGuestsCount);
            if (totalGuestsCount == 2) {
                decrementGuestsBtn.setEnabled(true);
            }
            calculateTotalPrice();
        });

        decrementGuestsBtn.setOnClickListener(view -> {
            if (totalGuestsCount > 1) {
                totalGuestsCount--;
                setTotalGuests(totalGuestsCount);
                if (totalGuestsCount == 1) {
                    view.setEnabled(false);
                }
            }
            calculateTotalPrice();
        });

        if (totalGuestsCount == 1) {
            decrementGuestsBtn.setEnabled(false);
        }
        if (totalRoomsCount == 1) {
            decrementRoomsBtn.setEnabled(false);
        }
    }

    private void setHotelOptions(Boolean option, Button optionBtn) {
        if (option) {
            optionBtn.setTextColor(getResources().getColor(android.R.color.white));
            optionBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.custom_btn_checked, null));
        } else {
            optionBtn.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            optionBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.custom_btn_unchecked, null));
        }
    }

    private void calculateTotalPrice() {
        float totalPrice, totalRoomPrice, totalGuestsPrice;
        totalRoomPrice = hotel.getMinPrice() * totalRoomsCount;
        totalGuestsPrice = hotel.getPricePerGuest() * (totalGuestsCount - 1);
        totalPrice = totalRoomPrice + totalGuestsPrice + getTotalJacuzziPrice()
                + getTotalSwimmingPrice() + getTotalMealsPrice();
        bookingPrice = totalPrice;
        showUpdatedBookingPrice();
    }

    private void showUpdatedBookingPrice() {
        book.setText(String.format("%s %s%s", getResources().getString(R.string.book),
                getResources().getString(R.string.dollar_symbol),
                bookingPrice));
    }

    private float getTotalJacuzziPrice() {
        if (jacuzzi) {
            return hotel.getJacuzziPerGuest() * totalGuestsCount;
        }
        return 0;
    }

    private float getTotalSwimmingPrice() {
        if (swimming) {
            return hotel.getSwimmingPerGuest() * totalGuestsCount;
        }
        return 0;
    }

    private float getTotalMealsPrice() {
        if (meals) {
            return hotel.getMealsPerGuest() * totalGuestsCount;
        }
        return 0;
    }

    private void setTotalGuests(int totalGuestsCount) {
        totalGuests.setText(String.format("%s%s", getText(R.string.total_guests), totalGuestsCount));
    }

    private void setTotalRooms(int totalRoomsCount) {
        totalRooms.setText(String.format("%s%s", getText(R.string.total_rooms), totalRoomsCount));
    }

    public String getHotels() {
        SharedPreferences sp = getSharedPreferences("hotel", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        if (sp.contains("data")) {
            return sp.getString("data", null);
        } else {
            return null;
        }

    }

    private void setBooking(Boolean complete) {
        UserHotel hotel1 = new UserHotel();
        hotel1.setName(hotel.getName());
        hotel1.setCompleted(complete);
        hotel1.setFavTags(hotel.getTags());

        int c;
        if (complete) {
            c = Integer.parseInt(hotel.getCompletedBookings());
            hotelResult.getHotels().get(pos).setCompletedBookings(String.valueOf(c + 1));
            if (draftBooking) {
                Hotel tempHotel = hotel;
                for (Hotel h : cartList) {
                    if (h.getName().equals(hotel.getName())) {
                        tempHotel = h;
                        break;
                    }
                }
                cartList.remove(tempHotel);
            }
        } else {
            c = Integer.parseInt(hotel.getDraftBookings());
            hotelResult.getHotels().get(pos).setDraftBookings(String.valueOf(c + 1));
            hotel1.setHotel(hotel);
            hotel1.setGuests(totalGuestsCount);
            hotel1.setRooms(totalRoomsCount);
            HashMap<String, Boolean> options = new HashMap<>();
            options.put("jacuzzi", jacuzzi);
            options.put("swimming", swimming);
            options.put("meals", meals);
            hotel1.setOptions(options);
        }

        for(int i=0; i<bookings.getUserHotels().size(); i++) {
            UserHotel uh = bookings.getUserHotels().get(i);
            if(uh.getName().equals(hotel1.getName())) {
                bookings.getUserHotels().set(i, hotel1);
            } else if(i==bookings.getUserHotels().size()-1) {
                bookings.getUserHotels().add(hotel1);
            }
        }

        if(bookings.getUserHotels().size()==0) {
            List<UserHotel> userHotels = bookings.getUserHotels();
            userHotels.add(hotel1);

            bookings.setUserHotels(userHotels);
        }

        Set<String> s = new HashSet<>();
        for (UserHotel userHotel : bookings.getUserHotels()) {
            if (userHotel.getCompleted()) {
                for (String ss : userHotel.getFavTags().split("\n")) {
                    ss = ss.replace("null", "");
                    s.add(ss);
                }

            }
        }
        StringBuilder sa = new StringBuilder();

        for (String sss : s) {
            sa.append(sss);
            Recommendation.tagSet.add(sss);
        }

        storeUpdates(hotelResult);

        if (isConnectionAvailable() && complete) {
            getPayment();
        } else if (complete) {
            toast("Network Unavailable. Try Again!");
        }

    }

    private void getPayment() {
        PayPalCheckout.start(
                createOrderActions -> {
                    ArrayList purchaseUnits = new ArrayList<>();
                    purchaseUnits.add(
                            new PurchaseUnit.Builder()
                                    .amount(
                                            new Amount.Builder()
                                                    .currencyCode(CurrencyCode.USD)
                                                    .value(Float.toString(bookingPrice))
                                                    .build()
                                    )
                                    .build()
                    );
                    Order order = new Order(
                            OrderIntent.CAPTURE,
                            new AppContext.Builder()
                                    .userAction(UserAction.PAY_NOW)
                                    .build(),
                            purchaseUnits
                    );
                    createOrderActions.create(order, orderId -> {
                    });
                },
                approval -> approval.getOrderActions().capture(result -> {
                    // Order successfully captured
                    toast("Booking Successful!");
                    Log.d("TAG", "getPayment: Success" + result);
                    finish();
                }),
                () -> {
                    // Optional callback for when a buyer cancels the payment
                    toast("Booking Failed!");
                },
                errorInfo -> {
                    // Optional error callback
                    toast("Error! Booking Failed.");
                }
        );
    }

    public void storeUpdates(HotelResult hotelResult) {
        SharedPreferences.Editor spe = getSharedPreferences(getString(R.string.SP_HOTEL_KEY), Context.MODE_PRIVATE).edit();
        String save = new Gson().toJson(hotelResult);
        spe.putString("data", save);
        spe.apply();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("MissingPermission")
    public boolean isConnectionAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
    }
}
