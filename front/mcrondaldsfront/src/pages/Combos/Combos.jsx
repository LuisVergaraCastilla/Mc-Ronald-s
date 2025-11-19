import React, { useContext } from "react";
import "./Combos.css";
import { CartContext } from "../../context/CartContext";

const Combos = () => {
    const { addToCart } = useContext(CartContext);


    return (
        <div className="combos__container">
            <h1 className="combos__item combos__title">
                Combos
            </h1>
            <div className="combos__item grid__menu">
                <div className="grid__item">
                    <div>
                        <a className="product-link" href="#">
                            <img className="img-product" src="https://www.donbelisario.com.pe/media/catalog/product/p/d/pdp_brasa-days_personal_1000x1000px_1__1.png?optimize=medium&bg-color=255,255,255&fit=bounds&height=400&width=400&canvas=400:400&format=jpeg"></img>
                        </a>
                    </div>
                    <div className="product-details">
                        <strong className="product-name">
                            <a className="product-item-link" href="#">
                                Brasa Personal
                            </a>
                        </strong>
                        <a className="description-container">
                            <div className="product-item-inner">
                                <div className="product-item-description">
                                    <p>
                                    1/4 pollo, papas fritas,
                                    gaseosa 500ml y salsas.
                                    </p>
                                </div>
                            </div>
                        </a>
                        <a className="price-box product-item">
                            <div className="price-box">
                                <span className="special-price">
                                    S/ 15.90
                                </span>
                                <span className="old-price">
                                    S/ 31.90
                                </span>
                            </div>
                        </a>
                        <div className="product-item-dinner">
                            <div className="product-item-btn">
                                <div className="actions-primary">
                                    <form className="tocart-form">
                                        <div className="fieldset">
                                            <button className="btn-add-tocart">
                                                <span>
                                                    Agregar
                                                </span>
                                            </button>
                                        </div>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div className="grid__item">
                    2
                </div>
                <div className="grid__item">
                    3
                </div>
                <div className="grid__item">
                    4
                </div>
                <div className="grid__item">
                    5
                </div>
                <div className="grid__item">
                    6
                </div>
                <div className="grid__item">
                    7
                </div>
                <div className="grid__item">
                    8
                </div>
                <div className="grid__item">
                    9
                </div>
            </div>
        </div>
    );
};

export default Combos;