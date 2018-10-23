package fractal.twitterservice;

/**
 * Created by vamshikirangullapelly on 22/10/2018.
 */

import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import junit.framework.Assert;
import org.junit.Test;

public class TwitterScannerTest {

    @Test(expected = IllegalArgumentException.class)
    public void testTwitterAuthNull() throws Exception {
        new TwitterScanner("Facebook", null);
    }

    /**
     * Tests the null Company Name
     */
    @Test(expected = IllegalArgumentException.class)
    public void testTwitterCPNull() throws Exception {
        new TwitterScanner(null, new OAuth1("", "", "", ""));
    }

    /**
     * Tests the empty Company Name
     */
    @Test(expected = IllegalArgumentException.class)
    public void testTwitterCPEmpty() throws Exception {
        new TwitterScanner("", new OAuth1("", "", "", ""));

    }

    /**
     * Tests a valid inputs
     */
    @Test
    public void testTwitterValidInput() throws Exception {
        String companyName = "Facebook";
        String consumerKey = "rt0VJDGgnJvC4yxnN82BP00dr";
        String consumerSecret = "XvC6mrYS4YqbZFDrJZyKpDX5CmMJqPnbnxpZSLG50yAtyCnIN6";
        String token = "1053205889778417665-A2NKILM25hPeyayTjLaLUM0rOiihD6";
        String secret = "fHlVMbI8m1t4BTokLoZ8wDHqsB2t16vMvbGVqQJw4NhHR";
        Authentication auth = new OAuth1(consumerKey, consumerSecret, token, secret);

        @SuppressWarnings("unused")
        TwitterScanner scanner = new TwitterScanner(companyName, auth);
        Assert.assertNotNull(scanner);
    }
}
