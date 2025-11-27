document.addEventListener("DOMContentLoaded", () => {
  console.log("🧩 Iniciando ListaPedidos con ACL...");

  let gridApiPedidos = null;

  // =====================================================
  // 🔐  PERMISOS / ACL
  // =====================================================
  const usuarioActual = JSON.parse(sessionStorage.getItem("usuario"));
  console.log("👤 Usuario logueado:", usuarioActual);

  if (!usuarioActual) {
    Swal.fire("Error", "No se encontró la sesión del usuario", "error");
    return;
  }

  const idUsuario = usuarioActual.idUsuario;
  const idRol = usuarioActual.rol?.idRol ?? 0;

  // Resolver endpoint según rol
  function getEndpointPedidos() {
    switch (idRol) {
      case 2: // Vendedor
        return `/api/pedidos/vendedor/${idUsuario}`;
      case 3: // Logistica
        return "/api/pedidos/logistica";
      default:
        return "/api/pedidos";
    }
  }

  // =====================================================
  // 🔧 Opciones adicionales según rol (herramientas)
  // =====================================================
  function PermisoVerPedido() {
    return idRol === 3;
  }

  function PermisoEditarPedido() {
    return idRol === 1 || idRol === 2; // Todos pueden ver detalle
  }

  // =====================================================
  // 🚀 INIT
  // =====================================================
  initListaPedidos();

  async function initListaPedidos() {
    const gridDiv = document.querySelector("#pedidosGrid");
    if (!gridDiv) return;

    const columnDefs = [
      {
        headerName: "N°",
        valueGetter: "node.rowIndex + 1",
        width: 70,
        sortable: false,
      },
      {
        headerName: "Documento",
        field: "documento",
        width: 140,
        sortable: true,
      },
      {
        headerName: "Cliente",
        field: "cliente.nombres",
        flex: 1,
        cellRenderer: (params) => {
          const c = params.data?.cliente;
          if (!c) return "-";
          return `
            <div>
                <p class="font-semibold text-gray-800">${c.nombres} ${
            c.apellidoPaterno ?? ""
          }</p>
                <p class="text-sm text-gray-500">${c.dni ?? ""}</p>
            </div>`;
        },
      },
      {
        headerName: "Usuario",
        field: "usuario.usuario",
        width: 180,
        cellRenderer: (params) =>
          `<span class="text-gray-700">${params.value ?? "-"}</span>`,
      },
      {
        headerName: "Monto Total (S/)",
        field: "montoTotal",
        width: 160,
        cellRenderer: (params) =>
          `<span class="font-semibold text-blue-700">S/. ${(
            params.value ?? 0
          ).toFixed(2)}</span>`,
      },
      {
        headerName: "Estado",
        field: "estadoPedido.descripcion",
        width: 160,
        cellRenderer: (params) => {
          const estado = (params.value ?? "").toUpperCase();
          const color =
            estado === "PENDIENTE"
              ? "bg-yellow-100 text-yellow-700"
              : estado === "RECHAZADO"
              ? "bg-red-100 text-red-700"
              : estado === "APROBADO"
              ? "bg-blue-100 text-blue-700"
              : estado === "ENVIADO"
              ? "bg-indigo-100 text-indigo-700"
              : estado === "COMPLETADO"
              ? "bg-green-100 text-green-700"
              : "bg-gray-200 text-gray-700";

          return `<span class="px-3 py-1 rounded-full text-xs font-semibold ${color}">${estado}</span>`;
        },
      },
      {
        headerName: "Detalles",
        field: "detalles",
        flex: 2,
        cellRenderer: (params) => {
          const detalles = params.value ?? [];
          const totalItems = detalles.length;

          // 🌟 Contenedor principal (texto visible en celda)
          const wrapper = document.createElement("div");
          wrapper.className =
            "cursor-pointer font-semibold text-gray-800 select-none w-full h-full flex items-center";

          wrapper.textContent = `${totalItems} ítem${
            totalItems !== 1 ? "s" : ""
          }`;

          // 🌟 Popover global
          const popover = document.createElement("div");
          popover.className = `
      absolute bg-white border shadow-xl rounded-lg p-3 w-64 z-[9999] hidden
    `;
          popover.innerHTML = `
      <p class="text-sm font-semibold text-indigo-600 mb-2">Productos</p>
      ${
        detalles.length > 0
          ? detalles
              .map(
                (d) => `
          <div class="flex justify-between text-sm py-0.5">
            <span>${d.nombreProducto}</span>
            <span class="font-semibold">x${d.cantidad}</span>
          </div>`
              )
              .join("")
          : `<p class="text-gray-400 italic text-sm">Sin detalles</p>`
      }
    `;
          document.body.appendChild(popover);

          // ⭐ Este es el truco:
          //   Detectamos la celda padre de AG Grid en lugar del wrapper
          let cellElement = null;

          setTimeout(() => {
            cellElement = wrapper.closest(".ag-cell");
            if (cellElement) {
              cellElement.classList.add("relative");
            }
          }, 0);

          // ========== EVENTOS SOBRE TODA LA CELDA ==========
          function showPopover(e) {
            const rect = cellElement.getBoundingClientRect();
            popover.style.left = `${rect.left + rect.width / 2 - 120}px`;
            popover.style.top = `${rect.bottom + 5}px`;
            popover.classList.remove("hidden");
          }

          function hidePopover() {
            popover.classList.add("hidden");
          }

          // Agregar eventos a la celda
          setTimeout(() => {
            if (cellElement) {
              cellElement.addEventListener("mouseenter", showPopover);
              cellElement.addEventListener("mouseleave", hidePopover);
            }
          }, 5);

          return wrapper;
        },
      },
      {
        headerName: "Acciones",
        width: 150,
        filter: false,
        cellRenderer: (params) => {
          const pedido = params.data;

          let html = `<div class="flex justify-center gap-2">`;

          if (PermisoEditarPedido()) {
            html += `
            <button
              onclick="editarDetallePedido('${pedido.codPedido}')"
              class="text-blue-600 hover:text-blue-800 transition"
              title="Ver detalle del pedido"
            >
              <i class="fa-solid fa-eye"></i>
            </button>`;
          }

          if (PermisoVerPedido()) {
            html += `
            <button
              onclick="DespachoPedido('${pedido.codPedido}')"
              class="text-purple-600 hover:text-purple-800 transition"
              title="Herramienta de admin/supervisor"
            >
              <i class="fa-solid fa-eye"></i>
            </button>`;
          }

          html += `</div>`;
          return html;
        },
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
        sortable: true,
        filter: true,
      },
      onGridReady: () => loadPedidos(),
    };

    gridApiPedidos = agGrid.createGrid(gridDiv, gridOptions);

    const searchBox = document.getElementById("searchBoxPedidos");
    if (searchBox) {
      searchBox.addEventListener("input", (e) => {
        gridApiPedidos.setQuickFilter(e.target.value.toLowerCase());
      });
    }
  }

  // =====================================================
  // 📡 Cargar pedidos según rol / ACL
  // =====================================================
  async function loadPedidos() {
    try {
      const endpoint = getEndpointPedidos();
      console.log("📡 Cargando pedidos desde:", endpoint);

      const res = await fetch(endpoint);
      if (!res.ok) throw new Error("Error al obtener pedidos");

      const data = await res.json();
      console.log("📦 Pedidos recibidos:", data.length);

      gridApiPedidos.setGridOption("rowData", data);
    } catch (err) {
      console.error("Error al cargar pedidos:", err);
      Swal.fire({
        icon: "error",
        title: "Error",
        text: "No se pudieron cargar los pedidos",
      });
    }
  }

  // =====================================================
  // 🔗 Ir a detalle
  // =====================================================
  window.DespachoPedido = (codPedido) => {
    localStorage.setItem("pedidoSeleccionado", codPedido);
    window.location.href = "/pedidos/despacho";
  };

  window.editarDetallePedido = (codPedido) => {
    localStorage.setItem("pedidoSeleccionado", codPedido);
    window.location.href = "/pedidos/editar";
  };
});
