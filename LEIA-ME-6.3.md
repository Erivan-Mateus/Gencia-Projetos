# Sprint 6.3 — Biblioteca por matéria

Atualização incremental sobre a Sprint 6.2. Substitua apenas `web/app.js`, `web/style.css` e `web/index.html` no repositório, mantendo as demais pastas e arquivos. O Render deve recompilar a aplicação. Se o deploy não começar automaticamente, execute Manual Deploy > Deploy latest commit.

A biblioteca exibe um cartão por matéria autorizada, inclusive matérias ainda sem publicações. Ao abrir um cartão, os materiais daquela matéria aparecem, com filtro por turma quando houver mais de uma. A publicação dentro da matéria oferece apenas as turmas correspondentes. Matérias homônimas de professores diferentes permanecem separadas por ID. A opção de voltar e atalhos em Matérias e Turmas facilitam a navegação.

Nenhuma modificação no Java, no banco, nas credenciais ou no SQL. Os materiais existentes são agrupados automaticamente com os dados já retornados pela API. Os controles de acesso do servidor não foram alterados.

Validação: `node --check web/app.js` e `node testes/testar_biblioteca.js` (9 testes de renderização/navegação com DOM simulado). A tentativa de executar a interface em Chromium foi bloqueada pela política de navegação do ambiente; valide o layout no site após o deploy. O teste opcional `testes/testar_biblioteca.py` usa Playwright, Chromium e API simulada.
