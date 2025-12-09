let selectedFile = null;
let documentText = "";
let chatHistory = [];

// Navigate
function goHistory() { window.location.href = "history.html"; }
function logout() { localStorage.clear(); window.location.href = "login.html"; }


// Drag & Drop
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
});


// Upload File → AI Analysis
function uploadFile() {

    if (!selectedFile) {
        selectedFile = document.getElementById("fileInput").files[0];
    }
    if (!selectedFile) {
        alert("Select a PDF or DOCX file");
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
        .then(res => res.json())
        .then(data => {

            document.getElementById("analysisBox").classList.remove("hidden");
            document.getElementById("chatSection").classList.remove("hidden");

            document.getElementById("summary").textContent = data.summary;
            document.getElementById("keywords").textContent = data.keywords.join(", ");
            document.getElementById("sentiment").textContent = data.sentiment;

            documentText = data.documentText; // for chat
        })
        .catch(err => alert("Error: " + err));
}



// Send chat message
function sendChat() {
    const input = document.getElementById("chatInput").value.trim();
    if (!input) return;

    // Display user message
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


// Save chat & summary
function saveChatHistory() {

    const history = JSON.parse(localStorage.getItem("history") || "[]");

    const summary = document.getElementById("summary").textContent;

    history.push({
        title: selectedFile.name,
        content: "Summary:\n" + summary + "\n\nChat:\n" +
            chatHistory.map(m => (m.role === "user" ? "You: " : "AI: ") + m.content).join("\n")
    });

    localStorage.setItem("history", JSON.stringify(history));

    alert("Saved!");
}
