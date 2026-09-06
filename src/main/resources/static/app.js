const API_URL = 'http://localhost:8080/api';

// ==========================================
// GERENCIADOR DE TEMAS (DARK / LIGHT THEME)
// ==========================================
function getStoredTheme() {
    return localStorage.getItem('clyvo_theme') || 
           (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
}

function applyTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('clyvo_theme', theme);
    const toggles = document.querySelectorAll('.theme-toggle');
    toggles.forEach(btn => {
        btn.innerHTML = theme === 'dark' ? '☀️' : '🌙';
        btn.setAttribute('title', theme === 'dark' ? 'Mudar para Tema Claro' : 'Mudar para Tema Escuro');
    });
}

function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
    const nextTheme = currentTheme === 'dark' ? 'light' : 'dark';
    applyTheme(nextTheme);
}

// Inicializa tema imediatamente
(function initTheme() {
    applyTheme(getStoredTheme());
})();

document.addEventListener('DOMContentLoaded', () => {
    applyTheme(getStoredTheme());
});

// ==========================================
// AUTENTICAÇÃO E UTILITÁRIOS GLOBAIS
// ==========================================
function getToken() { return localStorage.getItem('token'); }

function logout() {
    localStorage.removeItem('token');
    window.location.href = 'login.html';
}

function showMsg(elementId, msg, isSuccess = false) {
    const el = document.getElementById(elementId);
    if (!el) return;
    el.style.display = 'flex';
    el.innerHTML = (isSuccess ? '✔ ' : '⚠ ') + msg;
    if (isSuccess) {
        el.className = 'success-box';
    } else {
        el.className = 'error-box';
    }
    setTimeout(() => { el.style.display = 'none'; }, 5000);
}

function parseErrors(data) {
    if (data.error) return data.error;
    let msgs = [];
    for (let key in data) { msgs.push(data[key]); }
    return msgs.length > 0 ? msgs.join(' | ') : 'Ocorreu um erro desconhecido';
}

// Formulários de Login e Registro
if (document.getElementById('loginForm')) {
    document.getElementById('loginForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            const res = await fetch(`${API_URL}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    email: document.getElementById('email').value,
                    password: document.getElementById('password').value
                })
            });
            const data = await res.json();
            if (res.ok && data.token) {
                localStorage.setItem('token', data.token);
                showMsg('loginSuccess', 'Login efetuado com sucesso! Redirecionando...', true);
                setTimeout(() => {
                    if (data.role === 'TUTOR') window.location.href = 'dashboard-tutor.html';
                    else if (data.role === 'CLINICA') window.location.href = 'dashboard-clinica.html';
                    else window.location.href = 'dashboard-vet.html';
                }, 800);
            } else {
                showMsg('loginError', parseErrors(data));
            }
        } catch(err) {
            showMsg('loginError', 'Erro de conexão com o servidor.');
        }
    });

    document.getElementById('registerForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            const res = await fetch(`${API_URL}/auth/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    role: document.getElementById('role').value,
                    name: document.getElementById('regName').value,
                    email: document.getElementById('regEmail').value,
                    password: document.getElementById('regPassword').value,
                    age: document.getElementById('regAge').value,
                    phone: document.getElementById('regPhone').value,
                    crmv: document.getElementById('regCrmv').value
                })
            });
            const data = await res.json();
            if (res.ok && data.token) {
                localStorage.setItem('token', data.token);
                showMsg('regError', 'Cadastro realizado com sucesso! Redirecionando...', true);
                setTimeout(() => {
                    if (data.role === 'TUTOR') window.location.href = 'dashboard-tutor.html';
                    else if (data.role === 'CLINICA') window.location.href = 'dashboard-clinica.html';
                    else window.location.href = 'dashboard-vet.html';
                }, 800);
            } else {
                showMsg('regError', parseErrors(data), false);
            }
        } catch(err) {
            showMsg('regError', 'Erro de conexão com o servidor.', false);
        }
    });
}

// ==========================================
// FLUXO DO TUTOR (PETS, CLÍNICAS, AGENDAMENTOS)
// ==========================================
let racasGlobais = [];

async function carregarRacas() {
    try {
        const res = await fetch(`${API_URL}/racas`, { headers: { 'Authorization': 'Bearer ' + getToken() }});
        racasGlobais = await res.json();
        const breedList = document.getElementById('breedList');
        if (breedList) {
            let opts = '';
            racasGlobais.forEach(r => {
                opts += `<option value="${r.nome}">${r.especie.nome}</option>`;
            });
            breedList.innerHTML = opts;
        }
    } catch(err) { console.error('Erro ao carregar racas', err); }
}

async function carregarPets() {
    try {
        const res = await fetch(`${API_URL}/pets`, { headers: { 'Authorization': 'Bearer ' + getToken() }});
        if (res.status === 403) { logout(); return; }
        const pets = await res.json();
        let html = ''; let opts = '';
        if (pets.length === 0) {
            html = '<p style="color:var(--text-muted); font-style:italic;">Nenhum pet cadastrado ainda. Adicione o seu primeiro pet abaixo!</p>';
        } else {
            pets.forEach(p => {
                const racaNome = p.raca ? p.raca.nome : 'Sem Raça';
                html += `
                    <div class='card'>
                        <div style="display:flex; justify-content:space-between; align-items:center;">
                            <strong>🐾 ${p.nome}</strong>
                            <span class="badge" style="background:rgba(0,217,245,0.12); color:#00d9f5; border:1px solid rgba(0,217,245,0.25);">${racaNome}</span>
                        </div>
                        <span style="color:var(--text-secondary); font-size:12.5px;">Nascimento: ${p.dataNascimento}</span>
                        <span style="font-size:11px; color:var(--text-muted);">ID do Pet: ${p.idPet}</span>
                    </div>`;
                opts += `<option value='${p.idPet}'>${p.nome} (${racaNome})</option>`;
            });
        }
        const petsList = document.getElementById('petsList');
        if (petsList) petsList.innerHTML = html;
        const selPet = document.getElementById('selPet');
        if (selPet) selPet.innerHTML = opts || '<option value="">Nenhum pet encontrado</option>';
    } catch(err) { showMsg('globalError', 'Erro ao carregar pets'); }
}

async function cadastrarPet() {
    let breedName = document.getElementById('petBreed').value;
    let breedObj = racasGlobais.find(r => r.nome.toLowerCase() === breedName.toLowerCase());
    
    const res = await fetch(`${API_URL}/pets`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() },
        body: JSON.stringify({
            nome: document.getElementById('petName').value,
            dataNascimento: '2020-01-01',
            raca: breedObj ? { idRaca: breedObj.idRaca } : null
        })
    });
    if (res.ok) {
        showMsg('globalSuccess', 'Pet cadastrado com sucesso!', true);
        document.getElementById('petName').value = '';
        document.getElementById('petBreed').value = '';
        carregarPets();
    } else {
        const data = await res.json();
        showMsg('globalError', parseErrors(data));
    }
}

async function carregarClinicas() {
    try {
        const res = await fetch(`${API_URL}/clinicas`, { headers: { 'Authorization': 'Bearer ' + getToken() }});
        const clinicas = await res.json();
        let opts = '<option value="">Selecione uma Clínica...</option>';
        clinicas.forEach(c => {
            opts += `<option value="${c.idClinica}">${c.nomeFantasia} — ${c.endereco || 'Unidade Geral'}</option>`;
        });
        const selClinica = document.getElementById('selClinica');
        if (selClinica) {
            selClinica.innerHTML = opts;
            if (clinicas.length > 0) {
                selClinica.selectedIndex = 1;
                carregarVetsDaClinicaSelecionada();
            }
        }
    } catch(err) {
        showMsg('globalError', 'Erro ao carregar clínicas');
    }
}

async function carregarVetsDaClinicaSelecionada() {
    const selClinica = document.getElementById('selClinica');
    const selVet = document.getElementById('selVet');
    if (!selClinica || !selVet) return;

    const idClinica = selClinica.value;
    if (!idClinica) {
        selVet.innerHTML = '<option value="">Selecione uma clínica primeiro</option>';
        return;
    }

    try {
        const res = await fetch(`${API_URL}/clinicas/${idClinica}/veterinarios`, {
            headers: { 'Authorization': 'Bearer ' + getToken() }
        });
        const vets = await res.json();
        let opts = '';
        if (!vets || vets.length === 0) {
            opts = '<option value="">Nenhum veterinário disponível nesta clínica</option>';
        } else {
            vets.forEach(v => {
                const prefix = (v.nome && v.nome.startsWith('Dr')) ? '' : 'Dr(a). ';
                opts += `<option value="${v.idVeterinario}">${prefix}${v.nome} (${v.especialidade || 'Clínico Geral'})</option>`;
            });
        }
        selVet.innerHTML = opts;
    } catch(err) {
        showMsg('globalError', 'Erro ao carregar veterinários da clínica');
    }
}

async function agendarConsulta() {
    const selClinica = document.getElementById('selClinica');
    const selVet = document.getElementById('selVet');
    const selPet = document.getElementById('selPet');
    const appDate = document.getElementById('appDate');
    const appModality = document.getElementById('appModality');

    if (!selClinica.value || !selVet.value || !selPet.value || !appDate.value) {
        showMsg('globalError', 'Preencha todos os campos para agendar a consulta.');
        return;
    }

    const res = await fetch(`${API_URL}/consultas`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() },
        body: JSON.stringify({
            clinica: { idClinica: selClinica.value },
            veterinario: { idVeterinario: selVet.value },
            pet: { idPet: selPet.value },
            dataHora: appDate.value,
            modalidade: appModality.value
        })
    });
    if (res.ok) {
        showMsg('globalSuccess', 'Consulta agendada com sucesso! Autorização gerada automaticamente.', true);
        carregarNotificacoes();
        if (typeof carregarConsultasTutor === 'function') carregarConsultasTutor();
        if (typeof carregarAutorizacoesTutor === 'function') carregarAutorizacoesTutor();
    } else {
        const data = await res.json();
        showMsg('globalError', parseErrors(data));
    }
}

async function carregarConsultasTutor() {
    try {
        const res = await fetch(`${API_URL}/consultas`, { headers: { 'Authorization': 'Bearer ' + getToken() }});
        if (res.status === 403) return;
        const apps = await res.json();
        let html = '';
        if (!apps || apps.length === 0) {
            html = '<p style="color:var(--text-muted); font-style:italic;">Nenhuma consulta agendada.</p>';
        } else {
            apps.forEach(c => {
                const d = new Date(c.dataHora).toLocaleString('pt-BR');
                const petNome = c.pet ? c.pet.nome : 'Pet';
                const racaNome = (c.pet && c.pet.raca) ? ` (${c.pet.raca.nome})` : '';
                const vetNome = c.veterinario ? `Dr(a). ${c.veterinario.nome}` : 'Veterinário';
                const clinicaNome = c.clinica ? `Unidade: ${c.clinica.nomeFantasia}` : '';
                const status = c.status || 'AGENDADO';
                
                let badgeHtml = '';
                if (status === 'AGENDADO') {
                    badgeHtml = '<span class="badge badge-agendado">● Agendada</span>';
                } else if (status === 'CONCLUIDA') {
                    badgeHtml = '<span class="badge badge-concluida">✔ Concluída</span>';
                } else {
                    badgeHtml = '<span class="badge badge-cancelada">✖ Cancelada</span>';
                }

                let cancelBtn = '';
                if (status === 'AGENDADO') {
                    cancelBtn = `<button class="btn-danger btn-sm" style="margin-top:8px;" onclick="cancelarConsulta('${c.idConsulta}')">Cancelar Consulta</button>`;
                }

                html += `
                    <div class='card'>
                        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:4px;">
                            <strong>🐾 ${petNome}${racaNome}</strong>
                            ${badgeHtml}
                        </div>
                        <span>👨‍⚕️ Veterinário: <b>${vetNome}</b></span>
                        <span>🏥 ${clinicaNome}</span>
                        <span>📅 Horário: <b>${d}</b> | Modalidade: <b>${c.modalidade}</b></span>
                        ${cancelBtn}
                    </div>`;
            });
        }
        const container = document.getElementById('consultasTutorList');
        if (container) container.innerHTML = html;
    } catch(err) {
        showMsg('globalError', 'Erro ao carregar consultas do tutor');
    }
}

async function cancelarConsulta(idConsulta) {
    if (!confirm('Deseja realmente cancelar esta consulta agendada?')) return;
    try {
        const res = await fetch(`${API_URL}/consultas/${idConsulta}/cancelar`, {
            method: 'PUT',
            headers: { 'Authorization': 'Bearer ' + getToken() }
        });
        if (res.ok) {
            showMsg('globalSuccess', 'Consulta cancelada com sucesso!', true);
            carregarConsultasTutor();
            carregarNotificacoes();
        } else {
            const data = await res.json();
            showMsg('globalError', parseErrors(data));
        }
    } catch (err) {
        showMsg('globalError', 'Erro ao cancelar consulta');
    }
}

async function carregarAutorizacoesTutor() {
    try {
        const res = await fetch(`${API_URL}/autorizacoes`, { headers: { 'Authorization': 'Bearer ' + getToken() }});
        if (res.status === 403) return;
        const auths = await res.json();
        let html = '';
        if (!auths || auths.length === 0) {
            html = '<p style="color:var(--text-muted); font-style:italic;">Nenhuma autorização de acesso ativa no momento.</p>';
        } else {
            auths.forEach(a => {
                const petNome = a.pet ? a.pet.nome : 'Pet';
                const vetNome = a.veterinario ? a.veterinario.nome : 'Veterinário';
                const clinicaNome = a.clinica ? a.clinica.nomeFantasia : 'Clínica Geral';
                const status = a.status || 'ATIVA';
                
                const badgeHtml = status === 'ATIVA' ? 
                    '<span class="badge badge-concluida">● Ativa</span>' : 
                    '<span class="badge badge-cancelada">✖ Revogada</span>';

                let actionBtn = '';
                if (status === 'ATIVA') {
                    actionBtn = `<button class="btn-danger btn-sm" style="margin-top:8px;" onclick="revogarAutorizacaoTutor('${a.idAutorizacao}', '${petNome}', '${vetNome}', '${clinicaNome}')">Revogar Acesso do Médico</button>`;
                }

                html += `
                    <div class='card'>
                        <div style="display:flex; justify-content:space-between; align-items:center;">
                            <strong>🐾 Pet: ${petNome}</strong>
                            ${badgeHtml}
                        </div>
                        <span>👨‍⚕️ Veterinário Autorizado: <b>Dr(a). ${vetNome}</b></span>
                        <span>🏥 Unidade: <b>${clinicaNome}</b></span>
                        ${actionBtn}
                    </div>`;
            });
        }
        const container = document.getElementById('autorizacoesList');
        if (container) container.innerHTML = html;
    } catch(err) {
        showMsg('globalError', 'Erro ao carregar autorizações do tutor');
    }
}

async function revogarAutorizacaoTutor(idAuth, petNome, vetNome, clinicaNome) {
    const confirm1 = confirm(
        `ATENÇÃO (Etapa 1 de 2):\n\n` +
        `Ao revogar o acesso, Dr(a). ${vetNome} e a clínica ${clinicaNome} perderão imediatamente o acesso ao prontuário médico e histórico do pet ${petNome}.\n\n` +
        `Deseja prosseguir para a confirmação final?`
    );
    if (!confirm1) return;

    const confirm2 = confirm(
        `CONFIRMAÇÃO FINAL DE SEGURANÇA (Etapa 2 de 2):\n\n` +
        `Todas as consultas ativas/agendadas para o pet ${petNome} com este médico veterinário serão AUTOMATICAMENTE CANCELADAS.\n\n` +
        `Tem certeza absoluta que deseja revogar o vínculo agora?`
    );
    if (!confirm2) return;

    try {
        const res = await fetch(`${API_URL}/autorizacoes/${idAuth}/revogar`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() },
            body: JSON.stringify({ motivo: 'Revogado pelo tutor via painel do tutor.' })
        });
        if (res.ok) {
            showMsg('globalSuccess', 'Autorização revogada e consultas canceladas com sucesso.', true);
            carregarAutorizacoesTutor();
            carregarConsultasTutor();
            carregarNotificacoes();
        } else {
            const data = await res.json();
            showMsg('globalError', parseErrors(data));
        }
    } catch (err) {
        showMsg('globalError', 'Erro ao revogar autorização');
    }
}

// ==========================================
// FLUXO DO VETERINÁRIO
// ==========================================
async function finalizarConsulta() {
    const appId = document.getElementById('appId').value;
    const notes = document.getElementById('notes').value;
    if (!appId) {
        showMsg('globalError', 'Selecione ou informe o ID da consulta agendada.');
        return;
    }

    const res = await fetch(`${API_URL}/appointments/${appId}/complete`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() },
        body: JSON.stringify({ clinicalNotes: notes })
    });
    if (res.ok) {
        showMsg('globalSuccess', 'Consulta finalizada e prontuário salvo com sucesso!', true);
        document.getElementById('appId').value = '';
        document.getElementById('notes').value = '';
        carregarNotificacoes();
        if (typeof carregarConsultasVet === 'function') carregarConsultasVet();
    } else {
        const data = await res.json();
        showMsg('globalError', parseErrors(data));
    }
}

async function carregarConsultasVet() {
    try {
        const res = await fetch(`${API_URL}/consultas`, { headers: { 'Authorization': 'Bearer ' + getToken() }});
        if (res.status === 403) return;
        const apps = await res.json();
        let html = '';
        if (!apps || apps.length === 0) {
            html = '<p style="color:var(--text-muted); font-style:italic;">Nenhuma consulta agendada no momento.</p>';
        } else {
            apps.forEach(c => {
                const d = new Date(c.dataHora).toLocaleString('pt-BR');
                const petNome = c.pet ? c.pet.nome : 'Pet';
                const racaNome = (c.pet && c.pet.raca) ? ` - ${c.pet.raca.nome}` : '';
                const clinicaNome = c.clinica ? ` | Unidade: ${c.clinica.nomeFantasia}` : '';
                const status = c.status || 'AGENDADO';
                
                let badgeHtml = '';
                if (status === 'AGENDADO') {
                    badgeHtml = '<span class="badge badge-agendado">● Agendada</span>';
                } else if (status === 'CONCLUIDA') {
                    badgeHtml = '<span class="badge badge-concluida">✔ Concluída</span>';
                } else {
                    badgeHtml = '<span class="badge badge-cancelada">✖ Cancelada</span>';
                }

                const isClickable = status === 'AGENDADO' ? `onclick="document.getElementById('appId').value='${c.idConsulta}'; document.getElementById('notes').focus();"` : '';
                const hint = status === 'AGENDADO' ? `<span style='font-size:11.5px;color:var(--border-focus);display:block;margin-top:6px;font-weight:600;'>👉 Clique para selecionar e atender</span>` : '';

                html += `
                    <div class='card' style='${status === 'AGENDADO' ? 'cursor:pointer;' : ''}' ${isClickable}>
                        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:4px;">
                            <strong>🐾 ${petNome}${racaNome}</strong>
                            ${badgeHtml}
                        </div>
                        <span>📅 Horário: <b>${d}</b> | Modalidade: <b>${c.modalidade}${clinicaNome}</b></span>
                        <span style="font-size:11px; color:var(--text-muted);">ID: ${c.idConsulta}</span>
                        ${hint}
                    </div>`;
            });
        }
        const container = document.getElementById('consultasList');
        if (container) container.innerHTML = html;
    } catch(err) { showMsg('globalError', 'Erro ao carregar consultas'); }
}

async function carregarNotificacoes() {
    try {
        const res = await fetch(`${API_URL}/notificacoes`, { headers: { 'Authorization': 'Bearer ' + getToken() }});
        if (res.status === 403) return;
        const notifs = await res.json();
        let html = '';
        if (!notifs || notifs.length === 0) {
            html = '<p style="color:var(--text-muted); font-style:italic;">Nenhuma notificação recente.</p>';
        } else {
            notifs.forEach(n => { 
                const d = new Date(n.dataCriacao).toLocaleString('pt-BR');
                html += `
                    <div class='card' style='border-left: 3px solid #00d9f5;'>
                        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:2px;">
                            <span style="font-size:11px; font-weight:700; color:var(--border-focus); letter-spacing:0.5px;">🔔 NOTIFICAÇÃO</span>
                            <span style="font-size:11px; color:var(--text-muted);">${d}</span>
                        </div>
                        <span style="color:var(--text-primary); font-size:13px;">${n.mensagem}</span>
                    </div>`; 
            });
        }
        const container = document.getElementById('notifList');
        if (container) container.innerHTML = html;
    } catch(err) { showMsg('globalError', 'Erro ao carregar notificações'); }
}

// ==========================================
// FLUXO DO PAINEL DA CLÍNICA
// ==========================================
let vetsDaClinicaCache = [];

async function carregarDadosClinica() {
    try {
        const res = await fetch(`${API_URL}/clinicas/minha`, { headers: { 'Authorization': 'Bearer ' + getToken() }});
        if (res.status === 403) { logout(); return; }
        const c = await res.json();
        const nomeEl = document.getElementById('clinicaNome');
        const infoEl = document.getElementById('clinicaInfo');
        if (nomeEl) nomeEl.innerText = c.nomeFantasia || c.razaoSocial;
        if (infoEl) infoEl.innerText = `${c.razaoSocial} | ${c.endereco || 'Endereço Geral'} | Tel: ${c.telefone || 'Não informado'}`;
    } catch(err) { showMsg('globalError', 'Erro ao carregar dados da clínica'); }
}

async function carregarVeterinariosClinica() {
    try {
        const res = await fetch(`${API_URL}/clinicas/meus-veterinarios`, { headers: { 'Authorization': 'Bearer ' + getToken() }});
        const vinculos = await res.json();
        vetsDaClinicaCache = vinculos.filter(v => v.statusVinculo === 'ATIVO').map(v => v.veterinario);

        let html = '';
        if (!vinculos || vinculos.length === 0) {
            html = '<p style="color:var(--text-muted); font-style:italic;">Nenhum médico veterinário vinculado à unidade ainda.</p>';
        } else {
            vinculos.forEach(v => {
                const isAtivo = v.statusVinculo === 'ATIVO';
                const badgeHtml = isAtivo ? 
                    '<span class="badge badge-ativo">● Ativo</span>' : 
                    '<span class="badge badge-cancelada">Desativado</span>';
                
                const action = isAtivo ? `<button class="btn-secondary btn-sm" style="margin-top:8px;" onclick="desvincularVeterinario('${v.idVeterinarioClinica}')">Desativar Vínculo</button>` : '';

                html += `
                    <div class='card'>
                        <div style="display:flex; justify-content:space-between; align-items:center;">
                            <strong>👨‍⚕️ Dr(a). ${v.veterinario.nome}</strong>
                            ${badgeHtml}
                        </div>
                        <span>Especialidade: <b>${v.veterinario.especialidade || 'Clínico Geral'}</b></span>
                        <span style="font-size:11.5px; color:var(--text-muted);">Início do Vínculo: ${v.dataInicio}</span>
                        ${action}
                    </div>`;
            });
        }
        const container = document.getElementById('equipeVetList');
        if (container) container.innerHTML = html;
    } catch(err) { showMsg('globalError', 'Erro ao carregar corpo clínico'); }
}

async function carregarTodosVeterinariosDisponiveis() {
    try {
        const res = await fetch(`${API_URL}/veterinarios`, { headers: { 'Authorization': 'Bearer ' + getToken() }});
        const vets = await res.json();
        let opts = '<option value="">Selecione um profissional registrado...</option>';
        vets.forEach(v => {
            opts += `<option value="${v.idVeterinario}">Dr(a). ${v.nome} (${v.especialidade || 'Clínico Geral'})</option>`;
        });
        const sel = document.getElementById('selTodosVets');
        if (sel) sel.innerHTML = opts;
    } catch(err) { console.error('Erro ao listar todos veterinarios', err); }
}

async function vincularVeterinario() {
    const sel = document.getElementById('selTodosVets');
    if (!sel || !sel.value) {
        showMsg('globalError', 'Selecione um médico veterinário para vincular à unidade.');
        return;
    }
    try {
        const res = await fetch(`${API_URL}/clinicas/veterinarios/vincular`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() },
            body: JSON.stringify({ idVeterinario: sel.value })
        });
        if (res.ok) {
            showMsg('globalSuccess', 'Veterinário vinculado à equipe com sucesso!', true);
            carregarVeterinariosClinica();
            carregarNotificacoes();
        } else {
            const data = await res.json();
            showMsg('globalError', parseErrors(data));
        }
    } catch(err) { showMsg('globalError', 'Erro ao vincular veterinário'); }
}

async function desvincularVeterinario(idVinculo) {
    if (!confirm('Deseja realmente desativar o vínculo deste profissional com a unidade?')) return;
    try {
        const res = await fetch(`${API_URL}/clinicas/veterinarios/${idVinculo}/desvincular`, {
            method: 'PUT',
            headers: { 'Authorization': 'Bearer ' + getToken() }
        });
        if (res.ok) {
            showMsg('globalSuccess', 'Vínculo desativado com sucesso.', true);
            carregarVeterinariosClinica();
        } else {
            const data = await res.json();
            showMsg('globalError', parseErrors(data));
        }
    } catch(err) { showMsg('globalError', 'Erro ao desvincular veterinário'); }
}

async function carregarConsultasClinica() {
    try {
        const res = await fetch(`${API_URL}/clinicas/consultas`, { headers: { 'Authorization': 'Bearer ' + getToken() }});
        if (res.status === 403) return;
        const apps = await res.json();
        let html = '';
        if (!apps || apps.length === 0) {
            html = '<p style="color:var(--text-muted); font-style:italic;">Nenhuma consulta registrada para esta unidade.</p>';
        } else {
            apps.forEach(c => {
                const d = new Date(c.dataHora).toLocaleString('pt-BR');
                const petNome = c.pet ? c.pet.nome : 'Pet';
                const racaNome = (c.pet && c.pet.raca) ? ` (${c.pet.raca.nome})` : '';
                const vetNome = c.veterinario ? `Dr(a). ${c.veterinario.nome}` : 'Veterinário';
                const status = c.status || 'AGENDADO';
                
                let badgeHtml = '';
                if (status === 'AGENDADO') {
                    badgeHtml = '<span class="badge badge-agendado">● Agendada</span>';
                } else if (status === 'CONCLUIDA') {
                    badgeHtml = '<span class="badge badge-concluida">✔ Concluída</span>';
                } else {
                    badgeHtml = '<span class="badge badge-cancelada">✖ Cancelada</span>';
                }

                html += `
                    <div class='card'>
                        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:4px;">
                            <strong>🐾 ${petNome}${racaNome}</strong>
                            ${badgeHtml}
                        </div>
                        <span>👨‍⚕️ Médico Responsável: <b>${vetNome}</b></span>
                        <span>📅 Horário: <b>${d}</b> (${c.modalidade})</span>
                    </div>`;
            });
        }
        const container = document.getElementById('consultasClinicaList');
        if (container) container.innerHTML = html;
    } catch(err) { showMsg('globalError', 'Erro ao carregar consultas da clínica'); }
}

async function carregarAutorizacoesClinica() {
    try {
        const res = await fetch(`${API_URL}/clinicas/autorizacoes`, { headers: { 'Authorization': 'Bearer ' + getToken() }});
        if (res.status === 403) return;
        const auths = await res.json();
        let html = '';
        if (!auths || auths.length === 0) {
            html = '<p style="color:var(--text-muted); font-style:italic;">Nenhuma autorização de paciente nesta unidade.</p>';
        } else {
            auths.forEach(a => {
                const petNome = a.pet ? a.pet.nome : 'Pet';
                const vetNome = a.veterinario ? a.veterinario.nome : 'Veterinário';
                const status = a.status || 'ATIVA';
                const badgeHtml = status === 'ATIVA' ? 
                    '<span class="badge badge-concluida">● Ativa</span>' : 
                    '<span class="badge badge-cancelada">✖ Inativa</span>';

                let transferBtn = '';
                if (status === 'ATIVA') {
                    transferBtn = `<button class="btn-secondary btn-sm" style="margin-top:8px;" onclick="abrirModalTransfer('${a.idAutorizacao}', '${petNome}', '${vetNome}')">🔄 Transferir para outro Médico</button>`;
                }

                html += `
                    <div class='card'>
                        <div style="display:flex; justify-content:space-between; align-items:center;">
                            <strong>🐾 Paciente: ${petNome}</strong>
                            ${badgeHtml}
                        </div>
                        <span>👨‍⚕️ Médico Vinculado: <b>Dr(a). ${vetNome}</b></span>
                        ${transferBtn}
                    </div>`;
            });
        }
        const container = document.getElementById('autorizacoesClinicaList');
        if (container) container.innerHTML = html;
    } catch(err) { showMsg('globalError', 'Erro ao carregar autorizações da clínica'); }
}

function abrirModalTransfer(idAuth, petNome, vetAtualNome) {
    document.getElementById('transferAuthId').value = idAuth;
    document.getElementById('transferPetInfo').innerText = `Paciente: ${petNome} | Atendimento atual: Dr(a). ${vetAtualNome}`;

    let opts = '<option value="">Selecione o novo médico da equipe...</option>';
    vetsDaClinicaCache.forEach(v => {
        opts += `<option value="${v.idVeterinario}">Dr(a). ${v.nome} (${v.especialidade || 'Geral'})</option>`;
    });
    document.getElementById('selNovoVetTransfer').innerHTML = opts;
    document.getElementById('modalTransfer').style.display = 'flex';
}

function fecharModalTransfer() {
    document.getElementById('modalTransfer').style.display = 'none';
}

async function confirmarTransferencia() {
    const idAuth = document.getElementById('transferAuthId').value;
    const idNovoVet = document.getElementById('selNovoVetTransfer').value;
    if (!idNovoVet) {
        alert('Selecione o médico veterinário substituto.');
        return;
    }

    try {
        const res = await fetch(`${API_URL}/clinicas/autorizacoes/${idAuth}/transferir`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() },
            body: JSON.stringify({ idNovoVeterinario: idNovoVet })
        });
        if (res.ok) {
            fecharModalTransfer();
            showMsg('globalSuccess', 'Atendimento e autorização transferidos com sucesso!', true);
            carregarAutorizacoesClinica();
            carregarConsultasClinica();
            carregarNotificacoes();
        } else {
            const data = await res.json();
            alert(parseErrors(data));
        }
    } catch(err) { alert('Erro ao transferir autorização'); }
}
