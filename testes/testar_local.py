import pathlib,subprocess,tempfile,socket,time,json,urllib.request,urllib.error,datetime
ROOT=pathlib.Path(__file__).resolve().parents[1]
with socket.socket() as sock:sock.bind(('127.0.0.1',0));port=sock.getsockname()[1]
url=f'http://127.0.0.1:{port}'
count=0
def check(ok,msg):
 global count
 assert ok,msg
 count+=1;print(f'PASS {count:02}: {msg}',flush=True)
def req(path,data=None,token=None,expected=200):
 headers={}
 if token:headers['Authorization']='Bearer '+token
 if data is not None:headers['Content-Type']='application/json'
 q=urllib.request.Request(url+'/api/'+path,data=None if data is None else json.dumps(data).encode(),headers=headers)
 try:
  with urllib.request.urlopen(q,timeout=5) as r:status=r.status;v=json.load(r)
 except urllib.error.HTTPError as e:status=e.code;v=json.load(e)
 assert status==expected,(path,status,expected,v)
 return v
def login(role,id,senha='Piloto123!',expected=200):return req('login',dict(perfil=role,identificador=id,senha=senha),expected=expected)
def act(token,acao,expected=200,**kw):return req('acao',dict(acao=acao,**kw),token,expected)
def start(db):
 p=subprocess.Popen(['java','-jar',str(ROOT/'gestao-escolar-sprint6.jar'),str(port),str(db)],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
 for _ in range(100):
  try:
   urllib.request.urlopen(url,timeout=1).close();return p
  except OSError:time.sleep(.1)
 raise RuntimeError('Servidor não iniciou')
with tempfile.TemporaryDirectory() as td:
 db=pathlib.Path(td)/'servidor.json';p=start(db)
 try:
  for path in ['/','/style.css','/app.js','/manifest.webmanifest','/sw.js']:
   with urllib.request.urlopen(url+path) as r:check(r.status==200 and len(r.read())>0,'Arquivo Web '+path)
  for name in ['ana.prof','carlos.prof','helena.prof']:
   j=login('PROFESSOR',name);check(len(j['estado']['projetos'])==3,'Login professor '+name)
  for i in range(1,11):
   j=login('ALUNO',f'{i:02}012010-{1000+i}');check(len(j['estado']['projetos'])==3 and sum(e['souMembro'] for e in j['estado']['equipes'])==1,'Login aluno '+str(i))
  ana=login('PROFESSOR','ana.prof@exemplo.test')['token'];alice=login('ALUNO','01012010-1001')['token'];bruno=login('ALUNO','02012010-1002')['token'];diego=login('ALUNO','04012010-1004')['token']
  check(True,'Professor entra por e-mail')
  check('erro' in login('ALUNO','01012010-1001','errada',401),'Senha inválida bloqueada')
  check('erro' in req('estado',expected=401),'Acesso sem sessão bloqueado')
  check('erro' in req('codigo',{},alice,400),'Aluno não pode emitir código')
  codigo=req('codigo',{},ana)['codigo'];check(codigo.startswith('AL-'),'Professor emite código')
  form=dict(perfil='ALUNO',email='novo@exemplo.test',login='novo.aluno',senha='Teste123!',codigo=codigo)
  req('cadastro',dict(form,codigo='INVALIDO'),expected=400);check(True,'Código inválido bloqueado')
  req('cadastro',form,expected=201);novo=login('ALUNO',codigo,'Teste123!');check(novo['estado']['usuario']['nome']=='novo.aluno','Cadastro e login aluno')
  req('cadastro',dict(form,email='outro@exemplo.test',login='outro'),expected=400);check(True,'Código não pode ser reutilizado')
  req('cadastro',dict(perfil='PROFESSOR',email='novo.prof@exemplo.test',login='novo.prof',senha='Teste123!',codigo=''),expected=201);check(login('PROFESSOR','novo.prof','Teste123!')['estado']['usuario']['perfil']=='PROFESSOR','Cadastro professor')
  req('cadastro',dict(form,perfil='PROFESSOR',login='NOVO.PROF',email='outro@exemplo.test'),expected=400);check(True,'Usuário duplicado bloqueado')
  act(bruno,'transferirLider',expected=400,id=1,aluno=6);check(True,'Não líder bloqueado')
  act(alice,'transferirLider',expected=400,id=1,aluno=7);check(True,'Não membro bloqueado')
  j=act(alice,'transferirLider',id=1,aluno=5);check(not j['equipes'][0]['souLider'] and j['equipes'][0]['souMembro'],'Líder anterior mantém participação')
  check(req('estado',token=bruno)['equipes'][0]['souLider'],'Novo líder tem permissão')
  act(alice,'privacidadeProjeto',expected=400,id=1,publico=False);check(True,'Líder antigo perde permissões')
  act(bruno,'privacidadeProjeto',id=1,publico=False);check(len(req('estado',token=diego)['projetos'])==2,'Aluno externo não vê projeto privado')
  check(len(req('estado',token=ana)['projetos'])==3 and len(req('estado',token=login('PROFESSOR','carlos.prof')['token'])['projetos'])==3,'Todos professores veem projeto privado')
  act(bruno,'editarEquipe',id=1,nome='EcoTech',descricao='Privada',foto='',publica=False,nomePublico=False,liderPublico=False,membrosPublicos=False);check(len(req('estado',token=diego)['equipes'])==2,'Equipe privada não aparece a externos')
  act(ana,'atividade',titulo='Teste de atividade',materia='Ciência',descricao='Teste',prazo=(datetime.date.today()+datetime.timedelta(days=3)).isoformat());j=act(alice,'entregar',id=4,resposta='Teste concluído');check(j['atividades'][-1]['status']=='ENTREGUE','Publicação e entrega')
  check(len(req('estado',token=bruno)['atividades'][-1]['entregas'])==0,'Entrega individual protegida')
  act(bruno,'solicitar',id=1,assunto='Orientação',horario=(datetime.datetime.now()+datetime.timedelta(days=2)).replace(second=0,microsecond=0).isoformat());j=act(ana,'responderMentoria',id=1,decisao=1);check(j['mentorias'][0]['status']=='CONFIRMADA','Solicitação e confirmação de mentoria')
  assert all('senhaHash' not in x and 'codigo' not in x and 'email' not in x for x in j['usuarios']);check(True,'API não expõe credenciais')
  p.terminate();p.wait();p=start(db);j=login('ALUNO',codigo,'Teste123!');check(j['estado']['usuario']['nome']=='novo.aluno','Conta persiste após reinício')
  j=login('ALUNO','02012010-1002');check(j['estado']['equipes'][0]['souLider'] and not j['estado']['projetos'][0]['publico'],'Liderança e privacidade persistem')
  check(login('PROFESSOR','ana.prof')['estado']['mentorias'][0]['status']=='CONFIRMADA','Mentoria persiste')
 finally:p.terminate();p.wait()
print(f'RESULTADO: {count} verificações aprovadas.')
