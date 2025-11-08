(async () => {
  try {
    const container = document.getElementById("sidebar-container");
    if (!container) {
      console.warn("⚠️ No se encontró el contenedor #sidebar-container");
      return;
    }

    const res = await fetch("/components/navar.html");
    if (!res.ok) throw new Error("Error al cargar el navbar.");

    container.innerHTML = await res.text();
    initNavar();
  } catch (err) {
    console.error("❌ Error cargando navbar:", err);
  }
})();

function initNavar() {
  console.log("✅ Navbar inicializado");

  // Toggle de submenús
  document.querySelectorAll("[data-submenu]").forEach((btn) => {
    btn.addEventListener("click", () => {
      const id = btn.dataset.submenu;
      toggleSubmenu(id);
    });
  });

  // Cerrar sesión (si existe el botón en el HTML)
  const logoutBtn = document.querySelector("[data-logout]");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", (e) => {
      e.preventDefault();
      sessionStorage.removeItem("usuario");
      window.location.href = "/login";
    });
  }
}

function toggleSubmenu(id) {
  const submenu = document.getElementById(`${id}-submenu`);
  const arrow = document.getElementById(`${id}-arrow`);
  const container = document.getElementById(`${id}-container`);

  document
    .querySelectorAll("[id$='-submenu']")
    .forEach((el) => el.classList.add("hidden"));
  document
    .querySelectorAll("[id$='-arrow']")
    .forEach((el) => el.classList.remove("rotate-180"));
  document
    .querySelectorAll("[id$='-container']")
    .forEach((el) => el.classList.remove("bg-white/20"));

  if (submenu) submenu.classList.toggle("hidden");
  if (arrow) arrow.classList.toggle("rotate-180");
  if (container) container.classList.toggle("bg-white/20");
}
