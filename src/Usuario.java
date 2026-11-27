public class Usuario {
    public enum Perfil { ALUNO, PROFESSOR }
    private final int id;
    private final String nome;
    private final Perfil perfil;
    private String email="", login="", senhaHash="", codigo="";
    public Usuario(int id, String nome, Perfil perfil) { this.id=id; this.nome=nome; this.perfil=perfil; }
    void credenciais(String email,String login,String senhaHash,String codigo){this.email=email;this.login=login;this.senhaHash=senhaHash;this.codigo=codigo;}
    String email(){return email;} String login(){return login;} String senhaHash(){return senhaHash;} String codigo(){return codigo;}
    public int getId(){return id;}
    public String getNome(){return nome;}
    public Perfil getPerfil(){return perfil;}
    public boolean isProfessor(){return perfil==Perfil.PROFESSOR;}
    @Override public String toString(){return id+" - "+nome+" ("+perfil+")";}
}
