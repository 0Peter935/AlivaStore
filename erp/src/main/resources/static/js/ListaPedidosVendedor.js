document.addEventListener("DOMContentLoaded", () => {
  console.log("📦 Logística – Cargando vista...");

  // APIs de los dos grids
  let gridAprobadosApi = null;
  let gridEnviadosApi = null;

  let completarPedido = {
    codPedido: null,
    evidenciasFiles: [],
  };

  initTabs();
  initGrids();
  loadPedidosPendientes();
  loadPedidosRevision();

  // -----------------------------------------
  //  TABS (Cambiar visible)
  // -----------------------------------------
  function initTabs() {
    const tabAprobados = document.getElementById("tabAprobados");
    const tabEnviados = document.getElementById("tabEnviados");

    tabAprobados.addEventListener("click", () => activarTab("Aprobados"));
    tabEnviados.addEventListener("click", () => activarTab("Enviados"));
  }

  function activarTab(tab) {
    document
      .querySelectorAll(".tab-btn")
      .forEach((x) => x.classList.remove("active"));
    document.getElementById("tab" + tab).classList.add("active");

    // Mostrar buscador correcto
    document
      .getElementById("buscadorAprobados")
      .classList.toggle("hidden", tab !== "Aprobados");
    document
      .getElementById("buscadorEnviados")
      .classList.toggle("hidden", tab !== "Enviados");

    // Mostrar tabla correcta
    document
      .getElementById("tablaAprobados")
      .classList.toggle("hidden", tab !== "Aprobados");
    document
      .getElementById("tablaEnviados")
      .classList.toggle("hidden", tab !== "Enviados");

    // ❗ Importante: re-size del grid al cambiar pestaña
    setTimeout(() => {
      if (tab === "Aprobados") gridAprobadosApi.sizeColumnsToFit();
      else gridEnviadosApi.sizeColumnsToFit();
    }, 50);
  }

  // -----------------------------------------
  //  AG-GRID DEFINICIONES
  // -----------------------------------------
  function getColumnDefs(tipo) {
    return [
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
        minWidth: 130,
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
      tipo === "aprobados"
        ? {
            headerName: "",
            Width: 70,
            sortable: true,
            resizable: false,
            filter: false,
            cellRenderer: (p) => `
            <button onclick="DespachoPedido('${p.data.codPedido}')"
                class="text-blue-700 hover:text-blue-900">
              <i class="fa-solid fa-eye"></i>
            </button>`,
          }
        : {
            headerName: "",
            Width: 70,
            sortable: true,
            resizable: false,
            filter: false,
            cellRenderer: (p) => `
              <button onclick="RevisionPedido('${p.data.codPedido}')"
                  class="text-orange-600 hover:text-orange-800"
                  title="Revisar pedido">
                <i class="fa-solid fa-eye"></i>
              </button>
            `,
          },
    ];
  }

  // -----------------------------------------
  // INIT GRIDS (Se crean solo una vez)
  // -----------------------------------------
  function initGrids() {
    gridPendientesApi = agGrid.createGrid(
      document.getElementById("gridAprobados"),
      {
        columnDefs: getColumnDefs("aprobados"),
        rowData: [],
        pagination: true,
        paginationPageSize: 10,
        defaultColDef: {
          flex: 1,
          resizable: true,
          sortable: true,
          filter: true,
        },
      }
    );

    gridRevisionApi = agGrid.createGrid(
      document.getElementById("gridEnviados"),
      {
        columnDefs: getColumnDefs("enviados"),
        rowData: [],
        pagination: true,
        paginationPageSize: 10,
        defaultColDef: {
          flex: 1,
          resizable: true,
          sortable: true,
          filter: true,
        },
      }
    );
  }

  // -----------------------------------------
  //  CARGA DE DATA PARA VENDEDOR
  // -----------------------------------------

  async function loadPedidosPendientes() {
    try {
      const usr = JSON.parse(sessionStorage.getItem("usuario"));
      const idUsuario = usr?.idUsuario;

      if (!idUsuario) throw new Error("Usuario no encontrado en sesión");

      const res = await fetch(
        `/api/pedidos/vendedor/estado/1?idUsuario=${idUsuario}`
      );

      if (!res.ok) throw new Error("Error al cargar pendientes");

      const data = await res.json();

      gridPendientesApi.setGridOption("rowData", data);
    } catch (err) {
      Swal.fire(
        "Error",
        "No se pudieron cargar los pedidos pendientes",
        "error"
      );
    }
  }

  async function loadPedidosRevision() {
    try {
      const usr = JSON.parse(sessionStorage.getItem("usuario"));
      const idUsuario = usr?.idUsuario;

      if (!idUsuario) throw new Error("Usuario no encontrado en sesión");

      const res = await fetch(
        `/api/pedidos/vendedor/estado/5?idUsuario=${idUsuario}`
      );

      if (!res.ok) throw new Error("Error al cargar pedidos en revisión");

      const data = await res.json();

      gridRevisionApi.setGridOption("rowData", data);
    } catch (err) {
      Swal.fire(
        "Error",
        "No se pudieron cargar los pedidos en revisión",
        "error"
      );
    }
  }

  // -----------------------------------------
  //  BUSCADORES SEPARADOS
  // -----------------------------------------
  document
    .getElementById("inputSearchAprobados")
    .addEventListener("input", (e) => {
      gridAprobadosApi.setQuickFilter(e.target.value.toLowerCase());
    });

  document
    .getElementById("inputSearchEnviados")
    .addEventListener("input", (e) => {
      gridEnviadosApi.setQuickFilter(e.target.value.toLowerCase());
    });

  // -----------------------------------------
  // ABRIR PENDIENTE
  // -----------------------------------------
  window.DespachoPedido = (codPedido) => {
    localStorage.setItem("pedidoSeleccionado", codPedido);
    window.location.href = "/pedidos/editar";
  };

  // -----------------------------------------
  // ABRIR REVISION
  // -----------------------------------------
  window.RevisionPedido = (codPedido) => {
    localStorage.setItem("pedidoSeleccionado", codPedido);
    window.location.href = "/pedidos/revision";
  };
});
