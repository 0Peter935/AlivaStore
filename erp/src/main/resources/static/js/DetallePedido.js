// DetallePedido.js - versión con logs y guardado robusto
(() => {
  if (window.detallePedido_inicializado) return;
  window.detallePedido_inicializado = true;

  console.log("🧩 Iniciando módulo DetallePedido...");

  let gridApiDetalle = null;
  let productosDisponibles = [];
  let detallesPedido = [];

  // ========= CONFIG =========
  const API_BASE = ""; // deja vacío si sirves en mismo dominio, o usa "http://localhost:8080"
  const ENDPOINT_GUARDAR = "/api/pedidos/guardarPedidoCompleto";
  const ENDPOINT_PEDIDO = (id) => `/api/pedidos/${id}`;
  const ENDPOINT_PRODUCTOS = "/api/productos";

  // ========= ESTADO =========
  let gridOptions = null;
  let productosNormales = [];
  let productosRegalo = [];
  let estadoPago = "completo"; // "completo" | "adelanto"
  let montoAdelanto = 0;
  let pedidoActual = {
    idPedido: null,
    evidenciaFile: null,
  };

  // ========= INIT =========
  window.initDetallePedido = async function () {
    console.group("[initDetallePedido]");
    try {
      const idPedido = Number(localStorage.getItem("pedidoSeleccionado"));
      pedidoActual.idPedido = idPedido || null;

      if (!pedidoActual.idPedido) {
        console.error("No hay idPedido en localStorage.pedidoSeleccionado");
        Swal.fire(
          "Pedido no encontrado",
          "No se encontró el ID del pedido.",
          "warning"
        );
        return;
      }
      console.log("ID del pedido:", pedidoActual.idPedido);

      // Cargar pedido
      const pedido = await fetchJson(
        API_BASE + ENDPOINT_PEDIDO(pedidoActual.idPedido),
        "GET"
      );
      console.log("Pedido cargado:", pedido);

      // Renderizar secciones
      renderCliente(pedido.cliente);
      renderPedido(pedido);
      cargarSelectsPedido(pedido);
      renderEvidencia(pedido);
      renderEstadoPago(pedido);
      renderFinanzasInicial(pedido);

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

      // Evidencia + pago listeners
      initEvidenciaYPago();

      // Botón agregar producto
      document
        .getElementById("btnAgregarProducto")
        ?.addEventListener("click", abrirModalAgregar);

      // Botón guardar
      document
        .getElementById("btnGuardarPedido")
        ?.addEventListener("click", onGuardarPedido);

      // Recalcular finanzas inicial (por si difiere)
      recalcularFinanzas();
    } catch (err) {
      console.error("❌ Error en initDetallePedido:", err);
      Swal.fire("Error", "No se pudo cargar la vista del pedido.", "error");
      document.getElementById("main-content").innerHTML =
        "<p class='text-red-500 text-center text-lg mt-8'>Error al cargar el pedido</p>";
    } finally {
      console.groupEnd();
    }
  };

  // ========= HELPERS HTTP =========
  async function fetchJson(url, method = "GET", body) {
    console.log(`[HTTP] ${method} ${url}`, body ? { body } : "");
    const resp = await fetch(url, {
      method,
      body,
    });
    const text = await resp.text();
    let json;
    try {
      json = text ? JSON.parse(text) : null;
    } catch {
      json = null;
    }
    console.log("[HTTP RESP]", {
      status: resp.status,
      ok: resp.ok,
      text,
      json,
    });
    if (!resp.ok) {
      throw new Error(json?.message || text || `HTTP ${resp.status}`);
    }
    return json ?? {};
  }

  // ========= RENDER SECCIONES =========
  function renderCliente(cliente) {
    const cont = document.getElementById("infoCliente");
    if (!cont || !cliente) return;
    cont.innerHTML = `
      <p><strong>Código:</strong> ${cliente.codigoCliente ?? "-"}</p>
      <p><strong>Nombre:</strong> ${cliente.nombres ?? "-"}</p>
      <p><strong>Teléfono:</strong> ${cliente.telefono ?? "-"}</p>
      <p><strong>Correo:</strong> ${cliente.correo ?? "-"}</p>
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

    if (pedido.tipoPago === "completo") {
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
    estadoPago = pedido.tipoPago || "completo";
    montoAdelanto = Number(pedido.adelanto || 0);
  }

  function renderFinanzasInicial(pedido) {
    const subtotalEl = document.getElementById("subtotalValor");
    const igvEl = document.getElementById("igvValor");
    const adelantoEl = document.getElementById("adelantoValor");
    const totalEl = document.getElementById("totalValor");

    const subtotal = Number(pedido.subtotal || 0).toFixed(2);
    const igv = Number(pedido.igv || 0).toFixed(2);
    const total = Number(pedido.montoTotal || 0).toFixed(2);
    const adelanto = Number(pedido.adelanto || 0);

    subtotalEl.textContent = `S/ ${subtotal}`;
    igvEl.textContent = `S/ ${igv}`;
    totalEl.textContent = `S/ ${total}`;

    if (pedido.tipoPago === "completo") {
      adelantoEl.innerHTML = `<span class="text-green-600 font-semibold">Pagado</span>`;
    } else if (adelanto > 0) {
      adelantoEl.innerHTML = `<span class="text-yellow-600 font-semibold">S/ ${adelanto.toFixed(
        2
      )}</span>`;
    } else {
      adelantoEl.innerHTML = `<span class="text-gray-500 italic">Sin adelanto</span>`;
    }
  }

  // ========= DATOS PEDIDO =========
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
    const tiposComprobante = ["Boleta", "Factura"];
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

  // ========= AG GRID =========
  function initGridProductos(detalles) {
    const columnDefs = [
      { headerName: "Código", field: "producto.codProducto", flex: 1 },
      { headerName: "Descripción", field: "producto.descProducto", flex: 2 },
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
          setTimeout(recalcularFinanzas, 0);
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
          setTimeout(recalcularFinanzas, 0);
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
      {
        headerName: "Acción",
        width: 130,
        cellRenderer: () => `
          <button class="bg-red-500 hover:bg-red-600 text-white px-2 py-1 rounded text-xs">
            <i class="fa-solid fa-trash"></i> Eliminar
          </button>
        `,
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
          eliminarProducto(event.node);
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

  function eliminarProducto(node) {
    Swal.fire({
      title: "¿Eliminar producto?",
      text: "Esta acción no se puede deshacer",
      icon: "warning",
      showCancelButton: true,
      confirmButtonText: "Sí, eliminar",
      cancelButtonText: "Cancelar",
    }).then((r) => {
      if (r.isConfirmed && gridOptions?.api) {
        gridOptions.api.applyTransaction({ remove: [node.data] });
        recalcularFinanzas();
        Swal.fire("Eliminado", "El producto fue eliminado", "success");
      }
    });
  }

  // ========= MODAL AGREGAR =========
  async function cargarProductos() {
    console.group("[cargarProductos]");
    try {
      const data = await fetchJson(API_BASE + ENDPOINT_PRODUCTOS, "GET");
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

  function abrirModalAgregar() {
    const modal = document.getElementById("modalAgregarProducto");
    const toggle = document.getElementById("toggleRegalo");
    const toggleCircle = document.getElementById("toggleCircle");
    const toggleSwitch = document.getElementById("toggleSwitch");
    const select = document.getElementById("selectProducto");
    const inputCantidad = document.getElementById("inputCantidad");

    if (!productosNormales.length && !productosRegalo.length) {
      Swal.fire(
        "Cargando productos",
        "Vuelve a intentar en un momento.",
        "info"
      );
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

      recalcularFinanzas();

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

  // ========= EVIDENCIA + ESTADO DE PAGO =========
  function initEvidenciaYPago() {
    const inputComprobante = document.getElementById("inputComprobante");
    const preview = document.getElementById("previewComprobante");
    const inputAdelanto = document.getElementById("montoAdelanto");

    // Vista previa
    inputComprobante?.addEventListener("change", (e) => {
      const file = e.target.files?.[0];
      pedidoActual.evidenciaFile = file || null;
      if (file) {
        const reader = new FileReader();
        reader.onload = (ev) => {
          // 🔹 Mantener el mismo diseño siempre
          preview.innerHTML = `
    <div class="flex flex-col items-center justify-center mx-auto my-3 p-3 rounded-lg shadow-md border border-gray-300 bg-white transition-transform duration-300 group hover:scale-105">
      <div class="relative inline-block">
        <img src="${ev.target.result}" alt="Comprobante" 
             class="max-h-64 w-auto rounded-md shadow-sm object-contain mx-auto" />
        <div class="absolute inset-0 bg-black bg-opacity-50 opacity-0 group-hover:opacity-100 flex flex-col items-center justify-center transition-opacity duration-300 cursor-pointer"
             onclick="document.getElementById('inputComprobante').click()">
          <i class="fas fa-sync-alt text-white text-3xl mb-2 animate-spin-slow"></i>
          <p class="text-white font-semibold">Cambiar evidencia</p>
        </div>
      </div>
      <p class="text-sm text-gray-500 text-center mt-2">${file.name}</p>
    </div>
  `;
        };

        reader.readAsDataURL(file);
      } else {
        preview.innerHTML = "Ningún archivo seleccionado";
      }
    });

    // Tarjetas de pago vienen por onclick="selectCard..."
    // Escucha cambios en el monto de adelanto
    inputAdelanto?.addEventListener("input", () => {
      estadoPago = "adelanto";
      montoAdelanto = Number(inputAdelanto.value) || 0;
      recalcularFinanzas();
    });
  }

  // Usada por el HTML (onclick en las cards)
  window.selectCard = function (card, amountId, showAmount) {
    const parent = card.parentElement;
    const cards = parent.querySelectorAll("div[onclick]");
    cards.forEach((c) => {
      c.classList.remove("border-purple-500", "bg-purple-50");
      c.classList.add("border-gray-300");
      const icon = c.querySelector("i.fa-check-circle, i.far");
      if (icon) {
        icon.classList.remove("fa-check-circle", "fas", "text-purple-600");
        icon.classList.add("fa-circle", "far", "text-gray-400");
      }
      const rightIcon = c.querySelectorAll("i")[1];
      if (rightIcon) {
        rightIcon.classList.remove("text-purple-600");
        rightIcon.classList.add("text-gray-400");
      }
    });

    card.classList.remove("border-gray-300");
    card.classList.add("border-purple-500", "bg-purple-50");

    const amountDiv = document.getElementById(amountId);
    if (showAmount) {
      amountDiv.classList.remove("hidden");
      estadoPago = "adelanto";
      montoAdelanto =
        Number(document.getElementById("montoAdelanto").value) || 0;
    } else {
      amountDiv.classList.add("hidden");
      document.getElementById("montoAdelanto").value = "0.00";
      estadoPago = "completo";
      montoAdelanto = 0;
    }
    recalcularFinanzas();
  };

  // ========= FINANZAS =========
  function recalcularFinanzas() {
    if (!gridOptions?.api) return;

    const rows = [];
    gridOptions.api.forEachNode((n) => rows.push(n.data));
    const itemsNormales = rows.filter((r) => !r.producto?.regalo);

    const subtotal = itemsNormales.reduce(
      (acc, it) => acc + (it.cantidad || 0) * (it.precioUnitario || 0),
      0
    );
    const igv = subtotal * 0.18;
    const total = subtotal + igv;

    renderResumenFinanciero({ subtotal, igv, total });
  }

  function renderResumenFinanciero({ subtotal, igv, total }) {
    const cont = document.getElementById("infoFinanciera");
    if (!cont) return;

    const adelantoHtml =
      estadoPago === "adelanto"
        ? `S/ ${Number(montoAdelanto || 0).toFixed(2)}`
        : `<span class='text-green-600 font-semibold'>Pagado</span>`;

    cont.innerHTML = `
      <p><strong>Subtotal:</strong> S/ ${Number(subtotal).toFixed(2)}</p>
      <p><strong>IGV:</strong> S/ ${Number(igv).toFixed(2)}</p>
      <p><strong>Adelanto:</strong> ${adelantoHtml}</p>
      <p><strong>Total:</strong> <span class="font-bold text-green-600">S/ ${Number(
        total
      ).toFixed(2)}</span></p>
    `;
  }

  // ========= GUARDAR =========
  async function onGuardarPedido() {
    console.group("[onGuardarPedido]");
    try {
      if (!pedidoActual.idPedido) {
        Swal.fire(
          "Sin ID de pedido",
          "No se encontró el ID del pedido.",
          "warning"
        );
        return;
      }

      // 🔹 Recolectar filas actuales desde el grid
      const detalles = [];
      gridOptions.api.forEachNode((n) => {
        const esRegalo = !!n.data.producto.regalo;

        // Si es regalo, el precio siempre es 0
        const precioUnitario = esRegalo ? 0 : n.data.precioUnitario || 0;
        const precioTotal = esRegalo
          ? 0
          : (n.data.cantidad || 0) * precioUnitario;

        detalles.push({
          producto: {
            idProducto: n.data.producto.idProducto,
            regalo: esRegalo,
          },
          cantidad: n.data.cantidad,
          precioUnitario,
          precioTotal,
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

      // 🔹 Determinar adelanto final según estado de pago
      const isPagoCompleto = estadoPago === "completo";
      const adelantoFinal = isPagoCompleto
        ? Number(total.toFixed(2))
        : Number(montoAdelanto || 0);

      // 🔹 Construir objeto pedido
      const pedido = {
        idPedido: pedidoActual.idPedido,
        documento: document.getElementById("pedidoDocumento").value || null,
        tipoComprobante:
          document.getElementById("pedidoComprobante").value || null,
        tipoPago: document.getElementById("pedidoTipoPago").value || null,
        ciudad: document.getElementById("pedidoCiudad").value || null,
        empresaEntrega: {
          idEmpresaEntrega:
            Number(document.getElementById("pedidoEmpresaEntrega").value) ||
            null,
        },

        subtotal: Number(subtotal.toFixed(2)),
        igv: Number(igv.toFixed(2)),
        montoTotal: Number(total.toFixed(2)),
        adelanto: adelantoFinal,
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
      const url = API_BASE + ENDPOINT_GUARDAR;
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
      Swal.fire(
        "Error al guardar",
        err.message || "Fallo desconocido",
        "error"
      );
    } finally {
      console.groupEnd();
    }
  }

  document.addEventListener("DOMContentLoaded", () => {
    if (window.initDetallePedido) {
      console.log("🚀 Ejecutando initDetallePedido automáticamente...");
      window.initDetallePedido();
    }
  });
})();
