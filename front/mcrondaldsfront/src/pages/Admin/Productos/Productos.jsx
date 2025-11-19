import React from "react";
import "./Productos.css";
import Sidebar from "../Sidebar/Sidebar";

const Productos = () => {
  // EJEMPLO TEMPORAL de productos (después se conectará al backend)
  const productos = [
    { id: 1, nombre: "Combo Big", precio: 25.90, categoria: "Combos" },
    { id: 2, nombre: "Hamburguesa Deluxe", precio: 18.50, categoria: "Hamburguesas" },
    { id: 3, nombre: "Inka Kola 500ml", precio: 5.00, categoria: "Bebidas" },
  ];

  return (
    <div className="admin-layout">

      <Sidebar />

      <div className="admin-content">
        <h1 className="admin-title">Gestión de Productos</h1>

        <button className="btn-crear">+ Crear Producto</button>

        <table className="tabla-productos">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nombre</th>
              <th>Precio (S/.)</th>
              <th>Categoría</th>
              <th>Acciones</th>
            </tr>
          </thead>

          <tbody>
            {productos.map((p) => (
              <tr key={p.id}>
                <td>{p.id}</td>
                <td>{p.nombre}</td>
                <td>{p.precio.toFixed(2)}</td>
                <td>{p.categoria}</td>
                <td>
                  <button className="btn-editar">Editar</button>
                  <button className="btn-eliminar">Eliminar</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

    </div>
  );
};

export default Productos;