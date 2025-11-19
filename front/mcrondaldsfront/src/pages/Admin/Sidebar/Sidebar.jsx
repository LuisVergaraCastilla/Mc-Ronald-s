import React from "react";
import "./Sidebar.css";
import { NavLink } from "react-router-dom";


const Sidebar = () => {
    return (
        <aside className="sidebar">
            <h2 className="sidebar__title">Admin Panel</h2>

            <ul className="sidebar__menu">

        <li>
            <NavLink
            to="/Admin/Productos"
            className={({ isActive }) =>
            isActive ? "sidebar__link active" : "sidebar__link"
            }
        >
        Productos
        </NavLink>
        </li>

        <li>
        <NavLink
            to="/admin/usuarios"
            className={({ isActive }) =>
            isActive ? "sidebar__link active" : "sidebar__link"
            }
        >
        Usuarios
        </NavLink>
        </li>

        <li>
        <NavLink
            to="/admin/pedidos"
            className={({ isActive }) =>
            isActive ? "sidebar__link active" : "sidebar__link"
            }
        >
            Pedidos
        </NavLink>
        </li>

    </ul>
    </aside>
    );
};

export default Sidebar;