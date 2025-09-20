import pandas as pd
import pyodbc

# === CONFIGURACIÓN DE CONEXIÓN ===
server = 'NOMBRE_SERVIDOR'  # Ejemplo: 'localhost\\SQLEXPRESS'
database = 'ERP_DB'
username = 'tu_usuario'
password = 'tu_contraseña'
driver = '{ODBC Driver 17 for SQL Server}'  # Verifica que lo tengas instalado

conn = pyodbc.connect(
    f'DRIVER={driver};SERVER={server};DATABASE={database};UID={username};PWD={password}'
)
cursor = conn.cursor()

# === CARGAR EXCEL ===
archivo_excel = "BASE 2023 PEDIDOS.xlsx"
df = pd.read_excel(archivo_excel, sheet_name="ENERO")

# Limpiar columnas no deseadas
df = df.loc[:, ~df.columns.str.contains('^Unnamed')]

# Iterar filas y llamar procedimiento almacenado
for _, row in df.iterrows():
    # 1. Llamar GuardarPedido
    cursor.execute("""
        EXEC GuardarPedido ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?
    """,
    int(row["ID_PEDIDO"]),
    int(row["ID_VENDEDOR"]),
    int(row["ID_CLIENTE"]),
    int(row["ID_ESTADO_PEDIDO"]),
    int(row["ID_EMPRESA_ENTREGA"]),
    row["DOCUMENTO"],
    int(row["ID_REGALO"]),
    float(row["SUBTOTAL"]),
    float(row["IGV"]),
    int(row["ADELANTO"]) if not pd.isna(row["ADELANTO"]) else None,
    float(row["MONTO_TOTAL"]),
    row["CIUDAD"],
    row["TIPO_PAGO"],
    row["TIPO_COMPROBANTE"],
    float(row["MONTO_COBRADO"]) if not pd.isna(row["MONTO_COBRADO"]) else None,
    row["OBSERVACION"]
    )

    # 2. Llamar GuardarDetallePedido (puede que tengas varias filas de detalle por pedido)
    cursor.execute("""
        EXEC GuardarDetallePedido ?,?,?,?,?,?,?
    """,
    int(row["ID_DETALLE_P"]),
    int(row["ID_PEDIDO"]),
    int(row["ID_PRODUCTO"]),
    int(row["CANTIDAD"]),
    float(row["PRECIO_UNITARIO"]),
    float(row["PRECIO_TOTAL"]),
    float(row["ID_STOCK"]) if not pd.isna(row["ID_STOCK"]) else None
    )

# Confirmar cambios
conn.commit()
print("✅ Datos cargados correctamente en SQL Server")

cursor.close()
conn.close()
