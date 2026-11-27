import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Regras de negócio independentes do console, reutilizáveis nas próximas sprints. */
public class SistemaEscolar {
    private final ArrayList<Usuario> usuarios=new ArrayList<>();
    private final ArrayList<String> codigos=new ArrayList<>();
    final Academico academico=new Academico(this);
    List<Usuario> usuariosInternos(){return usuarios;} List<String> codigosInternos(){return codigos;}
    public String emitirCodigo(Usuario u){professor(u);String c=Credenciais.codigo();codigos.add(c);return c;}
    public Usuario cadastrar(String perfil,String email,String login,String senha,String codigo){
        email=Credenciais.normal(email);login=login.trim();codigo=codigo.trim().toUpperCase(java.util.Locale.ROOT);
        exigir(email.length()<=254&&email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"),"E-mail inválido.");
        exigir(login.matches("[A-Za-z0-9_.-]{3,32}"),"Nome de usuário: 3 a 32 letras, números, ponto, hífen ou sublinhado.");
        exigir(senha.length()>=8&&senha.length()<=128,"Senha deve ter entre 8 e 128 caracteres.");
        for(Usuario x:usuarios)exigir(!x.email().equalsIgnoreCase(email)&&!x.login().equalsIgnoreCase(login),"E-mail ou nome de usuário já cadastrado.");
        Usuario.Perfil tipo=Usuario.Perfil.valueOf(perfil);if(tipo==Usuario.Perfil.ALUNO)exigir(codigos.contains(codigo),"Código de aluno inválido ou já utilizado.");else codigo="";
        int id=usuarios.stream().mapToInt(Usuario::getId).max().orElse(0)+1;Usuario u=new Usuario(id,login,tipo);u.credenciais(email,login,Credenciais.hash(senha),codigo);usuarios.add(u);if(tipo==Usuario.Perfil.ALUNO){codigos.remove(codigo);academico.vincularCadastro(u,codigo);}return u;
    }
    public Usuario autenticar(String perfil,String identificador,String senha){for(Usuario u:usuarios){boolean aluno=u.getPerfil()==Usuario.Perfil.ALUNO;String id=identificador.trim();if(u.getPerfil().name().equals(perfil)&&(aluno?u.codigo().equalsIgnoreCase(id):u.email().equalsIgnoreCase(id)||u.login().equalsIgnoreCase(id))&&!id.isEmpty()&&Credenciais.confere(senha,u.senhaHash()))return u;}return null;}
    public void transferirLider(Usuario u,Equipe e,Usuario novo){lider(u,e);aluno(novo);e.transferir(novo);}
    private final ArrayList<Atividade> atividades=new ArrayList<>();
    private final ArrayList<Equipe> equipes=new ArrayList<>();
    private final ArrayList<Projeto> projetos=new ArrayList<>();
    private final ArrayList<Mentoria> mentorias=new ArrayList<>();
    public SistemaEscolar(){
        usuarios.add(new Usuario(1,"Prof. Ana",Usuario.Perfil.PROFESSOR));
        usuarios.add(new Usuario(2,"Prof. Carlos",Usuario.Perfil.PROFESSOR));
        usuarios.add(new Usuario(3,"Alice",Usuario.Perfil.ALUNO));
        usuarios.add(new Usuario(4,"Bruno",Usuario.Perfil.ALUNO));
        usuarios.add(new Usuario(5,"Clara",Usuario.Perfil.ALUNO));
    }
    private void exigir(boolean ok,String mensagem){if(!ok)throw new IllegalArgumentException(mensagem);}
    private String texto(String s){exigir(s!=null&&!s.trim().isEmpty(),"Campo obrigatorio.");return s.trim();}
    private void conta(Usuario u){exigir(usuarios.contains(u),"Usuario invalido.");}
    private void professor(Usuario u){conta(u);exigir(u.isProfessor(),"Apenas professor pode realizar esta operacao.");}
    private void aluno(Usuario u){conta(u);exigir(!u.isProfessor(),"Apenas aluno pode realizar esta operacao.");}
    private void lider(Usuario u,Equipe e){aluno(u);exigir(equipes.contains(e)&&e.ehLider(u),"Apenas o lider da equipe pode realizar esta operacao.");}
    private void futuro(LocalDateTime h){exigir(h!=null&&h.isAfter(LocalDateTime.now()),"Horario deve estar no futuro.");}
    public List<Usuario> getUsuarios(){return List.copyOf(usuarios);}
    public Usuario usuario(int id){return usuarios.stream().filter(u->u.getId()==id).findFirst().orElseThrow(()->new IllegalArgumentException("Usuario inexistente."));}
    public Atividade atividade(int id){return atividades.stream().filter(a->a.getId()==id).findFirst().orElseThrow(()->new IllegalArgumentException("Atividade inexistente."));}
    public Equipe equipe(int id){return equipes.stream().filter(e->e.getId()==id).findFirst().orElseThrow(()->new IllegalArgumentException("Equipe inexistente."));}
    public Projeto projeto(int id){return projetos.stream().filter(p->p.getId()==id).findFirst().orElseThrow(()->new IllegalArgumentException("Projeto inexistente."));}
    public Mentoria mentoria(int id){return mentorias.stream().filter(m->m.getId()==id).findFirst().orElseThrow(()->new IllegalArgumentException("Mentoria inexistente."));}
    public Atividade cadastrarAtividade(Usuario u,String titulo,String materia,String descricao,LocalDate prazo){professor(u);exigir(prazo!=null&&!prazo.isBefore(LocalDate.now()),"Prazo nao pode estar no passado.");Atividade a=new Atividade(atividades.size()+1,texto(titulo),texto(materia),texto(descricao),prazo,u);atividades.add(a);return a;}
    public List<Atividade> atividadesDe(Usuario u){conta(u);return atividades.stream().filter(a->u.isProfessor()?a.getProfessor()==u:academico.podeVer(u,a.getTurma())).collect(Collectors.toList());}
    public void entregar(Usuario u,Atividade a,String resposta){aluno(u);exigir(atividades.contains(a)&&academico.podeVer(u,a.getTurma()),"Atividade indisponível para este aluno.");a.registrar(u,texto(resposta),LocalDate.now());}
    public List<Entrega> consultarEntregas(Usuario u,Atividade a){professor(u);exigir(atividades.contains(a)&&a.getProfessor()==u,"Professor nao responsavel pela atividade.");return a.getEntregas();}
    public Equipe criarEquipe(Usuario u,String nome,String descricao,String foto){aluno(u);Equipe e=new Equipe(equipes.size()+1,texto(nome),texto(descricao),foto==null?"":foto.trim(),u);equipes.add(e);return e;}
    public void editarEquipe(Usuario u,Equipe e,String nome,String descricao,String foto){lider(u,e);e.editar(texto(nome),texto(descricao),foto==null?"":foto.trim());}
    public void convidar(Usuario u,Equipe e,Usuario convidado){lider(u,e);aluno(convidado);e.convidar(convidado);}
    public void responderConvite(Usuario u,Equipe e,boolean aceitar){aluno(u);exigir(equipes.contains(e),"Equipe inexistente.");e.responder(u,aceitar);}
    public List<Equipe> equipesDe(Usuario u){aluno(u);return equipes.stream().filter(e->e.temMembro(u)).collect(Collectors.toList());}
    public List<Equipe> convitesDe(Usuario u){aluno(u);return equipes.stream().filter(e->e.getConvites().contains(u)).collect(Collectors.toList());}
    public Projeto criarProjeto(Usuario u,Equipe e,String nome,String descricao){lider(u,e);Projeto p=new Projeto(projetos.size()+1,texto(nome),texto(descricao),e);projetos.add(p);return p;}
    public List<Projeto> projetosDe(Usuario u){conta(u);return projetos.stream().filter(p->u.isProfessor()?p.getMentor()==u:p.getEquipe().temMembro(u)).collect(Collectors.toList());}
    public List<Projeto> projetosSemMentor(Usuario u){professor(u);return projetos.stream().filter(p->p.getMentor()==null).collect(Collectors.toList());}
    public void orientar(Usuario u,Projeto p){professor(u);exigir(projetos.contains(p),"Projeto inexistente.");p.vincularMentor(u);}
    public void privacidadeEquipe(Usuario u,Equipe e,boolean publica,boolean nome,boolean lider,boolean membros){lider(u,e);e.privacidade(publica,nome,lider,membros);}
    public void privacidadeProjeto(Usuario u,Projeto p,boolean publico){lider(u,p.getEquipe());exigir(projetos.contains(p),"Projeto inexistente.");p.privacidade(publico);}
    public boolean podeVerEquipe(Usuario u,Equipe e){conta(u);return u.isProfessor()||e.temMembro(u)||e.isPublica();}
    public boolean podeVerProjeto(Usuario u,Projeto p){conta(u);return u.isProfessor()||p.getEquipe().temMembro(u)||p.isPublico();}
    public List<Equipe> equipesVisiveis(Usuario u){return equipes.stream().filter(e->podeVerEquipe(u,e)).collect(Collectors.toList());}
    public List<Projeto> projetosVisiveis(Usuario u){return projetos.stream().filter(p->podeVerProjeto(u,p)).collect(Collectors.toList());}
    // Acesso interno para a persistência; nunca exposto diretamente pela API.
    List<Atividade> todasAtividades(){return atividades;} List<Equipe> todasEquipes(){return equipes;} List<Projeto> todosProjetos(){return projetos;} List<Mentoria> todasMentorias(){return mentorias;}
    private void semConflito(Projeto p,Usuario prof,LocalDateTime horario,Mentoria ignorar){for(Mentoria m:mentorias)if(m!=ignorar&&m.getStatus()==Mentoria.Status.CONFIRMADA&&m.getHorario().equals(horario)&&(m.getProfessor()==prof||m.getProjeto().getEquipe()==p.getEquipe()))throw new IllegalArgumentException("Conflito com mentoria confirmada neste horario.");}
    private Mentoria novaMentoria(Projeto p,Usuario prof,String assunto,LocalDateTime horario,Mentoria.Status status){futuro(horario);semConflito(p,prof,horario,null);Mentoria m=new Mentoria(mentorias.size()+1,p,prof,texto(assunto),horario,status);mentorias.add(m);return m;}
    public Mentoria solicitarMentoria(Usuario u,Projeto p,String assunto,LocalDateTime horario){lider(u,p.getEquipe());exigir(projetos.contains(p)&&p.getMentor()!=null,"Projeto precisa ter mentor.");return novaMentoria(p,p.getMentor(),assunto,horario,Mentoria.Status.PENDENTE);}
    public Mentoria agendarMentoria(Usuario u,Projeto p,String assunto,LocalDateTime horario){professor(u);exigir(projetos.contains(p)&&p.getMentor()==u,"Apenas o mentor do projeto pode agendar.");return novaMentoria(p,u,assunto,horario,Mentoria.Status.CONFIRMADA);}
    public List<Mentoria> mentoriasDe(Usuario u){conta(u);return mentorias.stream().filter(m->u.isProfessor()?m.getProfessor()==u:m.getProjeto().getEquipe().temMembro(u)).collect(Collectors.toList());}
    public void responderMentoria(Usuario u,Mentoria m,int decisao,LocalDateTime novo){professor(u);exigir(mentorias.contains(m)&&m.getProfessor()==u&&m.getStatus()==Mentoria.Status.PENDENTE,"Solicitacao pendente nao pertence a este professor.");switch(decisao){case 1:futuro(m.getHorario());semConflito(m.getProjeto(),u,m.getHorario(),m);m.definirStatus(Mentoria.Status.CONFIRMADA);break;case 2:m.definirStatus(Mentoria.Status.RECUSADA);break;case 3:futuro(novo);semConflito(m.getProjeto(),u,novo,m);m.propor(novo);break;default:throw new IllegalArgumentException("Decisao invalida.");}}
    public void responderReagendamento(Usuario u,Mentoria m,boolean aceitar){lider(u,m.getProjeto().getEquipe());exigir(mentorias.contains(m)&&m.getStatus()==Mentoria.Status.REAGENDAMENTO_PROPOSTO,"Nao ha proposta de reagendamento.");if(aceitar){futuro(m.getHorario());semConflito(m.getProjeto(),m.getProfessor(),m.getHorario(),m);}m.definirStatus(aceitar?Mentoria.Status.CONFIRMADA:Mentoria.Status.RECUSADA);}
}
