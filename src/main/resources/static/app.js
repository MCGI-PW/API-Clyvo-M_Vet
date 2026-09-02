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
                    if (data.role === 'ROLE_TUTOR') window.location.href = 'dashboard-tutor.html';
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
                    if (data.role === 'ROLE_TUTOR') window.location.href = 'dashboard-tutor.html';
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
                html += `<div class='card'><strong>${p.name}</strong><span>Idade: ${p.age} anos | Raca: ${p.breed}</span><span style="font-size:11px;color:#888;">ID: ${p.id}</span></div>`;
                opts += `<option value='${p.id}'>${p.name}</option>`;
            });
        }
        document.getElementById('petsList').innerHTML = html;
        if(opts) document.getElementById('selPet').innerHTML = opts;
    } catch(err) { showMsg('globalError', 'Erro ao carregar pets'); }
}

async function cadastrarPet() {
    const res = await fetch(`${API_URL}/pets`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() },
        body: JSON.stringify({
            name: document.getElementById('petName').value,
            age: document.getElementById('petAge').value,
            breed: document.getElementById('petBreed').value
        })
    });
    if(res.ok) {
        showMsg('globalSuccess', 'Pet cadastrado com sucesso!', true);
        document.getElementById('petName').value = '';
        document.getElementById('petAge').value = '';
        document.getElementById('petBreed').value = '';
        carregarPets();
    } else {
        const data = await res.json();
        showMsg('globalError', parseErrors(data));
    }
}

async function carregarVeterinarios() {
    try {
        const res = await fetch(`${API_URL}/veterinarians`, { headers: { 'Authorization': 'Bearer ' + getToken() }});
        const vets = await res.json();
        let opts = '';
        vets.forEach(v => { opts += `<option value='${v.id}'>Dr(a). ${v.name} (CRMV: ${v.crmv})</option>`; });
        document.getElementById('selVet').innerHTML = opts || '<option value="">Nenhum veterinario disponivel</option>';
    } catch(err) { showMsg('globalError', 'Erro ao carregar vets'); }
}

async function agendarConsulta() {
    const res = await fetch(`${API_URL}/appointments/schedule`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() },
        body: JSON.stringify({
            veterinarianId: document.getElementById('selVet').value,
            petId: document.getElementById('selPet').value,
            appointmentDate: document.getElementById('appDate').value,
            modality: document.getElementById('appModality').value
        })
    });
    if(res.ok) {
        showMsg('globalSuccess', 'Consulta agendada! Uma notificacao foi enviada.', true);
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
    } else {
        const data = await res.json();
        showMsg('globalError', parseErrors(data));
    }
}

async function carregarNotificacoes() {
    try {
        const res = await fetch(`${API_URL}/notifications`, { headers: { 'Authorization': 'Bearer ' + getToken() }});
        if(res.status === 403) return;
        const notifs = await res.json();
        let html = '';
        if(notifs.length === 0) html = '<p style="color:#666;">Nenhuma notificacao.</p>';
        notifs.forEach(n => { 
            const d = new Date(n.sentAt).toLocaleString('pt-BR');
            html += `<div class='card' style='background:#e9f7ef;'><strong style='color:#155724;'>${d}</strong>${n.message}</div>`; 
        });
        document.getElementById('notifList').innerHTML = html;
    } catch(err) { showMsg('globalError', 'Erro ao carregar notificacoes'); }
}
