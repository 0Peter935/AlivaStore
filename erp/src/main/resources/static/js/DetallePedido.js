// ======================= CONFIG =======================
const ENDPOINT_PEDIDO = (cod) => `/api/pedidos/${cod}`;
const ENDPOINT_GUARDAR = "/api/pedidos/guardarPedidoCompleto";
const ENDPOINT_PRODUCTOS = "/api/productos";

// ======================= ESTADO =======================
let gridOptions = null;
let productosNormales = [];
let productosRegalo = [];
let estadoAdelanto = false;
let montoAdelanto = 0;
let productoSeleccionado = null;
let varianteSeleccionada = null;

let pedidoActual = {
  codPedido: null,
  codCliente: null,
  evidenciasFiles: [],
  evidenciasServidor: [],
  evidenciasEliminar: [],
};

// ======================= INIT =======================
async function initDetallePedido() {
  console.log("📌 initDetallePedido ejecutado");

  pedidoActual.codPedido = localStorage.getItem("pedidoSeleccionado");

  if (!pedidoActual.codPedido) {
    Swal.fire("Error", "No se encontró el pedido seleccionado", "warning");
    return;
  }

  const pedido = await fetchJson(
    ENDPOINT_PEDIDO(pedidoActual.codPedido),
    "GET"
  );
  console.log("Pedido cargado:", pedido);

  pedidoActual.evidenciasServidor = Array.isArray(pedido.evidencias)
    ? pedido.evidencias
    : [];
  pedidoActual.evidenciasEliminar = [];

  cargarSelectsPedido(pedido);
  initEvidencia();
  initPago();

  // Cargar productos (catálogo)
  await cargarProductos();

  // Normalizar detalles: regalo -> booleano
  const detalles = (pedido.detalles || []).map((d) => {
    const r = d.esRegalo === true || d.esRegalo === 1;

    return {
      ...d,
      esRegalo: r,
      producto: {
        ...(d.producto || {}),
        regalo: r, // compatibilidad con el código existente
      },
    };
  });
  initGridProductos(detalles);

  // Renderizar secciones
  renderCliente(pedido.cliente);
  renderPedido(pedido);
  renderEvidencia(pedido);
  renderEstadoPago(pedido);
  renderNotas(pedido.notas);
  renderResumenFinanciero(calcularFinanzas());

  // Botón agregar producto
  document
    .getElementById("btnAgregarProducto")
    ?.addEventListener("click", abrirModalAgregar);

  // Botón guardar
  document
    .getElementById("btnGuardarPedido")
    ?.addEventListener("click", onGuardarPedido);

  // Botón aprobar
  document
    .getElementById("btnAprobarPedido")
    ?.addEventListener("click", () => onGuardarPedido(true)); // ← true significa APROBAR
}

// ======================= HTTP =======================
async function fetchJson(url, method = "GET", body) {
  const resp = await fetch(url, { method, body });
  const text = await resp.text();
  try {
    return JSON.parse(text);
  } catch {
    return {};
  }
}

// ======================= CARGA DE DATOS =======================
async function cargarProductos() {
  console.group("[cargarProductos]");
  try {
    const data = await fetchJson(ENDPOINT_PRODUCTOS, "GET");
    productosNormales = data.filter((p) => !p.regalo);
    productosRegalo = data.filter((p) => p.regalo);
    console.log("Catálogo:", {
      normales: productosNormales.length,
      regalos: productosRegalo.length,
    });
  } finally {
    console.groupEnd();
  }
}

// ======================= RENDERS =======================
function renderCliente(cliente) {
  if (!cliente) return;

  document.getElementById("clienteDni").value = cliente.dni ?? "";
  document.getElementById("clienteNombre").value = cliente.nombres ?? "";
  document.getElementById("clienteTelefono").value = cliente.telefono ?? "";
  document.getElementById("clienteDireccion").value = cliente.direccion ?? "";
  document.getElementById("clienteCiudad").value = cliente.ciudad ?? "";
  document.getElementById("clienteProvincia").value = cliente.provincia ?? "";

  pedidoActual.codCliente = cliente.codCliente;
}

function renderPedido(pedido = {}) {
  // Helpers
  const get = (id) => document.getElementById(id);

  const setValue = (id, value) => {
    const el = get(id);
    if (!el) return;
    el.value = value ?? "";
  };

  const setText = (id, value) => {
    const el = get(id);
    if (!el) return;
    el.textContent = value ?? "";
  };

  // Normalizar campos que vienen del backend
  const tipoPago = (pedido.tipoPago ?? pedido.tipo_pago ?? "")
    .toString()
    .trim()
    .toUpperCase();

  const tipoComprobante = (
    pedido.tipoComprobante ??
    pedido.tipo_comprobante ??
    ""
  )
    .toString()
    .trim()
    .toUpperCase();

  const ciudad = pedido.ciudad ?? pedido.ciudadPedido ?? "";
  const observacion = pedido.observacion ?? pedido.observaciones ?? "";

  // N° de orden
  setText("pedidoDocumentoLabel", `N° ORDEN: ${pedido.documento ?? "---"}`);

  // Inputs
  setValue("pedidoCiudad", ciudad);
  setValue("pedidoObservaciones", observacion);

  // Selects: primero dejamos el valor, y luego
  // cargarSelectsPedido se encarga de marcar el option correcto
  const selPago = get("pedidoTipoPago");
  if (selPago) selPago.value = tipoPago;

  const selComp = get("pedidoComprobante");
  if (selComp) selComp.value = tipoComprobante;
}

function renderEvidencia(pedido) {
  const preview = document.getElementById("previewComprobante");
  const inputFile = document.getElementById("inputComprobante");
  if (!preview || !inputFile) return;

  // 🔄 reset contenedor
  preview.innerHTML = `
    <div
      id="dropEvidencias"
      class="w-full h-40 border-2 border-dashed border-gray-300 rounded-lg flex flex-col items-center justify-center text-gray-400 cursor-default hover:text-purple-600 hover:border-purple-400 transition relative overflow-hidden"
    >
      <i class="fas fa-cloud-upload-alt text-4xl mb-2"></i>
      <p class="text-sm font-medium text-center">
        Sube imágenes de comprobante<br/>
        <span class="text-xs text-gray-400">
          Haz clic en el ícono, arrastra o pega (Ctrl+V)
        </span>
      </p>
    </div>
  `;

  const dropZone = document.getElementById("dropEvidencias");
  const icono = dropZone.querySelector("i.fas.fa-cloud-upload-alt");
  if (icono) {
    icono.style.cursor = "pointer";
    icono.onclick = (e) => {
      e.stopPropagation();
      inputFile.click();
    };
  }

  dropZone.addEventListener("dragover", (e) => {
    e.preventDefault();
    dropZone.classList.add("border-purple-400", "text-purple-600");
  });

  dropZone.addEventListener("dragleave", () => {
    dropZone.classList.remove("border-purple-400", "text-purple-600");
  });

  dropZone.addEventListener("drop", (e) => {
    e.preventDefault();
    dropZone.classList.remove("border-purple-400", "text-purple-600");

    const files = e.dataTransfer?.files;
    if (!files || !files.length) return;

    if (!pedidoActual.evidenciasFiles) pedidoActual.evidenciasFiles = [];

    for (const f of files) {
      if (!f.type.startsWith("image/")) continue;
      pedidoActual.evidenciasFiles.push(f);
    }

    renderPreviewEvidencias();
  });

  // 🖼️ Evidencias que ya vienen guardadas en BD (solo mostrar)
  const evidenciasServidor = pedidoActual.evidenciasServidor || [];

  if (evidenciasServidor.length) {
    const container = document.createElement("div");
    container.className =
      "mt-3 grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3";

    evidenciasServidor.forEach((ev, index) => {
      const url = ev.url || ev.ruta || "";
      const nombre = ev.nombre || ev.nombreArchivo || `evidencia-${index + 1}`;
      const idEv = ev.idEvidenciaPedido;
      const motivo = (ev.motivo || "").toUpperCase();
      const puedeEliminar = motivo === "APROPACION";

      const card = document.createElement("div");
      card.className =
        "relative border rounded-lg overflow-hidden bg-white shadow-sm";
      card.dataset.idEvidencia = idEv; // para ubicarla luego

      card.innerHTML = `
      <img src="${url}"
           class="w-full h-24 object-cover"
           alt="${nombre}" />
      <div class="px-2 py-1 text-[11px] text-gray-600 truncate">
        ${nombre}
      </div>
      ${
        puedeEliminar
          ? `
            <button
              type="button"
              class="absolute top-1 right-1 bg-white/80 hover:bg-red-50 text-red-500 rounded-full w-6 h-6 flex items-center justify-center shadow btn-del-evidencia"
              title="Eliminar evidencia de APROPACION"
              data-id="${idEv}"
            >
              <i class="fa-solid fa-xmark text-xs"></i>
            </button>
          `
          : `
            <span
              class="absolute top-1 right-1 text-[9px] px-2 py-0.5 rounded-full bg-gray-200 text-gray-500 cursor-not-allowed"
              title="Esta evidencia no se puede eliminar (motivo: ${
                motivo || "N/A"
              })"
            >
              ${motivo || "FIJO"}
            </span>
          `
      }
    `;

      container.appendChild(card);
    });

    // 👉 Evento delegado para borrar SOLO las de APROPACION
    container.addEventListener("click", (e) => {
      const btn = e.target.closest(".btn-del-evidencia");
      if (!btn) return;

      const id = Number(btn.dataset.id);
      if (Number.isNaN(id)) return;

      // Buscamos la evidencia para verificar motivo por si acaso
      const ev = pedidoActual.evidenciasServidor.find(
        (x) => x.idEvidenciaPedido === id
      );
      const motivo = (ev?.motivo || "").toUpperCase();

      if (motivo !== "APROPACION") {
        Swal.fire(
          "No permitido",
          "Solo se pueden eliminar evidencias con motivo APROPACION.",
          "info"
        );
        return;
      }

      // Aseguramos array
      if (!Array.isArray(pedidoActual.evidenciasEliminar)) {
        pedidoActual.evidenciasEliminar = [];
      }

      if (!pedidoActual.evidenciasEliminar.includes(id)) {
        pedidoActual.evidenciasEliminar.push(id);
      }

      // Quitar del DOM
      const card = btn.closest("[data-id-evidencia]");
      if (card) card.remove();

      // Quitar del array en memoria
      pedidoActual.evidenciasServidor = pedidoActual.evidenciasServidor.filter(
        (x) => x.idEvidenciaPedido !== id
      );

      console.log("Marcada para borrar:", pedidoActual.evidenciasEliminar);
    });

    preview.appendChild(container);
  }

  // 🆕 Miniaturas nuevas
  renderPreviewEvidencias();
}

function renderPreviewEvidencias() {
  const preview = document.getElementById("previewComprobante");
  if (!preview) return;

  // borrar contenedor previo de nuevas evidencias, si existe
  const old = preview.querySelector("#nuevasEvidencias");
  if (old) old.remove();

  if (!pedidoActual.evidenciasFiles || !pedidoActual.evidenciasFiles.length) {
    return;
  }

  const container = document.createElement("div");
  container.id = "nuevasEvidencias";
  container.className =
    "mt-3 grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3";

  pedidoActual.evidenciasFiles.forEach((file, index) => {
    const card = document.createElement("div");
    card.className =
      "relative border rounded-lg overflow-hidden bg-white shadow-sm group";

    const url = URL.createObjectURL(file);

    card.innerHTML = `
      <img src="${url}"
           class="w-full h-24 object-cover"
           alt="${file.name}" />
      <div class="px-2 py-1 text-[11px] text-gray-600 truncate">
        ${file.name}
      </div>
      <button
        type="button"
        class="absolute top-1 right-1 bg-white/80 hover:bg-red-50 text-red-500 rounded-full w-6 h-6 flex items-center justify-center shadow"
        title="Quitar"
        data-index="${index}"
      >
        <i class="fa-solid fa-xmark text-xs"></i>
      </button>
    `;

    container.appendChild(card);
  });

  preview.appendChild(container);

  // eliminar uno de la lista
  container.addEventListener("click", (e) => {
    const btn = e.target.closest("button[data-index]");
    if (!btn) return;

    const idx = Number(btn.dataset.index);
    if (Number.isNaN(idx)) return;

    pedidoActual.evidenciasFiles.splice(idx, 1);
    renderPreviewEvidencias();
  });
}

function renderEstadoPago(pedido) {
  const pagoCompletoCard = document.querySelector(
    "#estadoPagoContainer > div:nth-child(2) > div:nth-child(1)"
  );
  const pagoAdelantoCard = document.querySelector(
    "#estadoPagoContainer > div:nth-child(2) > div:nth-child(2)"
  );
  const inputAdelanto = document.getElementById("montoAdelanto");
  const adelantoContainer = document.getElementById("adelantoContainer");

  if (!pagoCompletoCard || !pagoAdelantoCard) return;

  // Reset estilos
  [pagoCompletoCard, pagoAdelantoCard].forEach((card) => {
    card.classList.remove("border-purple-500", "bg-purple-50");
    card.classList.add("border-gray-300");
    const icon = card.querySelector("i.fa-check-circle, i.fa-circle");
    if (icon) {
      icon.classList.remove("fa-check-circle", "fas", "text-purple-600");
      icon.classList.add("fa-circle", "far", "text-gray-400");
    }
  });

  if (pedido.adelanto === true) {
    // ✅ Pago con adelanto
    pagoAdelantoCard.classList.add("border-purple-500", "bg-purple-50");
    const icon = pagoAdelantoCard.querySelector("i.far.fa-circle");
    if (icon) {
      icon.classList.remove("fa-circle", "far", "text-gray-400");
      icon.classList.add("fa-check-circle", "fas", "text-purple-600");
    }
    adelantoContainer.classList.remove("hidden");
    inputAdelanto.value = Number(pedido.montoAdelanto || 0).toFixed(2);
  } else {
    // ✅ Sin adelanto
    pagoCompletoCard.classList.add("border-purple-500", "bg-purple-50");
    const icon = pagoCompletoCard.querySelector("i.far.fa-circle");
    if (icon) {
      icon.classList.remove("fa-circle", "far", "text-gray-400");
      icon.classList.add("fa-check-circle", "fas", "text-purple-600");
    }
    adelantoContainer.classList.add("hidden");
    inputAdelanto.value = "0.00";
  }

  // Sincronizar variables globales
  estadoAdelanto = !!pedido.adelanto;
  montoAdelanto = Number(pedido.montoAdelanto || 0);
}

function renderNotas(notas = []) {
  const cont = document.getElementById("listaNotas");
  if (!cont) return;

  if (notas.length === 0) {
    cont.innerHTML = `
      <p class="text-gray-500 italic">Sin notas registradas</p>
    `;
    return;
  }

  cont.innerHTML = notas
    .map(
      (n) => `
        <div class="nota-card border border-gray-300 rounded-lg p-2.5 bg-gray-50 shadow-sm leading-tight">
          <p class="text-[11px] text-gray-500 mb-1 font-semibold">
            ID #${n.idNotaPedido}
          </p>
          <p class="font-semibold text-[14px]">${n.titulo}:</p>
          <p class="text-gray-700 text-[13px] whitespace-pre-line">
            ${n.descripcion}
          </p>
        </div>
      `
    )
    .join("");
}

function renderResumenFinanciero({ subtotal, igv, total }) {
  const subtotalEl = document.getElementById("subtotalValor");
  const igvEl = document.getElementById("igvValor");
  const totalEl = document.getElementById("totalValor");

  // 👇 Subtotal que quieres mostrar en el resumen: subtotal - IGV
  const subtotalSinIgv = subtotal - igv;

  subtotalEl.textContent = `S/ ${subtotalSinIgv.toFixed(2)}`;
  igvEl.textContent = `S/ ${igv.toFixed(2)}`;
  totalEl.textContent = `S/ ${total.toFixed(2)}`;

  // 👇 Subtotal que va debajo de la tabla (subtotal de productos)
  const tablaSubtotalEl = document.getElementById("subtotalProductosTabla");
  if (tablaSubtotalEl) {
    // aquí tiene sentido mostrar el subtotal bruto (antes de IGV)
    tablaSubtotalEl.textContent = `S/ ${subtotal.toFixed(2)}`;
  }
}

// ======================= GRID =======================
function initGridProductos(detalles) {
  const columnDefs = [
    {
      headerName: "N°",
      valueGetter: (params) => params.node.rowIndex + 1,
      minWidth: 70,
      maxWidth: 70,
      sortable: true,
      resizable: false,
      cellClass: "text-center",
    },
    {
      headerName: "DESCRIPCIÓN",
      field: "nombreProducto",
      minWidth: 430,
      maxWidth: 430,
      sortable: true,
      resizable: false,
      autoHeight: true,
      cellRenderer: (params) => {
        return `<div class="wrap-text">${params.value || ""}</div>`;
      },
    },
    {
      headerName: "CANTIDAD",
      field: "cantidad",
      minWidth: 130,
      maxWidth: 130,
      sortable: true,
      resizable: false,
      cellRenderer: (params) => {
        // contenedor de los botones y el valor
        const wrapper = document.createElement("div");
        // 👈 ahora alineado a la izquierda
        wrapper.className = "flex items-center justify-start gap-2";

        const btnMinus = document.createElement("button");
        btnMinus.innerHTML = `<i class="fa-solid fa-minus"></i>`;
        btnMinus.className =
          "w-6 h-6 flex items-center justify-center text-gray-600 hover:text-gray-800 text-[12px]";

        const valueSpan = document.createElement("span");
        valueSpan.textContent = params.value ?? 1;
        valueSpan.className = "min-w-[16px] text-center text-[13px]";

        const btnPlus = document.createElement("button");
        btnPlus.innerHTML = `<i class="fa-solid fa-plus"></i>`;
        btnPlus.className =
          "w-6 h-6 flex items-center justify-center text-gray-600 hover:text-gray-800 text-[12px]";

        const updateCantidad = (delta) => {
          let v = Number(params.data.cantidad || 0) + delta;
          if (v < 1) v = 1;

          params.data.cantidad = v;
          params.data.precioTotal = v * (params.data.precioUnitario || 0);
          valueSpan.textContent = v;

          params.api.refreshCells({
            rowNodes: [params.node],
            columns: ["subtotal"],
            force: true,
          });

          renderResumenFinanciero(calcularFinanzas());
        };

        btnMinus.onclick = (e) => {
          e.stopPropagation();
          updateCantidad(-1);
        };

        btnPlus.onclick = (e) => {
          e.stopPropagation();
          updateCantidad(1);
        };

        wrapper.append(btnMinus, valueSpan, btnPlus);
        return wrapper;
      },
    },
    {
      headerName: "PRECIO U.",
      field: "precioUnitario",
      minWidth: 130,
      maxWidth: 130,
      sortable: true,
      resizable: false,
      cellClass: "flex items-center", // opcional para centrar vertical

      cellRenderer: (p) =>
        p.data.esRegalo
          ? `<span class="text-gray-400">----</span>`
          : `S/ ${(p.value ?? 0).toFixed(2)}`,
    },
    {
      headerName: "SUBTOTAL",
      colId: "subtotal",
      minWidth: 130,
      maxWidth: 130,
      sortable: true,
      resizable: false,
      valueGetter: (p) =>
        p.data.esRegalo
          ? null
          : (p.data.cantidad || 0) * (p.data.precioUnitario || 0),
      width: 150,
      cellRenderer: (p) =>
        p.data.esRegalo
          ? `<span class="text-gray-400">----</span>`
          : `S/ ${(p.value ?? 0).toFixed(2)}`,
    },
    {
      headerName: "Acción",
      field: "accion",
      flex: 1, // ← esto hace que se estire hasta el borde derecho
      minWidth: 130, // evita que se haga demasiado pequeña
      sortable: false,
      resizable: false,

      cellClass: "flex items-center justify-start",
      // 👆 Centrado vertical y alineado a la izquierda

      cellRenderer: () => `
    <div class="flex items-center justify-start gap-3 h-full pl-3">
      <button 
        data-action="view"
        class="text-blue-600 hover:text-blue-700 text-xl"
        title="Ver imagen"
      >
        <i class="fa-solid fa-image"></i>
      </button>

      <button 
        data-action="delete"
        class="text-red-500 hover:text-red-600 text-xl"
        title="Eliminar"
      >
        <i class="fa-solid fa-trash"></i>
      </button>
    </div>
  `,
    },
  ];

  gridOptions = {
    columnDefs,
    rowData: detalles,
    domLayout: "autoHeight",
    defaultColDef: { resizable: true, sortable: true },

    enableCellTextSelection: true,
    suppressRowClickSelection: true,

    onCellClicked: (event) => {
      if (
        event.colDef.headerName === "Acción" &&
        event.event.target.closest("button")
      ) {
        const btn = event.event.target.closest("button");
        const action = btn.dataset.action;
        if (action === "delete") eliminarRegalo(event.node);
        if (action === "view") abrirModalDetalleProducto(event.data);
      }
    },
    getRowStyle: (params) => {
      if (params.data?.esRegalo) return { backgroundColor: "#676ce940" };
      return null;
    },
  };

  const gridEl = document.querySelector("#detallePedidoGrid");
  new agGrid.Grid(gridEl, gridOptions);
  console.log("AG Grid inicializado con", detalles.length, "filas");
  reordenarFilasGrid();
}

function reordenarFilasGrid() {
  if (!gridOptions?.api) return;

  const rows = [];
  gridOptions.api.forEachNode((n) => rows.push(n.data));

  rows.sort((a, b) => {
    const ra = a.esRegalo ? 1 : 0;
    const rb = b.esRegalo ? 1 : 0;

    // normales (0) primero, regalos (1) al final
    if (ra !== rb) return ra - rb;

    // si quieres mantener el orden original, no hagas más comparación
    return 0;
  });

  gridOptions.api.setRowData(rows);
}

function eliminarRegalo(rowNode) {
  Swal.fire({
    title: "¿Eliminar producto?",
    text: "Esta acción no se puede deshacer",
    icon: "warning",
    showCancelButton: true,
    confirmButtonText: "Sí, eliminar",
    cancelButtonText: "Cancelar",
  }).then((r) => {
    if (r.isConfirmed && gridOptions?.api) {
      gridOptions.api.applyTransaction({ remove: [rowNode.data] });
      reordenarFilasGrid();
      renderResumenFinanciero(calcularFinanzas());
      Swal.fire("Eliminado", "El producto fue eliminado", "success");
    }
  });
}

// ======================= MODAL AGREGAR =======================
function abrirModalAgregar() {
  const modal = document.getElementById("modalAgregarProducto");
  const toggle = document.getElementById("toggleRegalo");
  const toggleCircle = document.getElementById("toggleCircle");
  const toggleSwitch = document.getElementById("toggleSwitch");
  const inputBusqueda = document.getElementById("inputBuscarProducto");
  const listaSugerencias = document.getElementById("listaSugerenciasProductos");
  const inputCantidad = document.getElementById("inputCantidad");

  if (!productosNormales.length && !productosRegalo.length) {
    Swal.fire("Cargando productos", "Vuelve a intentar en un momento.", "info");
    return;
  }

  // 🔹 Reset selección
  productoSeleccionado = null;
  varianteSeleccionada = null;
  inputBusqueda.value = "";
  inputCantidad.value = 1;

  // 🔹 Helper: obtiene la lista base (normal/regalo)
  const getListaBase = (esRegalo) =>
    esRegalo ? productosRegalo : productosNormales;

  // 🔹 Render del dropdown
  const renderSugerencias = (termino = "") => {
    const esRegalo = toggle.checked;
    const lista = getListaBase(esRegalo);
    const term = termino.trim().toLowerCase();

    // Si no hay texto, mostramos máximo 20 productos/variantes
    const maxItems = 30;

    let html = "";

    let count = 0;
    for (const prod of lista) {
      const variantes =
        prod.variante && prod.variante.length ? prod.variante : [null]; // si no tiene variantes, tratamos al producto como una sola opción

      // ¿Hay al menos una variante que matchee filtro?
      const variantesFiltradas = variantes.filter((v) => {
        const textProducto = (prod.descProducto || "").toLowerCase();
        const textVariante = (v?.titulo || "").toLowerCase();
        const codProducto = (prod.codProducto || "").toLowerCase();
        const codVariante = (v?.codVariante || "").toLowerCase();

        if (!term) return true; // sin filtro, mostrar todo

        return (
          textProducto.includes(term) ||
          textVariante.includes(term) ||
          codProducto.includes(term) ||
          codVariante.includes(term)
        );
      });

      if (!variantesFiltradas.length) continue;

      // Header del producto
      html += `
        <div class="px-3 pt-2 pb-1 text-[11px] font-semibold text-gray-500 uppercase border-t border-gray-100 first:border-t-0 bg-gray-50">
          ${prod.codProducto || ""} - ${
        prod.descProducto || "Producto sin nombre"
      }
        </div>
      `;

      // Variantes
      for (const v of variantesFiltradas) {
        if (count >= maxItems) break;

        const titulo =
          v?.titulo || v?.codVariante || "Variante única / Sin título";

        html += `
          <button
            type="button"
            class="w-full text-left px-3 py-2 text-sm hover:bg-purple-50 flex flex-col gap-0.5"
            data-cod-producto="${prod.codProducto || ""}"
            data-cod-variante="${v?.codVariante || ""}"
          >
            <span class="text-[13px] text-gray-800">${titulo}</span>
            ${
              v?.codVariante
                ? `<span class="text-[11px] text-gray-500">Variante: ${v.codVariante}</span>`
                : ""
            }
          </button>
        `;

        count++;
      }

      if (count >= maxItems) break;
    }

    if (!html) {
      html =
        '<div class="px-3 py-2 text-sm text-gray-500">Sin resultados para tu búsqueda.</div>';
    }

    listaSugerencias.innerHTML = html;
    listaSugerencias.classList.remove("hidden");
  };

  // 🔹 Eventos del input
  inputBusqueda.oninput = (e) => {
    productoSeleccionado = null;
    varianteSeleccionada = null;
    renderSugerencias(e.target.value);
  };

  inputBusqueda.onfocus = () => {
    renderSugerencias(inputBusqueda.value);
  };

  // 🔹 Click en una sugerencia
  listaSugerencias.onclick = (e) => {
    const btn = e.target.closest("button[data-cod-producto]");
    if (!btn) return;

    const codProducto = btn.dataset.codProducto;
    const codVariante = btn.dataset.codVariante;

    const esRegalo = toggle.checked;
    const lista = getListaBase(esRegalo);

    const prod = lista.find((p) => p.codProducto === codProducto);
    if (!prod) return;

    let variante = null;
    if (codVariante) {
      variante = (prod.variante || []).find(
        (v) => v.codVariante === codVariante
      );
    }

    productoSeleccionado = prod;
    varianteSeleccionada = variante || null;

    const textoVar =
      varianteSeleccionada?.titulo || varianteSeleccionada?.codVariante || "";
    inputBusqueda.value = `${prod.codProducto || ""} - ${
      prod.descProducto || "Producto"
    }${textoVar ? " | " + textoVar : ""}`;

    listaSugerencias.classList.add("hidden");
  };

  // 🔹 Toggle Normal / Regalo
  toggle.onchange = () => {
    const esRegalo = toggle.checked;
    toggleCircle.style.transform = esRegalo
      ? "translateX(20px)"
      : "translateX(0px)";
    toggleSwitch.style.backgroundColor = esRegalo ? "#3b82f6" : "#d1d5db";

    // Reset selección al cambiar tipo
    productoSeleccionado = null;
    varianteSeleccionada = null;
    inputBusqueda.value = "";
    inputCantidad.value = 1;
    renderSugerencias("");
  };

  // Estado inicial
  toggle.checked = false;
  toggle.onchange();
  modal.classList.remove("hidden");
  modal.classList.add("flex");

  // Botón cancelar
  document.getElementById("btnCancelarAgregar").onclick = () => {
    modal.classList.add("hidden");
    modal.classList.remove("flex");
  };

  // 🔹 Botón confirmar
  document.getElementById("btnConfirmarAgregar").onclick = () => {
    const cantidad = Number(inputCantidad.value) || 1;
    const esRegalo = toggle.checked;

    if (!productoSeleccionado) {
      Swal.fire("Selecciona un producto / variante", "", "warning");
      return;
    }

    const prod = productoSeleccionado;
    const variante = varianteSeleccionada;

    // Precio: sacamos de la variante si tiene, si no del producto
    const precioBase = esRegalo ? 0 : variante?.precio ?? prod.precio ?? 0;

    const precioUnitario = precioBase;
    const precioTotal = precioUnitario * cantidad;

    // Unificar si ya existe misma combinación producto+variante+regalo
    const filas = [];
    gridOptions.api.forEachNode((n) => filas.push(n.data));

    const existente = filas.find((r) => {
      const codP = r.codProducto || r.producto?.codProducto;
      const codV = r.codVariante || r.variante?.codVariante;

      return (
        codP === prod.codProducto &&
        (codV || "") === (variante?.codVariante || "") &&
        !!(r.esRegalo || r.regalo) === !!esRegalo
      );
    });

    if (existente) {
      existente.cantidad += cantidad;
      existente.precioTotal =
        existente.cantidad * (existente.precioUnitario || 0);
      gridOptions.api.applyTransaction({ update: [existente] });
    } else {
      gridOptions.api.applyTransaction({
        add: [
          {
            variante: variante || {},
            codProducto: prod.codProducto,
            codVariante: variante?.codVariante || null,
            nombreProducto: prod.descProducto || variante?.titulo || "Producto",
            cantidad,
            precioUnitario,
            precioTotal,
            esRegalo: esRegalo,
          },
        ],
      });
    }

    reordenarFilasGrid();
    renderResumenFinanciero(calcularFinanzas());

    modal.classList.add("hidden");
    modal.classList.remove("flex");

    Swal.fire({
      title: esRegalo ? "🎁 Regalo agregado" : "✅ Producto agregado",
      icon: "success",
      timer: 1600,
      showConfirmButton: false,
      position: "top-end",
      toast: true,
    });
  };

  // 🔹 Cerrar sugerencias si se hace clic fuera del input/lista
  const handleClickOutsideSugerencias = (e) => {
    if (!listaSugerencias.contains(e.target) && e.target !== inputBusqueda) {
      listaSugerencias.classList.add("hidden");
    }
  };

  // lo registramos sin "once"
  document.addEventListener("click", handleClickOutsideSugerencias);
}

// ======================= MODAL DETALLE PRODUCTO =======================
function abrirModalDetalleProducto(row) {
  const modal = document.getElementById("modalDetalleProducto");
  const imgEl = document.getElementById("detalleProdImagen");
  const nombreEl = document.getElementById("detalleProdNombre");
  const varianteEl = document.getElementById("detalleProdVariante");

  const prod = row.producto || {};
  const variante = row.variante || {};

  // 👀 Nombre completo (el mismo que sale en la columna DESCRIPCIÓN)
  const nombre =
    row.nombreProducto || prod.descProducto || "Producto sin nombre";

  // Texto de variante / código
  const textoVariante =
    variante.titulo ||
    row.tituloVariante ||
    variante.codVariante ||
    row.codVariante ||
    "";

  // primero variante, luego fallback
  const imgUrl = variante.imgVariante || row.imgVariante || "/img/no-image.png";

  imgEl.src = imgUrl;
  imgEl.onerror = () => {
    imgEl.src = "/recursos/img/no-image.png";
  };

  nombreEl.textContent = nombre;
  varianteEl.textContent = textoVariante
    ? `Variante: ${textoVariante}`
    : "Sin variante específica";

  modal.classList.remove("hidden");
  modal.classList.add("flex");
}

// Cerrar con botón ✖
document
  .getElementById("btnCerrarDetalleProducto")
  ?.addEventListener("click", () => {
    const modal = document.getElementById("modalDetalleProducto");
    modal.classList.add("hidden");
    modal.classList.remove("flex");
  });

// Cerrar haciendo click en el fondo oscuro
document
  .getElementById("modalDetalleProducto")
  ?.addEventListener("click", (e) => {
    if (e.target.id === "modalDetalleProducto") {
      e.currentTarget.classList.add("hidden");
      e.currentTarget.classList.remove("flex");
    }
  });

// ======================= EVIDENCIA + PAGO =======================
function initEvidencia() {
  const inputComprobante = document.getElementById("inputComprobante");
  if (!inputComprobante) return;

  // aseguramos el array
  if (!pedidoActual.evidenciasFiles) {
    pedidoActual.evidenciasFiles = [];
  }

  // cuando el usuario elige archivos con el diálogo
  inputComprobante.addEventListener("change", (e) => {
    const files = e.target.files;
    if (!files || !files.length) return;

    for (const f of files) {
      if (!f.type.startsWith("image/")) continue;
      pedidoActual.evidenciasFiles.push(f);
    }

    renderPreviewEvidencias();

    // limpiar para poder volver a elegir los mismos archivos
    inputComprobante.value = "";
  });

  // pegar desde portapapeles (Ctrl+V)
  document.addEventListener("paste", (e) => {
    const items = e.clipboardData?.items;
    if (!items) return;

    let added = false;
    for (const item of items) {
      if (item.type && item.type.startsWith("image/")) {
        const file = item.getAsFile();
        if (file) {
          pedidoActual.evidenciasFiles.push(file);
          added = true;
        }
      }
    }

    if (added) {
      renderPreviewEvidencias();
    }
  });
}

function initPago() {
  const inputAdelanto = document.getElementById("montoAdelanto");

  if (!inputAdelanto) return;

  // Mientras escribe
  inputAdelanto.addEventListener("input", (e) => {
    let v = e.target.value;

    // SOLO números y punto
    v = v.replace(/[^\d.]/g, "");

    // Solo un punto decimal
    const parts = v.split(".");
    if (parts.length > 2) {
      v = parts[0] + "." + parts.slice(1).join("");
    }

    // Limitar a 2 decimales
    if (parts[1]?.length > 2) {
      v = parts[0] + "." + parts[1].slice(0, 2);
    }

    e.target.value = v;
  });

  // Al salir del input
  inputAdelanto.addEventListener("blur", () => {
    let value = parseFloat(inputAdelanto.value);
    const { total } = calcularFinanzas();

    // Si está vacío o inválido
    if (isNaN(value)) {
      inputAdelanto.value = "";
      montoAdelanto = 0;
      renderResumenFinanciero(calcularFinanzas());
      return;
    }

    // 🔥 No permitir adelanto mayor al total
    if (value > total) {
      value = total;
    }

    // Redondear a 2 decimales
    value = Number(value.toFixed(2));

    inputAdelanto.value = value.toFixed(2);
    montoAdelanto = value;

    // Actualizar UI
    estadoAdelanto = true;
    renderResumenFinanciero(calcularFinanzas());
  });
}

// ======================= FINANZAS =======================
function calcularFinanzas() {
  const rows = [];
  gridOptions.api.forEachNode((n) => rows.push(n.data));

  const normales = rows.filter((r) => !r.esRegalo);

  const subtotal = normales.reduce(
    (acc, it) => acc + (it.cantidad || 0) * (it.precioUnitario || 0),
    0
  );

  const igv = subtotal * 0.18;
  const total = subtotal;

  console.log("💰 calcularFinanzas:", { subtotal, igv, total });
  return { subtotal, igv, total };
}

function selectCard(card, amountId, showAmount) {
  // 1. Obtener todas las tarjetas del contenedor
  const parent = card.parentElement;
  const cards = parent.querySelectorAll(".p-3.cursor-pointer");

  cards.forEach((c) => {
    c.classList.remove("border-purple-500", "bg-purple-50");
    c.classList.add("border-gray-300");

    // Icono izquierdo (check / circle)
    const leftIcon = c.querySelector("i:first-child");
    if (leftIcon) {
      leftIcon.classList.remove("fa-check-circle", "fas", "text-purple-600");
      leftIcon.classList.add("fa-circle", "far", "text-gray-400");
    }

    // Icono derecho
    const rightIcon = c.querySelector("i:last-child");
    if (rightIcon) {
      rightIcon.classList.remove("text-purple-600");
      rightIcon.classList.add("text-gray-400");
    }
  });

  // 2. Activar tarjeta seleccionada
  card.classList.remove("border-gray-300");
  card.classList.add("border-purple-500", "bg-purple-50");

  const leftIcon = card.querySelector("i:first-child");
  if (leftIcon) {
    leftIcon.classList.remove("fa-circle", "far", "text-gray-400");
    leftIcon.classList.add("fa-check-circle", "fas", "text-purple-600");
  }

  const rightIcon = card.querySelector("i:last-child");
  if (rightIcon) {
    rightIcon.classList.remove("text-gray-400");
    rightIcon.classList.add("text-purple-600");
  }

  // 3. Mostrar u ocultar el monto adelantado
  const amountDiv = document.getElementById(amountId);

  if (showAmount) {
    amountDiv.classList.remove("hidden");
    estadoAdelanto = true;

    // Mantener el valor actual del input
    montoAdelanto = Number(document.getElementById("montoAdelanto").value || 0);
  } else {
    amountDiv.classList.add("hidden");
    document.getElementById("montoAdelanto").value = "0.00";
    estadoAdelanto = false;
    montoAdelanto = 0;
  }

  renderResumenFinanciero(calcularFinanzas());
}

async function cargarSelectsPedido(pedido = {}) {
  // Normalizar lo que viene del backend
  const tipoPagoPedido = (pedido.tipoPago ?? pedido.tipo_pago ?? "")
    .toString()
    .trim()
    .toUpperCase();

  const tipoComprobantePedido = (
    pedido.tipoComprobante ??
    pedido.tipo_comprobante ??
    ""
  )
    .toString()
    .trim()
    .toUpperCase();

  // === Tipo de Pago ===
  const tiposPago = ["TRANSFERENCIA", "YAPE", "EFECTIVO", "PLIN"];
  const selectPago = document.getElementById("pedidoTipoPago");
  if (selectPago) {
    selectPago.innerHTML = `<option value="">Seleccionar tipo</option>`;
    tiposPago.forEach((t) => {
      const opt = document.createElement("option");
      opt.value = t;
      opt.textContent = t;
      if (t === tipoPagoPedido) opt.selected = true;
      selectPago.appendChild(opt);
    });
  }

  // === Tipo de Comprobante ===
  const tiposComprobante = ["BOLETA", "FACTURA"];
  const selectComp = document.getElementById("pedidoComprobante");
  if (selectComp) {
    selectComp.innerHTML = `<option value="">Seleccionar comprobante</option>`;
    tiposComprobante.forEach((t) => {
      const opt = document.createElement("option");
      opt.value = t;
      opt.textContent = t;
      if (t === tipoComprobantePedido) opt.selected = true;
      selectComp.appendChild(opt);
    });
  }
}

// ========= VALIDACIONES =========
function validarCamposObligatorios() {
  let valido = true;
  let primerError = null;

  // === 1. Campos de texto / selects obligatorios ===
  // (OJO: Tipo de comprobante NO va aquí)
  const camposObligatorios = [
    { id: "clienteDni", nombre: "DNI" },
    { id: "clienteNombre", nombre: "Cliente" },
    { id: "clienteTelefono", nombre: "Teléfono" },
    { id: "clienteDireccion", nombre: "Dirección" },
    { id: "clienteCiudad", nombre: "Ciudad del cliente" },
    { id: "clienteProvincia", nombre: "Provincia" },

    { id: "pedidoTipoPago", nombre: "Tipo de Pago" },
    { id: "pedidoCiudad", nombre: "Ciudad del pedido" },
  ];

  camposObligatorios.forEach((c) => {
    const el = document.getElementById(c.id);
    limpiarErrorCampo(el);

    if (!el) return;

    const valor = (el.value || "").trim();
    if (!valor) {
      valido = false;
      marcarErrorCampo(el);
      if (!primerError) primerError = el;
    }
  });

  // === 2. Validar monto adelantado (si corresponde) ===
  if (estadoAdelanto === true) {
    const montoInput = document.getElementById("montoAdelanto");
    limpiarErrorCampo(montoInput);

    // Texto a número con reemplazo de coma a punto
    const valorNum = parseFloat(
      (montoInput.value || "0").toString().replace(",", ".")
    );

    const { total } = calcularFinanzas();

    if (isNaN(valorNum) || valorNum <= 0 || valorNum > total) {
      valido = false;
      marcarErrorCampo(montoInput);
      if (!primerError) primerError = montoInput;
    }
  }

  // === 3. Validar que exista al menos 1 producto en el grid ===
  const rows = [];
  if (gridOptions?.api) {
    gridOptions.api.forEachNode((n) => rows.push(n.data));
  }

  if (rows.length === 0) {
    valido = false;
    // marcamos visualmente la tabla (borde rojo en el contenedor)
    const gridEl = document.getElementById("detallePedidoGrid");
    if (gridEl) {
      gridEl.classList.add("border-2", "border-red-500", "animate-shake");
      setTimeout(() => {
        gridEl.classList.remove("border-red-500", "animate-shake");
      }, 600);
    }
    if (!primerError && gridEl) primerError = gridEl;
  }

  // === 4. Validar evidencia: debe haber al menos una imagen ===
  const evidenciaBox = document.getElementById("evidenciaContainer");
  if (evidenciaBox) {
    evidenciaBox.classList.remove("border-red-500", "animate-shake");
  }

  // 1) imágenes que ya están renderizadas (de BD o nuevas)
  const hayImgEnPreview = !!document.querySelector("#previewComprobante img");

  // 2) nuevas imágenes en esta sesión (si estás usando pedidoActual.evidenciasFiles)
  const hayNuevasEvidencias =
    Array.isArray(pedidoActual.evidenciasFiles) &&
    pedidoActual.evidenciasFiles.length > 0;

  if (!hayImgEnPreview && !hayNuevasEvidencias) {
    valido = false;
    if (evidenciaBox) {
      evidenciaBox.classList.add("border-red-500", "animate-shake");
      if (!primerError) primerError = evidenciaBox;
    }
  }

  // === 5. Si algo está mal, mostramos alerta y hacemos scroll al primer error ===
  if (!valido) {
    Swal.fire({
      icon: "warning",
      title: "Campos incompletos",
      text: "Por favor completa todos los campos obligatorios, agrega al menos un producto y una imagen de comprobante.",
    });

    return false;
  }

  return true;
}

function marcarErrorCampo(el) {
  if (!el) return;
  el.classList.add("border-red-500", "animate-shake");
}

function limpiarErrorCampo(el) {
  if (!el) return;
  el.classList.remove("border-red-500", "animate-shake");
}

// ========= GUARDAR =========
async function onGuardarPedido(esAprobacion = false) {
  console.group("[onGuardarPedido]");

  const { subtotal, igv, total } = calcularFinanzas();
  const subtotalSinIgv = subtotal - igv;

  if (!validarCamposObligatorios()) {
    console.groupEnd();
    return;
  }

  if (estadoAdelanto && Number(montoAdelanto || 0) > total) {
    Swal.fire({
      icon: "error",
      title: "Adelanto inválido",
      text: "El adelanto no puede ser mayor al monto total del pedido.",
    });
    console.groupEnd();
    return;
  }

  try {
    if (!pedidoActual.codPedido) {
      Swal.fire(
        "Sin COD de pedido",
        "No se encontró el COD del pedido.",
        "warning"
      );
      console.groupEnd();
      return;
    }

    // estado del pedido
    const estadoFinal = esAprobacion ? 2 : 1;

    // 1) Cliente
    const clienteActualizado = {
      codCliente: pedidoActual.codCliente,
      dni: document.getElementById("clienteDni").value,
      nombres: document.getElementById("clienteNombre").value,
      telefono: document.getElementById("clienteTelefono").value,
      direccion: document.getElementById("clienteDireccion").value,
      ciudad: document.getElementById("clienteCiudad").value,
      provincia: document.getElementById("clienteProvincia").value,
    };

    // 2) Detalles desde el grid
    const detalles = [];
    const filas = [];
    gridOptions.api.forEachNode((n) => filas.push(n.data));

    filas.forEach((row) => {
      const esRegalo = !!row.esRegalo;
      const precioUnitario = esRegalo ? 0 : row.precioUnitario || 0;
      const precioTotal = esRegalo ? 0 : (row.cantidad || 0) * precioUnitario;

      detalles.push({
        idDetallePedido: row.idDetallePedido || 0,
        codPedido: pedidoActual.codPedido,
        codProducto: row.codProducto || row.producto?.codProducto || null,
        variante: {
          codVariante: row.codVariante || row.variante?.codVariante || null,
        },
        nombreProducto:
          row.nombreProducto ||
          row.producto?.descProducto ||
          row.variante?.titulo ||
          null,
        cantidad: row.cantidad,
        precioUnitario,
        precioTotal,
        esRegalo: esRegalo,
      });
    });

    // 3) Totales (sin IGV / con IGV)
    const subtotalRed = Number(subtotalSinIgv.toFixed(2));
    const igvRed = Number(igv.toFixed(2));
    const totalRed = Number(total.toFixed(2));

    // 4) Archivos nuevos seleccionados en esta sesión
    const evidenciasNuevas =
      Array.isArray(pedidoActual.evidenciasFiles) &&
      pedidoActual.evidenciasFiles.length
        ? pedidoActual.evidenciasFiles
        : [];

    // 5) Metadatos para el DTO: uno por cada archivo nuevo
    const evidenciasPayload = evidenciasNuevas.map(() => ({
      idEvidenciaPedido: null, // nuevo => sin ID
      codPedido: pedidoActual.codPedido,
      url: null,
      motivo: "APROPACION",
    }));

    // Log
    const usuarioSesion = JSON.parse(sessionStorage.getItem("usuario"));
    const idUsuario = usuarioSesion.idUsuario;
    const username = usuarioSesion.usuario;
    const nombreCompleto = `${usuarioSesion.nombre} ${usuarioSesion.apPaterno} ${usuarioSesion.apMaterno}`;
    const motivoGenerado = esAprobacion
      ? `Pedido APROBADO por ${nombreCompleto} (${username}, id=${idUsuario})`
      : `Actualización del pedido por ${nombreCompleto} (${username}, id=${idUsuario})`;

    const logNuevo = {
      idUsuario: idUsuario,
      codPedido: pedidoActual.codPedido,
      idEstadoP: estadoFinal,
      motivoLog: motivoGenerado,
    };

    // 6) Objeto pedido que espera el backend
    const pedido = {
      codPedido: pedidoActual.codPedido,
      documento: document.getElementById("pedidoDocumento")?.value || null,

      estadoPedido: {
        idEstadoPedido: estadoFinal,
      },

      tipoComprobante:
        document.getElementById("pedidoComprobante").value || null,
      tipoPago: document.getElementById("pedidoTipoPago").value || null,
      ciudad: document.getElementById("pedidoCiudad").value || null,
      observacion: document.getElementById("pedidoObservaciones").value || null,

      empresaEntrega: {
        idEmpresaEntrega: 1,
      },

      subtotal: subtotalRed,
      igv: igvRed,
      montoTotal: totalRed,

      adelanto: estadoAdelanto,
      montoAdelanto: Number(montoAdelanto || 0),

      cliente: clienteActualizado,
      detalles,
      evidencias: evidenciasPayload,
      evidenciasEliminar: pedidoActual.evidenciasEliminar || [],
      logNuevo,
    };

    // 7) Armar FormData: JSON + archivos
    const formData = new FormData();
    formData.append(
      "pedido",
      new Blob([JSON.stringify(pedido)], { type: "application/json" })
    );

    evidenciasNuevas.forEach((file) => {
      formData.append("evidencias", file);
    });

    console.log("PAYLOAD ENVIADO:", JSON.stringify(pedido, null, 2));

    // 8) POST al endpoint
    const resp = await fetch(ENDPOINT_GUARDAR, {
      method: "POST",
      body: formData,
    });

    const rawText = await resp.text();
    let data = null;
    try {
      data = rawText ? JSON.parse(rawText) : null;
    } catch {
      data = null;
    }

    console.log("[RESP GUARDAR]", {
      status: resp.status,
      ok: resp.ok,
      rawText,
      data,
    });

    if (!resp.ok) {
      throw new Error(data?.message || rawText || `Error HTTP ${resp.status}`);
    }

    Swal.fire({
      icon: "success",
      title: esAprobacion ? "Pedido aprobado" : "Pedido guardado",
      timer: 2200,
      showConfirmButton: false,
    });

    // limpiar archivos nuevos y lista de eliminaciones
    pedidoActual.evidenciasFiles = [];
    pedidoActual.evidenciasEliminar = [];
    renderPreviewEvidencias();
  } catch (err) {
    console.error("Error al guardar pedido:", err);
    Swal.fire(
      "Error al guardar",
      err.message || "Fallo desconocido en el guardado",
      "error"
    );
  } finally {
    console.groupEnd();
  }
}

// ======================= AUTO EJECUCIÓN =======================
document.addEventListener("DOMContentLoaded", () => {
  console.log("🔥 DetallePedido.js cargado por <script>");
  initDetallePedido();
});
