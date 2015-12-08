package agents;

import jade.core.Agent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import hospital.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import utils.Utilities;

import java.util.Date;

public class PacienteAgent extends Agent {
    private float health;
    private ArrayList<Exame> exames = new ArrayList<Exame>();
    private boolean isSequencial; //true - exames tem que ser feitos por ordem
    private Date dataChegada = new Date();
    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private Behaviour existir;

    @Override
    protected void setup() {
        super.setup();
        System.out.println("PacienteAgent.setup");
        System.out.println("Usage: Paciente(health=1,isSequencial=false)");

        try {
            // Get the title of the book to buy as a start-up argument
            Object[] args = getArguments();
            if (args != null && args.length > 0) {
                health = Float.valueOf((String) args[0]);
                isSequencial = Boolean.valueOf((String) args[1]);

                for(int i = 2; i < args.length; i++){
                    exames.add(new Exame((String) args[i]));
                }
            } else {
                isSequencial = false;
                health = 1;
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            System.out.println("Exiting");
            doDelete();
        }

        System.out.println("Paciente "+ this.getAID().getName() + " with " + this.health + " health! And sequencial="+this.isSequencial);

        //TODO para ja o paciente precisa de todos os exames default
        //Exame.initDefaultExames();
        //exames = Exame.getDefaultExames();

        // manda pedido para cada exame
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        // TODO mudar qd for para por pedidos sequenciais
        for (int i = 0; i < exames.size(); i++) {
            ServiceDescription sd = new ServiceDescription();
            //sd.setType("preciso-exame");
            sd.setType("preciso-exame-" + exames.get(i).getNome());
            sd.setName("JADE-fazer-exame");
            dfd.addServices(sd);
        }

        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }


        existir = new OfferRequestsServer();
        addBehaviour(existir);
    }

    /**
     * Inner class OfferRequestsServer.
     * Este é o behaviour usado pelos pacientes para responder as "ofertas de exame" do hospital.
     * Se o exame que está disponível for um dos que o paciente necessita então este
     * responde com uma mensagem PROPOSE a especificar a sua urgência. Caso contrário é enviada de volta uma mensagem REFUSE .
     */
    private class OfferRequestsServer extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = myAgent.blockingReceive();
            if (msg != null) {
                if (msg.getPerformative() == ACLMessage.CFP) {

                    // Message received. Process it
                    String exame = msg.getContent();
                    ACLMessage reply = msg.createReply();

                    System.out.println("PACIENTE ["+myAgent.getName()+"] => Recebe proposta de recurso para: " + exame);

                    reply.setPerformative(ACLMessage.PROPOSE);
                    String resposta;
                    if (Utilities.FIRST_COME_FIRST_SERVE)
                        resposta = dateFormat.format(dataChegada) + "\n" + getNextExam().getNome();
                    else resposta = String.valueOf(utilityFunction(new Exame(exame))) + "\n" + getNextExam().getNome();

                    reply.setContent(resposta);
                    System.out.println("PACIENTE ["+myAgent.getName()+"] =>  Envia proposta c/: " + resposta);

                    myAgent.send(reply);
                }
                else if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                    String[] exameSplit = msg.getContent().split("\n");
                    System.out.println("PACIENTE ["+myAgent.getName()+"] => Recurso aceitou fazer: "+exameSplit[0]);
                    System.out.println("PACIENTE ["+myAgent.getName()+"] => RESPOSTA: [0]=>"+exameSplit[0] + "[1]=>"+exameSplit[1]);
                    Exame e = new Exame(exameSplit[0]);
                    System.out.println("Definicoes do exame: " + e.getNome() + " ID MAL: " + e.getUniqueID());
                    e.setUniqueID(exameSplit[1]);
                    System.out.println(" ID UPDATED: " + e.getUniqueID());
                    removeExame(e);

                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.CONFIRM);
                    reply.setContent("entao vou fazer um:"+exameSplit[0]);
                    myAgent.send(reply);

                    if(exames.isEmpty()) {
                        takeDown();
                    }
                }
            }
        }
    } // End of inner class OfferRequestsServer


    // Put agent clean-up operations here
    protected void takeDown() {
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Printout a dismissal message
        System.out.println("Paciente-agent "+getAID().getName()+" terminating.");
    }

    public double utilityFunction(Exame exameToDo) {
        double value = 0;
        Date now = new Date();
        double elapsedTime = (now.getTime() - dataChegada.getTime()) / 1000.0; //in seconds

        // f(t) = at + (b/2)t^2; a = z - s
        // a: severidade, z: saude atingivel, s: saude inicial, b: taxa decrescimo

        double b = Utilities.DECREASE_RATE;

        double z = exameToDo.getImprovement();

        double a = z - this.getHealth();

        value = a*elapsedTime + (b/2.0) * Math.pow(elapsedTime,2);

        return value;
    }

    /*
     *   Getters and setters
     */
    public boolean isSequencial() {
        return isSequencial;
    }

    public void setSequencial(boolean sequencial) {
        isSequencial = sequencial;
    }

    public ArrayList<Exame> getExames() {
        return exames;
    }

    public void setExames(ArrayList<Exame> exames) {
        this.exames = exames;
    }

    public void addExame(Exame e) {
        this.exames.add(e);
    }

    public void removeExame(Exame e) {
        System.out.println("Antes de remover.." + this.exames.size());
        System.out.println("Remover da lista de exames do paciente o exame que já foi realizador...");
        System.out.println("Informações do exame realizado... Nome: " + e.getNome() +"\nID: "+ e.getUniqueID());
        this.exames.remove(e);
        System.out.println("Depois de remover.." + this.exames.size());

        /*for(Exame ex: this.exames){

            if(ex.getUniqueID().equals(e.getUniqueID())) {
                this.exames.remove(ex);
                System.out.print("Exame a ser removido ... " + ex.getNome() + " " + ex.getUniqueID());
                break;

            }
        }*/
    }

    public void removeFirstExame() { if(!this.exames.isEmpty()) this.exames.remove(0);}

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public Exame getNextExam() {
        return exames.get(0);
    }

}
