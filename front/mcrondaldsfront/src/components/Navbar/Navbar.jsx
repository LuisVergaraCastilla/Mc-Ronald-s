import React, { useState } from "react";
import "./Navbar.css";

import { NavLink } from "react-router-dom";

const Navbar = () => {

return (
    <nav className="navbar__container">
        <div className="navbar">
            <ul className="nav__list">
                <li className="list__item">
                    <NavLink
                        to="/combos"
                        className={({ isActive }) =>
                            isActive ? "list__link active" : "list__link"
                        }
                        >
                        Combos
                    </NavLink>
                </li>
                <li className="list__item">
                    <NavLink
                        to="/hamburguesas"
                        className={({ isActive }) =>
                            isActive ? "list__link active" : "list__link"
                        }
                        >
                        Hamburguesas
                    </NavLink>
                </li>
                <li className="list__item">
                    <NavLink
                        to="/bebidas"
                        className={({ isActive }) =>
                            isActive ? "list__link active" : "list__link"
                        }
                        >
                        Bebidas
                    </NavLink>
                </li>
                <li className="list__item">
                    <NavLink
                        to="/postres"
                        className={({ isActive }) =>
                            isActive ? "list__link active" : "list__link"
                        }
                        >
                        Postres
                    </NavLink>
                </li>
            </ul>
        </div>
    </nav>
);
};

export default Navbar;