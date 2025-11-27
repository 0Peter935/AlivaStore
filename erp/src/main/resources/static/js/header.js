// ==========================
// Cargar header dinámico
// ==========================
(async () => {
  try {
    const container = document.getElementById("header-container");
    if (!container) {
      console.warn("⚠️ No existe #header-container en la página");
      return;
    }

    const res = await fetch("/components/header.html");
    if (!res.ok) throw new Error("Error al cargar /components/header.html");

    container.innerHTML = await res.text();

    // Cuando el header ya está en el DOM, inicializamos JS relacionado
    initHeader();
    initModalesHeader();

    console.log("✅ Header cargado correctamente");
  } catch (err) {
    console.error("❌ Error cargando header:", err);
  }
})();

function cargarDatosPerfil() {
  const usuario = JSON.parse(sessionStorage.getItem("usuario"));
  if (!usuario) return;

  // Inputs del modal
  document.getElementById("nombresInput").value = usuario.nombre ?? "";
  document.getElementById("apPaternoInput").value = usuario.apPaterno ?? "";
  document.getElementById("apMaternoInput").value = usuario.apMaterno ?? "";
  document.getElementById("correoInput").value = usuario.correo ?? "";
  document.getElementById("telefonoInput").value = usuario.telefono ?? "";

  document.getElementById("usuarioInput").value = usuario.usuario ?? "";
  document.getElementById("passwordInput").value = usuario.clave ?? "";
  document.getElementById("rolInput").value = usuario.rol?.descripcion ?? "";
  document.getElementById("fechaRegistroInput").value =
    usuario.fechaRegistro ?? "";
}

function initHeader() {
  console.log("🔧 Inicializando header...");

  const usuario = JSON.parse(sessionStorage.getItem("usuario"));
  if (usuario) {
    const nombreCompleto = `${usuario.nombre ?? ""} ${
      usuario.apPaterno ?? ""
    } ${usuario.apMaterno ?? ""}`.trim();
    const rol = usuario.rol?.descripcion ?? "Sin rol";

    document.getElementById("userNombre").textContent = nombreCompleto;
    document.getElementById("userRol").textContent = rol;
  }

  // === Dropdown ===
  const dropdownBtn = document.querySelector("[data-dropdown-btn]");
  const dropdownMenu = document.getElementById("userDropdown");

  if (dropdownBtn && dropdownMenu) {
    dropdownBtn.addEventListener("click", (e) => {
      dropdownMenu.classList.toggle("hidden");
    });

    document.addEventListener("click", (e) => {
      if (!dropdownMenu.contains(e.target) && !dropdownBtn.contains(e.target)) {
        dropdownMenu.classList.add("hidden");
      }
    });
  }

  // === Logout ===
  const logoutLink = document.querySelector("[data-logout]");
  if (logoutLink) {
    logoutLink.addEventListener("click", (e) => {
      e.preventDefault();
      sessionStorage.removeItem("usuario");
      window.location.href = "/login";
    });
  }

  // === Toggle password ===
  const btnTogglePassword = document.getElementById("togglePasswordPerfil");
  if (btnTogglePassword) {
    btnTogglePassword.addEventListener("click", togglePasswordPerfil);
  }
}

function initModalesHeader() {
  console.log("🔧 Inicializando modales del header...");

  // Abrir modales
  document.querySelectorAll("[data-modal-open]").forEach((btn) => {
    btn.addEventListener("click", () => {
      const id = btn.dataset.modalOpen;
      const modal = document.getElementById(id);

      // si es el modal de perfil → cargar datos del usuario
      if (id === "perfilModal") {
        cargarDatosPerfil();
      }

      modal?.classList.remove("hidden");
    });
  });

  // Cerrar modales
  document.querySelectorAll("[data-modal-close]").forEach((btn) => {
    btn.addEventListener("click", () => {
      const id = btn.dataset.modalClose;
      const modal = document.getElementById(id);
      modal?.classList.add("hidden");
    });
  });
}

function togglePasswordPerfil() {
  const input = document.getElementById("passwordInput");
  const icon = document.querySelector("#togglePasswordPerfil i");

  if (!input || !icon) return;

  if (input.type === "password") {
    input.type = "text";
    icon.classList.remove("fa-eye");
    icon.classList.add("fa-eye-slash");
  } else {
    input.type = "password";
    icon.classList.remove("fa-eye-slash");
    icon.classList.add("fa-eye");
  }
}

// === FORMULARIO DE PERFIL ===
const profileForm = document.getElementById("profileForm");

if (profileForm) {
  profileForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const usuario = JSON.parse(sessionStorage.getItem("usuario"));
    if (!usuario) {
      return Swal.fire("Error", "No hay sesión activa", "error");
    }

    const data = {
      idUsuario: usuario.idUsuario,
      nombre: document.getElementById("nombresInput").value.trim(),
      apPaterno: document.getElementById("apPaternoInput").value.trim(),
      apMaterno: document.getElementById("apMaternoInput").value.trim(),
      correo: document.getElementById("correoInput").value.trim(),
      telefono: document.getElementById("telefonoInput").value.trim(),
    };

    try {
      const resp = await fetch("/api/usuarios/perfil/actualizar", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });

      if (!resp.ok) throw new Error("Error guardando perfil");

      // 🔥 Actualizar sessionStorage
      const usuarioActualizado = {
        ...usuario,
        ...data,
      };

      sessionStorage.setItem("usuario", JSON.stringify(usuarioActualizado));

      Swal.fire({
        icon: "success",
        title: "Perfil actualizado",
        timer: 1500,
        showConfirmButton: false,
      });

      cerrarModal("perfilModal");
      actualizarHeader(usuarioActualizado);
    } catch (err) {
      console.error(err);
      Swal.fire("Error", "No se pudo actualizar el perfil", "error");
    }
  });
}

function cerrarModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.add("hidden");
}

function actualizarHeader(usuario) {
  const userNombre = document.getElementById("userNombre");
  const userRol = document.getElementById("userRol");

  if (userNombre)
    userNombre.textContent = `${usuario.nombre} ${usuario.apPaterno ?? ""}`;
  if (userRol) userRol.textContent = usuario.rol?.descripcion ?? "Sin rol";
}
