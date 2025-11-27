let chartPedidosTiempo;
let chartPedidosEstado;
let chartPedidosVendedor;
let chartTopProductos;
let chartVentasLugar;
let topProductosData = [];

let Fechainicio;
let Fechafin;
let lugar;

function actualizarVariablesGlobales() {
  Fechainicio = $("#fecha_inicio").val();
  Fechafin = $("#fecha_fin").val();
  lugar = $("#filtroLugar").val() || null;
}

$(document).ready(function () {
  // Inicializacipon de filtros
  const hoy = new Date();
  const primerDia = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
  const ultimoDia = new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0);

  const formato = (fecha) => fecha.toISOString().split("T")[0];

  $("#fecha_inicio").val(formato(primerDia));
  $("#fecha_fin").val(formato(ultimoDia));

  // Grafico de Estado por Pedido
  chartPedidosEstado = new ApexCharts(
    document.querySelector("#chartPedidosEstado"),
    {
      chart: { type: "donut", height: 260 },
      labels: [],
      series: [],
    }
  );
  chartPedidosEstado.render();

  //Grafido de Pedidos por Fecha

  chartPedidosTiempo = new ApexCharts(
    document.querySelector("#chartPedidosTiempo"),
    {
      chart: { type: "line", height: 280 },
      series: [{ name: "Pedidos", data: [10, 20, 30, 40] }],
      xaxis: { categories: ["Ene", "Feb", "Mar", "Abr"] },
      stroke: { width: 3, curve: "smooth" },
    }
  );
  chartPedidosTiempo.render();

  //Grafido de Pedidos por Vendedor
  chartPedidosVendedor = new ApexCharts(
    document.querySelector("#chartPedidosVendedor"),
    {
      chart: { type: "bar", height: 260 },
      series: [{ name: "Pedidos", data: [] }],
      xaxis: { categories: [] },
      colors: [
        "#3B82F6",
        "#10B981",
        "#F59E0B",
        "#EF4444",
        "#8B5CF6",
        "#06B6D4",
        "#F43F5E",
      ],
    }
  );

  chartPedidosVendedor.render();

  //Grafido de Productos TOP 10
  chartTopProductos = new ApexCharts(
    document.querySelector("#chartTopProductos"),
    {
      chart: { type: "bar", height: 300 },
      plotOptions: {
        bar: {
          horizontal: true,
          distributed: true,
          borderRadius: 6,
        },
      },
      series: [{ name: "Unidades", data: [] }],

      // 🔥 NO FORMATEAR LABELS AQUÍ
      xaxis: {
        categories: [],
        labels: { style: { fontSize: "11px" } },
      },
      colors: [
        "#3B82F6",
        "#10B981",
        "#F59E0B",
        "#EF4444",
        "#8B5CF6",
        "#06B6D4",
        "#F43F5E",
      ],
    }
  );
  chartTopProductos.render();

  chartVentasLugar = new ApexCharts(
    document.querySelector("#chartVentasLugar"),
    {
      chart: { type: "bar", height: 300 },
      plotOptions: {
        bar: {
          horizontal: false,
          columnWidth: "45%",
        },
      },
      xaxis: {
        categories: [],
        labels: {
          rotate: -45,
          style: { fontSize: "11px" },
        },
      },
      series: [{ name: "Ventas", data: [] }],
    }
  );
  chartVentasLugar.render();

  // Inicializando las funciones
  actualizarVariablesGlobales();
  cargarIndicadoresCards();
  cargarCiudades();
  cargarPedidosEstado();
  cargarPedidosPorFecha();
  cargarPedidosVendedor();
  cargarProductosMasVendidos();
  cargarVentasPorDepartamento();

  $("#btnAplicarFiltro").on("click", function () {
    actualizarVariablesGlobales();
    cargarIndicadoresCards();
    cargarPedidosEstado();
    cargarPedidosPorFecha();
    cargarPedidosVendedor();
    cargarProductosMasVendidos();
    cargarVentasPorDepartamento();
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

document.addEventListener("DOMContentLoaded", () => {
  // ==============================
  // ACCIÓN DEL BOTÓN APLICAR FILTRO
  // ==============================

  function cargarDashboard(inicio, fin, lugar) {
    console.log("Cargando dashboard:", inicio, fin, lugar);

    // Cuando tengas API:
    // fetch(`/api/reportes?inicio=${inicio}&fin=${fin}&lugar=${lugar}`)

    // chartPedidosTiempo.updateSeries([{ data: [10, 14, 20, 35, 40, 60] }]);

    // chartTopProductos.updateSeries([
    //   { data: [120, 110, 95, 80, 70, 60, 55, 40, 30, 25] },
    // ]);

    // chartPedidosVendedor.updateSeries([{ data: [80, 70, 65, 55, 40] }]);

    // chartVentasLugar.updateSeries([{ data: [15000, 10000, 8000, 5000, 3000] }]);

    chartAnalisisPredictivo.updateSeries([{ data: [50, 60, 70, 85, 95, 120] }]);
  }

  //  GRÁFICOS REALES SEGÚN TU HTML
  // ============================

  // 1) Pedidos por tiempo (lineal)

  // 2) Top 10 productos más vendidos
  // var chartTopProductos = new ApexCharts(
  //   document.querySelector("#chartTopProductos"),
  //   {
  //     chart: { type: "bar", height: 260 },
  //     series: [{ name: "Unidades", data: [] }],
  //     plotOptions: { bar: { horizontal: true, borderRadius: 6 } },
  //     xaxis: {
  //       categories: [
  //         "Prod1",
  //         "Prod2",
  //         "Prod3",
  //         "Prod4",
  //         "Prod5",
  //         "Prod6",
  //         "Prod7",
  //         "Prod8",
  //         "Prod9",
  //         "Prod10",
  //       ],
  //     },
  //   }
  // );
  // chartTopProductos.render();

  // 3) Pedidos por vendedor
  // var chartPedidosVendedor = new ApexCharts(
  //   document.querySelector("#chartPedidosVendedor"),
  //   {
  //     chart: { type: "bar", height: 260 },
  //     series: [{ name: "Pedidos", data: [] }],
  //     xaxis: { categories: ["Juan", "Ana", "Luis", "Karla", "Pedro"] },
  //     colors: ["#3B82F6"],
  //   }
  // );
  // chartPedidosVendedor.render();

  // 5) Ventas por departamento
  // var chartVentasLugar = new ApexCharts(
  //   document.querySelector("#chartVentasLugar"),
  //   {
  //     chart: { type: "bar", height: 280 },
  //     series: [{ name: "Ventas", data: [15000, 10000, 8000, 5000, 3000] }],
  //     xaxis: { categories: ["Lima", "Arequipa", "Cusco", "Piura", "Junín"] },
  //   }
  // );
  // chartVentasLugar.render();

  // 6) Análisis predictivo
  var chartAnalisisPredictivo = new ApexCharts(
    document.querySelector("#chartAnalisisPredictivo"),
    {
      chart: { type: "line", height: 280 },
      series: [{ name: "Proyección", data: [50, 60, 70, 90, 110, 130] }],
      stroke: { width: 3, curve: "smooth", dashArray: 4 },
    }
  );
  chartAnalisisPredictivo.render();
});
