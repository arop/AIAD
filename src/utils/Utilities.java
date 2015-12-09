package utils;

import hospital.Exame;

import java.util.ArrayList;

public final class Utilities {
    public final static boolean FIRST_COME_FIRST_SERVE = false;
    public final static double DECREASE_RATE = 0.9;

    public final static ArrayList<Exame> defaultExames = new ArrayList<Exame>() {
        //Exame(String nome, float improvement, float tempo)
        //TODO change values!
        {
            add(new Exame("raio-x",10,5000));
            add(new Exame("ecografia",20,10000));
            add(new Exame("radiografia",-10,3000));
            add(new Exame("tac",30,5000));
        }
    };
}
