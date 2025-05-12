package gals;

public class InsereTabela {
    private String nome;
    private int tipo;

    // Construtor para inicializar os parâmetros
    public InsereTabela(int tipo, String nome) {
        this.tipo = tipo;
        this.nome = nome;
    }

    // Getters para acessar os valores armazenados
    public String getNome() {
        return nome;
    }

    public int getTipo() {
        return tipo;
    }

    // Método para exibir as informações armazenadas (opcional)
    @Override
    public String toString() {
        return "InsereTabela{" +
                "nome='" + nome + '\'' +
                ", tipo=" + tipo +
                '}';
    }
}