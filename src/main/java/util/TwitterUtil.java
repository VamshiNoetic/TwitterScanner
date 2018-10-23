package util;

/**
 * Created by vamshikirangullapelly on 22/10/2018.
 */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class to provide some util methods
 */
public class TwitterUtil {

    /**
     * Method used to append the results to the mentioned file
     * @param fileName
     * @param line
     * @return
     */
    public static boolean appendToFile(String fileName, String line) {
        try {
            FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(line);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Method calculates the percentage change and returns the percentage value
     * @param prevVal
     * @param newVal
     * @return
     */
    public static double percentageChange(AtomicInteger prevVal, AtomicInteger newVal) {

        int newValue = newVal.intValue();
        int prevValue = prevVal.intValue();
        double change = 0;
        if (newValue >0 && prevValue >0 && newValue >= prevValue)
        {
            change =  ((newValue - prevValue) * 100)/prevValue;
        }
        return change;
       }


    }
