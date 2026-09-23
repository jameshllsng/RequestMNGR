let csrfToken;
const loginForm = document.querySelector("#login-form");
const loginMessage = document.querySelector("#login-message");
loginForm.addEventListener("submit", login);
loadCsrfToken().catch(() => showError("Não foi possível preparar o login. Atualize a página."));
async function loadCsrfToken() { const response = await fetch("/api/csrf"); if (!response.ok) throw new Error(); csrfToken = await response.json(); }
async function login(event) { event.preventDefault(); loginMessage.hidden = true; if (!csrfToken) { showError("Aguarde o carregamento da página e tente novamente."); return; } const response = await fetch("/login", { method: "POST", headers: { [csrfToken.headerName]: csrfToken.token, "Content-Type": "application/x-www-form-urlencoded" }, body: new URLSearchParams(new FormData(loginForm)) }); if (response.status === 204) { window.location.assign("/"); return; } showError("Usuário ou senha inválidos."); }
function showError(message) { loginMessage.textContent = message; loginMessage.hidden = false; }
