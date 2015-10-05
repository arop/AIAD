package agents;

import jade.core.*;

import java.util.ArrayList;

/**
 * Created by João on 05/10/2015.
 */
public class Exame extends Agent {
    TipoExame _tipo;
    float _tempo;
    ArrayList<Medico> _medicos;
    ArrayList<Tecnico> _tecnicos;
    Paciente _paciente;
}

enum TipoExame {
    Diagnostico, Ecografia, RaioX
}
