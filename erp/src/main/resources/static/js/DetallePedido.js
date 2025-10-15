let gridOptions;

async function initDetallePedido() {
  // 🔹 Obtener el id guardado por ListaPedidos.js
  const idPedido = localStorage.getItem("pedidoSeleccionado");

  if (!idPedido) {
    Swal.fire({
      icon: "warning",
      title: "Pedido no encontrado",
      text: "No se encontró el ID del pedido seleccionado.",
    });
    return;
  }

  console.log("📦 Cargando pedido con ID:", idPedido);

  try {
    const resp = await fetch(`/api/pedidos/${idPedido}`);
    if (!resp.ok) throw new Error("Error al obtener pedido");

    const pedido = await resp.json();

    // Renderizar secciones
    renderCliente(pedido.cliente);
    renderPedido(pedido);
    renderTotales(pedido);
    initAGGrid(pedido.detalles || []);
  } catch (err) {
    console.error("❌ Error al cargar pedido:", err);
    Swal.fire({
      icon: "error",
      title: "Error al cargar pedido",
      text: err.message,
    });
  }
}

// 🔸 Asegurar que se ejecute al cargar
document.addEventListener("DOMContentLoaded", initDetallePedido);

// Cliente
function renderCliente(cliente = {}) {
  document.getElementById("clienteNombre").textContent = cliente.nombres ?? "—";
  document.getElementById("clienteTelefono").textContent =
    cliente.telefono ?? "—";
  document.getElementById("clienteCorreo").textContent = cliente.correo ?? "—";
}

// Pedido
function renderPedido(pedido) {
  document.getElementById("pedidoDocumento").textContent =
    pedido.documento ?? "—";
  document.getElementById("pedidoCiudad").textContent = pedido.ciudad ?? "—";
  document.getElementById("pedidoPago").textContent = pedido.tipoPago ?? "—";
  document.getElementById("pedidoComprobante").textContent =
    pedido.tipoComprobante ?? "—";
  document.getElementById("pedidoEstado").textContent =
    pedido.estadoPedido?.descripcion ?? "—";
  document.getElementById("pedidoFecha").textContent = pedido.fechaRegistro
    ? new Date(pedido.fechaRegistro).toLocaleDateString()
    : "—";
}

// Totales
function renderTotales(pedido) {
  document.getElementById("subtotal").textContent =
    pedido.subtotal?.toFixed(2) ?? "0.00";
  document.getElementById("igv").textContent = pedido.igv?.toFixed(2) ?? "0.00";
  document.getElementById("adelanto").textContent =
    pedido.adelanto?.toFixed(2) ?? "0.00";
  document.getElementById("total").textContent =
    pedido.montoTotal?.toFixed(2) ?? "0.00";
}

// AG GRID
function initAGGrid(detalles) {
  const columnDefs = [
    { headerName: "Código", field: "producto.codProducto", flex: 1 },
    { headerName: "Descripción", field: "producto.descProducto", flex: 2 },
    { headerName: "Cantidad", field: "cantidad", width: 120, editable: true },
    {
      headerName: "Precio Unitario",
      field: "precioUnitario",
      width: 150,
      valueFormatter: (p) => `S/ ${(p.value ?? 0).toFixed(2)}`,
    },
    {
      headerName: "Subtotal",
      valueGetter: (p) => p.data.cantidad * p.data.precioUnitario,
      width: 150,
      valueFormatter: (p) => `S/ ${(p.value ?? 0).toFixed(2)}`,
    },
    {
      headerName: "Acciones",
      cellRenderer: () => `
        <button class="text-blue-600 hover:text-blue-800 mx-1"><i class="fa-solid fa-pen"></i></button>
        <button class="text-red-600 hover:text-red-800 mx-1"><i class="fa-solid fa-trash"></i></button>
      `,
      width: 120,
    },
  ];

  gridOptions = {
    columnDefs,
    rowData: detalles,
    domLayout: "autoHeight",
    defaultColDef: { resizable: true, sortable: true },
  };

  const gridDiv = document.querySelector("#detallePedidoGrid");
  new agGrid.Grid(gridDiv, gridOptions);
}
