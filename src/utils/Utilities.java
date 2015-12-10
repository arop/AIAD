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
            // EXAMES (iniciar com improvement = 0)
            add(new Exame("raio-x",0,5000));
            add(new Exame("ecografia",0,10000));
            add(new Exame("radiografia",0,3000));
            add(new Exame("tac",0,7000));

            // TRATAMENTOS
            add(new Exame("quimioterapia",100,20000));
            add(new Exame("engessar",50,20000));
            add(new Exame("cirurgia1",100,50000));
            add(new Exame("cirurgia2",80,45000));
            add(new Exame("cirurgia3",70,40000));
        }
    };
}
