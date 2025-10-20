(() => {
  // Evita ejecución duplicada si ya fue cargado
  if (window.detallePedidoInicializado) return;
  window.detallePedidoInicializado = true;

  window.initDetallePedido = async function () {
    const idPedido = localStorage.getItem("pedidoSeleccionado");
    if (!idPedido) {
      Swal.fire({
        icon: "warning",
        title: "Pedido no encontrado",
        text: "No se encontró el ID del pedido seleccionado.",
      });
      return;
    }

    try {
      const resp = await fetch(`/api/pedidos/${idPedido}`);
      if (!resp.ok) throw new Error("Error al obtener pedido");

      const pedido = await resp.json();

      renderCliente(pedido.cliente);
      renderPedido(pedido);
      renderFinanzas(pedido);

      cargarProductos();

      // Normaliza los valores de regalo
      const detalles = (pedido.detalles || []).map((d) => ({
        ...d,
        producto: {
          ...d.producto,
          regalo: d.producto?.regalo === true || d.producto?.regalo === 1,
        },
      }));

      initGridProductos(detalles);
      evidenciasDePedido();
      recalcularFinanzas();

      // Evento para agregar productos
      document
        .getElementById("btnAgregarProducto")
        .addEventListener("click", agregarNuevoProducto);
    } catch (err) {
      console.error("❌ Error al cargar pedido:", err);
      document.getElementById("main-content").innerHTML =
        "<p class='text-red-500 text-center text-lg mt-8'>Error al cargar el pedido</p>";
    }
  };

  /* ===================== SECCIONES ===================== */

  window.renderCliente = function (cliente) {
    if (!cliente) return;
    document.getElementById("infoCliente").innerHTML = `
    <p><strong>Nombre:</strong> ${cliente.nombres}</p>
    <p><strong>Apellidos / Teléfono:</strong> ${cliente.apellidos}</p>
    <p><strong>Correo:</strong> ${cliente.correo ?? "-"}</p>
    <p><strong>Documento:</strong> ${cliente.documento ?? "-"}</p>
  `;
  };

  window.renderPedido = function (pedido) {
    document.getElementById("infoPedido").innerHTML = `
    <p><strong>Documento:</strong> ${pedido.documento}</p>
    <p><strong>Tipo Pago:</strong> ${pedido.tipoPago}</p>
    <p><strong>Comprobante:</strong> ${pedido.tipoComprobante}</p>
    <p><strong>Ciudad:</strong> ${pedido.ciudad}</p>
    <p><strong>Usuario:</strong> ${pedido.usuario?.usuario}</p>
    <p><strong>Empresa Entrega:</strong> ${pedido.empresaEntrega?.razonSocial}</p>
  `;
  };

  window.renderFinanzas = function (pedido) {
    document.getElementById("infoFinanciera").innerHTML = `
    <p><strong>Subtotal:</strong> S/ ${pedido.subtotal.toFixed(2)}</p>
    <p><strong>IGV:</strong> S/ ${pedido.igv.toFixed(2)}</p>
    <p><strong>Adelanto:</strong> S/ ${pedido.adelanto.toFixed(2)}</p>
    <p><strong>Total:</strong> <span class="font-bold text-green-600">S/ ${pedido.montoTotal.toFixed(
      2
    )}</span></p>
  `;
  };

  /* ===================== AG GRID - PRODUCTOS ===================== */

  let gridOptions;

  window.initGridProductos = function (detalles) {
    const columnDefs = [
      { headerName: "Código", field: "producto.codProducto", flex: 1 },
      { headerName: "Descripción", field: "producto.descProducto", flex: 2 },
      {
        headerName: "Cantidad",
        field: "cantidad",
        editable: (p) => !p.data.producto?.regalo,
        width: 120,
        cellClass: "text-center",
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
      },
      {
        headerName: "Subtotal",
        valueGetter: (p) =>
          p.data.producto?.regalo
            ? null
            : p.data.cantidad * (p.data.precioUnitario || 0),
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
          return { backgroundColor: "#676ce940" }; // celeste para regalos
        return null;
      },
    };

    new agGrid.Grid(document.querySelector("#detallePedidoGrid"), gridOptions);
  };

  /* ===================== MODAL AGREGAR PRODUCTO ===================== */

  let productosNormales = [];
  let productosRegalo = [];

  window.cargarProductos = async function () {
    const resp = await fetch("/api/productos");
    if (!resp.ok) throw new Error("Error al cargar productos");

    const data = await resp.json();
    productosNormales = data.filter((p) => !p.regalo);
    productosRegalo = data.filter((p) => p.regalo);
    return true;
  };

  window.agregarNuevoProducto = async function () {
    const modal = document.getElementById("modalAgregarProducto");
    const toggle = document.getElementById("toggleRegalo");
    const toggleCircle = document.getElementById("toggleCircle");
    const toggleSwitch = document.getElementById("toggleSwitch");
    const select = document.getElementById("selectProducto");
    const inputCantidad = document.getElementById("inputCantidad");

    // 🔸 Si aún no se han cargado los productos, esperar a que se carguen
    if (!productosNormales.length && !productosRegalo.length) {
      try {
        cargarProductos();
      } catch (err) {
        console.error("❌ Error cargando productos:", err);
        Swal.fire("Error", "No se pudieron cargar los productos.", "error");
        return;
      }
    }

    // 🔸 Mostrar modal solo después de cargar productos
    modal.classList.remove("hidden");
    modal.classList.add("flex");

    // 🔹 Función auxiliar para llenar el select
    window.llenarSelect = function (esRegalo) {
      select.innerHTML = `<option value="">Seleccione un producto</option>`;
      const lista = esRegalo ? productosRegalo : productosNormales;
      lista.forEach((p) => {
        const opt = document.createElement("option");
        opt.value = p.idProducto;
        opt.textContent = `${p.codProducto} - ${p.descProducto}`;
        select.appendChild(opt);
      });
    };

    // 🔹 Actualizar toggle visualmente y limpiar al cambiar
    toggle.onchange = () => {
      const esRegalo = toggle.checked;
      toggleCircle.style.transform = esRegalo
        ? "translateX(20px)"
        : "translateX(0px)";
      toggleSwitch.style.backgroundColor = esRegalo ? "#3b82f6" : "#d1d5db";
      inputCantidad.value = 1;
      llenarSelect(esRegalo);
    };

    // 🔸 Inicializar en modo "Normal" por defecto
    toggle.checked = false;
    toggle.onchange();

    // 🔹 Botón cancelar
    document.getElementById("btnCancelarAgregar").onclick = () => {
      modal.classList.add("hidden");
      modal.classList.remove("flex");
      toggle.checked = false;
      toggle.onchange(); // limpiar
    };

    // 🔹 Confirmar agregar
    document.getElementById("btnConfirmarAgregar").onclick = () => {
      const idProducto = parseInt(selectProducto.value);
      const cantidad = parseInt(inputCantidad.value) || 1;
      const esRegalo = toggleRegalo.checked;

      if (!idProducto) {
        Swal.fire({
          title: "Selecciona un producto",
          icon: "warning",
          confirmButtonColor: "#3085d6",
        });
        return;
      }

      // Buscar el producto seleccionado
      const productoSeleccionado = (
        esRegalo ? productosRegalo : productosNormales
      ).find((p) => p.idProducto === idProducto);

      if (!productoSeleccionado) {
        Swal.fire({
          title: "Producto no encontrado",
          icon: "error",
          confirmButtonColor: "#d33",
        });
        return;
      }

      // Obtener los datos actuales del grid
      const rows = [];
      gridOptions.api.forEachNode((n) => rows.push(n.data));

      // Verificar si el producto ya existe (por ID y tipo regalo)
      const existente = rows.find(
        (r) =>
          r.producto.idProducto === idProducto &&
          !!r.producto.regalo === !!esRegalo
      );

      let mensaje = "";
      let icono = "success";

      if (existente) {
        // 🔹 Si ya existe, aumentar cantidad
        existente.cantidad += cantidad;
        gridOptions.api.applyTransaction({ update: [existente] });
        mensaje = `Se aumentó la cantidad del producto <b>${productoSeleccionado.descProducto}</b> en <b>+${cantidad}</b>.`;
      } else {
        // 🔹 Si no existe, agregar nuevo
        const nuevo = {
          producto: productoSeleccionado,
          cantidad,
          precioUnitario: productoSeleccionado.precio || 0,
          precioTotal: (productoSeleccionado.precio || 0) * cantidad,
        };
        gridOptions.api.applyTransaction({ add: [nuevo] });
        mensaje = `Se agregó el producto <b>${productoSeleccionado.descProducto}</b> correctamente.`;
      }

      // 🔹 Recalcular totales
      recalcularFinanzas();

      // 🔹 Cerrar modal
      modal.classList.add("hidden");
      modal.classList.remove("flex");

      // 🔹 Confirmación visual elegante
      Swal.fire({
        title: esRegalo ? "🎁 Producto de regalo" : "✅ Producto agregado",
        html: mensaje,
        icon: icono,
        timer: 2000,
        showConfirmButton: false,
        position: "top-end",
        toast: true,
        background: "#f8f9fa",
      });
    };
  };

  window.eliminarProducto = function (node) {
    Swal.fire({
      title: "¿Eliminar producto?",
      text: "Esta acción no se puede deshacer",
      icon: "warning",
      showCancelButton: true,
      confirmButtonText: "Sí, eliminar",
      cancelButtonText: "Cancelar",
    }).then((r) => {
      if (r.isConfirmed && gridOptions.api) {
        gridOptions.api.applyTransaction({ remove: [node.data] });
        recalcularFinanzas();
        Swal.fire("Eliminado", "El producto fue eliminado", "success");
      }
    });
  };

  window.evidenciasDePedido = async function () {
    const inputComprobante = document.getElementById("inputComprobante");
    const preview = document.getElementById("previewComprobante");
    const radiosPago = document.getElementsByName("estadoPago");
    const adelantoContainer = document.getElementById("adelantoContainer");
    const inputAdelanto = document.getElementById("montoAdelanto");

    // 🖼️ Vista previa del comprobante
    inputComprobante?.addEventListener("change", (e) => {
      const file = e.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (ev) => {
          preview.innerHTML = `
          <img src="${ev.target.result}" alt="Comprobante" class="max-h-64 rounded-lg shadow-md border border-gray-300" />
          <p class="text-xs text-gray-500 mt-2">${file.name}</p>
        `;
        };
        reader.readAsDataURL(file);
      } else {
        preview.innerHTML = "Ningún archivo seleccionado";
      }
    });

    // 💵 Controlar estado de pago
    radiosPago.forEach((radio) => {
      radio.addEventListener("change", () => {
        const esAdelanto = radio.value === "adelanto";
        adelantoContainer.classList.toggle("hidden", !esAdelanto);
        if (!esAdelanto) inputAdelanto.value = "0.00";
        actualizarFinanzasPago();
      });
    });

    inputAdelanto?.addEventListener("input", actualizarFinanzasPago);

    function actualizarFinanzasPago() {
      const totalElem = document.querySelector(
        "#infoFinanciera span.text-green-600"
      );
      if (!totalElem) return;

      const total = parseFloat(totalElem.textContent.replace("S/ ", "")) || 0;
      const adelanto = parseFloat(inputAdelanto.value) || 0;
      const igv = (total / 1.18) * 0.18;
      const subtotal = total - igv;

      document.getElementById("infoFinanciera").innerHTML = `
      <p><strong>Subtotal:</strong> S/ ${subtotal.toFixed(2)}</p>
      <p><strong>IGV:</strong> S/ ${igv.toFixed(2)}</p>
      <p><strong>Adelanto:</strong> S/ ${adelanto.toFixed(2)}</p>
      <p><strong>Total:</strong> <span class="font-bold text-green-600">S/ ${total.toFixed(
        2
      )}</span></p>
    `;
    }
  };

  /* ===================== SELECCIÓN TIPO PAGO ===================== */

  let estadoPago = "completo"; // "completo" | "adelanto"
  let montoAdelanto = 0;

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

    // Card seleccionada
    card.classList.remove("border-gray-300");
    card.classList.add("border-purple-500", "bg-purple-50");

    const amountDiv = document.getElementById(amountId);
    if (showAmount) {
      amountDiv.classList.remove("hidden");
      estadoPago = "adelanto";
      montoAdelanto =
        parseFloat(document.getElementById("montoAdelanto").value) || 0;
    } else {
      amountDiv.classList.add("hidden");
      document.getElementById("montoAdelanto").value = "0.00";
      estadoPago = "completo";
      montoAdelanto = 0;
    }

    recalcularFinanzas();
  };

  // Escuchar cambio del monto manualmente
  document.addEventListener("input", (e) => {
    if (e.target.id === "montoAdelanto") {
      estadoPago = "adelanto";
      montoAdelanto = parseFloat(e.target.value) || 0;
      recalcularFinanzas();
    }
  });

  /* ===================== RECALCULAR FINANZAS ===================== */

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
    <p><strong>Total:</strong> 
      <span class="font-bold text-green-600">S/ ${Number(total).toFixed(
        2
      )}</span>
    </p>
  `;
  }

  window.recalcularFinanzas = function () {
    if (!gridOptions?.api) return;

    const rows = [];
    gridOptions.api.forEachNode((node) => rows.push(node.data));

    // Solo sumar productos normales (no regalo)
    const productosNormales = rows.filter((r) => !r.producto?.regalo);

    const subtotal = productosNormales.reduce(
      (acc, p) => acc + (p.cantidad || 0) * (p.precioUnitario || 0),
      0
    );
    const igv = subtotal * 0.18;
    const total = subtotal + igv;

    renderResumenFinanciero({ subtotal, igv, total });
  };

  /* ===================== GUARDAR PEDIDO ===================== */

  let pedidoActual = {
    idPedido: null,
    detalles: [],
    evidenciaFile: null,
    estadoPago: "completo",
    adelanto: 0,
  };

  function agregarProductoAlGrid(producto, cantidad, esRegalo) {
    const existente = gridOptions.api.getDisplayedRowAtIndex(
      gridOptions.api.getDisplayedRowCount() - 1
    );

    const rows = [];
    gridOptions.api.forEachNode((n) => rows.push(n.data));

    const duplicado = rows.find(
      (r) =>
        r.producto.idProducto === producto.idProducto &&
        r.producto.regalo === esRegalo
    );

    if (duplicado) {
      duplicado.cantidad += cantidad;
      duplicado.precioTotal = duplicado.precioUnitario * duplicado.cantidad;
      gridOptions.api.applyTransaction({ update: [duplicado] });
    } else {
      const nuevo = {
        producto,
        cantidad,
        precioUnitario: producto.precio,
        precioTotal: producto.precio * cantidad,
      };
      gridOptions.api.applyTransaction({ add: [nuevo] });
    }

    recalcularFinanzas();
  }

  window.guardarPedidoCompleto = async function (idPedido) {
    const fileInput = document.getElementById("inputComprobante");
    const formData = new FormData();

    // 🖼️ Agregar archivo (opcional)
    if (fileInput?.files?.length > 0) {
      formData.append("file", fileInput.files[0]);
    }

    // 📦 Recolectar productos del grid
    const detalles = [];
    gridOptions.api.forEachNode((n) =>
      detalles.push({
        producto: {
          idProducto: n.data.producto.idProducto,
          regalo: !!n.data.producto.regalo,
        },
        cantidad: n.data.cantidad,
        precioUnitario: n.data.precioUnitario,
      })
    );

    // 💰 Recalcular totales
    const totalElem = document.querySelector(
      "#infoFinanciera span.text-green-600"
    );
    const total = parseFloat(totalElem.textContent.replace("S/ ", "")) || 0;
    const subtotal = total / 1.18;
    const igv = subtotal * 0.18;

    // 🧾 Crear objeto pedido
    const pedido = {
      idPedido,
      subtotal,
      igv,
      montoTotal: total,
      adelanto: montoAdelanto,
      tipoPago: estadoPago,
      detalles,
    };

    formData.append(
      "pedido",
      new Blob([JSON.stringify(pedido)], { type: "application/json" })
    );

    try {
      const resp = await fetch(
        "http://localhost:8080/api/pedidos/guardarPedidoCompleto",
        {
          method: "POST",
          body: formData,
        }
      );

      const data = await resp.json();
      if (data.success) {
        Swal.fire({
          icon: "success",
          title: "Pedido guardado correctamente",
          text: "Incluye evidencia y productos.",
          timer: 2500,
          showConfirmButton: false,
        });
      } else {
        throw new Error(data.message);
      }
    } catch (err) {
      Swal.fire({
        icon: "error",
        title: "Error al guardar pedido",
        text: err.message,
      });
    }
  };

  // Evidencia
  document
    .getElementById("inputComprobante")
    .addEventListener("change", (e) => {
      const file = e.target.files[0];
      if (file) pedidoActual.evidenciaFile = file;
    });

  // Pago completo / adelanto
  document.getElementsByName("estadoPago").forEach((radio) => {
    radio.addEventListener("change", (e) => {
      pedidoActual.estadoPago = e.target.value;
      recalcularFinanzas();
    });
  });

  document
    .getElementById("btnGuardarPedido")
    ?.addEventListener("click", async () => {
      try {
        const detalles = [];
        gridOptions.api.forEachNode((n) =>
          detalles.push({
            producto: {
              idProducto: n.data.producto.idProducto,
              regalo: !!n.data.producto.regalo,
            },
            cantidad: n.data.cantidad,
            precioUnitario: n.data.precioUnitario,
          })
        );

        const pagoCompleto = document
          .getElementById("cardPagoCompleto")
          .classList.contains("bg-purple-50");
        const montoAdelanto = parseFloat(
          document.getElementById("inputMontoAdelanto").value || 0
        );

        const pedido = {
          idPedido: pedidoActual.idPedido,
          subtotal: calcularSubtotal(detalles),
          igv: calcularIGV(detalles),
          montoTotal: calcularTotal(detalles),
          adelanto: pagoCompleto ? montoAdelanto : 0,
          tipoPago: pagoCompleto ? "Completo" : "Adelanto",
          detalles: detalles,
        };

        const formData = new FormData();
        formData.append(
          "pedido",
          new Blob([JSON.stringify(pedido)], { type: "application/json" })
        );
        if (pedidoActual.evidenciaFile)
          formData.append("file", pedidoActual.evidenciaFile);

        const response = await fetch("/api/pedidos/guardarPedidoCompleto", {
          method: "POST",
          body: formData,
        });

        const data = await response.json();

        if (data.success) {
          Swal.fire("Éxito", "El pedido fue guardado correctamente", "success");
        } else {
          Swal.fire(
            "Error",
            data.message || "No se pudo guardar el pedido",
            "error"
          );
        }
      } catch (e) {
        console.error(e);
        Swal.fire("Error", "Ocurrió un error al guardar el pedido", "error");
      }
    });

  window.initDetallePedido = initDetallePedido;
})();
