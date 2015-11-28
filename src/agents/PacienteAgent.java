package agents;

import FIPA.DateTime;
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

import java.util.Date;
import java.util.Random;


public class PacienteAgent extends Agent {
    private float health;
    private ArrayList<Exame> exames;
    private boolean isSequencial; //true - exames tem que ser feitos por ordem
    private Date dataChegada = new Date();
    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");



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
            } else {
                isSequencial = false;
                health = 1;
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            System.out.println("Exiting");
            doDelete();
        }

        exames = new ArrayList<Exame>();
        System.out.println("Paciente "+ this.getAID().getName() + " with " + this.health + " health! And sequencial="+this.isSequencial);

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("preciso-exame");
        sd.setName("JADE-fazer-exame");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new OfferRequestsServer());


    }

    /**
     * Inner class OfferRequestsServer.
     * Este é o behaviour usado pelos pacientes para responder as "ofertas de exame" do hospital.
     * Se o exame que está disponível for um dos que o paciente necessita então este
     * responde com uma mensagem PROPOSE a especificar a sua urgência. Caso contrário é enviada de volta uma mensagem REFUSE .
     */
    private class OfferRequestsServer extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                // Message received. Process it
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();

                Random r = new Random();
                Integer urgencia = r.nextInt((1000 - 0) + 1);

                if (urgencia != null) {
                    // The requested book is available for sale. Reply with the price
                    reply.setPerformative(ACLMessage.PROPOSE);
                    //reply.setContent(String.valueOf(urgencia.intValue()));
                    reply.setContent(dateFormat.format(dataChegada));
                }
                else {
                    // The requested book is NOT available for sale.
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("nao-preciso-exame");
                }
                myAgent.send(reply);
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

    protected void register( DFAgentDescription dfd )
    {
        try {
            DFService.register(this, dfd );
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
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
        this.exames.remove(e);
    }

    public void removeFirstExame() { if(!this.exames.isEmpty()) this.exames.remove(0);}

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }


}
