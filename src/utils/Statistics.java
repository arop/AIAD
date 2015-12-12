package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by João on 12/12/2015.
 */
public class Statistics {
    private static class StatisticsHolder {
        public static final Statistics instance = new Statistics();
    }
    private ArrayList<Long> pacienteLiveTimes;
    private Map<String,ArrayList<Long>> examesLiveTimes; //Exemplo -> ["Ecografia"][{s1,f1, s2,f2, s3,f3}]



    protected Statistics(){
        pacienteLiveTimes = new ArrayList<Long>();
        examesLiveTimes = new HashMap<String, ArrayList<Long>>();
    }


    public static Statistics getInstance(){
        return  StatisticsHolder.instance;
    }

    public long calculateAverageTimeOnHospital(){
        long totalTime = 0;
        for (int i =0; i < pacienteLiveTimes.size(); i++) {
            System.out.println("LOOP: " + pacienteLiveTimes.get(i));
            totalTime += pacienteLiveTimes.get(i).floatValue();
        }

        return (totalTime/pacienteLiveTimes.size());
    }

    public ArrayList<Long> getPacienteLiveTimes() {
        return pacienteLiveTimes;
    }
    public Map<String, ArrayList<Long>> getExamesLiveTimes(){
        return examesLiveTimes;
    }
}