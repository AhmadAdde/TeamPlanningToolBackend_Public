import React from "react";
import { Navigate } from "react-router-dom";
import { useUser } from "./useUser";

const PrivateRoute = ({ Component }) => {
  const user = useUser();

  return user ? <Component /> : <Navigate to="/" />;
};

export default PrivateRoute;
