package hospital;

import utils.Utilities;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Classe Exame, correspondendo a um exame/tratamento que um paciente quer fazer
 * Contem um nome, um "improvement" que nos diz quanto ira variar a saude do paciente com este exame
 * e um tempo - o tempo que demora o exame.
 * Exame - improvement random
 * Tratamento - improvement positivo
 */
public class Exame {
    private String nome;
    private float improvement = 0; //quanto vai melhorar a Health do paciente
    private float tempo = 0; //tempo que demora [em milissegundos]

    public Exame(String nome) {
        this.nome = nome;

        for (Exame e: Utilities.defaultExames) {
            if(e.getNome().equals(nome)) {
                improvement = e.getImprovement();
                tempo = e.getTempo();
                break;
            }
        }
    }

    public Exame(String nome, float imp, float tempo) {
        this.nome = nome;
        if(imp == 0) {
            imp = ThreadLocalRandom.current().nextInt(-50, 100 + 1); //generate
        }
        this.improvement = imp;
        this.tempo = tempo;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Exame)) return false;

        Exame exame = (Exame) o;

        // cannot compare improvement, because of the range values in exams
        //if (Float.compare(exame.improvement, improvement) != 0) return false;
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
