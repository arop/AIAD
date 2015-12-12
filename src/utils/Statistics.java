package utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by João on 12/12/2015.
 */
public class Statistics {
    private static class StatisticsHolder {
        public static final Statistics instance = new Statistics();
    }
    private ArrayList<Long> pacienteLiveTimes;
    private Map<String,ArrayList<Double>> examesLiveTimes; //Exemplo -> ["Ecografia"][{st,e1,e2,e3,e4}]



    protected Statistics(){
        pacienteLiveTimes = new ArrayList<Long>();
        examesLiveTimes = new HashMap<String, ArrayList<Double>>();
    }


    public static Statistics getInstance(){
        return  StatisticsHolder.instance;
    }

    public long calculateAverageTimeOnHospital(){
        long totalTime = 0;
        for (int i =0; i < pacienteLiveTimes.size(); i++) {
            totalTime += pacienteLiveTimes.get(i).floatValue();
        }

        return (totalTime/pacienteLiveTimes.size());
    }

    public long calculateAverageWaitingOnRoom(String id){
        ArrayList<Double> tempos = examesLiveTimes.get(id);
        long totalElapsedTimeOnExame = 0;
        long totalElapsedTime = 0;
        Date startingTime = new Date(tempos.get(0).longValue());
        for (int i=1; i < tempos.size(); i++) {
            totalElapsedTimeOnExame+= tempos.get(i).floatValue();
        }
        totalElapsedTime = TimeUnit.MILLISECONDS.toSeconds( new Date().getTime() - startingTime.getTime());
        totalElapsedTimeOnExame = TimeUnit.MILLISECONDS.toSeconds(totalElapsedTimeOnExame);

        return (totalElapsedTime-totalElapsedTimeOnExame)/(tempos.size()-1);

    }

    public ArrayList<Long> getPacienteLiveTimes() {
        return pacienteLiveTimes;
    }
    public Map<String, ArrayList<Double>> getExamesLiveTimes(){
        return examesLiveTimes;
    }
}