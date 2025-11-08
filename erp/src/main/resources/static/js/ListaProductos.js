document.addEventListener("DOMContentLoaded", () => {
  console.log("🧩 Iniciando ListaProductos...");

  let gridApiProductos = null;
  let almacenesDisponibles = [];
  let stockActual = [];
  let idProductoSeleccionado = null;

  initListaProductos();

  // Inicializar la tabla
  async function initListaProductos() {
    const gridDiv = document.querySelector("#productosGrid");
    if (!gridDiv) return;

    const columnDefs = [
      {
        headerName: "N°",
        valueGetter: "node.rowIndex + 1",
        width: 70,
        sortable: false,
        filter: false,
      },
      {
        headerName: "Código Producto",
        field: "codProducto",
        sortable: true,
        filter: true,
        cellRenderer: (params) =>
          `<span class="font-semibold text-gray-800">${
            params.value ?? "-"
          }</span>`,
      },
      {
        headerName: "Descripción",
        field: "descProducto",
        sortable: true,
        filter: true,
      },
      {
        headerName: "Precio",
        field: "precio",
        sortable: true,
        filter: true,
        cellRenderer: (params) => {
          const value = params.value ?? 0;
          return `<span class="font-semibold text-blue-700">S/. ${value.toFixed(
            2
          )}</span>`;
        },
      },
      {
        headerName: "Stock Shopify",
        field: "stock",
        sortable: true,
        filter: true,
      },
      {
        headerName: "Inventario en Almacenes",
        field: "almacenStock",
        flex: 1.5,
        filter: false,
        cellRenderer: (params) => {
          const almacenStock = params.value || [];
          const total = almacenStock.reduce(
            (sum, a) => sum + (a.inventario || 0),
            0
          );

          // Crear tooltip flotante (popper)
          const tooltip = document.createElement("div");
          tooltip.className =
            "hidden absolute bg-white border border-gray-200 rounded-lg shadow-lg p-3 min-w-[180px] z-[9999]";
          tooltip.innerHTML = `
      <p class="text-sm font-semibold text-indigo-600 mb-1">Detalle por almacén</p>
      ${
        almacenStock.length > 0
          ? almacenStock
              .map(
                (a) => `
                <div class="flex justify-between text-sm">
                  <span class="font-medium text-gray-700">${a.almacen.descripcion}</span>
                  <span>${a.inventario}</span>
                </div>`
              )
              .join("")
          : "<p class='text-gray-400 text-sm italic'>Sin stock registrado</p>"
      }
    `;
          document.body.appendChild(tooltip);

          // Crear el contenedor principal
          const wrapper = document.createElement("div");
          wrapper.className =
            "relative cursor-pointer font-semibold text-gray-800 select-none";
          wrapper.textContent = total;

          // Mostrar tooltip dinámico fuera del grid
          wrapper.addEventListener("mouseenter", (e) => {
            const rect = e.target.getBoundingClientRect();
            tooltip.style.display = "block";
            tooltip.style.left = `${rect.left + rect.width / 2 - 90}px`;
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
        width: 180,
        filter: false,
        cellRenderer: (params) => {
          const producto = params.data;
          const isActive = producto.estado;
          const esRegalo = producto.regalo;

          return `
      <div class="flex justify-center items-center gap-3">
        <!-- Editar stock -->
        <button
          onclick='abrirModalStock(${JSON.stringify(producto)})'
          class="text-aliva-yellow hover:text-aliva-purple transition-colors"
          title="Editar stock del producto"
        >
          <i class="fa-solid fa-boxes-stacked"></i>
        </button>

        <!-- Cambiar clase regalo -->
        <button
          onclick="toggleRegaloProducto(${producto.idProducto}, ${esRegalo})"
          title="${esRegalo ? "Quitar clase regalo" : "Marcar como regalo"}"
          class="hover:scale-110 transition-transform"
        >
          <i class="fa-solid fa-gift ${
            esRegalo ? "text-pink-600" : "text-gray-400"
          }"></i>
        </button>

        <!-- Cambiar estado (activo/inactivo) -->
        <button
          onclick="toggleEstadoProducto(${producto.idProducto}, this)"
          class="relative w-11 h-6 flex items-center rounded-full transition duration-300 
            ${isActive ? "bg-blue-500" : "bg-red-500"}">
          <span class="absolute left-0.5 top-0.5 w-5 h-5 bg-white rounded-full shadow-md 
            transform transition-transform duration-300
            ${isActive ? "translate-x-5" : ""}"></span>
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
        filter: true,
        sortable: true,
      },
      onGridReady: () => loadProductos(),
    };

    gridApiProductos = agGrid.createGrid(gridDiv, gridOptions);
    window.gridApiProductos = gridApiProductos;

    // Searchbox global
    const searchBox = document.getElementById("searchBox");
    if (searchBox) {
      searchBox.addEventListener("input", (e) => {
        gridApiProductos.setQuickFilter(e.target.value.toLowerCase());
      });
    }
  }

  // Cargar productos
  async function loadProductos() {
    try {
      const res = await fetch("/api/productos");
      if (!res.ok) throw new Error("Error al cargar productos");
      const data = await res.json();
      gridApiProductos.setGridOption("rowData", data);
      console.log(`✅ ${data.length} productos cargados`);
    } catch (err) {
      console.error("❌ Error al cargar productos:", err);
      Swal.fire("Error", "No se pudieron cargar los productos.", "error");
    }
  }

  // Cambiar estado de producto
  window.toggleEstadoProducto = async function (idProducto, btn) {
    const isActive = btn.classList.contains("bg-blue-500");
    const newState = !isActive;

    btn.classList.toggle("bg-blue-500", newState);
    btn.classList.toggle("bg-red-500", !newState);
    const span = btn.querySelector("span");
    span.classList.toggle("translate-x-5", newState);

    try {
      const res = await fetch(`/api/productos/${idProducto}/estado`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ estado: newState }),
      });

      if (!res.ok) throw new Error("Error al actualizar estado");
    } catch (err) {
      console.error(err);

      btn.classList.toggle("bg-blue-500", !newState);
      btn.classList.toggle("bg-red-500", newState);
      span.classList.toggle("translate-x-5", !newState);
      Swal.fire({
        icon: "error",
        title: "Error",
        text: "No se pudo cambiar el estado del producto",
      });
    }
  };

  // Cambiar estado regalo
  window.toggleRegaloProducto = async function (idProducto, regaloActual) {
    const nuevoValor = !regaloActual;

    try {
      const res = await fetch(`/api/productos/${idProducto}/regalo`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ regalo: nuevoValor }),
      });

      if (!res.ok) throw new Error(await res.text());
      loadProductos();
    } catch (err) {
      console.error("Error al actualizar regalo:", err);
      Swal.fire({
        icon: "error",
        title: "Error",
        text: "No se pudo cambiar el estado de regalo del producto.",
      });
    }
  };

  // Abrir modal
  window.abrirModalStock = async function (producto) {
    const idProducto = producto.idProducto;
    idProductoSeleccionado = idProducto;

    const modal = document.getElementById("modalEditarStock");
    const contenedor = document.getElementById("stockContainer");
    const descripcionEl = document.getElementById("productoDescripcion");

    // Mostrar info del producto arriba
    descripcionEl.textContent = `${producto.codProducto} — ${producto.descProducto}`;

    contenedor.innerHTML = `<p class="text-gray-500 italic">Cargando...</p>`;
    modal.classList.remove("hidden");
    document.body.classList.add("overflow-hidden");

    try {
      // Obtener almacenes y stock actual del producto
      const [almRes, stockRes] = await Promise.all([
        fetch("/api/almacenes"),
        fetch(`/api/almacen-stock/producto/${idProducto}`),
      ]);

      almacenesDisponibles = await almRes.json();
      stockActual = await stockRes.json();

      document.getElementById("idProductoStock").value = idProducto;
      renderizarFilasStock();
    } catch (err) {
      console.error("Error al cargar datos de stock:", err);
      contenedor.innerHTML = `<p class="text-red-500">Error al cargar datos</p>`;
    }
  };

  // Cerrar modal
  window.cerrarModalStock = function () {
    const modal = document.getElementById("modalEditarStock");
    modal.classList.add("hidden");
    document.body.classList.remove("overflow-hidden");
  };

  // Renderizar filas dinámicas
  function renderizarFilasStock() {
    const contenedor = document.getElementById("stockContainer");
    contenedor.innerHTML = "";

    stockActual.forEach((s, index) => {
      const fila = crearFilaStock(s.almacen.idAlmacen, s.inventario, index);
      contenedor.appendChild(fila);
    });

    // Evento agregar nueva fila
    document.getElementById("btnAgregarStock").onclick = () =>
      agregarNuevaFila();
  }

  // Crear fila de stock
  function crearFilaStock(idAlmacen, cantidad, index) {
    const fila = document.createElement("div");
    fila.className = "flex items-center gap-3 bg-gray-50 p-3 rounded-lg";

    const stock = stockActual[index];
    const esExistente = stock.idAlmacenStock && stock.idAlmacenStock > 0;

    // Obtener lista de almacenes usados
    const usados = stockActual
      .map((s, i) => (i !== index ? s.almacen.idAlmacen : null))
      .filter((id) => id !== null);

    // Filtrar almacenes que no estén usados
    const almacenesFiltrados = almacenesDisponibles.filter(
      (a) =>
        !usados.includes(a.idAlmacen) || a.idAlmacen === stock.almacen.idAlmacen
    );

    // Crear el select
    const select = document.createElement("select");
    select.className =
      "flex-1 border border-gray-300 rounded-lg px-2 py-1 text-sm focus:ring-1 focus:ring-blue-500";

    select.innerHTML = almacenesFiltrados
      .map(
        (a) => `
      <option value="${a.idAlmacen}" ${
          a.idAlmacen === stock.almacen.idAlmacen ? "selected" : ""
        }>
        ${a.descripcion}
      </option>
    `
      )
      .join("");

    // Si es existente, deshabilitar
    if (esExistente) {
      select.disabled = true;
      select.classList.add("bg-gray-100", "cursor-not-allowed", "opacity-70");
    } else {
      select.classList.add("bg-white");
    }

    // Campo cantidad
    const input = document.createElement("input");
    input.type = "number";
    input.value = cantidad || 0;
    input.min = 0;
    input.className =
      "w-24 border border-gray-300 rounded-lg px-2 py-1 text-right text-sm focus:ring-1 focus:ring-blue-500";

    // Botón eliminar
    const btnEliminar = document.createElement("button");
    btnEliminar.innerHTML = `<i class="fa-solid fa-trash"></i>`;
    btnEliminar.className =
      "text-red-500 hover:text-red-700 px-2 py-1 transition-all";
    btnEliminar.onclick = () => {
      Swal.fire({
        title: "¿Eliminar fila?",
        text: "¿Seguro que deseas eliminar este almacén del stock?",
        icon: "warning",
        showCancelButton: true,
        confirmButtonColor: "#d33",
        cancelButtonColor: "#3085d6",
        confirmButtonText: "Sí, eliminar",
        cancelButtonText: "Cancelar",
      }).then((r) => {
        if (r.isConfirmed) {
          stockActual.splice(index, 1);
          renderizarFilasStock();
        }
      });
    };

    fila.appendChild(select);
    fila.appendChild(input);
    fila.appendChild(btnEliminar);
    return fila;
  }

  // Agregar nueva fila
  function agregarNuevaFila() {
    const usados = stockActual.map((s) => s.almacen.idAlmacen);
    const disponibles = almacenesDisponibles.filter(
      (a) => !usados.includes(a.idAlmacen)
    );

    if (disponibles.length === 0) {
      Swal.fire({
        icon: "info",
        title: "Sin almacenes disponibles",
        text: "Ya se han asignado todos los almacenes a este producto.",
        timer: 2500,
        showConfirmButton: false,
      });
      return;
    }

    const nuevo = {
      idAlmacenStock: 0,
      almacen: { idAlmacen: disponibles[0].idAlmacen },
      inventario: 0,
    };

    stockActual.push(nuevo);
    renderizarFilasStock();
  }

  // Guardar cambios
  window.guardarCambiosStock = async function () {
    const idProducto = document.getElementById("idProductoStock")?.value;
    const filas = document.querySelectorAll("#stockContainer > div");

    // Validar que haya filas
    if (!filas.length) {
      Swal.fire({
        icon: "info",
        title: "Sin filas para guardar",
        text: "Debe existir al menos un almacén con stock.",
      });
      return;
    }

    // Construir detalle validando select/input
    const detalle = [];
    let errorDetectado = false;

    filas.forEach((fila, i) => {
      const select = fila.querySelector("select");
      const input = fila.querySelector("input");

      if (!select || !input) {
        console.warn(`Fila ${i + 1} sin select o input válido`);
        errorDetectado = true;
        return;
      }

      const idAlmacen = parseInt(select.value);
      const nombreAlmacen =
        select.options[select.selectedIndex]?.text || "Desconocido";
      const cantidad = parseInt(input.value) || 0;

      if (isNaN(idAlmacen)) {
        Swal.fire({
          icon: "warning",
          title: "Almacén no seleccionado",
          text: `Debe elegir un almacén en la fila ${i + 1}.`,
        });
        errorDetectado = true;
        return;
      }

      detalle.push({
        almacen: { idAlmacen, nombre: nombreAlmacen },
        inventario: cantidad,
      });
    });

    if (errorDetectado) return;

    // Mostrar resumen de cambios
    const resumenHtml = `
    <div class="text-left text-gray-800">
      <p class="mb-2 font-semibold">Se actualizarán los siguientes stocks:</p>
      <ul class="list-disc ml-5">
        ${detalle
          .map(
            (d) =>
              `<li><b>${d.almacen.nombre}</b>: ${d.inventario} unidades</li>`
          )
          .join("")}
      </ul>
    </div>
  `;

    const confirm = await Swal.fire({
      title: "¿Desea guardar los cambios?",
      html: resumenHtml,
      icon: "question",
      showCancelButton: true,
      confirmButtonColor: "#16a34a",
      cancelButtonColor: "#d33",
      confirmButtonText: "Sí, guardar",
      cancelButtonText: "Cancelar",
      reverseButtons: true,
    });

    if (!confirm.isConfirmed) return;

    // Enviar al backend
    try {
      const resp = await fetch(`/api/almacen-stock/producto/${idProducto}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(detalle),
      });

      if (!resp.ok) throw new Error(await resp.text());

      Swal.fire({
        icon: "success",
        title: "Stock actualizado correctamente",
        text: "Los cambios fueron guardados exitosamente.",
        timer: 2000,
        showConfirmButton: false,
      });

      cerrarModalStock();
      loadProductos?.();
    } catch (err) {
      console.error("Error al guardar:", err);
      Swal.fire({
        icon: "error",
        title: "Error al guardar",
        text: "Ocurrió un problema al actualizar el stock.",
      });
    }
  };

  // Abrir modal
  window.abrirModalNuevoProducto = function () {
    document.getElementById("formNuevoProducto").reset();
    document.getElementById("modalNuevoProducto").classList.remove("hidden");
    document.body.classList.add("overflow-hidden");
  };

  // Cerrar modal
  window.cerrarModalNuevoProducto = function () {
    document.getElementById("modalNuevoProducto").classList.add("hidden");
    document.body.classList.remove("overflow-hidden");
  };

  // Guardar producto
  window.guardarNuevoProducto = async function () {
    const producto = {
      codProducto: document.getElementById("npCodProducto").value.trim(),
      descProducto: document.getElementById("npDescProducto").value.trim(),
      stock: parseInt(document.getElementById("npStock").value) || 0,
      precio: parseFloat(document.getElementById("npPrecio").value) || 0,
      imagen: document.getElementById("npImagen").value.trim(),
      regalo: document.getElementById("npRegalo").checked,
      estado: document.getElementById("npEstado").checked,
    };

    // Validación rápida
    if (
      !producto.codProducto ||
      !producto.descProducto ||
      producto.precio <= 0
    ) {
      Swal.fire({
        icon: "warning",
        title: "Campos obligatorios",
        text: "Por favor completa código, descripción y precio válidos.",
      });
      return;
    }

    try {
      const resp = await fetch("/api/productos/nuevo", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(producto),
      });

      if (!resp.ok) throw new Error(await resp.text());

      Swal.fire({
        icon: "success",
        title: "Producto agregado",
        text: "El nuevo producto fue registrado correctamente.",
        timer: 2000,
        showConfirmButton: false,
      });

      cerrarModalNuevoProducto();
      loadProductos();
    } catch (err) {
      console.error("Error al guardar producto:", err);
      Swal.fire({
        icon: "error",
        title: "Error al guardar",
        text: "No se pudo registrar el producto.",
      });
    }
  };
});
