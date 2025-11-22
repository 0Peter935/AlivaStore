document.addEventListener("DOMContentLoaded", () => {
  let gridApiProductos;

  initListaProductos();

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
        cellRenderer: (p) => {
          // --- Si es fila detalle, renderiza el acordeón ---
          if (p.data.__type === "detail") {
            const variantes = p.data.variante || [];

            if (!variantes.length) {
              return `
      <div class="bg-gray-50 p-4 text-center text-gray-400 italic rounded-lg border border-gray-200">
        No se registran variantes para este producto.
      </div>`;
            }

            const filas = variantes
              .map((v) => {
                const totalInv = (v.almacenStock || []).reduce(
                  (a, s) => a + (s.inventario || 0),
                  0
                );

                // --- POPOVER DETALLADO POR ALMACÉN ---
                const detalleAlmacenes = (v.almacenStock || [])
                  .map(
                    (a) => `
          <div class="flex justify-between text-[13px] text-gray-700 border-b last:border-none py-1">
            <span>${a.almacen?.descripcion || "-"}</span>
            <span class="font-semibold">${a.inventario} und.</span>
          </div>`
                  )
                  .join("");

                return `
      <tr class="border-b last:border-none hover:bg-indigo-50/40 transition">
        <td class="py-3 px-4 font-medium text-gray-800">${v.titulo ?? "-"}</td>

        <td class="py-3 px-4 text-gray-700">S/. ${Number(v.precio || 0).toFixed(
          2
        )}</td>

        <!-- Inventario -->
        <td class="py-3 px-4">
          <div class="relative group cursor-pointer text-indigo-700 font-medium">
            ${totalInv} unidades

            <!-- Tooltip -->
            <div class="absolute left-0 -top-2 -translate-y-full hidden group-hover:block
    bg-white shadow-xl border border-gray-300 rounded-lg p-3 w-64 z-50">
              <p class="font-semibold text-indigo-700 mb-2 text-[14px]">
                Inventario por almacén
              </p>
              ${detalleAlmacenes || "<p class='text-gray-400'>Sin stock</p>"}
            </div>
          </div>
        </td>

        <!-- Estado -->
        <td class="py-3 px-4">
          ${
            totalInv === 0
              ? `<span class="px-3 py-1 bg-red-100 text-red-700 rounded-full text-sm font-semibold">Agotado</span>`
              : totalInv <= 10
              ? `<span class="px-3 py-1 bg-amber-100 text-amber-700 rounded-full text-sm font-semibold">Poco stock</span>`
              : `<span class="px-3 py-1 bg-emerald-100 text-emerald-700 rounded-full text-sm font-semibold">En stock</span>`
          }
        </td>

        <!-- Acciones -->
        <td class="py-3 px-4 text-center">
          <div class="flex justify-center gap-4 text-[15px]">
            <button
              onclick='abrirModalStock(${JSON.stringify(v)})'
              class="text-yellow-500 hover:text-yellow-700 transition-colors"
              title="Editar stock del producto"
            >
              <i class="fa-solid fa-boxes-stacked"></i>
            </button>
          </div>
        </td>
      </tr>`;
              })
              .join("");

            return `
  <div class="col-span-full bg-gradient-to-br from-indigo-50 to-white rounded-xl shadow-sm border border-indigo-100 p-5 mt-2">
    <p class="text-[15px] font-semibold text-indigo-700 uppercase tracking-wide mb-3">
      Variantes del producto
    </p>

    <div class="overflow-visible rounded-lg border border-gray-200 bg-white relative">
      <table class="min-w-full text-sm">
        <thead class="bg-gray-50 text-gray-600 uppercase text-[13px] border-b">
          <tr>
            <th class="py-2 px-4 text-left font-semibold">Título</th>
            <th class="py-2 px-4 text-left font-semibold">Precio</th>
            <th class="py-2 px-4 text-left font-semibold">Inventario</th>
            <th class="py-2 px-4 text-left font-semibold">Estado</th>
            <th class="py-2 px-4 text-center font-semibold">Acciones</th>
          </tr>
        </thead>
        <tbody>${filas}</tbody>
      </table>
    </div>
  </div>`;
          }

          // --- Fila normal ---
          return `<span class="font-semibold text-gray-700">${p.value}</span>`;
        },
        colSpan: (p) =>
          p.data.__type === "detail"
            ? p.columnApi.getAllDisplayedColumns().length
            : 1,
      },
      {
        headerName: "Código Prod.",
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
        headerName: "Inventario por Variante",
        field: "variante",
        filter: false,
        autoHeight: true,
        cellRenderer: (params) => {
          const variantes = params.value || [];

          // 🔹 Total general (sumando inventarios de todas las variantes)
          const total = variantes.reduce((sum, v) => {
            const sub = (v.almacenStock || []).reduce(
              (s, a) => s + (a.inventario || 0),
              0
            );
            return sum + sub;
          }, 0);

          // 🔹 Tooltip dinámico
          const tooltip = document.createElement("div");
          tooltip.className = `
      hidden fixed bg-white border border-gray-200 rounded-xl shadow-xl
      p-4 text-sm z-[9999] max-w-[90vw] sm:max-w-[380px]
      overflow-auto transition-all duration-200
    `;

          // 🔹 Contenido del tooltip
          tooltip.innerHTML = variantes.length
            ? `
        <p class="text-[14px] font-semibold text-indigo-700 mb-2 border-b pb-1">
          Variantes del producto
        </p>
        <div class="space-y-2">
          ${variantes
            .map((v) => {
              const totalVar = (v.almacenStock || []).reduce(
                (sum, a) => sum + (a.inventario || 0),
                0
              );
              return `
                <div class="text-gray-800 flex flex-wrap items-baseline gap-x-1 text-[13px] leading-snug">
                  <span class="font-medium">${v.titulo ?? "(Sin título)"}</span>
                  <span class="text-indigo-700 font-semibold ml-auto whitespace-nowrap">
                    — ${totalVar} unidades
                  </span>
                </div>
              `;
            })
            .join("")}
        </div>
      `
            : `<p class="text-gray-400 italic">Sin variantes registradas</p>`;

          document.body.appendChild(tooltip);

          // 🔹 Contenedor principal (celda)
          const wrapper = document.createElement("div");
          wrapper.className =
            "relative font-semibold text-indigo-700 cursor-pointer hover:text-indigo-900 transition";
          wrapper.innerHTML = `${total} unidades`;

          // 🔹 Mostrar tooltip al pasar el mouse
          wrapper.addEventListener("mouseenter", (e) => {
            const rect = e.target.getBoundingClientRect();
            tooltip.style.display = "block";
            tooltip.style.left = `${
              rect.left + rect.width / 2 - tooltip.offsetWidth / 2
            }px`;
            tooltip.style.top = `${rect.bottom + 8}px`;
          });
          wrapper.addEventListener("mouseleave", () => {
            tooltip.style.display = "none";
          });

          return wrapper;
        },
      },
      {
        headerName: "Acciones",
        width: 200,
        filter: false,
        cellRenderer: (params) => {
          const producto = params.data;
          const rowId = params.node.id;
          const expanded = !!producto._expanded;
          const isActive = producto.estado;
          const esRegalo = producto.regalo;

          // Evita acciones para filas detalle
          if (producto.__type === "detail") return "";

          return `
      <div class="flex justify-center items-center gap-3">
        <!-- Mostrar/ocultar variantes -->
        <button
          onclick="toggleFilaVariantes('${rowId}', this)"
          class="text-gray-600 hover:text-indigo-600 transition-transform duration-300"
          title="Ver variantes"
        >
          <i class="fa-solid fa-chevron-down ${
            expanded ? "rotate-180 text-indigo-600" : ""
          } transform transition-transform duration-300"></i>
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
            ${isActive ? "bg-blue-500" : "bg-red-500"}"
          title="Activar / Desactivar producto"
        >
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
      defaultColDef: {
        flex: 1,
        resizable: true,
        minWidth: 120,
        filter: true,
        sortable: true,
      },
      pagination: true,
      paginationPageSize: 10,
      getRowHeight: (params) => {
        if (params.data && params.data.__type === "detail") {
          const el = document.createElement("div");
          el.innerHTML = params.data.variante?.length
            ? `
        <div class="p-5">
          <p class="font-semibold mb-2">${
            params.data.variante.length
          } variantes</p>
          <table class="min-w-full"><tbody>${params.data.variante
            .map((v) => `<tr><td class='py-2'>${v.titulo ?? "-"}</td></tr>`)
            .join("")}</tbody></table>
        </div>`
            : `<div class="p-4 text-gray-400 italic">Sin variantes</div>`;

          document.body.appendChild(el);
          const height = el.scrollHeight + 90; // margen extra
          el.remove();
          return height;
        }

        return 40; // altura normal para filas regulares
      },
      rowClassRules: {
        "bg-white": (p) => !p.data.__type,
        "ag-row-no-hover": (p) => p.data.__type === "detail",
      },
      onGridReady: () => loadProductos(),
    };

    // Crear grid
    gridApiProductos = agGrid.createGrid(gridDiv, gridOptions);
    window.gridApiProductos = gridApiProductos;

    // Filtro global
    const searchBox = document.getElementById("searchBox");
    if (searchBox) {
      searchBox.addEventListener("input", (e) => {
        gridApiProductos.setQuickFilter(e.target.value.toLowerCase());
      });
    }
  }

  async function loadProductos() {
    try {
      const res = await fetch("/api/productos");
      if (!res.ok) throw new Error("Error al cargar productos");
      const data = await res.json();

      if (window.gridApiProductos) {
        gridApiProductos.setGridOption("rowData", data);
      }

      console.log(`✅ ${data.length} productos cargados`);
    } catch (err) {
      console.error("Error al cargar productos:", err);
      Swal.fire("Error", "No se pudieron cargar los productos.", "error");
    }
  }

  window.toggleFilaVariantes = function (rowId, btn) {
    const api = gridApiProductos;
    const rowNode = api.getRowNode(String(rowId));
    if (!rowNode) return;

    const expanded = !rowNode.data._expanded;
    rowNode.data._expanded = expanded;

    const icon = btn.querySelector("i");
    icon.classList.toggle("rotate-180", expanded);
    icon.classList.toggle("text-indigo-600", expanded);

    const parentKey = rowNode.data.idProducto || rowNode.data.codProducto;
    const detailId = `d-${parentKey}`;
    const existingDetail = api.getRowNode(detailId);

    // Si hay otra fila detalle abierta, se cierra
    api.forEachNode((n) => {
      if (n.data && n.data.__type === "detail" && n.id !== detailId) {
        api.applyTransaction({ remove: [n.data] });
        const parent = api.getRowNode(String(n.data.parentId));
        if (parent && parent.data) parent.data._expanded = false;
      }
    });

    if (expanded && !existingDetail) {
      const detailRow = {
        __type: "detail",
        id: detailId, // 🔹 clave única
        parentId: parentKey,
        variante: rowNode.data.variante || [],
      };
      api.applyTransaction({
        add: [detailRow],
        addIndex: rowNode.rowIndex + 1,
      });

      // recalcular altura suavemente
      setTimeout(() => api.resetRowHeights(), 80);
    } else if (!expanded && existingDetail) {
      api.applyTransaction({ remove: [existingDetail.data] });
      setTimeout(() => api.resetRowHeights(), 80);
    }
  };

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
  window.abrirModalStock = async function (variante) {
    const idVariante = variante.codVariante;
    idVarianteSeleccionado = idVariante;

    const modal = document.getElementById("modalEditarStock");
    const contenedor = document.getElementById("stockContainer");
    const descripcionEl = document.getElementById("productoDescripcion");

    // Mostrar info del producto arriba
    descripcionEl.textContent = `${idVariante} — ${variante.titulo}`;

    contenedor.innerHTML = `<p class="text-gray-500 italic">Cargando...</p>`;
    modal.classList.remove("hidden");
    document.body.classList.add("overflow-hidden");

    try {
      // Obtener almacenes y stock actual del producto
      const [almRes, stockRes] = await Promise.all([
        fetch("/api/almacenes"),
        fetch(`/api/almacen-stock/producto/${idVariante}`),
      ]);

      almacenesDisponibles = await almRes.json();
      stockActual = await stockRes.json();

      document.getElementById("idVarianteStock").value = idVariante;
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
    const idProducto = document.getElementById("idVarianteStock")?.value;
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
});
