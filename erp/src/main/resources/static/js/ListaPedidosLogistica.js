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
  loadPedidosAprobados();
  loadPedidosEnviados();

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
              <button 
                onclick="abrirModalCompletar('${p.data.codPedido}')"
                class="text-purple-600 hover:text-purple-800"
                title="Completar pedido"
              >
                <i class="fa-solid fa-check-to-slot text-xl"></i>
              </button>
            `,
          },
    ];
  }

  // -----------------------------------------
  // INIT GRIDS (Se crean solo una vez)
  // -----------------------------------------
  function initGrids() {
    gridAprobadosApi = agGrid.createGrid(
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

    gridEnviadosApi = agGrid.createGrid(
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
  //  CARGA DE DATA
  // -----------------------------------------
  async function loadPedidosAprobados() {
    try {
      const res = await fetch("/api/pedidos/logistica/estado/2");
      if (!res.ok) throw new Error("Error al cargar Aprobados");
      const data = await res.json();

      gridAprobadosApi.setGridOption("rowData", data);
    } catch (err) {
      Swal.fire(
        "Error",
        "No se pudieron cargar los pedidos aprobados",
        "error"
      );
    }
  }

  async function loadPedidosEnviados() {
    try {
      const res = await fetch("/api/pedidos/logistica/estado/3");
      if (!res.ok) throw new Error("Error al cargar Enviados");
      const data = await res.json();

      gridEnviadosApi.setGridOption("rowData", data);
    } catch (err) {
      Swal.fire("Error", "No se pudieron cargar los pedidos enviados", "error");
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
  // ABRIR DETALLE
  // -----------------------------------------
  window.DespachoPedido = (codPedido) => {
    localStorage.setItem("pedidoSeleccionado", codPedido);
    window.location.href = "/pedidos/despacho";
  };

  // =====================================================
  // 🚀 NUEVO: MODAL PARA SUBIR EVIDENCIAS Y COMPLETAR PEDIDO
  // =====================================================

  let pedidoCompletar = null;

  window.abrirModalCompletar = function (codPedido) {
    completarPedido.codPedido = codPedido;
    completarPedido.evidenciasFiles = [];

    document.getElementById("previewComprobante").innerHTML = "";
    document.getElementById("inputComprobante").value = "";

    document.getElementById("modalCompletar").classList.remove("hidden");
    document.getElementById("modalCompletar").classList.add("flex");

    initEvidenciaCompletar();
  };

  document
    .getElementById("btnCerrarCompletar")
    .addEventListener("click", () => {
      document.getElementById("modalCompletar").classList.add("hidden");
      document.getElementById("modalCompletar").classList.remove("flex");
    });

  function initEvidenciaCompletar() {
    const inputFile = document.getElementById("inputComprobante");
    const preview = document.getElementById("previewComprobante");

    preview.innerHTML = `
    <div id="dropCompletar"
      class="w-full h-40 border-2 border-dashed border-gray-300 rounded-lg flex flex-col items-center justify-center text-gray-400 cursor-pointer hover:text-purple-600 hover:border-purple-400 transition">
      <i class="fas fa-cloud-upload-alt text-4xl mb-2"></i>
      <p class="text-sm font-medium text-center">
        Subir evidencia<br/>
        <span class="text-xs text-gray-400">Click, arrastra o pega (Ctrl+V)</span>
      </p>
    </div>
  `;

    const drop = document.getElementById("dropCompletar");

    drop.onclick = () => inputFile.click();

    drop.addEventListener("dragover", (e) => {
      e.preventDefault();
      drop.classList.add("border-purple-400", "text-purple-600");
    });

    drop.addEventListener("dragleave", () => {
      drop.classList.remove("border-purple-400", "text-purple-600");
    });

    drop.addEventListener("drop", (e) => {
      e.preventDefault();
      drop.classList.remove("border-purple-400", "text-purple-600");

      const files = e.dataTransfer.files;
      for (const f of files) {
        if (f.type.startsWith("image/"))
          completarPedido.evidenciasFiles.push(f);
      }

      renderPreviewCompletar();
    });

    // input normal
    inputFile.onchange = (e) => {
      for (const f of e.target.files) {
        if (f.type.startsWith("image/"))
          completarPedido.evidenciasFiles.push(f);
      }
      renderPreviewCompletar();
      inputFile.value = "";
    };

    // pegar imágenes
    document.addEventListener("paste", (e) => {
      const items = e.clipboardData.items;
      for (const item of items) {
        if (item.type.startsWith("image/")) {
          completarPedido.evidenciasFiles.push(item.getAsFile());
        }
      }
      renderPreviewCompletar();
    });
  }

  function renderPreviewCompletar() {
    const preview = document.getElementById("previewComprobante");

    const old = document.getElementById("contenedorImgsCompletar");
    if (old) old.remove();

    const cont = document.createElement("div");
    cont.id = "contenedorImgsCompletar";
    cont.className =
      "mt-3 grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3";

    completarPedido.evidenciasFiles.forEach((file, index) => {
      const url = URL.createObjectURL(file);

      const card = document.createElement("div");
      card.className =
        "relative border rounded-lg overflow-hidden bg-white shadow-sm";

      card.innerHTML = `
      <img src="${url}" class="w-full h-24 object-cover" />
      <div class="px-2 py-1 text-[11px] text-gray-600 truncate">${file.name}</div>
      <button data-index="${index}"
        class="absolute top-1 right-1 bg-white/80 hover:bg-red-50 text-red-600 rounded-full w-6 h-6 flex items-center justify-center shadow">
        <i class="fa-solid fa-xmark text-xs"></i>
      </button>
    `;

      cont.appendChild(card);
    });

    preview.appendChild(cont);

    // eliminar
    cont.addEventListener("click", (e) => {
      const btn = e.target.closest("button[data-index]");
      if (!btn) return;
      const i = btn.dataset.index;
      completarPedido.evidenciasFiles.splice(i, 1);
      renderPreviewCompletar();
    });
  }

  document
    .getElementById("btnSubirCompletar")
    .addEventListener("click", async () => {
      if (completarPedido.evidenciasFiles.length === 0) {
        Swal.fire(
          "Falta evidencia",
          "Debes subir al menos 1 imagen",
          "warning"
        );
        return;
      }

      // Usuario de sesión (para log y evidencias)
      const usr = JSON.parse(sessionStorage.getItem("usuario"));

      // -------------------------------
      // ARMAR OBJETO PEDIDO COMPLETAR
      // -------------------------------
      const pedidoPayload = {
        codPedido: completarPedido.codPedido,

        // Estado 4 → COMPLETADO
        estadoPedido: { idEstadoPedido: 4 },

        // Evidencias nuevas (enviar con usuario)
        evidencias: completarPedido.evidenciasFiles.map(() => ({
          idEvidenciaPedido: null,
          codPedido: completarPedido.codPedido,
          idUsuario: usr.idUsuario,
          motivo: "COMPLETADO",
        })),

        // Log enviado desde el front
        logNuevo: {
          idUsuario: usr.idUsuario,
          codPedido: completarPedido.codPedido,
          idEstadoP: 4,
          motivoLog: "Pedido completado por logística",
        },
      };

      const formData = new FormData();
      formData.append(
        "pedido",
        new Blob([JSON.stringify(pedidoPayload)], {
          type: "application/json",
        })
      );

      // Archivos reales
      completarPedido.evidenciasFiles.forEach((f) =>
        formData.append("evidencias", f)
      );

      // -------------------------------
      // PETICIÓN AL ENDPOINT CORRECTO
      // -------------------------------
      try {
        const resp = await fetch("/api/pedidos/completar", {
          method: "POST",
          body: formData,
        });

        if (!resp.ok) throw new Error("Error al completar pedido");

        Swal.fire(
          "Completado",
          "El pedido ha sido marcado como COMPLETADO",
          "success"
        ).then(() => {
          document.getElementById("modalCompletar").classList.add("hidden");
          location.reload();
        });
      } catch (err) {
        Swal.fire("Error", err.message, "error");
      }
    });
});
