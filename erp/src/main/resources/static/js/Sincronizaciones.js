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
      console.log("🌐 Enviando petición a backend Shopify...");

      const resp = await fetch(`/api/shopify/sync`, {
        method: "POST",
      });

      if (!resp.ok) {
        const text = await resp.text();
        console.error("❌ Error HTTP:", resp.status, text);
        throw new Error(`HTTP ${resp.status}: ${text}`);
      }

      const data = await resp.json();
      console.log("✅ Respuesta del backend:", data);

      Swal.fire({
        icon: "success",
        title: "Sincronización completa",
        html: `
          <p>Productos insertados: <b>${data.insertados ?? 0}</b></p>
          <p>Productos actualizados: <b>${data.actualizados ?? 0}</b></p>
        `,
        confirmButtonText: "OK",
      });

      // 🔁 Recargar tabla de productos (si existe la función)
      if (typeof cargarProductos === "function") {
        console.log("🔄 Recargando tabla de productos...");
        cargarProductos();
      } else {
        console.warn("⚠️ Función cargarProductos() no definida.");
      }
    } catch (err) {
      console.error("💥 Error al sincronizar Shopify:", err);
      Swal.fire({
        icon: "error",
        title: "Error",
        text: err.message || "No se pudo sincronizar con Shopify",
      });
    }
  });

document
  .getElementById("btnSyncClientesShopify")
  ?.addEventListener("click", async () => {
    Swal.fire({
      title: "Sincronizando clientes...",
      text: "Espere mientras se actualizan los datos desde Shopify",
      allowOutsideClick: false,
      didOpen: () => Swal.showLoading(),
    });

    try {
      console.log("🌐 Enviando petición a /api/shopify/sync-clientes");

      const resp = await fetch("/api/shopify/sync-clientes", {
        method: "POST",
      });
      if (!resp.ok) throw new Error("Error en la sincronización");

      const data = await resp.json();
      console.log("✅ Respuesta del backend:", data);

      Swal.fire({
        icon: "success",
        title: "Sincronización completa",
        html: `
        <p>Clientes insertados: <b>${data.insertados ?? 0}</b></p>
        <p>Clientes actualizados: <b>${data.actualizados ?? 0}</b></p>
      `,
        confirmButtonText: "OK",
      });

      // 🔁 Recargar la tabla
      if (typeof cargarClientes === "function") {
        console.log("🔄 Recargando lista de clientes...");
        cargarClientes();
      }
    } catch (err) {
      console.error("💥 Error al sincronizar clientes Shopify:", err);
      Swal.fire("Error", "No se pudo sincronizar con Shopify", "error");
    }
  });
