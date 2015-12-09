package utils;

/**
 * Created by João on 09/12/2015.



Java Swing, 2nd Edition
By Marc Loy, Robert Eckstein, Dave Wood, James Elliott, Brian Cole
ISBN: 0-596-00408-7
Publisher: O'Reilly

// ListModelExample.java
//An example of JList with a DefaultListModel that we build up at runtime.
**/

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class DynamicList extends JPanel {

    JList list;

    DefaultListModel model;

    public DynamicList() {
        setLayout(new BorderLayout());
        model = new DefaultListModel();
        list = new JList(model);
        JScrollPane pane = new JScrollPane(list);


        add(pane, BorderLayout.NORTH);
        //add(addButton, BorderLayout.WEST);
        //add(removeButton, BorderLayout.EAST);
    }

    public void addMessage(String exame, String duracao, String entrada){
        model.addElement("Exame: "+exame);
        model.addElement("Duracao: "+duracao);
        model.addElement("Entrada: "+entrada);
        model.addElement("////////////////////////");
    }

    public void addMessage2(String exame, String duracao,String paciente, String estado){

        if(estado.equals("Ocupado")){
            model.addElement("Inicio: "+new Date().toString());
            model.addElement("Paciente: "+paciente);
            model.addElement("Exame: "+exame);
            model.addElement("Duracao: "+duracao);
            model.addElement("////////////////////////");
        }
        else{
            model.addElement("Livre: "+ new Date().toString());
            model.addElement("////////////////////////");
        }
    }

    public void deleteMessage(int id){
        model.remove(id);
    }
}