document
  .getElementById("btnSyncShopify")
  ?.addEventListener("click", async () => {
    Swal.fire({
      title: "Sincronizando...",
      text: "Espere mientras se actualizan los productos desde Shopify",
      allowOutsideClick: false,
      didOpen: () => Swal.showLoading(),
    });

    try {
      const resp = await fetch(`${API_BASE}/shopify/sync`, { method: "POST" });
      const data = await resp.json();

      Swal.fire({
        icon: "success",
        title: "Sincronización completa",
        html: `
        <p>Productos insertados: <b>${data.insertados}</b></p>
        <p>Productos actualizados: <b>${data.actualizados}</b></p>
      `,
        confirmButtonText: "OK",
      });

      // 🔁 Recargar tabla de productos
      cargarProductos();
    } catch (err) {
      Swal.fire("Error", "No se pudo sincronizar con Shopify", "error");
    }
  });
