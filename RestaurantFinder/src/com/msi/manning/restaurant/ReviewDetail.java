package com.msi.manning.restaurant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.msi.manning.restaurant.data.Review;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Show Review detail for review item user selected.
 * 
 * @author charliecollins
 */
public class ReviewDetail extends Activity {

    private static final String CLASSTAG = ReviewDetail.class.getSimpleName();
    private static final int MENU_CALL_REVIEW = Menu.FIRST + 2;
    private static final int MENU_MAP_REVIEW = Menu.FIRST + 1;
    private static final int MENU_WEB_REVIEW = Menu.FIRST; 
    
    private String imageLink;
    private String link;
    private TextView location;
    private TextView name;
    private TextView phone;
    private TextView rating;
    private TextView review;
    private ImageView reviewImage;
    
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if ((imageLink != null) && !imageLink.equals("")) {
                try {
                    URL url = new URL(imageLink);
                    URLConnection conn = url.openConnection();
                    conn.connect();
                    BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                    Bitmap bm = BitmapFactory.decodeStream(bis);
                    bis.close();
                    reviewImage.setImageBitmap(bm);
                } catch (IOException e) {
                    Log.e(Constants.LOGTAG, " " + ReviewDetail.CLASSTAG, e);
                }
            } else {
                reviewImage.setImageResource(R.drawable.no_review_image);
            }
        }
    };
    /* Creating ReviewDetail
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(Constants.LOGTAG, " " + ReviewDetail.CLASSTAG + " onCreate");
        // inflate layout
        setContentView(R.layout.review_detail);
        // reference XML defined views that we will touch in code
        name = (TextView) findViewById(R.id.name_detail);
        name.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scaler));
        rating = (TextView) findViewById(R.id.rating_detail);
        location = (TextView) findViewById(R.id.location_detail);
        phone = (TextView) findViewById(R.id.phone_detail);
        review = (TextView) findViewById(R.id.review_detail);
        reviewImage = (ImageView) findViewById(R.id.review_image);
        // get the current review from the Application (global state placed there)
        RestaurantFinderApplication application = (RestaurantFinderApplication) getApplication();
        Review currentReview = application.getCurrentReview();
        link = currentReview.link;
        imageLink = currentReview.imageLink;
        name.setText(currentReview.name);
        rating.setText(currentReview.rating);
        location.setText(currentReview.location);
        review.setText(currentReview.content);
        if ((currentReview.phone != null) && !currentReview.phone.equals("")) {
            phone.setText(currentReview.phone);
        } else {
            phone.setText("NA");
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.v(Constants.LOGTAG, " " + ReviewDetail.CLASSTAG + " onResume");
        // tell handler to load image
        handler.sendEmptyMessage(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, ReviewDetail.MENU_WEB_REVIEW, 0, R.string.menu_web_review).setIcon(
            android.R.drawable.ic_menu_info_details);
        menu.add(0, ReviewDetail.MENU_MAP_REVIEW, 1, R.string.menu_map_review).setIcon(
            android.R.drawable.ic_menu_mapmode);
        menu.add(0, ReviewDetail.MENU_CALL_REVIEW, 2, R.string.menu_call_review).setIcon(
            android.R.drawable.ic_menu_call);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case MENU_WEB_REVIEW:
                Log.v(Constants.LOGTAG, " " + ReviewDetail.CLASSTAG + " WEB - " + link);
                if ((link != null) && !link.equals("")) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    startActivity(intent);
                } else {
                    new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.alert_label)).setMessage(
                        R.string.no_link_message).setPositiveButton("Continue", new OnClickListener() {

                        public void onClick(final DialogInterface dialog, final int arg1) {
                        }
                    }).show();
                }
                return true;
            case MENU_MAP_REVIEW:
                Log.v(Constants.LOGTAG, " " + ReviewDetail.CLASSTAG + " MAP ");
                if ((location.getText() != null) && !location.getText().equals("")) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri
                        .parse("geo:0,0?q=" + location.getText().toString()));
                    startActivity(intent);
                } else {
                    new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.alert_label)).setMessage(
                        R.string.no_location_message).setPositiveButton("Continue", new OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int arg1) {
                        }
                    }).show();
                }
                return true;
            case MENU_CALL_REVIEW:
                Log.v(Constants.LOGTAG, " " + ReviewDetail.CLASSTAG + " PHONE ");
                if ((phone.getText() != null) && !phone.getText().equals("")
                    && !phone.getText().equals("NA")) {
                    Log
                        .v(Constants.LOGTAG, " " + ReviewDetail.CLASSTAG + " phone - "
                            + phone.getText().toString());
                    String phoneString = parsePhone(phone.getText().toString());
                    intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneString));
                    startActivity(intent);
                } else {
                    new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.alert_label)).setMessage(
                        R.string.no_phone_message).setPositiveButton("Continue", new OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int arg1) {
                        }
                    }).show();
                }
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }    
    
    private String parsePhone(final String p) {
        String tempP = p;
        tempP = tempP.replaceAll("\\D", "");
        tempP = tempP.replaceAll("\\s", "");
        return tempP.trim();
    }
}
