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
import utils.DynamicList;
import utils.Utilities;

import javax.swing.*;
import java.util.Date;

import static java.lang.Thread.sleep;

public class PacienteAgent extends Agent {
    private float health;
    private ArrayList<Exame> exames = new ArrayList<Exame>();
    private boolean isSequencial; //true - exames tem que ser feitos por ordem
    private Date dataChegada = new Date();
    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private Behaviour existir;
    private Boolean available = true;
    DFAgentDescription dfd = new DFAgentDescription();

    DynamicList List;

    private String pacienteName;

    @Override
    protected void setup() {
        super.setup();
        System.out.println("PacienteAgent.setup");
        System.out.println("Usage: Paciente(health=1,isSequencial=false,[exames]+)");

        pacienteName = this.getName().split("@")[0];

        //Criacao da GUI
        List = new DynamicList();
        JFrame frame = new JFrame(pacienteName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(List);
        frame.setSize(300, 250);
        frame.setVisible(true);

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

        dfd.setName(getAID());
        if(isSequencial) {
            // manda pedido para o primeiro exame
            ServiceDescription sd = new ServiceDescription();
            sd.setType("preciso-exame-" + exames.get(0).getNome());
            sd.setName("JADE-fazer-exame");
            dfd.addServices(sd);
        }
        else {
            // manda pedido para cada exame
            for (int i = 0; i < exames.size(); i++) {
                ServiceDescription sd = new ServiceDescription();
                sd.setType("preciso-exame-" + exames.get(i).getNome());
                sd.setName("JADE-fazer-exame");
                dfd.addServices(sd);
            }
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
            if (msg != null ) {
                if(available) {
                    if (msg.getPerformative() == ACLMessage.CFP ) {

                        // Message received. Process it
                        String exame = msg.getContent();
                        Exame e = new Exame(exame);
                        ACLMessage reply = msg.createReply();

                        String resposta;
                        if((exames.contains(e) && !isSequencial) || (isSequencial && exames.get(0).equals(e))) {
                            System.out.println("PACIENTE [" + pacienteName + "] => Recebe proposta de recurso para: " + exame);

                            reply.setPerformative(ACLMessage.PROPOSE);

                            if (Utilities.FIRST_COME_FIRST_SERVE)
                                resposta = dateFormat.format(dataChegada) + "\n" + e.getNome();
                            else resposta = String.valueOf(utilityFunction(e)) + "\n" + e.getNome();
                        }
                        else {
                            reply.setPerformative(ACLMessage.DISCONFIRM);
                            resposta = "Nao quero esse exame";
                        }
                        reply.setContent(resposta);
                        System.out.println("PACIENTE [" + pacienteName + "] =>  Envia proposta c/: " + resposta);

                        myAgent.send(reply);
                    }
                    else if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                        String exameSplit = msg.getContent();
                        System.out.println("PACIENTE ["+pacienteName+"] => Recurso aceitou fazer: "+exameSplit);
                        Exame e = new Exame(exameSplit);

                        if((exames.contains(e) && !isSequencial) || (isSequencial && exames.get(0).equals(e))) {
                            List.addMessage(e.getNome(),String.valueOf(e.getTempo()), new Date().toString());

                            System.out.println("PACIENTE [" + pacienteName + "] => RESPOSTA: " + exameSplit);
                            System.out.println("Definicoes do exame: " + e.getNome());

                            ACLMessage reply = msg.createReply();

                            reply.setPerformative(ACLMessage.INFORM);
                            System.out.println("PACIENTE [" + pacienteName + "] => Confirmou fazer o exame e vai bloquear " + e.getTempo() + "ms");
                            reply.setContent("entao vou fazer um:" + exameSplit);
                            myAgent.send(reply);
                            available = false;

                        } else {
                            ACLMessage reply = msg.createReply();
                            reply.setPerformative(ACLMessage.FAILURE);
                            reply.setContent("Nao pedi esse exame! [Exame: " + exameSplit + "]");
                            myAgent.send(reply);
                        }
                    }
                } else if(msg.getPerformative() == ACLMessage.INFORM) {
                    String exame = msg.getContent();
                    Exame e = new Exame(exame);

                    health += e.getImprovement();

                    if(!removeExame(e))
                        System.err.println("NAO REMOVEU EXAME");

                    available = true;

                    if (exames.isEmpty()) {
                        doDelete();
                    }

                    dfd = new DFAgentDescription();
                    dfd.setName(myAgent.getAID());

                    if(isSequencial && !exames.isEmpty()) {
                        // manda pedido para o proximo exame
                        ServiceDescription sd1 = new ServiceDescription();
                        sd1.setType("preciso-exame-" + exames.get(0).getNome());
                        sd1.setName("JADE-fazer-exame");

                        dfd.addServices(sd1);

                        try {
                            //modify faz overwrite
                            DFService.modify(myAgent, dfd);
                        } catch (FIPAException e1) {
                            e1.printStackTrace();
                        }
                    } else if(!isSequencial && !exames.contains(e)) {
                        System.out.println("PACIENTE ["+pacienteName+"] => vai remover exame do df: " + e.getNome());

                        for (Exame exame1 : exames) {
                            ServiceDescription sd1 = new ServiceDescription();
                            sd1.setType("preciso-exame-" + exame1.getNome());
                            sd1.setName("JADE-fazer-exame");
                            dfd.addServices(sd1);
                        }

                        try {
                            DFService.modify(myAgent, dfd);
                        } catch (FIPAException e1) {
                            e1.printStackTrace();
                        }
                    }
                } else {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REFUSE);
                    System.out.println("PACIENTE [" + pacienteName + "] =>  Estou ocupado");
                    myAgent.send(reply);
                }
            }
        }
    }

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
        double value;
        Date now = new Date();
        double elapsedTime = (now.getTime() - dataChegada.getTime()) / 1000.0; //in seconds

        // f(t) = at + (b/2)t^2; a = z - s
        // a: severidade, z: saude atingivel, s: saude inicial, b: taxa decrescimo

        double b = Utilities.DECREASE_RATE;

        double z = exameToDo.getImprovement();
        System.out.println("improvement ["+exameToDo.getNome()+"]: "+z);

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

    public boolean removeExame(Exame e) {
        System.out.println("Paciente ["+pacienteName+"] vai remover exame");
        System.out.println("Antes de remover.." + this.exames.size());
        System.out.println("Remover da lista de exames do paciente o exame que já foi realizado...");
        System.out.println("Informações do exame realizado... Nome: " + e.getNome());
        boolean x = this.exames.remove(e);
        System.out.println("Depois de remover.." + this.exames.size());
        return x;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

}
