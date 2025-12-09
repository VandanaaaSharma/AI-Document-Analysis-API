let selectedFile = null;

// DARK MODE TOGGLE
document.getElementById("darkToggle").onclick = () => {
    document.body.classList.toggle("dark");
};

// DRAG & DROP LOGIC
const dropArea = document.getElementById("dropArea");

dropArea.addEventListener("dragover", (e) => {
    e.preventDefault();
    dropArea.classList.add("dragover");
});

dropArea.addEventListener("dragleave", () => {
    dropArea.classList.remove("dragover");
});

dropArea.addEventListener("drop", (e) => {
    e.preventDefault();
    dropArea.classList.remove("dragover");

    selectedFile = e.dataTransfer.files[0];
});

// UPLOAD + PROGRESS + API REQUEST
function uploadFile() {
    if (!selectedFile) {
        selectedFile = document.getElementById("fileInput").files[0];
    }
    if (!selectedFile) {
        alert("Please select a file");
        return;
    }

    const formData = new FormData();
    formData.append("file", selectedFile);

    // Show progress bar
    document.getElementById("progressContainer").style.display = "block";

    let progress = 0;
    const progressBar = document.getElementById("progressBar");
    const loading = document.getElementById("loading");

    // Simulate progress animation
    const interval = setInterval(() => {
        if (progress < 90) {
            progress += 10;
            progressBar.style.width = progress + "%";
        }
    }, 200);

    loading.style.display = "block";

    fetch("/api/v1/document/analyze", {
    method: "POST",
    body: formData
})
    .then(async res => {
        const text = await res.text(); // Read as text ALWAYS

        if (!res.ok) {
            throw new Error(text);
        }

        // Try to convert to JSON
        const data = JSON.parse(text);

        return data;
    })
    .then(data => {
        clearInterval(interval);
        progressBar.style.width = "100%";
        loading.style.display = "none";

        document.getElementById("resultBox").classList.remove("hidden");

        document.getElementById("summary").textContent = data.summary;
        document.getElementById("keywords").textContent = data.keywords.join(", ");
        document.getElementById("sentiment").textContent = data.sentiment;
    })
        .catch(err => {
            clearInterval(interval);
            loading.style.display = "none";
            alert("Error analyzing file:\n" + err.message);
            console.error(err);
        });
    }
