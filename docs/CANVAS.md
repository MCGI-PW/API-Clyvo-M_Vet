# 🧩 Business Model Canvas: Clyvo Vet

> ⚠️ **Rascunho para o grupo revisar.** Ele foi montado a partir do que o projeto já implementa (perfis, fluxos, IA e as assinaturas do serviço `.NET`). Ajustem preços, parceiros e números antes da entrega.

| 🤝 Parceiros-chave | ⚙️ Atividades-chave | 🎁 Proposta de valor | 💬 Relacionamento | 👥 Segmentos de clientes |
|---|---|---|---|---|
| Clínicas e hospitais veterinários parceiros<br>Conselhos regionais (CRMV)<br>Provedor de nuvem (Azure)<br>Provedor de IA (Anthropic/Claude)<br>Gateway de pagamento (Stripe)<br>Laboratórios de exames | Desenvolver e manter a plataforma (Web, API e app)<br>Garantir segurança e LGPD<br>Treinar e evoluir a IA da teleconsulta<br>Integrar novas clínicas<br>Suporte aos usuários | **Tutor:** o histórico do pet acompanha o animal, e o tutor decide quem acessa o prontuário.<br>**Veterinário:** atende com o contexto clínico completo, e a IA transcreve e resume a consulta.<br>**Clínica:** gestão da equipe, agenda da unidade e transferência de pacientes entre profissionais. | Autoatendimento pela Web e pelo app<br>Notificações a cada etapa<br>Resumo da consulta em linguagem simples para o tutor<br>Onboarding assistido das clínicas | **Tutores de pets** (cães e gatos)<br>**Médicos veterinários** autônomos<br>**Clínicas veterinárias** de pequeno e médio porte |
| | **🧱 Recursos-chave** | | **📣 Canais** | |
| | Plataforma Clyvo Vet (Spring Boot + Oracle)<br>App mobile AuroraPet<br>Serviço de IA ClyvoScribe<br>Equipe de tecnologia<br>Base de clínicas parceiras | | Aplicação Web<br>App mobile (iOS/Android)<br>Indicação de clínicas para os tutores<br>Redes sociais e eventos veterinários | |

| 💸 Estrutura de custos | 💰 Fontes de receita |
|---|---|
| Infraestrutura em nuvem (containers e banco de dados)<br>Uso da API de IA por consulta resumida<br>Taxas do gateway de pagamento<br>Desenvolvimento, suporte e marketing | **Assinatura mensal das clínicas**, por planos que liberam recursos adicionais (cobrança recorrente pela Stripe, implementada no serviço `.NET`)<br>Plano por profissional para veterinários autônomos<br>Uso gratuito para tutores (é o que atrai as clínicas) |
