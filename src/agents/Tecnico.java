package agents;

import jade.content.*;
import jade.core.Agent;

import java.util.ArrayList;

/**
 * Created by Jo�o on 05/10/2015.
 */
public class Tecnico extends Agent {
    int _id;
    boolean _disponivel;
    String _nome;
    ArrayList<TipoExame> _especialidades;
}
