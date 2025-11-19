import React, { useState } from "react";
import "./Login.css";
import { useNavigate } from "react-router-dom";

const Login = ({ onLogin }) => {

    const [user, setUser] = useState("");
    const [pass, setPass] = useState("");
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            const response = await fetch("http://localhost:8080/api/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    username: user,
                    password: pass
                })
            });

            if (!response.ok) {
                alert("Credenciales incorrectas");
                return;
            }

            const data = await response.json();

            onLogin(data.role);

            if (data.role === "ADMIN") {
                navigate("/admin");
            } else {
                alert("No tienes permisos para acceder");
            }

        } catch (error) {
            console.error("Error:", error);
            alert("Error en el servidor");
        }
    };

    return (
        <div className="login-container">
            <form className="login-box" onSubmit={handleSubmit}>
                <h2 className="login-title">Bienvenido</h2>
                <p className="login-subtitle">Inicia sesión para continuar</p>

                <div className="input-group">
                    <input
                        type="text"
                        required
                        value={user}
                        onChange={(e) => setUser(e.target.value)}
                    />
                    <label>Usuario</label>
                </div>

                <div className="input-group">
                    <input
                        type="password"
                        required
                        value={pass}
                        onChange={(e) => setPass(e.target.value)}
                    />
                    <label>Contraseña</label>
                </div>

                <button type="submit" className="btn-login">
                    Ingresar
                </button>
            </form>
        </div>
    );
};

export default Login;
