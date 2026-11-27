public class Projeto {
    private final int id;
    private final String nome, descricao;
    private final Equipe equipe;
    private Usuario mentor;
    private boolean publico=true;
    public Projeto(int id,String nome,String descricao,Equipe equipe){this.id=id;this.nome=nome;this.descricao=descricao;this.equipe=equipe;}
    public int getId(){return id;} public String getNome(){return nome;} public String getDescricao(){return descricao;} public Equipe getEquipe(){return equipe;} public Usuario getMentor(){return mentor;}
    public boolean isPublico(){return publico;}
    void privacidade(boolean publico){this.publico=publico;}
    void vincularMentor(Usuario professor){if(mentor!=null)throw new IllegalArgumentException("Projeto ja tem mentor.");mentor=professor;}
}
