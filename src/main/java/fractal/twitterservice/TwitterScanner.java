package fractal.twitterservice;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesSampleEndpoint;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.out;
import static util.TwitterUtil.appendToFile;
import static util.TwitterUtil.percentageChange;

/**
 * Created by vamshikirangullapelly on 22/10/2018.
 */
public class TwitterScanner {


    private static final String oauthConsumerKey;
    private static final String oauthToken;
    private static final String consumerSecret;
    private static final String accessTokenSecret;
    private static final String APP = "FractalStreamApp";
    private static final int PERIOD = 120000;
    private static final int DELAY = 0;
    private static final String COMPANY_NAME = "Facebook";
    private static AtomicInteger newVal = new AtomicInteger();
    private static AtomicInteger prevVal = new AtomicInteger();
    private static int flag = -1;
    private static final ResourceBundle rb = ResourceBundle.getBundle("twitter_config");
    private final Authentication mAuth;
    private Timer mTimer = new Timer();
    private volatile int mMentions = 0;
    private volatile int totalPrevMentions = 0;
    private String companyName;


    /**
     * This block will execute just after loading the class and pulls the config file to fetch the keys
     */
    static {
        oauthConsumerKey = rb.getString("consumerKey");
        oauthToken = rb.getString("accessToken");
        consumerSecret = rb.getString("consumerSecret");
        accessTokenSecret = rb.getString("accessTokenSecret");
        File file = new File("facebook_mention_count.dat");
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor which takes Authentication and companyName
     * @param companyName
     * @param auth
     */
    public TwitterScanner(String companyName, Authentication auth) {
        if (companyName == null) {
            throw new IllegalArgumentException("Company name null");
        } else if (auth == null) {
            throw new IllegalArgumentException("Authentication null");
        } else if (companyName.isEmpty()) {
            throw new IllegalArgumentException("No company name");
        } else {
            this.companyName = companyName;
            this.mAuth = auth;
        }
    }

    /**
     *
     * @param args
     */
    public static void main(String... args) {
        Authentication auth = new OAuth1(oauthConsumerKey, consumerSecret, oauthToken, accessTokenSecret);
        TwitterScanner scanner = new TwitterScanner(COMPANY_NAME, auth); // Change it to "the" for quick test
        scanner.run();
    }

    private void storeValue(TSValue value) {
        String message = value.getTimestamp().atZone(ZoneId.systemDefault()).toString()
                + ":\t\t\t";

        if (value.isIncreased) {
            appendToFile("facebook_mention_count.dat", message + " increase by " + String.valueOf(value.getVal()) + "%");
        } else {
            appendToFile("facebook_mention_count.dat", message + " decrease by " + String.valueOf(value.getVal()) + "%");
        }
    }

    public void run() {
        Timer timer = new Timer();
        timer.schedule(new TimerTaskExecutor(), DELAY, PERIOD);
        // Start Calculating text mentions. Every hour and the change in percentage
        TimerTask task = new TimerTask() {
            private int prvMentions = 0;

            public void run() {

                TSValue value = new TSValue(Instant.now(), (prvMentions == 0) ? 100.0 : 100.0 * (mMentions - prvMentions) / prvMentions, (prvMentions == 0) ? true : false);
                storeValue(value);

                // Reset the mention counters
                prvMentions = mMentions;
                mMentions = 0;
            }
        };

        // Start Scheduling
        mTimer.scheduleAtFixedRate(task, 0, TimerUnit.H1.getValue());

        // Setup loop for scanning twitter msgs
        try {
            // Set up your blocking queues: Be sure to size these properly based on expected
            // TPS of your stream
            BlockingQueue<String> msgQueue = new LinkedBlockingQueue<>(100000);
            BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>(1000);

            // Declare the the endpoint
            StatusesSampleEndpoint hbEndpoint = new StatusesSampleEndpoint();
            hbEndpoint.stallWarnings(false);

            // Creating a client:
            ClientBuilder builder = new ClientBuilder()
                    .name(APP)
                    .hosts(Constants.STREAM_HOST)
                    .authentication(mAuth)
                    .endpoint(hbEndpoint)
                    .processor(new StringDelimitedProcessor(msgQueue))
                    .eventMessageQueue(eventQueue);
            // No client events

            BasicClient hbClient = builder.build();

            // Attempts to establish a connection.
            hbClient.connect();

            for (int msgRead = 0; msgRead < 1000; msgRead++) {
                if (hbClient.isDone()) {
                    out.println(hbClient.getExitEvent().getMessage());
                    break;
                }
                String msg = msgQueue.poll(50, TimeUnit.SECONDS);
                if (msg == null) continue;
                JSONTokener jsTokener = new JSONTokener(msg);
                JSONObject payload = (JSONObject) jsTokener.nextValue();
                try {
                    String text = payload.getString("text");

                    if (text.contains(companyName)) {
                        mMentions++;
                    }
                } catch (JSONException e) {
                    continue;
                }
            }
            // Close the connection
            hbClient.stop();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public enum TimerUnit {
        H1(60 * 60 * 1000);

        private long mValue;

        TimerUnit(long value) {
            mValue = value;
        }

        public long getValue() {
            return mValue;
        }
    }

    public static class TSValue {
        private Instant timestamp;
        private double val;
        private boolean isIncreased;

        public TSValue(Instant timestamp, double val, boolean isIncreased) {
            this.timestamp = timestamp;
            this.val = val;
            this.isIncreased = isIncreased;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public double getVal() {
            return val;
        }
    }

    private class TimerTaskExecutor extends TimerTask {
        public void run() {
            newVal.set(mMentions - totalPrevMentions);

            TSValue tsValue;

            if (prevVal.intValue() == 0 || (newVal.intValue() > prevVal.intValue())) {
                tsValue = new TSValue(Instant.now(), percentageChange(prevVal, newVal), true);

            } else {
                tsValue = new TSValue(Instant.now(), percentageChange(newVal, prevVal), false);

            }

            totalPrevMentions = newVal.intValue() - prevVal.intValue();

            prevVal.set(newVal.intValue());

            newVal.set(0);

            if (flag > 0)

                storeValue(tsValue);
            else
                flag++;
        }
    }
}

