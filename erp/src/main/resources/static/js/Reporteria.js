let chartPedidosTiempo;
let chartPedidosEstado;
let chartPedidosVendedor;
let chartTopProductos;
let chartVentasLugar;
let chartAnalisisPredictivo;
let topProductosData = [];

let Fechainicio;
let Fechafin;
let lugar;

$("#btnActualizarPredictivo").on("click", function () {
  entrenarModeloPredictivo();
});

function actualizarVariablesGlobales() {
  Fechainicio = $("#fecha_inicio").val();
  Fechafin = $("#fecha_fin").val();
  lugar = $("#filtroLugar").val() || null;
}

function forceChartsResize() {
  setTimeout(() => {
    window.dispatchEvent(new Event("resize"));
  }, 50);
}

$(document).ready(function () {
  // Inicialización de fechas
  const hoy = new Date();
  const primerDia = new Date(hoy.getFullYear(), hoy.getMonth() - 1, 1);
  const ultimoDia = new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0);

  const formato = (fecha) => fecha.toISOString().split("T")[0];

  $("#fecha_inicio").val(formato(primerDia));
  $("#fecha_fin").val(formato(ultimoDia));

  // ================================
  //   G R Á F I C O S   I N I C I A L E S
  // ================================

  chartPedidosEstado = new ApexCharts(
    document.querySelector("#chartPedidosEstado"),
    {
      chart: { type: "donut", height: 350 },
      legend: { position: "bottom" },
      labels: [],
      series: [],
    }
  );
  chartPedidosEstado.render();
  forceChartsResize();

  chartPedidosTiempo = new ApexCharts(
    document.querySelector("#chartPedidosTiempo"),
    {
      chart: { type: "line", height: 280 },
      series: [{ name: "Pedidos", data: [] }],
      xaxis: { categories: [] },
      stroke: { width: 3, curve: "smooth" },
    }
  );
  chartPedidosTiempo.render();
  forceChartsResize();

  chartPedidosVendedor = new ApexCharts(
    document.querySelector("#chartPedidosVendedor"),
    {
      chart: { type: "bar", height: 260 },
      plotOptions: { bar: { distributed: true } },
      colors: [
        "#3B82F6",
        "#10B981",
        "#F59E0B",
        "#EF4444",
        "#8B5CF6",
        "#06B6D4",
        "#F43F5E",
      ],
      series: [{ name: "Pedidos", data: [] }],
      xaxis: { categories: [] },
    }
  );
  chartPedidosVendedor.render();
  forceChartsResize();

  chartTopProductos = new ApexCharts(
    document.querySelector("#chartTopProductos"),
    {
      chart: { type: "bar", height: "100%", width: "100%" },
      plotOptions: {
        bar: { horizontal: true, distributed: true, borderRadius: 6 },
      },
      series: [{ name: "Unidades", data: [] }],
      xaxis: { categories: [], labels: { style: { fontSize: "11px" } } },
    }
  );
  chartTopProductos.render();
  forceChartsResize();

  chartVentasLugar = new ApexCharts(
    document.querySelector("#chartVentasLugar"),
    {
      chart: { type: "bar", height: 300 },
      plotOptions: {
        bar: { horizontal: false, distributed: true, columnWidth: "45%" },
      },
      xaxis: {
        categories: [],
        labels: { rotate: -45, style: { fontSize: "11px" } },
      },
      series: [{ name: "Ventas", data: [] }],
    }
  );
  chartVentasLugar.render();
  forceChartsResize();

  // ==========================================
  //  🔥 GRAFICO ANÁLISIS PREDICTIVO — FIX REAL
  // ==========================================
  setTimeout(() => {
    chartAnalisisPredictivo = new ApexCharts(
      document.querySelector("#chartAnalisisPredictivo"),
      {
        chart: { type: "line", height: "100%", width: "100%" },
        series: [{ name: "Pedidos proyectados", data: [] }],
        xaxis: {
          categories: [],
          labels: { rotate: -45, style: { fontSize: "10px" } },
        },
        stroke: { width: 3, curve: "smooth", dashArray: 4 },
        noData: { text: "Cargando proyección..." },
      }
    );

    chartAnalisisPredictivo.render();
    forceChartsResize();
  }, 300); // 👈 ESTA LÍNEA ES LA CLAVE PARA QUE NO SE ROMPA

  // ===============================
  //   C A R G A R   D A T O S
  // ===============================
  actualizarVariablesGlobales();
  cargarIndicadoresCards();
  cargarCiudades();
  cargarPedidosEstado();
  cargarPedidosPorFecha();
  cargarPedidosVendedor();
  cargarProductosMasVendidos();
  cargarVentasPorDepartamento();
  cargarPromedioPedidos();
  cargarDespachoError();
  entrenarModeloPredictivo();
  // Redibujar al terminar de cargar todo el layout
  setTimeout(() => {
    forceChartsResize();
  }, 700);

  // Botón aplicar filtro
  $("#btnAplicarFiltro").on("click", function () {
    actualizarVariablesGlobales();
    cargarIndicadoresCards();
    cargarPedidosEstado();
    cargarPedidosPorFecha();
    cargarPedidosVendedor();
    cargarProductosMasVendidos();
    cargarVentasPorDepartamento();
    cargarPromedioPedidos();
    cargarDespachoError();
    forceChartsResize();
  });
});

function normalizarFecha(f) {
  if (!f) return null;
  const partes = f.split("-");
  if (partes.length !== 3) return null;
  return `${partes[0]}-${partes[1].padStart(2, "0")}-${partes[2].padStart(
    2,
    "0"
  )}`;
}

function cargarCiudades() {
  $.ajax({
    url: "/api/utils/ciudades",
    type: "GET",
    success: function (data) {
      const select = $("#filtroLugar");

      select.empty();
      select.append(`<option value="">Todos</option>`);

      data.forEach((c) => {
        select.append(`<option value="${c.ciudad}">${c.ciudad}</option>`);
      });
    },
    error: function () {
      console.error("Error cargando ciudades");
    },
  });
}

function cargarIndicadoresCards() {
  $.ajax({
    url: `/api/reporte/cards`,
    type: "GET",
    data: {
      inicio: Fechainicio,
      fin: Fechafin,
      lugar: lugar ?? "",
    },
    success: function (data) {
      actualizarCards(data);
    },
    error: function () {
      Swal.fire(
        "Error",
        "Ocurrió un error al obtener los indicadores",
        "error"
      );
    },
  });
}

function actualizarCards(data) {
  $("#cardVentasTotales").text(`S/ ${data.ventasTotales.toFixed(2)}`);
  $("#cardNumeroPedidos").text(data.numeroPedidos);
  $("#cardPromedioPedidosDia").text(data.promedioPedidosPorDia.toFixed(2));
  $("#cardCantidadClientes").text(data.cantidadClientes);
}

function cargarPedidosEstado() {
  $.ajax({
    url: "/api/reporte/pedidos-estado",
    type: "GET",
    data: {
      inicio: Fechainicio,
      fin: Fechafin,
      lugar: lugar,
    },
    success: function (data) {
      actualizarGraficoPedidosEstado(data);
    },
    error: function () {
      console.error("Error consultando pedidos por estado");
    },
  });
}

function actualizarGraficoPedidosEstado(lista) {
  const labels = lista.map((item) => item.descripcion);
  const series = lista.map((item) => item.totalPedidos);

  chartPedidosEstado.updateOptions({ labels, series });
}

function actualizarGraficoPedidosTiempo(data) {
  if (!data || data.length === 0) return;

  // Convertir datos al formato [timestamp, valor]
  const seriesData = data.map((item) => [
    new Date(item.fecha + "T00:00:00").getTime(),
    item.totalPedidos,
  ]);

  // Obtener mes/año para el título

  const fi = new Date(Fechainicio + "T00:00:00");
  const ff = new Date(Fechafin + "T00:00:00");

  const tituloRango = `${fi.toLocaleDateString("es-PE", {
    day: "numeric",
    month: "long",
  })} - ${ff.toLocaleDateString("es-PE", {
    day: "numeric",
    month: "long",
    year: "numeric",
  })}`;

  // Actualizar gráfico
  chartPedidosTiempo.updateOptions({
    title: {
      text: `Pedidos (${tituloRango})`,
      align: "center",
      style: {
        fontSize: "16px",
        fontWeight: "bold",
        color: "#333",
      },
    },

    xaxis: {
      type: "datetime",
      labels: {
        rotate: -45,
        style: { fontSize: "11px" },
        formatter: function (timestamp) {
          return new Date(timestamp).getDate(); // solo día
        },
      },
    },

    series: [
      {
        name: "Pedidos",
        data: seriesData,
      },
    ],

    tooltip: {
      x: {
        formatter: function (timestamp) {
          // convertimos el timestamp a fecha correcta sin desfase
          const fecha = new Date(timestamp);

          return fecha.toLocaleDateString("es-PE", {
            weekday: "long",
            day: "numeric",
            month: "long",
            year: "numeric",
          });
        },
      },
    },

    stroke: {
      width: 3,
      curve: "smooth",
    },

    markers: {
      size: 3,
    },
  });
}

function actualizarGraficoPedidosVendedor(lista) {
  if (!lista || lista.length === 0) {
    chartPedidosVendedor.updateOptions({
      series: [{ name: "Pedidos", data: [] }],
      xaxis: { categories: [] },
    });
    return;
  }

  const categorias = lista.map((item) => item.usuario);
  const valores = lista.map((item) => item.totalPedidos);

  chartPedidosVendedor.updateOptions({
    xaxis: { categories: categorias },
    series: [
      {
        name: "Pedidos",
        data: valores,
      },
    ],
  });
}

function actualizarGraficoTopProductos(lista) {
  // Guarda la lista completa para el tooltip
  topProductosData = lista;

  const categorias = lista.map((item) => item.nombreProductoCorto); // label completo
  const valores = lista.map((item) => item.totalVendido);

  chartTopProductos.updateOptions({
    xaxis: { categories: categorias },

    tooltip: {
      fixed: {
        enabled: true,
        position: "right", // <-- SIEMPRE A LA DERECHA
        offsetX: 30, // <-- ajusta separación horizontal
        offsetY: 0,
      },
      x: {
        formatter: function (_, opts) {
          return topProductosData[opts.dataPointIndex].nombreProducto;
        },
      },
      y: {
        formatter: (val) => val + " unidades",
      },
    },

    series: [{ name: "Unidades", data: valores }],
  });
}

function actualizarGraficoVentasLugar(lista) {
  if (!lista || lista.length === 0) {
    chartVentasLugar.updateOptions({
      xaxis: { categories: [] },
      series: [{ name: "Ventas", data: [] }],
    });
    return;
  }

  // Ordenar de mayor a menor
  lista.sort((a, b) => b.totalPedidos - a.totalPedidos);

  const categorias = lista.map((item) => item.ciudad);
  const valores = lista.map((item) => item.totalPedidos);

  chartVentasLugar.updateOptions({
    xaxis: { categories: categorias },
    series: [
      {
        name: "Ventas",
        data: valores,
      },
    ],
  });
}

function cargarPedidosPorFecha() {
  $.ajax({
    url: "/api/reporte/pedidos-fecha",
    type: "GET",
    data: {
      inicio: Fechainicio,
      fin: Fechafin,
      lugar: lugar ?? "",
    },
    success: function (data) {
      actualizarGraficoPedidosTiempo(data);
    },
    error: function () {
      console.error("Error obteniendo pedidos por fecha");
    },
  });
}

function cargarPedidosVendedor() {
  $.ajax({
    url: "/api/reporte/pedidos-vendedor",
    type: "GET",
    data: {
      inicio: Fechainicio,
      fin: Fechafin,
      lugar: lugar ?? "",
    },
    success: function (data) {
      actualizarGraficoPedidosVendedor(data);
    },
    error: function () {
      console.error("Error obteniendo pedidos por vendedor");
    },
  });
}

function cargarProductosMasVendidos() {
  $.ajax({
    url: "/api/reporte/productos-vendidos",
    type: "GET",
    data: {
      inicio: Fechainicio,
      fin: Fechafin,
      lugar: lugar ?? "",
    },
    success: function (data) {
      console.log(data);
      actualizarGraficoTopProductos(data);
    },
    error: function () {
      console.error("Error obteniendo productos más vendidos");
    },
  });
}

function cargarVentasPorDepartamento() {
  $.ajax({
    url: "/api/reporte/pedidos-departamento",
    type: "GET",
    data: {
      inicio: Fechainicio,
      fin: Fechafin,
    },
    success: function (data) {
      actualizarGraficoVentasLugar(data);
    },
    error: function () {
      console.error("Error obteniendo ventas por departamento");
    },
  });
}

function cargarPromedioPedidos() {
  $.ajax({
    url: "/api/reporte/pedidos-promedio",
    type: "GET",

    success: function (response) {
      const data = response[0];

      if (!data) {
        console.error("La respuesta está vacía o no contiene datos.");
        $("#promedioTotal").text("Sin Data");
        return;
      }

      // 🚨 2. USAR LOS NOMBRES DE CLAVE CORRECTOS
      const paHoras = parseFloat(data.promedio_P_A).toFixed(2);
      const aeHoras = parseFloat(data.promedio_P_E).toFixed(2);
      const totalHoras = parseFloat(data.promedio_Pedido).toFixed(2);

      // 3. Actualizar el DOM
      $("#promedioPA").text(paHoras + " hrs");
      $("#promedioAE").text(aeHoras + " hrs");
      $("#promedioTotal").text(totalHoras + " hrs");
    },
    error: function () {
      console.error("Error obteniendo promedios");
      $("#promedioTotal").text("Error");
    },
  });
}

function cargarDespachoError() {
  $.ajax({
    url: "/api/reporte/despacho-error",
    type: "GET",
    data: {
      inicio: Fechainicio,
      fin: Fechafin,
    },
    success: function (response) {
      const data = response[0];

      if (!data) {
        $("#promedioErrorDespacho").text("Sin Data");
        return;
      }
      const promedioError = parseFloat(data.porcentaje_Error_Despacho).toFixed(
        2
      );

      $("#promedioErrorDespacho").text(promedioError + " %");
    },
    error: function () {
      console.error("Error obteniendo promedios");
      $("#promedioErrorDespacho").text("Error");
    },
  });
}

function entrenarModeloPredictivo() {
  console.log("🔄 Entrenando modelo predictivo...");

  fetch("/api/predictivo/pedidos/entrenar")
    .then((res) => {
      if (!res.ok) {
        throw new Error("HTTP " + res.status);
      }
      return res.text();
    })
    .then((msg) => {
      console.log("✔ Modelo entrenado:", msg);
      cargarAnalisisPredictivo();
    })
    .catch((err) => {
      console.error("❌ Error entrenando modelo:", err);
      Swal.fire({
        icon: "error",
        title: "Error",
        text: "No se pudo entrenar el modelo.",
      });
    });
}

function cargarAnalisisPredictivo() {
  // ⛔ Si el gráfico aún no existe, lo esperamos
  if (!chartAnalisisPredictivo) {
    console.warn("⏳ Esperando a que chartAnalisisPredictivo exista...");
    setTimeout(cargarAnalisisPredictivo, 200);
    return;
  }

  fetch("/api/predictivo/pedidos/proximo-mes/json")
    .then((res) => {
      if (!res.ok) {
        throw new Error("HTTP " + res.status);
      }
      return res.json();
    })
    .then((data) => {
      console.log("📊 Datos análisis predictivo:", data);

      if (!Array.isArray(data)) {
        console.error("Formato inesperado:", data);
        chartAnalisisPredictivo.updateOptions({
          series: [],
          xaxis: { categories: [] },
          noData: { text: "Formato inesperado de datos" },
        });
        return;
      }

      const fechas = data.map((d) => d.fecha);
      const valores = data.map((d) => d.valor);

      chartAnalisisPredictivo.updateOptions({
        xaxis: { categories: fechas },
        series: [{ name: "Pedidos proyectados", data: valores }],
      });
    })
    .catch((err) => {
      console.error("Error cargando análisis predictivo:", err);
      chartAnalisisPredictivo.updateOptions({
        series: [],
        xaxis: { categories: [] },
        noData: { text: "Error al cargar datos" },
      });
    });
}
