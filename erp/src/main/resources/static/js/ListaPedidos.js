document.addEventListener("DOMContentLoaded", () => {
  console.log("🧩 Iniciando ListaPedidos...");

  let gridApiPedidos = null;

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
          return `<div>
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
          const estado = params.value?.toUpperCase() || "";

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
              : estado === "CANCELADO"
              ? "bg-gray-200 text-gray-700"
              : "bg-slate-100 text-slate-700";

          return `
            <span class="px-3 py-1 rounded-full text-xs font-semibold ${color}">
              ${estado}
            </span>
          `;
        },
      },
      {
        headerName: "Detalles",
        field: "detalles",
        flex: 2,
        cellRenderer: (params) => {
          const detalles = params.value ?? [];
          const totalItems = detalles.length;
          const tooltip = document.createElement("div");
          tooltip.className =
            "hidden absolute bg-white border border-gray-200 rounded-lg shadow-lg p-3 min-w-[220px] z-[9999]";
          tooltip.innerHTML = `
            <p class="text-sm font-semibold text-indigo-600 mb-1">Productos en pedido</p>
            ${
              totalItems > 0
                ? detalles
                    .map(
                      (d) => `
                      <div class="flex justify-between text-sm">
                        <span>${d.nombreProducto}</span>
                        <span>x${d.cantidad}</span>
                      </div>`
                    )
                    .join("")
                : "<p class='text-gray-400 text-sm italic'>Sin detalles</p>"
            }
          `;
          document.body.appendChild(tooltip);

          const wrapper = document.createElement("div");
          wrapper.className =
            "relative cursor-pointer font-semibold text-gray-800 select-none";
          wrapper.textContent = `${totalItems} ítem${
            totalItems !== 1 ? "s" : ""
          }`;

          wrapper.addEventListener("mouseenter", (e) => {
            const rect = e.target.getBoundingClientRect();
            tooltip.style.display = "block";
            tooltip.style.left = `${rect.left + rect.width / 2 - 100}px`;
            tooltip.style.top = `${rect.bottom + 6}px`;
          });
          wrapper.addEventListener("mouseleave", () => {
            tooltip.style.display = "none";
          });

          return wrapper;
        },
      },
      {
        headerName: "Acciones",
        width: 150,
        filter: false,
        cellRenderer: (params) => {
          const pedido = params.data;
          return `
      <div class="flex justify-center gap-2">
        <button
          onclick="verDetallePedido(${pedido.idPedido})"
          class="text-blue-600 hover:text-blue-800 transition"
          title="Ver detalle del pedido"
        >
          <i class="fa-solid fa-eye"></i>
        </button>
      </div>
    `;
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
      const res = await fetch("/api/pedidos");
      if (!res.ok) throw new Error("Error al obtener pedidos");
      const data = await res.json();
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

  window.verDetallePedido = (idPedido) => {
    localStorage.setItem("pedidoSeleccionado", idPedido);
    window.location.href = "/pedidos/detallePedido";
  };
});
