document.addEventListener("DOMContentLoaded", () => {
  console.log("🧩 Iniciando ListaPedidos con ACL...");

  let gridApiPedidos = null;

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
  // Permisso por rol
  // =====================================================
  function PermisoVerPedido() {
    return idRol === 3;
  }

  function PermisoEditarPedido() {
    return idRol === 1 || idRol === 2;
  }

  initListaPedidos();

  async function initListaPedidos() {
    const gridDiv = document.querySelector("#pedidosGrid");
    if (!gridDiv) return;

    const columnDefs = [
      {
        headerName: "N° PEDIDO",
        field: "documento",
        minWidth: 120,
        sortable: true,
        resizable: false,
      },
      {
        headerName: "CLIENTE",
        field: "cliente.nombres",
        minWidth: 180,
        sortable: true,
        resizable: false,
        autoHeight: true,
        wrapText: true,
        cellClass: "cliente-cell",

        cellRenderer: (params) => {
          const c = params.data?.cliente;
          if (!c) return "-";

          return `
        <p style="line-height: 1.3; margin-top:10px; margin-bottom:10px;">
          ${c.nombres} ${c.apellidoPaterno ?? ""}
        </p>
    `;
        },
      },
      {
        headerName: "VENDEDOR",
        field: "usuario.usuario",
        minWidth: 130,
        sortable: true,
        resizable: false,
        autoHeight: true,
        cellRenderer: (params) =>
          `<span class="text-gray-700">${params.value ?? "-"}</span>`,
      },
      {
        headerName: "MONTO TOTAL",
        field: "montoTotal",
        minWidth: 150,
        sortable: true,
        resizable: false,
        cellRenderer: (params) =>
          `<span class="font-semibold text-blue-700">S/. ${(
            params.value ?? 0
          ).toFixed(2)}</span>`,
      },
      {
        headerName: "ESTADO",
        field: "estadoPedido.descripcion",
        minWidth: 150,
        sortable: true,
        resizable: false,
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
        headerName: "ITEMS",
        field: "detalles",
        minWidth: 100,
        sortable: true,
        resizable: false,
        cellRenderer: (params) => {
          const detalles = params.value ?? [];
          const totalItems = detalles.length;

          const wrapper = document.createElement("div");
          wrapper.className =
            "cursor-pointer font-semibold text-gray-800 select-none w-full h-full flex items-center";
          wrapper.textContent = `${totalItems} ítem${
            totalItems !== 1 ? "s" : ""
          }`;

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

          let cellElement = null;

          setTimeout(() => {
            cellElement = wrapper.closest(".ag-cell");

            if (!cellElement) return;

            cellElement.classList.add("relative");

            // 👉 Mostrar popover al entrar en la celda
            cellElement.addEventListener("mouseenter", () => {
              showPopover();
            });

            // 👉 Ocultar popover al salir inmediatamente
            cellElement.addEventListener("mouseleave", () => {
              hidePopover();
            });
          }, 0);

          function showPopover() {
            if (!cellElement) return;

            popover.classList.remove("hidden");
            popover.style.visibility = "hidden";
            popover.style.display = "block";

            const cellRect = cellElement.getBoundingClientRect();
            const popRect = popover.getBoundingClientRect();
            const margin = 8;

            let top = cellRect.bottom + margin;

            if (top + popRect.height > window.innerHeight) {
              top = cellRect.top - margin - popRect.height;
            }

            let left = cellRect.left + cellRect.width / 2 - popRect.width / 2;
            left = Math.max(
              margin,
              Math.min(left, window.innerWidth - popRect.width - margin)
            );

            popover.style.left = `${left}px`;
            popover.style.top = `${top}px`;
            popover.style.visibility = "visible";
          }

          function hidePopover() {
            popover.classList.add("hidden");
            popover.style.visibility = "hidden";
            popover.style.display = "none";
          }

          return wrapper;
        },
      },
      {
        headerName: "FECHA",
        field: "fechaReg",
        minWidth: 185,
        maxWidth: 185,
        sortable: true,
        resizable: false,
        valueFormatter: (params) => {
          if (!params.value) return "";

          const date = new Date(params.value);
          return date.toLocaleString("es-PE", {
            year: "numeric",
            month: "2-digit",
            day: "2-digit",
            hour: "2-digit",
            minute: "2-digit",
          });
        },
      },
      {
        headerName: "",
        Width: 100,
        sortable: true,
        resizable: false,
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
              <i class="fa-regular fa-pen-to-square"></i>
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

  window.DespachoPedido = (codPedido) => {
    localStorage.setItem("pedidoSeleccionado", codPedido);
    window.location.href = "/pedidos/despacho";
  };

  window.editarDetallePedido = (codPedido) => {
    localStorage.setItem("pedidoSeleccionado", codPedido);
    window.location.href = "/pedidos/editar";
  };
});
