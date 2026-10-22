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
