package agents;

import jade.core.*;

import java.util.ArrayList;

/**
 * Created by João on 05/10/2015.
 */
public class Paciente extends Agent {
    int _id;
    String _nome;
    TipoUrgencia _urgencia;
    ArrayList<TipoExame> _exame;

}

enum TipoUrgencia{
    Vermelho, Amarelo, Verde
}
