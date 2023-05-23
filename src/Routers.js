import { Route, Routes, BrowserRouter } from "react-router-dom";

import WelcomePage from "./components/WelcomePage";
import SignIn from "./sing_in_up/SignIn";
import SingUp from "./sing_in_up/SignUp";
import SignOut from "./sing_in_up/SignOut";
import HomePage from "./components/HomePage";
import PrivateRoute from "./auth/PrivateRoute";

function Routers() {
  return (
    <BrowserRouter>
      <Routes>
        <Route index element={<WelcomePage />} />

        <Route path="signin" element={<SignIn />} />

        <Route path="signup" element={<SingUp />} />

        <Route
          exact
          path="signout"
          element={<PrivateRoute Component={SignOut} />}
        />

        <Route
          exact
          path="homepage"
          element={<PrivateRoute Component={HomePage} />}
        />

        <Route path="*" element={<div>Not Found</div>} />
      </Routes>
    </BrowserRouter>
  );
}

export default Routers;
