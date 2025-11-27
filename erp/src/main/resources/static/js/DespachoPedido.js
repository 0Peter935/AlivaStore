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

let pedidoActual = {
  codPedido: null,
  evidenciaFile: null,
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

  cargarSelectsPedido(pedido);
  initEvidencia();
  initPago();

  // Cargar productos (catálogo)
  await cargarProductos();

  // Normalizar detalles: regalo -> booleano
  const detalles = (pedido.detalles || []).map((d) => ({
    ...d,
    producto: {
      ...d.producto,
      regalo: d.producto?.regalo === true || d.producto?.regalo === 1,
    },
  }));
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

// ======================= RENDER =======================
function renderCliente(cliente) {
  const cont = document.getElementById("infoCliente");
  if (!cont || !cliente) return;
  cont.innerHTML = `
      <p><strong>DNI:</strong> ${cliente.dni ?? "-"}</p>
      <p><strong>Nombre:</strong> ${cliente.nombres ?? "-"}</p>
      <p><strong>Teléfono:</strong> ${cliente.telefono ?? "-"}</p>
      <p><strong>Correo:</strong> ${
        cliente.direccion + ", " + cliente.ciudad + ", " + cliente.provincia ??
        ""
      }</p>
    `;
}

function renderPedido(pedido) {
  document.getElementById("pedidoDocumento").value = pedido.documento || "";
  document.getElementById("pedidoComprobante").value =
    pedido.tipoComprobante || "";
  document.getElementById("pedidoCiudad").value = pedido.ciudad || "";
  document.getElementById("pedidoEmpresaEntrega").value =
    pedido.empresaEntrega?.razonSocial || "";
}

function renderEvidencia(pedido) {
  const preview = document.getElementById("previewComprobante");
  const inputFile = document.getElementById("inputComprobante");
  if (!preview || !inputFile) return;

  preview.innerHTML = "";

  if (
    pedido.evidencia &&
    pedido.evidencia.trim() !== "" &&
    pedido.evidencia.includes(".")
  ) {
    // Contenedor principal centrado
    const wrapper = document.createElement("div");
    wrapper.className = `
      flex flex-col items-center justify-center 
      mx-auto my-3 p-3 rounded-lg shadow-md border border-gray-300 
      bg-white transition-transform duration-300 group hover:scale-105
    `;

    // Imagen centrada
    const img = document.createElement("img");
    img.src = `/recursos/img/evidencia/${pedido.evidencia}`;
    img.alt = "Comprobante";
    img.className = `
      max-h-64 w-auto rounded-md shadow-sm 
      object-contain mx-auto
    `;
    img.onerror = () => {
      wrapper.innerHTML = `
        <div class="flex flex-col items-center justify-center h-40 cursor-pointer text-gray-400 hover:text-purple-600"
             onclick="document.getElementById('inputComprobante').click()">
          <i class="fas fa-cloud-upload-alt text-3xl mb-2"></i>
          <p class="text-sm font-medium">Sube una imagen de comprobante</p>
        </div>`;
    };

    // 🔹 Nombre del archivo debajo
    const fileName = document.createElement("p");
    fileName.className = "text-sm text-gray-500 text-center mt-2";
    fileName.textContent = pedido.evidencia;

    // 🔹 Overlay opcional para cambiar evidencia (solo si ya está implementado)
    const overlay = document.createElement("div");
    overlay.className =
      "absolute inset-0 bg-black bg-opacity-50 opacity-0 group-hover:opacity-100 flex flex-col items-center justify-center transition-opacity duration-300";
    overlay.innerHTML = `
      <i class="fas fa-sync-alt text-white text-3xl mb-2 animate-spin-slow"></i>
      <p class="text-white font-semibold">Cambiar evidencia</p>
    `;
    overlay.onclick = () => inputFile.click();

    // 🔹 Estructura final
    const overlayContainer = document.createElement("div");
    overlayContainer.className = "relative inline-block";
    overlayContainer.appendChild(img);
    overlayContainer.appendChild(overlay);

    wrapper.appendChild(overlayContainer);
    wrapper.appendChild(fileName);
    preview.appendChild(wrapper);
  } else {
    // 🔹 Si no hay evidencia
    preview.innerHTML = `
      <div class="flex flex-col items-center justify-center h-40 border-2 border-dashed border-gray-300 rounded-lg text-gray-400 cursor-pointer hover:text-purple-600 hover:border-purple-400 transition"
           onclick="document.getElementById('inputComprobante').click()">
        <i class="fas fa-cloud-upload-alt text-4xl mb-2"></i>
        <p class="text-sm font-medium">Sube una imagen de comprobante</p>
      </div>
    `;
  }
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
    const icon = card.querySelector("i.fa-check-circle");
    if (icon) {
      icon.classList.remove("fa-check-circle", "fas", "text-purple-600");
      icon.classList.add("fa-circle", "far", "text-gray-400");
    }
  });

  if (pedido.adelanto === false) {
    // Marcar pago completo
    pagoCompletoCard.classList.add("border-purple-500", "bg-purple-50");
    const icon = pagoCompletoCard.querySelector("i.far.fa-circle");
    if (icon) {
      icon.classList.remove("fa-circle", "far", "text-gray-400");
      icon.classList.add("fa-check-circle", "fas", "text-purple-600");
    }
    adelantoContainer.classList.add("hidden");
    inputAdelanto.value = "0.00";
  } else {
    // Marcar pago con adelanto
    pagoAdelantoCard.classList.add("border-purple-500", "bg-purple-50");
    const icon = pagoAdelantoCard.querySelector("i.far.fa-circle");
    if (icon) {
      icon.classList.remove("fa-circle", "far", "text-gray-400");
      icon.classList.add("fa-check-circle", "fas", "text-purple-600");
    }
    adelantoContainer.classList.remove("hidden");
    inputAdelanto.value = Number(pedido.adelanto || 0).toFixed(2);
  }

  // Sincroniza variables globales
  estadoAdelanto = pedido.adelanto;
  montoAdelanto = Number(pedido.montoAdelanto || 0);
}

function renderNotas(notas = []) {
  const cont = document.getElementById("listaNotas");
  if (!cont) return;

  if (notas.length === 0) {
    cont.innerHTML = `<p class="text-gray-500 italic">Sin notas registradas</p>`;
    return;
  }

  cont.innerHTML = notas
    .map(
      (n) => `
      <div class="border border-gray-300 rounded-lg p-3 bg-gray-50">
        <p class="text-xs text-gray-500 mb-1">
          <strong>ID #${n.idNotaPedido}</strong>
        </p>
        <p><strong>${n.titulo}:</strong></p>
        <p class="text-gray-700">${n.descripcion}</p>
      </div>
    `
    )
    .join("");
}

function renderResumenFinanciero({ subtotal, igv, total, adelanto }) {
  const subtotalEl = document.getElementById("subtotalValor");
  const igvEl = document.getElementById("igvValor");
  const adelantoEl = document.getElementById("adelantoValor");
  const totalEl = document.getElementById("totalValor");

  subtotalEl.textContent = `S/ ${subtotal.toFixed(2)}`;
  igvEl.textContent = `S/ ${igv.toFixed(2)}`;
  totalEl.textContent = `S/ ${total.toFixed(2)}`;

  if (estadoAdelanto === false) {
    adelantoEl.innerHTML = `<span class="text-green-600 font-semibold">Pagado</span>`;
  } else if (adelanto > 0) {
    adelantoEl.innerHTML = `<span class="text-yellow-600 font-semibold">S/ ${adelanto.toFixed(
      2
    )}</span>`;
  } else {
    adelantoEl.innerHTML = `<span class="text-gray-500 italic">Sin adelanto</span>`;
  }
}

// ======================= GRID =======================
function initGridProductos(detalles) {
  const columnDefs = [
    {
      headerName: "N°",
      valueGetter: (params) => params.node.rowIndex + 1,
      width: 80,
      cellClass: "text-center",
    },
    { headerName: "Descripción", field: "nombreProducto", flex: 2 },
    {
      headerName: "Cantidad",
      field: "cantidad",
      editable: (p) => !p.data.producto?.regalo,
      width: 120,
      cellClass: "text-center",
      valueSetter: (p) => {
        const v = Number(p.newValue);
        if (Number.isNaN(v) || v < 1) return false;
        p.data.cantidad = v;
        p.data.precioTotal = (p.data.precioUnitario || 0) * v;
        renderResumenFinanciero(calcularFinanzas());
        return true;
      },
    },
    {
      headerName: "Precio Unitario",
      field: "precioUnitario",
      editable: (p) => !p.data.producto?.regalo,
      width: 150,
      cellRenderer: (p) =>
        p.data.producto?.regalo
          ? `<span class="text-gray-400">----</span>`
          : `S/ ${(p.value ?? 0).toFixed(2)}`,
      valueSetter: (p) => {
        const v = Number(p.newValue);
        if (Number.isNaN(v) || v < 0) return false;
        p.data.precioUnitario = v;
        p.data.precioTotal = v * (p.data.cantidad || 0);
        renderResumenFinanciero(calcularFinanzas());
        return true;
      },
    },
    {
      headerName: "Subtotal",
      valueGetter: (p) =>
        p.data.producto?.regalo
          ? null
          : (p.data.cantidad || 0) * (p.data.precioUnitario || 0),
      width: 150,
      cellRenderer: (p) =>
        p.data.producto?.regalo
          ? `<span class="text-gray-400">----</span>`
          : `S/ ${(p.value ?? 0).toFixed(2)}`,
    },
  ];

  gridOptions = {
    columnDefs,
    rowData: detalles,
    domLayout: "autoHeight",
    defaultColDef: { resizable: true, sortable: true },
    onCellClicked: (event) => {
      if (
        event.colDef.headerName === "Acción" &&
        event.event.target.closest("button")
      ) {
        eliminarRegalo(event.node);
      }
    },
    getRowStyle: (params) => {
      if (params.data?.producto?.regalo)
        return { backgroundColor: "#676ce940" }; // celeste
      return null;
    },
  };

  const gridEl = document.querySelector("#detallePedidoGrid");
  new agGrid.Grid(gridEl, gridOptions);
  console.log("AG Grid inicializado con", detalles.length, "filas");
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
  const select = document.getElementById("selectProducto");
  const inputCantidad = document.getElementById("inputCantidad");

  if (!productosNormales.length && !productosRegalo.length) {
    Swal.fire("Cargando productos", "Vuelve a intentar en un momento.", "info");
    return;
  }

  const llenarSelect = (esRegalo) => {
    select.innerHTML = `<option value="">Seleccione un producto</option>`;
    const lista = esRegalo ? productosRegalo : productosNormales;
    lista.forEach((p) => {
      const opt = document.createElement("option");
      opt.value = p.idProducto;
      opt.textContent = `${p.codProducto} - ${p.descProducto}`;
      select.appendChild(opt);
    });
  };

  toggle.onchange = () => {
    const esRegalo = toggle.checked;
    toggleCircle.style.transform = esRegalo
      ? "translateX(20px)"
      : "translateX(0px)";
    toggleSwitch.style.backgroundColor = esRegalo ? "#3b82f6" : "#d1d5db";
    inputCantidad.value = 1;
    llenarSelect(esRegalo);

    const lista = esRegalo ? productosRegalo : productosNormales;
    renderCustomSelect(lista);
  };

  // Estado inicial
  toggle.checked = false;
  toggle.onchange();
  modal.classList.remove("hidden");
  modal.classList.add("flex");

  document.getElementById("btnCancelarAgregar").onclick = () => {
    modal.classList.add("hidden");
    modal.classList.remove("flex");
  };

  document.getElementById("btnConfirmarAgregar").onclick = () => {
    const idProducto = Number(select.value);
    const cantidad = Number(inputCantidad.value) || 1;
    const esRegalo = toggle.checked;

    if (!idProducto) {
      Swal.fire("Selecciona un producto", "", "warning");
      return;
    }

    const prod = (esRegalo ? productosRegalo : productosNormales).find(
      (p) => p.idProducto === idProducto
    );
    if (!prod) {
      Swal.fire("Producto no encontrado", "", "error");
      return;
    }

    // Unificar si ya existe
    const filas = [];
    gridOptions.api.forEachNode((n) => filas.push(n.data));
    const existente = filas.find(
      (r) =>
        r.producto.idProducto === idProducto &&
        !!r.producto.regalo === !!esRegalo
    );
    if (existente) {
      existente.cantidad += cantidad;
      existente.precioTotal =
        existente.cantidad * (existente.precioUnitario || 0);
      gridOptions.api.applyTransaction({ update: [existente] });
    } else {
      gridOptions.api.applyTransaction({
        add: [
          {
            producto: prod,
            cantidad,
            precioUnitario: prod.precio || 0,
            precioTotal: (prod.precio || 0) * cantidad,
          },
        ],
      });
    }

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
}

// ======================= EVIDENCIA + PAGO =======================
function initEvidencia() {
  const inputComprobante = document.getElementById("inputComprobante");
  const preview = document.getElementById("previewComprobante");

  inputComprobante?.addEventListener("change", (e) => {
    const file = e.target.files?.[0];
    pedidoActual.evidenciaFile = file || null;

    if (!file) {
      preview.innerHTML = "Ningún archivo seleccionado";
      return;
    }

    const reader = new FileReader();
    reader.onload = (ev) => {
      preview.innerHTML = `
        <div class="flex flex-col items-center justify-center mx-auto my-3 p-3 rounded-lg shadow-md border border-gray-300 bg-white group hover:scale-105 transition">
          <div class="relative inline-block">
            <img src="${ev.target.result}" class="max-h-64 w-auto rounded-md shadow-sm object-contain mx-auto" />
            <div class="absolute inset-0 bg-black bg-opacity-50 opacity-0 group-hover:opacity-100 flex items-center justify-center cursor-pointer transition"
                 onclick="document.getElementById('inputComprobante').click()">
              <i class="fas fa-sync-alt text-white text-3xl mb-2"></i>
            </div>
          </div>
          <p class="text-sm text-gray-500 mt-2">${file.name}</p>
        </div>
      `;
    };

    reader.readAsDataURL(file);
  });
}

function initPago() {
  const inputAdelanto = document.getElementById("montoAdelanto");

  inputAdelanto?.addEventListener("input", () => {
    const { total } = calcularFinanzas();
    let valor = Number(inputAdelanto.value) || 0;

    // 🔒 No permitir que coloque más del total
    if (valor > total) {
      valor = total;
      inputAdelanto.value = total.toFixed(2); // reajusta automáticamente
    }

    estadoAdelanto = true;
    montoAdelanto = valor;

    renderResumenFinanciero(calcularFinanzas());
  });
}

// ======================= FINANZAS =======================
function calcularFinanzas() {
  const rows = [];
  gridOptions.api.forEachNode((n) => rows.push(n.data));

  const normales = rows.filter((r) => !r.producto?.regalo);

  const subtotal = normales.reduce(
    (acc, it) => acc + (it.cantidad || 0) * (it.precioUnitario || 0),
    0
  );

  const igv = subtotal * 0.18;
  const total = subtotal;

  const adelanto =
    estadoAdelanto === false ? total : Number(montoAdelanto || 0);

  console.log("💰 calcularFinanzas:", { subtotal, igv, total, adelanto });
  return { subtotal, igv, total, adelanto };
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
  // === Empresa Entrega ===
  try {
    const resEmp = await fetch("/api/empresas-entrega");
    const empresas = await resEmp.json();

    const selectEmp = document.getElementById("pedidoEmpresaEntrega");
    selectEmp.innerHTML = `<option value="">Seleccionar empresa</option>`;
    empresas.forEach((e) => {
      const opt = document.createElement("option");
      opt.value = e.idEmpresaEntrega;
      opt.textContent = e.razonSocial;
      if (pedido.empresaEntrega?.idEmpresaEntrega === e.idEmpresaEntrega)
        opt.selected = true;
      selectEmp.appendChild(opt);
    });
  } catch (err) {
    console.error("Error al cargar empresas de entrega:", err);
  }

  // === Tipo de Pago ===
  const tiposPago = ["Transferencia", "Yape", "Efectivo", "Plin"];
  const selectPago = document.getElementById("pedidoTipoPago");
  selectPago.innerHTML = `<option value="">Seleccionar tipo</option>`;
  tiposPago.forEach((t) => {
    const opt = document.createElement("option");
    opt.value = t;
    opt.textContent = t;
    if (pedido.tipoPago === t) opt.selected = true;
    selectPago.appendChild(opt);
  });

  // === Tipo de Comprobante ===
  const tiposComprobante = ["BOLETA", "FACTURA"];
  const selectComp = document.getElementById("pedidoComprobante");
  selectComp.innerHTML = `<option value="">Seleccionar comprobante</option>`;
  tiposComprobante.forEach((t) => {
    const opt = document.createElement("option");
    opt.value = t;
    opt.textContent = t;
    if (pedido.tipoComprobante === t) opt.selected = true;
    selectComp.appendChild(opt);
  });
}

// ========= VALIDACIONES =========
function validarCamposObligatorios() {
  let valido = true;
  let primerError = null;

  // === Lista de campos obligatorios ===
  const campos = [
    { id: "pedidoComprobante", nombre: "Tipo de Comprobante" },
    { id: "pedidoTipoPago", nombre: "Tipo de Pago" },
    { id: "pedidoCiudad", nombre: "Ciudad" },
    { id: "pedidoEmpresaEntrega", nombre: "Empresa de Entrega" },
  ];

  campos.forEach((c) => {
    const el = document.getElementById(c.id);
    el.classList.remove("border-red-500", "animate-shake");

    if (!el.value || el.value.trim() === "") {
      valido = false;
      el.classList.add("border-red-500", "animate-shake");
      if (!primerError) primerError = el;
    }
  });

  // === Validar monto adelantado ===
  if (estadoAdelanto === true) {
    const monto = document.getElementById("montoAdelanto");
    monto.classList.remove("border-red-500", "animate-shake");

    if (Number(monto.value) <= 0) {
      valido = false;
      monto.classList.add("border-red-500", "animate-shake");
      if (!primerError) primerError = monto;
    }
  }

  // === Validar productos ===
  const rows = [];
  gridOptions.api.forEachNode((n) => rows.push(n.data));

  if (rows.length === 0) {
    Swal.fire("Pedido vacío", "Debe agregar al menos un producto.", "warning");
    return false;
  }

  // === Validar evidencia ===
  const evidenciaNueva = pedidoActual.evidenciaFile;
  const evidenciaPrevia = document
    .getElementById("previewComprobante")
    ?.querySelector("img");

  const evidenciaBox = document.getElementById("evidenciaContainer");
  evidenciaBox.classList.remove("border-red-500", "animate-shake");

  if (!evidenciaNueva && !evidenciaPrevia) {
    valido = false;
    evidenciaBox.classList.add("border-red-500", "animate-shake");
    if (!primerError) primerError = evidenciaBox;
  }

  // === Mostrar alerta si algo falta ===
  if (!valido) {
    Swal.fire({
      icon: "warning",
      title: "Campos incompletos",
      text: "Por favor completa todos los campos obligatorios.",
    });

    if (primerError) {
      primerError.scrollIntoView({ behavior: "smooth", block: "center" });
    }

    return false;
  }

  return true;
}

// ========= GUARDAR =========
async function onGuardarPedido() {
  console.group("[onGuardarPedido]");

  const { total } = calcularFinanzas();

  if (!validarCamposObligatorios()) return;
  if (estadoAdelanto && montoAdelanto > total) {
    Swal.fire({
      icon: "error",
      title: "Adelanto inválido",
      text: "El adelanto no puede ser mayor al total del pedido.",
    });
    return;
  }

  try {
    if (!pedidoActual.codPedido) {
      Swal.fire(
        "Sin COD de pedido",
        "No se encontró el COD del pedido.",
        "warning"
      );
      return;
    }

    // 🔹 Recolectar filas actuales desde el grid
    const detalles = [];
    gridOptions.api.forEachNode((n) => {
      const row = n.data;
      const esRegalo = !!row.producto?.regalo;

      const precioUnitario = esRegalo ? 0 : row.precioUnitario || 0;
      const precioTotal = esRegalo ? 0 : (row.cantidad || 0) * precioUnitario;

      detalles.push({
        idDetallePedido: row.idDetallePedido || 0,
        codPedido: pedidoActual.codPedido,

        // 🔥 ESTOS 3 SON LOS NUEVOS CAMPOS IMPORTANTES
        codProducto: row.codProducto || null,
        codVariante: row.codVariante || null,
        nombreProducto: row.nombreProducto || null,

        cantidad: row.cantidad,
        precioUnitario,
        precioTotal,
        regalo: esRegalo,
      });
    });

    // 🔹 Calcular totales reales (solo productos normales)
    const itemsNormales = detalles.filter((r) => !r.producto?.regalo);
    const subtotal = itemsNormales.reduce(
      (acc, it) => acc + (it.cantidad || 0) * (it.precioUnitario || 0),
      0
    );
    const igv = subtotal * 0.18;
    const total = subtotal + igv;

    // 🔹 Construir objeto pedido
    const pedido = {
      codPedido: pedidoActual.codPedido,
      documento: document.getElementById("pedidoDocumento").value || null,
      estadoPedido: {
        idEstadoPedido: 2, // Mantener el estado actual (no se edita aquí)
      },
      tipoComprobante:
        document.getElementById("pedidoComprobante").value || null,
      tipoPago: document.getElementById("pedidoTipoPago").value || null,
      ciudad: document.getElementById("pedidoCiudad").value || null,
      empresaEntrega: {
        idEmpresaEntrega:
          Number(document.getElementById("pedidoEmpresaEntrega").value) || null,
      },

      subtotal: Number(subtotal.toFixed(2)),
      igv: Number(igv.toFixed(2)),
      montoTotal: Number(total.toFixed(2)),
      adelanto: estadoAdelanto,
      montoAdelanto: montoAdelanto,

      detalles,
    };

    console.log("Pedido a enviar:", pedido);

    // 🔹 Construir FormData (pedido + evidencia opcional)
    const formData = new FormData();
    formData.append(
      "pedido",
      new Blob([JSON.stringify(pedido)], { type: "application/json" })
    );
    if (pedidoActual.evidenciaFile) {
      formData.append("file", pedidoActual.evidenciaFile);
      console.log("Evidencia adjunta:", pedidoActual.evidenciaFile.name);
    } else {
      console.log("Sin evidencia (file no adjunto)");
    }

    // 🔹 Envío al backend
    const url = ENDPOINT_GUARDAR;
    console.log("[POST] Enviando a:", url);
    const resp = await fetch(url, { method: "POST", body: formData });

    const rawText = await resp.text();
    let data = null;
    try {
      data = rawText ? JSON.parse(rawText) : null;
    } catch {}
    console.log("[RESP GUARDAR]", {
      status: resp.status,
      ok: resp.ok,
      rawText,
      data,
    });

    if (!resp.ok)
      throw new Error(data?.message || rawText || `HTTP ${resp.status}`);

    Swal.fire({
      icon: "success",
      title: "Pedido guardado",
      text: "Los cambios fueron registrados correctamente.",
      timer: 2200,
      showConfirmButton: false,
    });
  } catch (err) {
    console.error("❌ Error al guardar pedido:", err);
    Swal.fire("Error al guardar", err.message || "Fallo desconocido", "error");
  } finally {
    console.groupEnd();
  }
}

// ======================= AUTO EJECUCIÓN =======================
document.addEventListener("DOMContentLoaded", () => {
  console.log("🔥 DetallePedido.js cargado por <script>");
  initDetallePedido();
});
