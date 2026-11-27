# Sprint 6.1 — Gestão Escolar online (piloto)

## Arquitetura
GitHub armazena o código; Render executa Java e serve o site e API no mesmo endereço HTTPS; Supabase PostgreSQL guarda os dados compartilhados. Não é necessário GitHub Pages para a aplicação online. A versão estática antiga é apenas uma demonstração local independente; desative-a ou troque-a por um link para o Render depois do deploy.

O piloto persiste o estado completo da Sprint 5 na tabela privada `gestao_privado.estado` com coluna JSONB e revisão otimista. Não é o mesmo que salvar um arquivo JSON no Render: a gravação é durável no PostgreSQL. A estrutura é propositalmente simples para uma única instância e pequeno volume; tabelas normalizadas e atualização incremental ficam para evolução. Em conflito de revisão, a gravação é recusada e sessões são encerradas para recarregar o estado. Não escale para múltiplas instâncias. O seed inclui 13 contas e três projetos fictícios e só inicializa quando a linha ainda não existe. Não importa automaticamente alterações do JSON local.

## 1. Supabase
1. Redefina a senha do banco (a antiga foi compartilhada). Guarde a nova privadamente.
2. No SQL Editor, clique em New query, cole o conteúdo de `schema.sql`, e execute Run. O script é idempotente e não apaga dados. Não exponha o schema gestao_privado na Data API.
3. O host configurado é aws-1-us-west-2.pooler.supabase.com, porta 5432, banco postgres. Não execute o comando npx de agent-skills: ele não é necessário.

## 2. GitHub
Extraia o ZIP. Envie os arquivos internos da pasta sprint6_1_gestao_escolar para a raiz do repositório Erivan-Mateus/Gencia-Projetos. Preserve as pastas src e web. Substitua os arquivos da versão anterior, não envie o banco JSON local, .env, senhas ou chaves privadas. O Dockerfile e pom.xml devem estar na raiz da branch main. O JAR local é opcional no GitHub; o Docker compila tudo sozinho.

## 3. Render
No painel, New > Web Service > Git Provider, conecte o repositório e conceda acesso a ele. Escolha branch main, região Oregon (US West), se disponível, Runtime Docker, instância Free, Root Directory vazio e Dockerfile Path ./Dockerfile. Não preencha comandos de build/start: o Dockerfile cuida deles. Em Advanced, Health Check Path /health. Configure as variáveis de ambiente (adicione uma por vez):

| Nome | Valor |
|---|---|
| DB_URL | `jdbc:postgresql://aws-1-us-west-2.pooler.supabase.com:5432/postgres?sslmode=verify-full&sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory` |
| DB_USER | `postgres.cihapbheqxqoogbvqhln` |
| DB_PASSWORD | A NOVA senha do banco, sem aspas, sem colchetes, sem codificação URL |
| PROFESSOR_INVITE | Código secreto novo escolhido por você, pelo menos 16 caracteres, distinto da senha do banco |

Não coloque as duas últimas variáveis no GitHub nem compartilhe em capturas. O Render fornece PORT, o Java escuta em 0.0.0.0. Clique em Create Web Service/Deploy. Acompanhe os logs. A inicialização falha se faltar configuração ou a conexão não funcionar, sem fallback silencioso para JSON. Se houver erro TLS, mantenha verificação de certificado e confira a cadeia CA com a documentação Supabase/pgJDBC. Não desative TLS para resolver.

## 4. Verificação após deploy
Abra a URL HTTPS fornecida pelo Render, depois /health. Esperado: {"ok":true,"storage":"postgresql"}. A rota informa o estado de inicialização do serviço; não é um teste contínuo do banco. Entre como Ana, selecione ou crie uma matéria e turma, vincule Alice, e publique uma atividade fictícia com prazo futuro, em outra janela/dispositivo entre como Alice e consulte a atividade. Entregue uma resposta fictícia. No Render faça Manual Deploy (sem alterar seed), aguarde, entre novamente e confirme a entrega. Faça os testes de privacidade e transferência de liderança em contas de demonstração. As sessões duram até oito horas e são encerradas no reinício do servidor. O banco permanece.

## 5. Aplicativo e abertura
O site já é responsivo e contém manifesto e ícones PWA. Abra pelo endereço HTTPS do Render: no Chrome Android, menu > Instalar aplicativo/Adicionar à tela inicial; no Safari iPhone, Compartilhar > Adicionar à Tela de Início. Instalação e aparência dependem do navegador e ainda devem ser verificadas em um aparelho. A PWA requer internet para consultar o servidor e não guarda respostas da API em cache. A abertura mostra a frase fornecida em fundo branco, com fade-in de 1,2 s, pausa de 4 s e fade-out de 1,2 s (6,4 s ao todo), a cada recarga. Respeita a preferência de movimento reduzido.

## 6. Teste local sem banco
Java 11+: execute `executar.bat` ou `java -jar gestao-escolar-sprint6.jar` na raiz e abra http://localhost:8080. O arquivo dados/servidor.json é criado apenas localmente. O JAR local não contém o driver PostgreSQL; o Docker baixa o driver via Maven. Para PostgreSQL local, baixe as dependências com Maven (`mvn dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=lib`), defina as quatro variáveis e use `java -cp "gestao-escolar-sprint6.jar;lib/*" Servidor` no Windows (no Linux/macOS, use `:` no lugar de `;`). Atenção: conectar ao banco online altera seus dados reais; use banco de testes.

## 7. Evidências e limites
- Compilação Java 11, sintaxe JS e 43 verificações HTTP no modo local executadas neste ambiente.
- Build Docker, conexão JDBC no Supabase, deploy no Render, teste visual e instalação PWA em aparelho não foram executados aqui; dependem do ambiente online.
- Senhas PBKDF2, sessões aleatórias, controle de permissões no Java, convite secreto para cadastro de professor no modo online. Códigos escolares são de uso único para cadastro; alunos entram com código e senha.
- Contas fictícias possuem senha pública de demonstração; qualquer pessoa que a conheça pode acessar essas contas. Não use dados reais de alunos, professores ou da escola. Antes de uso real, remova contas fictícias, adote aprovação individual de professores, senhas individuais, recuperação de acesso, limites contra tentativas repetidas, política de privacidade, backups e revisão de segurança/LGPD. O plano gratuito pode suspender ou atrasar a disponibilidade.

## 8. Gestão acadêmica (Sprint 6.1)
- Matérias: cada professor cadastra as suas disciplinas. Turmas: cada sala é vinculada a uma matéria e ao professor que a criou. O professor pode matricular alunos existentes, removê-los e gerar passes de novos alunos. Uma conta pode participar de várias turmas.
- Passe escolar: `DDMMAAAA-MATRICULA-SUFIXO_ALEATORIO`. É apresentado apenas na geração; copie e entregue ao aluno. Se perder antes do cadastro, cancele e gere outro. O aluno utiliza o passe para criar uma conta (e-mail, usuário e senha) e depois entra com o mesmo passe e senha. O cadastro o vincula automaticamente à turma. O sufixo evita que somente a matrícula e a data bastem para adivinhar o passe. Não use datas ou matrículas reais no piloto: o código inclui esses dados. Para uso real, substitua o formato por um código opaco aleatório.
- Materiais: título, descrição, turma, PDF (até 2 MiB por arquivo, 16 MiB somados) e/ou link HTTP(S). O arquivo é salvo em base64 no estado privado JSONB, fora das respostas de listagem. Downloads exigem sessão e matrícula ou propriedade da turma. O professor pode excluir materiais. Para documentos grandes, use um link com permissão de acesso apropriada; a plataforma não controla a privacidade do site externo. Não há análise antivírus; apenas validação básica de tamanho e cabeçalho. Em produção, utilize Supabase Storage privado e verificação de arquivos.
- Atividades novas pela interface exigem turma, e a matéria é derivada do cadastro. Atividades antigas sem turma permanecem gerais para não apagar dados. A API online exige turma para novas atividades.
- As novas áreas são Matérias, Turmas e Materiais. Alunos consultam apenas as próprias turmas e recursos. Professores administram somente as próprias turmas. A regra anterior de visibilidade de projetos privados para todos os professores continua inalterada.
- O seed novo inclui três matérias e três turmas fictícias (Ciências/Ana, História/Carlos, Tecnologia/Helena). Bases antigas carregam normalmente, com módulos acadêmicos vazios até o professor cadastrar as salas. Não sobrescreva o estado existente no Supabase. O schema.sql não mudou: os novos campos ficam no JSONB da mesma tabela.
- Atualização: substitua src/, web/ e seed.json e o JAR local, mantendo a configuração atual de Render e Supabase. Não é necessário recriar a tabela. Faça backup do estado antes de atualizar.
- Testes: `python testes/testar_academico.py` registra 37 verificações novas; `python testes/testar_local.py` registra 43 anteriores; `TesteSistema.java` registra outras 37. Consulte os registros em testes/. Conexão com o Supabase real e publicação ainda não verificadas.

Teste visual automatizado: Chromium bloqueou o endereço local com ERR_BLOCKED_BY_ADMINISTRATOR. O fluxo visual e a instalação em aparelho devem ser validados manualmente após a publicação.
