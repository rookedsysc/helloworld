import React from "react";
import { useNavigate } from "react-router-dom";
import "./MainPage.css";

export const MainPage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="main-page">
      <h1>Game Event Management</h1>
      <div className="button-container">
        <button className="nav-button" onClick={() => navigate("/record")}>
          Record Game Events
        </button>
        <button className="nav-button" onClick={() => navigate("/history")}>
          View Event History
        </button>
      </div>
    </div>
  );
};
