import React from "react";
import "./Header.css";

const Header = () => {
    return (
        <header className="header">
        <div className="container-header">
            <div className="header-item-first">
                <img src="https://1000marcas.net/wp-content/uploads/2019/11/McDonalds-Logo-2003.jpg" className="img"></img>
            </div>
            <h1 className="header-title">
                ¡BIENVENIDO A MC RONALD'S!
            </h1>
        </div>
        </header>
    );
};

export default Header;