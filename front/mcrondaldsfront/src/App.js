import React, { useState } from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import "./App.css";

import Header from "./components/Header/Header";
import Navbar from "./components/Navbar/Navbar";
import Combos from "./pages/Combos/Combos";

import Sidebar from "./pages/Admin/Sidebar/Sidebar";
import Login from "./pages/Login/Login";

import { CartProvider } from "./context/CartContext";

function App() {
  const [role, setRole] = useState(null);

  return (
    <CartProvider>
      <Routes>

        {/* VISTAS DEL CLIENTE */}
        <Route
          path="/"
          element={
            <div className="flex">
              <Header className="flex-item header" />
              <Navbar className="flex-item navbar" />
              <Combos className="flex-item combos" />
            </div>
          }
        />

        {/* LOGIN */}
        <Route
          path="/login"
          element={<Login onLogin={(r) => setRole(r)} />}
        />

        {/* ADMIN */}
        <Route
          path="/admin/*"
          element={
            role === "ADMIN" ? (
              <div className="admin-layout">
                <Sidebar />
                <div className="admin-content">
                  <h1>Bienvenido Admin</h1>
                </div>
              </div>
            ) : (
              <Navigate to="/login" />
            )
          }
        />

      </Routes>
    </CartProvider>
  );
}

export default App;