(async () => {
  try {
    const container = document.getElementById("header-container");
    if (!container) {
      console.warn("⚠️ No se encontró el contenedor #header-container");
      return;
    }

    const res = await fetch("/components/header.html");
    if (!res.ok) throw new Error("Error al cargar el header.");

    container.innerHTML = await res.text();
    initHeader();
  } catch (err) {
    console.error("❌ Error cargando header:", err);
  }
})();

function initHeader() {
  console.log("✅ Header inicializado");

  const usuario = JSON.parse(sessionStorage.getItem("usuario"));
  if (usuario) {
    const nombreCompleto = `${usuario.nombre ?? ""} ${
      usuario.apPaterno ?? ""
    } ${usuario.apMaterno ?? ""}`.trim();
    const rol = usuario.rol?.descripcion ?? "Sin rol asignado";

    const userNombre = document.getElementById("userNombre");
    const userRol = document.getElementById("userRol");

    if (userNombre) userNombre.textContent = nombreCompleto;
    if (userRol) userRol.textContent = rol;
  }

  // Dropdown del usuario
  const dropdownBtn = document.querySelector("[data-dropdown-btn]");
  const dropdownMenu = document.getElementById("userDropdown");

  if (dropdownBtn && dropdownMenu) {
    dropdownBtn.addEventListener("click", () =>
      dropdownMenu.classList.toggle("hidden")
    );

    document.addEventListener("click", (e) => {
      if (!dropdownMenu.contains(e.target) && !dropdownBtn.contains(e.target)) {
        dropdownMenu.classList.add("hidden");
      }
    });
  }

  // Cerrar sesión (si existe)
  const logoutLink = document.querySelector("[data-logout]");
  if (logoutLink) {
    logoutLink.addEventListener("click", (e) => {
      e.preventDefault();
      sessionStorage.removeItem("usuario");
      window.location.href = "/login";
    });
  }
}
