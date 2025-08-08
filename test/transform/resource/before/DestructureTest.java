import lombok.Destructure;

public class DestructureTest {

    public static void main(String[] args) {
        Pessoa pessoa = new Pessoa("João", 30);

        @Destructure({"nome", "idade"})
        Pessoa p = pessoa;

        System.out.println(p.getNome() + " tem " + p.getIdade() + " anos");
    }

    static class Pessoa {
        private final String nome;
        private final int idade;

        public Pessoa(String nome, int idade) {
            this.nome = nome;
            this.idade = idade;
        }

        public String getNome() { return nome; }
        public int getIdade() { return idade; }
    }
}
