(() => {
  // Evita ejecución duplicada si ya se cargó
  if (window.usuariosGridInicializado) return;
  window.usuariosGridInicializado = true;

  let gridApiUsuarios = null;

  // Inicializa la tabla de usuarios
  async function initListaUsuarios() {
    const gridDiv = document.querySelector("#usuariosGrid");
    if (!gridDiv) return;

    const columnDefs = [
      {
        headerName: "N°",
        valueGetter: "node.rowIndex + 1",
        width: 70,
        sortable: false,
        filter: false,
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
      },
      {
        headerName: "Usuario",
        field: "usuario",
        sortable: true,
        filter: true,
      },
      {
        headerName: "Contraseña",
        field: "clave",
        sortable: false,
        filter: false,
        cellRenderer: (params) => `
          <div class="flex justify-between items-center">
            <span class="password-value">********</span>
            <button class="text-aliva-blue hover:text-aliva-purple" 
              onclick="revealPassword(${params.data.idUsuario}, this)">
              <i class="fa-solid fa-eye-slash"></i>
            </button>
          </div>
        `,
      },
      {
        headerName: "Rol",
        field: "rol.descripcion",
        sortable: true,
        filter: true,
      },
      {
        headerName: "Fecha Registro",
        field: "fechaRegistro",
        sortable: true,
        filter: true,
      },
      {
        headerName: "Acciones",
        width: 150,
        filter: false,
        cellRenderer: (params) => `
          <div class="flex items-center justify-center gap-2">
            <button 
              class="text-aliva-blue hover:text-aliva-purple transition"
              onclick="abrirModalActualizar(${params.data.idUsuario})">
              <i class="fa-solid fa-pen-to-square"></i>
            </button>
            <button
              onclick="toggleEstadoUsuario(${params.data.idUsuario}, this)"
              class="relative w-11 h-6 flex items-center rounded-full transition duration-300 
                ${params.data.estado ? "bg-blue-500" : "bg-red-500"}">
              <span class="absolute left-0.5 top-0.5 w-5 h-5 bg-white rounded-full shadow-md 
                transform transition-transform duration-300
                ${params.data.estado ? "translate-x-5" : ""}"></span>
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
        resizable: true,
        filter: true,
        sortable: true,
      },
      onGridReady: () => loadUsuarios(),
    };

    gridApiUsuarios = agGrid.createGrid(gridDiv, gridOptions);
    window.gridApiUsuarios = gridApiUsuarios;

    // Searchbox global
    const searchBox = document.getElementById("searchBox");
    if (searchBox) {
      searchBox.addEventListener("input", (e) => {
        const value = e.target.value.toLowerCase();
        gridApiUsuarios.setQuickFilter(value);
      });
    }
  }

  // Cargar usuarios desde el backend
  async function loadUsuarios() {
    try {
      const res = await fetch("/api/usuarios");
      if (!res.ok) throw new Error("Error al obtener usuarios");
      const data = await res.json();
      gridApiUsuarios.setGridOption("rowData", data);
    } catch (err) {
      console.error("Error al cargar usuarios:", err);
      Swal.fire({
        icon: "error",
        title: "Error",
        text: "No se pudieron cargar los usuarios",
      });
    }
  }

  // Cargar roles para selects
  async function cargarRolesSelect(preselectedId = null, selectId) {
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

  // Generar nombre de usuario automático
  window.generarUsuarioAuto = function () {
    const nombre = document.getElementById("nombresNuevo").value.trim();
    const apellido = document.getElementById("apPaternoNuevo").value.trim();
    const usuarioInput = document.getElementById("usuarioNuevo");

    if (nombre && apellido) {
      const user = (
        nombre.charAt(0) + apellido.replace(/\s+/g, "")
      ).toUpperCase();
      usuarioInput.value = user;
    }
  };

  // Toggle estado en formulario nuevo usuario
  window.toggleEstadoNuevoUsuario = function () {
    const toggle = document.getElementById("toggleEstadoNuevo");
    const label = document.getElementById("labelEstadoNuevo");
    const circle = toggle.querySelector("span");

    const isActive = toggle.classList.contains("bg-blue-500");

    if (isActive) {
      toggle.classList.replace("bg-blue-500", "bg-red-500");
      circle.classList.remove("translate-x-6");
      label.textContent = "Inactivo";
      label.classList.replace("text-blue-500", "text-red-500");
    } else {
      toggle.classList.replace("bg-red-500", "bg-blue-500");
      circle.classList.add("translate-x-6");
      label.textContent = "Activo";
      label.classList.replace("text-red-500", "text-blue-500");
    }
  };

  // Mostrar/Ocultar contraseña en lista
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

  // Mostrar/ocultar contraseñas en formularios
  window.togglePasswordVisibility = function (button) {
    const input = button
      .closest("div")
      .querySelector("input[type='password'], input[type='text']");
    const icon = button.querySelector("i");

    if (!input) return;

    const showing = input.type === "password";
    input.type = showing ? "text" : "password";

    icon.classList.replace(
      showing ? "fa-eye" : "fa-eye-slash",
      showing ? "fa-eye-slash" : "fa-eye"
    );

    // animación ligera
    button.classList.add("scale-110");
    setTimeout(() => button.classList.remove("scale-110"), 150);
  };

  // Cambiar estado
  window.toggleEstadoUsuario = async function (idUsuario, btn) {
    const isActive = btn.classList.contains("bg-blue-500");
    const newState = !isActive;

    btn.classList.toggle("bg-blue-500", newState);
    btn.classList.toggle("bg-red-500", !newState);
    const span = btn.querySelector("span");
    span.classList.toggle("translate-x-5", newState);

    try {
      const res = await fetch(`/api/usuarios/${idUsuario}/estado`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ estado: newState }),
      });

      if (!res.ok) throw new Error("Error al actualizar estado");
    } catch (err) {
      console.error(err);

      btn.classList.toggle("bg-blue-500", !newState);
      btn.classList.toggle("bg-red-500", newState);
      span.classList.toggle("translate-x-5", !newState);
      Swal.fire({
        icon: "error",
        title: "Error",
        text: "No se pudo cambiar el estado del usuario",
      });
    }
  };

  // Abrir modal agregar usuario
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

  window.agregarUsuario = async function () {
    const nombre = document.getElementById("nombresNuevo").value.trim();
    const apPaterno = document.getElementById("apPaternoNuevo").value.trim();
    const apMaterno = document.getElementById("apMaternoNuevo").value.trim();
    const usuario = document.getElementById("usuarioNuevo").value.trim();
    const correo = document.getElementById("correoNuevo").value.trim();
    const telefono = document.getElementById("telefonoNuevo").value.trim();
    const clave = document.getElementById("claveNuevo").value.trim();
    const idRol = document.getElementById("rolNuevo").value;
    const estado =
      document
        .getElementById("toggleEstadoNuevo")
        .classList.contains("bg-blue-500") || false;

    const body = {
      nombre,
      apPaterno,
      apMaterno,
      usuario,
      correo,
      telefono,
      clave,
      estado,
      rol: { idRol: Number(idRol) },
    };

    console.log("Enviando datos de nuevo usuario:", body);

    try {
      const res = await fetch("/api/usuarios/registrar", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });

      if (!res.ok) throw new Error("Error al registrar usuario");
      const msg = await res.text();

      Swal.fire({
        icon: "success",
        title: "Usuario registrado",
        text: msg || "El usuario fue agregado correctamente.",
        showConfirmButton: false,
        timer: 1000,
      }).then(async () => {
        document.getElementById("modalAgregarUsuario").classList.add("hidden");
        loadUsuarios();
      });
    } catch (error) {
      console.error("❌ Error al registrar usuario:", error);
      Swal.fire({
        icon: "error",
        title: "Error",
        text: "No se pudo registrar el usuario. Verifique los datos e inténtelo nuevamente.",
      });
    }
  };

  // Abrir modal actualizar usuario
  window.abrirModalActualizar = async function (idUsuario) {
    try {
      const res = await fetch(`/api/usuarios/${idUsuario}/buscar`);
      if (!res.ok) throw new Error("No se pudo obtener el usuario");
      const data = await res.json();
      await cargarRolesSelect(data.rol.idRol, "rolActualizar");

      document.getElementById("idUsuarioActualizar").dataset.id =
        data.idUsuario;
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

  // Actualizar usuario
  window.actualizarUsuario = async function () {
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
        timer: 1000,
      }).then(async () => {
        document
          .getElementById("modalActualizarUsuario")
          .classList.add("hidden");
        loadUsuarios();
      });
    } catch (error) {
      console.error(error);
      Swal.fire({
        icon: "error",
        title: "Error",
        text: "No se pudo actualizar el usuario",
      });
    }
  };

  // Exponer init manualmente (por si cambias de vista sin recargar)
  window.initListaUsuarios = initListaUsuarios;
})();
