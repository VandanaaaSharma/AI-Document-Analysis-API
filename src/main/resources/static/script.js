let selectedFile = null;
let documentText = "";
let chatHistory = [];

// NAVIGATION
function goHistory() { window.location.href = "history.html"; }
function logout() { localStorage.removeItem("currentUser"); window.location.href = "login.html"; }

// Enable Analyze button when file selected
function enableAnalyzeButton() {
    document.getElementById("analyzeBtn").disabled = false;
}

// Show file name
function showFileName() {
    const fileInput = document.getElementById("fileInput");
    const label = document.getElementById("fileNameLabel");

    if (fileInput.files.length > 0) {
        selectedFile = fileInput.files[0];
        label.textContent = "📄 Selected: " + selectedFile.name;
        enableAnalyzeButton();
    }
}

// DRAG & DROP
const dropArea = document.getElementById("dropArea");

dropArea.addEventListener("dragover", e => {
    e.preventDefault();
    dropArea.classList.add("dragover");
});

dropArea.addEventListener("dragleave", () => dropArea.classList.remove("dragover"));

dropArea.addEventListener("drop", e => {
    e.preventDefault();
    dropArea.classList.remove("dragover");

    selectedFile = e.dataTransfer.files[0];
    document.getElementById("fileNameLabel").textContent = "📄 Selected: " + selectedFile.name;

    enableAnalyzeButton();
});

// ANALYZE DOCUMENT
function uploadFile() {

    if (!selectedFile) {
        selectedFile = document.getElementById("fileInput").files[0];
    }
    if (!selectedFile) {
        alert("Please select a PDF or DOCX file");
        return;
    }

    const formData = new FormData();
    formData.append("file", selectedFile);

    document.getElementById("progressContainer").style.display = "block";
    document.getElementById("loading").style.display = "block";

    fetch("/api/v1/document/analyze", {
        method: "POST",
        body: formData
    })
        .then(async res => {
            if (!res.ok) throw new Error(await res.text());
            return res.json();
        })
        .then(data => {
            document.getElementById("analysisBox").classList.remove("hidden");
            document.getElementById("chatSection").classList.remove("hidden");

            document.getElementById("summary").textContent = data.summary;
            document.getElementById("keywords").textContent = data.keywords.join(", ");
            document.getElementById("sentiment").textContent = data.sentiment;

            documentText = data.summary;
        })
        .catch(err => {
            alert("⚠️ " + err.message);
            console.error(err);
        });
}

// CHAT
function sendChat() {
    const input = document.getElementById("chatInput").value.trim();
    if (!input) return;

    const box = document.getElementById("chatBox");

    box.innerHTML += `<div class="user-msg">${input}</div>`;
    chatHistory.push({ role: "user", content: input });

    document.getElementById("chatInput").value = "";

    fetch("/api/v1/document/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            documentText,
            question: input,
            history: chatHistory
        })
    })
        .then(res => res.text())
        .then(answer => {
            box.innerHTML += `<div class="ai-msg">${answer}</div>`;
            chatHistory.push({ role: "assistant", content: answer });
            box.scrollTop = box.scrollHeight;
        });
}

// SAVE SUMMARY
function saveChatHistory() {

    const user = localStorage.getItem("currentUser");
    const key = "history_" + user;

    const history = JSON.parse(localStorage.getItem(key) || "[]");

    history.push({
        fileName: selectedFile ? selectedFile.name : "Unknown File",
        summary: document.getElementById("summary").textContent,
        timestamp: new Date().toLocaleString()
    });

    localStorage.setItem(key, JSON.stringify(history));
    alert("Saved to your history!");
}
