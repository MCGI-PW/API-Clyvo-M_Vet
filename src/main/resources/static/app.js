const API_URL = 'http://localhost:8080/api';

function getToken() {
    return localStorage.getItem('token');
}

function logout() {
    localStorage.removeItem('token');
    window.location.href = 'login.html';
}

if(document.getElementById('loginForm')) {
    document.getElementById('loginForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const res = await fetch(\/auth/login, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: document.getElementById('email').value,
                password: document.getElementById('password').value
            })
        });
        const data = await res.json();
        if (data.token) {
            localStorage.setItem('token', data.token);
            if (data.role === 'ROLE_TUTOR') window.location.href = 'dashboard-tutor.html';
            else window.location.href = 'dashboard-vet.html';
        } else {
            alert('Falha no login');
        }
    });

    document.getElementById('registerForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const res = await fetch(\/auth/register, {
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
        if (data.token) {
            localStorage.setItem('token', data.token);
            if (data.role === 'ROLE_TUTOR') window.location.href = 'dashboard-tutor.html';
            else window.location.href = 'dashboard-vet.html';
        }
    });
}

async function carregarPets() {
    const res = await fetch(\/pets, { headers: { 'Authorization': 'Bearer ' + getToken() }});
    const pets = await res.json();
    let html = '';
    let opts = '';
    pets.forEach(p => {
        html += <div class='card'>Nome: \ - Raca: \ (ID: \)</div>;
        opts += <option value='\'>\</option>;
    });
    document.getElementById('petsList').innerHTML = html;
    document.getElementById('selPet').innerHTML = opts;
}

async function cadastrarPet() {
    await fetch(\/pets, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() },
        body: JSON.stringify({
            name: document.getElementById('petName').value,
            age: document.getElementById('petAge').value,
            breed: document.getElementById('petBreed').value
        })
    });
    alert('Pet salvo!');
    carregarPets();
}

async function carregarVeterinarios() {
    const res = await fetch(\/veterinarians, { headers: { 'Authorization': 'Bearer ' + getToken() }});
    const vets = await res.json();
    let opts = '';
    vets.forEach(v => { opts += <option value='\'>\ (CRMV: \)</option>; });
    document.getElementById('selVet').innerHTML = opts;
}

async function agendarConsulta() {
    await fetch(\/appointments/schedule, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() },
        body: JSON.stringify({
            veterinarianId: document.getElementById('selVet').value,
            petId: document.getElementById('selPet').value,
            appointmentDate: document.getElementById('appDate').value,
            modality: document.getElementById('appModality').value
        })
    });
    alert('Agendado com sucesso! Notificacao enviada.');
    carregarNotificacoes();
}

async function finalizarConsulta() {
    await fetch(\/appointments/\/complete, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() },
        body: JSON.stringify({ clinicalNotes: document.getElementById('notes').value })
    });
    alert('Consulta finalizada! Notificacao enviada ao tutor.');
}

async function carregarNotificacoes() {
    const res = await fetch(\/notifications, { headers: { 'Authorization': 'Bearer ' + getToken() }});
    const notifs = await res.json();
    let html = '';
    notifs.forEach(n => { html += <div class='card'>\: \</div>; });
    document.getElementById('notifList').innerHTML = html;
}
