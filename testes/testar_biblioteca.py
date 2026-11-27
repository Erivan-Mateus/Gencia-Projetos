"""Teste de interface com API simulada; requer playwright e Chromium."""
from pathlib import Path
from playwright.sync_api import sync_playwright
import os
root=Path(__file__).resolve().parents[1]/'web'
base='https://biblioteca.test'
subjects=[{'id':1,'nome':'Ciências','professor':{'nome':'Ana'}},{'id':2,'nome':'Matemática','professor':{'nome':'Ana'}},{'id':3,'nome':'Abissologia','professor':{'nome':'Ana'}},{'id':4,'nome':'Ciências','professor':{'nome':'Carlos'}}]
classes=[{'id':11,'materia':1,'nome':'A','materiaNome':'Ciências'},{'id':12,'materia':1,'nome':'B','materiaNome':'Ciências'},{'id':21,'materia':2,'nome':'C','materiaNome':'Matemática'},{'id':41,'materia':4,'nome':'D','materiaNome':'Ciências'}]
materials=[dict(id=i,turma=t,titulo=name,materiaNome=next(c['materiaNome'] for c in classes if c['id']==t),turmaNome=next(c['nome'] for c in classes if c['id']==t),descricao='',data='2026-11-27',arquivo='',link='https://example.com',tamanho=0) for i,t,name in [(1,11,'Apostila A'),(2,12,'Apostila B'),(3,21,'Álgebra'),(4,41,'Ciências Carlos')]]
def state(role):
 return dict(usuario={'id':1,'nome':'Ana' if role=='PROFESSOR' else 'Alice','perfil':role},materias=subjects if role=='ALUNO' else subjects[:3],turmas=classes if role=='ALUNO' else classes[:3],materiais=materials if role=='ALUNO' else materials[:3],atividades=[],projetos=[],mentorias=[],equipes=[],convites=[],usuarios=[])
with sync_playwright() as p:
 browser=p.chromium.launch(executable_path=os.environ.get('CHROMIUM','/usr/bin/chromium'),args=['--no-sandbox'])
 for role in ['PROFESSOR','ALUNO']:
  context=browser.new_context(viewport={'width':390 if role=='ALUNO' else 1200,'height':800},service_workers='block');page=context.new_page();errors=[];page.on('pageerror',lambda e:errors.append(str(e)))
  def route(r):
   path=r.request.url.split(base)[-1].split('?')[0]
   if path=='/api/login':r.fulfill(json={'token':'fake','estado':state(role)});return
   if path=='/api/estado':r.fulfill(json=state(role));return
   f=root/('index.html' if path=='/' else path.lstrip('/'))
   if f.is_file():r.fulfill(path=f)
   else:r.fulfill(status=404,body='')
  page.route(base+'/**',route);page.goto(base);page.locator('.dream-intro').evaluate("e=>e.remove()")
  page.select_option('#auth-role',role);page.fill('#auth-id','demo');page.fill('#auth-password','demo');page.click('#auth-submit');page.locator('[data-page=materiais]').click()
  assert page.locator('.materia-pasta').count()==(3 if role=='PROFESSOR' else 4)
  assert '0 materiais' in page.locator('[data-act=abrirMateria][data-id="3"]').inner_text()
  page.locator('[data-act=abrirMateria][data-id="1"]').click();assert page.locator('.card').count()==2;assert 'Álgebra' not in page.locator('#content').inner_text();assert 'Ciências Carlos' not in page.locator('#content').inner_text()
  page.locator('[data-act=filtrarMaterialTurma][data-id="12"]').click();assert page.locator('.card').count()==1;assert 'Apostila B' in page.locator('.card').inner_text()
  if role=='PROFESSOR':
   page.locator('[data-act=publicarMaterial]').click();assert page.locator('select[name=turma] option').count()==2;assert page.locator('select[name=turma]').input_value()=='12';page.click('#cancel')
  else:assert page.locator('[data-act=publicarMaterial]').count()==0;assert page.locator('[data-act=excluirMaterial]').count()==0
  page.locator('[data-act=voltarMaterias]').click();page.locator('[data-act=abrirMateria][data-id="3"]').click();assert 'Nenhum material publicado' in page.locator('#content').inner_text()
  page.locator('[data-page=materiais]').click();assert page.locator('.materia-pasta').count()==(3 if role=='PROFESSOR' else 4)
  assert not errors,errors
  print(role+': biblioteca, separação por ID, filtro, retorno, disciplina vazia e permissões de interface OK')
  context.close()
 browser.close()
