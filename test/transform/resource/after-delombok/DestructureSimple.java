public class DestructureSimple {
    public void run() {
        Pessoa pessoa = new Pessoa("João", 30);

        @Data
        Pessoa p = pessoa;
        java.lang.Object nome = p.getNome();
        java.lang.Object idade = p.getIdade();
        System.out.println(nome + " tem " + idade);
    }

    static class Pessoa {
        private final String nome;
        private final int idade;

        Pessoa(String nome, int idade) {
            this.nome = nome;
            this.idade = idade;
        }

        public String getNome() {
            return nome;
        }

        public int getIdade() {
            return idade;
        }
    }
}