import { useState } from "react";

export const useToken = () => {
  const [token, setTokenInternal] = useState(() => {
    return localStorage.getItem("jwt");
  });

  const setToken = (newToken) => {
    localStorage.setItem("jwt", newToken);
    setTokenInternal(newToken);
  };

  return [token, setToken];
};
