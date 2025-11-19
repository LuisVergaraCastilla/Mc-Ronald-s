import React from "react";
import { Outlet } from "react-router-dom";
import Sidebar from "./Sidebar/Sidebar";

const LayoutAdmin = () => {
    return (
        <div style={{ display: "flex" }}>
        <Sidebar />
        <main style={{ marginLeft: "250px", padding: "20px", width: "100%" }}>
            <Outlet />
        </main>
        </div>
    );
};

export default LayoutAdmin;