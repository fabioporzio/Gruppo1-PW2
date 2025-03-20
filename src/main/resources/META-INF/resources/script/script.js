// Effetto navbar che scompare quando si scrolla in basso e riappare quando si scrolla in su
let lastScrollY = window.pageYOffset; // Memorizza la posizione dello scroll corrente

window.addEventListener("scroll", () => {
    const currentScrollY = window.pageYOffset; // Posizione corrente dello scroll
    const navbar = document.getElementById("navbar"); // Seleziona la navbar

    if (lastScrollY > currentScrollY) { 
        // Se lo scroll va verso l'alto, mostra la navbar
        navbar.style.top = "0px";
    } else {
        // Se lo scroll va verso il basso, nasconde la navbar
        navbar.style.top = "-200px";

        // Chiude il menu a tendina quando si scrolla verso il basso
        const navLinks = document.querySelector('.nav-links');
        navLinks.classList.remove('active');
    }

    lastScrollY = currentScrollY; // Aggiorna la posizione dello scroll
});

// Gestisce il caricamento del DOM e aggiunge interattività al menu hamburger
document.addEventListener('DOMContentLoaded', function () {
    const hamburgerMenu = document.getElementById('hamburger-menu'); // Seleziona il bottone hamburger
    const navLinks = document.querySelector('.nav-links'); // Seleziona i link del menu

    hamburgerMenu.addEventListener('click', function () {
        // Mostra o nasconde i link del menu al click dell'hamburger
        navLinks.classList.toggle('active');
    });
});

// Funzione per mostrare un modal di conferma prima di eliminare un elemento
function confirmDelete(event, form) {
    event.preventDefault(); // Previene l'invio del form

    // Crea un overlay semi-trasparente
    let overlay = document.createElement("div");
    overlay.style.position = "fixed";
    overlay.style.top = "0";
    overlay.style.left = "0";
    overlay.style.width = "100vw";
    overlay.style.height = "100vh";
    overlay.style.backgroundColor = "rgba(0, 0, 0, 0.5)";
    overlay.style.display = "flex";
    overlay.style.justifyContent = "center";
    overlay.style.alignItems = "center";
    overlay.style.zIndex = "1000";

    // Crea il contenitore del modal
    let modal = document.createElement("div");
    modal.style.backgroundColor = "white";
    modal.style.padding = "70px";
    modal.style.borderRadius = "10px";
    modal.style.fontSize = "30px";
    modal.style.textAlign = "center";
    modal.innerHTML = "<p>Sei sicuro di voler eliminare questa visita?</p>";

    // Bottone di conferma (Sì, elimina)
    let btnYes = document.createElement("button-confirm");
    btnYes.style.cursor = "pointer";
    btnYes.textContent = "Sì, elimina";
    btnYes.style.backgroundColor = "#ff4d4d"; // Rosso chiaro
    btnYes.style.padding = "12px 20px";
    btnYes.style.fontSize = "16px";
    btnYes.style.fontWeight = "bold";
    btnYes.style.borderRadius = "8px";
    btnYes.style.border = "2px solid #ff0000";
    btnYes.style.boxShadow = "0px 4px 8px rgba(0, 0, 0, 0.2)";
    btnYes.style.marginRight = "20px";
    btnYes.onmouseover = function () {
        btnYes.style.backgroundColor = "#cc0000"; // Cambia colore al passaggio del mouse
    };
    btnYes.onmouseout = function () {
        btnYes.style.backgroundColor = "#ff4d4d";
    };
    btnYes.onclick = function () {
        form.submit(); // Invio del form
    };

    // Bottone per annullare l'operazione
    let btnNo = document.createElement("button-confirm");
    btnNo.style.cursor = "pointer";
    btnNo.textContent = "Annulla";
    btnNo.style.backgroundColor = "#555"; // Grigio scuro
    btnNo.style.color = "white";
    btnNo.style.padding = "12px 20px";
    btnNo.style.fontSize = "16px";
    btnNo.style.borderRadius = "8px";
    btnNo.style.border = "2px solid #333";
    btnNo.style.boxShadow = "0px 4px 8px rgba(0, 0, 0, 0.2)";
    btnNo.style.marginLeft = "20px";
    btnNo.onmouseover = function () {
        btnNo.style.backgroundColor = "#777"; // Cambia colore al passaggio del mouse
    };
    btnNo.onmouseout = function () {
        btnNo.style.backgroundColor = "#555";
    };
    btnNo.onclick = function () {
        document.body.removeChild(overlay); // Rimuove il modal
    };

    // Aggiunge i bottoni al modal
    modal.appendChild(btnYes);
    modal.appendChild(btnNo);
    // Aggiunge il modal all'overlay
    overlay.appendChild(modal);
    // Aggiunge l'overlay al documento
    document.body.appendChild(overlay);

    return false; // Previene altre azioni predefinite
}

// Funzione per svuotare l'input della data
function emptyInputDate() {
    document.getElementById("date").value = ""; // Imposta il valore a vuoto
}
