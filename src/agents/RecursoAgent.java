package agents;

import agents.PacienteAgent;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import java.util.ArrayList;
import hospital.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class RecursoAgent extends Agent {
    private PacienteAgent currentPaciente;
    private Exame currentExame;
    private ArrayList<Exame> examesPossiveis; //lista de exames que o recurso consegue tratar

    private String exame;

    private AID[] pacientes;

    @Override
    protected void setup() {
        super.setup();

        System.out.println("RecursoAgent.setup");
        System.out.println("Usage: Recurso(String nome_do_exame)");

        examesPossiveis = new ArrayList<Exame>();

        // Vai buscar o nome do exame ao argumento passado
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            exame = (String) args[0];
            System.out.println("Posso fazer o exame: " + exame);

            // Vai buscar todos os pacientes que precisam de um determinado exame
            // É preciso mudar para informar isto só quando estiver available e não tipo ciclo.
            addBehaviour(new TickerBehaviour(this, 60000) {
                protected void onTick() {
                    // De seguida é feito update da lista dos pacientes porque podem entrar pacientes a qq hora
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("preciso-exame");
                    template.addServices(sd);
                    try {
                        // O hospital vai procurar todos os pacientes que "ofereçam um serviço" do tipo "preciso-exame"
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        pacientes = new AID[result.length];
                        for (int i = 0; i < result.length; ++i) {
                            pacientes[i] = result[i].getName();
                        }
                    }
                    catch (FIPAException fe) {
                        fe.printStackTrace();
                    }
                    // Perform the request
                    myAgent.addBehaviour(new RequestPerformer());
                }
            } );

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
        public void action() {
            switch (step) {
                case 0:
                    // Mandar um cfp a todos os pacientes
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < pacientes.length; ++i) {
                        cfp.addReceiver(pacientes[i]);
                    }
                    cfp.setContent(exame);
                    cfp.setConversationId("oferta-exame");
                    cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);
                    // Prepare the template to get proposals
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("oferta-exame"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    // Recebe de todos os seus pacientes a sua urgencia
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Recebeu resposta
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            // Isto é uma bid
                            int urgencia = Integer.parseInt(reply.getContent());
                            if (maisUrgente == null || urgencia > maisUrgenteVal) {
                                // Escolhe a melhor "oferta", isto é o paciente mais urgente
                                maisUrgenteVal = urgencia;
                                maisUrgente = reply.getSender();
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
                        break;
                    }
                    break;
                case 2:
                    // Chamar o paciente para o exame segundo a melhor oferta
                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(maisUrgente);
                    order.setContent(exame);
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
                            System.out.println(exame+" feito com sucesso.");
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
            return ((step == 2 && maisUrgente == null) || step == 4);
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

