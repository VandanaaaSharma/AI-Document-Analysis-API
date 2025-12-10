function login() {
    const user = document.getElementById("username").value;
    const pass = document.getElementById("password").value;

    if (user === "admin" && pass === "admin123") {
        window.location.href = "index.html";
    } else {
        document.getElementById("loginError").textContent = "Invalid login!";
    }
}

