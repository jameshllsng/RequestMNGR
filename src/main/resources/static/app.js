const apiUrl = "/api/purchase-requests";
const priorityLabels = { LOW: "Baixa", MEDIUM: "Média", HIGH: "Alta" };
const statusLabels = { OPEN: "Aberta", PENDING_APPROVAL: "Aguardando aprovação", APPROVED: "Aprovada", IN_PURCHASE: "Em compra", PARTIALLY_RECEIVED: "Recebida parcialmente", COMPLETED: "Concluída", CANCELLED: "Cancelada" };
const summaryDefinitions = [["OPEN", "Abertas"], ["PENDING_APPROVAL", "Aguardando aprovação"], ["IN_PURCHASE", "Em compra"], ["PARTIALLY_RECEIVED", "Parcialmente recebidas"], ["COMPLETED", "Concluídas"]];

const dialog = document.querySelector("#create-request-dialog");
const createForm = document.querySelector("#create-request-form");
const itemForms = document.querySelector("#item-forms");
const itemFormTemplate = document.querySelector("#item-form-template");
const formMessage = document.querySelector("#form-message");
let csrfToken;

document.querySelector("#open-create-form").addEventListener("click", openCreateDialog);
document.querySelector("#close-create-form").addEventListener("click", closeCreateDialog);
document.querySelector("#cancel-create").addEventListener("click", closeCreateDialog);
document.querySelector("#add-item").addEventListener("click", addItemForm);
document.querySelector("#refresh-list").addEventListener("click", loadPurchaseRequests);
document.querySelector("#logout").addEventListener("click", logout);
createForm.addEventListener("submit", createPurchaseRequest);

function openCreateDialog() { formMessage.hidden = true; dialog.showModal(); }
function closeCreateDialog() { dialog.close(); createForm.reset(); itemForms.replaceChildren(); addItemForm(); setTodayAsRequestedDate(); }
function addItemForm() { const itemForm = itemFormTemplate.content.cloneNode(true); itemForm.querySelector(".remove-item").addEventListener("click", event => { event.currentTarget.closest(".item-form").remove(); updateRemoveButtons(); }); itemForms.append(itemForm); updateRemoveButtons(); }
function updateRemoveButtons() { const removeButtons = itemForms.querySelectorAll(".remove-item"); removeButtons.forEach(button => button.hidden = removeButtons.length === 1); }

async function loadCsrfToken() { const response = await fetch("/api/csrf"); if (!response.ok) throw new Error("Não foi possível iniciar a sessão."); csrfToken = await response.json(); }
async function loadAuthenticatedUser() { const response = await fetch("/api/auth/me"); if (response.status === 401) { window.location.assign("/login.html"); return false; } if (!response.ok) throw new Error("Não foi possível identificar o usuário autenticado."); const user = await response.json(); document.querySelector("#current-user").textContent = `${user.username} · ${user.role}`; return true; }

async function loadPurchaseRequests() { try { const response = await fetch(apiUrl); if (!response.ok) throw new Error(await readErrorMessage(response)); const purchaseRequests = await response.json(); renderSummaryCards(purchaseRequests); renderPurchaseRequestTable(purchaseRequests); } catch (error) { document.querySelector("#request-count").textContent = error.message; } }
function renderSummaryCards(purchaseRequests) { const summaryCards = document.querySelector("#summary-cards"); summaryCards.replaceChildren(); summaryDefinitions.forEach(([status, label]) => { const card = document.createElement("article"); card.className = "summary-card"; const title = document.createElement("p"); title.textContent = label; const value = document.createElement("strong"); value.textContent = purchaseRequests.filter(purchaseRequest => purchaseRequest.status === status).length; card.append(title, value); summaryCards.append(card); }); }
function renderPurchaseRequestTable(purchaseRequests) { const tableBody = document.querySelector("#request-table-body"); const emptyState = document.querySelector("#empty-state"); tableBody.replaceChildren(); document.querySelector("#request-count").textContent = `${purchaseRequests.length} requisição(ões) encontrada(s)`; emptyState.hidden = purchaseRequests.length !== 0; purchaseRequests.forEach(purchaseRequest => { const row = document.createElement("tr"); appendCell(row, purchaseRequest.requestNumber); appendCell(row, purchaseRequest.department); appendCell(row, purchaseRequest.requesterName); appendCell(row, purchaseRequest.purchaseReason); appendBadgeCell(row, priorityLabels[purchaseRequest.priority], `priority-${purchaseRequest.priority.toLowerCase()}`); appendBadgeCell(row, statusLabels[purchaseRequest.status], `status-${purchaseRequest.status.toLowerCase().replaceAll("_", "-")}`); appendCell(row, formatDate(purchaseRequest.requestedOn)); appendCell(row, purchaseRequest.itemCount); appendCell(row, formatCurrency(purchaseRequest.totalValue)); tableBody.append(row); }); }
function appendCell(row, value) { const cell = document.createElement("td"); cell.textContent = value ?? "—"; row.append(cell); }
function appendBadgeCell(row, value, className) { const cell = document.createElement("td"); const badge = document.createElement("span"); badge.className = `badge ${className}`; badge.textContent = value; cell.append(badge); row.append(cell); }

async function createPurchaseRequest(event) {
    event.preventDefault(); formMessage.hidden = true;
    const formData = new FormData(createForm);
    const items = [...itemForms.querySelectorAll(".item-form")].map(itemForm => ({ description: itemForm.querySelector(".item-description").value, brandModel: blankToNull(itemForm.querySelector(".item-brand-model").value), quantity: Number(itemForm.querySelector(".item-quantity").value), supplierName: blankToNull(itemForm.querySelector(".item-supplier-name").value), totalValue: Number(itemForm.querySelector(".item-total-value").value), notes: blankToNull(itemForm.querySelector(".item-notes").value) }));
    const request = { requestNumber: formData.get("requestNumber"), department: formData.get("department"), requesterName: formData.get("requesterName"), requestedOn: formData.get("requestedOn"), purchaseReason: formData.get("purchaseReason"), priority: formData.get("priority"), buyerName: blankToNull(formData.get("buyerName")), managerName: blankToNull(formData.get("managerName")), costCenter: blankToNull(formData.get("costCenter")), notes: blankToNull(formData.get("notes")), items };
    try { const response = await fetch(apiUrl, { method: "POST", headers: { "Content-Type": "application/json", [csrfToken.headerName]: csrfToken.token }, body: JSON.stringify(request) }); if (!response.ok) throw new Error(await readErrorMessage(response)); closeCreateDialog(); await loadPurchaseRequests(); } catch (error) { formMessage.textContent = error.message; formMessage.hidden = false; }
}

async function logout() { const response = await fetch("/logout", { method: "POST", headers: { [csrfToken.headerName]: csrfToken.token } }); if (response.status === 204) window.location.assign("/login.html"); else window.alert("Não foi possível encerrar a sessão."); }
async function readErrorMessage(response) { if (response.status === 401) return "Sua sessão expirou. Entre novamente."; if (response.status === 403) return "Você não tem permissão para esta operação."; const error = await response.json().catch(() => null); return error?.errors ? Object.values(error.errors).join(" ") : error?.detail ?? "Não foi possível concluir a operação."; }
function blankToNull(value) { return value?.trim() || null; }
function formatCurrency(value) { return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(value); }
function formatDate(value) { return new Intl.DateTimeFormat("pt-BR", { timeZone: "UTC" }).format(new Date(`${value}T00:00:00Z`)); }
function setTodayAsRequestedDate() { createForm.elements.requestedOn.value = new Date().toISOString().slice(0, 10); }

async function start() { setTodayAsRequestedDate(); addItemForm(); await loadCsrfToken(); if (await loadAuthenticatedUser()) await loadPurchaseRequests(); }
start().catch(error => { document.querySelector("#request-count").textContent = error.message; });
