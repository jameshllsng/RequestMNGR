const apiUrl = "/api/purchase-requests";
const priorityLabels = { LOW: "Baixa", MEDIUM: "Média", HIGH: "Alta" };
const statusLabels = { OPEN: "Aberta", PENDING_APPROVAL: "Aguardando aprovação", APPROVED: "Aprovada", IN_PURCHASE: "Em compra", PARTIALLY_RECEIVED: "Recebida parcialmente", COMPLETED: "Concluída", CANCELLED: "Cancelada" };
const summaryDefinitions = [["OPEN", "Abertas"], ["PENDING_APPROVAL", "Aguardando aprovação"], ["IN_PURCHASE", "Em compra"], ["PARTIALLY_RECEIVED", "Parcialmente recebidas"], ["COMPLETED", "Concluídas"]];

const dialog = document.querySelector("#create-request-dialog");
const createForm = document.querySelector("#create-request-form");
const itemForms = document.querySelector("#item-forms");
const itemFormTemplate = document.querySelector("#item-form-template");
const formMessage = document.querySelector("#form-message");
const operationFeedback = document.querySelector("#operation-feedback");
let csrfToken;
let editingRequestId = null;
let currentUserRole;
let feedbackTimeout;

document.querySelector("#open-create-form").addEventListener("click", openCreateDialog);
document.querySelector("#close-create-form").addEventListener("click", closeCreateDialog);
document.querySelector("#cancel-create").addEventListener("click", closeCreateDialog);
document.querySelector("#add-item").addEventListener("click", () => addItemForm(true));
document.querySelector("#refresh-list").addEventListener("click", loadPurchaseRequests);
document.querySelector("#logout").addEventListener("click", logout);
createForm.addEventListener("submit", createPurchaseRequest);

function openCreateDialog() { editingRequestId = null; formMessage.hidden = true; dialog.showModal(); }
function closeCreateDialog() { editingRequestId = null; dialog.close(); createForm.reset(); itemForms.replaceChildren(); addItemForm(); setTodayAsRequestedDate(); }
function addItemForm(insertAtTop = false) { const itemForm = itemFormTemplate.content.cloneNode(true); itemForm.querySelector(".remove-item").addEventListener("click", event => { event.currentTarget.closest(".item-form").remove(); updateRemoveButtons(); }); if (insertAtTop) itemForms.prepend(itemForm); else itemForms.append(itemForm); updateRemoveButtons(); }
function updateRemoveButtons() { const removeButtons = itemForms.querySelectorAll(".remove-item"); removeButtons.forEach(button => button.hidden = removeButtons.length === 1); }

async function loadCsrfToken() { const response = await fetch("/api/csrf"); if (!response.ok) throw new Error("Não foi possível iniciar a sessão."); csrfToken = await response.json(); }
async function loadAuthenticatedUser() { const response = await fetch("/api/auth/me"); if (response.status === 401) { window.location.assign("/login.html"); return false; } if (!response.ok) throw new Error("Não foi possível identificar o usuário autenticado."); const user = await response.json(); currentUserRole = user.role; document.querySelector("#current-user").textContent = user.username; return true; }

async function loadPurchaseRequests() { try { const response = await fetch(apiUrl); if (!response.ok) throw new Error(await readErrorMessage(response)); const purchaseRequests = await response.json(); renderSummaryCards(purchaseRequests); renderPurchaseRequestTable(purchaseRequests); } catch (error) { document.querySelector("#request-count").textContent = error.message; } }
function renderSummaryCards(purchaseRequests) { const summaryCards = document.querySelector("#summary-cards"); summaryCards.replaceChildren(); summaryDefinitions.forEach(([status, label]) => { const card = document.createElement("article"); card.className = "summary-card"; const title = document.createElement("p"); title.textContent = label; const value = document.createElement("strong"); value.textContent = purchaseRequests.filter(purchaseRequest => purchaseRequest.status === status).length; card.append(title, value); summaryCards.append(card); }); }
function renderPurchaseRequestTable(purchaseRequests) { const tableBody = document.querySelector("#request-table-body"); const emptyState = document.querySelector("#empty-state"); tableBody.replaceChildren(); document.querySelector("#request-count").textContent = `${purchaseRequests.length} requisição(ões) encontrada(s)`; emptyState.hidden = purchaseRequests.length !== 0; purchaseRequests.forEach(purchaseRequest => { const row = document.createElement("tr"); appendCell(row, purchaseRequest.requestNumber); appendCell(row, purchaseRequest.department); appendCell(row, purchaseRequest.requesterName); appendCell(row, purchaseRequest.purchaseReason); appendBadgeCell(row, priorityLabels[purchaseRequest.priority], `priority-${purchaseRequest.priority.toLowerCase()}`); appendBadgeCell(row, statusLabels[purchaseRequest.status], `status-${purchaseRequest.status.toLowerCase().replaceAll("_", "-")}`); appendCell(row, formatDate(purchaseRequest.requestedOn)); appendCell(row, formatOpenDuration(purchaseRequest.requestedOn)); appendCell(row, purchaseRequest.itemCount); appendCell(row, formatCurrency(purchaseRequest.totalValue)); appendActionCell(row, purchaseRequest); tableBody.append(row); }); }
function appendCell(row, value) { const cell = document.createElement("td"); cell.textContent = value ?? "Não informado"; row.append(cell); }
function appendBadgeCell(row, value, className) { const cell = document.createElement("td"); const badge = document.createElement("span"); badge.className = `badge ${className}`; badge.textContent = value; cell.append(badge); row.append(cell); }
function appendActionCell(row, request) { const cell = document.createElement("td"); cell.className = "table-actions"; if (currentUserRole === "ADMIN" && request.status === "OPEN") { cell.append(actionButton("Editar", "edit", () => editPurchaseRequest(request.id)), actionButton("Cancelar", "cancel", () => cancelPurchaseRequest(request.id))); } if (currentUserRole === "ADMIN" && request.status === "CANCELLED") cell.append(actionButton("Excluir", "delete", () => deletePurchaseRequest(request.id))); row.append(cell); }
function actionButton(label, variant, action) { const button = document.createElement("button"); button.className = `table-action table-action-${variant}`; button.type = "button"; button.textContent = label; button.addEventListener("click", action); return button; }

async function createPurchaseRequest(event) {
    event.preventDefault(); formMessage.hidden = true;
    const formData = new FormData(createForm);
    const items = [...itemForms.querySelectorAll(".item-form")].map(itemForm => ({ description: itemForm.querySelector(".item-description").value, brandModel: blankToNull(itemForm.querySelector(".item-brand-model").value), quantity: Number(itemForm.querySelector(".item-quantity").value), supplierName: blankToNull(itemForm.querySelector(".item-supplier-name").value), totalValue: Number(itemForm.querySelector(".item-total-value").value), notes: blankToNull(itemForm.querySelector(".item-notes").value) }));
    const request = { requestNumber: formData.get("requestNumber"), department: formData.get("department"), requesterName: formData.get("requesterName"), requestedOn: formData.get("requestedOn"), purchaseReason: formData.get("purchaseReason"), priority: formData.get("priority"), buyerName: blankToNull(formData.get("buyerName")), managerName: blankToNull(formData.get("managerName")), costCenter: blankToNull(formData.get("costCenter")), notes: blankToNull(formData.get("notes")), items };
    const message = editingRequestId ? "Requisição atualizada." : "Requisição cadastrada.";
    try { const response = await fetch(editingRequestId ? `${apiUrl}/${editingRequestId}` : apiUrl, { method: editingRequestId ? "PUT" : "POST", headers: { "Content-Type": "application/json", [csrfToken.headerName]: csrfToken.token }, body: JSON.stringify(request) }); if (!response.ok) throw new Error(await readErrorMessage(response)); closeCreateDialog(); await loadPurchaseRequests(); showOperationFeedback(message, "success"); } catch (error) { formMessage.textContent = error.message; formMessage.hidden = false; }
}

async function editPurchaseRequest(id) { const response = await fetch(`${apiUrl}/${id}`); if (!response.ok) { window.alert(await readErrorMessage(response)); return; } const request = await response.json(); editingRequestId = id; createForm.elements.requestNumber.value = request.requestNumber; createForm.elements.department.value = request.department; createForm.elements.requesterName.value = request.requesterName; createForm.elements.requestedOn.value = request.requestedOn; createForm.elements.purchaseReason.value = request.purchaseReason; createForm.elements.priority.value = request.priority; createForm.elements.costCenter.value = request.costCenter ?? ""; createForm.elements.buyerName.value = request.buyerName ?? ""; createForm.elements.managerName.value = request.managerName ?? ""; createForm.elements.notes.value = request.notes ?? ""; itemForms.replaceChildren(); request.items.forEach(item => { addItemForm(); const form = itemForms.lastElementChild; form.querySelector(".item-description").value = item.description; form.querySelector(".item-brand-model").value = item.brandModel ?? ""; form.querySelector(".item-quantity").value = item.quantity; form.querySelector(".item-supplier-name").value = item.supplierName ?? ""; form.querySelector(".item-total-value").value = item.totalValue; form.querySelector(".item-notes").value = item.notes ?? ""; }); dialog.showModal(); }
async function cancelPurchaseRequest(id) { if (!window.confirm("Cancelar esta requisição? O histórico será mantido.")) return; await performRequest(`${apiUrl}/${id}/cancel`, "POST", "Requisição cancelada.", "warning"); }
async function deletePurchaseRequest(id) { if (!window.confirm("Excluir definitivamente esta requisição cancelada? Esta ação não pode ser desfeita.")) return; await performRequest(`${apiUrl}/${id}`, "DELETE", "Requisição excluída definitivamente.", "danger"); }
async function performRequest(url, method, message, variant) { const response = await fetch(url, { method, headers: { [csrfToken.headerName]: csrfToken.token } }); if (!response.ok) { window.alert(await readErrorMessage(response)); return; } await loadPurchaseRequests(); showOperationFeedback(message, variant); }
function showOperationFeedback(message, variant) { clearTimeout(feedbackTimeout); operationFeedback.className = `operation-feedback operation-feedback-${variant}`; operationFeedback.textContent = message; operationFeedback.hidden = false; feedbackTimeout = window.setTimeout(() => operationFeedback.hidden = true, 3200); }

async function logout() { const response = await fetch("/logout", { method: "POST", headers: { [csrfToken.headerName]: csrfToken.token } }); if (response.status === 204) window.location.assign("/login.html"); else window.alert("Não foi possível encerrar a sessão."); }
async function readErrorMessage(response) { if (response.status === 401) return "Sua sessão expirou. Entre novamente."; if (response.status === 403) return "Você não tem permissão para esta operação."; const error = await response.json().catch(() => null); return error?.errors ? Object.values(error.errors).join(" ") : error?.detail ?? "Não foi possível concluir a operação."; }
function blankToNull(value) { return value?.trim() || null; }
function formatCurrency(value) { return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(value); }
function formatDate(value) { return new Intl.DateTimeFormat("pt-BR", { timeZone: "UTC" }).format(new Date(`${value}T00:00:00Z`)); }
function formatOpenDuration(value) { const requestedOn = new Date(`${value}T00:00:00Z`); const now = new Date(); const today = Date.UTC(now.getFullYear(), now.getMonth(), now.getDate()); const days = Math.max(0, Math.floor((today - requestedOn.getTime()) / 86_400_000)); if (days === 0) return "Hoje"; return `${days} ${days === 1 ? "dia" : "dias"}`; }
function setTodayAsRequestedDate() { createForm.elements.requestedOn.value = new Date().toISOString().slice(0, 10); }

async function start() { setTodayAsRequestedDate(); addItemForm(); await loadCsrfToken(); if (await loadAuthenticatedUser()) await loadPurchaseRequests(); }
start().catch(error => { document.querySelector("#request-count").textContent = error.message; });
