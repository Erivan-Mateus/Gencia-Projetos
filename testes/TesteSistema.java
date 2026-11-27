import java.time.*;
public class TesteSistema {
    private static int total;
    static void verificar(boolean ok,String nome){if(!ok)throw new AssertionError(nome);System.out.println("OK - "+nome);total++;}
    static void negar(Runnable acao,String nome){try{acao.run();throw new AssertionError("Deveria recusar: "+nome);}catch(IllegalArgumentException e){verificar(true,nome+" ("+e.getMessage()+")");}}
    public static void main(String[] args){
        SistemaEscolar s=new SistemaEscolar();Usuario prof=s.usuario(1), outro=s.usuario(2), alice=s.usuario(3), bruno=s.usuario(4), clara=s.usuario(5);LocalDate hoje=LocalDate.now();LocalDateTime h=hoje.plusDays(3).atTime(14,0);
        verificar(s.getUsuarios().size()==5,"Contas de demonstracao");
        Atividade a=s.cadastrarAtividade(prof,"Pesquisa","Fisica","Leia e responda",hoje.plusDays(2));
        verificar(s.atividadesDe(alice).size()==1&&a.status(alice,hoje).equals("PENDENTE"),"Publicacao e consulta");
        negar(()->s.cadastrarAtividade(alice,"X","X","X",hoje),"Aluno nao publica atividade");
        negar(()->s.cadastrarAtividade(prof,"X","X","X",hoje.minusDays(1)),"Prazo passado");
        negar(()->s.cadastrarAtividade(prof," ","X","X",hoje),"Titulo vazio");
        s.entregar(alice,a,"Resposta A");verificar(a.status(alice,hoje).equals("ENTREGUE")&&a.status(bruno,hoje).equals("PENDENTE"),"Entregas individuais");
        negar(()->s.entregar(alice,a,"Outra resposta"),"Entrega duplicada");
        s.entregar(bruno,a,"Resposta B");verificar(s.consultarEntregas(prof,a).size()==2,"Professor consulta duas entregas");
        negar(()->s.consultarEntregas(outro,a),"Professor alheio nao consulta entregas");
        verificar(a.status(clara,hoje.plusDays(3)).equals("ATRASADA"),"Status atrasado");
        Atividade antiga=new Atividade(99,"Teste","Teste","Teste",hoje.minusDays(1),prof);antiga.registrar(clara,"Resposta",hoje);verificar(antiga.status(clara,hoje).equals("ENTREGUE COM ATRASO"),"Entrega tardia");
        Equipe e=s.criarEquipe(alice,"Eco","Projeto ambiental","foto.png");verificar(e.ehLider(alice)&&e.temMembro(alice),"Criacao com lider");
        s.convidar(alice,e,bruno);verificar(s.convitesDe(bruno).size()==1&&!e.temMembro(bruno),"Convite pendente");
        negar(()->s.convidar(alice,e,bruno),"Convite duplicado");
        negar(()->s.responderConvite(clara,e,true),"Nao convidado nao ingressa");
        s.responderConvite(bruno,e,true);verificar(e.temMembro(bruno)&&s.equipesDe(bruno).size()==1,"Aceitar convite");
        s.convidar(alice,e,clara);s.responderConvite(clara,e,false);verificar(!e.temMembro(clara)&&s.convitesDe(clara).isEmpty(),"Recusar convite");
        negar(()->s.editarEquipe(bruno,e,"X","X",""),"Integrante nao edita equipe");
        negar(()->s.convidar(bruno,e,clara),"Integrante nao convida");
        s.editarEquipe(alice,e,"EcoTech","Nova descricao","nova.png");verificar(e.getNome().equals("EcoTech")&&e.getFoto().equals("nova.png"),"Lider personaliza equipe");
        negar(()->s.criarProjeto(bruno,e,"X","X"),"Integrante nao cria projeto");
        Projeto p=s.criarProjeto(alice,e,"Horta","Horta inteligente");verificar(s.projetosDe(bruno).contains(p)&&s.projetosDe(clara).isEmpty(),"Projetos visiveis aos membros");
        negar(()->s.solicitarMentoria(alice,p,"X",h),"Mentoria sem mentor recusada");
        s.orientar(prof,p);verificar(s.projetosDe(prof).contains(p)&&p.getMentor()==prof,"Professor ingressa como mentor");
        negar(()->s.orientar(outro,p),"Mentor existente nao substituido");
        negar(()->s.solicitarMentoria(bruno,p,"X",h),"Integrante nao solicita mentoria");
        negar(()->s.solicitarMentoria(alice,p,"X",LocalDateTime.now().minusMinutes(1)),"Horario passado recusado");
        Mentoria m=s.solicitarMentoria(alice,p,"Planejamento",h);verificar(m.getStatus()==Mentoria.Status.PENDENTE&&s.mentoriasDe(prof).contains(m),"Solicitacao pendente visivel");
        negar(()->s.responderMentoria(outro,m,1,null),"Outro professor nao confirma");
        s.responderMentoria(prof,m,1,null);verificar(m.getStatus()==Mentoria.Status.CONFIRMADA&&s.mentoriasDe(bruno).contains(m),"Confirmacao visivel a equipe");
        negar(()->s.agendarMentoria(prof,p,"Conflito",h),"Conflito de horario exato");
        Mentoria m2=s.solicitarMentoria(alice,p,"Revisao",h.plusHours(1));s.responderMentoria(prof,m2,3,h.plusHours(2));verificar(m2.getStatus()==Mentoria.Status.REAGENDAMENTO_PROPOSTO,"Professor propoe horario");
        negar(()->s.responderReagendamento(bruno,m2,true),"Integrante nao aceita reagendamento");
        s.responderReagendamento(alice,m2,true);verificar(m2.getStatus()==Mentoria.Status.CONFIRMADA&&m2.getHorario().equals(h.plusHours(2)),"Lider confirma novo horario");
        Mentoria m3=s.solicitarMentoria(alice,p,"Outra",h.plusDays(1));s.responderMentoria(prof,m3,2,null);verificar(m3.getStatus()==Mentoria.Status.RECUSADA,"Professor recusa pedido");
        Mentoria m4=s.agendarMentoria(prof,p,"Reuniao",h.plusDays(2));verificar(m4.getStatus()==Mentoria.Status.CONFIRMADA,"Professor agenda diretamente");
        verificar(s.mentoriasDe(clara).isEmpty(),"Nao membro nao lista mentorias");
        System.out.println("TOTAL: "+total+" verificacoes aprovadas.");
    }
}
