package com.store.erp.Services;

import com.store.erp.Models.PedidoPredictivoDTO;
import com.store.erp.Repo.ReporteriaRepo;

import org.springframework.stereotype.Service;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.data.Tuple;
import smile.data.type.DataTypes;
import smile.data.type.StructField;
import smile.data.type.StructType;
import smile.data.vector.IntVector;
import smile.regression.RandomForest;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PedidoPredictivoService {

    private final ReporteriaRepo repo;
    private RandomForest modelo;

    public PedidoPredictivoService(ReporteriaRepo repo) {
        this.repo = repo;
    }

    /**
     * Entrena el modelo con TODO el histórico de VW_DATASET_PEDIDOS_DIARIOS
     */
    public void entrenarModelo() {
        List<PedidoPredictivoDTO> data = repo.obtenerDatasetPredictivo();

        if (data == null || data.isEmpty()) {
            throw new IllegalStateException("No hay datos históricos para entrenar el modelo.");
        }

        // Creamos columnas para el DataFrame
        int n = data.size();
        int[] diaSemana = new int[n];
        int[] mes = new int[n];
        int[] diaAnio = new int[n];
        int[] totalPedidos = new int[n];

        for (int i = 0; i < n; i++) {
            PedidoPredictivoDTO d = data.get(i);
            diaSemana[i] = d.getDiaSemana();
            mes[i] = d.getMes();
            diaAnio[i] = d.getDiaAnio();
            totalPedidos[i] = d.getTotalPedidos();
        }

        DataFrame df = DataFrame.of(
                IntVector.of("diaSemana", diaSemana),
                IntVector.of("mes", mes),
                IntVector.of("diaAnio", diaAnio),
                IntVector.of("totalPedidos", totalPedidos));

        // Columna objetivo: totalPedidos
        Formula formula = Formula.lhs("totalPedidos");

        // Entrenamos RandomForest con la nueva API (Formula + DataFrame)
        this.modelo = RandomForest.fit(formula, df);

        System.out.println("✅ Modelo entrenado. Registros usados: " + n);
    }

    /**
     * Predice la cantidad de pedidos para una fecha específica.
     */
    public int predecirParaFecha(LocalDate fecha) {
        if (modelo == null) {
            throw new IllegalStateException("El modelo aún no ha sido entrenado.");
        }

        // Construimos un Tuple con las mismas columnas de entrada que el DataFrame
        StructType schema = DataTypes.struct(
                new StructField("diaSemana", DataTypes.IntegerType),
                new StructField("mes", DataTypes.IntegerType),
                new StructField("diaAnio", DataTypes.IntegerType));

        Tuple row = Tuple.of(new Object[] {
                fecha.getDayOfWeek().getValue(),
                fecha.getMonthValue(),
                fecha.getDayOfYear()
        }, schema);

        double pred = modelo.predict(row);
        return (int) Math.round(pred);
    }

    /**
     * Predice todos los días del próximo mes (por si luego quieres pintar un
     * gráfico).
     */
    public Map<LocalDate, Integer> predecirProximoMes() {
        if (modelo == null) {
            throw new IllegalStateException("El modelo aún no ha sido entrenado.");
        }

        Map<LocalDate, Integer> resultado = new LinkedHashMap<>();

        LocalDate hoy = LocalDate.now();
        LocalDate primerDia = hoy.withDayOfMonth(1).plusMonths(1);
        LocalDate ultimoDia = primerDia.withDayOfMonth(primerDia.lengthOfMonth());

        LocalDate fecha = primerDia;
        while (!fecha.isAfter(ultimoDia)) {
            int pred = predecirParaFecha(fecha);
            resultado.put(fecha, pred);
            fecha = fecha.plusDays(1);
        }

        return resultado;
    }
}