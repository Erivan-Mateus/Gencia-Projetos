import java.time.LocalDateTime;
public class Mentoria {
    public enum Status { PENDENTE, CONFIRMADA, RECUSADA, REAGENDAMENTO_PROPOSTO }
    private final int id;
    private final Projeto projeto;
    private final Usuario professor;
    private final String assunto;
    private LocalDateTime horario;
    private Status status;
    public Mentoria(int id,Projeto projeto,Usuario professor,String assunto,LocalDateTime horario,Status status){this.id=id;this.projeto=projeto;this.professor=professor;this.assunto=assunto;this.horario=horario;this.status=status;}
    public int getId(){return id;} public Projeto getProjeto(){return projeto;} public Usuario getProfessor(){return professor;} public String getAssunto(){return assunto;} public LocalDateTime getHorario(){return horario;} public Status getStatus(){return status;}
    void definirStatus(Status status){this.status=status;} void propor(LocalDateTime horario){this.horario=horario;status=Status.REAGENDAMENTO_PROPOSTO;}
}
