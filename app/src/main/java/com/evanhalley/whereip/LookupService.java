package com.evanhalley.whereip;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.List;


/**
 * Service that processes IP lookup requests
 * Using an Intent Service to execute HTTP interaction code on a background thread, but in a service
 *   who's lifecycle is seprate from the calling client (Activity / Fragment / etc.)
 */
public class LookupService extends IntentService {
    private static final String TAG = "LookupService";

    private static final String ACTION_LOOKUP_IP = "com.evanhalley.whereip.action.lookupIp";
    public static final String ACTION_NEW_LOCATION = "com.evanhalley.whereip.newLocation";
    public static final String ACTION_PROCESSING_STARTED = "com.evanhalley.whereip.processingStarted";
    public static final String ACTION_PROCESSING_FINISHED = "com.evanhalley.whereip.processingFinished";

    private static final String EXTRA_PARAM_IP_RANGE_START = "com.evanhalley.whereip.extra.ipRangeStart";
    private static final String EXTRA_PARAM_IP_RANGE_END = "com.evanhalley.whereip.extra.ipRangeEnd";
    public static final String EXTRA_PARAM_LOCATION = "com.evanhalley.whereip.extra.location";

    /**
     * Helper function for starting the LookupService with an IP address lookup
     * @param context context
     * @param rangeStart IP address at the start of the range
     * @param rangeEnd IP address at the end of the range
     */
    public static void lookupIpRange(Context context, String rangeStart, String rangeEnd) {

        if (context == null) {
            throw new IllegalArgumentException("Context is null");
        }

        Intent intent = new Intent(context, LookupService.class);
        intent.setAction(ACTION_LOOKUP_IP);
        intent.putExtra(EXTRA_PARAM_IP_RANGE_START, rangeStart);
        intent.putExtra(EXTRA_PARAM_IP_RANGE_END, rangeEnd);
        context.startService(intent);
    }

    public LookupService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {
            final String action = intent.getAction();

            if (ACTION_LOOKUP_IP.equals(action)) {
                final String ipRangeStart = intent.getStringExtra(EXTRA_PARAM_IP_RANGE_START);
                final String ipRangeEnd = intent.getStringExtra(EXTRA_PARAM_IP_RANGE_END);
                performIpLookup(ipRangeStart, ipRangeEnd);
            }
        }
    }

    /**
     * Performs the IP lookup and broadcasts the location to the appropriate subscriber
     * @param ipRangeStart start of the IP range
     * @param ipRangeEnd end of the IP range
     */
    private void performIpLookup(String ipRangeStart, String ipRangeEnd) {
        List<String> ipAddresses = LookupHelper.getIpAddressesInRange(ipRangeStart, ipRangeEnd);

        if (ipAddresses != null && ipAddresses.size() > 0) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(
                    new Intent(ACTION_PROCESSING_STARTED));

            for (int i = 0; i < ipAddresses.size(); i++) {
                Log.i(TAG, String.format("Performing lookup of IP address %s", ipAddresses.get(i)));

                try {
                    Location location = LookupHelper.getLocationByIp(ipAddresses.get(i),
                            getString(R.string.ipinfo_key));
                    Intent intent = new Intent(ACTION_NEW_LOCATION);
                    intent.putExtra(EXTRA_PARAM_LOCATION, location);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                } catch (Exception e) {
                    Log.w(TAG, "Error occurred during IP address lookup", e);
                }
            }

            LocalBroadcastManager.getInstance(this).sendBroadcast(
                    new Intent(ACTION_PROCESSING_FINISHED));
        }
    }
}
