import java.util.ArrayList;
import java.util.List;
public class Equipe {
    private final int id;
    private String nome, descricao, foto;
    private boolean publica=true, nomePublico=true, liderPublico=true, membrosPublicos=true;
    private Usuario lider;
    private final ArrayList<Usuario> membros=new ArrayList<>(), convites=new ArrayList<>();
    public Equipe(int id,String nome,String descricao,String foto,Usuario lider){this.id=id;this.nome=nome;this.descricao=descricao;this.foto=foto;this.lider=lider;membros.add(lider);}
    public int getId(){return id;} public String getNome(){return nome;} public String getDescricao(){return descricao;} public String getFoto(){return foto;} public Usuario getLider(){return lider;}
    public List<Usuario> getMembros(){return List.copyOf(membros);} public List<Usuario> getConvites(){return List.copyOf(convites);}
    public boolean temMembro(Usuario u){return membros.contains(u);} public boolean ehLider(Usuario u){return lider==u;}
    public boolean isPublica(){return publica;} public boolean isNomePublico(){return nomePublico;} public boolean isLiderPublico(){return liderPublico;} public boolean isMembrosPublicos(){return membrosPublicos;}
    void privacidade(boolean publica,boolean nomePublico,boolean liderPublico,boolean membrosPublicos){this.publica=publica;this.nomePublico=nomePublico;this.liderPublico=liderPublico;this.membrosPublicos=membrosPublicos;}
    void transferir(Usuario novo){if(novo==lider||!membros.contains(novo))throw new IllegalArgumentException("Selecione outro integrante da equipe.");lider=novo;}
    void editar(String nome,String descricao,String foto){this.nome=nome;this.descricao=descricao;this.foto=foto;}
    void convidar(Usuario u){if(membros.contains(u)||convites.contains(u))throw new IllegalArgumentException("Aluno ja pertence a equipe ou possui convite.");convites.add(u);}
    void responder(Usuario u,boolean aceitar){if(!convites.remove(u))throw new IllegalArgumentException("Convite inexistente.");if(aceitar)membros.add(u);}
}
