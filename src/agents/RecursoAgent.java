package agents;

import jade.core.Agent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import hospital.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
//import jade.core.behaviours.WakerBehaviour;

public class RecursoAgent extends Agent {
    private PacienteAgent currentPaciente;
    private Exame currentExame;
    private ArrayList<Exame> examesPossiveis; //lista de exames que o recurso consegue tratar

    private AID[] pacientes;

    @Override
    protected void setup() {
        super.setup();

        System.out.println("RecursoAgent.setup");
        System.out.println("Usage: Recurso([String nome_do_exame]*)");

        examesPossiveis = new ArrayList<Exame>();

        // Vai buscar o nome do exame ao argumento passado
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            for (int i = 0; i< args.length;i++) {
                examesPossiveis.add(new Exame((String) args[i]));
                System.out.println("Posso fazer o exame: " + examesPossiveis.get(i).getNome());
            }

            // Vai buscar todos os pacientes que precisam de um determinado exame
            // TODO É preciso mudar para informar isto só quando estiver available e não tipo ciclo.
            if(currentExame == null)
                addBehaviour(new TickerBehaviour(this, 10000) {
                    protected void onTick() {
                        for (int i = 0; i < examesPossiveis.size(); i++) {
                            // De seguida é feito update da lista dos pacientes porque podem entrar pacientes a qq hora
                            DFAgentDescription template = new DFAgentDescription();
                            ServiceDescription sd = new ServiceDescription();
                            sd.setType("preciso-exame-"+examesPossiveis.get(i).getNome());
                            template.addServices(sd);
                            try {
                                // O hospital vai procurar todos os pacientes que "ofereçam um serviço" do tipo "preciso-exame"
                                DFAgentDescription[] result = DFService.search(myAgent, template);
                                pacientes = new AID[result.length];
                                for (int j = 0; j < result.length; j++) {
                                    pacientes[j] = result[j].getName();
                                }
                            }
                            catch (FIPAException fe) {
                                fe.printStackTrace();
                            }
                        }

                        // Perform the request
                        // apenas se alguem precisar dos exames que eu forneço
                        if(pacientes.length > 0)
                            myAgent.addBehaviour(new RequestPerformer());
                    }
                });

        }
        else {
            // Make the agent terminate
            System.out.println("Não existe esse tipo de exame");
            doDelete();
        }
    }

    // Put agent clean-up operations here
    protected void takeDown() {
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        System.out.println("Recurso-agent "+getAID().getName()+" terminating.");
    }

    private class RequestPerformer extends Behaviour {
        private AID maisUrgente; // O agente paciente que faz a bid mais alta (ie que tem mais urgência)
        private int maisUrgenteVal; // O valor da maior oferta
        private int repliesCnt = 0; // O número de respostas de pacientes
        private MessageTemplate mt; // O template para receber respostas
        private int step = 0;
        private Date maisAntigoVal;
        private AID maisAntigo;

        public void action() {
            switch (step) {
                case 0:
                    for (int i = 0; i < examesPossiveis.size(); i++) {
                        // Mandar um cfp a todos os pacientes
                        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                        for (int j = 0; j < pacientes.length; j++) {
                            cfp.addReceiver(pacientes[j]);
                        }
                        cfp.setContent(examesPossiveis.get(i).getNome());
                        cfp.setConversationId("oferta-exame");
                        cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
                        myAgent.send(cfp);
                        // Prepare the template to get proposals
                        //TODO n sei mudar isto
                        mt = MessageTemplate.and(MessageTemplate.MatchConversationId("oferta-exame"),
                                MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    }
                    step = 1;
                    break;
                case 1:
                    // Recebe de todos os seus pacientes a sua urgencia/data de inicio
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Recebeu resposta
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            // Isto é uma bid
                            //int urgencia = Integer.parseInt(reply.getContent());

                            Date dataChegada = new Date();
                            String[] resposta = new String[] {};
                            try {
                                resposta = reply.getContent().split("\n");
                                dataChegada = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(resposta[0]);
                                System.out.println(dataChegada);
                                System.out.println("reposta com exame: " + resposta[1]);
                            }catch(Exception e) {
                                System.out.println(e.getMessage());
                            }

                            /*// Escolhe o mais urgente
                            if (maisUrgente == null || urgencia > maisUrgenteVal) {
                                //Escolhe a melhor "oferta", isto é o paciente mais urgente
                                maisUrgenteVal = urgencia;
                                maisUrgente = reply.getSender();
                            }*/


                            // Escolhe o 1º que chegou - mais antigo
                            if (maisAntigo == null || !dataChegada.before(maisAntigoVal)) {
                                maisAntigoVal = dataChegada;
                                maisAntigo = reply.getSender();
                                currentExame = new Exame(resposta[1]);
                            }

                        }
                        repliesCnt++;

                        if (repliesCnt >= pacientes.length) {
                            // Já foram recebidas todas as respostas
                            step = 2;
                        }
                    }
                    else {
                        block();
                    }
                    break;
                case 2:
                    // Chamar o paciente para o exame segundo a melhor oferta
                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    //System.out.println(maisAntigo.toString());
                    order.addReceiver(maisAntigo);
                    System.out.println("paciente exame: " + currentExame.toString());
                    order.setContent(currentExame.toString());
                    order.setConversationId("oferta-exame");
                    order.setReplyWith("order"+System.currentTimeMillis());
                    myAgent.send(order);
                    // Prepare the template to get the purchase order reply
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("oferta-exame"),
                            MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                    step = 3;
                    break;
                case 3:
                    // Receber a resposta ao chamamento
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Purchase order reply received
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            // Purchase successful. We can terminate
                            System.out.println(currentExame+" feito com sucesso.");
                            System.out.println("Urgencia = "+maisUrgenteVal);
                            myAgent.doDelete();
                        }
                        step = 4;
                    }
                    else {
                        block();
                    }
                    break;
            }
        }
        public boolean done() {
            return ((step == 2 && (maisUrgente == null && maisAntigo == null)) || step == 4);
        }
    } // End of inner class RequestPerformer*/

    //addBehaviour(new OfferRequestsServer());

    //adicionar so depois de ter paciente
 /*       addBehaviour(new WakerBehaviour(this, (long) currentExame.getTempo()) {
            protected void onWake() {
                // perform operation X
                //demora x tempo para fazer o exame, depois acorda e modifica a health do paciente
                currentPaciente.setHealth(currentPaciente.getHealth() + currentExame.getImprovement());
                currentPaciente.removeFirstExame();
                setCurrentPaciente(null);
                setCurrentExame(null);
            }
        } );
        */


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

