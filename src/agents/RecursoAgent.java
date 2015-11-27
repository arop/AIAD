package agents;

import agents.PacienteAgent;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import java.util.ArrayList;

import hospital.*;

/**
 * Created by andre on 27/11/2015.
 */
public class RecursoAgent extends Agent {
    private PacienteAgent currentPaciente;
    private Exame currentExame;
    private ArrayList<Exame> examesPossiveis; //lista de exames que o recurso consegue tratar

    @Override
    protected void setup() {
        super.setup();

        examesPossiveis = new ArrayList<Exame>();

        //adicionar so depois de ter paciente
        addBehaviour(new WakerBehaviour(this, (long) currentExame.getTempo()) {
            protected void onWake() {
                // perform operation X
                //demora x tempo para fazer o exame, depois acorda e modifica a health do paciente
                currentPaciente.setHealth(currentPaciente.getHealth() + currentExame.getImprovement());
                currentPaciente.removeFirstExame();
                setCurrentPaciente(null);
                setCurrentExame(null);
            }
        } );
    }

    /*
     *   Getters and setters
     */
    public PacienteAgent getCurrentPaciente() {
        return currentPaciente;
    }

    public void setCurrentPaciente(PacienteAgent currentPaciente) {
        this.currentPaciente = currentPaciente;
    }

    public boolean isAvailable() {
        if(this.currentPaciente == null) return true;
        return false;
    }

    public ArrayList<Exame> getExamesPossiveis() {
        return examesPossiveis;
    }

    public void setExamesPossiveis(ArrayList<Exame> examesPossiveis) {
        this.examesPossiveis = examesPossiveis;
    }

    public Exame getCurrentExame() {
        return currentExame;
    }

    public void setCurrentExame(Exame currentExame) {
        this.currentExame = currentExame;
    }
}
