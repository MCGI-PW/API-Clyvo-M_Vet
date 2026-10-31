# 🎬 Entrega Final: checklist e roteiro do vídeo

Guia do grupo para a entrega final do **Clyvo Vet**, organizado pela rubrica (Demonstração 40 · Narrativa 20 · Integração 20 · Apresentação 10 · Organização 10).

---

## 📦 1. Artefatos de entrega

| Artefato | Onde | Status |
|---|---|:---:|
| Repositório no GitHub com README completo | [MCGI-PW/API-Clyvo-M_Vet](https://github.com/MCGI-PW/API-Clyvo-M_Vet) | 🟡 faltam os itens marcados com ⚠️ no README |
| Link de acesso à aplicação | [clyvovet-app-16666](http://clyvovet-app-16666.brazilsouth.azurecontainer.io:8080/) · [Swagger](http://clyvovet-app-16666.brazilsouth.azurecontainer.io:8080/swagger-ui.html) | 🟢 no ar desde 31/10/2026 |
| Vídeo da apresentação (máx. **15 min**) | README, seção do topo | 🔴 gravar (seção 4) e colocar o link |
| Serviço de IA (disciplina de IA/IoT) | [MCGI-PW/IoT-Clyvo-Vet_M](https://github.com/MCGI-PW/IoT-Clyvo-Vet_M) | 🟢 |
| App mobile | [MCGI-PW/AuroraPet](https://github.com/MCGI-PW/AuroraPet) | 🟢 |
| Assinaturas das clínicas (.NET) | [MCGI-PW/StripeAurora](https://github.com/MCGI-PW/StripeAurora) | 🟢 |
| Canvas e protótipo | [`CANVAS.md`](CANVAS.md) · [`prototipo/`](prototipo/README.md) | 🟡 canvas em rascunho |
| Infraestrutura (Docker + Azure) | [MCGI-PW/DevOps-Clyvo-M_Vet](https://github.com/MCGI-PW/DevOps-Clyvo-M_Vet) | 🟢 |

> ⚠️ **Entrega fora do prazo ou fora do portal = −100 pontos.** Confirme a data e envie pelo portal da FIAP, não por e-mail ou Teams.

---

## ☁️ 2. Aplicação online

No ar em **http://clyvovet-app-16666.brazilsouth.azurecontainer.io:8080/** (ACI `aci-clyvovet-app`, grupo `rg-clyvovet-fiap`, conectado ao Oracle da FIAP com o schema `RM561279` na versão V7). As contas de demonstração usam a senha `senha123`.

- **Não desligue o container antes de sair a nota:** o avaliador pode abrir o link dias depois da entrega.
- **Se precisar refazer o deploy:** rode `scripts/deploy_acr_aci.sh` do repositório DevOps. O usuário do Oracle é `rm561279`, **com o "rm"**. Sem ele, o Oracle recusa o login e o container fica reiniciando.
- **Logs:** `az container logs -g rg-clyvovet-fiap -n aci-clyvovet-app`.
- **OAuth2 online:** os botões Google/GitHub só aparecem com chaves reais (`GOOGLE_CLIENT_ID`, `GITHUB_CLIENT_ID`). Sem elas, mostre o login social no ambiente local (botões "Testar Google/GitHub").

---

## 🧭 3. Antes de gravar (evita −5 por erro visual ou de fluxo)

- [ ] Gravar com a aplicação **online**, não com o `localhost`: o requisito é "rodando online".
- [ ] Ensaiar o roteiro inteiro uma vez, cronometrando.
- [ ] Usar o **Google Chrome**. O microfone da IA só funciona em https ou `localhost`. Na URL `http://` do Azure, o Chrome bloqueia o microfone, então **na demonstração online, cole a transcrição** (deixe o texto abaixo copiado). A transcrição ao vivo pode ser mostrada no ClyvoScribe, rodando localmente.
- [ ] Deixar abertas as abas: aplicação (deslogada), Swagger, GitHub (README e `db/migration`), repositório IoT e repositório AuroraPet.
- [ ] Fechar notificações do computador e aumentar o zoom do navegador para 110–125%.

Transcrição para colar na demonstração da IA:

```text
Tutora: O Thor está coçando muito a orelha direita há três dias e balança a cabeça.
Veterinário: A orelha está vermelha e com secreção. Parece ser uma otite externa.
Vou prescrever solução otológica, 3 gotas duas vezes ao dia por 7 dias.
Mantenha a orelha seca e não use cotonete. Se ele tiver febre ou parar de comer, volte antes.
Retorno em 10 dias.
```

---

## 🎥 4. Roteiro do vídeo (≈14 min, os 4 integrantes falam)

Cada bloco cita os critérios da rubrica que ele cobre. A divisão segue a área em que cada pessoa trabalhou; troquem como preferirem, mas **todos precisam aparecer falando**.

### Bloco 1: Abertura e problema · Iago · 0:00–1:45 · *Narrativa*
- Apresentar o grupo (nome e RM de cada um, câmera ligada).
- **Problema real:** o histórico do pet fica espalhado entre clínicas; o veterinário atende sem contexto; o tutor não controla quem vê os dados (LGPD).
- **Proposta em uma frase:** "O Clyvo Vet conecta tutor, veterinário e clínica, com o tutor no controle do prontuário do pet."
- Mostrar a tela de login online.

### Bloco 2: Jornada do tutor · Charlles · 1:45–4:30 · *Demonstração, UI/UX*
- Login como **tutor** → 🔔 notificações → cadastrar um pet.
- **Agendar consulta** e mostrar a validação: horário ocupado ou data no passado aparecem como aviso, sem erro de tela.
- Abrir o **prontuário** existente do Thor.
- **Autorizações:** explicar que o agendamento já criou o consentimento; conceder acesso à Dra. Camila e revogar em seguida (as consultas pendentes com ela são canceladas em cascata).
- **UI/UX:** trocar tema claro/escuro e idioma PT/EN, e mostrar a tela num tamanho de celular.

### Bloco 3: Veterinário, IA e clínica · Gabriel · 4:30–8:00 · *Demonstração, originalidade*
- Login como **veterinário** → fila de atendimentos.
- **Assistente de IA da teleconsulta:** colar a transcrição → "Gerar resumo com IA" → diagnóstico, prescrição e orientações preenchidos, mais o resumo para o tutor e os sinais de alerta. Reforçar: **a IA sugere, o veterinário revisa e assina**.
- Concluir o atendimento e mostrar que **sem diagnóstico o sistema recusa**.
- Login como **clínica** → vincular a Dra. Camila → **transferir** o paciente Thor para ela.
- Fechar com o diferencial: consentimento como regra de negócio + multi-tenancy (cada clínica só vê o que é dela).

### Bloco 4: Tecnologias da disciplina · Maicon · 8:00–11:15 · *Conceitos aplicados, decisões técnicas*
- **Spring Security:** três perfis com permissões diferentes. Logado como tutor, tentar abrir `/web/vet/dashboard` → é barrado. CSRF nos formulários; JWT revogável na API (mostrar no Swagger).
- **OAuth2:** botões Google/GitHub no login (com as chaves configuradas) e o modo de simulação local.
- **Flyway:** abrir `db/migration` (V1 a V7) e explicar que qualquer ambiente sobe com o mesmo schema, inclusive o Oracle da FIAP.
- **Arquitetura:** a mesma camada de services atende a Web (Thymeleaf) e a API REST do app mobile, sem duplicar regra.
- **IA:** o resumo vem do **ClyvoScribe AI** (Python, outro repositório) via HTTP; se ele cair, um assistente local assume e a consulta não trava.
- **Deploy:** Dockerfile multi-stage não-root, build no ACR e execução no ACI.

### Bloco 5: Integração multidisciplinar e qualidade · Iago · 11:15–13:15 · *Integração, evidências*
- Percorrer a tabela **Integração Multidisciplinar** do README mostrando cada evidência:
  - Banco de Dados: DER + scripts SQL;
  - Mobile: app AuroraPet consumindo a API;
  - IA/IoT: ClyvoScribe;
  - DevOps: repositório e Azure;
  - QA: rodar `./mvnw test` ou mostrar os 67 testes passando;
  - .NET: StripeAurora, com as assinaturas das clínicas pela Stripe (a fonte de receita do canvas);
  - Negócios/UX: [canvas](CANVAS.md) e [telas do protótipo](prototipo/README.md).

### Bloco 6: Encerramento · todos · 13:15–14:15 · *Comunicação em equipe*
- Cada um diz em uma frase o que construiu.
- Originalidade em uma frase: "o tutor é dono do dado, e a IA tira o teclado do caminho do veterinário".
- Próximos passos (ex.: agenda por especialidade, compartilhamento do prontuário entre clínicas por QR code).

---

## 🤝 5. Evidência de colaboração (−10 se faltar)

Hoje quase todos os commits estão na conta do Maicon ou em `Clyvo Dev`. Para a colaboração aparecer no GitHub, **cada integrante deve fazer as próprias contribuições com a própria conta**, por exemplo:

- preencher a própria linha da tabela **Equipe e Colaboração** do README (GitHub e responsabilidades);
- subir a evidência da sua disciplina em `docs/` (canvas, prints do protótipo, link do Figma);
- abrir um Pull Request e pedir a revisão de outro integrante antes do merge.
