// ==========================================
// GERENCIADOR DE TEMAS (DARK / LIGHT THEME)
// As páginas são renderizadas no servidor (Thymeleaf); o JavaScript cuida apenas do tema.
// ==========================================
function getStoredTheme() {
    try {
        const saved = localStorage.getItem('clyvo_theme');
        if (saved) return saved;
    } catch (e) {
        // localStorage indisponível (modo privado): segue com a preferência do sistema
    }
    return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

function applyTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    try {
        localStorage.setItem('clyvo_theme', theme);
    } catch (e) {
        // preferência apenas para esta visita
    }
    document.querySelectorAll('.theme-toggle').forEach(btn => {
        const dark = theme === 'dark';
        btn.textContent = dark ? '☀️' : '🌙';
        btn.setAttribute('title', btn.dataset[dark ? 'labelLight' : 'labelDark'] || '');
        btn.setAttribute('aria-pressed', String(dark));
    });
}

function toggleTheme() {
    const current = document.documentElement.getAttribute('data-theme') || 'light';
    applyTheme(current === 'dark' ? 'light' : 'dark');
}

// Aplica o tema antes da renderização para evitar "piscar" e novamente quando o cabeçalho existir
applyTheme(getStoredTheme());
document.addEventListener('DOMContentLoaded', () => {
    applyTheme(getStoredTheme());
    document.querySelectorAll('.theme-toggle').forEach(btn => btn.addEventListener('click', toggleTheme));
    // Confirmação de ações destrutivas sem JavaScript inline nos templates
    document.querySelectorAll('form[data-confirm]').forEach(form => {
        form.addEventListener('submit', event => {
            if (!window.confirm(form.dataset.confirm)) event.preventDefault();
        });
    });
});

// ==========================================
// ASSISTENTE DE IA DA CONSULTA (painel do veterinário)
// Transcrição em tempo real com a Web Speech API do navegador (como a legenda ao vivo),
// e resumo clínico gerado no servidor pela IA, que pré-preenche o formulário do prontuário.
// ==========================================
function setupAiPanel(panel) {
    const form = panel.closest('.clinical-form').querySelector('form');
    const transcript = panel.querySelector('[data-ai-transcript]');
    const interim = panel.querySelector('[data-ai-interim]');
    const status = panel.querySelector('[data-ai-status]');
    const live = panel.querySelector('[data-ai-live]');
    const recordBtn = panel.querySelector('[data-ai-record]');
    const summarizeBtn = panel.querySelector('[data-ai-summarize]');
    const msg = panel.dataset;

    const Recognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    let recognition = null;
    let recording = false;
    let pending = '';

    function setRecording(on) {
        recording = on;
        live.hidden = !on;
        recordBtn.textContent = on ? recordBtn.dataset.labelStop : recordBtn.dataset.labelStart;
        recordBtn.setAttribute('aria-pressed', String(on));
        if (!on) interim.textContent = '';
    }

    if (!Recognition) {
        recordBtn.disabled = true;
        status.textContent = msg.msgUnsupported;
    } else {
        recognition = new Recognition();
        recognition.lang = msg.lang || 'pt-BR';
        recognition.continuous = true;
        recognition.interimResults = true;

        recognition.onresult = event => {
            let partial = '';
            for (let i = event.resultIndex; i < event.results.length; i++) {
                const text = event.results[i][0].transcript.trim();
                if (!text) continue;
                if (event.results[i].isFinal) {
                    appendSentence(text);
                } else {
                    partial += text + ' ';
                }
            }
            pending = partial.trim();
            interim.textContent = pending;
        };
        // O navegador encerra a escuta após silêncio; reinicia enquanto o médico não clicar em parar
        recognition.onend = () => {
            if (!recording) return;
            setTimeout(() => {
                if (!recording) return;
                try {
                    recognition.start();
                } catch (e) {
                    stopListening(msg.msgMicStopped);
                }
            }, 250);
        };
        // Falhas que não se resolvem tentando de novo: para a escuta e mostra o motivo.
        // "no-speech" e "aborted" são pausas normais e a escuta reinicia sozinha no onend.
        recognition.onerror = event => {
            const reason = {
                'not-allowed': msg.msgMicDenied,
                'service-not-allowed': msg.msgUnsupported,
                'language-not-supported': msg.msgUnsupported,
                'audio-capture': msg.msgMicNotFound,
                'network': msg.msgMicNetwork
            }[event.error];
            if (reason) stopListening(reason);
        };

        recordBtn.addEventListener('click', async () => {
            if (recording) {
                stopListening('');
                return;
            }
            if (!window.isSecureContext) {
                status.textContent = msg.msgMicInsecure;
                return;
            }
            // Pede o microfone antes: o navegador mostra a permissão e já sabemos se há microfone
            if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
                try {
                    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
                    stream.getTracks().forEach(track => track.stop());
                } catch (e) {
                    status.textContent = e.name === 'NotFoundError' ? msg.msgMicNotFound : msg.msgMicDenied;
                    return;
                }
            }
            try {
                recognition.start();
            } catch (e) {
                status.textContent = msg.msgMicStopped;
                return;
            }
            setRecording(true);
            status.textContent = msg.msgListening;
        });
    }

    function appendSentence(text) {
        const sentence = text.charAt(0).toUpperCase() + text.slice(1);
        transcript.value += (transcript.value.trim() ? ' ' : '') + sentence + (/[.!?]$/.test(sentence) ? '' : '.');
        transcript.scrollTop = transcript.scrollHeight;
        pending = '';
    }

    function stopListening(message) {
        setRecording(false);
        try { recognition.stop(); } catch (e) { /* já estava parada */ }
        // Alguns navegadores (ex.: Safari) não confirmam a última frase ao parar: ela não pode se perder
        if (pending) appendSentence(pending);
        if (message) status.textContent = message;
    }

    summarizeBtn.addEventListener('click', async () => {
        // Para a escuta antes de ler o texto, para a última frase falada entrar no resumo
        if (recording) stopListening('');
        const text = transcript.value.trim();
        if (!text) {
            status.textContent = msg.msgEmpty;
            return;
        }
        summarizeBtn.disabled = true;
        status.textContent = msg.msgLoading;

        const body = new URLSearchParams({ transcricao: text });
        const csrf = form.querySelector('input[name="_csrf"]');
        if (csrf) body.append('_csrf', csrf.value);

        try {
            const response = await fetch(msg.endpoint, {
                method: 'POST',
                headers: { 'Accept': 'application/json' },
                body
            });
            if (!response.ok) throw new Error(String(response.status));
            fillSuggestions(panel, form, await response.json());
            status.textContent = msg.msgDone;
        } catch (e) {
            status.textContent = msg.msgError;
        } finally {
            summarizeBtn.disabled = false;
        }
    });
}

function fillSuggestions(panel, form, data) {
    ['diagnostico', 'prescricao', 'orientacoes'].forEach(key => {
        const field = form.querySelector(`[data-ai-field="${key}"]`);
        if (field && data[key]) {
            field.value = data[key];
            field.classList.add('ai-filled');
        }
    });

    panel.querySelector('[data-ai-tutor]').textContent = data.resumoTutor || '-';
    const alerts = panel.querySelector('[data-ai-alerts]');
    alerts.replaceChildren(...(data.sinaisDeAlerta || []).map(text => {
        const li = document.createElement('li');
        li.textContent = text;
        return li;
    }));
    panel.querySelector('[data-ai-alerts-box]').hidden = !alerts.children.length;
    panel.querySelector('[data-ai-source]').textContent = 'IA: ' + data.fonte;
    panel.querySelector('[data-ai-result]').hidden = false;
}

document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('[data-ai-panel]').forEach(setupAiPanel);
});
