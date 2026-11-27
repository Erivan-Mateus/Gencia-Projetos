const fs=require('fs'),vm=require('vm'),assert=require('node:assert/strict'),path=require('path');
const elements=new Map();const get=id=>{if(!elements.has(id))elements.set(id,{value:'',hidden:false,innerHTML:'',textContent:'',showModal(){},close(){}});return elements.get(id);};
const ctx=vm.createContext({console,document:{getElementById:get,addEventListener(){}},sessionStorage:{getItem(){return ''},removeItem(){},setItem(){}},navigator:{},window:{addEventListener(){}},setTimeout,confirm:()=>true});
vm.runInContext(fs.readFileSync(path.join(__dirname,'../web/app.js'),'utf8'),ctx);
const run=code=>vm.runInContext(code,ctx);
run(`state={usuario:{id:1,nome:'Ana',perfil:'PROFESSOR'},materias:[{id:1,nome:'Ciências',professor:{nome:'Ana'}},{id:2,nome:'Matemática',professor:{nome:'Ana'}},{id:3,nome:'Abissologia',professor:{nome:'Ana'}},{id:4,nome:'Ciências',professor:{nome:'Carlos'}}],turmas:[{id:11,materia:1,nome:'A',materiaNome:'Ciências'},{id:12,materia:1,nome:'B',materiaNome:'Ciências'},{id:21,materia:2,nome:'C',materiaNome:'Matemática'},{id:41,materia:4,nome:'D',materiaNome:'Ciências'}],materiais:[[1,11,'Apostila A'],[2,12,'Apostila B'],[3,21,'Álgebra'],[4,41,'Ciências Carlos']].map(([id,turma,titulo])=>({id,turma,titulo,materiaNome:'Disciplina',turmaNome:'Turma',descricao:'',data:'2026-11-27',arquivo:'',link:'https://example.com'}))};page='materiais';`);
let n=0;function test(name,fn){fn();console.log('OK — '+name);n++;}
test('Quatro cartões, incluindo disciplinas sem materiais',()=>{const h=run('materiais()');assert.equal((h.match(/class="materia-pasta"/g)||[]).length,4);assert.match(h,/Abissologia/);assert.match(h,/0 materiais/);assert.doesNotMatch(h,/Apostila A/);});
test('Separação por ID, inclusive matérias homônimas',()=>{run('abrirBiblioteca(1)');const h=get('content').innerHTML;assert.match(h,/Apostila A/);assert.match(h,/Apostila B/);assert.doesNotMatch(h,/Álgebra|Ciências Carlos/);});
test('Filtro por turma',()=>{run("handle('filtrarMaterialTurma',12)");const h=get('content').innerHTML;assert.match(h,/Apostila B/);assert.doesNotMatch(h,/Apostila A/);});
test('Publicação restrita às opções da matéria selecionada',()=>{run("handle('publicarMaterial',12)");const h=get('fields').innerHTML;assert.match(h,/value="11"/);assert.match(h,/value="12" selected/);assert.doesNotMatch(h,/value="21"|value="41"/);});
test('Retorno à biblioteca',()=>{run("handle('voltarMaterias',0)");assert.match(get('content').innerHTML,/materia-pasta/);});
test('Matéria vazia',()=>{run("handle('abrirMateria',3)");assert.match(get('content').innerHTML,/Nenhum material publicado/);});
test('Aluno sem botões de publicar ou excluir',()=>{run("state.usuario.perfil='ALUNO';abrirBiblioteca(1)");const h=get('content').innerHTML;assert.doesNotMatch(h,/data-act="publicarMaterial"|data-act="excluirMaterial"/);assert.match(h,/Abrir link/);});
test('Atalho direto da turma',()=>{run("handle('abrirTurmaMateriais',21)");assert.match(get('content').innerHTML,/Álgebra/);assert.doesNotMatch(get('content').innerHTML,/Apostila A/);});
test('Seleção inválida volta ao catálogo',()=>{run('abrirBiblioteca(999)');assert.match(get('content').innerHTML,/materia-pasta/);});
console.log(`${n} testes de renderização e navegação aprovados (DOM simulado).`);
