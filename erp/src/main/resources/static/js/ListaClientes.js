(() => {
  if (window.clientesGridInicializado) return;
  window.clientesGridInicializado = true;

  let gridApiClientes = null;

  async function initListaClientes() {
    const gridDiv = document.querySelector("#clientesGrid");
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
        headerName: "Codigo",
        field: "codigoCliente",
        sortable: true,
        filter: true,
      },
      {
        headerName: "Nombre Completo",
        field: "nombres",
        sortable: true,
        filter: true,
      },
      {
        headerName: "Teléfono",
        field: "telefono",
        sortable: true,
        filter: true,
      },
      {
        headerName: "Correo",
        field: "correo",
        sortable: true,
        filter: true,
      },
      {
        headerName: "Acciones",
        width: 130,
        filter: false,
        cellRenderer: (params) => `
          <div class="flex items-center justify-center gap-2">
            <button 
              class="text-aliva-blue hover:text-aliva-purple transition"
              onclick="verDetalleCliente(${params.data.idCliente})">
              <i class="fa-solid fa-eye"></i>
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
      onGridReady: () => cargarClientes(),
    };

    gridApiClientes = agGrid.createGrid(gridDiv, gridOptions);
    window.gridApiClientes = gridApiClientes;

    // Buscador global
    const searchBox = document.getElementById("searchBox");
    if (searchBox) {
      searchBox.addEventListener("input", (e) => {
        const value = e.target.value.toLowerCase();
        gridApiClientes.setQuickFilter(value);
      });
    }
  }

  // Cargar clientes
  async function cargarClientes() {
    console.log("🔄 [FETCH] /api/clientes...");
    try {
      const res = await fetch("/api/clientes");
      if (!res.ok) throw new Error("Error al obtener clientes");

      const data = await res.json();
      console.log(`✅ [OK] ${data.length} clientes recibidos`);
      gridApiClientes.setGridOption("rowData", data);
    } catch (err) {
      console.error("❌ Error al cargar clientes:", err);
      Swal.fire({
        icon: "error",
        title: "Error",
        text: "No se pudieron cargar los clientes",
      });
    }
  }

  // Ver detalle del cliente + logs
  window.verDetalleCliente = async function (idCliente) {
    try {
      console.log(`👁️ Solicitando logs para cliente ID=${idCliente}`);

      const res = await fetch(`/api/clientes/${idCliente}/logs`);
      if (!res.ok) throw new Error("Error al obtener logs del cliente");
      const logs = await res.json();
      console.log("🧾 Logs recibidos:", logs);

      // Buscar datos del cliente en la grilla
      const fila = gridApiClientes.getDisplayedRowAtIndex(
        gridApiClientes.getDisplayedRowCount() - 1
      )?.data;

      if (!fila) {
        Swal.fire("Error", "No se encontró el cliente en la lista.", "error");
        return;
      }

      const logsHTML =
        logs.length > 0
          ? logs
              .map(
                (l) => `
                <li class="border-b py-2">
                  <p class="text-gray-800">${l.actividad}</p>
                  <p class="text-sm text-gray-500">
                    ${new Date(l.fechaActividad).toLocaleString()}
                  </p>
                </li>
              `
              )
              .join("")
          : '<p class="text-gray-500 italic text-center py-3">Sin actividad registrada.</p>';

      Swal.fire({
        title: `<strong>${fila.NOMBRE_COMPLETO}</strong>`,
        html: `
          <div class="text-left">
            <p><b>📞 Teléfono:</b> ${fila.TELEFONO || "—"}</p>
            <p><b>✉️ Correo:</b> ${fila.CORREO || "—"}</p>
            <p><b>📅 Fecha Registro:</b> ${fila.FECHA_REGISTRO || "—"}</p>
            <hr class="my-2">
            <h4 class="text-lg font-semibold mb-2">🕓 Actividades recientes</h4>
            <ul>${logsHTML}</ul>
          </div>
        `,
        width: 700,
        confirmButtonText: "Cerrar",
      });
    } catch (e) {
      console.error("❌ Error al obtener detalles del cliente:", e);
      Swal.fire(
        "Error",
        "No se pudieron cargar los detalles del cliente",
        "error"
      );
    }
  };

  window.initListaClientes = initListaClientes;
})();
