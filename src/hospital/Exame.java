package hospital;

import java.util.ArrayList;
import java.util.UUID;

public class Exame {
    String uniqueID;

    private String nome;
    private float improvement; //quanto vai melhorar a Health do paciente
    private float tempo; //tempo que demora [em milissegundos]

    public Exame(String nome) {
        this.nome = nome;
        improvement = 0;
        tempo = 10000;
        uniqueID = UUID.randomUUID().toString();
    }

    public Exame(String nome, float imp, float tempo) {
        this.nome = nome;
        this.improvement = imp;
        this.tempo = tempo;
        uniqueID = UUID.randomUUID().toString();
    }

    /*
     *   Getters and setters
     */
    public String getNome() {
        return nome;
    }

    public float getImprovement() {
        return improvement;
    }

    public void setImprovement(float improvement) {
        this.improvement = improvement;
    }

    public float getTempo() {
        return tempo;
    }

    public void setTempo(float tempo) {
        this.tempo = tempo;
    }

    public void setNome(String nome) {
        this.nome = nome;

    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Exame)) return false;

        Exame exame = (Exame) o;

        if (Float.compare(exame.improvement, improvement) != 0) return false;
        if (Float.compare(exame.tempo, tempo) != 0) return false;
        return nome.equals(exame.nome);

    }

    @Override
    public int hashCode() {
        int result = nome.hashCode();
        result = 31 * result + (improvement != +0.0f ? Float.floatToIntBits(improvement) : 0);
        result = 31 * result + (tempo != +0.0f ? Float.floatToIntBits(tempo) : 0);
        return result;
    }

    @Override
    public String toString() {
        return this.nome;
    }
}
