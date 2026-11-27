import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
public class Atividade {
    private final int id;
    private int turma;
    public int getTurma(){return turma;} void definirTurma(int id){turma=id;}
    private final String titulo, materia, descricao;
    private final LocalDate prazo;
    private final Usuario professor;
    private final ArrayList<Entrega> entregas=new ArrayList<>();
    public Atividade(int id,String titulo,String materia,String descricao,LocalDate prazo,Usuario professor){this.id=id;this.titulo=titulo;this.materia=materia;this.descricao=descricao;this.prazo=prazo;this.professor=professor;}
    public int getId(){return id;} public String getTitulo(){return titulo;} public String getMateria(){return materia;} public String getDescricao(){return descricao;} public LocalDate getPrazo(){return prazo;} public Usuario getProfessor(){return professor;}
    public List<Entrega> getEntregas(){return List.copyOf(entregas);}
    public Entrega entregaDe(Usuario aluno){for(Entrega e:entregas)if(e.getAluno()==aluno)return e;return null;}
    public String status(Usuario aluno,LocalDate hoje){Entrega e=entregaDe(aluno);if(e!=null)return e.getData().isAfter(prazo)?"ENTREGUE COM ATRASO":"ENTREGUE";return hoje.isAfter(prazo)?"ATRASADA":"PENDENTE";}
    void registrar(Usuario aluno,String resposta,LocalDate data){if(entregaDe(aluno)!=null)throw new IllegalArgumentException("Esta atividade ja foi entregue por este aluno.");entregas.add(new Entrega(aluno,resposta,data));}
}
