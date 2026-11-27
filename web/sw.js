// Não armazena dados privados ou respostas da API; o aplicativo exige o servidor ativo.
self.addEventListener('install',()=>self.skipWaiting());self.addEventListener('activate',e=>e.waitUntil(self.clients.claim()));self.addEventListener('fetch',()=>{});
