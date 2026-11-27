import base64, datetime, json, pathlib, socket, subprocess, tempfile, time, urllib.request, urllib.error
ROOT=pathlib.Path(__file__).resolve().parents[1]
with socket.socket() as sock:
 sock.bind(('127.0.0.1',0)); port=sock.getsockname()[1]
url=f'http://127.0.0.1:{port}'
count=0
def check(ok,msg):
 global count
 assert ok,msg
 count+=1;print(f'PASS {count:02}: {msg}',flush=True)
def req(path,data=None,token=None,expected=200,raw=False):
 headers={}
 if token:headers['Authorization']='Bearer '+token
 if data is not None:headers['Content-Type']='application/json'
 q=urllib.request.Request(url+'/api/'+path,data=None if data is None else json.dumps(data).encode(),headers=headers)
 try:
  with urllib.request.urlopen(q,timeout=10) as r:status=r.status;v=r.read() if raw else json.load(r)
 except urllib.error.HTTPError as e:status=e.code;v=json.load(e)
 assert status==expected,(path,status,expected,v)
 return v
def login(role,id,senha='Piloto123!'):return req('login',dict(perfil=role,identificador=id,senha=senha))
def act(token,acao,expected=200,**kw):return req('acao',dict(acao=acao,**kw),token,expected)
def estado(token):return req('estado',token=token)
def start(db):
 p=subprocess.Popen(['java','-jar',str(ROOT/'gestao-escolar-sprint6.jar'),str(port),str(db)],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
 for _ in range(100):
  try:urllib.request.urlopen(url,timeout=1).close();return p
  except OSError:time.sleep(.1)
 p.terminate();p.wait();raise RuntimeError('Servidor não iniciou')
with tempfile.TemporaryDirectory() as td:
 db=pathlib.Path(td)/'servidor.json';p=start(db)
 try:
  ana=login('PROFESSOR','ana.prof')['token']; carlos=login('PROFESSOR','carlos.prof')['token']; alice=login('ALUNO','01012010-1001')['token']; diego=login('ALUNO','04012010-1004')['token']
  check(len(estado(ana)['turmas'])==1 and len(estado(alice)['turmas'])==1,'Turmas iniciais para demonstração')
  act(alice,'criarMateria',expected=400,nome='Física');check(True,'Aluno não cria matéria')
  j=act(ana,'criarMateria',nome='Física');mid=j['materias'][-1]['id'];check(j['materias'][-1]['nome']=='Física','Professor cadastra matéria')
  act(ana,'criarMateria',expected=400,nome='física');check(True,'Matéria duplicada bloqueada')
  act(carlos,'criarTurma',expected=400,materia=mid,nome='A');check(True,'Outro professor não usa matéria privada')
  j=act(ana,'criarTurma',materia=mid,nome='1º A');tid=j['turmas'][-1]['id'];check(j['turmas'][-1]['materiaNome']=='Física','Turma vinculada à matéria')
  check(all(t['id']!=tid for t in estado(carlos)['turmas']),'Outro professor não vê turma')
  act(carlos,'adicionarAluno',expected=400,id=tid,aluno=4);check(True,'Outro professor não altera matrícula')
  act(ana,'adicionarAluno',expected=400,id=tid,aluno=2);check(True,'Professor não pode ser matriculado como aluno')
  act(ana,'adicionarAluno',id=tid,aluno=4);check(any(t['id']==tid for t in estado(alice)['turmas']),'Aluno existente vê sua turma')
  act(ana,'adicionarAluno',expected=400,id=tid,aluno=4);check(True,'Matrícula duplicada bloqueada')
  check(all(t['id']!=tid for t in estado(diego)['turmas']),'Aluno externo não vê turma')
  act(ana,'criarPasse',expected=400,id=tid,nome='Teste',matricula='abc',nascimento='2010-01-01');check(True,'Matrícula inválida bloqueada')
  j=act(ana,'criarPasse',id=tid,nome='Aluno fictício',matricula='12345',nascimento='2010-02-01');codigo=j['codigo'];check(codigo.startswith('01022010-12345-'),'Passe combina data, matrícula e sufixo aleatório')
  check(codigo not in json.dumps(estado(ana)) and codigo not in json.dumps(estado(diego)),'Passe não retorna nas consultas')
  form=dict(perfil='ALUNO',email='turma@exemplo.test',login='aluno.turma',senha='Teste123!',codigo=codigo)
  req('cadastro',form,expected=201);novo=login('ALUNO',codigo,'Teste123!');novo_token=novo['token'];check(any(t['id']==tid for t in novo['estado']['turmas']),'Cadastro vincula aluno automaticamente à turma')
  req('cadastro',dict(form,email='outra@exemplo.test',login='outra.turma'),expected=400);check(True,'Passe de cadastro de uso único')
  check(not next(t for t in estado(ana)['turmas'] if t['id']==tid)['pendentes'],'Passe sai da lista pendente após cadastro')
  j=act(ana,'criarPasse',id=tid,nome='Cancelar teste',matricula='54321',nascimento='2010-02-02');passe=j['codigo'];pid=next(t for t in j['estado']['turmas'] if t['id']==tid)['pendentes'][0]['id'];act(ana,'cancelarPasse',id=pid);req('cadastro',dict(form,email='cancelado@exemplo.test',login='cancelado',codigo=passe),expected=400);check(True,'Passe cancelado não permite cadastro')
  # PDF mínimo sintético para testar bytes, não destinado a leitura.
  pdf=b'%PDF-1.4\n% teste de transporte\n%%EOF\n';payload=base64.b64encode(pdf).decode();material=dict(turma=tid,titulo='Apostila de Física',descricao='Material de teste',link='https://example.com/apostila',arquivo='apostila.pdf',base64=payload)
  act(alice,'publicarMaterial',expected=400,**material);check(True,'Aluno não publica material')
  act(carlos,'publicarMaterial',expected=400,**material);check(True,'Professor externo não publica na turma')
  act(ana,'publicarMaterial',expected=400,**dict(material,link='javascript:alert(1)'));check(True,'URL perigosa bloqueada')
  act(ana,'publicarMaterial',expected=400,**dict(material,base64='aGVsbG8='));check(True,'Arquivo sem cabeçalho PDF bloqueado')
  j=act(ana,'publicarMaterial',**material);mid=j['materiais'][-1]['id'];check(j['materiais'][-1]['titulo']=='Apostila de Física' and 'base64' not in j['materiais'][-1],'Material publicado; bytes fora do estado público')
  check(any(m['id']==mid for m in estado(alice)['materiais']) and any(m['id']==mid for m in estado(novo_token)['materiais']),'Alunos matriculados consultam materiais')
  check(not any(m['id']==mid for m in estado(diego)['materiais']) and not any(m['id']==mid for m in estado(carlos)['materiais']),'Material oculto para terceiros')
  check(req('material',{'id':mid},alice,raw=True)==pdf,'Download autenticado mantém bytes')
  req('material',{'id':mid},diego,expected=400);req('material',{'id':mid},carlos,expected=400);req('material',{'id':mid},expected=401);check(True,'Download de terceiros ou anônimo bloqueado')
  act(carlos,'excluirMaterial',expected=400,id=mid);check(True,'Exclusão de outro professor bloqueada')
  j=act(ana,'atividade',turma=tid,titulo='Exercícios',descricao='Resolva',prazo=(datetime.date.today()+datetime.timedelta(days=4)).isoformat());aid=j['atividades'][-1]['id'];check(j['atividades'][-1]['materia']=='Física' and j['atividades'][-1]['turma']==tid,'Atividade nova vinculada à turma e matéria')
  check(not any(a['id']==aid for a in estado(diego)['atividades']),'Aluno externo não consulta atividade de turma')
  act(diego,'entregar',expected=400,id=aid,resposta='tentativa');check(True,'Aluno externo não envia entrega')
  act(alice,'entregar',id=aid,resposta='Resolvido');check(True,'Aluno matriculado entrega atividade')
  act(ana,'removerAluno',id=tid,aluno=4);req('material',{'id':mid},alice,expected=400);check(not any(a['id']==aid for a in estado(alice)['atividades']),'Remoção revoga acesso à turma, materiais e atividades')
  p.terminate();p.wait();p=start(db);novo=login('ALUNO',codigo,'Teste123!');check(req('material',{'id':mid},novo['token'],raw=True)==pdf,'Matrícula e PDF persistem após reinício')
  ana=login('PROFESSOR','ana.prof')['token'];act(ana,'excluirMaterial',id=mid);req('material',{'id':mid},novo['token'],expected=400);check(True,'Exclusão remove acesso ao PDF')
 finally:p.terminate();p.wait()
 # Compatibilidade com arquivo antigo sem bloco acadêmico
 old=pathlib.Path(td)/'antigo.json';raw=json.loads((ROOT/'seed.json').read_text());raw.pop('academico');old.write_text(json.dumps(raw));p=start(old)
 try:check(login('PROFESSOR','ana.prof')['estado']['turmas']==[],'Dados da Sprint 6 original carregam sem migração destrutiva')
 finally:p.terminate();p.wait()
print(f'RESULTADO: {count} verificações aprovadas.')
