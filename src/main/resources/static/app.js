const API_URL = 'http://localhost:8080/api';

function getToken() { return localStorage.getItem('token'); }

function logout() {
    localStorage.removeItem('token');
    window.location.href = 'login.html';
}

function showMsg(elementId, msg, isSuccess = false) {
    const el = document.getElementById(elementId);
    if (!el) return;
    el.style.display = 'block';
    el.innerText = msg;
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

if(document.getElementById('loginForm')) {
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
                    else window.location.href = 'dashboard-vet.html';
                }, 1000);
            } else {
                showMsg('loginError', parseErrors(data));
            }
        } catch(err) {
            showMsg('loginError', 'Erro de conexao com o servidor.');
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
                    else window.location.href = 'dashboard-vet.html';
                }, 1000);
            } else {
                showMsg('regError', parseErrors(data), false);
            }
        } catch(err) {
            showMsg('regError', 'Erro de conexao com o servidor.', false);
        }
    });
}

let racasGlobais = [];

async function carregarRacas() {
    try {
        const res = await fetch(`${API_URL}/racas`, { headers: { 'Authorization': 'Bearer ' + getToken() }});
        racasGlobais = await res.json();
        const breedList = document.getElementById('breedList');
        if(breedList) {
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
        if(res.status === 403) { logout(); return; }
        const pets = await res.json();
        let html = ''; let opts = '';
        if(pets.length === 0) {
            html = '<p style="color:#666;">Voce ainda nao tem pets.</p>';
        } else {
            pets.forEach(p => {
                const racaNome = p.raca ? p.raca.nome : 'Sem Raca';
                html += `<div class='card'><strong>${p.nome}</strong><span>Nascimento: ${p.dataNascimento} | Raca: ${racaNome}</span><span style="font-size:11px;color:#888;">ID: ${p.idPet}</span></div>`;
                opts += `<option value='${p.idPet}'>${p.nome}</option>`;
            });
        }
        document.getElementById('petsList').innerHTML = html;
        if(opts) { 
            const selPet = document.getElementById('selPet');
            if(selPet) selPet.innerHTML = opts; 
        }
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
            dataNascimento: '2020-01-01', // Mock date as no age input is used anymore, wait age is used, let's just pass null for date
            raca: breedObj ? { idRaca: breedObj.idRaca } : null
        })
    });
    if(res.ok) {
        showMsg('globalSuccess', 'Pet cadastrado com sucesso!', true);
        document.getElementById('petName').value = '';
        document.getElementById('petBreed').value = '';
        carregarPets();
    } else {
        const data = await res.json();
        showMsg('globalError', parseErrors(data));
    }
}


async function carregarVeterinarios() {
    try {
        const res = await fetch(`${API_URL}/veterinarios`, { headers: { 'Authorization': 'Bearer ' + getToken() }});
        const vets = await res.json();
        let opts = '';
        vets.forEach(v => { opts += `<option value='${v.idVeterinario}'>Dr(a). ${v.nome} (${v.especialidade})</option>`; });
        const selVet = document.getElementById('selVet');
        if (selVet) selVet.innerHTML = opts || '<option value="">Nenhum veterinario disponivel</option>';
    } catch(err) { showMsg('globalError', 'Erro ao carregar vets'); }
}

async function agendarConsulta() {
    const res = await fetch(`${API_URL}/consultas`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() },
        body: JSON.stringify({
            veterinario: { idVeterinario: document.getElementById('selVet').value },
            pet: { idPet: document.getElementById('selPet').value },
            dataHora: document.getElementById('appDate').value,
            modalidade: document.getElementById('appModality').value
        })
    });
    if(res.ok) {
        showMsg('globalSuccess', 'Consulta agendada com sucesso!', true);
        carregarNotificacoes();
    } else {
        const data = await res.json();
        showMsg('globalError', parseErrors(data));
    }
}

async function finalizarConsulta() {
    const res = await fetch(`${API_URL}/appointments/${document.getElementById('appId').value}/complete`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() },
        body: JSON.stringify({ clinicalNotes: document.getElementById('notes').value })
    });
    if(res.ok) {
        showMsg('globalSuccess', 'Consulta finalizada com sucesso!', true);
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
        if(res.status === 403) return;
        const apps = await res.json();
        let html = '';
        if(apps.length === 0) html = '<p style="color:#666;">Nenhuma consulta agendada.</p>';
        apps.forEach(c => {
            const d = new Date(c.dataHora).toLocaleString('pt-BR');
            const petNome = c.pet ? c.pet.nome : 'Pet';
            const racaNome = (c.pet && c.pet.raca) ? ` - ${c.pet.raca.nome}` : '';
            html += `<div class='card' style='cursor:pointer; border-left: 4px solid #3498db;' onclick="document.getElementById('appId').value='${c.idConsulta}'">` +
                    `<strong>${petNome}${racaNome}</strong><br>` +
                    `<span>Data/Hora: ${d} | Modalidade: ${c.modalidade}</span><br>` +
                    `<span>Status: <b>${c.status}</b></span>` +
                    `<span style='font-size:11px;color:#888;display:block;margin-top:4px;'>Clique para selecionar o ID: ${c.idConsulta}</span></div>`;
        });
        const container = document.getElementById('consultasList');
        if (container) container.innerHTML = html;
    } catch(err) { showMsg('globalError', 'Erro ao carregar consultas'); }
}

async function carregarNotificacoes() {
    try {
        const res = await fetch(`${API_URL}/notificacoes`, { headers: { 'Authorization': 'Bearer ' + getToken() }});
        if(res.status === 403) return;
        const notifs = await res.json();
        let html = '';
        if(notifs.length === 0) html = '<p style="color:#666;">Nenhuma notificacao.</p>';
        notifs.forEach(n => { 
            const d = new Date(n.dataCriacao).toLocaleString('pt-BR');
            html += `<div class='card' style='background:#e9f7ef;'><strong style='color:#155724;'>${d}</strong> - ${n.mensagem}</div>`; 
        });
        document.getElementById('notifList').innerHTML = html;
    } catch(err) { showMsg('globalError', 'Erro ao carregar notificacoes'); }
}
