import java.time.LocalDate;
public class Entrega {
    private final Usuario aluno;
    private final String resposta;
    private final LocalDate data;
    public Entrega(Usuario aluno,String resposta,LocalDate data){this.aluno=aluno;this.resposta=resposta;this.data=data;}
    public Usuario getAluno(){return aluno;}
    public String getResposta(){return resposta;}
    public LocalDate getData(){return data;}
}
