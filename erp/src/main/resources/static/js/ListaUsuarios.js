document.addEventListener("DOMContentLoaded", () => {
  const togglePassword = document.getElementById("togglePasswordActualizar");
  const input = document.getElementById("claveUsuario");

  togglePassword.addEventListener("click", () => {
    const visible = input.type === "text";
    input.type = visible ? "password" : "text";
    togglePassword.innerHTML = visible
      ? '<i class="fa-solid fa-eye"></i>'
      : '<i class="fa-solid fa-eye-slash"></i>';
  });
});

let gridApi = null;

function initListaUsuarios() {
  const columnDefs = [
    {
      headerName: "N°",
      valueGetter: "node.rowIndex + 1",
      width: 80,
      sortable: false,
      filter: false,
      minWidth: 50,
      maxWidth: 50,
      suppressSizeToFit: true,
    },
    {
      headerName: "Nombre Completo",
      valueGetter: (params) => {
        const n = params.data.nombres || params.data.nombre || "";
        const p = params.data.apellidoPaterno || params.data.apPaterno || "";
        const m = params.data.apellidoMaterno || params.data.apMaterno || "";
        return `${n} ${p} ${m}`.trim();
      },
      sortable: true,
      filter: true,
      maxWidth: 250,
    },

    {
      headerName: "Usuario",
      field: "usuario",
      sortable: true,
      filter: true,
      maxWidth: 250,
    },

    {
      headerName: "Contraseña",
      field: "clave",
      sortable: false,
      filter: false,
      maxWidth: 250,
      cellRenderer: (params) => {
        return `
      <div class="flex justify-between">
        <span class="password-value">********</span>
        <button class="text-aliva-blue hover:text-aliva-purple transition-colors" onclick="revealPassword(${params.data.idUsuario}, this)">
          <i class="fa-solid fa-eye-slash"></i>
        </button>
      </div>
    `;
      },
    },

    {
      headerName: "Rol",
      field: "rol.descripcion",
      sortable: true,
      filter: true,
      minWidth: 120,
      maxWidth: 120,
    },
    {
      headerName: "Fecha Registro",
      field: "fechaRegistro",
      sortable: true,
      filter: true,
      minWidth: 120,
      maxWidth: 120,
    },
    {
      headerName: "Acciones",
      width: 150,
      filter: false,
      minWidth: 120,
      maxWidth: 120,
      cellRenderer: (params) => `
    <div class="flex items-center justify-center gap-2">
      <button 
        class="text-aliva-blue hover:text-aliva-purple transition-colors"
        onclick="abrirModalActualizar(${params.data.idUsuario})"
      >
        <i class="fa-solid fa-pen-to-square"></i>
      </button>

      <button
      onclick="toggleEstado(${params.data.idUsuario}, this)"
      class="relative w-11 h-6 flex items-center rounded-full transition-colors duration-300 
        ${params.data.estado ? "bg-blue-500" : "bg-red-500"}"
    >
      <span class="absolute left-0.5 top-0.5 w-5 h-5 bg-white rounded-full shadow-md 
        transform transition-transform duration-300
        ${params.data.estado ? "translate-x-5" : ""}">
      </span>
    </button>
    </div>
  `,
    },
  ];

  const gridOptions = {
    columnDefs,
    rowData: [],
    pagination: true,
    paginationPageSize: 10,
    paginationPageSizeSelector: [10, 20, 50],
    defaultColDef: {
      flex: 1,
      filter: true,
    },
  };

  const eGridDiv = document.querySelector("#usuariosGrid");
  if (!gridApi) {
    gridApi = agGrid.createGrid(eGridDiv, gridOptions);
  }

  fetch("/api/usuarios")
    .then((r) => r.json())
    .then((data) => {
      gridApi.setGridOption("rowData", data);
    })
    .catch((err) => console.error("Error al cargar usuarios:", err));
}

document.getElementById("searchBox").addEventListener("input", function () {
  const value = this.value.toLowerCase();
  if (gridApi) {
    gridApi.setGridOption("quickFilterText", value);
  }
});

window.revealPassword = async function (idUsuario, btnEl) {
  const btn = btnEl.closest("button");
  const span = btn?.previousElementSibling;

  const icon = btn.querySelector("i");
  btn.disabled = true;

  try {
    const res = await fetch("/api/usuarios/" + idUsuario + "/clave");
    if (!res.ok) throw new Error("Error al obtener clave");
    const data = await res.json();

    const clave = data.clave || "No disponible";

    span.textContent = clave;
    icon.classList.remove("fa-spinner", "fa-spin");
    icon.classList.replace("fa-eye-slash", "fa-eye");

    setTimeout(() => {
      span.textContent = "********";
      icon.classList.replace("fa-eye", "fa-eye-slash");
    }, 3000);
  } catch (e) {
    console.error(e);
    span.textContent = "Error";
    icon.classList.replace("fa-eye", "fa-eye-slash");
  } finally {
    btn.disabled = false;
  }
};

window.toggleEstado = async function (idUsuario, btn) {
  const isActive = btn.classList.contains("bg-blue-500");
  const newState = !isActive;

  if (newState) {
    btn.classList.remove("bg-red-500");
    btn.classList.add("bg-blue-500");
    btn.querySelector("span").classList.add("translate-x-5");
  } else {
    btn.classList.remove("bg-blue-500");
    btn.classList.add("bg-red-500");
    btn.querySelector("span").classList.remove("translate-x-5");
  }

  try {
    const res = await fetch(`/api/usuarios/${idUsuario}/estado`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ estado: newState }),
    });

    if (!res.ok) throw new Error("Error al actualizar estado");

    const data = await res.json();
    console.log("Estado actualizado:", data);
  } catch (err) {
    console.error(err);
    // Revertir si falla
    if (newState) {
      btn.classList.replace("bg-blue-500", "bg-red-500");
      btn.querySelector("span").classList.remove("translate-x-5");
    } else {
      btn.classList.replace("bg-red-500", "bg-blue-500");
      btn.querySelector("span").classList.add("translate-x-5");
    }
  }
};

document.addEventListener("click", function (e) {
  if (e.target.closest("#togglePasswordActualizar")) {
    const input = document.getElementById("claveActualizar");
    const icon = e.target
      .closest("#togglePasswordActualizar")
      .querySelector("i");

    if (input.type === "password") {
      input.type = "text";
      icon.classList.replace("fa-eye-slash", "fa-eye");
    } else {
      input.type = "password";
      icon.classList.replace("fa-eye", "fa-eye-slash");
    }
  }
});

async function cargarRolesSelect(preselectedId = null, selectId = "rolNuevo") {
  const select = document.getElementById(selectId);
  select.innerHTML = `<option value="">Cargando...</option>`;
  try {
    const res = await fetch("/api/roles");
    const roles = await res.json();
    select.innerHTML = `<option value="">Seleccione un rol...</option>`;

    roles.forEach((r) => {
      const opt = document.createElement("option");
      opt.value = r.idRol;
      opt.textContent = r.descripcion;
      if (preselectedId && preselectedId === r.idRol) {
        opt.selected = true;
      }
      select.appendChild(opt);
    });
  } catch (e) {
    console.error("Error al cargar roles:", e);
    select.innerHTML = `<option>Error al cargar roles</option>`;
  }
}

window.abrirModalAgregar = async function () {
  try {
    await cargarRolesSelect(null, "rolNuevo");

    document.getElementById("nombresNuevo").value = "";
    document.getElementById("apPaternoNuevo").value = "";
    document.getElementById("apMaternoNuevo").value = "";
    document.getElementById("correoNuevo").value = "";
    document.getElementById("telefonoNuevo").value = "";
    document.getElementById("claveNuevo").value = "";
    document.getElementById("rolNuevo").value = "";

    // Mostrar
    const modal = document.getElementById("modalAgregarUsuario");
    modal.classList.remove("hidden");
    document.body.classList.add("overflow-hidden");

    // Cerrar
    modal.querySelectorAll("[data-modal-close]").forEach((btn) => {
      btn.addEventListener("click", () => {
        modal.classList.add("hidden");
        document.body.classList.remove("overflow-hidden");
      });
    });
  } catch (e) {
    console.error("Error al abrir modal agregar:", e);
    Swal.fire({
      icon: "error",
      title: "Error",
      text: "No se pudo preparar el formulario de registro.",
    });
  }
};

window.abrirModalActualizar = async function (idUsuario) {
  try {
    const res = await fetch(`/api/usuarios/${idUsuario}/buscar`);
    if (!res.ok) throw new Error("No se pudo obtener el usuario");
    const data = await res.json();
    await cargarRolesSelect(data.rol.idRol, "rolActualizar");

    document.getElementById("idUsuarioActualizar").dataset.id = data.idUsuario;
    document.getElementById("nombresActualizar").value = data.nombre || "";
    document.getElementById("apellidoPaternoActualizar").value =
      data.apPaterno || "";
    document.getElementById("apellidoMaternoActualizar").value =
      data.apMaterno || "";
    document.getElementById("correoActualizar").value = data.correo || "";
    document.getElementById("telefonoActualizar").value = data.telefono || "";
    document.getElementById("usuarioActualizar").value = data.usuario || "";
    document.getElementById("claveActualizar").value = data.clave || "";
    document.getElementById("rolActualizar").value = data.rol?.idRol ?? "";
    document.getElementById("fechaRegistroActualizar").value =
      data.fechaRegistro || "";

    // Muestra
    const modal = document.getElementById("modalActualizarUsuario");
    modal.classList.remove("hidden");
    document.body.classList.add("overflow-hidden");

    // Cerrar
    modal.querySelectorAll("[data-modal-close]").forEach((btn) => {
      btn.addEventListener("click", () => {
        modal.classList.add("hidden");
        document.body.classList.remove("overflow-hidden");
      });
    });
  } catch (e) {
    console.error("Error al abrir modal:", e);
    Swal.fire({
      icon: "error",
      title: "Error",
      text: "No se pudo cargar el usuario seleccionado.",
    });
  }
};

async function actualizarUsuario() {
  const id = document.getElementById("idUsuarioActualizar").dataset.id;
  const nombre = document.getElementById("nombresActualizar").value.trim();
  const apPaterno = document
    .getElementById("apellidoPaternoActualizar")
    .value.trim();
  const apMaterno = document
    .getElementById("apellidoMaternoActualizar")
    .value.trim();
  const correo = document.getElementById("correoActualizar").value.trim();
  const telefono = document.getElementById("telefonoActualizar").value.trim();
  const clave = document.getElementById("claveActualizar").value.trim();
  const idRol = document.getElementById("rolActualizar").value;

  const body = {
    idUsuario: Number(id),
    nombre,
    apPaterno,
    apMaterno,
    correo,
    telefono,
    clave,
    rol: { idRol: Number(idRol) },
  };

  try {
    const res = await fetch(`/api/usuarios/${id}/actualizar`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });

    if (!res.ok) throw new Error("Error al actualizar usuario");
    const msg = await res.text();

    Swal.fire({
      icon: "success",
      title: "Actualizado",
      text: msg,
      showConfirmButton: false,
      timer: 1500,
    }).then(async () => {
      document.getElementById("modalActualizarUsuario").classList.add("hidden");
      await initListaUsuarios();
    });
  } catch (error) {
    console.error(error);
    Swal.fire({
      icon: "error",
      title: "Error",
      text: "No se pudo actualizar el usuario",
    });
  }
}
